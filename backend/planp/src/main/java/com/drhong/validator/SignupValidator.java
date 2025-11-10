
package com.drhong.validator;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import com.drhong.dto.SignupRequest;

/**
 * 회원가입 유효성 검증 클래스
 */
public class SignupValidator {
    
    private static final Pattern EMAIL_PATTERN = 
        Pattern.compile("^[A-Za-z0-9+_.-]+@([A-Za-z0-9.-]+\\.[A-Za-z]{2,})$");
    
    private static final Pattern USER_ID_PATTERN = 
        Pattern.compile("^[a-zA-Z0-9_]{3,20}$");

    /**
     * 회원가입 요청 데이터 유효성 검증
     */
    public static List<String> validate(SignupRequest request) {
        List<String> errors = new ArrayList<>();

        // null 체크
        if (request == null) {
            errors.add("요청 데이터가 없습니다.");
            return errors;
        }

        // 사용자 ID 검증
        validateUserId(request.getUserId(), errors);
        
        // 비밀번호 검증
        validatePassword(request.getPassword(), errors);
        
        // 이름 검증
        validateName(request.getName(), errors);
        
        // 이메일 검증
        validateEmail(request.getEmail(), errors);

        return errors;
    }

    private static void validateUserId(String userId, List<String> errors) {
        if (userId == null || userId.trim().isEmpty()) {
            errors.add("사용자 ID는 필수입니다.");
        } else if (!USER_ID_PATTERN.matcher(userId).matches()) {
            errors.add("사용자 ID는 3-20자의 영문자, 숫자, 언더스코어만 사용 가능합니다.");
        }
    }

    private static void validatePassword(String password, List<String> errors) {
        if (password == null || password.isEmpty()) {
            errors.add("비밀번호는 필수입니다.");
        } else if (password.length() < 6) {
            errors.add("비밀번호는 최소 6자 이상이어야 합니다.");
        } else if (password.length() > 50) {
            errors.add("비밀번호는 최대 50자까지 가능합니다.");
        }
    }

    private static void validateName(String name, List<String> errors) {
        if (name == null || name.trim().isEmpty()) {
            errors.add("이름은 필수입니다.");
        } else if (name.length() > 50) {
            errors.add("이름은 최대 50자까지 가능합니다.");
        }
    }

    private static void validateEmail(String email, List<String> errors) {
        if (email == null || email.trim().isEmpty()) {
            errors.add("이메일은 필수입니다.");
        } else if (!EMAIL_PATTERN.matcher(email).matches()) {
            errors.add("올바른 이메일 형식이 아닙니다.");
        }
    }
}