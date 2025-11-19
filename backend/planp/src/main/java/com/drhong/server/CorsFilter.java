package com.drhong.server;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.drhong.config.EnvironmentConfig;
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

    /** 환경변수 기반 허용 오리진 목록 */
    private final List<String> allowedOrigins;

    public CorsFilter() {
        this.allowedOrigins = Arrays.asList(EnvironmentConfig.getAllowedOrigins());
        logger.info("Cors 허용 오리진 목록: {}", allowedOrigins);
    }

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
     * 허용된 Origin의 경우 정확한 Origin을 설정하고,
     * 허용되지 않은 Origin에 대해서는 null을 설정한다.
     * </p>
     * 
     * <h4>Origin 검증 로직:</h4>
     * <ul>
     *   <li><strong>허용된 Origin:</strong> 정확한 Origin 설정</li>
     *   <li><strong>허용되지 않는 Origin:</strong> null 설정</li>
     * </ul>
     * 
     * @param exchange HTTP 요청/응답 교환 객체
     * @param origin 요청의 Origin 헤더 값 (null 가능)
     */
    private void setCorsHeaders(HttpExchange exchange, String origin) {
        // 환경변수 기반 Origin 검증
        if (origin != null && isAllowedOrigin(origin)) {
            // 허용된 Origin인 경우 정확한 Origin 값 설정
            exchange.getResponseHeaders().set("Access-Control-Allow-Origin", origin);
            logger.debug("허용된 Origin 설정: {}", origin);
        } else {
            // 허용되지 않는 Origin 차단
            exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "null");
            logger.warn("허용되지 않은 Origin 요청: {}", origin);
        }

        // 허용된 HTTP 메서드 설정
        exchange.getResponseHeaders().set("Access-Control-Allow-Methods",
            "GET, POST, PUT, DELETE, OPTIONS");

        // 허용된 요청 헤더 설정
        exchange.getResponseHeaders().set("Access-Control-Allow-Headers",
            "Content-Type, Accept, Authorization, X-Requested-With, Origin");

        // 인증 정보(쿠키, 인증 헤더) 포함 여부
        exchange.getResponseHeaders().set("Access-Control-Allow-Credentials", "true");

        // Preflight 결과 캐시 시간 (3600초 = 1시간)
        exchange.getResponseHeaders().set("Access-Control-Max-Age", "3600");

        logger.debug("CORS 헤더 설정 완료");
    }

    /**
     * 주어진 Origin이 허용된 출처인지 검증하는 헬퍼 메서드
     * <p>
     * 환경변수를 기반으로 Origin을 검증한다.
     * </p>
     * 
     * @param origin 검증할 Origin 문자열
     * @return 허용된 Origin이면 true, 그렇지 않으면 false
     */
    private boolean isAllowedOrigin(String origin) {
        if (origin == null) {
            return false;
        }

        // 정확한 매치
        if (allowedOrigins.contains(origin)) {
            return true;
        }

        // 와일드카드 매치 (예: http://localhost:*)
        return allowedOrigins.stream().anyMatch(allowed -> {
            if (allowed.endsWith("*")) {
                String prefix = allowed.substring(0, allowed.length() - 1);
                return origin.startsWith(prefix);
            }
            return false;
        });
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
        return "CORS Filter for PlanP API - 환경변수 기반 Cross-Origin Resource Sharing 정책 처리";
    }
}