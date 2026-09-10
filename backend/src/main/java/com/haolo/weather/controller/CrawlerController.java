package com.haolo.weather.controller;

import com.haolo.weather.dto.CrawlerStatusDTO;
import com.haolo.weather.task.AirQualityCrawlerTask;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/crawler")
public class CrawlerController {

    private final AirQualityCrawlerTask airQualityCrawlerTask;

    public CrawlerController(AirQualityCrawlerTask airQualityCrawlerTask) {
        this.airQualityCrawlerTask = airQualityCrawlerTask;
    }

    @PostMapping("/run")
    public CrawlerStatusDTO run() {
        return airQualityCrawlerTask.runOnce();
    }

    @GetMapping("/status")
    public CrawlerStatusDTO status() {
        return airQualityCrawlerTask.getStatus();
    }
}
