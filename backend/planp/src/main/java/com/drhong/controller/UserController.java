package com.drhong.controller;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
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
 */
public class UserController {
    
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);
    private final UserService userService;
    private final Gson gson;
    
    public UserController(UserService userService) {
        this.userService = userService;
        this.gson = new Gson();
    }
    
    /**
     * 회원가입 처리 (CORS는 필터에서 처리됨)
     */
    public void handleSignup(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        String clientIP = exchange.getRemoteAddress().getAddress().getHostAddress();
        
        logger.info("회원가입 요청: method={}, clientIP={}", method, clientIP);
        
        // POST 요청만 허용
        if (!"POST".equals(method)) {
            logger.warn("잘못된 HTTP 메서드: method={}, clientIP={}", method, clientIP);
            sendErrorResponse(exchange, 405, "Method Not Allowed");
            return;
        }
        
        try {
            String requestBody = readRequestBody(exchange);
            logger.debug("요청 본문 수신: length={}", requestBody.length());
            
            SignupRequest request = gson.fromJson(requestBody, SignupRequest.class);
            
            if (request == null) {
                logger.warn("잘못된 JSON: clientIP={}", clientIP);
                sendErrorResponse(exchange, 400, "Invalid JSON format");
                return;
            }
            
            logger.info("회원가입 처리: userId={}", request.getUserId());
            
            SignupResponse response = userService.signup(request);
            String jsonResponse = gson.toJson(response);
            int statusCode = response.isSuccess() ? 200 : 400;
            
            logger.info("회원가입 결과: userId={}, success={}, status={}", 
                request.getUserId(), response.isSuccess(), statusCode);
            
            sendJsonResponse(exchange, statusCode, jsonResponse);
            
        } catch (JsonSyntaxException e) {
            logger.error("JSON 파싱 오류: clientIP={}", clientIP, e);
            sendErrorResponse(exchange, 400, "Invalid JSON format: " + e.getMessage());
        } catch (Exception e) {
            logger.error("회원가입 처리 오류: clientIP={}", clientIP, e);
            sendErrorResponse(exchange, 500, "Internal server error");
        }
    }
    
    /**
     * 로그인 처리
     */
    public void handleLogin(HttpExchange exchange) throws IOException {
        if (!"POST".equals(exchange.getRequestMethod())) {
            sendErrorResponse(exchange, 405, "Method Not Allowed");
            return;
        }
        sendErrorResponse(exchange, 501, "Login not implemented yet");
    }
    
    /**
     * 사용자 ID 중복 확인
     */
    public void handleCheckUserId(HttpExchange exchange) throws IOException {
        if (!"GET".equals(exchange.getRequestMethod())) {
            sendErrorResponse(exchange, 405, "Method Not Allowed");
            return;
        }
        sendErrorResponse(exchange, 501, "Check user ID not implemented yet");
    }
    
    /**
     * 이메일 중복 확인
     */
    public void handleCheckEmail(HttpExchange exchange) throws IOException {
        if (!"GET".equals(exchange.getRequestMethod())) {
            sendErrorResponse(exchange, 405, "Method Not Allowed");
            return;
        }
        sendErrorResponse(exchange, 501, "Check email not implemented yet");
    }
    
    /**
     * JSON 응답 전송
     */
    private void sendJsonResponse(HttpExchange exchange, int statusCode, String jsonResponse) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        
        byte[] responseBytes = jsonResponse.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(statusCode, responseBytes.length);
        
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(responseBytes);
        }
        
        logger.debug("응답 전송: status={}, length={}", statusCode, responseBytes.length);
    }
    
    /**
     * 오류 응답 전송
     */
    private void sendErrorResponse(HttpExchange exchange, int statusCode, String message) throws IOException {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("success", false);
        errorResponse.put("error", message);
        errorResponse.put("timestamp", System.currentTimeMillis());
        
        String jsonResponse = gson.toJson(errorResponse);
        sendJsonResponse(exchange, statusCode, jsonResponse);
    }
    
    /**
     * 요청 본문 읽기
     */
    private String readRequestBody(HttpExchange exchange) throws IOException {
        try (InputStream is = exchange.getRequestBody()) {
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}