package com.drhong.dto;

/**
 * Google OAuth 사용자 정보 DTO 클래스
 * 
 * <p>
 * Google API에서 받아온 사용자 정보를 담는 간단한 데이터 전송 객체이다.
 * </p>
 * 
 * @author wnwoghd
 * @since 2025-12-03
 */
public class GoogleUser {
    
    private String id;
    private String email;
    private String name;
    
    public GoogleUser() {
    }
    
    public GoogleUser(String id, String email, String name) {
        this.id = id;
        this.email = email;
        this.name = name;
    }
    
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    

    

    
    @Override
    public String toString() {
        return "GoogleUser{" +
                "id='" + id + '\'' +
                ", email='" + email + '\'' +
                ", name='" + name + '\'' +
                '}';
    }}