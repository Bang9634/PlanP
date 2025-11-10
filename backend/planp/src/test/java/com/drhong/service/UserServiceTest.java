package com.drhong.service;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.drhong.dao.UserDAO;
import com.drhong.dto.SignupRequest;
import com.drhong.dto.SignupResponse;
import com.drhong.model.User;
import com.drhong.util.PasswordUtil;

/**
 * 테스트 시 참고사항
 * 
 * <p> 
 * 회원가입 로직 테스트시, 임의로 작성한 비밀번호 강도가 낮을 경우,
 * 회원가입에 실패하여 관련 테스트 실패 가능성 있음.
 * 테스트 시 임의 비밀번호 값의 강도가 너무 낮아 회원가입 실패가
 * 되지않도록 주의할 것
 * 
 * 추후 비밀번호 강도 관련 기능 삭제시, 수정 요함
 * </p>
 * 
 */
@SuppressWarnings("unused")
@DisplayName("UserService 통합 테스트")
class UserServiceTest {

    private UserService userService;
    private UserDAO userDAO;

    @BeforeEach
    void setUp() {
        userDAO = new UserDAO();
        userService = new UserService(userDAO);
        // 각 테스트 전에 데이터 정리
        userDAO.deleteAll();
    }

    @Nested
    @DisplayName("회원가입 테스트")
    class SignupTests {

        @Test
        @DisplayName("정상적인 회원가입 - 성공")
        void successfulSignup() {
            // Given
            SignupRequest request = new SignupRequest(
                "testuser", "password123", "테스트사용자", "test@example.com"
            );

            // When
            SignupResponse response = userService.signup(request);

            // Then
            assertThat(response.isSuccess()).isTrue();
            assertThat(response.getMessage()).isEqualTo("회원가입이 완료되었습니다.");
            assertThat(response.getUserId()).isEqualTo("testuser");

            // 실제로 저장되었는지 확인
            User savedUser = userService.getUserById("testuser");
            assertThat(savedUser).isNotNull();
            assertThat(savedUser.getName()).isEqualTo("테스트사용자");
            assertThat(savedUser.getEmail()).isEqualTo("test@example.com");
            
            // 비밀번호가 암호화되었는지 확인
            assertThat(PasswordUtil.verify("password123", savedUser.getPassword())).isTrue();
        }

        @Test
        @DisplayName("사용자 ID 중복 - 실패")
        void duplicateUserId() {
            // Given - 먼저 사용자 등록
            SignupRequest firstRequest = new SignupRequest(
                "duplicate", "password123", "첫번째사용자", "first@example.com"
            );
            userService.signup(firstRequest);

            // When - 같은 ID로 재등록 시도
            SignupRequest secondRequest = new SignupRequest(
                "duplicate", "password456", "두번째사용자", "second@example.com"
            );
            SignupResponse response = userService.signup(secondRequest);

            // Then
            assertThat(response.isSuccess()).isFalse();
            assertThat(response.getMessage()).isEqualTo("이미 사용중인 사용자 ID입니다.");
        }

        @Test
        @DisplayName("이메일 중복 - 실패")
        void duplicateEmail() {
            // Given - 먼저 사용자 등록
            SignupRequest firstRequest = new SignupRequest(
                "user1", "password123", "첫번째사용자", "duplicate@example.com"
            );
            userService.signup(firstRequest);

            // When - 같은 이메일로 재등록 시도
            SignupRequest secondRequest = new SignupRequest(
                "user2", "password456", "두번째사용자", "duplicate@example.com"
            );
            SignupResponse response = userService.signup(secondRequest);

            // Then
            assertThat(response.isSuccess()).isFalse();
            assertThat(response.getMessage()).isEqualTo("이미 사용중인 이메일입니다.");
        }

        @Test
        @DisplayName("잘못된 입력 데이터 - 실패")
        void invalidInputData() {
            // Given - 잘못된 요청 데이터
            SignupRequest request = new SignupRequest(
                "ab", "123", "", "invalid-email"
            );

            // When
            SignupResponse response = userService.signup(request);

            // Then
            assertThat(response.isSuccess()).isFalse();
            assertThat(response.getMessage()).contains("사용자 ID는 3-20자");
        }

        @Test
        @DisplayName("null 요청 데이터 - 실패")
        void nullRequest() {
            // When
            SignupResponse response = userService.signup(null);

            // Then
            assertThat(response.isSuccess()).isFalse();
            assertThat(response.getMessage()).isEqualTo("요청 데이터가 없습니다.");
        }
    }

    @Nested
    @DisplayName("중복 확인 테스트")
    class DuplicateCheckTests {

        @Test
        @DisplayName("사용 가능한 사용자 ID 확인")
        void availableUserId() {
            assertThat(userService.isUserIdAvailable("newuser")).isTrue();
        }

        @Test
        @DisplayName("사용 불가능한 사용자 ID 확인")
        void unavailableUserId() {
            // Given - 사용자 등록
            SignupRequest request = new SignupRequest(
                "existinguser", "password123", "기존사용자", "existing@example.com"
            );
            userService.signup(request);

            // When & Then
            assertThat(userService.isUserIdAvailable("existinguser")).isFalse();
        }

        @Test
        @DisplayName("사용 가능한 이메일 확인")
        void availableEmail() {
            assertThat(userService.isEmailAvailable("new@example.com")).isTrue();
        }

        @Test
        @DisplayName("사용 불가능한 이메일 확인")
        void unavailableEmail() {
            // Given - 사용자 등록
            SignupRequest request = new SignupRequest(
                "user", "password123", "사용자", "existing@example.com"
            );
            userService.signup(request);

            // When & Then
            assertThat(userService.isEmailAvailable("existing@example.com")).isFalse();
        }
    }

    @Nested
    @DisplayName("사용자 조회 테스트")
    class UserRetrievalTests {

        @Test
        @DisplayName("ID로 사용자 조회")
        void getUserById() {
            // Given - 사용자 등록
            SignupRequest request = new SignupRequest(
                "findme", "password123", "찾을사용자", "find@example.com"
            );
            userService.signup(request);

            // When
            User user = userService.getUserById("findme");

            // Then
            assertThat(user).isNotNull();
            assertThat(user.getUserId()).isEqualTo("findme");
            assertThat(user.getName()).isEqualTo("찾을사용자");
        }

        @Test
        @DisplayName("존재하지 않는 ID로 조회")
        void getUserByNonExistentId() {
            User user = userService.getUserById("nonexistent");
            assertThat(user).isNull();
        }

        @Test
        @DisplayName("이메일로 사용자 조회")
        void getUserByEmail() {
            // Given - 사용자 등록
            SignupRequest request = new SignupRequest(
                "emailuser", "password123", "이메일사용자", "email@example.com"
            );
            userService.signup(request);

            // When
            User user = userService.getUserByEmail("email@example.com");

            // Then
            assertThat(user).isNotNull();
            assertThat(user.getEmail()).isEqualTo("email@example.com");
            assertThat(user.getName()).isEqualTo("이메일사용자");
        }
    }

    @Nested
    @DisplayName("다중 사용자 시나리오 테스트")
    class MultipleUserTests {

        @Test
        @DisplayName("여러 사용자 연속 회원가입")
        void multipleSignups() {
            // Given - 여러 회원가입 요청
            SignupRequest[] requests = {
                new SignupRequest("user1", "pass123123123", "사용자1", "user1@example.com"),
                new SignupRequest("user2", "pass2123123123", "사용자2", "user2@example.com"),
                new SignupRequest("user3", "pass3123123123", "사용자3", "user3@example.com")
            };

            // When & Then - 모든 회원가입이 성공해야 함
            for (SignupRequest request : requests) {
                SignupResponse response = userService.signup(request);
                assertThat(response.isSuccess()).isTrue();
            }

            // 전체 사용자 수 확인
            assertThat(userService.getUserCount()).isEqualTo(3);
        }

        @Test
        @DisplayName("회원가입 후 중복 확인")
        void signupThenDuplicateCheck() {
            // Given - 회원가입
            SignupRequest request = new SignupRequest(
                "testuser", "password123123", "테스트", "test@example.com"
            );
            userService.signup(request);

            // When & Then - 중복 확인
            assertThat(userService.isUserIdAvailable("testuser")).isFalse();
            assertThat(userService.isEmailAvailable("test@example.com")).isFalse();
            
            // 다른 ID/이메일은 사용 가능해야 함
            assertThat(userService.isUserIdAvailable("anotheruser")).isTrue();
            assertThat(userService.isEmailAvailable("another@example.com")).isTrue();
        }

        @Test
        @DisplayName("회원가입 후 중복 확인 - 상세 검증")
        void signupThenDuplicateCheckDetailed() {
            // Given - 회원가입
            SignupRequest request = new SignupRequest(
                "testuser", "password123123", "테스트", "test@example.com"
            );

            // When - 회원가입 실행
            SignupResponse response = userService.signup(request);

            // Then - 단계별 검증
            System.out.println("1. 회원가입 응답: " + response.toJson());
            assertThat(response.isSuccess())
                .withFailMessage("회원가입이 실패했습니다: " + response.getMessage())
                .isTrue();

            System.out.println("2. 저장된 사용자 확인");
            User savedUser = userService.getUserById("testuser");
            assertThat(savedUser)
                .withFailMessage("사용자가 저장되지 않았습니다")
                .isNotNull();

            System.out.println("3. 사용자 정보: " + savedUser);

            System.out.println("4. 중복 확인 테스트");
            
            // ID 중복 확인
            boolean userIdExists = userDAO.existsByUserId("testuser");
            boolean userIdAvailable = userService.isUserIdAvailable("testuser");
            System.out.println("  - userDAO.existsByUserId('testuser'): " + userIdExists);
            System.out.println("  - userService.isUserIdAvailable('testuser'): " + userIdAvailable);
            
            // 이메일 중복 확인  
            boolean emailExists = userDAO.existsByEmail("test@example.com");
            boolean emailAvailable = userService.isEmailAvailable("test@example.com");
            System.out.println("  - userDAO.existsByEmail('test@example.com'): " + emailExists);
            System.out.println("  - userService.isEmailAvailable('test@example.com'): " + emailAvailable);

            // 최종 검증
            assertThat(userIdExists).isTrue();
            assertThat(userIdAvailable).isFalse();
            assertThat(emailExists).isTrue(); 
            assertThat(emailAvailable).isFalse();
        }
    }
}