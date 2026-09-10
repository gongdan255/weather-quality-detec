package com.haolo.weather.dto;

import java.math.BigDecimal;

public class RegionComparisonDTO {

    private String region;
    private BigDecimal avgAqi;
    private BigDecimal avgPm25;
    private BigDecimal avgPm10;
    private Integer cityCount;
    private Integer goodCityCount;
    private Integer pollutedCityCount;
    private BigDecimal goodRate;
    private Integer warningCount;

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public BigDecimal getAvgAqi() {
        return avgAqi;
    }

    public void setAvgAqi(BigDecimal avgAqi) {
        this.avgAqi = avgAqi;
    }

    public BigDecimal getAvgPm25() {
        return avgPm25;
    }

    public void setAvgPm25(BigDecimal avgPm25) {
        this.avgPm25 = avgPm25;
    }

    public BigDecimal getAvgPm10() {
        return avgPm10;
    }

    public void setAvgPm10(BigDecimal avgPm10) {
        this.avgPm10 = avgPm10;
    }

    public Integer getCityCount() {
        return cityCount;
    }

    public void setCityCount(Integer cityCount) {
        this.cityCount = cityCount;
    }

    public Integer getGoodCityCount() {
        return goodCityCount;
    }

    public void setGoodCityCount(Integer goodCityCount) {
        this.goodCityCount = goodCityCount;
    }

    public Integer getPollutedCityCount() {
        return pollutedCityCount;
    }

    public void setPollutedCityCount(Integer pollutedCityCount) {
        this.pollutedCityCount = pollutedCityCount;
    }

    public BigDecimal getGoodRate() {
        return goodRate;
    }

    public void setGoodRate(BigDecimal goodRate) {
        this.goodRate = goodRate;
    }

    public Integer getWarningCount() {
        return warningCount;
    }

    public void setWarningCount(Integer warningCount) {
        this.warningCount = warningCount;
    }
}
