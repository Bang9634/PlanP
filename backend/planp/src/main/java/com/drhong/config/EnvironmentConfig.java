package com.drhong.config;

/**
 * PlanP 백엔드 애플리케이션 환경설정 통합 관리 클래스
 * <p>
 * 서버 실행 환경 및 기본 설정을 관리하고, 환경변수를 읽는 공통 메서드를 제공한다.
 * </p>
 * 
 * <h3>관리하는 설정:</h3>
 * <ul>
 *   <li>HTTP 서버 설정 (호스트, 포트)</li>
 *   <li>CORS 정책 (허용 오리진)</li>
 * </ul>
 * 
 * <h3>설정 우선순위:</h3>
 * <ol>
 *   <li>시스템 환경변수 (최고 우선순위)</li>
 *   <li>전역 기본값 (환경변수가 없을 때 사용)</li>
 * </ol>
 * 
 * <h3>지원하는 환경변수:</h3>
 * <ul>
 *   <li><code>PLANP_HOST</code> - 서버 바인딩 호스트</li>
 *   <li><code>PLANP_PORT</code> - 서버 포트 번호</li>
 *   <li><code>PLANP_ALLOWED_ORIGINS</code> - CORS 허용 오리진 (쉼표로 구분)</li>
 * </ul>
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
     * String host = getEnvValue("PLANP_HOST", "localhost");
     * String port = getEnvValue("PLANP_PORT", "8080");
     * String database = getEnvValue("MYSQL_DATABASE", "planp_db");
     * }</pre>
     * 
     * @param key 조회할 환경변수명 (예: "PLANP_HOST", "MYSQL_PORT")
     * @param defaultValue 환경변수가 없을 때 사용할 기본값
     * @return 환경변수 값 또는 기본값 (null이 아님을 보장)
     */
    public static String getEnvValue(String key, String defaultValue) {
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
     *   <li>서버 호스트 및 포트</li>
     *   <li>CORS 허용 오리진 목록</li>
     * </ul>
     * 
     * <h4>출력 예시:</h4>
     * <pre>
     * === PlanP 환경 설정 ===
     * 호스트: localhost
     * 포트: 8080
     * 허용 오리진: http://localhost:3000
     * ====================
     * </pre>
     * 
     * @apiNote 서버 시작 시 Main 클래스에서 호출되어 설정 확인용으로 사용
     * 
     * @see com.drhong.Main#main(String[])
     */
    public static void printConfig() {
        System.out.println("=== PlanP 환경 설정 ===");
        System.out.println("호스트: " + getHost());
        System.out.println("포트: " + getPort());
        System.out.println("허용 오리진: " + String.join(", ", getAllowedOrigins()));
        System.out.println("====================");
    }
}
