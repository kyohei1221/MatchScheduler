package com.example.matchscheduler.db;

import java.time.LocalDateTime;

public class Bookmark {
    private long id; // ID
    private LocalDateTime createdAt; // 作成日時
    private String memo; // メモ
    private String scheduleJson; // teamOrder, numberOfWeeks, weekMatches を含む JSON

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public String getMemo() { return memo; }
    public void setMemo(String memo) { this.memo = memo; }

    public String getScheduleJson() { return scheduleJson; }
    public void setScheduleJson(String scheduleJson) { this.scheduleJson = scheduleJson; }
}
