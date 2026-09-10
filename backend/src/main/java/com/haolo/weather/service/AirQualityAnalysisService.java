package com.haolo.weather.service;

import com.haolo.weather.dto.ChinaMapPointDTO;
import com.haolo.weather.dto.RegionComparisonDTO;

import java.util.List;

public interface AirQualityAnalysisService {

    List<ChinaMapPointDTO> getChinaMapPoints();

    List<RegionComparisonDTO> getRegionComparison();
}
