package com.haolo.weather.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.haolo.weather.entity.AirQualityRecord;
import com.haolo.weather.mapper.AirQualityRecordMapper;
import com.haolo.weather.service.AirQualityRecordService;
import org.springframework.stereotype.Service;

@Service
public class AirQualityRecordServiceImpl extends ServiceImpl<AirQualityRecordMapper, AirQualityRecord>
        implements AirQualityRecordService {
}
