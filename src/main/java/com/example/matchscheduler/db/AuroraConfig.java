package com.example.matchscheduler.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Properties;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.rds.auth.GetIamAuthTokenRequest;
import com.amazonaws.services.rds.auth.RdsIamAuthTokenGenerator;

@Configuration
@Profile("aurora")
public class AuroraConfig {

    @Value("${db.host}") private String host;
    @Value("${db.port}") private int port;
    @Value("${db.user}") private String user;
    @Value("${db.database}") private String database;
    @Value("${db.region}") private String region;

    public Connection getConnection() throws Exception {
        String url = String.format("jdbc:mysql://%s:%d/%s", host, port, database);

        // IAM トークン生成
        RdsIamAuthTokenGenerator generator = RdsIamAuthTokenGenerator.builder()
            .credentials(new DefaultAWSCredentialsProviderChain())
            .region(Region.getRegion(Regions.fromName(region)))
            .build();
        String token = generator.getAuthToken(
            GetIamAuthTokenRequest.builder()
              .hostname(host).port(port).userName(user).build()
        );

        Properties props = new Properties();
        props.put("user", user);
        props.put("password", token);
        props.put("sslMode", "REQUIRED");
        return DriverManager.getConnection(url, props);
    }
    
    @Bean
    public ConnectionProvider connectionProvider() {
      return this::getConnection;
    }
    // aurora プロファイルの時だけ起動時に接続テストを走らせる
    @Bean
    CommandLineRunner auroraConnectionSmokeTest() {
      return args -> {
        try (Connection conn = getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT CURRENT_USER(), NOW()")) {
          if (rs.next()) {
            System.out.println("Aurora connected as: " + rs.getString(1) + " at " + rs.getString(2));
          }
        }
      };
    }
}
