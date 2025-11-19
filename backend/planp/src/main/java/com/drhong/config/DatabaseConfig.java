package com.drhong.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.drhong.database.ConnectionManager;
import com.drhong.database.DatabaseInitializer;

/**
 * MySQL 데이터베이스 연결 설정을 관리하는 클래스
 * 
 * <p>
 * 환경변수 또는 환경별 기본값을 기반으로 MySQL 연결에 필요한 정보를 제공한다.
 * {@link EnvironmentConfig#getEnvValue(String, String)}를 사용하여 환경변수 또는 기본값을
 * 기반으로 MySQL 연결 정보를 구성한다.
 * </p>
 * 
 * <h3>지원하는 환경변수:</h3>
 * <ul>
 *   <li><code>MYSQL_PLANP_HOST</code> - MySQL 서버 호스트</li>
 *   <li><code>MYSQL_PLANP_PORT</code> - MySQL 서버 포트</li>
 *   <li><code>MYSQL_PLANP_DATABASE</code> - 데이터베이스명</li>
 *   <li><code>MYSQL_PLANP_USERNAME</code> - 데이터베이스 사용자명</li>
 *   <li><code>MYSQL_PLANP_PASSWORD</code> - 데이터베이스 비밀번호</li>
 * </ul>

 * <h3>보안 권장사항</h3>
 * <ul>
 *   <li><strong>비밀번호 하드코딩 금지:</strong> 반드시 환경변수 사용</li>
 *   <li><strong>Git 커밋 주의:</strong> 비밀번호를 코드에 포함하지 말 것</li>
 *   <li><strong>프로덕션 SSL:</strong> useSSL=true 권장 (현재는 false)</li>
 *   <li><strong>최소 권한 원칙:</strong> 필요한 권한만 부여된 사용자 사용</li>
 * </ul>
 * 
 * <h3>JDBC URL 파라미터</h3>
 * <ul>
 *   <li><code>useSSL=false</code> - SSL 비활성화 (개발용, 프로덕션에서는 true 권장)</li>
 *   <li><code>allowPublicKeyRetrieval=true</code> - MySQL 8.0+ 인증 허용</li>
 *   <li><code>serverTimezone=UTC</code> - 타임존 명시 (Timestamp 오류 방지)</li>
 *   <li><code>characterEncoding=UTF-8</code> - 한글 등 유니코드 인코딩</li>
 * </ul>
 * 
 * @author bang9634
 * @since 2025-11-18
 * 
 * @see EnvironmentConfig
 * @see ConnectionManager
 * @see DatabaseInitializer
 */
public class DatabaseConfig {
    private static final Logger logger = LoggerFactory.getLogger(DatabaseConfig.class);
    
    private final String host;
    private final int port;
    private final String database;
    private final String username;
    private final String password;
    private final String jdbcUrl;


    /**
     * DatabaseConfig 인스턴스를 생성한다.
     * 
     * <p>
     * 환경변수 및 환경별 기본값을 기반으로 모든 설정을 로드하고,
     * JDBC URL을 생성한다. 생성 완료 후 설정 정보를 로그로 출력한다.
     * </p>
     * 
     * @apiNote 불변 객체로 설계되어 모든 필드가 final
     * @implNote 생성자에서 final 필드만 초기화하여 스레드 안전성 보장
     */
    public DatabaseConfig() {
        this.host = EnvironmentConfig.getEnvValue("MYSQL_PLANP_HOST", "localhost");
        this.port = Integer.parseInt(EnvironmentConfig.getEnvValue("MYSQL_PLANP_PORT", "3306"));
        this.database = EnvironmentConfig.getEnvValue("MYSQL_PLANP_DATABASE", "planp_db");
        this.username = EnvironmentConfig.getEnvValue("MYSQL_PLANP_USERNAME", "root");
        this.password = getPasswordFromEnv();
        this.jdbcUrl = buildJdbcUrl(true);

        logger.info("=== MySQL 데이터베이스 설정 ===");
        logger.info("호스트: {}:{}", host, port);
        logger.info("데이터베이스: {}", database);
        logger.info("사용자: {}", username);
        logger.info("JDBC URL: {}\n", jdbcUrl);
    }

    public String getHost() { return host; }
    public int getPort() { return port; }
    public String getDatabase() { return database; }
    public String getUsername() { return username; }
    public String getPassword() { return password; }
    public String getJdbcUrl() { return jdbcUrl; }


    /**
     * 환경변수에서 데이터베이스 비밀번호를 가져온다.
     * 
     * <p>
     * 보안상 비밀번호는 반드시 환경변수 MYSQL_PLANP_PASSWORD에서만 읽어온다.
     * </p>
     * 
     * <h3>환경변수 설정 예시</h3>
     * <pre>
     * # 개발 환경 (비밀번호 없어도 됨)
     * # 환경변수 설정 안 해도 OK
     * 
     * export MYSQL_PLANP_PASSWORD=secure_password_here
     * </pre>
     * 
     * @return 데이터베이스 비밀번호
     * 
     * @apiNote private 메서드로 생성자에서만 호출됨
     * @implNote System.getenv() 직접 호출 (기본값 없음)
     */
    private String getPasswordFromEnv() {
        String envPassword = System.getenv("MYSQL_PLANP_PASSWORD");
        if (envPassword == null) {
            return "";
        }
        else {
            return envPassword;
        }
    }
    
    /**
     * JDBC URL을 생성한다.
     * 
     * <p>
     * MySQL 서버 연결에 필요한 전체 JDBC URL을 구성한다.
     * 호스트, 포트, 데이터베이스명, 그리고 필수 파라미터들을 포함한다.
     * </p>
     * 
     * <h3>포함되는 파라미터:</h3>
     * <ul>
     *   <li><code>useSSL=false</code> - 개발환경용 (프로덕션에서는 true 권장)</li>
     *   <li><code>allowPublicKeyRetrieval=true</code> - MySQL 8.0+ 인증</li>
     *   <li><code>serverTimezone=UTC</code> - 타임존 명시</li>
     *   <li><code>characterEncoding=UTF-8</code> - 한글 인코딩</li>
     * </ul>
     * 
     * @param includeDatabase 데이터베이스명 포함 여부
     * @return JDBC URL
     * 
     * @apiNote private 메서드로 생성자와 getJdbcUrlWithoutDatabase()에서만 호출
     * @implNote String.format()으로 호스트:포트 조합 후 파라미터 추가
     * 
     * @see #getJdbcUrl()
     * @see #getJdbcUrlWithoutDatabase()
     */
    private String buildJdbcUrl(boolean includeDatabase) {
        String baseUrl = String.format("jdbc:mysql://%s:%d", host, port);
        
        if (includeDatabase) {
            baseUrl += "/" + database;
        } else {
            baseUrl += "/";
        }
        
        return baseUrl + "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC&characterEncoding=UTF-8";
    }

    

    /**
     * 데이터베이스명을 제외한 JDBC URL을 반환한다.
     * 
     * <p>
     * 데이터베이스 초기화 시 사용된다.
     * 데이터베이스가 아직 생성되지 않은 상태에서 MySQL 서버에 연결하여
     * CREATE DATABASE 명령을 실행할 때 필요하다.
     * </p>
     * 
     * @return 데이터베이스명을 제외한 JDBC URL
     * 
     * @apiNote DatabaseInitializer에서 데이터베이스 생성 시 사용
     * @implNote buildJdbcUrl(false) 호출로 구현
     * 
     * @see DatabaseInitializer#createDatabaseIfNotExists()
     * @see #buildJdbcUrl(boolean)
     */
    public String getJdbcUrlWithoutDatabase() {
        return buildJdbcUrl(false);
    }
}
