package com.example.matchscheduler.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Repository;

@Repository
public class BookmarkRepository {

    private final ConnectionProvider cp;

    public BookmarkRepository(ConnectionProvider cp) {
        this.cp = cp;
    }

    public long insert(String scheduleJson) throws Exception {
        try (Connection conn = cp.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement psCount = conn.prepareStatement("SELECT COUNT(*) FROM bookmarks");
                 ResultSet rs = psCount.executeQuery()) {
                int count = 0;
                if (rs.next()) count = rs.getInt(1);
                if (count >= 20) {
                    conn.rollback();
                    throw new SQLException("bookmark limit reached");
                }
            }
            try (PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO bookmarks (schedule_json) VALUES (?)",
                    Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, scheduleJson);
                ps.executeUpdate();
                try (ResultSet rs2 = ps.getGeneratedKeys()) {
                    if (rs2.next()) {
                        long id = rs2.getLong(1);
                        conn.commit();
                        return id;
                    }
                }
            } catch (Exception e) {
                conn.rollback();
                throw e;
            }
        }
        throw new SQLException("Failed to insert bookmark");
    }

    public List<Bookmark> findAll() throws Exception {
        String sql = "SELECT id, created_at, memo, schedule_json FROM bookmarks ORDER BY created_at DESC, id DESC";
        List<Bookmark> list = new ArrayList<>();
        try (Connection conn = cp.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Bookmark b = new Bookmark();
                b.setId(rs.getLong("id"));
                Timestamp ts = rs.getTimestamp("created_at");
                if (ts != null) {
                    b.setCreatedAt(ts.toInstant().atZone(ZoneId.of("Asia/Tokyo")).toLocalDateTime());
                }
                b.setMemo(rs.getString("memo"));
                b.setScheduleJson(rs.getString("schedule_json"));
                list.add(b);
            }
        }
        return list;
    }

    public void updateMemo(long id, String memo) throws Exception {
        String sql = "UPDATE bookmarks SET memo = ? WHERE id = ?";
        try (Connection conn = cp.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, memo);
            ps.setLong(2, id);
            ps.executeUpdate();
        }
    }

    public void deleteById(long id) throws Exception {
        String sql = "DELETE FROM bookmarks WHERE id = ?";
        try (Connection conn = cp.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            ps.executeUpdate();
        }
    }
}