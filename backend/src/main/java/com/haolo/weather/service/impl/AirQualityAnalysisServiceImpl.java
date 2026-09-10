package com.haolo.weather.service.impl;

import com.haolo.weather.dto.ChinaMapPointDTO;
import com.haolo.weather.dto.RegionComparisonDTO;
import com.haolo.weather.mapper.AirQualityRecordMapper;
import com.haolo.weather.service.AirQualityAnalysisService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class AirQualityAnalysisServiceImpl implements AirQualityAnalysisService {

    private final AirQualityRecordMapper airQualityRecordMapper;
    private final boolean demoDataEnabled;

    public AirQualityAnalysisServiceImpl(AirQualityRecordMapper airQualityRecordMapper,
                                         @Value("${weather.analysis.demo-data-enabled:true}") boolean demoDataEnabled) {
        this.airQualityRecordMapper = airQualityRecordMapper;
        this.demoDataEnabled = demoDataEnabled;
    }

    @Override
    public List<ChinaMapPointDTO> getChinaMapPoints() {
        List<ChinaMapPointDTO> rows = airQualityRecordMapper.selectLatestCityMapPoints();
        if (!rows.isEmpty() || !demoDataEnabled) {
            return rows;
        }
        return demoMapPoints();
    }

    @Override
    public List<RegionComparisonDTO> getRegionComparison() {
        List<RegionComparisonDTO> rows = airQualityRecordMapper.selectRegionComparison();
        if (!rows.isEmpty() || !demoDataEnabled) {
            return rows;
        }
        return demoRegionComparison();
    }

    private List<ChinaMapPointDTO> demoMapPoints() {
        List<ChinaMapPointDTO> list = new ArrayList<>();
        list.add(point("北京", "华北", "北京", 86, 51, 88, "良"));
        list.add(point("天津", "华北", "天津", 112, 71, 122, "轻度污染"));
        list.add(point("河北", "华北", "石家庄", 156, 96, 171, "中度污染"));
        list.add(point("上海", "华东", "上海", 42, 22, 48, "优"));
        list.add(point("江苏", "华东", "南京", 65, 38, 74, "良"));
        list.add(point("浙江", "华东", "杭州", 58, 31, 64, "良"));
        list.add(point("广东", "华南", "广州", 39, 19, 43, "优"));
        list.add(point("广西", "华南", "南宁", 55, 28, 62, "良"));
        list.add(point("湖北", "华中", "武汉", 94, 56, 99, "良"));
        list.add(point("湖南", "华中", "长沙", 78, 44, 85, "良"));
        list.add(point("四川", "西南", "成都", 121, 76, 137, "轻度污染"));
        list.add(point("重庆", "西南", "重庆", 107, 66, 118, "轻度污染"));
        list.add(point("陕西", "西北", "西安", 168, 104, 183, "中度污染"));
        list.add(point("甘肃", "西北", "兰州", 132, 82, 149, "轻度污染"));
        list.add(point("辽宁", "东北", "沈阳", 88, 52, 91, "良"));
        list.add(point("黑龙江", "东北", "哈尔滨", 72, 41, 80, "良"));
        return list;
    }

    private ChinaMapPointDTO point(String province, String region, String city, int aqi, int pm25, int pm10, String level) {
        ChinaMapPointDTO dto = new ChinaMapPointDTO();
        dto.setProvince(province);
        dto.setRegion(region);
        dto.setCity(city);
        dto.setAqi(aqi);
        dto.setPm25(BigDecimal.valueOf(pm25));
        dto.setPm10(BigDecimal.valueOf(pm10));
        dto.setQualityLevel(level);
        dto.setMonitorTime(LocalDateTime.now().withSecond(0).withNano(0));
        dto.setWarning(aqi >= 150);
        return dto;
    }

    private List<RegionComparisonDTO> demoRegionComparison() {
        List<RegionComparisonDTO> list = new ArrayList<>();
        list.add(region("华北", 118, 73, 127, 24, 11, 13, 45.8, 4));
        list.add(region("华东", 57, 32, 65, 36, 31, 5, 86.1, 0));
        list.add(region("华南", 49, 24, 55, 18, 17, 1, 94.4, 0));
        list.add(region("华中", 86, 50, 92, 16, 11, 5, 68.8, 1));
        list.add(region("西南", 114, 71, 128, 20, 8, 12, 40.0, 2));
        list.add(region("西北", 145, 90, 162, 17, 5, 12, 29.4, 5));
        list.add(region("东北", 80, 47, 86, 14, 10, 4, 71.4, 1));
        return list;
    }

    private RegionComparisonDTO region(String region, int avgAqi, int avgPm25, int avgPm10,
                                       int cityCount, int goodCityCount, int pollutedCityCount,
                                       double goodRate, int warningCount) {
        RegionComparisonDTO dto = new RegionComparisonDTO();
        dto.setRegion(region);
        dto.setAvgAqi(BigDecimal.valueOf(avgAqi));
        dto.setAvgPm25(BigDecimal.valueOf(avgPm25));
        dto.setAvgPm10(BigDecimal.valueOf(avgPm10));
        dto.setCityCount(cityCount);
        dto.setGoodCityCount(goodCityCount);
        dto.setPollutedCityCount(pollutedCityCount);
        dto.setGoodRate(BigDecimal.valueOf(goodRate));
        dto.setWarningCount(warningCount);
        return dto;
    }
}
