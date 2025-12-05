package com.drhong.handler;

import java.io.IOException;

import com.drhong.controller.UserController;
import com.drhong.dto.ApiResponse;
import com.drhong.dto.LoginRequest;
import com.drhong.dto.LogoutRequest;
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

    private void handleLogout(HttpExchange exchange) throws IOException {
        try {
            String requestBody = readRequestBody(exchange);
            LogoutRequest request = gson.fromJson(requestBody, LogoutRequest.class);
            if (request == null) {
                sendErrorResponse(exchange, 400, "잘못된 요청 형식");
                return;
            }
            ApiResponse<?> response = userController.logout(request);
            int statusCode = response.isSuccess() ? 200 : 400;
            sendResponse(exchange, statusCode, response);
        } catch (Exception e) {
            logger.error("로그아웃 처리 중 오류", e);
            sendErrorResponse(exchange, 500, "서버 오류가 발생했습니다.");
        }
    }
}
