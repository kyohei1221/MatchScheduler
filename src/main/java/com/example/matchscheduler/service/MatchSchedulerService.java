package com.example.matchscheduler.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.example.matchscheduler.model.MatchSchedulerForm;
import com.example.matchscheduler.model.Schedule;

public class MatchSchedulerService {

    // 6チーム総当たりの15試合定義
    private final int[][] MATCHES = {
        {0, 1}, {0, 2}, {0, 3}, {0, 4}, {0, 5},
        {1, 2}, {1, 3}, {1, 4}, {1, 5},
        {2, 3}, {2, 4}, {2, 5},
        {3, 4}, {3, 5},
        {4, 5}
    };
    private final int MATCH_COUNT = MATCHES.length;

    // 入力パラメータ
    private List<String> teams; // 順位順のチーム名（サイズ6）
    private int[] weekMatchNumber; // 各週のカード枚数
    private int numberOfWeeks;

    // 固定条件：第1節固定
    private boolean fixedWeek0;
    private List<Integer> fixedMatchesWeek0;
    
    // 固定条件：最終節固定
    private boolean fixedFinal;
    private List<Integer> fixedMatchesFinal;
    
    // 固定条件：中間節固定 3セット
    private boolean fixedMid1;
    private int fixedMidWeekIndex1;
    private List<Integer> fixedMatchesMid1;
    
    private boolean fixedMid2;
    private int fixedMidWeekIndex2;
    private List<Integer> fixedMatchesMid2;
    
    private boolean fixedMid3;
    private int fixedMidWeekIndex3;
    private List<Integer> fixedMatchesMid3;

    // 各試合のチームビットマスク
    private int[] matchTeamMask = new int[MATCH_COUNT];

    // バックトラック状態変数
    private List<List<Integer>> weekMatches;  // 各週の試合割当
    private int[] lastPlayedWeek;   // 各チームの最終出場週（初期値 -1）
    private int[] teamMatchCount;   // 各チームの消化試合数
    private long totalCount;
    private List<Schedule> scheduleList;

    public static class SchedulerResult {
        private long totalCount;
        private List<Schedule> schedules;
        public SchedulerResult(long totalCount, List<Schedule> schedules) {
            this.totalCount = totalCount;
            this.schedules = schedules;
        }
        public long getTotalCount() { return totalCount; }
        public List<Schedule> getSchedules() { return schedules; }
    }

    public SchedulerResult runScheduler(MatchSchedulerForm form) {
        // フォーム入力を各フィールドにセット
        this.teams = form.getTeamOrder();
        this.numberOfWeeks = form.getNumberOfWeeks();
        List<Integer> weekMatchNumbersList = form.getWeekMatchNumbers();
        this.weekMatchNumber = new int[numberOfWeeks];
        for (int i = 0; i < numberOfWeeks; i++) {
            weekMatchNumber[i] = weekMatchNumbersList.get(i);
        }
        this.fixedWeek0 = form.isFixedWeek0();
        this.fixedMatchesWeek0 = form.getFixedMatchesWeek0();
        this.fixedFinal = form.isFixedFinal();
        this.fixedMatchesFinal = form.getFixedMatchesFinal();
        this.fixedMid1 = form.isFixedMid1();
        this.fixedMidWeekIndex1 = form.getFixedMidWeekIndex1() != null ? form.getFixedMidWeekIndex1() : -1;
        this.fixedMatchesMid1 = form.getFixedMatchesMid1();
        this.fixedMid2 = form.isFixedMid2();
        this.fixedMidWeekIndex2 = form.getFixedMidWeekIndex2() != null ? form.getFixedMidWeekIndex2() : -1;
        this.fixedMatchesMid2 = form.getFixedMatchesMid2();
        this.fixedMid3 = form.isFixedMid3();
        this.fixedMidWeekIndex3 = form.getFixedMidWeekIndex3() != null ? form.getFixedMidWeekIndex3() : -1;
        this.fixedMatchesMid3 = form.getFixedMatchesMid3();

        // 各試合のチームマスクを準備 
        for (int m = 0; m < MATCH_COUNT; m++) {
            int t1 = MATCHES[m][0];
            int t2 = MATCHES[m][1];
            matchTeamMask[m] = (1 << t1) | (1 << t2); // 6bitの整数が15個の配列
        }

        // 週ごとの割当リスト初期化
        weekMatches = new ArrayList<>();
        for (int i = 0; i < numberOfWeeks; i++) {
            weekMatches.add(new ArrayList<>());
        }
        lastPlayedWeek = new int[teams.size()];
        teamMatchCount = new int[teams.size()];
        Arrays.fill(lastPlayedWeek, -1);
        Arrays.fill(teamMatchCount, 0);

        totalCount = 0;
        scheduleList = new ArrayList<>();

        int allMatchesMask = (1 << MATCH_COUNT) - 1;

        // 固定処理：第1節固定
        if (fixedWeek0) {
            if (fixedMatchesWeek0.size() != weekMatchNumber[0]) {
                return new SchedulerResult(0, scheduleList);
            }
            int fixedMask = 0; // 15bit
            for (int m : fixedMatchesWeek0) {
                fixedMask |= (1 << m);
            }
            if (!notOverlapInWeek(fixedMask)) {
                return new SchedulerResult(0, scheduleList);
            }
            weekMatches.get(0).addAll(toList(fixedMask));
            int usedTeams = teamsUsedInMask(fixedMask);
            for (int t = 0; t < teams.size(); t++) {
                if ((usedTeams & (1 << t)) != 0) {
                    lastPlayedWeek[t] = 0;
                    teamMatchCount[t]++;
                }
            }
            int newRemaining = allMatchesMask & ~fixedMask;
            backtrack(1, newRemaining);
        } else {
            backtrack(0, allMatchesMask);
        }
        return new SchedulerResult(totalCount, scheduleList);
    }
    
    // 同一週でチームが重複しないか？ 15bitの試合 -> 6bitのチーム 
    private boolean notOverlapInWeek(int matchMask) {
        int usedTeams = 0;
        for (int m = 0; m < MATCH_COUNT; m++) {
            int bitM = 1 << m;
            if ((matchMask & bitM) != 0) {
                int tMask = matchTeamMask[m];
                if ((usedTeams & tMask) != 0) {
                    return false;
                }
                usedTeams |= tMask;
            }
        }
        return true;
    }

    // ビットマスクに含まれる試合IDをリスト化 15bitの試合　-> IDのリスト
    private List<Integer> toList(int matchMask) {
        List<Integer> list = new ArrayList<>();
        for (int m = 0; m < MATCH_COUNT; m++) {
            if ((matchMask & (1 << m)) != 0) {
                list.add(m);
            }
        }
        return list;
    }

    // ビットマスクに含まれる15bitの試合IDから6bitの出場チームを作成
    private int teamsUsedInMask(int matchMask) {
        int usedTeams = 0;
        for (int m = 0; m < MATCH_COUNT; m++) {
            if ((matchMask & (1 << m)) != 0) {
                usedTeams |= matchTeamMask[m];
            }
        }
        return usedTeams;
    }

    // index = 週，remaining = 15bit から試合割り当てを決め、次へ進むバックトラック（深さ優先）
    private void backtrack(int weekIndex, int remaining) {
        // (A) 最後の2週に入る前に5試合完了チームがあればNG
        if (weekIndex == weekMatchNumber.length - 2) {
            for (int t = 0; t < teams.size(); t++) {
                if (teamMatchCount[t] == 5) {
                    return;
                }
            }
        }
        // (B) 全週決定済みなら
        if (weekIndex == weekMatchNumber.length) {
            if (remaining == 0) {
                totalCount++;
                List<List<Integer>> scheduleCopy = new ArrayList<>();
                for (List<Integer> week : weekMatches) {
                    scheduleCopy.add(new ArrayList<>(week));
                }
                scheduleList.add(new Schedule(scheduleCopy));
            }
            return;
        }
        // (C) 中間固定条件（3セット）
        if (fixedMid1 && weekIndex == fixedMidWeekIndex1) {
            handleFixWeek(remaining, fixedMatchesMid1, weekIndex);
            return;
        }
        if (fixedMid2 && weekIndex == fixedMidWeekIndex2) {
            handleFixWeek(remaining, fixedMatchesMid2, weekIndex);
            return;
        }
        if (fixedMid3 && weekIndex == fixedMidWeekIndex3) {
            handleFixWeek(remaining, fixedMatchesMid3, weekIndex);
            return;
        }
        // (D) 最終週固定条件
        if (weekIndex == weekMatchNumber.length - 1 && fixedFinal) {
            handleFixWeek(remaining, fixedMatchesFinal, weekIndex);
            return;
        }
        // (E) 通常の週
        int needed = weekMatchNumber[weekIndex];
        int mustUseTeamsMask = getMustUseTeamsMask(weekIndex, remaining);
        pickMatchesForWeek(weekIndex, 0, remaining, needed, 0, 0, mustUseTeamsMask);
    }

    // 直近2週以上出ていないチームで、まだ試合が残っているものを強制出場させるためのマスク 15bit -> 6bit
    private int getMustUseTeamsMask(int weekIndex, int remaining) {
        int mask = 0;
        for (int t = 0; t < teams.size(); t++) {
            if (lastPlayedWeek[t] <= weekIndex - 2) {
                if (teamHasRemainingMatch(t, remaining)) {
                    mask |= (1 << t);
                }
            }
        }
        return mask;
    }

    // チームtにまだ割り当てていない(remainingに含まれる)試合が有るか
    private boolean teamHasRemainingMatch(int t, int remaining) {
        for (int m = 0; m < MATCH_COUNT; m++) {
            if ((remaining & (1 << m)) != 0) {
                if ((matchTeamMask[m] & (1 << t)) != 0) {
                    return true; // 有る
                }
            }
        }
        return false; // 無い
    }

    // 週の固定処理
    private void handleFixWeek(int remaining, List<Integer> fixedMatches, int weekIndex) {
        int fixedMask = 0;
        for (int m : fixedMatches) {
            int bit = 1 << m;
            if ((remaining & bit) == 0) { // 既に試合を終えている -> NG
                return;
            }
            fixedMask |= bit;
        }
        if (fixedMatches.size() > weekMatchNumber[weekIndex]) {
            return;
        }
        if (!notOverlapInWeek(fixedMask)) {
            return;
        }
        // 残り枠 = 試合数 - 固定数
        int remainNeeded = weekMatchNumber[weekIndex] - fixedMatches.size();
        int usedTeams = teamsUsedInMask(fixedMask);
        if (remainNeeded == 0) {
            finalizeWeekAndGoNext(weekIndex, fixedMask, remaining);
            return;
        }
        int newRem = remaining & ~fixedMask;
        int mustUseTeamsMask = getMustUseTeamsMask(weekIndex, newRem);
        pickMatchesForWeek(weekIndex, 0, newRem, remainNeeded, fixedMask, usedTeams, mustUseTeamsMask);
    }

    // 今週(weekIndex)でneeded個の試合を選ぶバックトラック
    private void pickMatchesForWeek(int weekIndex, int startMatchId, int remaining, int needed, int chosenMask, int usedTeams, int mustUseTeamsMask) {
        if (needed == 0) {
            if (allMustTeamsIncluded(chosenMask, mustUseTeamsMask)) { // mustUseTeamsMask を満たしていれば確定
                finalizeWeekAndGoNext(weekIndex, chosenMask, remaining);
            }
            return;
        }
        if (startMatchId >= MATCH_COUNT) {
            return;
        }
        int bitM = 1 << startMatchId;
        if ((remaining & bitM) != 0) { // startMatchIdの試合が残っているか
            int tm = matchTeamMask[startMatchId];
            if ((usedTeams & tm) == 0) { // startMatchIdの試合のチームが同一週に入っていないか
                pickMatchesForWeek(weekIndex, startMatchId + 1, remaining, needed - 1, chosenMask | bitM, usedTeams | tm, mustUseTeamsMask);
            }
        }
        pickMatchesForWeek(weekIndex, startMatchId + 1, remaining, needed, chosenMask, usedTeams, mustUseTeamsMask);
    }

    // chosenMaskに含まれる試合がmustUseTeamsMaskのチームをすべてカバーしているか？
    private boolean allMustTeamsIncluded(int chosenMask, int mustUseTeamsMask) {
        if (mustUseTeamsMask == 0) return true;
        int usedTeams = teamsUsedInMask(chosenMask);
        return (usedTeams & mustUseTeamsMask) == mustUseTeamsMask;
    }

    // weekIndexの試合(chosenMask)が確定したので、weekMatches, lastPlayedWeek, teamMatchCountを更新して次の週へ
    private void finalizeWeekAndGoNext(int weekIndex, int chosenMask, int remaining) {
        weekMatches.get(weekIndex).clear();
        weekMatches.get(weekIndex).addAll(toList(chosenMask));
        int[] backupLastPlayed = Arrays.copyOf(lastPlayedWeek, lastPlayedWeek.length);
        int[] backupTeamCount = Arrays.copyOf(teamMatchCount, teamMatchCount.length);
        int usedTeams = teamsUsedInMask(chosenMask);
        for (int t = 0; t < teams.size(); t++) {
            if ((usedTeams & (1 << t)) != 0) {
                lastPlayedWeek[t] = weekIndex;
                teamMatchCount[t]++;
            }
        }
        int newRem = remaining & ~chosenMask;
        backtrack(weekIndex + 1, newRem);
        System.arraycopy(backupLastPlayed, 0, lastPlayedWeek, 0, lastPlayedWeek.length);
        System.arraycopy(backupTeamCount, 0, teamMatchCount, 0, teamMatchCount.length);
    }
}