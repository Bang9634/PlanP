package com.drhong.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.drhong.config.DatabaseConfig;

/**
 * 애플리케이션 시작 시 데이터베이스 스키마를 초기화하는 클래스
 * 
 * <p>
 * 애플리케이션의 데이터베이스 환경을 자동으로 설정한다.
 * 데이터베이스 생성, 테이블 생성을 담당하며, 여러 번 실행되도 안전하다.
 * </p>
 * 
 * <h3>주요 기능</h3>
 * <ul>
 *   <li><strong>데이터베이스 자동 생성:</strong> planp 데이터베이스가 없으면 생성</li>
 *   <li><strong>스키마 자동 구성:</strong> 필요한 테이블들을 자동으로 생성</li>
 *   <li><strong>멱등성 보장:</strong> 중복 실행해도 오류 없음 (IF NOT EXISTS)</li>
 * </ul>
 * 
 * <h3>초기화 순서</h3>
 * <ol>
 *   <li>데이터베이스 존재 확인 및 생성 (utf8mb4 인코딩)</li>
 *   <li>users 테이블 생성 (이미 있으면 스킵)</li>
 *   <li>인덱스 생성 (email, username 조회 최적화)</li>
 * </ol>
 * 
 * <h3>생성되는 테이블</h3>
 * <pre>
 * users:
 *   - user_id (PK, VARCHAR(50))
 *   - username (VARCHAR(100), NOT NULL)
 *   - password_hash (VARCHAR(255), NOT NULL)
 *   - email (VARCHAR(255), UNIQUE)
 *   - is_active (BOOLEAN, DEFAULT FALSE)
 *   - created_at (TIMESTAMP, DEFAULT CURRENT_TIMESTAMP)
 *   - INDEX: idx_email, idx_username
 * </pre>
 * 
 * @author bang9634
 * @since 2025-11-18
 * 
 * @see DatabaseConfig
 * @see ConnectionManager
 */
public class DatabaseInitializer {
    private static final Logger logger = LoggerFactory.getLogger(DatabaseInitializer.class);
    private final DatabaseConfig databaseConfig;

    /**
     * DatabaseInitializer 인스턴스를 생성한다.
     * 
     * <p>
     * DatabaseConfig을 초기화하여 환경별 데이터베이스 접속 정보를 로드한다.
     * 환경변수 또는 설정 파일에서 MySQL 연결 정보를 가져온다.
     * </p>
     * 
     * @implNote DatabaseConfig는 환경변수(MYSQL_*)를 우선 사용
     * @see DatabaseConfig
     */
    public DatabaseInitializer() {
        this.databaseConfig = new DatabaseConfig();
    }


    /**
     * 데이터베이스 전체 초기화를 수행한다.
     * 
     * <p>
     * 애플리케이션 시작 시 한 번 호출되어 데이터베이스 환경을 구성한다.
     * 멱등성이 보장되므로 여러 번 실행해도 안전하다.
     * </p>
     * 
     * <h3>실행 단계</h3>
     * <ol>
     *   <li>데이터베이스 생성 (없으면)</li>
     *   <li>테이블 생성 (없으면)</li>
     * </ol>
     * 
     * <h3>로그 출력 예시</h3>
     * <pre>
     * 데이터베이스 초기화 시작...
     *   ✓ 데이터베이스 'planp' 준비 완료
     *   ✓ users 테이블 준비 완료
     * 데이터베이스 초기화 완료
     * </pre>
     * 
     * @throws RuntimeException 다음과 같은 경우 발생:
     *         <ul>
     *         <li>MySQL 서버에 연결할 수 없음 (서버 중지, 네트워크 오류)</li>
     *         <li>데이터베이스 생성 권한 부족</li>
     *         <li>테이블 생성 권한 부족</li>
     *         <li>잘못된 SQL 문법 (스키마 정의 오류)</li>
     *         <li>잘못된 인증 정보 (username, password)</li>
     *         </ul>
     *         <strong>처리:</strong> SQLException을 RuntimeException으로 래핑하여 전파
     *         <strong>해결:</strong>
     *         <pre>{@code
     *         # MySQL 서버 상태 확인
     *         brew services list | grep mysql
     *         brew services start mysql
     *         
     *         # 연결 테스트
     *         mysql -u root -p -h localhost -P 3306
     *         
     *         # 권한 확인
     *         SHOW GRANTS FOR 'planp_user'@'localhost';
     *         
     *         # 권한 부여
     *         GRANT ALL PRIVILEGES ON planp.* TO 'planp_user'@'localhost';
     *         }</pre>
     * 
     * @apiNote 애플리케이션 시작 시 main 메서드에서 호출
     * @implNote 멱등성 보장: IF NOT EXISTS, INSERT IGNORE 사용
     */
    public void initialize() {
        logger.info("데이터베이스 초기화 시작...");
        
        try {
            createDatabaseIfNotExists();
            createTablesIfNotExists();
            
            logger.info("데이터베이스 초기화 완료");
            
        } catch (SQLException e) {
            logger.error("데이터베이스 초기화 실패", e);
            throw new RuntimeException("Database initialization failed", e);
        }
    }

    
    /**
     * 데이터베이스가 없으면 생성한다.
     * 
     * <p>
     * MySQL 서버에 직접 연결하여 planp 데이터베이스를 생성한다.
     * 이미 존재하면 아무 작업도 하지 않는다 (멱등성).
     * </p>
     * 
     * <h3>생성 설정</h3>
     * <ul>
     *   <li><strong>Character Set:</strong> utf8mb4 (이모지 지원)</li>
     *   <li><strong>Collation:</strong> utf8mb4_unicode_ci (대소문자 무시, 유니코드)</li>
     *   <li><strong>Engine:</strong> InnoDB (트랜잭션 지원)</li>
     * </ul>
     * 
     * <h3>실행 SQL</h3>
     * <pre>{@code
     * CREATE DATABASE IF NOT EXISTS planp 
     * CHARACTER SET utf8mb4 
     * COLLATE utf8mb4_unicode_ci
     * }</pre>
     * 
     * <h3>연결 정보</h3>
     * <pre>
     * URL: jdbc:mysql://localhost:3306/ (데이터베이스명 제외)
     * User: DatabaseConfig에서 로드
     * Password: DatabaseConfig에서 로드
     * </pre>
     * 
     * @throws SQLException 다음과 같은 경우 발생:
     *         <ul>
     *         <li>MySQL 서버에 연결할 수 없음</li>
     *         <li>인증 실패 (잘못된 username/password)</li>
     *         <li>데이터베이스 생성 권한 부족 (CREATE DATABASE 권한 필요)</li>
     *         <li>네트워크 오류</li>
     *         </ul>
     * 
     * @apiNote 데이터베이스명 없이 MySQL 서버에 연결
     * @implNote try-with-resources로 Connection 자동 해제
     * 
     * @see DatabaseConfig#getJdbcUrlWithoutDatabase()
     */
    private void createDatabaseIfNotExists() throws SQLException {
        try (Connection conn = DriverManager.getConnection(
                databaseConfig.getJdbcUrlWithoutDatabase(),
                databaseConfig.getUsername(),
                databaseConfig.getPassword()
             );
             Statement stmt = conn.createStatement()) {
            
            String createDb = String.format(
                "CREATE DATABASE IF NOT EXISTS %s CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci",
                databaseConfig.getDatabase()
            );
            stmt.executeUpdate(createDb);
            logger.info("  ✓ 데이터베이스 '{}' 준비 완료", databaseConfig.getDatabase());
        }
    }


    /**
     * 필요한 테이블들이 없으면 생성한다.
     * 
     * <p>
     * PlanP 애플리케이션에 필요한 모든 테이블을 생성한다.
     * 이미 존재하면 아무 작업도 하지 않는다 (멱등성).
     * </p>
     * 
     * <h3>생성되는 테이블</h3>
     * 
     * <h3>1. users 테이블</h3>
     * <pre>
     * 컬럼:
     *   - user_id: VARCHAR(50) PRIMARY KEY
     *     사용자 고유 ID (로그인 ID)
     *   
     *   - username: VARCHAR(100) NOT NULL
     *     사용자 이름 (표시명)
     *   
     *   - password_hash: VARCHAR(255) NOT NULL
     *     BCrypt 해시된 비밀번호
     *   
     *   - email: VARCHAR(255) UNIQUE
     *     이메일 주소 (중복 불가)
     *   
     *   - is_active: BOOLEAN DEFAULT FALSE
     *     계정 활성화 여부
     *   
     *   - created_at: TIMESTAMP DEFAULT CURRENT_TIMESTAMP
     *     계정 생성 시각 (자동 입력)
     * 
     * 인덱스:
     *   - PRIMARY KEY (user_id)
     *   - UNIQUE KEY (email)
     *   - INDEX idx_email (email) - 이메일 조회 최적화
     *   - INDEX idx_username (username) - 이름 검색 최적화
     * 
     * 설정:
     *   - Engine: InnoDB (트랜잭션, 외래키 지원)
     *   - Charset: utf8mb4 (이모지 지원)
     *   - Collation: utf8mb4_unicode_ci (대소문자 무시)
     * </pre>
     * 
     * @throws SQLException 다음과 같은 경우 발생:
     *         <ul>
     *         <li>데이터베이스에 연결할 수 없음</li>
     *         <li>테이블 생성 권한 부족 (CREATE TABLE 권한 필요)</li>
     *         <li>SQL 문법 오류</li>
     *         <li>스토리지 엔진(InnoDB) 미지원</li>
     *         </ul>
     * 
     * @apiNote 테이블 추가 시 이 메서드에 SQL 추가
     * @implNote IF NOT EXISTS로 멱등성 보장
     */
    private void createTablesIfNotExists() throws SQLException {
        try (Connection conn = DriverManager.getConnection(
                databaseConfig.getJdbcUrl(),
                databaseConfig.getUsername(),
                databaseConfig.getPassword()
             );
             Statement stmt = conn.createStatement()) {
            
            // users 테이블
            stmt.executeUpdate("""
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
            """);
            logger.info("  ✓ users 테이블 준비 완료");
            
        }
    }
}