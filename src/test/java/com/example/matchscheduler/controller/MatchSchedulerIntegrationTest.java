package com.example.matchscheduler.controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * H2 プロファイルでアプリを実起動し、
 * /calculateAjax → /schedulesAjax → /bookmarks 系を疎通。
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("h2")
class MatchSchedulerIntegrationTest {

    @LocalServerPort
    int port;

    @Autowired
    TestRestTemplate rest;

    private String url(String path) {
        return "http://localhost:" + port + path;
    }

    @BeforeEach
    void health() {
        ResponseEntity<String> r = rest.getForEntity(url("/actuator/health"), String.class);
        assertEquals(HttpStatus.OK, r.getStatusCode());
        assertTrue(r.getBody() != null && r.getBody().contains("UP"));
    }

    @Test
    void calculate_and_bookmark_flow() {
        // 1) /calculateAjax にフォーム POST
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.addAll("teamOrder", Arrays.asList("A","B","C","D","E","F"));
        form.add("numberOfWeeks", "7");
        // weekMatchNumbers を 7 回（合計 15）
        form.addAll("weekMatchNumbers", Arrays.asList("3","2","2","2","2","2","2"));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        ResponseEntity<Map> calcResp = rest.postForEntity(
                url("/calculateAjax"),
                new HttpEntity<>(form, headers),
                Map.class
        );
        assertEquals(HttpStatus.OK, calcResp.getStatusCode());
        Number totalCount = (Number) calcResp.getBody().get("totalCount");
        assertNotNull(totalCount);
        assertTrue(totalCount.longValue() > 0);

        // 2) /schedulesAjax で直前の結果が返る
        ResponseEntity<Map> schResp = rest.getForEntity(url("/schedulesAjax"), Map.class);
        assertEquals(HttpStatus.OK, schResp.getStatusCode());
        assertTrue(schResp.getBody().containsKey("schedules"));

        // 3) /bookmarks へ保存（payloadの妥当性チェックは緩い想定）
        Map<String,Object> payload = new HashMap<>();
        payload.put("teamOrder", Arrays.asList("A","B","C","D","E","F"));
        payload.put("numberOfWeeks", 7);
        // 最低限の形で OK（Controller 側で JSON をそのまま保管想定）
        payload.put("weekMatches", List.of());

        HttpHeaders jsonHeaders = new HttpHeaders();
        jsonHeaders.setContentType(MediaType.APPLICATION_JSON);

        ResponseEntity<Map> saveResp = rest.postForEntity(
                url("/bookmarks"),
                new HttpEntity<>(payload, jsonHeaders),
                Map.class
        );
        assertEquals(HttpStatus.CREATED, saveResp.getStatusCode());
        assertNotNull(saveResp.getBody().get("id"));

        // 4) /bookmarks 一覧で 1 件以上返る
        ResponseEntity<List> listResp = rest.getForEntity(url("/bookmarks"), List.class);
        assertEquals(HttpStatus.OK, listResp.getStatusCode());
        assertNotNull(listResp.getBody());
        assertFalse(listResp.getBody().isEmpty(), "保存済みが1件以上ある");
    }
}