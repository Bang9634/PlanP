package com.drhong.util;

/**
 * 환경 설정 및 상수를 관리하는 유틸리티 클래스
 * <p>
 * 데이터베이스 연결 정보, CORS 정책, 서버 포트 등 
 * 환경별로 변경될 수 있는 설정값들을 중앙집중식으로 관리한다.
 * </p>
 * 
 * <h3>환경변수 사용 방식:</h3>
 * <ul>
 *   <li>시스템 환경변수: <code>System.getenv("VARIABLE_NAME")</code></li>
 *   <li>Java 시스템 속성: <code>System.getProperty("property.name")</code></li>
 *   <li>기본값 (default) 제공으로 안정성 보장</li>
 * </ul>
 * 
 * <h3>설정 방법:</h3>
 * <pre>{@code
 * // 환경변수 설정 (Linux/Mac)
 * export PLANP_CORS_ALLOWED_ORIGINS="http://localhost:3000,http://example.com"
 * export PLANP_CLOUD_SERVER_HOST="example.com"
 * export PLANP_CLOUD_SERVER_PORT="8080"
 * 
 * // 또는 Java 시스템 속성으로 전달
 * java -Dplanp.cors.origins="http://localhost:3000" App
 * }</pre>
 * 
 * @author bang9634
 * @since 2025-11-10
 * 
 * @see com.drhong.server.CorsFilter
 */
public class ConfigUtil {
    
    /**
     * CORS에서 허용할 도메인을 환경변수에서 로드하는 메서드
     * <p>
     * 쉼표(,)로 구분된 여러 도메인을 지원한다.
     * 환경변수가 설정되지 않았을 경우 개발 환경용 기본값을 반환한다.
     * </p>
     * 
     * <h4>환경변수명:</h4>
     * <ul>
     *   <li><code>PLANP_CORS_ALLOWED_ORIGINS</code> - CORS 허용 도메인 (우선순위: 높음)</li>
     *   <li><code>planp.cors.origins</code> - Java 시스템 속성 (우선순위: 낮음)</li>
     * </ul>
     * 
     * <h4>기본값:</h4>
     * <pre>{@code
     * http://localhost:3000,http://localhost:8080,http://127.0.0.1:3000
     * }</pre>
     * 
     * @return 허용할 도메인 목록 (쉼표로 구분)
     */
    public static String getCorsAllowedOrigins() {
        // 1. 시스템 환경변수에서 먼저 확인
        String envOrigins = System.getenv("PLANP_CORS_ALLOWED_ORIGINS");
        if (envOrigins != null && !envOrigins.trim().isEmpty()) {
            return envOrigins;
        }
        
        // 2. Java 시스템 속성에서 확인
        String sysOrigins = System.getProperty("planp.cors.origins");
        if (sysOrigins != null && !sysOrigins.trim().isEmpty()) {
            return sysOrigins;
        }
        
        // 3. 기본값 - 개발 환경용
        return "http://localhost:3000,http://localhost:8080,http://127.0.0.1:3000";
    }
    
    /**
     * 클라우드 서버의 호스트명을 환경변수에서 로드하는 메서드
     * <p>
     * 프로덕션 환경의 서버 IP 또는 도메인을 설정한다.
     * </p>
     * 
     * <h4>환경변수명:</h4>
     * <ul>
     *   <li><code>PLANP_CLOUD_SERVER_HOST</code> - 클라우드 서버 호스트</li>
     *   <li><code>planp.cloud.host</code> - Java 시스템 속성</li>
     * </ul>
     * 
     * <h4>기본값:</h4>
     * <pre>{@code
     * 49.50.133.229 (nCloud 서버)
     * }</pre>
     * 
     * @return 클라우드 서버 호스트명 또는 IP
     */
    public static String getCloudServerHost() {
        // 1. 시스템 환경변수에서 먼저 확인
        String envHost = System.getenv("PLANP_CLOUD_SERVER_HOST");
        if (envHost != null && !envHost.trim().isEmpty()) {
            return envHost;
        }
        
        // 2. Java 시스템 속성에서 확인
        String sysHost = System.getProperty("planp.cloud.host");
        if (sysHost != null && !sysHost.trim().isEmpty()) {
            return sysHost;
        }
        
        // 3. 기본값 - nCloud 서버
        return "49.50.133.229";
    }
    
    /**
     * 클라우드 서버의 포트번호를 환경변수에서 로드하는 메서드
     * <p>
     * 프로덕션 환경의 API 서버 포트를 설정한다.
     * </p>
     * 
     * <h4>환경변수명:</h4>
     * <ul>
     *   <li><code>PLANP_CLOUD_SERVER_PORT</code> - 클라우드 서버 포트</li>
     *   <li><code>planp.cloud.port</code> - Java 시스템 속성</li>
     * </ul>
     * 
     * <h4>기본값:</h4>
     * <pre>{@code
     * 8080
     * }</pre>
     * 
     * @return 클라우드 서버 포트번호
     */
    public static String getCloudServerPort() {
        // 1. 시스템 환경변수에서 먼저 확인
        String envPort = System.getenv("PLANP_CLOUD_SERVER_PORT");
        if (envPort != null && !envPort.trim().isEmpty()) {
            return envPort;
        }
        
        // 2. Java 시스템 속성에서 확인
        String sysPort = System.getProperty("planp.cloud.port");
        if (sysPort != null && !sysPort.trim().isEmpty()) {
            return sysPort;
        }
        
        // 3. 기본값
        return "8080";
    }
    
    /**
     * 지정된 원본(origin)이 허용된 도메인 목록에 포함되어 있는지 확인하는 메서드
     * <p>
     * 환경변수에서 로드한 허용 도메인 목록과 요청의 Origin을 비교한다.
     * </p>
     * 
     * <h4>예시:</h4>
     * <pre>{@code
     * // PLANP_CORS_ALLOWED_ORIGINS = "http://localhost:3000,http://example.com"
     * ConfigUtil.isOriginAllowed("http://localhost:3000") // true
     * ConfigUtil.isOriginAllowed("http://example.com")    // true
     * ConfigUtil.isOriginAllowed("http://unknown.com")    // false
     * }</pre>
     * 
     * @param origin 검증할 요청의 Origin
     * @return 허용된 도메인에 포함되면 true, 그렇지 않으면 false
     */
    public static boolean isOriginAllowed(String origin) {
        if (origin == null || origin.trim().isEmpty()) {
            return false;
        }
        
        String allowedOrigins = getCorsAllowedOrigins();
        String[] origins = allowedOrigins.split(",");
        
        for (String allowed : origins) {
            if (allowed.trim().equals(origin)) {
                return true;
            }
        }
        
        return false;
    }
}
