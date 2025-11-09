package com.drhong;

/**
 * 
 * @auther whwoghd
 */
public class LoginTest {
    public static void main(String[] args) {
        Login login = new Login();
        
        // 1. 잘못된 로그인 시도
        System.out.println("1. 잘못된 ID/PW로 로그인 시도:");
        boolean result1 = login.login("wrong", "wrong");
        System.out.println("로그인 결과: " + (result1 ? "성공" : "실패"));
        
        // 2. 올바른 로그인 시도
        System.out.println("\n2. 테스트 계정으로 로그인 시도:");
        // 데이터베이스에 있는 계정 정보로 수정하세요
        boolean result2 = login.login("admin", "admin123");
        System.out.println("로그인 결과: " + (result2 ? "성공" : "실패"));
        
        // 추가 테스트를 위한 다른 계정
        System.out.println("\n3. 다른 계정으로 로그인 시도:");
        boolean result3 = login.login("user1", "pass1");
        System.out.println("로그인 결과: " + (result3 ? "성공" : "실패"));
    }
}