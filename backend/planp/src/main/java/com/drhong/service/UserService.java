package com.drhong.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.drhong.dao.UserDAO;
import com.drhong.dto.SignupRequest;
import com.drhong.dto.SignupResponse;
import com.drhong.model.User;
import com.drhong.util.PasswordUtil;
import com.drhong.validator.SignupValidator;

/**
 * 사용자 관련 비즈니스 로직을 처리하는 서비스 클래스
 * <p>
 * 이 클래스는 사용자 관리의 핵심 비즈니스 로직을 담당한다.
 * 회원가입, 로그인 인증, 비밀번호 변경, 중복 확인 등의 기능을 제공하며,
 * 컨트롤러와 DAO 사이에서 비즈니스 규칙을 적용하고 데이터 무결성을 보장한다.
 * </p>
 * 
 * <h3>주요 책임:</h3>
 * <ul>
 *   <li>입력 데이터 검증 및 비즈니스 룰 적용</li>
 *   <li>비밀번호 암호화/검증 처리</li>
 *   <li>사용자 중복성 검사 (ID, 이메일)</li>
 *   <li>사용자 인증 및 권한 검증</li>
 *   <li>데이터 접근 계층과의 상호작용</li>
 * </ul>
 * 
 * <h3>보안 정책:</h3>
 * <ul>
 *   <li>모든 비밀번호는 bcrypt 해싱으로 암호화</li>
 *   <li>비밀번호 강도 검증 </li>
 *   <li>사용자 ID와 이메일 유일성 보장</li>
 *   <li>입력값 유효성 검증을 통한 보안 강화</li>
 * </ul>
 * 
 * <h3>사용 예시:</h3>
 * <pre>{@code
 * UserDAO userDAO = new UserDAO();
 * UserService userService = new UserService(userDAO);
 * 
 * // 회원가입
 * SignupRequest request = new SignupRequest("user123", "password!", "홍길동", "hong@example.com");
 * SignupResponse response = userService.signup(request);
 * 
 * // 로그인
 * boolean loginSuccess = userService.login("user123", "password!");
 * 
 * // 중복 확인
 * boolean available = userService.isUserIdAvailable("newuser");
 * }</pre>
 * 
 * @author bang9634
 * @since 2025-11-10
 * 
 * @see com.drhong.dao.UserDAO
 * @see com.drhong.dto.SignupRequest
 * @see com.drhong.dto.SignupResponse
 * @see com.drhong.util.PasswordUtil
 * @see com.drhong.validator.SignupValidator
 */
public class UserService {

    /** SLF4J 로거 인스턴스 - 비즈니스 로직 처리 과정 로깅 */
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);
    
    /** 사용자 데이터 접근 객체 */
    private final UserDAO userDAO;

    /** 비밀번호 최소 강도 점수 (0-100 점 중) */
    private static final int MIN_PASSWORD_STRENGTH = 40;

    /**
     * UserDAO를 주입받는 생성자
     * <p>
     * 의존성 주입을 통해 UserDAO 구현체를 받아 초기화한다.
     * 테스트나 다른 DAO 구현체 사용 시 유용하다.
     * </p>
     * 
     * @param userDAO 사용자 데이터 접근을 위한 DAO 객체
     * 
     * @throws NullPointerException userDAO가 null인 경우
     */
    public UserService(UserDAO userDAO) {
        if (userDAO == null) {
            throw new NullPointerException("UserDAO는 null일 수 없습니다");
        }
        this.userDAO = userDAO;
        logger.info("UserService 초기화 완료 (주입된 UserDAO 사용)");
    }

    /**
     * 사용자 회원가입을 처리하는 메서드
     * <p>
     * 회원가입 요청을 받아 다음과 같은 처리를 수행한다:
     * 입력 데이터 검증 → 중복 확인 → 비밀번호 강도 검증 → 암호화 → 저장
     * 각 단계에서 실패 시 적절한 오류 메시지를 반환한다.
     * </p>
     * 
     * <h4>처리 단계:</h4>
     * <ol>
     *   <li>입력 데이터 유효성 검증 (필수 필드, 형식 등)</li>
     *   <li>사용자 ID 중복 확인</li>
     *   <li>이메일 주소 중복 확인</li>
     *   <li>비밀번호 강도 검증 (최소 40점)</li>
     *   <li>비밀번호 bcrypt 해싱</li>
     *   <li>사용자 객체 생성 및 저장</li>
     *   <li>성공/실패 응답 반환</li>
     * </ol>
     * 
     * <h4>검증 실패 시나리오:</h4>
     * <ul>
     *   <li>필수 필드 누락 → "필드명이 필수입니다"</li>
     *   <li>형식 오류 → "올바른 형식이 아닙니다"</li>
     *   <li>ID 중복 → "이미 사용중인 사용자 ID입니다"</li>
     *   <li>이메일 중복 → "이미 사용중인 이메일입니다"</li>
     *   <li>비밀번호 약함 → "비밀번호 강도가 너무 약합니다"</li>
     * </ul>
     * 
     * @param request 회원가입 정보가 포함된 요청 DTO
     * @return 회원가입 처리 결과를 담은 응답 DTO
     * 
     * @exception Exception 회원 처리 중 예외가 발생한 경우
     * 
     * @apiNote 예외 발생 시에도 사용자 친화적 메시지로 실패 응답 반환
     */
    public SignupResponse signup(SignupRequest request) {
        if (request == null) {
            logger.error("회원가입 요청이 null입니다");
            return new SignupResponse(false, "잘못된 요청입니다.");
        }
        
        logger.info("회원가입 처리 시작: userId={}, email={}", request.getUserId(), request.getEmail());
        
        try {
            // 1. 입력 데이터 유효성 검증
            logger.debug("Request 유효성 검증 시작...");
            List<String> validationErrors = SignupValidator.validate(request);
            if (!validationErrors.isEmpty()) {
                logger.debug("Request가 유효하지 않음");
                String errorMessage = String.join(", ", validationErrors);
                return new SignupResponse(false, errorMessage);
            }
            logger.debug("정상적인 Request");

            // 2. 사용자 ID 중복 확인
            logger.debug("사용자 ID 중복 확인 시작...");
            if (!isUserIdAvailable(request.getUserId())) {
                logger.warn("사용자 ID 중복 발생: userId={}", request.getUserId());
                return new SignupResponse(false, "이미 사용중인 사용자 ID입니다.");
            }
            logger.debug("사용자 ID 중복되지않음: userId={}", request.getUserId());

            // 3. 이메일 중복 확인
            logger.debug("사용자 이메일 중복 시작...");
            if (!isEmailAvailable(request.getEmail())) {
                logger.warn("사용자 이메일 중복 발생: email={}", request.getEmail());
                return new SignupResponse(false, "이미 사용중인 이메일입니다.");
            }
            logger.debug("사용자 이메일 중복되지않음: email={}", request.getEmail());

            // 4. 비밀번호 강도 확인 (선택사항)
            logger.debug("비밀번호 강도 확인 시작...");
            int passwordStrength = PasswordUtil.getPasswordStrength(request.getPassword());
            if (passwordStrength < MIN_PASSWORD_STRENGTH) {
                String strengthText = PasswordUtil.getPasswordStrengthText(request.getPassword());
                logger.warn("비밀번호 강도 부족: userId={}, strength={}/{}", 
                    request.getUserId(), passwordStrength, MIN_PASSWORD_STRENGTH);
                return new SignupResponse(false, 
                    String.format("비밀번호 강도가 너무 약합니다. (현재: %s, 권장: 보통 이상)", strengthText));
            }
            logger.debug("비밀번호 강도 충분함");

            // 5. 비밀번호 암호화
            logger.debug("비밀번호 암호화 시작...");
            String hashedPassword = PasswordUtil.hash(request.getPassword());
            logger.debug("비밀번호 암호화 완료: userId={}", request.getUserId());

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
                logger.error("사용자 저장 실패: userId={}", request.getUserId());
                return new SignupResponse(false, "회원가입 처리 중 오류가 발생했습니다.");
            }

            // 8. 성공 응답
            logger.info("회원가입 성공: userId={}, email={}",
                request.getUserId(), request.getEmail());
            return new SignupResponse(true, "회원가입이 완료되었습니다.", request.getUserId());

        } catch (Exception e) {
            System.err.println("회원가입 처리 중 예외 발생: " + e.getMessage());
            logger.error("회원가입 처리 중 예상치 못한 오류: userId={}", request.getUserId(), e);
            return new SignupResponse(false, "서버 오류가 발생했습니다. 잠시 후 다시 시도해주세요.");
        }
    }

    /**
     * 사용자 로그인 인증을 처리하는 메서드
     * <p>
     * 제공된 사용자 ID와 비밀번호를 검증하여 로그인 성공 여부를 반환한다.
     * 비밀번호는 bcrypt를 사용하여 안전하게 검증된다.
     * </p>
     * 
     * <h4>인증 과정:</h4>
     * <ol>
     *   <li>사용자 ID로 사용자 조회</li>
     *   <li>사용자 존재 여부 확인</li>
     *   <li>입력된 비밀번호와 저장된 해시 비교</li>
     *   <li>인증 결과 반환</li>
     * </ol>
     * 
     * @param userId 로그인할 사용자 ID
     * @param password 평문 비밀번호
     * @return 인증 성공 시 true, 실패 시 false
     * 
     * @apiNote 보안을 위해 사용자 존재 여부와 비밀번호 오류를 구분하지 않음
     */
    public boolean login(String userId, String password) {
        if (userId == null || password == null) {
            logger.warn("로그인 시도 - null 파라미터: userId={}, password={}", userId, password != null);
            return false;
        }

        logger.debug("로그인 시도: userId={}", userId);

        try {
            // 사용자 조회
            User user = userDAO.findByUserId(userId);
            if (user == null) {
                logger.debug("로그인 실패 - 사용자 없음: userId={}", userId);
                return false;
            }
            
            // 비밀번호 검증
            boolean isValid = PasswordUtil.verify(password, user.getPassword());
            
            if (isValid) {
                logger.info("로그인 성공: userId={}", userId);
            } else {
                logger.warn("로그인 실패 - 비밀번호 불일치: userId={}", userId);
            }

            return isValid;

        } catch (Exception e) {
            logger.error("로그인 처리 중 예외 발생: userId={}", userId, e);
            return false;
        }
    }

    /**
     * 사용자 비밀번호를 변경하는 메서드
     * <p>
     * 기존 비밀번호를 확인한 후 새 비밀번호로 안전하게 변경한다.
     * 새 비밀번호는 bcrypt로 해싱되어 저장된다.
     * </p>
     * 
     * <h4>변경 과정:</h4>
     * <ol>
     *   <li>사용자 ID로 사용자 조회</li>
     *   <li>기존 비밀번호 검증</li>
     *   <li>새 비밀번호 해싱</li>
     *   <li>사용자 정보 업데이트</li>
     * </ol>
     * 
     * @param userId 비밀번호를 변경할 사용자 ID
     * @param oldPassword 현재 비밀번호 (평문)
     * @param newPassword 새 비밀번호 (평문)
     * @return 변경 성공 시 true, 실패 시 false
     * 
     * @apiNote 새 비밀번호의 강도 검증은 별도로 수행하는 것을 권장
     */
    public boolean changePassword(String userId, String oldPassword, String newPassword) {
        if (userId == null || oldPassword == null || newPassword == null) {
            logger.warn("비밀번호 변경 시도 - null 파라미터: userId={}", userId);
            return false;
        }

        logger.info("비밀번호 변경 시도: userId={}", userId);

        try {
            // 사용자 조회
            User user = userDAO.findByUserId(userId);
            if (user == null) {
                logger.warn("비밀번호 변경 실패 - 사용자 없음: userId={}", userId);
                return false;
            }
            
            // 기존 비밀번호 확인
            if (!PasswordUtil.verify(oldPassword, user.getPassword())) {
                logger.warn("비밀번호 변경 실패 - 기존 비밀번호 불일치: userId={}", userId);
                return false;
            }
            
            // 새 비밀번호 해싱 및 저장
            String newHashedPassword = PasswordUtil.hash(newPassword);
            user.setPassword(newHashedPassword);
            
            boolean updated = userDAO.save(user);

            if (updated) {
                logger.info("비밀번호 변경 성공: userId={}", userId);
            } else {
                logger.error("비밀번호 변경 실패 - 저장 오류: userId={}", userId);
            }

            return updated;
        } catch (Exception e) {
            logger.error("비밀번호 변경 중 예외 발생: userId={}", userId, e);
            return false;
        }
    }

    /**
     * 사용자 ID 사용 가능 여부를 확인하는 메서드
     * <p>
     * 주어진 사용자 ID가 이미 등록되어 있는지 확인한다.
     * 회원가입 시 실시간 중복 검사에 사용된다.
     * </p>
     * 
     * @param userId 확인할 사용자 ID
     * @return 사용 가능하면 true, 이미 사용중이면 false
     * 
     * @apiNote null이거나 빈 문자열인 경우 false 반환
     */
    public boolean isUserIdAvailable(String userId) {
        if (userId == null || userId.trim().isEmpty()) {
            logger.debug("ID 사용가능성 확인 - 유효하지 않은 ID: userId={}", userId);
            return false;
        }
        
        boolean available = userDAO.findByUserId(userId) == null;
        logger.debug("ID 사용가능성 확인: userId={}, available={}", userId, available);
        return available;
    }

    /**
     * 이메일 주소 사용 가능 여부를 확인하는 메서드
     * <p>
     * 주어진 이메일이 이미 다른 사용자에 의해 등록되어 있는지 확인한다.
     * 회원가입 시 실시간 중복 검사에 사용된다.
     * </p>
     * 
     * @param email 확인할 이메일 주소
     * @return 사용 가능하면 true, 이미 사용중이면 false
     * 
     * @apiNote null이거나 빈 문자열인 경우 false 반환
     */
    public boolean isEmailAvailable(String email) {
        if (email == null || email.trim().isEmpty()) {
            logger.debug("이메일 사용가능성 확인 - 유효하지 않은 이메일: email={}", email);
            return false;
        }
        
        boolean available = userDAO.findByEmail(email) == null;
        logger.debug("이메일 사용가능성 확인: email={}, available={}", email, available);
        return available;
    }

    /**
     * 사용자 ID로 사용자 정보를 조회하는 메서드
     * <p>
     * 주어진 사용자 ID에 해당하는 사용자 객체를 반환한다.
     * 프로필 조회, 정보 수정 등에 사용된다.
     * </p>
     * 
     * @param userId 조회할 사용자 ID
     * @return 조회된 사용자 객체, 존재하지 않으면 null
     * 
     * @apiNote 반환된 User 객체의 비밀번호는 해시된 상태임
     */
    public User getUserByUserId(String userId) {
        if (userId == null || userId.trim().isEmpty()) {
            logger.debug("사용자 조회 시도 - 유효하지 않은 ID: userId={}", userId);
            return null;
        }
        
        logger.debug("사용자 ID로 조회: userId={}", userId);
        User user = userDAO.findByUserId(userId);
        
        if (user != null) {
            logger.debug("사용자 조회 성공: userId={}, name={}", userId, user.getName());
        } else {
            logger.debug("사용자 조회 실패 - 존재하지 않음: userId={}", userId);
        }
        
        return user;
    }

    /**
     * 이메일로 사용자 정보를 조회하는 메서드
     * <p>
     * 주어진 이메일에 해당하는 사용자 객체를 반환한다.
     * 비밀번호 찾기, 계정 복구 등에 사용된다.
     * </p>
     * 
     * @param email 조회할 이메일 주소
     * @return 조회된 사용자 객체, 존재하지 않으면 null
     * 
     * @apiNote 반환된 User 객체의 비밀번호는 해시된 상태임
     */
    public User getUserByEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            logger.debug("사용자 조회 시도 - 유효하지 않은 이메일: email={}", email);
            return null;
        }
        
        logger.debug("이메일로 사용자 조회: email={}", email);
        User user = userDAO.findByEmail(email);
        
        if (user != null) {
            logger.debug("이메일로 사용자 조회 성공: email={}, userId={}", email, user.getUserId());
        } else {
            logger.debug("이메일로 사용자 조회 실패 - 존재하지 않음: email={}", email);
        }
        
        return user;
    }

    /**
     * 등록된 전체 사용자 수를 조회하는 메서드
     * <p>
     * 관리자 대시보드나 통계 목적으로 사용한다.
     * 시스템의 사용자 규모를 파악하는데 유용하다.
     * </p>
     * 
     * @return 등록된 사용자의 총 개수
     * 
     * @apiNote 관리자 권한이 필요한 기능으로 사용 시 권한 검증 필요
     */
    public int getUserCount() {
        int count = userDAO.count();
        logger.debug("전체 사용자 수 조회: count={}", count);
        return count;
    }


    
    /**
     * 사용자 저장소 상태를 디버깅하는 메서드
     * <p>
     * <strong>개발/테스트 전용</strong> - 현재 저장된 사용자 정보를 콘솔에 출력한다.
     * 데이터 저장 상태를 확인하거나 문제를 진단할 때 사용한다.
     * </p>
     * 
     * @deprecated 개발 목적으로만 사용 - 프로덕션에서는 제거 예정
     * @apiNote 민감한 정보가 포함될 수 있으므로 프로덕션에서는 사용 금지
     */
    @Deprecated
    public void debugUserStorage() {
        logger.warn("사용자 저장소 디버깅 메서드 호출 - 개발 전용 기능");
        
        System.out.println("=== 사용자 저장소 상태 ===");
        System.out.println("저장된 사용자 수: " + userDAO.count());
        
        // 모든 사용자 출력 (비밀번호 제외)
        userDAO.findAll().forEach(user -> {
            System.out.println(String.format("사용자: %s, 이메일: %s, 이름: %s", 
                user.getUserId(), user.getEmail(), user.getName()));
        });
        
        System.out.println("========================");
        
        logger.info("사용자 저장소 디버깅 완료: 총 {}명의 사용자 정보 출력", userDAO.count());
    }
}