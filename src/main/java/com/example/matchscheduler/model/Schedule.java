package com.example.matchscheduler.model;

import java.util.List;

public class Schedule {
    // 各週の試合IDリストのリスト
    private List<List<Integer>> weekMatches;

    public Schedule(List<List<Integer>> weekMatches) {
        this.weekMatches = weekMatches;
    }

    public List<List<Integer>> getWeekMatches() {
        return weekMatches;
    }

    public void setWeekMatches(List<List<Integer>> weekMatches) {
        this.weekMatches = weekMatches;
    }
}