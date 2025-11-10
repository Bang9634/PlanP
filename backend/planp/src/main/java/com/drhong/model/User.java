package com.drhong.model;

import java.time.LocalDateTime;

/**
 * 사용자 정보를 담는 모델 클래스
 * <p>
 * 비즈니스 로직을 포함하지 않는 순수 데이터 모델이다.
 * 회원가입, 로그인 등 사용자 관련 기능에서 사용되며,
 * 데이터베이스의 user 테이블과 매핑된다.
 * </p>
 * 
 * @author bang9634
 * @since 2025-11-10
 */
public class User {
    private String userId;          // 사용자의 ID
    private String password;        // 사용자의 비밀번호. 데이터베이스에 전송할 땐 해싱해서 보낸다.
    private String name;            // 사용자의 이름
    private String email;           // 사용자의 이메일
    private LocalDateTime createdAt;// 계정 생성 날짜
    private boolean active;         // 계정 활성화 여부

    /**
     * 기본 유저 객체를 생성한다.
     * <p>
     * 계정 생성 날짜를 현재 로컬 시간으로 초기화한다.
     * 계정 활성화 여부를 true로 설정한다.
     * </p>
     */
    public User() {
        this.createdAt = LocalDateTime.now();
        this.active = true;
    }

    /**
     * 로그인용 유저 객체를 생성한다.
     * <p>
     * 매개변수로 사용자의 아이디와 비밀번호, 이름, 이메일을 받아 초기화한다.
     * 계정 생성 날짜를 현재 로컬 시간으로 초기화한다.
     * 계정 활성화 여부를 true로 설정한다.
     * </p>
     * 
     * @param userId 사용자의 아이디
     * @param password 사용자의 비밀번호
     * @param name 사용자의 이름
     * @param email 사용자의 이메일
     */
    public User(String userId, String password, String name, String email) {
        this();
        this.userId = userId;
        this.password = password;
        this.name = name;
        this.email = email;
    }

    /**
     * 각 멤버 변수의 게터와 세터
     */
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    /**
     * User 객체의 문자열 표현을 반환한다.
     * <p>
     * 사용자의 ID, 이름, 이메일, 활성화 상태를 포함한 문자열을 생성한다.
     * 비밀번호와 생성 날짜는 보안과 가독성을 위해 제외된다.
     * </p>
     * 
     * @return User 객체의 문자열 표현 (형식: "User{userId='값', name='값', email='값', active=값}")
     */
    @Override
    public String toString() {
        return String.format("User{userId='%s', name='%s', email='%s', active=%s}", 
                           userId, name, email, active);
    }
}