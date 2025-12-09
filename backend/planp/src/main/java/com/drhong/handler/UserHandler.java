package com.drhong.handler;

import java.io.IOException;

import com.drhong.controller.UserController;
import com.drhong.dto.ApiResponse;
import com.drhong.dto.GoogleLoginRequest;
import com.drhong.dto.LoginRequest;
import com.drhong.dto.SignupRequest;
import com.sun.net.httpserver.HttpExchange;

public class UserHandler extends BaseHandler {
    private final UserController userController;

    public UserHandler(UserController userController) {
        this.userController = userController;
    }

    @Override
    protected void registerRoutes() {
        post("/signup", this::handleSignup);
        post("/login", this::handleLogin);
        post("/auth/google", this::handleGoogleLogin);
        
        // 사용자 정보 조회 라우트 (accessToken 기반)
        post("/get-info", this::handleGetUserInfoByToken);

        // 이메일 인증 관련 라우트
        post("/send-email-code", this::handleSendEmailCode);
        post("/verify-email-code", this::handleVerifyEmailCode);
    }
    /**
     * 이메일 인증 코드 전송 요청 처리
     * POST /api/users/send-email-code
     * body: { "email": "user@example.com" }
     */
    private void handleSendEmailCode(HttpExchange exchange) throws IOException {
        logger.debug("이메일 인증 코드 전송 요청");
        try {
            String requestBody = readRequestBody(exchange);
            com.google.gson.JsonObject json = gson.fromJson(requestBody, com.google.gson.JsonObject.class);
            if (json == null || !json.has("email")) {
                sendErrorResponse(exchange, 400, "이메일이 필요합니다");
                return;
            }
            String email = json.get("email").getAsString();
            ApiResponse<?> response = userController.sendEmailCode(email);
            if (response.isSuccess()) {
                sendSuccessResponse(exchange, response);
            } else {
                sendErrorResponse(exchange, 400, response.getMessage());
            }
        } catch (Exception e) {
            logger.warn("이메일 인증 코드 전송 중 오류", e);
            sendErrorResponse(exchange, 500, "이메일 인증 코드 전송 실패");
        }
    }

    /**
     * 이메일 인증 코드 검증 요청 처리
     * POST /api/users/verify-email-code
     * body: { "email": "user@example.com", "code": "123456" }
     */
    private void handleVerifyEmailCode(HttpExchange exchange) throws IOException {
        logger.debug("이메일 인증 코드 검증 요청");
        try {
            String requestBody = readRequestBody(exchange);
            com.google.gson.JsonObject json = gson.fromJson(requestBody, com.google.gson.JsonObject.class);
            if (json == null || !json.has("email") || !json.has("code")) {
                sendErrorResponse(exchange, 400, "이메일과 인증 코드가 필요합니다");
                return;
            }
            String email = json.get("email").getAsString();
            String code = json.get("code").getAsString();
            ApiResponse<?> response = userController.verifyEmailCode(email, code);
            if (response.isSuccess()) {
                sendSuccessResponse(exchange, response);
            } else {
                sendErrorResponse(exchange, 400, response.getMessage());
            }
        } catch (Exception e) {
            logger.warn("이메일 인증 코드 검증 중 오류", e);
            sendErrorResponse(exchange, 500, "이메일 인증 코드 검증 실패");
        }
    }

    @Override
    protected String extractEndpoint(String fullPath) {
        String basePath = "/api/users";
        if (fullPath.startsWith(basePath)) {
            return fullPath.substring(basePath.length());
        }
        return fullPath;
    }

    private void handleSignup(HttpExchange exchange) throws IOException {
        logger.debug("회원가입 시도");
        try {
            String requestBody = readRequestBody(exchange);
            SignupRequest request = gson.fromJson(requestBody, SignupRequest.class);

            if (request == null) {
                sendErrorResponse(exchange, 400, "잘못된 요청 형식");
                return;
            }

            ApiResponse<?> response = userController.signup(request);

            if (response.isSuccess()) {
                sendSuccessResponse(exchange, response);
            } else {
                sendErrorResponse(exchange, 400, response.getMessage());
            }
        } catch (Exception e) {
            logger.warn("회원가입 중 오류 발생", e);
            sendErrorResponse(exchange, 500, "서버 문제 발생");
        }
    }

    private void handleLogin(HttpExchange exchange) throws IOException {
        try {
            String requestBody = readRequestBody(exchange);
            LoginRequest request = gson.fromJson(requestBody, LoginRequest.class);

            if (request == null) {
                sendErrorResponse(exchange, 400, "잘못된 요청 형식");
                return;
            }
            ApiResponse<?> response = userController.login(request);

            int statusCode = response.isSuccess() ? 200 : 401;
            sendResponse(exchange,statusCode, response);
        } catch (Exception e) {
            logger.error("로그인 처리 중 오류", e);
            sendErrorResponse(exchange, 500, "서버 오류가 발생했습니다.");        
        }
    }
    
    private void handleGoogleLogin(HttpExchange exchange) throws IOException {
        logger.debug("Google OAuth 로그인 시도");
        try {
            String requestBody = readRequestBody(exchange);
            GoogleLoginRequest request = gson.fromJson(requestBody, GoogleLoginRequest.class);

            ApiResponse<?> response = userController.googleLogin(request);

            logger.debug("구글 로그인 응답:"+response.toJson());
            if (response.isSuccess()) {
                sendSuccessResponse(exchange, response);
            } else {
                sendErrorResponse(exchange, 400, response.getMessage());
            }
        } catch (Exception e) {
            logger.warn("Google OAuth 로그인 중 오류 발생", e);
            sendErrorResponse(exchange, 500, "Google 로그인 처리 중 오류가 발생했습니다");
        }
    }
    
    /**
     * 사용자 정보 조회 요청을 처리하는 메서드 (accessToken 기반)
     * POST /api/users/get-info
     * Authorization 헤더의 accessToken(JWT)로 사용자 정보 반환
     */
    private void handleGetUserInfoByToken(HttpExchange exchange) throws IOException {
        logger.debug("사용자 정보 조회 요청 (accessToken 기반)");
        try {
            String authHeader = exchange.getRequestHeaders().getFirst("Authorization");
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                sendErrorResponse(exchange, 401, "Authorization 헤더가 필요합니다");
                return;
            }
            String accessToken = authHeader.substring("Bearer ".length());
            ApiResponse<?> response = userController.getUserInfoByToken(accessToken);
            if (response.isSuccess()) {
                sendSuccessResponse(exchange, response);
            } else {
                sendErrorResponse(exchange, 401, response.getMessage());
            }
        } catch (Exception e) {
            logger.error("사용자 정보 조회 중 오류", e);
            sendErrorResponse(exchange, 500, "서버 오류가 발생했습니다");
        }
    }
    
    /**
     * URL 쿼리 파라미터를 추출하는 도우미 메서드
     * 
     * @param exchange HTTP 요청 객체
     * @param paramName 추출할 파라미터 이름
     * @return 파라미터 값, 없으면 null
     */
    private String getQueryParameter(HttpExchange exchange, String paramName) {
        String query = exchange.getRequestURI().getQuery();
        if (query == null) {
            return null;
        }
        
        for (String param : query.split("&")) {
            String[] keyValue = param.split("=", 2);
            if (keyValue.length == 2 && paramName.equals(keyValue[0])) {
                try {
                    return java.net.URLDecoder.decode(keyValue[1], "UTF-8");
                } catch (java.io.UnsupportedEncodingException e) {
                    logger.warn("쿼리 파라미터 디코딩 실패: {}", e.getMessage());
                    return keyValue[1];
                }
            }
        }
        
        return null;
    }

}
