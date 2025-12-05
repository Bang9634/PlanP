package com.drhong;

import com.google.gson.Gson;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

/**
 * Google OAuth API 실제 테스트
 * 
 * 백엔드 서버가 실행 중일 때 Google OAuth 엔드포인트를 직접 테스트한다.
 */
public class GoogleOAuthApiTest {
    
    private static final String API_URL = "http://localhost:8080/api/users/auth/google";
    
    public static void main(String[] args) {
        System.out.println("🚀 Google OAuth API 테스트 시작");
        System.out.println("백엔드 서버가 http://localhost:8080에서 실행 중인지 확인하세요.\n");
        
        // 테스트 케이스 1: 잘못된 토큰
        testInvalidToken();
        
        // 테스트 케이스 2: 빈 토큰
        testEmptyToken();
        
        System.out.println("\n📝 실제 Google OAuth 테스트 방법:");
        System.out.println("1. https://developers.google.com/oauthplayground/ 접속");
        System.out.println("2. Google OAuth2 API v2 선택");
        System.out.println("3. Access Token 획득");
        System.out.println("4. testRealGoogleToken() 메소드에 토큰 입력 후 실행");
    }
    
    private static void testInvalidToken() {
        System.out.println("🧪 테스트 1: 잘못된 토큰");
        
        try {
            String requestBody = """
                {
                    "accessToken": "invalid_fake_token_12345"
                }
                """;
            
            String response = sendRequest(requestBody);
            System.out.println("응답: " + response);
            
        } catch (Exception e) {
            System.err.println("오류: " + e.getMessage());
        }
        
        System.out.println();
    }
    
    private static void testEmptyToken() {
        System.out.println("🧪 테스트 2: 빈 토큰");
        
        try {
            String requestBody = """
                {
                    "accessToken": ""
                }
                """;
            
            String response = sendRequest(requestBody);
            System.out.println("응답: " + response);
            
        } catch (Exception e) {
            System.err.println("오류: " + e.getMessage());
        }
        
        System.out.println();
    }
    
    /**
     * 실제 Google Access Token으로 테스트
     * OAuth Playground에서 얻은 토큰을 여기에 입력하여 테스트
     */
    private static void testRealGoogleToken() {
        System.out.println("🧪 테스트 3: 실제 Google 토큰");
        
        // 여기에 실제 Google Access Token 입력
        String realToken = "실제_토큰_여기에_입력";
        
        if ("실제_토큰_여기에_입력".equals(realToken)) {
            System.out.println("⚠️  실제 Google 토큰을 입력한 후 이 메소드를 호출하세요.");
            return;
        }
        
        try {
            String requestBody = String.format("""
                {
                    "accessToken": "%s"
                }
                """, realToken);
            
            String response = sendRequest(requestBody);
            System.out.println("응답: " + response);
            
        } catch (Exception e) {
            System.err.println("오류: " + e.getMessage());
        }
    }
    
    private static String sendRequest(String requestBody) throws Exception {
        HttpClient client = HttpClient.newHttpClient();
        
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(API_URL))
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(requestBody))
            .build();
        
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        
        return String.format("Status: %d, Body: %s", 
            response.statusCode(), 
            response.body());
    }
}