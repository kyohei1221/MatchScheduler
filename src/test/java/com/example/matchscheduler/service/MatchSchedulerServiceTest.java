package com.example.matchscheduler.service;

import com.example.matchscheduler.model.MatchSchedulerForm;
import com.example.matchscheduler.model.Schedule;
import org.junit.jupiter.api.Test;

import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * MatchSchedulerService の基本動作テスト。
 * - 制約を満たす日程が1件以上出ること
 * - 週ごとのカード数が指定どおり
 * - 同一週に同一チームが重複しない
 * - 「最終2節に必ず出場」の制約を満たす（= どのチームも最終2節のどちらかに出ている）
 * - 初週固定の枚数が合わないと 0 件
 */
public class MatchSchedulerServiceTest {

    private MatchSchedulerForm makeBaseForm() {
        MatchSchedulerForm f = new MatchSchedulerForm();
        f.setTeamOrder(Arrays.asList("A","B","C","D","E","F"));
        f.setNumberOfWeeks(7);
        // 合計 15 になる配分
        f.setWeekMatchNumbers(Arrays.asList(3, 2, 2, 2, 2, 2, 2));

        // 固定なし
        f.setFixedWeek0(false);
        f.setFixedFinal(false);
        f.setFixedMid1(false);
        f.setFixedMid2(false);
        f.setFixedMid3(false);
        return f;
    }

    @Test
    void calculatesAtLeastOneScheduleAndRespectsConstraints() {
        MatchSchedulerForm form = makeBaseForm();

        MatchSchedulerService svc = new MatchSchedulerService();
        MatchSchedulerService.SchedulerResult result = svc.runScheduler(form);

        assertTrue(result.getTotalCount() > 0, "少なくとも1件は解が出る想定");

        // 任意の1件を詳細検査（全件検査ではなく代表検査）
        Schedule s = result.getSchedules().get(0);
        List<List<Integer>> byWeek = s.getWeekMatches();

        // 週数 & 週ごとのカード枚数
        assertEquals(form.getNumberOfWeeks(), byWeek.size());
        for (int i = 0; i < byWeek.size(); i++) {
            assertEquals(form.getWeekMatchNumbers().get(i).intValue(), byWeek.get(i).size(),
                    "第" + (i+1) + "節のカード数が不一致");
        }

        // 同一週に同一チームが重複しない
        int[][] matches = {
                {0,1},{0,2},{0,3},{0,4},{0,5},
                {1,2},{1,3},{1,4},{1,5},
                {2,3},{2,4},{2,5},
                {3,4},{3,5},
                {4,5}
        };
        for (int w = 0; w < byWeek.size(); w++) {
            Set<Integer> usedTeams = new HashSet<>();
            for (int mid : byWeek.get(w)) {
                int t1 = matches[mid][0], t2 = matches[mid][1];
                assertFalse(usedTeams.contains(t1), "同一週のチーム重複あり");
                assertFalse(usedTeams.contains(t2), "同一週のチーム重複あり");
                usedTeams.add(t1);
                usedTeams.add(t2);
            }
        }

        // どのチームも最終2節（= 6節 or 7節）に必ず出場
        boolean[] appearsInLast2 = new boolean[6];
        for (int w = byWeek.size()-2; w < byWeek.size(); w++) {
            for (int mid : byWeek.get(w)) {
                appearsInLast2[matches[mid][0]] = true;
                appearsInLast2[matches[mid][1]] = true;
            }
        }
        for (int t=0;t<6;t++) {
            assertTrue(appearsInLast2[t], "チーム"+t+"が最終2節のいずれかに出場していない");
        }
    }

    @Test
    void fixedWeek0CountMismatchReturnsZero() {
        MatchSchedulerForm form = makeBaseForm();
        form.setFixedWeek0(true);
        // 第1節は 3 カードの想定だが、固定を1つだけ指定 → 0件になるはず
        form.setFixedMatchesWeek0(Collections.singletonList(0));

        MatchSchedulerService svc = new MatchSchedulerService();
        MatchSchedulerService.SchedulerResult result = svc.runScheduler(form);

        assertEquals(0, result.getTotalCount());
        assertTrue(result.getSchedules().isEmpty());
    }
}