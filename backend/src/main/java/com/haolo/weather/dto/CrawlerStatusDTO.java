package com.haolo.weather.dto;

import java.time.LocalDateTime;

public class CrawlerStatusDTO {

    private boolean running;
    private boolean lastSuccess;
    private Integer lastCount;
    private String lastMessage;
    private LocalDateTime lastRunTime;

    public boolean isRunning() {
        return running;
    }

    public void setRunning(boolean running) {
        this.running = running;
    }

    public boolean isLastSuccess() {
        return lastSuccess;
    }

    public void setLastSuccess(boolean lastSuccess) {
        this.lastSuccess = lastSuccess;
    }

    public Integer getLastCount() {
        return lastCount;
    }

    public void setLastCount(Integer lastCount) {
        this.lastCount = lastCount;
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
    }

    public LocalDateTime getLastRunTime() {
        return lastRunTime;
    }

    public void setLastRunTime(LocalDateTime lastRunTime) {
        this.lastRunTime = lastRunTime;
    }
}
