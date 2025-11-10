package com.drhong.util;

import org.mindrot.jbcrypt.BCrypt;

/**
 * BCrypt를 사용한 비밀번호 해싱 및 검증 유틸리티
 */
public class PasswordUtil {
    
    /**
     * 기본 해싱 라운드 (보안과 성능의 균형)
     * 라운드가 높을수록 보안은 강화되지만 처리 시간 증가
     */
    private static final int DEFAULT_ROUNDS = 12;

    /**
     * 비밀번호 해싱
     * 
     * @param plainPassword 평문 비밀번호
     * @return 해싱된 비밀번호
     * @throws IllegalArgumentException 비밀번호가 null이거나 빈 문자열인 경우
     */
    public static String hash(String plainPassword) {
        if (plainPassword == null || plainPassword.trim().isEmpty()) {
            throw new IllegalArgumentException("비밀번호는 null이거나 빈 문자열일 수 없습니다.");
        }
        
        return BCrypt.hashpw(plainPassword, BCrypt.gensalt(DEFAULT_ROUNDS));
    }

    /**
     * 커스텀 라운드로 비밀번호 해싱
     * 
     * @param plainPassword 평문 비밀번호
     * @param rounds 해싱 라운드 (4-31 사이)
     * @return 해싱된 비밀번호
     * @throws IllegalArgumentException 비밀번호가 null이거나 빈 문자열이거나 라운드가 범위를 벗어난 경우
     */
    public static String hash(String plainPassword, int rounds) {
        if (plainPassword == null || plainPassword.trim().isEmpty()) {
            throw new IllegalArgumentException("비밀번호는 null이거나 빈 문자열일 수 없습니다.");
        }
        
        if (rounds < 4 || rounds > 31) {
            throw new IllegalArgumentException("라운드는 4-31 사이여야 합니다.");
        }
        
        return BCrypt.hashpw(plainPassword, BCrypt.gensalt(rounds));
    }

    /**
     * 비밀번호 검증
     * 
     * @param plainPassword 평문 비밀번호
     * @param hashedPassword 해싱된 비밀번호
     * @return 일치하면 true, 불일치하면 false
     */
    public static boolean verify(String plainPassword, String hashedPassword) {
        if (plainPassword == null || hashedPassword == null) {
            return false;
        }
        
        try {
            return BCrypt.checkpw(plainPassword, hashedPassword);
        } catch (Exception e) {
            // 잘못된 해시 형식이나 기타 오류
            System.err.println("비밀번호 검증 중 오류 발생: " + e.getMessage());
            return false;
        }
    }

    /**
     * 해싱된 비밀번호가 유효한 BCrypt 형식인지 확인
     * 
     * @param hashedPassword 해싱된 비밀번호
     * @return 유효한 BCrypt 해시면 true
     */
    public static boolean isValidHash(String hashedPassword) {
        if (hashedPassword == null || hashedPassword.isEmpty()) {
            return false;
        }
        
        // BCrypt 해시는 '$2a$', '$2b$', '$2x$', '$2y$'로 시작하고 60자여야 함
        return hashedPassword.matches("^\\$2[abxy]\\$\\d{2}\\$.{53}$");
    }

    /**
     * 해시된 비밀번호에서 라운드 수 추출
     * 
     * @param hashedPassword 해싱된 비밀번호
     * @return 라운드 수, 유효하지 않으면 -1
     */
    public static int getRounds(String hashedPassword) {
        if (!isValidHash(hashedPassword)) {
            return -1;
        }
        
        try {
            // $2a$12$... 형태에서 12 부분 추출
            String[] parts = hashedPassword.split("\\$");
            return Integer.parseInt(parts[2]);
        } catch (Exception e) {
            return -1;
        }
    }

    /**
     * 비밀번호 강도 확인
     * 
     * @param password 확인할 비밀번호
     * @return 강도 점수 (0-100)
     */
    public static int getPasswordStrength(String password) {
        if (password == null || password.isEmpty()) {
            return 0;
        }
        
        int score = 0;
        
        // 길이 점수 (최대 25점)
        if (password.length() >= 12) score += 25;
        else if (password.length() >= 8) score += 20;
        else if (password.length() >= 6) score += 15;
        else if (password.length() >= 4) score += 5;
        
        // 소문자 포함 (15점)
        if (password.matches(".*[a-z].*")) score += 15;
        
        // 대문자 포함 (15점)
        if (password.matches(".*[A-Z].*")) score += 15;
        
        // 숫자 포함 (15점)
        if (password.matches(".*\\d.*")) score += 15;
        
        // 특수문자 포함 (15점)
        if (password.matches(".*[!@#$%^&*(),.?\":{}|<>].*")) score += 15;
        
        // 다양한 문자 조합 보너스 (최대 10점)
        int variety = 0;
        if (password.matches(".*[a-z].*")) variety++;
        if (password.matches(".*[A-Z].*")) variety++;
        if (password.matches(".*\\d.*")) variety++;
        if (password.matches(".*[!@#$%^&*(),.?\":{}|<>].*")) variety++;
        
        if (variety >= 3) score += 10;
        else if (variety >= 2) score += 5;
        
        return Math.min(score, 100);
    }

    /**
     * 비밀번호 강도를 문자열로 반환
     * 
     * @param password 확인할 비밀번호
     * @return 강도 문자열 (매우 약함, 약함, 보통, 강함, 매우 강함)
     */
    public static String getPasswordStrengthText(String password) {
        int strength = getPasswordStrength(password);
        
        if (strength >= 80) return "매우 강함";
        else if (strength >= 60) return "강함";
        else if (strength >= 40) return "보통";
        else if (strength >= 20) return "약함";
        else return "매우 약함";
    }

    /**
     * 안전한 임시 비밀번호 생성 (선택사항)
     * 
     * @param length 비밀번호 길이 (최소 8자)
     * @return 임시 비밀번호
     */
    public static String generateTemporaryPassword(int length) {
        if (length < 8) {
            throw new IllegalArgumentException("임시 비밀번호는 최소 8자 이상이어야 합니다.");
        }
        
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*";
        StringBuilder password = new StringBuilder();
        
        // 각 카테고리에서 최소 1개씩 포함
        password.append("ABCDEFGHIJKLMNOPQRSTUVWXYZ".charAt((int) (Math.random() * 26))); // 대문자
        password.append("abcdefghijklmnopqrstuvwxyz".charAt((int) (Math.random() * 26))); // 소문자
        password.append("0123456789".charAt((int) (Math.random() * 10))); // 숫자
        password.append("!@#$%^&*".charAt((int) (Math.random() * 8))); // 특수문자
        
        // 나머지 길이만큼 랜덤 선택
        for (int i = 4; i < length; i++) {
            password.append(chars.charAt((int) (Math.random() * chars.length())));
        }
        
        // 문자열 섞기
        char[] array = password.toString().toCharArray();
        for (int i = array.length - 1; i > 0; i--) {
            int index = (int) (Math.random() * (i + 1));
            char temp = array[index];
            array[index] = array[i];
            array[i] = temp;
        }
        
        return new String(array);
    }
}