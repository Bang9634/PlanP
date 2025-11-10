package com.drhong.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

import com.drhong.controller.UserController;
import com.drhong.service.UserService;
import com.sun.net.httpserver.HttpServer;

/**
 * PlanP HTTP 서버
 */
public class PlanPServer {
    
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
        
        // 스레드 풀 설정 (동시 요청 처리)
        server.setExecutor(Executors.newFixedThreadPool(10));
    }
    
    /**
     * API 라우트 설정
     */
    private void setupRoutes(UserController userController) {
        
        // 헬스 체크 API
        server.createContext("/health", new HealthCheckHandler());
        // 사용자 관련 API
        server.createContext("/api/users/signup", userController::handleSignup);
        server.createContext("/api/users/login", userController::handleLogin);
        server.createContext("/api/users/check-id", userController::handleCheckUserId);
        server.createContext("/api/users/check-email", userController::handleCheckEmail);
        
        // CORS 설정을 위한 기본 핸들러
        server.createContext("/", new CorsHandler());
        
        System.out.println("라우트 설정 완료:");
        System.out.println("  POST /api/users/signup - 회원가입");
        System.out.println("  POST /api/users/login - 로그인");
        System.out.println("  GET  /api/users/check-id?userId=xxx - ID 중복 확인");
        System.out.println("  GET  /api/users/check-email?email=xxx - 이메일 중복 확인");
        System.out.println("  GET  /health - 서버 상태 확인");
    }
    
    /**
     * 서버 시작
     */
    public void start() {
        server.start();
        System.out.printf("HTTP 서버가 %s:%d에서 실행 중입니다.%n", host, port);
        
        // Graceful shutdown을 위한 shutdown hook 추가
        Runtime.getRuntime().addShutdownHook(new Thread(this::stop));
    }
    
    /**
     * 서버 중지
     */
    public void stop() {
        System.out.println("\n서버를 종료합니다...");
        server.stop(3); // 3초 후 강제 종료
        System.out.println("서버가 종료되었습니다.");
    }
}