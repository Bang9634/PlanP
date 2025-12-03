package com.drhong.handler;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

public abstract class BaseHandler implements HttpHandler {
    protected final Logger logger = LoggerFactory.getLogger(getClass());
    protected final Gson gson = new Gson();

        
    private final Map<String, Map<String, RouteHandler>> routes = new HashMap<>();

    /**
     * 라우트 핸들러 함수형 인터페이스
     */
    @FunctionalInterface
    protected interface RouteHandler {
        void handle(HttpExchange exchange) throws IOException;
    }

    /**
     * BaseHandler 생성자
     * 
     * <p>
     * 자식 클래스에서 registerRoutes()를 호출하여 라우트를 등록해야 한다.
     * </p>
     */
    public BaseHandler() {
        registerRoutes();
    }

    /**
     * 라우트를 등록하는 추상 메서드
     * 
     * <p>
     * 자식 클래스에서 반드시 구현해야 하며,
     * get(), post(), put(), delete() 메서드를 사용하여 라우트를 등록한다.
     * </p>
     */
    protected abstract void registerRoutes();


    /**
     * GET 요청 라우트 등록
     * 
     * @param path 엔드포인트 경로 (예: "/check-userid")
     * @param handler 요청 처리 핸들러
     */
    protected void get(String path, RouteHandler handler) {
        addRoute("GET", path, handler);
    }
    
    /**
     * POST 요청 라우트 등록
     * 
     * @param path 엔드포인트 경로 (예: "/signup")
     * @param handler 요청 처리 핸들러
     */
    protected void post(String path, RouteHandler handler) {
        addRoute("POST", path, handler);
    }
    
    /**
     * PUT 요청 라우트 등록
     * 
     * @param path 엔드포인트 경로 (예: "/update-user")
     * @param handler 요청 처리 핸들러
     */
    protected void put(String path, RouteHandler handler) {
        addRoute("PUT", path, handler);
    }


    /**
     * DELETE 요청 라우트 등록
     * 
     * @param path 엔드포인트 경로 (예: "/delete-user")
     * @param handler 요청 처리 핸들러
     */
    protected void delete(String path, RouteHandler handler) {
        addRoute("DELETE", path, handler);
    }


    /**
     * 라우트 추가 (내부 메서드)
     */
    private void addRoute(String method, String path, RouteHandler handler) {
        routes.computeIfAbsent(method, k -> new HashMap<>()).put(path, handler);
        logger.debug("라우트 등록: {} {}", method, path);
    }


    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String fullPath = exchange.getRequestURI().getPath();
        String method = exchange.getRequestMethod();
        
        logger.debug("요청 수신: {} {}", method, fullPath);
        
        try {
            // 메서드별 라우트 테이블 가져오기
            Map<String, RouteHandler> methodRoutes = routes.get(method);
            
            if (methodRoutes == null) {
                // 405 Method Not Allowed
                logger.warn("지원하지 않는 HTTP 메서드: {}", method);
                sendErrorResponse(exchange, 405, "허용되지 않은 HTTP 메서드");
                return;
            }
            
            // 엔드포인트 추출 (예: /api/user/login → /login)
            String endpoint = extractEndpoint(fullPath);
            RouteHandler handler = methodRoutes.get(endpoint);
            
            if (handler == null) {
                // 404 Not Found
                logger.warn("존재하지 않는 엔드포인트: {} {}", method, endpoint);
                sendErrorResponse(exchange, 404, "존재하지 않는 엔드포인트");
                return;
            }
            
            // 핸들러 실행
            handler.handle(exchange);
        
        } catch (IOException e) {
            logger.error("요청 처리 중 오류 발생", e);
            sendErrorResponse(exchange, 500, "서버 내부 오류");
        }
    }


    /**
     * 전체 경로에서 엔드포인트 추출
     * 
     * <p>
     * 기본 경로를 제거하고 실제 엔드포인트만 추출한다.
     * 자식 클래스에서 오버라이드하여 커스터마이징 가능.
     * </p>
     * 
     * @param fullPath 전체 요청 경로 (예: /api/user/login)
     * @return 엔드포인트 (예: /login)
     */
    protected String extractEndpoint(String fullPath) {
        // 기본 구현: 전체 경로 반환
        // 자식 클래스에서 오버라이드하여 기본 경로 제거 가능
        return fullPath;
    }



    /**
     * JSON 형태의 성공 응답을 클라이언트에게 전송하는 헬퍼 메서드
     * <p>
     * HTTP 헤더를 적절히 설정하고 JSON 데이터를 UTF-8 인코딩으로 전송한다.
     * Content-Type은 application/json으로 설정되어 클라이언트가 응답을 올바르게 파싱할 수 있게 한다.
     * </p>
     * 
     * @param exchange HTTP 요청/응답 처리를 위한 교환 객체
     * @param statusCode HTTP 응답 상태 코드 (200, 400, 500 등)
     * @param jsonResponse 클라이언트에게 전송할 JSON 문자열
     * @throws IOException 응답 전송 중 네트워크 오류가 발생한 경우
     */
    protected void sendResponse(HttpExchange exchange, int statusCode, Object data) throws IOException {
        logger.debug("JSON 응답 전송 시작");
        String jsonResponse = gson.toJson(data);

        // HTTP 응답 헤더 설정
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        // JSON 문자열을 UTF-8 바이트 배열로 변환
        byte[] responseBytes = jsonResponse.getBytes(StandardCharsets.UTF_8);
        // HTTP 응답 헤더 전송 (상태 코드와 Content-Length를 포함)
        exchange.sendResponseHeaders(statusCode, responseBytes.length);
        // HTTP 응답 body 전송 (try-with-resources로 자동 스트림 닫기)
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(responseBytes);
        }
        logger.debug("JSON 응답 전송 완료: statusCode={}, length={} bytes", statusCode, responseBytes.length);
    }
        
     /**
     * 표준화된 오류 응답을 JSON 형태로 클라이언트에게 전송하는 헬퍼 메서드
     * <p>
     * 모든 오류 응답은 동일한 구조를 가지도록 표준화되어 있다.
     * 클라이언트는 success 필드를 통해 요청의 성공 여부를 쉽게 판단할 수 있다.
     * </p>
     * 
     * @param exchange HTTP 요청/응답 처리를 위한 교환 객체
     * @param statusCode HTTP 오류 상태 코드 (400, 404, 405, 500 등)
     * @param message 클라이언트에게 전달할 오류 메시지
     * @throws IOException 응답 전송 중 네트워크 오류가 발생한 경우
     */
    protected void sendErrorResponse(HttpExchange exchange, int statusCode, String message) throws IOException {
        logger.warn("오류 응답 전송 시작");
        // 표준 오류 reponse 구조 생성
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("success", false);                 // 요청 실패 표시
        errorResponse.put("message", message);                        // 사용자에게 전달할 오류 메시지
        errorResponse.put("timestamp", System.currentTimeMillis()); // 오류 발생 시각 (ms)
        // JSON 응답 전달
        sendResponse(exchange, statusCode, errorResponse);
        logger.warn("오류 응답 전송 완료: statusCode={}, message={}", statusCode, message);
    }

    /**
     * 표준 성공 응답 전송
     * 
     * @param exchange HTTP 교환 객체
     * @param data 응답 데이터
     * @throws IOException 응답 전송 실패 시
     */
    protected void sendSuccessResponse(HttpExchange exchange, Object data) throws IOException {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", data);
        response.put("timestamp", System.currentTimeMillis());
        
        sendResponse(exchange, 200, response);
    }

    /**
     * HTTP 요청 본문에서 텍스트 데이터를 읽어오는 헬퍼 메서드
     * <p>
     * InputStream으로부터 모든 바이트를 읽어와서 UTF-8 문자열로 변환한다.
     * try-with-resources 구문을 사용하여 스트림이 자동으로 해제되도록 보장한다.
     * </p>
     * 
     * @param exchange HTTP 요청/응답 처리를 위한 교환 객체
     * @return 요청 본문의 전체 내용을 담은 UTF-8 문자열
     * @throws IOException 요청 본문 읽기 중 I/O 오류가 발생한 경우
     */
    protected String readRequestBody(HttpExchange exchange) throws IOException {
        // try-with-resources로 InputStream 자동 해제
        try (InputStream is = exchange.getRequestBody()) {
            // 모든 바이트를 읽어와서 UTF-8 문자열로 변환
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

        
    /**
     * HTTP 메서드를 검증하는 헬퍼 메서드
     * 
     * @param exchange HTTP 교환 객체
     * @param expectedMethod 예상되는 HTTP 메서드 (GET, POST 등)
     * @return 메서드가 일치하면 true
     */
    protected boolean validateHttpMethod(HttpExchange exchange, String expectedMethod) throws IOException {
        String method = exchange.getRequestMethod();
        if (!method.equals(expectedMethod)) {
            logger.warn("유효하지 않은 HTTP 메서드: expected {}, got {}", expectedMethod, method);
            sendErrorResponse(exchange, 405, "허용되지 않은 방식");
            return false;
        }
        return true;
    }


    /**
     * URL 쿼리 문자열을 파싱하여 키-값 쌍의 Map으로 변환하는 헬퍼 메서드
     * <p>
     * HTTP GET 요청의 쿼리 파라미터들을 파싱한다. URL 인코딩된 특수문자들은
     * 자동으로 디코딩되어 원래의 문자로 복원된다. 잘못된 형식의 파라미터는 무시된다.
     * </p>
     * 
     * @param query URL 쿼리 문자열 (예: "key1=value1&key2=value2")
     * @return 파싱된 파라미터들을 담은 Map 객체 (키: 파라미터명, 값: 파라미터값)
     */
    protected Map<String, String> parseQueryParams(String query) {
        Map<String, String> params = new HashMap<>();
        // 쿼리 문자열이 존재하는 경우에만 파싱을 수행
        if (query != null) {
            // '&'을 기준으로 개별 파라미터들을 분리
            String[] pairs = query.split("&");

            // 각 파라미터를 "key=value" 형식으로 파싱
            for (String pair : pairs) {
                // 값에 '='가 포함될 수 있으니 최대 2개로 분리
                String[] keyValue = pair.split("=",2);
                
                // 올바른 "key=value" 형식인 경우에만 처리
                if (keyValue.length == 2) {
                    try {
                        // URL 디코딩을 통해 인코딩된 특수문자 복원
                        String key = URLDecoder.decode(keyValue[0], StandardCharsets.UTF_8);
                        String value = URLDecoder.decode(keyValue[1], StandardCharsets.UTF_8);

                        params.put(key, value);

                    } catch (Exception e) {
                        // URL  디코딩 실패 시 해당 파라미터 무시 및 로그에 기록
                        logger.warn("쿼리 파라미터 디코딩 실패 - 무시됨: pair={}, error={}", pair, e.getMessage());
                    }
                }
            }
        }
        return params;
    }

    /**
     * Authorization 헤더에서 토큰을 추출한다.
     * 
     * @param exchange 토큰을 추출할 HTTP문
     * @return authHeader에서 토큰을 반환한다.
     */
    protected String extractBearerToken(HttpExchange exchange) {
        String authHeader = exchange.getRequestHeaders().getFirst("Authorization");
        
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        
        return null;
    }
}
