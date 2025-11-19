package com.drhong.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.drhong.config.EnvironmentConfig.Environment;
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
 * 
 * <h3>환경별 기본값</h3>
 * <pre>
 * DEVELOPMENT (개발):
 *   - Database: planp_development
 *   - Username: root
 *   - Password: "" (빈 문자열)
 * 
 * TESTING (테스트):
 *   - Database: planp_test
 *   - Username: test_user
 *   - Password: 환경변수 필수
 * 
 * PRODUCTION (프로덕션):
 *   - Database: planp_production
 *   - Username: planp_user
 *   - Password: 환경변수 필수
 * </pre>
 * 
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
    private final EnvironmentConfig.Environment environment;


    /**
     * DatabaseConfig 인스턴스를 생성한다.
     * 
     * <p>
     * 환경변수 및 환경별 기본값을 기반으로 모든 설정을 로드하고,
     * JDBC URL을 생성한다. 생성 완료 후 설정 정보를 로그로 출력한다.
     * </p>
     * 
     * <h3>초기화 순서</h3>
     * <ol>
     *   <li>현재 환경 확인 (PLANP_ENV)</li>
     *   <li>MySQL 서버 정보 로드 (host, port)</li>
     *   <li>데이터베이스명 결정 (환경별 기본값 또는 환경변수)</li>
     *   <li>사용자명 결정 (환경별 기본값 또는 환경변수)</li>
     *   <li>비밀번호 로드 (환경변수 필수, 개발환경 제외)</li>
     *   <li>JDBC URL 생성</li>
     *   <li>설정 정보 로그 출력</li>
     * </ol>
     * 
     * <h3>로그 출력</h3>
     * <p>
     * 생성 시 자동으로 설정 정보를 로그로 출력한다.
     * 비밀번호는 보안상 출력하지 않는다.
     * </p>
     * 
     * @apiNote 불변 객체로 설계되어 모든 필드가 final
     * @implNote 생성자에서 final 필드만 초기화하여 스레드 안전성 보장
     */
    public DatabaseConfig() {
        this.environment = EnvironmentConfig.getCurrentEnvironment();

        this.host = EnvironmentConfig.getEnvValue("MYSQL_PLANP_HOST", "localhost");
        this.port = Integer.parseInt(EnvironmentConfig.getEnvValue("MYSQL_PLANP_PORT", "3306"));
        this.database = getDatabaseFromEnv();
        this.username = getUsernameFromEnv();
        this.password = getPasswordFromEnv();
        this.jdbcUrl = buildJdbcUrl(true);

        logger.info("=== MySQL 데이터베이스 설정 ===");
        logger.info("환경: {}", environment);
        logger.info("호스트: {}:{}", host, port);
        logger.info("데이터베이스: {}", database);
        logger.info("사용자: {}", username);
        logger.info("JDBC URL: {}\n", jdbcUrl);
    }

    // Getters
    public String getHost() { return host; }
    public int getPort() { return port; }
    public String getDatabase() { return database; }
    public String getUsername() { return username; }
    public String getPassword() { return password; }
    public String getJdbcUrl() { return jdbcUrl; }


    /**
     * 환경변수 또는 환경별 기본값에서 데이터베이스명을 가져온다.
     * 
     * <p>
     * 환경변수 MYSQL_PLANP_DATABASE가 설정되어 있으면 해당 값을 사용하고,
     * 없으면 환경에 따라 자동으로 데이터베이스명을 결정한다.
     * </p>
     * 
     * <h3>환경별 기본값</h3>
     * <ul>
     *   <li><strong>PRODUCTION:</strong> planp_production</li>
     *   <li><strong>TESTING:</strong> planp_test</li>
     *   <li><strong>DEVELOPMENT:</strong> planp_development (기본)</li>
     * </ul>
     * 
     * <h3>사용 예시</h3>
     * <pre>{@code
     * // 환경변수 미설정 시
     * PLANP_ENV=production → "planp_production"
     * PLANP_ENV=testing    → "planp_test"
     * PLANP_ENV=development → "planp_development"
     * 
     * // 환경변수 설정 시 (재정의)
     * MYSQL_PLANP_DATABASE=custom_db → "custom_db"
     * }</pre>
     * 
     * @return 데이터베이스명
     * 
     * @apiNote private 메서드로 생성자에서만 호출됨
     */
    private String getDatabaseFromEnv() {
        String defaultDb = switch (environment) {
            case PRODUCTION -> "planp_production";
            case TESTING -> "planp_test";
            default -> "planp_development";
        };
        return EnvironmentConfig.getEnvValue("MYSQL_PLANP_DATABASE", defaultDb);
    }


    /**
     * 환경변수 또는 환경별 기본값에서 사용자명을 가져온다.
     * 
     * <p>
     * 환경변수 MYSQL_PLANP_USERNAME이 설정되어 있으면 해당 값을 사용하고,
     * 없으면 환경에 따라 자동으로 사용자명을 결정한다.
     * </p>
     * 
     * <h3>환경별 기본값</h3>
     * <ul>
     *   <li><strong>PRODUCTION:</strong> planp_user (최소 권한 사용자)</li>
     *   <li><strong>TESTING:</strong> test_user (테스트 전용 사용자)</li>
     *   <li><strong>DEVELOPMENT:</strong> root (개발 편의성, 모든 권한)</li>
     * </ul>
     * 
     * <h3>보안 권장사항</h3>
     * <ul>
     *   <li>프로덕션에서는 최소 권한 원칙 적용</li>
     *   <li>root 사용자는 개발 환경에서만 사용</li>
     *   <li>각 환경별로 별도의 MySQL 사용자 생성 권장</li>
     * </ul>
     * 
     * @return 데이터베이스 사용자명
     * 
     * @apiNote private 메서드로 생성자에서만 호출됨
     */
    private String getUsernameFromEnv() {
        String defaultUser = switch (environment) {
            case PRODUCTION -> "planp_user";
            case TESTING -> "test_user";
            default -> "root";
        };
        return EnvironmentConfig.getEnvValue("MYSQL_PLANP_USERNAME", defaultUser);
    }


    /**
     * 환경변수에서 데이터베이스 비밀번호를 가져온다.
     * 
     * <p>
     * 보안상 비밀번호는 반드시 환경변수 MYSQL_PLANP_PASSWORD에서만 읽어온다.
     * 개발 환경에서만 예외적으로 비밀번호가 없어도 허용한다 (빈 문자열 반환).
     * </p>
     * 
     * <h3>환경별 동작</h3>
     * <ul>
     *   <li><strong>DEVELOPMENT:</strong> 환경변수 없으면 빈 문자열("") 반환</li>
     *   <li><strong>TESTING:</strong> 환경변수 필수 (없으면 null 반환)</li>
     *   <li><strong>PRODUCTION:</strong> 환경변수 필수 (없으면 null 반환)</li>
     * </ul>
     * 
     * <h3>보안 권장사항</h3>
     * <ul>
     *   <li>비밀번호를 코드에 하드코딩하지 말 것</li>
     *   <li>환경변수 파일(.env 등)을 Git에 커밋하지 말 것</li>
     *   <li>프로덕션에서는 반드시 강력한 비밀번호 설정</li>
     *   <li>정기적으로 비밀번호 변경</li>
     * </ul>
     * 
     * <h3>환경변수 설정 예시</h3>
     * <pre>
     * # 개발 환경 (비밀번호 없어도 됨)
     * # 환경변수 설정 안 해도 OK
     * 
     * # 테스트/프로덕션 환경 (필수)
     * export MYSQL_PLANP_PASSWORD=secure_password_here
     * </pre>
     * 
     * @return 데이터베이스 비밀번호 (개발환경에서만 빈 문자열 허용)
     * 
     * @apiNote private 메서드로 생성자에서만 호출됨
     * @implNote System.getenv() 직접 호출 (기본값 없음)
     */
    private String getPasswordFromEnv() {
        String envPassword = System.getenv("MYSQL_PLANP_PASSWORD");
        if (envPassword == null && environment == Environment.DEVELOPMENT) {
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
     * <h3>URL 형식</h3>
     * <pre>
     * jdbc:mysql://localhost:3306/?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC&characterEncoding=UTF-8
     * </pre>
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
