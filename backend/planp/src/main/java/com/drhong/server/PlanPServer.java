package com.drhong.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.drhong.controller.UserController;
import com.drhong.service.UserService;
import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpServer;

/**
 * PlanP HTTP 서버
 */
public class PlanPServer {
    
    private static final Logger logger = LoggerFactory.getLogger(PlanPServer.class);
    private final HttpServer server;
    private final String host;
    private final int port;
    
    public PlanPServer(String host, int port, UserService userService) throws IOException {
        this.host = host;
        this.port = port;
        
        // HTTP 서버 생성
        this.server = HttpServer.create(new InetSocketAddress(host, port), 0);
        
        // 컨트롤러 초기화
        UserController userController = new UserController(userService);
        
        // 라우트 설정
        setupRoutes(userController);
        
        // 스레드 풀 설정
        server.setExecutor(Executors.newFixedThreadPool(10));
        
        logger.info("서버 초기화 완료: {}:{}", host, port);
    }
    
    /**
     * API 라우트 설정
     */
    private void setupRoutes(UserController userController) {
        logger.info("라우트 설정 시작...");
        
        // CORS 필터 생성
        CorsFilter corsFilter = new CorsFilter();
        
        // 헬스 체크 API
        HttpContext healthContext = server.createContext("/health", new HealthCheckHandler());
        healthContext.getFilters().add(corsFilter);
        
        // 사용자 관련 API (CORS 필터 적용)
        HttpContext signupContext = server.createContext("/api/users/signup", userController::handleSignup);
        signupContext.getFilters().add(corsFilter);
        
        HttpContext loginContext = server.createContext("/api/users/login", userController::handleLogin);
        loginContext.getFilters().add(corsFilter);
        
        HttpContext checkIdContext = server.createContext("/api/users/check-id", userController::handleCheckUserId);
        checkIdContext.getFilters().add(corsFilter);
        
        HttpContext checkEmailContext = server.createContext("/api/users/check-email", userController::handleCheckEmail);
        checkEmailContext.getFilters().add(corsFilter);
      
        logger.info("라우트 설정 완료:");
        logger.info("  GET  /health - 서버 상태 확인 (CORS 필터 적용)");
        logger.info("  POST /api/users/signup - 회원가입 (CORS 필터 적용)");
        logger.info("  POST /api/users/login - 로그인 (CORS 필터 적용)");
        logger.info("  GET  /api/users/check-id - ID 중복 확인 (CORS 필터 적용)");
        logger.info("  GET  /api/users/check-email - 이메일 중복 확인 (CORS 필터 적용)");
        logger.info("  *    / - 기본 CORS 처리");
    }
    
    /**
     * 서버 시작
     */
    public void start() {
        server.start();
        logger.info("HTTP 서버가 시작되었습니다: http://{}:{}", host, port);
        logger.info("CORS 설정: localhost:3000, localhost:8080 허용");
    }
    
    /**
     * 서버 중지
     */
    public void stop() {
        logger.info("서버를 종료합니다...");
        server.stop(3);
        logger.info("서버가 종료되었습니다.");
    }
}