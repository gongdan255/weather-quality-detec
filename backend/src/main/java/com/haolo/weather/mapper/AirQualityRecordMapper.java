package com.haolo.weather.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.haolo.weather.dto.ChinaMapPointDTO;
import com.haolo.weather.dto.RegionComparisonDTO;
import com.haolo.weather.entity.AirQualityRecord;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface AirQualityRecordMapper extends BaseMapper<AirQualityRecord> {

    List<ChinaMapPointDTO> selectLatestCityMapPoints();

    List<RegionComparisonDTO> selectRegionComparison();
}
