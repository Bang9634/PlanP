package com.drhong.controller;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.drhong.dto.SignupRequest;
import com.drhong.dto.SignupResponse;
import com.drhong.service.UserService;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.sun.net.httpserver.HttpExchange;

/**
 * 사용자 관련 HTTP 요청 처리 컨트롤러
 * <p>
 * 사용자 인증 및 관리와 관련된 모든 HTTP 엔드포인트를 담당한다.
 * 회원가입, 로그인, 중복 확인 등의 기능을 제공한다.
 * </p>
 * <p>
 * <h3>지원하는 API 엔드포인트:</h3>
 * <ul>
 *   <li><strong>POST</strong> /api/users/signup - 새 사용자 회원가입</li>
 *   <li><strong>POST</strong> /api/users/login - 사용자 로그인 인증</li>
 *   <li><strong>GET</strong> /api/users/check-id - 사용자 ID 중복 확인</li>
 *   <li><strong>GET</strong> /api/users/check-email - 이메일 중복 확인</li>
 * </ul>
 * </p>
 * <p>
 * CORS 처리는 CorsFilter에서 담당하므로 해당 클래스는 비즈니스 로직만 다룬다.
 * </p>
 * 
 * @author bang9634
 * @since 2025-11-10
 */
public class UserController {

    /** SLF4J Logger 인스턴스 - 요청 처리 로그를 기록 */
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);
    
    /** 사용자 비즈니스 로직 처리 서비스 */
    private final UserService userService;

    /** JSON을 다루기 위한 Gson 인스턴스 */
    private final Gson gson;
    
    /**
     * UserController 객체를 생성하는 생성자
     * 
     * @param userService 사용자 관련 비즈니스 로직을 처리하는 서비스 객체
     */
    public UserController(UserService userService) {
        this.userService = userService;
        this.gson = new Gson();
    }
    
    /**
     * 회원가입 요청을 처리하는 메서드
     * <p>
     * POST /api/users/signup 요청을 처리한다.
     * 클라이언트로부터 JSON 형태의 회원가입 정보를 전달 받아서
     * 유효성을 검증하고 새로운 사용자 계정을 생성한다.
     * 비밀번호는 해시화되어 저장된다.
     * </p>
     * 
     * <h4>HTTP 요청 형식:</h4>
     * <pre>{@code
     * POST /api/users/signup
     * Content-Type: application/json
     * 
     * {
     *   "userId": "myuser123",
     *   "password": "securePassword!",
     *   "name": "홍길동",
     *   "email": "hong@example.com"
     * }
     * }</pre>
     * 
     * <h4>응답 예시:</h4>
     * <pre>{@code
     * // 성공 시 (HTTP 200)
     * {
     *   "success": true,
     *   "message": "회원가입이 완료되었습니다.",
     *   "userId": "myuser123"
     * }
     * 
     * // 실패 시 (HTTP 400)
     * {
     *   "success": false,
     *   "message": "이미 사용중인 사용자 ID입니다."
     * }
     * }</pre>
     * 
     * @param exchange HTTP 요청/응답 처리를 위한 교환 객체
     * @throws IOException 네트워크 I/O 처리 중 오류가 발생한 경우
     */
    public void handleSignup(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        String clientIP = exchange.getRemoteAddress().getAddress().getHostAddress();
        
        logger.info("회원가입 요청: method={}, clientIP={}", method, clientIP);
        
        // HTTP 메서드 검증 - POST 방식만 허용
        if (!"POST".equals(method)) {
            logger.warn("잘못된 HTTP 메서드: method={}, clientIP={}", method, clientIP);
            sendErrorResponse(exchange, 405, "Method Not Allowed");
            return;
        }
        
        try {
            // Request문에서 JSON 데이터의 Body 부분만 읽음
            String requestBody = readRequestBody(exchange);
            logger.debug("요청 본문 수신: length={}", requestBody.length());
            
            // JSON 데이터를 SignupRequest 객체로 변환
            SignupRequest request = gson.fromJson(requestBody, SignupRequest.class);
            
            // Request 데이터의 유효성 검사, 잘못된 데이터일 경우 에러 Reponse 전송
            if (request == null) {
                logger.warn("잘못된 JSON: clientIP={}", clientIP);
                sendErrorResponse(exchange, 400, "Invalid JSON format");
                return;
            }
            
            logger.info("회원가입 처리: userId={}", request.getUserId());
            
            // 비즈니스 로직 처리
            SignupResponse response = userService.signup(request);

            // JSON 형태의 Response 생성
            String jsonResponse = gson.toJson(response);

            // Response의 성공 여부에 따라 HTTP status 코드 생성 (성공: 200, 실패: 400)
            int statusCode = response.isSuccess() ? 200 : 400;
            
            logger.info("회원가입 결과: userId={}, success={}, status={}", 
                request.getUserId(), response.isSuccess(), statusCode);
            
            // 클라이언트에 JSON형태의 Response 전송
            sendJsonResponse(exchange, statusCode, jsonResponse);
            
        } catch (JsonSyntaxException e) {
            logger.error("JSON 파싱 오류: clientIP={}", clientIP, e);
            sendErrorResponse(exchange, 400, "Invalid JSON format: " + e.getMessage());
        } catch (Exception e) {
            // 예상치 못한 서버 오류 처리
            logger.error("회원가입 처리 오류: clientIP={}", clientIP, e);
            sendErrorResponse(exchange, 500, "Internal server error");
        }
    }
    
    /**
     * 로그인 인증 요청을 처리하는 메서드
     * <p>
     * 사용자가 제공한 ID와 비밀번호를 검증하여 인증을 수행한다.
     * 성공 시 사용자 정보를 반환하고, 실패 시 적절한 오류 메시지를 제공한다.
     * </p>
     * 
     * @param exchange HTTP 요청/응답 처리를 위한 교환 객체
     * @throws IOException 네트워크 I/O 처리 중 오류가 발생한 경우
     */
    public void handleLogin(HttpExchange exchange) throws IOException {
        // HTTP POST 메서드 방식만 허용
        if (!"POST".equals(exchange.getRequestMethod())) {
            sendErrorResponse(exchange, 405, "Method Not Allowed");
            return;
        }
        
        try {
            // request의 body에서 로그인 정보 추출
            String requestBody = readRequestBody(exchange);
            // TODO: 임시로 Map 사용, 추후 LoginRequest DTO로 변경 예정
            @SuppressWarnings("unchecked")
            Map<String, String> loginData = gson.fromJson(requestBody, Map.class); 
            
            String userId = loginData.get("userId");
            String password = loginData.get("password");

            logger.info("로그인 인증 시도: userId={}", userId);
            
            // UserService를 통한 로그인 검증
            boolean loginSuccess = userService.login(userId, password);
            
            // response 객체 생성
            Map<String, Object> response = new HashMap<>();
            response.put("success", loginSuccess);
            response.put("message", loginSuccess ? "로그인 성공" : "아이디 또는 비밀번호가 올바르지 않습니다.");
            
            // 로그인 성공 시 reponse에 사용자 아이디를 포함
            if (loginSuccess) {
                response.put("userId", userId);
            }

            logger.info("로그인 처리 완료: userId={}, success={}", userId, loginSuccess);

            // response를 JSON 형태로 파싱하여 클라이언트에게 전송
            String jsonResponse = gson.toJson(response);
            sendJsonResponse(exchange, loginSuccess ? 200 : 401, jsonResponse);
            
        } catch (Exception e) {
            System.err.println("로그인 처리 중 오류: " + e.getMessage());
            sendErrorResponse(exchange, 500, "서버 내부 오류가 발생했습니다.");
        }
    }
    
    /**
     * 사용자 ID 중복 여부를 확인하는 메서드
     * <p>
     * 클라이언트가 입력한 사용자 ID가 이미 시스템에 등록되어 있는지 확인한다.
     * 회원가입 시 실시간으로 ID 중복을 검사하기 위해 사용된다.
     * </p>
     * 
     * <h4>HTTP 요청 형식:</h4>
     * <pre>{@code
     * GET /api/users/check-id?userId=testuser123
     * }</pre>
     * 
     * <h4>응답 예시:</h4>
     * <pre>{@code
     * {
     *   "available": true,
     *   "message": "사용 가능한 ID입니다."
     * }
     * }</pre>
     * 
     * @param exchange HTTP 요청/응답 처리를 위한 교환 객체
     * @throws IOException 네트워크 I/O 처리 중 오류가 발생한 경우
     */
    public void handleCheckUserId(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        String clientIP = exchange.getRemoteAddress().getAddress().getHostAddress();
        
        logger.info("ID 중복 확인 요청 수신: method={}, clientIP={}", method, clientIP);

        // HTTP GET 메서드 방식만 허용
        if (!"GET".equals(exchange.getRequestMethod())) {
            sendErrorResponse(exchange, 405, "Method Not Allowed");
            return;
        }
        
        try {
            // URL 쿼리 파라미터 파싱
            Map<String, String> params = parseQueryParams(exchange.getRequestURI().getQuery());
            String userId = params.get("userId");
            
            // 필수 파라미터 검증
            if (userId == null || userId.trim().isEmpty()) {
                sendErrorResponse(exchange, 400, "userId parameter is required");
                return;
            }

            logger.info("ID 중복 확인 처리: userId={}", userId);

            // UserService를 통해 ID 중복 확인
            boolean available = userService.isUserIdAvailable(userId);
            
            // reponse 객체 생성
            Map<String, Object> response = new HashMap<>();
            response.put("available", available);
            response.put("message", available ? "사용 가능한 ID입니다." : "이미 사용중인 ID입니다.");
            
            logger.info("ID 중복 확인 완료: userId={}, available={}", userId, available);

            // reponse를 JSON 형태로 클라이언트에게 전송
            String jsonResponse = gson.toJson(response);
            sendJsonResponse(exchange, 200, jsonResponse);
            
        } catch (Exception e) {
            logger.error("ID 중복 확인 중 오류: clientIP={}", clientIP, e);
            sendErrorResponse(exchange, 500, "Internal server error");
        }
    }
    
    /**
     * 이메일 주소 중복 여부를 확인하는 메서드
     * <p>
     * 클라이언트가 입력한 이메일 주소가 이미 다른 사용자에 의해 
     * 등록되어 있는지 확인한다. 회원가입 시 이메일 중복을 방지하기 위해 사용된다.
     * </p>
     * 
     * * <h4>HTTP 요청 형식:</h4>
     * <pre>{@code
     * GET /api/users/check-email?email=hong@gmail.com
     * }</pre>
     * 
     * 
     * @param exchange HTTP 요청/응답 처리를 위한 교환 객체
     * @throws IOException 네트워크 I/O 처리 중 오류가 발생한 경우
     */
    public void handleCheckEmail(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        String clientIP = exchange.getRemoteAddress().getAddress().getHostAddress();
        
        logger.info("이메일 중복 확인 요청 수신: method={}, clientIP={}", method, clientIP);
        
        // HTTP GET 메서드 방식만 허용
        if (!"GET".equals(exchange.getRequestMethod())) {
            sendErrorResponse(exchange, 405, "Method Not Allowed");
            return;
        }
        
        try {
            // URL 쿼리 파라미터 파싱
            Map<String, String> params = parseQueryParams(exchange.getRequestURI().getQuery());
            String email = params.get("email");
            
            // 필수 파라미터 검증
            if (email == null || email.trim().isEmpty()) {
                logger.warn("email 파라미터 누락: clientIP={}", clientIP);
                sendErrorResponse(exchange, 400, "email parameter is required");
                return;
            }

            logger.info("이메일 중복 확인 처리: email={}", email);
            
            // UserService를 통해 이메일 중복 확인
            boolean available = userService.isEmailAvailable(email);
            
            // response 객체 생성
            Map<String, Object> response = new HashMap<>();
            response.put("available", available);
            response.put("message", available ? "사용 가능한 이메일입니다." : "이미 사용중인 이메일입니다.");
            
            logger.info("이메일 중복 확인 완료: email={}, available={}", email, available);

            // reponse를 JSON 형태로 클라이언트에게 전송
            String jsonResponse = gson.toJson(response);
            sendJsonResponse(exchange, 200, jsonResponse);
            
        } catch (Exception e) {
            logger.error("이메일 중복 확인 중 오류: clientIP={}", clientIP, e);
            sendErrorResponse(exchange, 500, "Internal server error");
        }
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
    private void sendJsonResponse(HttpExchange exchange, int statusCode, String jsonResponse) throws IOException {
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
     * <h4>오류 응답 형식:</h4>
     * <pre>{@code
     * {
     *   "success": false,
     *   "error": "오류 메시지",
     *   "timestamp": 1699123456789
     * }
     * }</pre>
     * 
     * @param exchange HTTP 요청/응답 처리를 위한 교환 객체
     * @param statusCode HTTP 오류 상태 코드 (400, 404, 405, 500 등)
     * @param message 클라이언트에게 전달할 오류 메시지
     * @throws IOException 응답 전송 중 네트워크 오류가 발생한 경우
     */
    private void sendErrorResponse(HttpExchange exchange, int statusCode, String message) throws IOException {
        // 표준 오류 reponse 구조 생성
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("success", false);                 // 요청 실패 표시
        errorResponse.put("error", message);                        // 사용자에게 전달할 오류 메시지
        errorResponse.put("timestamp", System.currentTimeMillis()); // 오류 발생 시각 (ms)
        
        // 오류 응답을 JSON으로 변환
        String jsonResponse = gson.toJson(errorResponse);

        logger.warn("오류 응답 전송: statusCode={}, message={}", statusCode, message);

        // JSON 응답 전달
        sendJsonResponse(exchange, statusCode, jsonResponse);
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
    private String readRequestBody(HttpExchange exchange) throws IOException {
        // try-with-resources로 InputStream 자동 해제
        try (InputStream is = exchange.getRequestBody()) {
            // 모든 바이트를 읽어와서 UTF-8 문자열로 변환
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    /**
     * URL 쿼리 문자열을 파싱하여 키-값 쌍의 Map으로 변환하는 헬퍼 메서드
     * <p>
     * HTTP GET 요청의 쿼리 파라미터들을 파싱한다. URL 인코딩된 특수문자들은
     * 자동으로 디코딩되어 원래의 문자로 복원된다. 잘못된 형식의 파라미터는 무시된다.
     * </p>
     * 
     * <h4>지원하는 쿼리 형식:</h4>
     * <pre>{@code
     * userId=testuser&email=test%40example.com
     * → Map: {"userId": "testuser", "email": "test@example.com"}
     * }</pre>
     * 
     * @param query URL 쿼리 문자열 (예: "key1=value1&key2=value2")
     * @return 파싱된 파라미터들을 담은 Map 객체 (키: 파라미터명, 값: 파라미터값)
     */
    private Map<String, String> parseQueryParams(String query) {
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
}