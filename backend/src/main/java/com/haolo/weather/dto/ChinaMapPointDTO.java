package com.haolo.weather.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class ChinaMapPointDTO {

    private String province;
    private String region;
    private String city;
    private Integer aqi;
    private BigDecimal pm25;
    private BigDecimal pm10;
    private String qualityLevel;
    private LocalDateTime monitorTime;
    private Boolean warning;

    public String getProvince() {
        return province;
    }

    public void setProvince(String province) {
        this.province = province;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public Integer getAqi() {
        return aqi;
    }

    public void setAqi(Integer aqi) {
        this.aqi = aqi;
    }

    public BigDecimal getPm25() {
        return pm25;
    }

    public void setPm25(BigDecimal pm25) {
        this.pm25 = pm25;
    }

    public BigDecimal getPm10() {
        return pm10;
    }

    public void setPm10(BigDecimal pm10) {
        this.pm10 = pm10;
    }

    public String getQualityLevel() {
        return qualityLevel;
    }

    public void setQualityLevel(String qualityLevel) {
        this.qualityLevel = qualityLevel;
    }

    public LocalDateTime getMonitorTime() {
        return monitorTime;
    }

    public void setMonitorTime(LocalDateTime monitorTime) {
        this.monitorTime = monitorTime;
    }

    public Boolean getWarning() {
        return warning;
    }

    public void setWarning(Boolean warning) {
        this.warning = warning;
    }
}
