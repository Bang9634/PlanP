package com.drhong.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.drhong.config.TestDatabaseConfig;

/**
 * 테스트용 데이터베이스 초기화 헬퍼
 */
public class TestDatabaseHelper {
    
    private static final Logger logger = LoggerFactory.getLogger(TestDatabaseHelper.class);
    
    /**
     * 테스트용 테이블을 생성한다.
     */
    public static void initializeTestDatabase() {
        TestDatabaseConfig config = new TestDatabaseConfig();
        
        try (Connection conn = DriverManager.getConnection(
                config.getJdbcUrl(),
                config.getUsername(),
                config.getPassword()
             );
             Statement stmt = conn.createStatement()) {
            
            // users 테이블 생성
            String createUsersTable = """
                CREATE TABLE IF NOT EXISTS users (
                    user_id VARCHAR(50) PRIMARY KEY,
                    username VARCHAR(100) NOT NULL,
                    password_hash VARCHAR(255) NOT NULL,
                    email VARCHAR(255) UNIQUE,
                    is_active BOOLEAN DEFAULT FALSE,
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    INDEX idx_email (email),
                    INDEX idx_username (username)
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
                """;
            stmt.execute(createUsersTable);
            
            logger.info("테스트 DB 테이블 생성 완료");
            
        } catch (Exception e) {
            logger.error("테스트 DB 초기화 실패", e);
            throw new RuntimeException("Test database initialization failed", e);
        }
    }
    
    /**
     * 모든 테이블 데이터를 삭제한다.
     */
    public static void cleanDatabase() {
        TestDatabaseConfig config = new TestDatabaseConfig();
        
        try (Connection conn = DriverManager.getConnection(
                config.getJdbcUrl(),
                config.getUsername(),
                config.getPassword()
             );
             Statement stmt = conn.createStatement()) {
            
            stmt.execute("DELETE FROM users");
            logger.info("테스트 DB 데이터 정리 완료");
            
        } catch (Exception e) {
            logger.error("테스트 DB 정리 실패", e);
        }
    }
}