package com.drhong.server;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.drhong.util.ConfigUtil;
import com.sun.net.httpserver.Filter;
import com.sun.net.httpserver.HttpExchange;

/**
 * CORS(Cross-Origin Resource Sharing) 정책을 처리하는 HTTP 필터
 * <p>
 * 모든 HTTP 요청에 대해 CORS 헤더를 자동으로 추가하여 브라우저의
 * 동일 출처 정책(Same-Origin Policy)으로 인한 차단을 방지한다.
 * 특히 프론트엔드가 다른 포트(localhost:3000)에서 실행될 때 필수적이다.
 * </p>
 *
 * <h3>주요 기능:</h3>
 * <ul>
 *   <li>허용된 Origin 검증 및 CORS 헤더 설정</li>
 *   <li>Preflight 요청(OPTIONS) 자동 처리</li>
 *   <li>개발/프로덕션 환경별 CORS 정책 적용</li>
 *   <li>CORS 관련 요청 로깅 및 모니터링</li>
 * </ul>
 *
 * <h3>처리하는 CORS 헤더:</h3>
 * <ul>
 *   <li><code>Access-Control-Allow-Origin</code> - 허용된 출처 설정</li>
 *   <li><code>Access-Control-Allow-Methods</code> - 허용된 HTTP 메서드</li>
 *   <li><code>Access-Control-Allow-Headers</code> - 허용된 요청 헤더</li>
 *   <li><code>Access-Control-Allow-Credentials</code> - 인증 정보 포함 여부</li>
 *   <li><code>Access-Control-Max-Age</code> - Preflight 결과 캐시 시간</li>
 * </ul>
 *
 * <h3>보안 정책:</h3>
 * <p>
 * 개발 환경에서는 localhost 기반 요청을 허용하고,
 * 알 수 없는 Origin에 대해서는 와일드카드(*)를 사용한다.
 * 프로덕션 환경에서는 더 엄격한 검증이 권장된다.
 * </p>
 *
 * @author bang9634
 * @since 2025-11-10
 *
 * @see com.drhong.server.PlanPServer
 *
 * @implNote HttpServer의 Filter 체인에서 가장 먼저 실행되도록 설정해야 함
 */
public class CorsFilter extends Filter {

    /** SLF4J 로거 인스턴스 - CORS 처리 과정을 로깅 */
    private static final Logger logger = LoggerFactory.getLogger(CorsFilter.class);

    /**
     * HTTP 요청을 필터링하여 CORS 헤더를 추가하는 메인 메서드
     * <p>
     * 모든 HTTP 요청에 대해 실행되며, CORS 헤더를 설정하고 
     * OPTIONS 요청은 바로 처리한다. 다른 요청들은 다음 핸들러로 전달한다.
     * </p>
     *
     * <h4>처리 과정:</h4>
     * <ol>
     *   <li>요청 정보(Origin, Method, Path) 추출 및 로깅</li>
     *   <li>CORS 헤더 설정</li>
     *   <li>OPTIONS 요청인 경우 즉시 200 응답 반환</li>
     *   <li>다른 요청은 다음 필터/핸들러로 전달</li>
     * </ol>
     *
     * @param exchange HTTP 요청/응답 교환 객체
     * @param chain 다음 필터나 핸들러로 요청을 전달하는 체인
     * @throws IOException HTTP 처리 중 I/O 오류가 발생한 경우
     *
     * @implNote OPTIONS 요청은 실제 요청 전에 브라우저가 보내는 Preflight 요청
     */
    @Override
    public void doFilter(HttpExchange exchange, Chain chain) throws IOException {
        // 요청 정보 추출
        String origin = exchange.getRequestHeaders().getFirst("Origin");
        String method = exchange.getRequestMethod();
        String path = exchange.getRequestURI().getPath();
        
        logger.debug("CORS 필터 처리 시작: {} {}, Origin: {}", method, path, origin);
        
        // 모든 요청에 CORS 헤더 설정
        setCorsHeaders(exchange, origin);
        
        // OPTIONS 요청 처리 (Preflight 요청)
        if ("OPTIONS".equals(method)) {
            logger.debug("Preflight 요청 처리 완료: {}", path);
            // 200 OK 응답으로 브라우저에게 CORS 허용 신호
            exchange.sendResponseHeaders(200, -1);
            return; // 여기서 요청 처리 완료
        }
        
        // OPTIONS가 아닌 실제 요청은 다음 핸들러로 전달
        logger.debug("실제 요청을 다음 핸들러로 전달: {} {}", method, path);
        chain.doFilter(exchange);
    }
    
    /**
     * HTTP 응답에 CORS 관련 헤더를 설정하는 헬퍼 메서드
     * <p>
     * 요청의 Origin을 검증하여 적절한 CORS 헤더를 설정한다.
     * 개발 환경에서는 localhost 요청을 허용하고,
     * 알 수 없는 출처에 대해서는 와일드카드를 사용한다.
     * </p>
     *
     * <h4>Origin 검증 로직:</h4>
     * <ul>
     *   <li><strong>localhost 계열:</strong> 정확한 Origin 반환 (개발 환경)</li>
     *   <li><strong>알 수 없는 Origin:</strong> 와일드카드(*) 사용</li>
     *   <li><strong>Origin 없음:</strong> 와일드카드(*) 사용 (직접 API 호출)</li>
     * </ul>
     *
     * @param exchange HTTP 요청/응답 교환 객체
     * @param origin 요청의 Origin 헤더 값 (null 가능)
     *
     * @apiNote 프로덕션 환경에서는 반드시 환경변수를 통해 CORS_ALLOWED_ORIGINS을 설정할 것
     * @see ConfigUtil#getCorsAllowedOrigins()
     */
    private void setCorsHeaders(HttpExchange exchange, String origin) {
        // Origin 기반 Access-Control-Allow-Origin 설정
        if (isAllowedOrigin(origin)) {
            // 허용된 Origin인 경우 정확한 Origin 값 설정
            exchange.getResponseHeaders().set("Access-Control-Allow-Origin", origin);
            logger.debug("허용된 Origin 설정: {}", origin);
        } else {
            // 알 수 없는 Origin이거나 Origin이 없는 경우 와일드카드 사용
            exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
            logger.debug("와일드카드 Origin 설정: origin={}", origin);
        }

        // 허용된 HTTP 메서드 설정
        exchange.getResponseHeaders().set("Access-Control-Allow-Methods",
            "GET, POST, PUT, DELETE, OPTIONS");

        // 허용된 요청 헤더 설정
        exchange.getResponseHeaders().set("Access-Control-Allow-Headers",
            "Content-Type, Accept, Authorization, X-Requested-With, Origin");

        // 인증 정보(쿠키, 인증 헤더) 포함 여부 - 현재는 비허용
        exchange.getResponseHeaders().set("Access-Control-Allow-Credentials", "false");

        // Preflight 결과 캐시 시간 (3600초 = 1시간)
        exchange.getResponseHeaders().set("Access-Control-Max-Age", "3600");

        logger.debug("CORS 헤더 설정 완료");
    }

    /**
     * 주어진 Origin이 허용된 출처인지 검증하는 헬퍼 메서드
     * <p>
     * ConfigUtil을 사용하여 환경변수에서 허용된 도메인 목록을 동적으로 로드한다.
     * 개발 환경에서 사용하는 localhost 기반 URL들과 프로덕션 도메인을 지원한다.
     * 보안을 위해 ConfigUtil에서 관리하는 명시적 목록만 허용한다.
     * </p>
     * 
     * <h4>환경변수를 통한 설정:</h4>
     * <pre>{@code
     * # 환경변수로 허용할 도메인 지정
     * export PLANP_CORS_ALLOWED_ORIGINS="http://localhost:3000,http://example.com,https://app.example.com"
     * export PLANP_CLOUD_SERVER_HOST="49.50.133.229"
     * export PLANP_CLOUD_SERVER_PORT="8080"
     * 
     * # 또는 Java 시스템 속성 사용
     * java -Dplanp.cors.origins="http://localhost:3000,http://example.com" App
     * }</pre>
     * 
     * <h4>기본 허용 도메인 (환경변수 미설정 시):</h4>
     * <ul>
     *   <li><code>http://localhost:3000</code> - 프론트엔드 개발 서버</li>
     *   <li><code>http://localhost:8080</code> - 백엔드 API 서버</li>
     *   <li><code>http://127.0.0.1:3000</code> - IP 기반 localhost</li>
     * </ul>
     * 
     * @param origin 검증할 Origin 문자열
     * @return 허용된 Origin이면 true, 그렇지 않으면 false
     * 
     * @implNote 프로덕션 배포 시에는 반드시 환경변수를 통해 도메인을 명시적으로 설정할 것
     * 
     * @see ConfigUtil#isOriginAllowed(String)
     * @see ConfigUtil#getCorsAllowedOrigins()
     */
    private boolean isAllowedOrigin(String origin) {
        if (origin == null) {
            logger.debug("요청에 Origin 헤더가 없음: 와일드카드 적용");
            return false; // Origin이 없으면 와일드카드 사용하지 않고 처리
        }
        
        // ConfigUtil을 통해 환경변수 기반 허용 도메인 검증
        boolean allowed = ConfigUtil.isOriginAllowed(origin);
        
        // 클라우드 서버 자체의 Origin도 허용 (localhost 패턴 호환)
        if (!allowed) {
            String cloudHost = ConfigUtil.getCloudServerHost();
            String cloudPort = ConfigUtil.getCloudServerPort();
            String cloudOrigin = "http://" + cloudHost + ":" + cloudPort;
            
            if (origin.equals(cloudOrigin)) {
                allowed = true;
                logger.debug("클라우드 서버 Origin 허용: {}", origin);
            }
        }
        
        // localhost 계열 주소도 개발 환경에서는 계속 허용 (호환성 유지)
        if (!allowed && isLocalhost(origin)) {
            allowed = true;
            logger.debug("개발 환경 localhost Origin 허용: {}", origin);
        }
        
        logger.debug("Origin 검증 결과: origin={}, allowed={}", origin, allowed);
        return allowed;
    }
    
    /**
     * 주어진 URL이 localhost 기반 주소인지 확인하는 헬퍼 메서드
     * <p>
     * 개발 환경에서의 호환성을 위해 localhost 및 127.0.0.1 주소를 감지한다.
     * </p>
     * 
     * @param origin 검증할 Origin 문자열
     * @return localhost 기반이면 true, 그렇지 않으면 false
     */
    private boolean isLocalhost(String origin) {
        return origin != null && (
            origin.startsWith("http://localhost:") ||
            origin.startsWith("http://127.0.0.1:")
        );
    }

    /**
     * 필터에 대한 설명을 반환하는 메서드
     * <p>
     * HttpServer의 관리 도구나 모니터링 시스템에서 
     * 이 필터의 역할을 식별하기 위해 사용된다.
     * </p>
     * 
     * @return 필터의 역할을 설명하는 문자열
     */
    @Override
    public String description() {
        return "CORS Filter for PlanP API - Cross-Origin Resource Sharing 정책 처리";
    }
}