package com.haolo.weather.task;

import com.haolo.weather.crawler.AirQualityCrawler;
import com.haolo.weather.dto.CrawlerStatusDTO;
import com.haolo.weather.entity.AirQualityRecord;
import com.haolo.weather.service.AirQualityRecordService;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

@Component
public class AirQualityCrawlerTask {

    private final AirQualityCrawler airQualityCrawler;
    private final AirQualityRecordService airQualityRecordService;
    private final boolean enabled;
    private final AtomicBoolean running = new AtomicBoolean(false);
    private final CrawlerStatusDTO status = new CrawlerStatusDTO();

    public AirQualityCrawlerTask(AirQualityCrawler airQualityCrawler,
                                 AirQualityRecordService airQualityRecordService,
                                 @Value("${weather.crawler.enabled:true}") boolean enabled) {
        this.airQualityCrawler = airQualityCrawler;
        this.airQualityRecordService = airQualityRecordService;
        this.enabled = enabled;
        status.setLastCount(0);
        status.setLastMessage("爬虫尚未执行");
    }

    @Scheduled(cron = "${weather.crawler.cron}")
    public void scheduledRun() {
        if (enabled) {
            runOnce();
        }
    }

    public CrawlerStatusDTO runOnce() {
        if (!running.compareAndSet(false, true)) {
            status.setRunning(true);
            status.setLastMessage("爬虫任务正在执行，请稍后再试");
            return status;
        }
        status.setRunning(true);
        status.setLastRunTime(LocalDateTime.now());
        try {
            List<AirQualityRecord> records = airQualityCrawler.crawlLatest();
            if (!records.isEmpty()) {
                saveIgnoringDuplicates(records);
            }
            status.setLastSuccess(true);
            status.setLastCount(records.size());
            status.setLastMessage("采集完成，已入库 " + records.size() + " 条空气质量数据");
        } catch (Exception exception) {
            status.setLastSuccess(false);
            status.setLastCount(0);
            status.setLastMessage("采集失败：" + exception.getMessage());
        } finally {
            running.set(false);
            status.setRunning(false);
        }
        return status;
    }

    public CrawlerStatusDTO getStatus() {
        status.setRunning(running.get());
        return status;
    }

    private void saveIgnoringDuplicates(List<AirQualityRecord> records) {
        for (AirQualityRecord record : records) {
            try {
                airQualityRecordService.save(record);
            } catch (DuplicateKeyException ignored) {
                // Same city and monitor time has already been collected.
            }
        }
    }
}
