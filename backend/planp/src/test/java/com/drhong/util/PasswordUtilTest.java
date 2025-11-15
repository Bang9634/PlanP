package com.drhong.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

@SuppressWarnings("unused")
@DisplayName("PasswordUtil 테스트")
class PasswordUtilTest {

    @Nested
    @DisplayName("비밀번호 해싱 테스트")
    class HashingTests {

        @Test
        @DisplayName("기본 해싱 - 성공")
        void hashPassword() {
            String password = "testPassword123";
            String hashedPassword = PasswordUtil.hash(password);
            
            assertThat(hashedPassword).isNotNull();
            assertThat(hashedPassword).isNotEqualTo(password);
            assertThat(hashedPassword).startsWith("$2a$12$");
            assertThat(hashedPassword).hasSize(60);
        }

        @Test
        @DisplayName("같은 비밀번호도 매번 다른 해시 생성")
        void differentSaltEachTime() {
            String password = "samePassword";
            String hash1 = PasswordUtil.hash(password);
            String hash2 = PasswordUtil.hash(password);
            
            assertThat(hash1).isNotEqualTo(hash2);
        }

        @Test
        @DisplayName("null 비밀번호 해싱 - 예외 발생")
        void hashNullPassword() {
            assertThatThrownBy(() -> PasswordUtil.hash(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("비밀번호는 null이거나 빈 문자열일 수 없습니다.");
        }
    }

    @Nested
    @DisplayName("비밀번호 검증 테스트")
    class VerificationTests {

        @Test
        @DisplayName("올바른 비밀번호 검증 - 성공")
        void verifyCorrectPassword() {
            String password = "correctPassword123";
            String hashedPassword = PasswordUtil.hash(password);
            
            assertThat(PasswordUtil.verify(password, hashedPassword)).isTrue();
        }

        @Test
        @DisplayName("잘못된 비밀번호 검증 - 실패")
        void verifyIncorrectPassword() {
            String password = "correctPassword123";
            String wrongPassword = "wrongPassword123";
            String hashedPassword = PasswordUtil.hash(password);
            
            assertThat(PasswordUtil.verify(wrongPassword, hashedPassword)).isFalse();
        }
    }

    @Nested
    @DisplayName("비밀번호 강도 테스트")
    class PasswordStrengthTests {

        @ParameterizedTest
        @ValueSource(strings = {"Password123!", "StrongPass1@"})
        @DisplayName("강한 비밀번호")
        void strongPassword(String password) {
            int strength = PasswordUtil.getPasswordStrength(password);
            assertThat(strength).isGreaterThanOrEqualTo(60);
            assertThat(PasswordUtil.getPasswordStrengthText(password))
                .isIn("강함", "매우 강함");
        }

        @ParameterizedTest
        @ValueSource(strings = {"password", "123456", "abc"})
        @DisplayName("약한 비밀번호")
        void weakPassword(String password) {
            int strength = PasswordUtil.getPasswordStrength(password);
            assertThat(strength).isLessThan(40);
            assertThat(PasswordUtil.getPasswordStrengthText(password))
                .isIn("매우 약함", "약함");
        }
    }

    @Test
    @DisplayName("임시 비밀번호 생성")
    void generateTemporaryPassword() {
        String tempPassword = PasswordUtil.generateTemporaryPassword(12);
        
        assertThat(tempPassword).hasSize(12);
        assertThat(PasswordUtil.getPasswordStrength(tempPassword)).isGreaterThan(60);
    }
}