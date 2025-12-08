package com.drhong.service;

import com.drhong.repository.UserRepository;
import org.mockito.Mockito;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UserServiceMailTest {
    @Test
    void testSendAndVerifyEmailCode() {
        UserRepository userRepository = Mockito.mock(UserRepository.class);
        UserService userService = new UserService(userRepository);
        String testEmail = "choo1815@gmail.com";
        // 인증코드 발송 및 저장
        userService.sendEmailCode(testEmail);
        // 서버에 저장된 인증코드 출력
        String savedCode = null;
        try {
            java.lang.reflect.Field codeMapField = userService.getClass().getDeclaredField("emailCodeMap");
            codeMapField.setAccessible(true);
            @SuppressWarnings("unchecked")
            java.util.Map<String, String> codeMap = (java.util.Map<String, String>) codeMapField.get(userService);
            savedCode = codeMap.get(testEmail);
            System.out.println("서버에 저장된 인증코드: " + savedCode);
        } catch (Exception e) {
            System.out.println("코드 조회 오류: " + e.getMessage());
        }
        // 바로 저장된 인증코드로 검증
        boolean result = userService.verifyEmailCode(testEmail, savedCode);
        System.out.println("즉시 검증 결과: " + result);
        assertTrue(result, "발송된 인증코드로 인증에 성공해야 합니다.");
    }
}
