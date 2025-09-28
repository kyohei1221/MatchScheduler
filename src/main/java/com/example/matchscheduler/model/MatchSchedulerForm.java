package com.example.matchscheduler.model;

import java.util.List;

public class MatchSchedulerForm {
    // 6チームの順位（ドロップダウンで選択）
    private List<String> teamOrder;
    
    // 週数（6,7,8）
    private int numberOfWeeks;
    
    // 各週のカード枚数（サイズ＝numberOfWeeks）
    private List<Integer> weekMatchNumbers;
    
    // 固定条件：第1節の固定
    private boolean fixedWeek0;
    private List<Integer> fixedMatchesWeek0;
    
    // 固定条件：最終節の固定
    private boolean fixedFinal;
    private List<Integer> fixedMatchesFinal;
    
    // 固定条件：中間節の固定 3セット
    private boolean fixedMid1;
    private Integer fixedMidWeekIndex1;
    private List<Integer> fixedMatchesMid1;
    
    private boolean fixedMid2;
    private Integer fixedMidWeekIndex2;
    private List<Integer> fixedMatchesMid2;
    
    private boolean fixedMid3;
    private Integer fixedMidWeekIndex3;
    private List<Integer> fixedMatchesMid3;

    // --- Getters and Setters ---
    public List<String> getTeamOrder() {
        return teamOrder;
    }
    public void setTeamOrder(List<String> teamOrder) {
        this.teamOrder = teamOrder;
    }
    public int getNumberOfWeeks() {
        return numberOfWeeks;
    }
    public void setNumberOfWeeks(int numberOfWeeks) {
        this.numberOfWeeks = numberOfWeeks;
    }
    public List<Integer> getWeekMatchNumbers() {
        return weekMatchNumbers;
    }
    public void setWeekMatchNumbers(List<Integer> weekMatchNumbers) {
        this.weekMatchNumbers = weekMatchNumbers;
    }
    public boolean isFixedWeek0() {
        return fixedWeek0;
    }
    public void setFixedWeek0(boolean fixedWeek0) {
        this.fixedWeek0 = fixedWeek0;
    }
    public List<Integer> getFixedMatchesWeek0() {
        return fixedMatchesWeek0;
    }
    public void setFixedMatchesWeek0(List<Integer> fixedMatchesWeek0) {
        this.fixedMatchesWeek0 = fixedMatchesWeek0;
    }
    public boolean isFixedFinal() {
        return fixedFinal;
    }
    public void setFixedFinal(boolean fixedFinal) {
        this.fixedFinal = fixedFinal;
    }
    public List<Integer> getFixedMatchesFinal() {
        return fixedMatchesFinal;
    }
    public void setFixedMatchesFinal(List<Integer> fixedMatchesFinal) {
        this.fixedMatchesFinal = fixedMatchesFinal;
    }
    public boolean isFixedMid1() {
        return fixedMid1;
    }
    public void setFixedMid1(boolean fixedMid1) {
        this.fixedMid1 = fixedMid1;
    }
    public Integer getFixedMidWeekIndex1() {
        return fixedMidWeekIndex1;
    }
    public void setFixedMidWeekIndex1(Integer fixedMidWeekIndex1) {
        this.fixedMidWeekIndex1 = fixedMidWeekIndex1;
    }
    public List<Integer> getFixedMatchesMid1() {
        return fixedMatchesMid1;
    }
    public void setFixedMatchesMid1(List<Integer> fixedMatchesMid1) {
        this.fixedMatchesMid1 = fixedMatchesMid1;
    }
    public boolean isFixedMid2() {
        return fixedMid2;
    }
    public void setFixedMid2(boolean fixedMid2) {
        this.fixedMid2 = fixedMid2;
    }
    public Integer getFixedMidWeekIndex2() {
        return fixedMidWeekIndex2;
    }
    public void setFixedMidWeekIndex2(Integer fixedMidWeekIndex2) {
        this.fixedMidWeekIndex2 = fixedMidWeekIndex2;
    }
    public List<Integer> getFixedMatchesMid2() {
        return fixedMatchesMid2;
    }
    public void setFixedMatchesMid2(List<Integer> fixedMatchesMid2) {
        this.fixedMatchesMid2 = fixedMatchesMid2;
    }
    public boolean isFixedMid3() {
        return fixedMid3;
    }
    public void setFixedMid3(boolean fixedMid3) {
        this.fixedMid3 = fixedMid3;
    }
    public Integer getFixedMidWeekIndex3() {
        return fixedMidWeekIndex3;
    }
    public void setFixedMidWeekIndex3(Integer fixedMidWeekIndex3) {
        this.fixedMidWeekIndex3 = fixedMidWeekIndex3;
    }
    public List<Integer> getFixedMatchesMid3() {
        return fixedMatchesMid3;
    }
    public void setFixedMatchesMid3(List<Integer> fixedMatchesMid3) {
        this.fixedMatchesMid3 = fixedMatchesMid3;
    }
}