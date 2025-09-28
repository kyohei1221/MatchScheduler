package com.example.matchscheduler.db;

import java.sql.Connection;
import java.sql.DriverManager;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("h2")
public class H2Config {

  // H2（インメモリ）に接続する ConnectionProvider
  @Bean
  public ConnectionProvider connectionProvider() {
    return () -> DriverManager.getConnection(
      "jdbc:h2:mem:matchdb;MODE=MySQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1",
      "sa", ""
    );
  }

  // 起動時にテーブル作成
  @Bean
  CommandLineRunner initSchema(ConnectionProvider cp) {
    return args -> {
      try (Connection c = cp.getConnection();
           var st = c.createStatement()) {
    	 st.executeUpdate(
    			  "CREATE TABLE IF NOT EXISTS bookmarks (" +
    			  "  id BIGINT AUTO_INCREMENT PRIMARY KEY," + 
    			  "  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
    			  "  memo VARCHAR(50)," +
    			  "  schedule_json TEXT" + 
    			  ")"
    	);
      }
    };
  }
}

