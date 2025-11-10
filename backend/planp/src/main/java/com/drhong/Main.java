package com.drhong;

import com.drhong.server.PlanPServer;
import com.drhong.service.UserService;

/**
 * PlanP 백엔드 애플리케이션 메인 클래스
 */
public class Main {
    
    // 서버 설정
    private static final int DEFAULT_PORT = 8080;
    private static final String DEFAULT_HOST = "localhost";
    
    public static void main(String[] args) {
        System.out.println("=== PlanP 백엔드 서버 시작 ===");
        
        try {
            // 포트 설정 (프로그램 인수 또는 환경 변수로 변경 가능)
            int port = getPort(args);
            String host = getHost(args);
            
            // 서비스 초기화
            UserService userService = new UserService();
            
            // HTTP 서버 생성 및 시작
            PlanPServer server = new PlanPServer(host, port, userService);
            
            System.out.printf("서버가 http://%s:%d 에서 시작되었습니다.%n", host, port);
            System.out.println("종료하려면 Ctrl+C를 누르세요.");
            
            // 서버 시작 (블로킹)
            server.start();
            
        } catch (Exception e) {
            System.err.println("서버 시작 중 오류 발생: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
    
    /**
     * 포트 번호 결정
     * 1. 프로그램 인수 확인
     * 2. 환경 변수 확인
     * 3. 기본값 사용
     */
    private static int getPort(String[] args) {
        // 1. 프로그램 인수에서 포트 확인
        if (args.length > 0) {
            try {
                return Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                System.err.println("잘못된 포트 번호: " + args[0] + ", 기본값 사용");
            }
        }
        
        // 2. 환경 변수에서 포트 확인
        String envPort = System.getenv("PLANP_PORT");
        if (envPort != null) {
            try {
                return Integer.parseInt(envPort);
            } catch (NumberFormatException e) {
                System.err.println("잘못된 환경변수 포트: " + envPort + ", 기본값 사용");
            }
        }
        
        // 3. 기본값 반환
        return DEFAULT_PORT;
    }
    
    /**
     * 호스트 주소 결정
     */
    private static String getHost(String[] args) {
        // 환경 변수에서 호스트 확인
        String envHost = System.getenv("PLANP_HOST");
        if (envHost != null && !envHost.trim().isEmpty()) {
            return envHost.trim();
        }
        
        return DEFAULT_HOST;
    }
}