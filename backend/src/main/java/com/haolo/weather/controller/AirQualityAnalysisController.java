package com.haolo.weather.controller;

import com.haolo.weather.dto.ChinaMapPointDTO;
import com.haolo.weather.dto.RegionComparisonDTO;
import com.haolo.weather.service.AirQualityAnalysisService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/analysis")
public class AirQualityAnalysisController {

    private final AirQualityAnalysisService airQualityAnalysisService;

    public AirQualityAnalysisController(AirQualityAnalysisService airQualityAnalysisService) {
        this.airQualityAnalysisService = airQualityAnalysisService;
    }

    @GetMapping("/china-map")
    public List<ChinaMapPointDTO> chinaMap() {
        return airQualityAnalysisService.getChinaMapPoints();
    }

    @GetMapping("/region-comparison")
    public List<RegionComparisonDTO> regionComparison() {
        return airQualityAnalysisService.getRegionComparison();
    }
}
