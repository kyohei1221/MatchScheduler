package com.example.matchscheduler.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import com.example.matchscheduler.model.MatchSchedulerForm;
import com.example.matchscheduler.model.Schedule;
import com.example.matchscheduler.service.MatchSchedulerService;
import com.example.matchscheduler.service.MatchSchedulerService.SchedulerResult;

@RestController
@RequestMapping("/")
public class MatchSchedulerController {

    // 直前の計算結果を保持
    private SchedulerResult lastResult;

    // アプリのトップページ用
    @GetMapping("")
    public ModelAndView index() {
        return new ModelAndView("index");
    }

    @PostMapping("/calculateAjax")
    public Map<String, Object> calculateAjax(@ModelAttribute MatchSchedulerForm form) {
        MatchSchedulerService service = new MatchSchedulerService();
        lastResult = service.runScheduler(form);
        Map<String, Object> response = new HashMap<>();
        response.put("totalCount", lastResult.getTotalCount());
        return response;
    }

    @GetMapping("/schedulesAjax")
    public Map<String, Object> schedulesAjax(@RequestParam(value = "page", required = false, defaultValue = "0") int page) {
        Map<String, Object> response = new HashMap<>();
        if (lastResult == null) {
            response.put("schedules", new Schedule[0]);
        } else {
            response.put("schedules", lastResult.getSchedules());
        }
        return response;
    }
}