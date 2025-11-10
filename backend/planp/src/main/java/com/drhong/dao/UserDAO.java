package com.drhong.dao;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.drhong.model.User;

/**
 * 사용자 데이터 접근 객체 (임시 메모리 저장소)
 * 나중에 DB 연동으로 교체 예정
 */
public class UserDAO {
    
    // 임시 메모리 저장소 (thread-safe)
    private static final ConcurrentMap<String, User> userStorage = new ConcurrentHashMap<>();
    private static final ConcurrentMap<String, User> emailStorage = new ConcurrentHashMap<>();

    /**
     * 사용자 저장
     */
    public boolean save(User user) {
        try {
            /**
             * @TODO: 임시 스토리지가 아닌 DB연동으로 수정해야함.
             */
            userStorage.put(user.getUserId(), user);
            emailStorage.put(user.getEmail(), user);
            return true;
        } catch (Exception e) {
            System.err.println("사용자 저장 실패: " + e.getMessage());
            return false;
        }
    }

    /**
     * 사용자 ID로 조회
     */
    public User findByUserId(String userId) {
        return userStorage.get(userId);
    }

    /**
     * 이메일로 조회
     */
    public User findByEmail(String email) {
        return emailStorage.get(email);
    }

    /**
     * 사용자 ID 중복 확인
     */
    public boolean existsByUserId(String userId) {
        return userStorage.containsKey(userId);
    }

    /**
     * 이메일 중복 확인
     */
    public boolean existsByEmail(String email) {
        return emailStorage.containsKey(email);
    }

    /**
     * 모든 사용자 조회 (테스트용)
     */
    public List<User> findAll() {
        return new ArrayList<>(userStorage.values());
    }

    /**
     * 전체 사용자 수 (테스트용)
     */
    public int count() {
        return userStorage.size();
    }

    /**
     * 모든 데이터 삭제 (테스트용)
     */
    public void deleteAll() {
        userStorage.clear();
        emailStorage.clear();
    }

    /**
     * 특정 사용자 삭제 (테스트용)
     */
    public boolean deleteByUserId(String userId) {
        User user = userStorage.remove(userId);
        if (user != null) {
            emailStorage.remove(user.getEmail());
            return true;
        }
        return false;
    }
}