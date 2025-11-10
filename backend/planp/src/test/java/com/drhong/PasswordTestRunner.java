package com.drhong;

import com.drhong.util.PasswordUtil;

/**
 * PasswordUtil 기능 테스트 실행기
 */
public class PasswordTestRunner {

    public static void main(String[] args) {
        System.out.println("=== PasswordUtil 기능 테스트 ===\n");

        // 1. 기본 해싱 테스트
        System.out.println("1. 비밀번호 해싱 테스트");
        String originalPassword = "myPassword123!";
        String hashedPassword = PasswordUtil.hash(originalPassword);
        System.out.println("원본 비밀번호: " + originalPassword);
        System.out.println("해싱된 비밀번호: " + hashedPassword);
        System.out.println("해시 유효성: " + PasswordUtil.isValidHash(hashedPassword));
        System.out.println("라운드 수: " + PasswordUtil.getRounds(hashedPassword));

        // 2. 비밀번호 검증 테스트
        System.out.println("\n2. 비밀번호 검증 테스트");
        boolean correctVerification = PasswordUtil.verify(originalPassword, hashedPassword);
        boolean incorrectVerification = PasswordUtil.verify("wrongPassword", hashedPassword);
        System.out.println("올바른 비밀번호 검증: " + correctVerification);
        System.out.println("틀린 비밀번호 검증: " + incorrectVerification);

        // 3. 비밀번호 강도 테스트
        System.out.println("\n3. 비밀번호 강도 테스트");
        String[] testPasswords = {
            "abc",
            "password",
            "Password1",
            "Password123",
            "Password123!",
            "MyVeryStrongPassword123!@#"
        };

        for (String password : testPasswords) {
            int strength = PasswordUtil.getPasswordStrength(password);
            String strengthText = PasswordUtil.getPasswordStrengthText(password);
            System.out.printf("'%s' -> 점수: %d, 강도: %s%n", password, strength, strengthText);
        }

        // 4. 임시 비밀번호 생성 테스트
        System.out.println("\n4. 임시 비밀번호 생성 테스트");
        for (int i = 0; i < 3; i++) {
            String tempPassword = PasswordUtil.generateTemporaryPassword(12);
            int tempStrength = PasswordUtil.getPasswordStrength(tempPassword);
            System.out.printf("임시 비밀번호 %d: %s (강도: %d)%n", i+1, tempPassword, tempStrength);
        }

        System.out.println("\n=== 테스트 완료 ===");
    }
}