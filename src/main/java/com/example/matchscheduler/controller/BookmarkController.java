package com.example.matchscheduler.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.matchscheduler.db.Bookmark;
import com.example.matchscheduler.db.BookmarkRepository;

@RestController
public class BookmarkController {

    private final BookmarkRepository repo;

    public BookmarkController(BookmarkRepository repo) {
        this.repo = repo;
    }

    @PostMapping(value="/bookmarks", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> save(@RequestBody Map<String, Object> body) {
        try {
            com.fasterxml.jackson.databind.ObjectMapper om = new com.fasterxml.jackson.databind.ObjectMapper();
            String json = om.writeValueAsString(body);
            try {
                long id = repo.insert(json);
                Map<String, Object> resp = new HashMap<>();
                resp.put("id", id);
                return ResponseEntity.status(HttpStatus.CREATED).body(resp);
            } catch (java.sql.SQLException ex) {
                if ("bookmark limit reached".equalsIgnoreCase(ex.getMessage())) {
                    return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", "bookmark limit reached"));
                }
                throw ex;
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping(value="/bookmarks", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> list() {
        try {
            List<Bookmark> items = repo.findAll();
            return ResponseEntity.ok(items);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", e.getMessage()));
        }
    }

    public static class MemoBody {
        public String memo;
    }

    @PatchMapping(value="/bookmarks/{id}/memo", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> updateMemo(@PathVariable("id") long id, @RequestBody MemoBody body) {
        try {
            String memo = body == null ? "" : (body.memo == null ? "" : body.memo);
            if (memo.length() > 50) {
                return ResponseEntity.badRequest().body(Map.of("error", "memo must be <= 50 characters"));
            }
            repo.updateMemo(id, memo);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/bookmarks/{id}")
    public ResponseEntity<?> delete(@PathVariable("id") long id) {
        try {
            repo.deleteById(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", e.getMessage()));
        }
    }
}
