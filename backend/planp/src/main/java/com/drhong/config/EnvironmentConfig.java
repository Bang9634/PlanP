package com.drhong.config;

/**
 * PlanP 백엔드 애플리케이션 환경설정 통합 관리 클래스
 * <p>
 * PlanP 백엔드 애플리케이션의 모든 환경설정을 중앙화하여 관리한다.
 * 개발, 테스트, 프로덕션 환경에서 각각 다른 설정값을 자동으로 적용하며,
 * 환경변수와 기본값을 체계적으로 관리해 배포 환경에 관계없이 일관된 동작을 보장한다.
 * </p>
 * 
 * <h3>주요 특징:</h3>
 * <ul>
 *   <li><strong>환경별 자동 설정:</strong> PLANP_ENV 값에 따라 개발/테스트/프로덕션 설정 자동 적용</li>
 *   <li><strong>환경변수 우선:</strong> 시스템 환경변수가 설정되면 기본값보다 우선 적용</li>
 *   <li><strong>타입 안전성:</strong> 문자열, 정수, 배열 등 타입별로 적절한 변환 및 검증</li>
 *   <li><strong>중앙화된 관리:</strong> 모든 설정값을 한 곳에서 관리하여 유지보수성 향상</li>
 * </ul>
 * 
 *  * <h3>설정 우선순위:</h3>
 * <ol>
 *   <li>시스템 환경변수 (최고 우선순위)</li>
 *   <li>환경별 기본값 (PLANP_ENV에 따라 결정)</li>
 *   <li>전역 기본값 (환경변수가 없을 때 사용)</li>
 * </ol>
 * 
 * <h3>지원하는 환경변수:</h3>
 * <ul>
 *   <li><code>PLANP_ENV</code> - 실행 환경 (development/testing/production)</li>
 *   <li><code>PLANP_HOST</code> - 서버 바인딩 호스트</li>
 *   <li><code>PLANP_PORT</code> - 서버 포트 번호</li>
 *   <li><code>PLANP_ALLOWED_ORIGINS</code> - CORS 허용 오리진 (쉼표로 구분)</li>
 *   <li><code>MYSQL_HOST</code> - MySQL 서버 호스트</li>
 *   <li><code>MYSQL_PORT</code> - MySQL 서버 포트</li>
 *   <li><code>MYSQL_DATABASE</code> - 데이터베이스명</li>
 *   <li><code>MYSQL_USERNAME</code> - 데이터베이스 사용자명</li>
 *   <li><code>MYSQL_PASSWORD</code> - 데이터베이스 비밀번호</li>
 * </ul>
 * 
 * <h3>사용 예시:</h3>
 * <pre>{@code
 * // 환경 확인
 * Environment env = EnvironmentConfig.getCurrentEnvironment();
 * if (env == Environment.PRODUCTION) {
 *     // 프로덕션 전용 로직
 * }
 * 
 * // 서버 설정 가져오기
 * String host = EnvironmentConfig.getHost();
 * int port = EnvironmentConfig.getPort();
 * 
 * // 데이터베이스 연결 정보
 * String dbUrl = "jdbc:mysql://" + EnvironmentConfig.getMysqlHost() 
 *                + ":" + EnvironmentConfig.getMysqlPort() 
 *                + "/" + EnvironmentConfig.getMysqlDatabase();
 * }</pre>
 * 
 * @author bang9634
 * @since 2025-11-15
 * 
 * @see com.drhong.Main
 * @see com.drhong.server.CorsFilter
 * 
 * @implNote 모든 메서드는 static으로 구현되어 인스턴스 생성없이 사용 가능
 */
public class EnvironmentConfig {
    
    /**
     * 애플리케이션 실행 환경을 나타내는 열거형
     * <p>
     * 각 환경별로 다른 기본값과 정책을 적용하여 
     * 일관된 설정 관리를 지원한다.
     * </p>
     * 
     * <ul>
     *   <li><strong>DEVELOPMENT:</strong> 로컬 개발용 - 관대한 보안정책, 상세한 로깅</li>
     *   <li><strong>TESTING:</strong> 테스트용 - 별도 DB, 디버그 로깅</li>
     *   <li><strong>PRODUCTION:</strong> 운영용 - 엄격한 보안정책, 최소한의 로깅</li>
     * </ul>
     */
    public enum Environment {
        /** 개발 환경 - localhost 기반 */
        DEVELOPMENT, 
        /** 테스트 환경 - 별도 테스트 DB */
        TESTING, 
        /** 프로덕션 환경 - 운영 DB */
        PRODUCTION
    }
    
    /**
     * 현재 애플리케이션이 실행되고 있는 환경을 반환한다.
     * <p>
     * PLANP_ENV 환경변수 값을 기반으로 실행 환경을 결정한다.
     * 환경변수가 설정되지 않았거나 잘못된 값인 경우 개발 환경으로 기본 설정된다.
     * </p>
     * 
     * <h4>환경변수 값과 매핑:</h4>
     * <ul>
     *   <li><code>"production"</code> → {@link Environment#PRODUCTION}</li>
     *   <li><code>"testing"</code> → {@link Environment#TESTING}</li>
     *   <li>기타 모든 값 → {@link Environment#DEVELOPMENT}</li>
     * </ul>
     * 
     * <h4>사용 예시:</h4>
     * <pre>{@code
     * Environment env = EnvironmentConfig.getCurrentEnvironment();
     * switch (env) {
     *     case PRODUCTION -> setupProductionLogging();
     *     case TESTING -> setupTestDatabase();
     *     default -> enableDebugMode();
     * }
     * }</pre>
     * 
     * @return 현재 실행 환경 (기본값: {@link Environment#DEVELOPMENT})
     * 
     * @implNote 환경변수 검사는 대소문자를 구분하지 않음
     */
    public static Environment getCurrentEnvironment() {
        String env = getEnvValue("PLANP_ENV", "development");
        return switch (env.toLowerCase()) {
            case "production" -> Environment.PRODUCTION;
            case "testing" -> Environment.TESTING;
            default -> Environment.DEVELOPMENT;
        };
    }
    
    /**
     * HTTP 서버가 바인딩할 호스트 주소를 반환한다.
     * <p>
     * 서버가 수신할 네트워크 인터페이스를 지정한다.
     * 기본값은 로컬호스트로 설정된다.
     * 외부 접속이 필요한 경우 환경변수를 통해 변경할 수 있다.
     * </p>
     * 
     * <h4>주요 호스트 값:</h4>
     * <ul>
     *   <li><code>"localhost"</code> - 로컬 접속만 허용 (기본값, 보안)</li>
     *   <li><code>"127.0.0.1"</code> - IPv4 루프백 인터페이스</li>
     *   <li><code>"0.0.0.0"</code> - 모든 네트워크 인터페이스 (주의 필요)</li>
     *   <li><code>"특정IP"</code> - 특정 네트워크 인터페이스만 바인딩</li>
     * </ul>
     * 
     * @return 서버 바인딩 호스트 주소 (기본값: "localhost")
     */
    public static String getHost() {
        return getEnvValue("PLANP_HOST", "localhost");
    }
    
    /**
     * HTTP 서버가 사용할 포트 번호를 반환한다.
     * <p>
     * 서버가 HTTP 요청을 수신할 TCP 포트 번호를 반환한다.
     * 기본값은 8080번 포트이며, 환경변수를 통해 다른 포트로 변경이 가능하다.
     * </p>
     * 
     * <h4>포트 번호 가이드:</h4>
     * <ul>
     *   <li><strong>1-1023:</strong> 시스템 예약 포트 (관리자 권한 필요)</li>
     *   <li><strong>1024-49151:</strong> 등록된 포트 (일반적으로 안전)</li>
     *   <li><strong>49152-65535:</strong> 동적/사설 포트</li>
     *   <li><strong>8080:</strong> 일반적인 웹 개발 포트 (기본값)</li>
     *   <li><strong>3000:</strong> Node.js 개발 서버 기본 포트</li>
     * </ul>
     * 
     * <h4>주의사항:</h4>
     * <ul>
     *   <li>이미 사용 중인 포트는 바인딩 시 오류 발생</li>
     *   <li>1024 미만 포트 사용 시 관리자 권한 필요할 수 있음</li>
     *   <li>방화벽 설정에서 해당 포트 허용 필요</li>
     * </ul>
     * 
     * @return 서버 포트 번호 (기본값: 8080, 범위: 1-65535)
     * 
     * @throws NumberFormatException PLANP_PORT 환경변수가 숫자가 아닌 경우
     * 
     */
    public static int getPort() {
        return Integer.parseInt(getEnvValue("PLANP_PORT", "8080"));
    }
    
    /**
     * CORS(Cross-Origin Resource Sharing)에서 허용할 오리진 목록을 반환한다.
     * <p>
     * 브라우저의 동일 출처 정책을 우회하여 지정된 도메인에서의 API 요청을 허용한다.
     * 여러 오리진은 쉼표로 구분하여 지정할 수 있으며, 보안상 필요한 도메인만 포함해야한다.
     * </p>
     * 
     * <h4>오리진 형식:</h4>
     * <ul>
     *   <li><code>http://localhost:3000</code> - 개발 환경 프론트엔드</li>
     *   <li><code>https://example.com</code> - 프로덕션 도메인</li>
     *   <li><code>http://192.168.1.100:8080</code> - 특정 IP와 포트</li>
     *   <li><code>*</code> - 모든 오리진 허용 (개발 전용, 보안 위험)</li>
     * </ul>
     * 
     * <h4>설정 예시:</h4>
     * <pre>{@code
     * // 환경변수 설정
     * export PLANP_ALLOWED_ORIGINS="http://localhost:3000,https://myapp.com,https://www.myapp.com"
     * 
     * // 코드에서 사용
     * String[] origins = EnvironmentConfig.getAllowedOrigins();
     * for (String origin : origins) {
     *     corsPolicy.addAllowedOrigin(origin);
     * }
     * }</pre>
     * 
     * <h4>보안 고려사항:</h4>
     * <p>
     * 와일드카드(*)는 개발 환경에서만 사용하고, 프로덕션에서는
     * 실제 필요한 도메인만 명시적으로 허용하는 것이 보안상 안전하다.
     * </p>
     * 
     * @return 허용할 오리진 배열 (기본값: ["http://localhost:3000"])
     * 
     * @see com.drhong.server.CorsFilter
     */
    public static String[] getAllowedOrigins() {
        String origins = getEnvValue("PLANP_ALLOWED_ORIGINS", "http://localhost:3000");
        return origins.split(",");
    }
    
    /**
     * MySQL 데이터베이스 서버의 호스트 주소를 반환한다.
     * <p>
     * 애플리케이션이 연결할 MySQL 서버의 네트워크 주소를 반환한다.
     * 로컬 개발환경에서는 localhost를 사용하고, 프로덕션에서는
     * 실제 데이터베이스 서버의 주소를 환경변수로 설정한다.
     * </p>
     * 
     * <h4>일반적인 설정값:</h4>
     * <ul>
     *   <li><code>"localhost"</code> - 로컬 MySQL 서버 (개발용)</li>
     *   <li><code>"127.0.0.1"</code> - 로컬 IPv4 주소</li>
     *   <li><code>"mysql.example.com"</code> - 외부 MySQL 서버</li>
     *   <li><code>"192.168.1.100"</code> - 내부 네트워크 MySQL 서버</li>
     * </ul>
     * 
     * <h4>연결 테스트:</h4>
     * <pre>{@code
     * String host = EnvironmentConfig.getMysqlHost();
     * int port = EnvironmentConfig.getMysqlPort();
     * 
     * try (Socket socket = new Socket(host, port)) {
     *     System.out.println("MySQL 서버 연결 가능: " + host + ":" + port);
     * } catch (IOException e) {
     *     System.err.println("MySQL 서버 연결 실패: " + e.getMessage());
     * }
     * }</pre>
     * 
     * @return MySQL 서버 호스트 주소 (기본값: "localhost")
     * 
     * @see #getMysqlPort()
     * @see #getMysqlDatabase()
     */
    public static String getMysqlHost() {
        return getEnvValue("MYSQL_HOST", "localhost");
    }
    
    /**
     * MySQL 데이터베이스 서버의 포트 번호를 반환한다.
     * <p>
     * MySQL 서버가 클라이언트 연결을 수신하는 TCP 포트 번호를 반환한다.
     * 표준 MySQL 포트인 3306번을 기본값으로 사용하며,
     * 다른 포트를 사용하는 경우 환경변수로 변경할 수 있다.
     * </p>
     * 
     * <h4>일반적인 MySQL 포트:</h4>
     * <ul>
     *   <li><strong>3306:</strong> MySQL 기본 포트</li>
     *   <li><strong>3307:</strong> MySQL 대체 포트</li>
     *   <li><strong>33060:</strong> MySQL X Protocol 포트</li>
     * </ul>
     * 
     * <h4>포트 사용 확인:</h4>
     * <pre>{@code
     * // Linux/Mac에서 포트 사용 확인
     * netstat -an | grep 3306
     * 
     * // Windows에서 포트 사용 확인
     * netstat -an | findstr 3306
     * }</pre>
     * 
     * @return MySQL 서버 포트 번호 (기본값: 3306)
     * 
     * @throws NumberFormatException MYSQL_PORT 환경변수가 숫자가 아닌 경우
     * 
     * @see #getMysqlHost()
     */
    public static int getMysqlPort() {
        return Integer.parseInt(getEnvValue("MYSQL_PORT", "3306"));
    }
    
    /**
     * 사용할 MySQL 데이터베이스명을 환경에 따라 자동으로 반환한다.
     * <p>
     * 실행 환경에 따라 서로 다른 데이터베이스를 사용하여 개발, 테스트, 프로덕션 데이터를
     * 분리한다.. 이를 통해 테스트가 실제 데이터에 영향을 주지 않고,
     * 환경별로 독립적인 데이터 관리가 가능하다.
     * </p>
     * 
     * <h4>환경별 기본 데이터베이스명:</h4>
     * <ul>
     *   <li><strong>DEVELOPMENT:</strong> planp_development</li>
     *   <li><strong>TESTING:</strong> planp_test</li>
     *   <li><strong>PRODUCTION:</strong> planp_production</li>
     * </ul>
     * 
     * <h4>데이터베이스 생성 예시:</h4>
     * <pre>{@code
     * -- MySQL에서 환경별 데이터베이스 생성
     * CREATE DATABASE planp_development CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
     * CREATE DATABASE planp_test CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
     * CREATE DATABASE planp_production CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
     * }</pre>
     * 
     * <h4>사용 예시:</h4>
     * <pre>{@code
     * String dbName = EnvironmentConfig.getMysqlDatabase();
     * String url = "jdbc:mysql://localhost:3306/" + dbName 
     *            + "?useSSL=true&serverTimezone=UTC";
     * }</pre>
     * 
     * @return 환경별 데이터베이스명 (환경변수로 재정의 가능)
     * 
     * @see #getCurrentEnvironment()
     * @see #getMysqlHost()
     * @see #getMysqlPort()
     */
    public static String getMysqlDatabase() {
        Environment env = getCurrentEnvironment();
        String defaultDb = switch (env) {
            case PRODUCTION -> "planp_production";
            case TESTING -> "planp_test";
            default -> "planp_development";
        };
        return getEnvValue("MYSQL_DATABASE", defaultDb);
    }
    
    /**
     * MySQL 데이터베이스 연결에 사용할 사용자명을 환경에 따라 반환한다.
     * <p>
     * 환경별로 다른 데이터베이스 사용자를 사용하여 권한 분리와 보안을 강화한다.
     * 개발환경에서는 편의를 위해 root를 사용하지만, 프로덕션에서는
     * 제한된 권한을 가진 전용 사용자를 사용하는 것이 보안상 안전하다.
     * </p>
     * 
     * <h4>환경별 기본 사용자명:</h4>
     * <ul>
     *   <li><strong>DEVELOPMENT:</strong> root (개발 편의성)</li>
     *   <li><strong>TESTING:</strong> test_user (테스트 전용)</li>
     *   <li><strong>PRODUCTION:</strong> planp_user (최소 권한)</li>
     * </ul>
     * 
     * <h4>사용자 생성 및 권한 설정 예시:</h4>
     * <pre>{@code
     * -- 프로덕션 사용자 생성
     * CREATE USER 'planp_user'@'%' IDENTIFIED BY 'secure_password';
     * GRANT SELECT, INSERT, UPDATE, DELETE ON planp_production.* TO 'planp_user'@'%';
     * 
     * -- 테스트 사용자 생성
     * CREATE USER 'test_user'@'localhost' IDENTIFIED BY 'test_password';
     * GRANT ALL PRIVILEGES ON planp_test.* TO 'test_user'@'localhost';
     * 
     * FLUSH PRIVILEGES;
     * }</pre>
     * 
     * <h4>보안 권장사항:</h4>
     * <ul>
     *   <li>프로덕션에서는 필요한 최소 권한만 부여</li>
     *   <li>각 환경별로 다른 비밀번호 사용</li>
     *   <li>가능하면 호스트 제한 적용 (@'localhost' 등)</li>
     * </ul>
     * 
     * @return 환경별 데이터베이스 사용자명 (환경변수로 재정의 가능)
     * 
     * @see #getCurrentEnvironment()
     * @see #getMysqlPassword()
     */
    public static String getMysqlUsername() {
        Environment env = getCurrentEnvironment();
        String defaultUser = switch (env) {
            case PRODUCTION -> "planp_user";
            case TESTING -> "test_user";
            default -> "root";
        };
        return getEnvValue("MYSQL_USERNAME", defaultUser);
    }
    
    /**
     * MySQL 데이터베이스 연결에 사용할 비밀번호를 반환한다.
     * <p>
     * 데이터베이스 비밀번호는 보안상 매우 중요하므로 환경변수에서만 읽어온다.
     * 코드에 하드코딩하지 않으며, 개발 환경에서만 예외적으로 빈 비밀번호를 허용한다.
     * </p>
     * 
     * <h4>환경별 비밀번호 정책:</h4>
     * <ul>
     *   <li><strong>DEVELOPMENT:</strong> 빈 문자열 허용 (로컬 MySQL 편의성)</li>
     *   <li><strong>TESTING:</strong> 환경변수 필수</li>
     *   <li><strong>PRODUCTION:</strong> 환경변수 필수 (강력한 비밀번호 권장)</li>
     * </ul>
     * 
     * <h4>안전한 비밀번호 관리:</h4>
     * <pre>{@code
     * // 환경변수로 비밀번호 설정
     * export MYSQL_PASSWORD="your_secure_password_here"
     * 
     * 
     * @return MySQL 데이터베이스 비밀번호 (개발환경에서만 빈 문자열 허용)
     * 
     * @apiNote 프로덕션/테스트 환경에서 MYSQL_PASSWORD 미설정 시 null 반환될 수 있음
     * 
     * @see #getCurrentEnvironment()
     * @see #getMysqlUsername()
     */
    public static String getMysqlPassword() {
        Environment env = getCurrentEnvironment();
        String password = System.getenv("MYSQL_PASSWORD");
        
        // 개발 환경에서는 비밀번호 없어도 됨
        if (password == null && env == Environment.DEVELOPMENT) {
            return "";
        }
        
        return password;
    }
    
   /**
     * 환경변수에서 값을 읽되, 없으면 기본값을 반환하는 헬퍼 메서드
     * <p>
     * 이 클래스의 모든 환경변수 읽기 작업을 통합하는 유틸리티 메서드다.
     * null 체크와 기본값 처리를 일관되게 수행하여 코드 중복을 제거한다.
     * </p>
     * 
     * <h4>처리 로직:</h4>
     * <ol>
     *   <li>System.getenv(key)로 환경변수 값 조회</li>
     *   <li>값이 존재하면 해당 값 반환</li>
     *   <li>값이 null이면 기본값 반환</li>
     * </ol>
     * 
     * <h4>사용 예시:</h4>
     * <pre>{@code
     * // 내부에서만 사용되는 헬퍼 메서드
     * String host = getEnvValue("PLANP_HOST", "localhost");
     * String port = getEnvValue("PLANP_PORT", "8080");
     * String database = getEnvValue("MYSQL_DATABASE", "planp_db");
     * }</pre>
     * 
     * @param key 조회할 환경변수명 (예: "PLANP_HOST", "MYSQL_PORT")
     * @param defaultValue 환경변수가 없을 때 사용할 기본값
     * @return 환경변수 값 또는 기본값 (null이 아님을 보장)
     * 
     * @implNote private 메서드로 이 클래스 내부에서만 사용
     */
    private static String getEnvValue(String key, String defaultValue) {
        String value = System.getenv(key);
        return value != null ? value : defaultValue;
    }
    
    /**
     * 현재 적용된 모든 설정 정보를 콘솔에 출력한다.
     * <p>
     * 서버 시작 시 현재 설정 상태를 한눈에 확인할 수 있도록 
     * 모든 주요 설정값을 정리해서 출력한다.
     * </p>
     * 
     * <h4>출력되는 정보:</h4>
     * <ul>
     *   <li>현재 실행 환경 (development/testing/production)</li>
     *   <li>서버 호스트 및 포트</li>
     *   <li>CORS 허용 오리진 목록</li>
     *   <li>MySQL 연결 정보 (호스트, 포트, 데이터베이스명, 사용자명)</li>
     * </ul>
     * 
     * <h4>출력 예시:</h4>
     * <pre>
     * === PlanP 환경 설정 ===
     * 환경: DEVELOPMENT
     * 호스트: localhost
     * 포트: 8080
     * 허용 오리진: http://localhost:3000
     * DB 호스트: localhost:3306
     * DB 이름: planp_development
     * DB 사용자: root
     * ====================
     * </pre>
     * 
     * <h4>주의사항:</h4>
     * <p>
     * 보안상 데이터베이스 비밀번호는 출력하지 않는다.
     * </p>
     * 
     * @apiNote 서버 시작 시 Main 클래스에서 호출되어 설정 확인용으로 사용
     * 
     * @see com.drhong.Main#main(String[])
     */
    public static void printConfig() {
        System.out.println("=== PlanP 환경 설정 ===");
        System.out.println("환경: " + getCurrentEnvironment());
        System.out.println("호스트: " + getHost());
        System.out.println("포트: " + getPort());
        System.out.println("허용 오리진: " + String.join(", ", getAllowedOrigins()));
        System.out.println("DB 호스트: " + getMysqlHost() + ":" + getMysqlPort());
        System.out.println("DB 이름: " + getMysqlDatabase());
        System.out.println("DB 사용자: " + getMysqlUsername());
        System.out.println("====================");
    }
}
