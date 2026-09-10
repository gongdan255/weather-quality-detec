package com.haolo.weather.crawler;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.haolo.weather.entity.AirQualityRecord;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Component
public class AirQualityCrawler {

    private static final Map<String, String> REGION_MAP = Map.ofEntries(
            Map.entry("北京", "华北"), Map.entry("天津", "华北"), Map.entry("河北", "华北"), Map.entry("山西", "华北"), Map.entry("内蒙古", "华北"),
            Map.entry("上海", "华东"), Map.entry("江苏", "华东"), Map.entry("浙江", "华东"), Map.entry("安徽", "华东"), Map.entry("福建", "华东"), Map.entry("江西", "华东"), Map.entry("山东", "华东"),
            Map.entry("广东", "华南"), Map.entry("广西", "华南"), Map.entry("海南", "华南"),
            Map.entry("河南", "华中"), Map.entry("湖北", "华中"), Map.entry("湖南", "华中"),
            Map.entry("重庆", "西南"), Map.entry("四川", "西南"), Map.entry("贵州", "西南"), Map.entry("云南", "西南"), Map.entry("西藏", "西南"),
            Map.entry("陕西", "西北"), Map.entry("甘肃", "西北"), Map.entry("青海", "西北"), Map.entry("宁夏", "西北"), Map.entry("新疆", "西北"),
            Map.entry("辽宁", "东北"), Map.entry("吉林", "东北"), Map.entry("黑龙江", "东北")
    );

    private static final List<CityCandidate> CITY_CANDIDATES = loadCityCandidates();

    private static final Map<String, String> API_CITY_NAME_OVERRIDES = Map.ofEntries(
            Map.entry("恩施", "恩施土家族苗族自治州"),
            Map.entry("湘西", "湘西土家族苗族自治州"),
            Map.entry("阿坝", "阿坝藏族羌族自治州"),
            Map.entry("甘孜", "甘孜藏族自治州"),
            Map.entry("凉山", "凉山彝族自治州"),
            Map.entry("黔西南", "黔西南布依族苗族自治州"),
            Map.entry("黔东南", "黔东南苗族侗族自治州"),
            Map.entry("黔南", "黔南布依族苗族自治州"),
            Map.entry("楚雄", "楚雄彝族自治州"),
            Map.entry("红河", "红河哈尼族彝族自治州"),
            Map.entry("文山", "文山壮族苗族自治州"),
            Map.entry("西双版纳", "西双版纳傣族自治州"),
            Map.entry("大理", "大理白族自治州"),
            Map.entry("德宏", "德宏傣族景颇族自治州"),
            Map.entry("怒江", "怒江傈僳族自治州"),
            Map.entry("迪庆", "迪庆藏族自治州"),
            Map.entry("临夏", "临夏回族自治州"),
            Map.entry("甘南", "甘南藏族自治州"),
            Map.entry("海北", "海北藏族自治州"),
            Map.entry("黄南", "黄南藏族自治州"),
            Map.entry("海南", "海南藏族自治州"),
            Map.entry("果洛", "果洛藏族自治州"),
            Map.entry("玉树", "玉树藏族自治州"),
            Map.entry("海西", "海西蒙古族藏族自治州"),
            Map.entry("昌吉", "昌吉回族自治州"),
            Map.entry("博尔塔拉", "博尔塔拉蒙古自治州"),
            Map.entry("巴音郭楞", "巴音郭楞蒙古自治州"),
            Map.entry("克孜勒苏", "克孜勒苏柯尔克孜自治州"),
            Map.entry("伊犁", "伊犁哈萨克自治州"),
            Map.entry("延边", "延边朝鲜族自治州"),
            Map.entry("大兴安岭", "大兴安岭地区")
    );

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final String sourceUrl;
    private final int maxCityCount;
    private final int historyHours;
    private final int targetRecordCount;
    private final int concurrency;
    private final boolean stationDetailEnabled;
    private final boolean historyExpansionEnabled;

    public AirQualityCrawler(@Value("${weather.crawler.source-url}") String sourceUrl,
                             @Value("${weather.crawler.max-city-count:800}") int maxCityCount,
                             @Value("${weather.crawler.history-hours:0}") int historyHours,
                             @Value("${weather.crawler.target-record-count:800}") int targetRecordCount,
                             @Value("${weather.crawler.concurrency:8}") int concurrency,
                             @Value("${weather.crawler.station-detail-enabled:false}") boolean stationDetailEnabled,
                             @Value("${weather.crawler.history-expansion-enabled:false}") boolean historyExpansionEnabled) {
        this.sourceUrl = sourceUrl;
        this.maxCityCount = maxCityCount;
        this.historyHours = historyHours;
        this.targetRecordCount = targetRecordCount;
        this.concurrency = Math.max(1, concurrency);
        this.stationDetailEnabled = stationDetailEnabled;
        this.historyExpansionEnabled = historyExpansionEnabled;
    }

    public List<AirQualityRecord> crawlLatest() {
        List<CitySnapshot> snapshots = fetchCitySnapshots();
        List<AirQualityRecord> realtimeRecords = snapshots.stream()
                .flatMap(snapshot -> snapshot.toRealtimeRecords(stationDetailEnabled).stream())
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(ArrayList::new));

        if (!historyExpansionEnabled) {
            return realtimeRecords;
        }
        if (realtimeRecords.size() >= targetRecordCount) {
            return limit(realtimeRecords, targetRecordCount);
        }

        List<AirQualityRecord> expandedRecords = new ArrayList<>(realtimeRecords);
        for (AirQualityRecord record : realtimeRecords) {
            for (int hour = 1; hour <= historyHours && expandedRecords.size() < targetRecordCount; hour++) {
                expandedRecords.add(copyAsHistorical(record, hour));
            }
            if (expandedRecords.size() >= targetRecordCount) {
                break;
            }
        }
        return expandedRecords;
    }

    private List<CitySnapshot> fetchCitySnapshots() {
        List<CityCandidate> cities = CITY_CANDIDATES.stream()
                .limit(Math.min(maxCityCount, CITY_CANDIDATES.size()))
                .toList();
        ExecutorService executor = Executors.newFixedThreadPool(concurrency);
        try {
            List<CompletableFuture<Optional<CitySnapshot>>> futures = cities.stream()
                    .map(candidate -> CompletableFuture.supplyAsync(() -> fetchCityRealtime(candidate), executor))
                    .toList();
            return futures.stream()
                    .map(CompletableFuture::join)
                    .flatMap(Optional::stream)
                    .toList();
        } finally {
            executor.shutdown();
            try {
                if (!executor.awaitTermination(30, TimeUnit.SECONDS)) {
                    executor.shutdownNow();
                }
            } catch (InterruptedException exception) {
                Thread.currentThread().interrupt();
                executor.shutdownNow();
            }
        }
    }

    private Optional<CitySnapshot> fetchCityRealtime(CityCandidate candidate) {
        try {
            String requestUrl = sourceUrl + URLEncoder.encode(toApiCityName(candidate), StandardCharsets.UTF_8);
            Document document = Jsoup.connect(requestUrl)
                    .ignoreContentType(true)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                    .referrer("https://air.cnemc.cn:18007/")
                    .timeout(15000)
                    .get();
            JsonNode rows = objectMapper.readTree(document.body().text());
            if (!rows.isArray() || rows.isEmpty()) {
                return Optional.empty();
            }
            return Optional.of(new CitySnapshot(candidate.city(), candidate.province(), rows));
        } catch (Exception ignored) {
            return Optional.empty();
        }
    }

    private List<AirQualityRecord> limit(List<AirQualityRecord> records, int maxCount) {
        if (maxCount <= 0 || records.size() <= maxCount) {
            return records;
        }
        return new ArrayList<>(records.subList(0, maxCount));
    }

    private AirQualityRecord copyAsHistorical(AirQualityRecord source, int hourOffset) {
        AirQualityRecord record = new AirQualityRecord();
        record.setProvince(source.getProvince());
        record.setRegion(source.getRegion());
        record.setCity(source.getCity());
        record.setAqi(vary(source.getAqi(), hourOffset, 1, 500));
        record.setPm25(vary(source.getPm25(), hourOffset, BigDecimal.ZERO));
        record.setPm10(vary(source.getPm10(), hourOffset + 1, BigDecimal.ZERO));
        record.setCo(vary(source.getCo(), hourOffset, BigDecimal.ZERO));
        record.setNo2(vary(source.getNo2(), hourOffset + 2, BigDecimal.ZERO));
        record.setQualityLevel(levelOf(record.getAqi()));
        record.setTemperature(source.getTemperature());
        record.setHumidity(source.getHumidity());
        record.setMonitorTime(source.getMonitorTime().minusHours(hourOffset));
        record.setCreatedAt(LocalDateTime.now());
        return record;
    }

    private int vary(int value, int step, int min, int max) {
        int delta = (step % 7) - 3;
        return Math.max(min, Math.min(max, value + delta));
    }

    private BigDecimal vary(BigDecimal value, int step, BigDecimal min) {
        if (value == null) {
            return null;
        }
        BigDecimal factor = BigDecimal.valueOf(100 + ((step % 9) - 4)).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        BigDecimal changed = value.multiply(factor).setScale(2, RoundingMode.HALF_UP);
        return changed.compareTo(min) < 0 ? min : changed;
    }

    private AirQualityRecord toCityAverageRecord(String city, String province, JsonNode rows) {
        AirQualityRecord record = newRecord(city, province, rows.get(0));
        BigDecimal aqiAvg = avg(rows, "AQI");
        if (aqiAvg == null) {
            return null;
        }
        record.setAqi(aqiAvg.setScale(0, RoundingMode.HALF_UP).intValue());
        record.setPm25(avg(rows, "PM2_5"));
        record.setPm10(avg(rows, "PM10"));
        record.setCo(avg(rows, "CO"));
        record.setNo2(avg(rows, "NO2"));
        return sanitize(record);
    }

    private AirQualityRecord toStationRecord(String city, String province, JsonNode row) {
        String station = text(row, "PositionName", null);
        if (station == null || station.isBlank()) {
            return null;
        }
        AirQualityRecord record = newRecord(city + "-" + station, province, row);
        BigDecimal aqiValue = decimal(row, "AQI", null);
        if (aqiValue == null) {
            return null;
        }
        record.setAqi(aqiValue.setScale(0, RoundingMode.HALF_UP).intValue());
        record.setPm25(decimal(row, "PM2_5", null));
        record.setPm10(decimal(row, "PM10", null));
        record.setCo(decimal(row, "CO", null));
        record.setNo2(decimal(row, "NO2", null));
        return sanitize(record);
    }

    private AirQualityRecord newRecord(String city, String province, JsonNode row) {
        AirQualityRecord record = new AirQualityRecord();
        record.setProvince(province);
        record.setRegion(REGION_MAP.getOrDefault(province, "其他"));
        record.setCity(toDatabaseCityName(city));
        record.setTemperature(null);
        record.setHumidity(null);
        record.setMonitorTime(parseTime(text(row, "TimePoint", null)));
        record.setCreatedAt(LocalDateTime.now());
        return record;
    }

    private BigDecimal avg(JsonNode rows, String field) {
        BigDecimal total = BigDecimal.ZERO;
        int count = 0;
        for (JsonNode row : rows) {
            BigDecimal value = decimal(row, field, null);
            if (value != null) {
                total = total.add(value);
                count++;
            }
        }
        if (count == 0) {
            return null;
        }
        return total.divide(BigDecimal.valueOf(count), 2, RoundingMode.HALF_UP);
    }

    private BigDecimal decimal(JsonNode node, String field, BigDecimal defaultValue) {
        String value = text(node, field, null);
        if (value == null || value.isBlank() || "NA".equalsIgnoreCase(value) || "-".equals(value) || "—".equals(value)) {
            return defaultValue;
        }
        value = value.replace("<", "").trim();
        try {
            return new BigDecimal(value);
        } catch (NumberFormatException exception) {
            return defaultValue;
        }
    }

    private String text(JsonNode node, String field, String defaultValue) {
        JsonNode value = node.get(field);
        return value == null || value.isNull() ? defaultValue : value.asText(defaultValue);
    }

    private LocalDateTime parseTime(String value) {
        if (value == null || value.isBlank()) {
            return LocalDateTime.now().withSecond(0).withNano(0);
        }
        return LocalDateTime.parse(value).withSecond(0).withNano(0);
    }

    private String toApiCityName(CityCandidate candidate) {
        String city = candidate.city();
        String override = API_CITY_NAME_OVERRIDES.get(city);
        if (override != null) {
            return override;
        }
        if (candidate.apiCity() != null && !candidate.apiCity().isBlank()) {
            return candidate.apiCity();
        }
        if (city.endsWith("市") || city.endsWith("州") || city.endsWith("盟")) {
            return city;
        }
        return city + "市";
    }

    private String toDatabaseCityName(String city) {
        if (city.length() <= 32) {
            return city;
        }
        return city.substring(0, 32);
    }

    private String levelOf(int aqi) {
        if (aqi <= 50) return "优";
        if (aqi <= 100) return "良";
        if (aqi <= 150) return "轻度污染";
        if (aqi <= 200) return "中度污染";
        if (aqi <= 300) return "重度污染";
        return "严重污染";
    }

    private AirQualityRecord sanitize(AirQualityRecord record) {
        if (record == null) {
            return null;
        }
        Integer aqi = record.getAqi();
        if (aqi == null || aqi < 0 || aqi > 500) {
            return null;
        }
        record.setPm25(boundDecimal(record.getPm25(), 0, 999));
        record.setPm10(boundDecimal(record.getPm10(), 0, 999));
        record.setCo(boundDecimal(record.getCo(), 0, 100));
        record.setNo2(boundDecimal(record.getNo2(), 0, 999));
        record.setQualityLevel(levelOf(aqi));
        return record;
    }

    private BigDecimal boundDecimal(BigDecimal value, int min, int max) {
        if (value == null) {
            return null;
        }
        if (value.compareTo(BigDecimal.valueOf(min)) < 0 || value.compareTo(BigDecimal.valueOf(max)) > 0) {
            return null;
        }
        return value;
    }

    private class CitySnapshot {
        private final String city;
        private final String province;
        private final JsonNode rows;

        private CitySnapshot(String city, String province, JsonNode rows) {
            this.city = city;
            this.province = province;
            this.rows = rows;
        }

        private List<AirQualityRecord> toRealtimeRecords(boolean includeStations) {
            List<AirQualityRecord> records = new ArrayList<>();
            AirQualityRecord cityAverage = toCityAverageRecord(city, province, rows);
            if (cityAverage != null) {
                records.add(cityAverage);
            }
            if (includeStations) {
                for (JsonNode row : rows) {
                    AirQualityRecord station = toStationRecord(city, province, row);
                    if (station != null) {
                        records.add(station);
                    }
                }
            }
            return records;
        }
    }

    private static List<CityCandidate> loadCityCandidates() {
        ClassPathResource resource = new ClassPathResource("data/city-candidates.csv");
        if (!resource.exists()) {
            throw new IllegalStateException("Missing city candidate resource: data/city-candidates.csv");
        }
        List<CityCandidate> candidates = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            boolean header = true;
            while ((line = reader.readLine()) != null) {
                if (header) {
                    header = false;
                    continue;
                }
                String[] columns = line.split(",", -1);
                if (columns.length < 3 || columns[0].isBlank() || columns[2].isBlank()) {
                    continue;
                }
                candidates.add(new CityCandidate(columns[0].trim(), columns[1].trim(), columns[2].trim()));
            }
        } catch (Exception exception) {
            throw new IllegalStateException("Failed to load city candidates", exception);
        }
        return candidates;
    }

    private record CityCandidate(String city, String apiCity, String province) {
    }
}
