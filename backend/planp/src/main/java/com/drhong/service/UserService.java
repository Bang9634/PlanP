package com.drhong.service;

import java.util.List;

import com.drhong.dao.UserDAO;
import com.drhong.dto.SignupRequest;
import com.drhong.dto.SignupResponse;
import com.drhong.model.User;
import com.drhong.util.PasswordUtil;
import com.drhong.validator.SignupValidator;

/**
 * 사용자 관련 비즈니스 로직을 가지고 있는 서비스 클래스.
 * 
 * <p>회원가입, 로그인 등 사용자 관련 주요 비즈니스 로직을 수행한다.</p>
 * 
 * @author bang9634
 */
public class UserService {
    
    private final UserDAO userDAO;

    public UserService() {
        this.userDAO = new UserDAO();
    }

    public UserService(UserDAO userDAO) {
        this.userDAO = userDAO;
    }

    /**
     * 회원가입을 수행한다.
     * 
     * <p> 매개변수로 회원가입 요청문을 전달받은뒤,
     * 입력 데이터의 유효성 검증과 ID 중복, 이메일 중복 등의
     * 확인 절차를 걸치고, 비밀번호 암호화를 수행해 최종적으로
     * 만들어 진 사용자 객체를 데이터베이스에 저장 후,
     * 성공 유무를 반환한다. <p>
     * 
     * @param request 회원가입 정보가 포함된 요청문
     * @return 회원가입 결과가 포함된 응답문
     * @throw 회원가입 처리 중 예외가 발생하면 예외를 던지고
     *  서버 오류 발생 메시지를 첨부한 회원가입 실패 응답문을
     *  반환한다.
     * @author bang9634
     */
    public SignupResponse signup(SignupRequest request) {
        try {
            // 1. 입력 데이터 유효성 검증
            List<String> validationErrors = SignupValidator.validate(request);
            if (!validationErrors.isEmpty()) {
                String errorMessage = String.join(", ", validationErrors);
                return new SignupResponse(false, errorMessage);
            }

            // 2. 사용자 ID 중복 확인
            if (userDAO.existsByUserId(request.getUserId())) {
                return new SignupResponse(false, "이미 사용중인 사용자 ID입니다.");
            }

            // 3. 이메일 중복 확인
            if (userDAO.existsByEmail(request.getEmail())) {
                return new SignupResponse(false, "이미 사용중인 이메일입니다.");
            }

            // 4. 비밀번호 강도 확인 (선택사항)
            int passwordStrength = PasswordUtil.getPasswordStrength(request.getPassword());
            if (passwordStrength < 40) {
                String strengthText = PasswordUtil.getPasswordStrengthText(request.getPassword());
                return new SignupResponse(false, 
                    String.format("비밀번호 강도가 너무 약합니다. (현재: %s, 권장: 보통 이상)", strengthText));
            }

            // 5. 비밀번호 암호화
            String hashedPassword = PasswordUtil.hash(request.getPassword());

            // 6. 사용자 객체 생성
            User newUser = new User(
                request.getUserId(),
                hashedPassword,
                request.getName(),
                request.getEmail()
            );

            // 7. 데이터베이스에 저장
            boolean saved = userDAO.save(newUser);
            if (!saved) {
                return new SignupResponse(false, "회원가입 처리 중 오류가 발생했습니다.");
            }

            // 8. 성공 응답
            return new SignupResponse(true, "회원가입이 완료되었습니다.", request.getUserId());

        } catch (Exception e) {
            System.err.println("회원가입 처리 중 예외 발생: " + e.getMessage());
            e.printStackTrace();
            return new SignupResponse(false, "서버 오류가 발생했습니다. 잠시 후 다시 시도해주세요.");
        }
    }

    /**
     * 로그인 처리 (비밀번호 검증)
     */
    public boolean login(String userId, String password) {
        try {
            User user = userDAO.findByUserId(userId);
            if (user == null) {
                return false;
            }
            
            return PasswordUtil.verify(password, user.getPassword());
        } catch (Exception e) {
            System.err.println("로그인 처리 중 예외 발생: " + e.getMessage());
            return false;
        }
    }

    /**
     * 비밀번호 변경
     */
    public boolean changePassword(String userId, String oldPassword, String newPassword) {
        try {
            User user = userDAO.findByUserId(userId);
            if (user == null) {
                return false;
            }
            
            // 기존 비밀번호 확인
            if (!PasswordUtil.verify(oldPassword, user.getPassword())) {
                return false;
            }
            
            // 새 비밀번호 해싱 및 저장
            String newHashedPassword = PasswordUtil.hash(newPassword);
            user.setPassword(newHashedPassword);
            
            return userDAO.save(user);
        } catch (Exception e) {
            System.err.println("비밀번호 변경 중 예외 발생: " + e.getMessage());
            return false;
        }
    }

    /**
     * 사용자 ID 중복 확인
     */
    public boolean isUserIdAvailable(String userId) {
        if (userId == null || userId.trim().isEmpty()) {
            return false;
        }
        return !userDAO.existsByUserId(userId);
    }

    /**
     * 이메일 중복 확인
     */
    public boolean isEmailAvailable(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        return !userDAO.existsByEmail(email);
    }

    /**
     * 사용자 조회 (ID로)
     */
    public User getUserById(String userId) {
        return userDAO.findByUserId(userId);
    }

    /**
     * 사용자 조회 (이메일로)
     */
    public User getUserByEmail(String email) {
        return userDAO.findByEmail(email);
    }

    /**
     * 전체 사용자 수 조회 (관리용)
     */
    public int getUserCount() {
        return userDAO.count();
    }


    
    // UserService.java에 디버깅 메서드 추가
    public void debugUserStorage() {
        System.out.println("=== 사용자 저장소 상태 ===");
        System.out.println("저장된 사용자 수: " + userDAO.count());
        
        // 모든 사용자 출력
        userDAO.findAll().forEach(user -> {
            System.out.println("사용자: " + user.getUserId() + ", 이메일: " + user.getEmail());
        });
        
        System.out.println("========================");
    }
}