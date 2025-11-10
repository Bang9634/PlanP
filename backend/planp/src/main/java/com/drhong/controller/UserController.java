package com.drhong.controller;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import com.drhong.dto.SignupRequest;
import com.drhong.dto.SignupResponse;
import com.drhong.service.UserService;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.sun.net.httpserver.HttpExchange;

/**
 * 사용자 관련 HTTP 요청 처리 컨트롤러
 */
public class UserController {
    
    private final UserService userService;
    private final Gson gson;
    
    public UserController(UserService userService) {
        this.userService = userService;
        this.gson = new Gson();
    }
    
    /**
     * 회원가입 처리
     * POST /api/users/signup
     */
    public void handleSignup(HttpExchange exchange) throws IOException {
        if (!"POST".equals(exchange.getRequestMethod())) {
            sendErrorResponse(exchange, 405, "Method Not Allowed");
            return;
        }
        
        try {
            // 요청 본문 읽기
            String requestBody = readRequestBody(exchange);
            SignupRequest request = gson.fromJson(requestBody, SignupRequest.class);
            
            if (request == null) {
                sendErrorResponse(exchange, 400, "Invalid request body");
                return;
            }
            
            // 회원가입 처리
            SignupResponse response = userService.signup(request);
            
            // 응답 전송
            String jsonResponse = gson.toJson(response);
            sendJsonResponse(exchange, response.isSuccess() ? 200 : 400, jsonResponse);
            
        } catch (JsonSyntaxException e) {
            sendErrorResponse(exchange, 400, "Invalid JSON format");
        } catch (Exception e) {
            System.err.println("회원가입 처리 중 오류: " + e.getMessage());
            e.printStackTrace();
            sendErrorResponse(exchange, 500, "Internal server error");
        }
    }
    
    /**
     * 로그인 처리
     * POST /api/users/login
     */
    public void handleLogin(HttpExchange exchange) throws IOException {
        if (!"POST".equals(exchange.getRequestMethod())) {
            sendErrorResponse(exchange, 405, "Method Not Allowed");
            return;
        }
        
        try {
            String requestBody = readRequestBody(exchange);
            // TODO: 추후 SigninRequest 클래스 작성시 Map.class를 SigninRequest.class로 수정
            Map<String, String> loginData = gson.fromJson(requestBody, Map.class); 
            
            String userId = loginData.get("userId");
            String password = loginData.get("password");
            
            boolean loginSuccess = userService.login(userId, password);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", loginSuccess);
            response.put("message", loginSuccess ? "로그인 성공" : "아이디 또는 비밀번호가 올바르지 않습니다.");
            
            if (loginSuccess) {
                response.put("userId", userId);
            }
            
            String jsonResponse = gson.toJson(response);
            sendJsonResponse(exchange, loginSuccess ? 200 : 401, jsonResponse);
            
        } catch (Exception e) {
            System.err.println("로그인 처리 중 오류: " + e.getMessage());
            sendErrorResponse(exchange, 500, "Internal server error");
        }
    }
    
    /**
     * 사용자 ID 중복 확인
     * GET /api/users/check-id?userId=xxx
     */
    public void handleCheckUserId(HttpExchange exchange) throws IOException {
        if (!"GET".equals(exchange.getRequestMethod())) {
            sendErrorResponse(exchange, 405, "Method Not Allowed");
            return;
        }
        
        try {
            Map<String, String> params = parseQueryParams(exchange.getRequestURI().getQuery());
            String userId = params.get("userId");
            
            if (userId == null || userId.trim().isEmpty()) {
                sendErrorResponse(exchange, 400, "userId parameter is required");
                return;
            }
            
            boolean available = userService.isUserIdAvailable(userId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("available", available);
            response.put("message", available ? "사용 가능한 ID입니다." : "이미 사용중인 ID입니다.");
            
            String jsonResponse = gson.toJson(response);
            sendJsonResponse(exchange, 200, jsonResponse);
            
        } catch (Exception e) {
            System.err.println("ID 중복 확인 중 오류: " + e.getMessage());
            sendErrorResponse(exchange, 500, "Internal server error");
        }
    }
    
    /**
     * 이메일 중복 확인
     * GET /api/users/check-email?email=xxx
     */
    public void handleCheckEmail(HttpExchange exchange) throws IOException {
        if (!"GET".equals(exchange.getRequestMethod())) {
            sendErrorResponse(exchange, 405, "Method Not Allowed");
            return;
        }
        
        try {
            Map<String, String> params = parseQueryParams(exchange.getRequestURI().getQuery());
            String email = params.get("email");
            
            if (email == null || email.trim().isEmpty()) {
                sendErrorResponse(exchange, 400, "email parameter is required");
                return;
            }
            
            boolean available = userService.isEmailAvailable(email);
            
            Map<String, Object> response = new HashMap<>();
            response.put("available", available);
            response.put("message", available ? "사용 가능한 이메일입니다." : "이미 사용중인 이메일입니다.");
            
            String jsonResponse = gson.toJson(response);
            sendJsonResponse(exchange, 200, jsonResponse);
            
        } catch (Exception e) {
            System.err.println("이메일 중복 확인 중 오류: " + e.getMessage());
            sendErrorResponse(exchange, 500, "Internal server error");
        }
    }
    
    // 유틸리티 메서드들
    
    private String readRequestBody(HttpExchange exchange) throws IOException {
        try (InputStream is = exchange.getRequestBody()) {
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
    
    private Map<String, String> parseQueryParams(String query) {
        Map<String, String> params = new HashMap<>();
        if (query != null) {
            String[] pairs = query.split("&");
            for (String pair : pairs) {
                String[] keyValue = pair.split("=");
                if (keyValue.length == 2) {
                    try {
                        String key = URLDecoder.decode(keyValue[0], StandardCharsets.UTF_8);
                        String value = URLDecoder.decode(keyValue[1], StandardCharsets.UTF_8);
                        params.put(key, value);
                    } catch (Exception e) {
                        // 파싱 오류 무시
                    }
                }
            }
        }
        return params;
    }
    
    private void sendJsonResponse(HttpExchange exchange, int statusCode, String jsonResponse) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type");
        
        byte[] responseBytes = jsonResponse.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(statusCode, responseBytes.length);
        
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(responseBytes);
        }
    }
    
    private void sendErrorResponse(HttpExchange exchange, int statusCode, String message) throws IOException {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("success", false);
        errorResponse.put("error", message);
        
        String jsonResponse = gson.toJson(errorResponse);
        sendJsonResponse(exchange, statusCode, jsonResponse);
    }
}