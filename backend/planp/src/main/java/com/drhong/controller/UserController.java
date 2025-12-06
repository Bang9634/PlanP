package com.drhong.controller;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.drhong.dto.ApiResponse;
import com.drhong.dto.GoogleLoginRequest;
import com.drhong.dto.GoogleUser;
import com.drhong.dto.LoginRequest;
import com.drhong.dto.LogoutRequest;
import com.drhong.dto.SignupRequest;
import com.drhong.model.User;
import com.drhong.service.GoogleOAuthService;
import com.drhong.service.JwtService;
import com.drhong.service.UserService;

/**
 * 사용자 관련 HTTP 요청 처리 컨트롤러
 * <p>
 * 사용자 인증 및 관리와 관련된 모든 HTTP 엔드포인트를 담당한다.
 * 회원가입, 로그인, Google OAuth 로그인, 중복 확인 등의 기능을 제공한다.
 * </p>
 * 
 * @author bang9634
 * @since 2025-11-10
 */
public class UserController {

    /** SLF4J Logger 인스턴스 - 요청 처리 로그를 기록 */
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);
    
    /** 사용자 비즈니스 로직 처리 서비스 */
    private final UserService userService;
    private final JwtService jwtService;
    
    /** Google OAuth 처리 서비스 */
    private final GoogleOAuthService googleOAuthService;
    
    /**
     * UserController 객체를 생성하는 생성자
     * 
     * @param userService 사용자 관련 비즈니스 로직을 처리하는 서비스 객체
     * @param googleOAuthService Google OAuth 처리 서비스 객체
     */
    public UserController(UserService userService, JwtService jwtService,GoogleOAuthService googleOAuthService) {
        this.userService = userService;
        this.jwtService = jwtService;
        this.googleOAuthService = googleOAuthService;
    }
    
    public ApiResponse<?> signup(SignupRequest request) {
        logger.debug("회원가입 시작");
        try {
            Optional<User> user = userService.signup(request);

            // 향후 추가될 기능들:
            // if (response.isSuccess()) {
            //     emailService.sendWelcomeEmail(request.getEmail());
            //     notificationService.notifyAdmins(request.getUserId());
            // }

            String accessToken = jwtService.generateAccessToken(user.get());
            String refreshToken = jwtService.generateRefreshToken(user.get().getUserId());

            Map<String, Object> data = new HashMap<>();
            data.put("userId", user.get().getUserId());
            data.put("name", user.get().getName());
            data.put("email", user.get().getEmail());
            data.put("isGoogleAccount", user.get().isGoogleAccount());
            data.put("accessToken", accessToken);
            data.put("refreshToken", refreshToken);

            return ApiResponse.success("회원가입이 완료되었습니다.", data);
        } catch (RuntimeException e) {
            logger.warn("회원가입 실패: {}", e.getMessage());
            return ApiResponse.fail(e.getMessage());
        } catch (Exception e) {
            logger.warn("예상치 못한 예외 발생", e);
            return ApiResponse.fail("예상치 못한 오류 발생");
        }
    }
    
    /**
     * 로그인 인증 요청을 처리하는 메서드
     * <p>
     * 사용자가 제공한 ID와 비밀번호를 검증하여 인증을 수행한다.
     * 성공 시 사용자 정보를 반환하고, 실패 시 적절한 오류 메시지를 제공한다.
     * </p>
     * @param exchange HTTP 요청/응답 처리를 위한 교환 객체
     * @throws IOException 네트워크 I/O 처리 중 오류가 발생한 경우
     * @author wnwoghd
     * @apiNote LoginRequest DTO를 사용하여 타입 안정성을 보장함
     */
    public ApiResponse<?> login(LoginRequest request)  {
        logger.debug("로그인 시작");
        try {
            Optional<User> user = userService.login(request.getUserId(), request.getPassword());
                    
            if (user.isEmpty()) {
                return ApiResponse.fail("로그인 실패");
            }
            String accessToken = jwtService.generateAccessToken(user.get());
            String refreshToken = jwtService.generateRefreshToken(user.get().getUserId());

            Map<String, Object> data = new HashMap<>();
            data.put("userId", user.get().getUserId());
            data.put("name", user.get().getName());
            data.put("email", user.get().getEmail());
            data.put("isGoogleAccount", user.get().isGoogleAccount());
            data.put("accessToken", accessToken);
            data.put("refreshToken", refreshToken);
            
            return ApiResponse.success("로그인 성공", data);
        } catch (RuntimeException e) {
            return ApiResponse.fail(e.getMessage());
        }
    }

    public ApiResponse<?> logout(LogoutRequest request) {
        logger.debug("로그아웃 시작: userId={}", request.getUserId());
        try {
            Optional<User> user = userService.logout(request.getUserId());
            if (user.isEmpty()) {
                return ApiResponse.fail("사용자가 존재하지 않습니다.");
            }
   
            return ApiResponse.success("로그아웃 성공");
        } catch (RuntimeException e) {
            return ApiResponse.fail(e.getMessage());
        }
    }
    
    /**
     * Google OAuth 로그인 요청을 처리하는 메서드
     * <p>
     * 프론트엔드에서 받은 Google Access Token으로 OAuth 로그인을 처리한다.
     * 입력 검증은 서비스 계층에 위임하여 중복을 제거한다.
     * </p>
     * 
     * <h3>플로우</h3>
     * <ol>
     *   <li>GoogleOAuthService: 토큰 검증 + 사용자 정보 조회</li>
     *   <li>UserService: 로그인/회원가입 비즈니스 로직</li>
     *   <li>HTTP 응답 변환</li>
     * </ol>
     * 
     * @param request Google 로그인 요청 (accessToken 포함)
     * @return 로그인 성공 시 사용자 정보, 실패 시 오류 메시지
     */
    public ApiResponse<?> googleLogin(GoogleLoginRequest request) {
        logger.info("Google OAuth 로그인 요청 시작");
        
        try {
            // 요청 및 토큰 유효성 검증
            if (request == null) {
                logger.warn("Google 로그인 요청 객체가 null입니다");
                return ApiResponse.fail("잘못된 요청입니다");
            }
            
            if (request.getAccessToken() == null || request.getAccessToken().trim().isEmpty()) {
                logger.warn("Google Access Token이 비어있습니다");
                return ApiResponse.fail("Google Access Token이 필요합니다");
            }
            
            // 1. Google Access Token 검증 및 사용자 정보 조회 (서비스에 위임)
            GoogleUser googleUser = googleOAuthService.verifyToken(request.getAccessToken());
            if (googleUser == null) {
                logger.warn("Google 토큰 검증 실패");
                return ApiResponse.fail("Google 인증에 실패했습니다. 다시 시도해주세요.");
            }
            
            logger.debug("Google 사용자 정보 조회 성공: email={}", googleUser.getEmail());
            
            // 2. UserService를 통한 비즈니스 로직 처리 (로그인/회원가입)
            User user = userService.googleLogin(
                googleUser.getId(),
                googleUser.getEmail(), 
                googleUser.getName()
            );
            
            // 3. JWT 토큰 생성
            String accessToken = jwtService.generateAccessToken(user);
            String refreshToken = jwtService.generateRefreshToken(user.getUserId());
            
            // 4. 성공 응답 생성
            Map<String, Object> data = new HashMap<>();
            data.put("userId", user.getUserId());
            data.put("email", user.getEmail());
            data.put("name", user.getName());
            data.put("isGoogleAccount", user.isGoogleAccount());
            data.put("loginType", "google");
            data.put("accessToken", accessToken);
            data.put("refreshToken", refreshToken);
            
            logger.info("Google OAuth 처리 성공: email={}", user.getEmail());
            return ApiResponse.success("인증 성공", data);
            
        } catch (IllegalArgumentException e) {
            logger.warn("Google OAuth 요청 파라미터 오류: {}", e.getMessage());
            return ApiResponse.fail(e.getMessage());
            
        } catch (RuntimeException e) {
            // 이메일 중복 등의 비즈니스 로직 오류
            logger.warn("Google OAuth 비즈니스 로직 오류: {}", e.getMessage());
            return ApiResponse.fail(e.getMessage());
            
        } catch (Exception e) {
            logger.error("Google OAuth 처리 중 예상치 못한 오류", e);
            return ApiResponse.fail("로그인 처리 중 오류가 발생했습니다. 잠시 후 다시 시도해주세요.");
        }
    }
    
    /**
     * 리프레시 토큰을 사용하여 새로운 액세스 토큰을 발급받는 API
     * 
     * @param refreshToken 리프레시 토큰
     * @return 새로운 액세스 토큰 또는 오류 메시지
     */
    public ApiResponse<?> refreshToken(String refreshToken) {
        logger.debug("토큰 갱신 요청");
        
        try {
            if (refreshToken == null || refreshToken.trim().isEmpty()) {
                return ApiResponse.fail("리프레시 토큰이 필요합니다");
            }
            
            // 리프레시 토큰 검증
            Optional<User> user = jwtService.validateToken(refreshToken);
            
            if (user.isEmpty()) {
                logger.warn("유효하지 않은 리프레시 토큰");
                return ApiResponse.fail("유효하지 않은 리프레시 토큰입니다");
            }
            
            // 새로운 액세스 토큰과 리프레시 토큰 생성
            String newAccessToken = jwtService.generateAccessToken(user.get());
            String newRefreshToken = jwtService.generateRefreshToken(user.get().getUserId());
            
            Map<String, Object> data = new HashMap<>();
            data.put("accessToken", newAccessToken);
            data.put("refreshToken", newRefreshToken);
            
            logger.info("토큰 갱신 성공: userId={}", user.get().getUserId());
            return ApiResponse.success("토큰 갱신 성공", data);
            
        } catch (Exception e) {
            logger.error("토큰 갱신 중 오류", e);
            return ApiResponse.fail("토큰 갱신에 실패했습니다");
        }
    }
    
    /**
     * 사용자 정보 조회 API (엔드포인트: /api/users/get-info)
     * <p>
     * accessToken 기반으로 현재 로그인한 사용자 정보를 반환한다.
     * </p>
     * @param accessToken Authorization 헤더에서 추출
     * @return 사용자 정보 API 응답
     */
    public ApiResponse<?> getUserInfoByToken(String accessToken) {
        logger.debug("사용자 정보 조회 API 호출: accessToken={}", accessToken != null ? accessToken.substring(0, 10) + "..." : null);
        try {
            if (accessToken == null || accessToken.trim().isEmpty()) {
                return ApiResponse.fail("액세스 토큰이 필요합니다");
            }
            // 토큰에서 사용자 정보 추출
            java.util.Optional<com.drhong.model.User> user = jwtService.validateToken(accessToken);
            if (user.isEmpty()) {
                return ApiResponse.fail("유효하지 않은 토큰입니다");
            }
            java.util.Map<String, Object> userInfo = userService.getUserPublicInfo(user.get().getUserId());
            logger.info("사용자 정보 조회 성공: userId={}", user.get().getUserId());
            return ApiResponse.success("사용자 정보 조회 성공", userInfo);
        } catch (Exception e) {
            logger.error("사용자 정보 조회 중 오류", e);
            return ApiResponse.fail("사용자 정보 조회 중 오류가 발생했습니다");
        }
    }
}