package com.drhong.controller;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.drhong.dto.ApiResponse;
import com.drhong.dto.LoginRequest;
import com.drhong.dto.SignupRequest;
import com.drhong.model.User;
import com.drhong.service.UserService;

/**
 * 사용자 관련 HTTP 요청 처리 컨트롤러
 * <p>
 * 사용자 인증 및 관리와 관련된 모든 HTTP 엔드포인트를 담당한다.
 * 회원가입, 로그인, 중복 확인 등의 기능을 제공한다.
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
    
    /**
     * UserController 객체를 생성하는 생성자
     * 
     * @param userService 사용자 관련 비즈니스 로직을 처리하는 서비스 객체
     */
    public UserController(UserService userService) {
        this.userService = userService;
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

            Map<String, Object> data = new HashMap<>();
            data.put("userId", user.get().getUserId());

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
            Map<String, Object> data = new HashMap<>();
            data.put("userId", user.get().getUserId());
            data.put("name", user.get().getName());
            
            return ApiResponse.success("로그인 성공", data);
        } catch (RuntimeException e) {
            return ApiResponse.fail(e.getMessage());
        }

    }
}