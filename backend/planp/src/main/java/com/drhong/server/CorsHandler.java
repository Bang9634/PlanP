package com.drhong.server;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

/**
 * 기본 경로와 알 수 없는 경로에 대한 CORS 처리를 담당하는 HTTP 핸들러
 * <p>
 * 이 핸들러는 주로 루트 경로("/")와 정의되지 않은 경로들에 대한 요청을 처리한다.
 * 모든 요청에 CORS 헤더를 추가하고, 서버 상태 확인이나 404 응답을 제공한다.
 * CorsFilter와 함께 사용되어 완전한 CORS 지원을 제공한다.
 * </p>
 * 
 * <h3>주요 기능:</h3>
 * <ul>
 *   <li>루트 경로("/") 접근 시 서버 상태 메시지 제공</li>
 *   <li>정의되지 않은 경로에 대한 404 응답 처리</li>
 *   <li>모든 응답에 CORS 헤더 자동 추가</li>
 *   <li>OPTIONS 요청(Preflight) 처리</li>
 * </ul>
 * 
 * <h3>처리하는 요청:</h3>
 * <ul>
 *   <li><strong>GET /</strong> → "PlanP Backend Server is running!" 메시지</li>
 *   <li><strong>OPTIONS *</strong> → CORS Preflight 응답 (200 OK)</li>
 *   <li><strong>* /*</strong> → 404 Not Found (정의되지 않은 경로)</li>
 * </ul>
 * 
 * <h3>CORS 정책:</h3>
 * <p>
 * localhost와 127.0.0.1 기반 요청을 허용하며, 
 * 알 수 없는 Origin에 대해서는 와일드카드(*)를 사용한다.
 * </p>
 * 
 * @author bang9634
 * @since 2025-11-10
 * 
 * @see com.sun.net.httpserver.HttpHandler
 * @see com.drhong.server.CorsFilter
 * @see com.drhong.server.PlanPServer
 * 
 * @implNote 이 핸들러는 fallback 역할을 하므로 다른 구체적인 핸들러들보다 나중에 등록되어야 함
 */
public class CorsHandler implements HttpHandler {

    /** SLF4J 로거 인스턴스 - 핸들러 처리 과정을 로깅 */
    private static final Logger logger = LoggerFactory.getLogger(CorsHandler.class);

    /**
     * HTTP 요청을 처리하는 메인 메서드
     * <p>
     * 요청 경로에 따라 적절한 응답을 생성하고, 모든 응답에 CORS 헤더를 추가한다.
     * 이 핸들러는 주로 정의되지 않은 경로나 루트 경로에 대한 fallback 역할을 한다.
     * </p>
     * 
     * <h4>처리 과정:</h4>
     * <ol>
     *   <li>요청 정보(경로, 메서드, Origin) 추출 및 로깅</li>
     *   <li>모든 응답에 CORS 헤더 설정</li>
     *   <li>OPTIONS 요청 처리 (Preflight 응답)</li>
     *   <li>루트 경로("/") 처리 (서버 상태 메시지)</li>
     *   <li>기타 경로 처리 (404 Not Found)</li>
     * </ol>
     * 
     * <h4>응답 예시:</h4>
     * <pre>{@code
     * GET / → 200 OK: "PlanP Backend Server is running!"
     * OPTIONS /api/test → 200 OK: (빈 응답, CORS 헤더만)
     * GET /unknown → 404 Not Found: "404 - Path not found: /unknown"
     * }</pre>
     * 
     * @param exchange HTTP 요청/응답 교환 객체
     * @throws IOException HTTP 처리 중 I/O 오류가 발생한 경우
     * 
     * @apiNote 이 핸들러는 모든 미처리 요청의 마지막 처리자 역할을 함
     */
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        // 요청 정보 추출
        String path = exchange.getRequestURI().getPath();
        String method = exchange.getRequestMethod();
        String origin = exchange.getRequestHeaders().getFirst("Origin");
        
        logger.debug("CORS 핸들러 요청 수신: {} {}, Origin: {}", method, path, origin);

        // 모든 응답에 CORS 헤더 설정
        setCorsHeaders(exchange, origin);

        // OPTIONS 요청 처리 (Preflight)
        if ("OPTIONS".equals(method)) {
            handleOptionsRequest(exchange, path, origin);
            return;
        }

        // 루트 경로는 기본 응답 제공
        if ("/".equals(path)) {
            handleRootPath(exchange);
            return;
        }

        // 다른 경로는 404 응답
        handleNotFoundPath(exchange, method, path);
    }
    
    /**
     * OPTIONS 요청(Preflight)을 처리하는 헬퍼 메서드
     * <p>
     * 브라우저가 실제 요청을 보내기 전에 CORS 정책을 확인하기 위해 
     * 보내는 Preflight 요청을 처리한다. 200 OK 응답을 반환하여 
     * 브라우저에게 실제 요청이 허용됨을 알린다.
     * </p>
     * 
     * @param exchange HTTP 요청/응답 교환 객체
     * @param path 요청 경로 (로깅용)
     * @param origin 요청 Origin (로깅용)
     * @throws IOException 응답 전송 중 오류가 발생한 경우
     * 
     * @implNote Preflight 응답에는 응답 본문이 없으므로 Content-Length는 -1로 설정
     */
    private void handleOptionsRequest(HttpExchange exchange, String path, String origin) throws IOException {
        logger.debug("CORS Preflight 요청 처리: path={}, Origin={}", path, origin);
        
        // 200 OK 응답, 응답 본문 없음 (-1로 설정)
        exchange.sendResponseHeaders(200, -1);
        
        logger.debug("Preflight 응답 전송 완료: path={}", path);
    }

    /**
     * 루트 경로("/") 요청을 처리하는 헬퍼 메서드
     * <p>
     * 서버가 정상적으로 실행 중임을 알리는 메시지를 반환한다.
     * 헬스 체크나 서버 상태 확인 목적으로 사용할 수 있다.
     * </p>
     * 
     * @param exchange HTTP 요청/응답 교환 객체
     * @throws IOException 응답 전송 중 오류가 발생한 경우
     * 
     * @apiNote 이 응답은 브라우저에서 직접 접근했을 때 서버 상태를 쉽게 확인할 수 있게 해줌
     */
    private void handleRootPath(HttpExchange exchange) throws IOException {
        String response = "PlanP Backend Server is running!";
        
        logger.debug("루트 경로 요청 처리: 서버 상태 메시지 반환");
        
        // Content-Type 헤더 설정 (UTF-8 인코딩 명시)
        exchange.getResponseHeaders().set("Content-Type", "text/plain; charset=UTF-8");
        
        // UTF-8 바이트 배열로 변환하여 정확한 길이 계산
        byte[] responseBytes = response.getBytes(StandardCharsets.UTF_8);
        
        // HTTP 응답 헤더 전송
        exchange.sendResponseHeaders(200, responseBytes.length);
        
        // 응답 본문 전송 (try-with-resources로 자동 스트림 닫기)
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(responseBytes);
        }
        
        logger.debug("루트 경로 응답 전송 완료: length={} bytes", responseBytes.length);
    }

    /**
     * 정의되지 않은 경로에 대한 404 응답을 처리하는 헬퍼 메서드
     * <p>
     * 등록된 핸들러가 없는 경로에 대해 404 Not Found 응답을 생성한다.
     * 요청된 경로 정보를 포함하여 디버깅에 도움이 되도록 한다.
     * </p>
     * 
     * @param exchange HTTP 요청/응답 교환 객체
     * @param method HTTP 메서드 (로깅용)
     * @param path 요청된 경로
     * @throws IOException 응답 전송 중 오류가 발생한 경우
     * 
     * @apiNote 프로덕션에서는 보안상 경로 정보를 노출하지 않는 것을 고려할 수 있음
     */
    private void handleNotFoundPath(HttpExchange exchange, String method, String path) throws IOException {
        logger.debug("404 - 정의되지 않은 경로 요청: {} {}", method, path);
        
        String notFoundResponse = "404 - Path not found: " + path;
        
        // Content-Type 헤더 설정
        exchange.getResponseHeaders().set("Content-Type", "text/plain; charset=UTF-8");
        
        // UTF-8 바이트 배열로 변환
        byte[] responseBytes = notFoundResponse.getBytes(StandardCharsets.UTF_8);
        
        // 404 Not Found 응답 전송
        exchange.sendResponseHeaders(404, responseBytes.length);
        
        // 응답 본문 전송
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(responseBytes);
        }
        
        logger.warn("404 응답 전송: {} {} → {}", method, path, notFoundResponse);
    }

     /**
     * HTTP 응답에 CORS 관련 헤더를 설정하는 헬퍼 메서드
     * <p>
     * 요청의 Origin을 검증하여 적절한 CORS 헤더를 설정한다.
     * CorsFilter와 유사한 정책을 적용하여 일관된 CORS 처리를 보장한다.
     * </p>
     * 
     * <h4>Origin 검증 로직:</h4>
     * <ul>
     *   <li><strong>localhost/127.0.0.1 기반:</strong> 정확한 Origin 반환</li>
     *   <li><strong>기타 Origin:</strong> 와일드카드(*) 사용</li>
     * </ul>
     * 
     * <h4>설정하는 CORS 헤더:</h4>
     * <ul>
     *   <li><code>Access-Control-Allow-Origin</code> - 허용된 출처</li>
     *   <li><code>Access-Control-Allow-Methods</code> - 허용된 HTTP 메서드</li>
     *   <li><code>Access-Control-Allow-Headers</code> - 허용된 요청 헤더</li>
     *   <li><code>Access-Control-Allow-Credentials</code> - 인증 정보 포함 여부</li>
     *   <li><code>Access-Control-Max-Age</code> - Preflight 캐시 시간</li>
     * </ul>
     * 
     * @param exchange HTTP 요청/응답 교환 객체
     * @param origin 요청의 Origin 헤더 값 (null 가능)
     * 
     * @see com.drhong.server.CorsFilter#setCorsHeaders(HttpExchange, String)
     */
    private void setCorsHeaders(HttpExchange exchange, String origin) {
        // Origin 검증 및 Access-Control-Allow-Origin 설정
        if (isLocalOrigin(origin)) {
            // 로컬 개발 환경의 요청인 경우 정확한 Origin 설정
            exchange.getResponseHeaders().set("Access-Control-Allow-Origin", origin);
            logger.debug("로컬 Origin 허용: {}", origin);
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

        // 인증 정보 포함 여부 (현재는 비허용)
        exchange.getResponseHeaders().set("Access-Control-Allow-Credentials", "false");

        // Preflight 결과 캐시 시간 (1시간)
        exchange.getResponseHeaders().set("Access-Control-Max-Age", "3600");
        
        logger.debug("CORS 헤더 설정 완료: Origin={}", origin);
    }

    /**
     * 주어진 Origin이 로컬 개발 환경의 요청인지 확인하는 헬퍼 메서드
     * <p>
     * localhost와 127.0.0.1로 시작하는 Origin들을 로컬 요청으로 판단한다.
     * 개발 환경에서 주로 사용되는 패턴들을 허용한다.
     * </p>
     * 
     * @param origin 검증할 Origin 문자열
     * @return 로컬 Origin이면 true, 그렇지 않으면 false
     * 
     * @implNote CorsFilter의 isAllowedOrigin() 메서드와 유사한 로직
     */
    private boolean isLocalOrigin(String origin) {
        return origin != null && (
            origin.startsWith("http://localhost:") || 
            origin.startsWith("http://127.0.0.1:")
        );
    }
}