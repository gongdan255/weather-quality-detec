CREATE DATABASE IF NOT EXISTS weather_analysis DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE weather_analysis;

CREATE TABLE IF NOT EXISTS air_quality_record (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
    province VARCHAR(32) NOT NULL COMMENT '省份',
    region VARCHAR(16) NOT NULL COMMENT '区域：华北、华东、华南、华中、西南、西北、东北',
    city VARCHAR(32) NOT NULL COMMENT '城市',
    aqi INT NOT NULL COMMENT 'AQI指数',
    pm25 DECIMAL(8, 2) COMMENT 'PM2.5',
    pm10 DECIMAL(8, 2) COMMENT 'PM10',
    co DECIMAL(8, 2) COMMENT '一氧化碳',
    no2 DECIMAL(8, 2) COMMENT '二氧化氮',
    quality_level VARCHAR(16) NOT NULL COMMENT '空气质量等级',
    temperature DECIMAL(5, 2) COMMENT '温度',
    humidity DECIMAL(5, 2) COMMENT '湿度',
    monitor_time DATETIME NOT NULL COMMENT '监测时间',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '入库时间',
    INDEX idx_city_time (city, monitor_time),
    INDEX idx_region_time (region, monitor_time),
    INDEX idx_aqi (aqi),
    UNIQUE KEY uk_city_time (city, monitor_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='空气质量采集记录表';
