package com.drhong.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 테스트용 데이터베이스 설정 (H2 인메모리 DB)
 * 
 * <p>
 * 테스트 실행마다 새로운 DB가 생성되고 테스트 종료 시 자동으로 삭제된다.
 * 실제 MySQL DB에 영향을 주지 않는다.
 * </p>
 */
public class TestDatabaseConfig extends DatabaseConfig {
    
    private static final Logger logger = LoggerFactory.getLogger(TestDatabaseConfig.class);
    
    private final String jdbcUrl;
    
    public TestDatabaseConfig() {
        // H2 인메모리 DB 사용
        this.jdbcUrl = "jdbc:h2:mem:testdb;MODE=MySQL;DB_CLOSE_DELAY=-1";
        logger.info("테스트 DB 초기화: {}", jdbcUrl);
    }
    
    @Override
    public String getHost() {
        return "localhost";
    }
    
    @Override
    public int getPort() {
        return 0; // 인메모리 DB는 포트 불필요
    }
    
    @Override
    public String getDatabase() {
        return "testdb";
    }
    
    @Override
    public String getUsername() {
        return "sa";
    }
    
    @Override
    public String getPassword() {
        return "";
    }
    
    @Override
    public String getJdbcUrl() {
        return jdbcUrl;
    }
    
    @Override
    public String getJdbcUrlWithoutDatabase() {
        return "jdbc:h2:mem:";
    }
}