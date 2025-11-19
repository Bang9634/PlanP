package com.drhong.server;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

/**
 * 서버 헬스 체크를 위한 HTTP 핸들러
 * <p>
 * 서버의 상태, 버전, 실행 시간 등의 정보를 JSON 형태로 제공하는 헬스 체크 엔드포인트를 처리한다.
 * 로드 밸런서, 모니터링 시스템, 운영팀에서 서버의 정상 작동 여부를 확인하는데 사용된다.
 * </p>
 * 
 * <h3>주요 기능:</h3>
 * <ul>
 *   <li>서버 상태 정보 제공 (상태, 타임스탬프, 서비스명, 버전)</li>
 *   <li>JSON 형태의 구조화된 응답</li>
 *   <li>CORS 헤더 자동 설정으로 브라우저 호환성 보장</li>
 *   <li>빠른 응답 시간으로 헬스 체크 최적화</li>
 * </ul>
 * 
 * <h3>API 엔드포인트:</h3>
 * <pre>{@code
 * GET /health
 * 
 * 응답 예시:
 * {
 *   "status": "UP",
 *   "timestamp": "2025-11-11T15:30:45.123",
 *   "service": "PlanP Backend",
 *   "version": "1.0.0",
 *   "uptime": "2 hours 15 minutes"
 * }
 * }</pre>
 * 
 * @author bang9634
 * @since 2025-11-10
 * 
 * @see com.sun.net.httpserver.HttpHandler
 * @see com.drhong.server.PlanPServer
 * 
 * @implNote 이 핸들러는 가능한 한 빠르게 응답해야 하므로 복잡한 로직이나 외부 의존성을 피해야 함
 */
public class HealthCheckHandler implements HttpHandler {

    /** SLF4J 로거 인스턴스 - 헬스 체크 요청을 로깅 */
    private static final Logger logger = LoggerFactory.getLogger(HealthCheckHandler.class);
    
    /** JSON 직렬화를 위한 Gson 인스턴스 */
    private final Gson gson = new Gson();
    
    /** 서버 시작 시간 (업타임 계산용) */
    private static final long SERVER_START_TIME = System.currentTimeMillis();

    /** 날짜/시간 포맷터 (ISO 8601 형식) */
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS");

    /**
     * 헬스 체크 요청을 처리하는 메인 메서드
     * <p>
     * GET 요청만 허용하며, 서버의 현재 상태 정보를 JSON 형태로 반환한다.
     * 빠른 응답을 위해 최소한의 처리만 수행한다.
     * </p>
     * 
     * <h4>처리 과정:</h4>
     * <ol>
     *   <li>HTTP 메서드 검증 (GET만 허용)</li>
     *   <li>CORS 헤더 설정</li>
     *   <li>헬스 상태 정보 수집</li>
     *   <li>JSON 응답 생성 및 전송</li>
     * </ol>
     * 
     * <h4>응답 정보:</h4>
     * <ul>
     *   <li><strong>status:</strong> "UP" (서버 정상 작동)</li>
     *   <li><strong>timestamp:</strong> 현재 시간 (ISO 8601 형식)</li>
     *   <li><strong>service:</strong> 서비스명 ("PlanP Backend")</li>
     *   <li><strong>version:</strong> 현재 버전</li>
     *   <li><strong>uptime:</strong> 서버 가동 시간 (human-readable)</li>
     * </ul>
     * 
     * @param exchange HTTP 요청/응답 교환 객체
     * @throws IOException HTTP 처리 중 I/O 오류가 발생한 경우
     * 
     * @apiNote 이 메서드는 헬스 체크 목적으로 최대한 빠르게 응답해야 함
     */
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        String clientIP = exchange.getRemoteAddress().getAddress().getHostAddress();
        
        logger.debug("헬스 체크 요청 수신: method={}, clientIP={}", method, clientIP);
        
        // HTTP 메서드 검증 - GET만 허용
        if (!"GET".equals(method)) {
            logger.warn("헬스 체크에 잘못된 HTTP 메서드 사용: method={}, clientIP={}", method, clientIP);
            sendErrorResponse(exchange, 405, "Method Not Allowed - GET 요청만 지원됩니다");
            return;
        }
        
        try {
            // 헬스 상태 정보 수집
            Map<String, Object> healthStatus = createHealthStatusInfo();
            
            // JSON 응답 생성
            String jsonResponse = gson.toJson(healthStatus);
            
            logger.debug("헬스 체크 응답 생성 완료: status={}", healthStatus.get("status"));
            
            // 성공 응답 전송
            sendHealthResponse(exchange, jsonResponse);
            
        } catch (JsonSyntaxException e) {
            logger.error("JSON 파싱 오류: clientIP={}", clientIP, e);
            sendErrorResponse(exchange, 400, "Invalid JSON format: " + e.getMessage());
        } catch (IOException | RuntimeException e) {
            logger.error("헬스 체크 처리 오류: clientIP={}", clientIP, e);
            sendErrorResponse(exchange, 500, "Internal server error");
        }
    }

    /**
     * 서버의 헬스 상태 정보를 수집하는 헬퍼 메서드
     * <p>
     * 현재 시간, 서버 버전, 가동 시간 등의 정보를 Map 형태로 수집한다.
     * 향후 데이터베이스 연결 상태, 메모리 사용량 등을 추가할 수 있다.
     * </p>
     * 
     * @return 헬스 상태 정보를 담은 Map 객체
     * 
     * @implNote 이 메서드는 빠른 응답을 위해 복잡한 검사를 피하고 기본 정보만 수집
     */
    private Map<String, Object> createHealthStatusInfo() {
        Map<String, Object> healthStatus = new HashMap<>();
        
        // 기본 상태 정보
        healthStatus.put("status", "UP");
        healthStatus.put("timestamp", LocalDateTime.now().format(DATE_TIME_FORMATTER));
        healthStatus.put("service", "PlanP Backend");
        healthStatus.put("version", "1.0.0");
        
        // 서버 가동 시간 추가
        healthStatus.put("uptime", calculateUptime());
        
        // JVM 기본 정보 (선택사항)
        healthStatus.put("javaVersion", System.getProperty("java.version"));
        
        // 추가 가능한 정보들 (향후 확장)
        // healthStatus.put("dbStatus", checkDatabaseConnection());
        // healthStatus.put("memoryUsage", getMemoryUsage());
        // healthStatus.put("activeConnections", getActiveConnectionCount());
        
        return healthStatus;
    }

    /**
     * 서버 가동 시간을 계산하여 사람이 읽기 쉬운 형태로 반환하는 헬퍼 메서드
     * <p>
     * 서버 시작 시간부터 현재까지의 경과 시간을 계산하여
     * "X days, Y hours, Z minutes" 형태의 문자열로 변환한다.
     * </p>
     * 
     * @return 사람이 읽기 쉬운 형태의 가동 시간 문자열
     */
    private String calculateUptime() {
        long uptimeMillis = System.currentTimeMillis() - SERVER_START_TIME;
        long seconds = uptimeMillis / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;
        
        if (days > 0) {
            return String.format("%d days, %d hours, %d minutes", 
                days, hours % 24, minutes % 60);
        } else if (hours > 0) {
            return String.format("%d hours, %d minutes", 
                hours, minutes % 60);
        } else if (minutes > 0) {
            return String.format("%d minutes, %d seconds", 
                minutes, seconds % 60);
        } else {
            return String.format("%d seconds", seconds);
        }
    }

    /**
     * 성공적인 헬스 체크 응답을 전송하는 헬퍼 메서드
     * <p>
     * JSON 형태의 헬스 상태 정보를 HTTP 200 OK 응답으로 전송한다.
     * CORS 헤더를 포함하여 브라우저에서도 접근 가능하도록 한다.
     * </p>
     * 
     * @param exchange HTTP 요청/응답 교환 객체
     * @param jsonResponse 전송할 JSON 응답 문자열
     * @throws IOException 응답 전송 중 I/O 오류가 발생한 경우
     */
    private void sendHealthResponse(HttpExchange exchange, String jsonResponse) throws IOException {
        // HTTP 응답 헤더 설정
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET");
        exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type");
        exchange.getResponseHeaders().set("Cache-Control", "no-cache"); // 헬스 체크 결과는 캐시하지 않음
        
        // UTF-8 바이트 배열로 변환
        byte[] responseBytes = jsonResponse.getBytes(StandardCharsets.UTF_8);
        
        // HTTP 200 OK 응답 전송
        exchange.sendResponseHeaders(200, responseBytes.length);
        
        // 응답 본문 전송
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(responseBytes);
        }
        
        logger.debug("헬스 체크 응답 전송 완료: length={} bytes", responseBytes.length);
    }

    /**
     * 오류 응답을 전송하는 헬퍼 메서드
     * <p>
     * 헬스 체크 처리 중 발생한 오류에 대해 적절한 HTTP 상태 코드와 
     * 오류 메시지를 포함한 JSON 응답을 전송한다.
     * </p>
     * 
     * @param exchange HTTP 요청/응답 교환 객체
     * @param statusCode HTTP 오류 상태 코드
     * @param message 오류 메시지
     * @throws IOException 응답 전송 중 I/O 오류가 발생한 경우
     */
    private void sendErrorResponse(HttpExchange exchange, int statusCode, String message) throws IOException {
        // 표준 오류 응답 구조
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("status", "ERROR");
        errorResponse.put("error", message);
        errorResponse.put("timestamp", LocalDateTime.now().format(DATE_TIME_FORMATTER));
        
        String jsonResponse = gson.toJson(errorResponse);
        
        // HTTP 응답 헤더 설정
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        
        // UTF-8 바이트 배열로 변환
        byte[] responseBytes = jsonResponse.getBytes(StandardCharsets.UTF_8);
        
        // 오류 응답 전송
        exchange.sendResponseHeaders(statusCode, responseBytes.length);
        
        // 응답 본문 전송
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(responseBytes);
        }
        
        logger.warn("헬스 체크 오류 응답 전송: statusCode={}, message={}", statusCode, message);
    }
}