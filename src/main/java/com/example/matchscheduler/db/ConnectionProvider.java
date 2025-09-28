package com.example.matchscheduler.db;

import java.sql.Connection;

@FunctionalInterface
public interface ConnectionProvider {
    Connection getConnection() throws Exception;
}