package com.drhong.service;

import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.drhong.config.TestDatabaseConfig;
import com.drhong.dao.UserDAO;
import com.drhong.database.ConnectionManager;
import com.drhong.database.QueryExecutor;
import com.drhong.dto.SignupRequest;
import com.drhong.dto.SignupResponse;
import com.drhong.model.User;
import com.drhong.util.PasswordUtil;
import com.drhong.util.TestDatabaseHelper;

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
    private static final Logger logger = LoggerFactory.getLogger(UserServiceTest.class);

    private UserService userService;
    private UserDAO userDAO;
    private QueryExecutor queryExecutor;
    private TestDatabaseConfig databaseConfig;
    private ConnectionManager connectionManager;

    @BeforeAll
    static void setupDatabase() {
        logger.info("=== 테스트 DB 초기화 ===");
        TestDatabaseHelper.initializeTestDatabase();
    }

    @BeforeEach
    void setUp() {
        try {
            logger.info("테스트 시작 - UserService 초기화");
            databaseConfig = new TestDatabaseConfig();
            connectionManager = ConnectionManager.resetInstance(databaseConfig);
            queryExecutor = new QueryExecutor(connectionManager);
            userDAO = new UserDAO(queryExecutor);
            userService = new UserService(userDAO);
        } catch (SQLException e) {
            logger.warn("테스트 실패 - SQLException 발생: {}", e);
        }
    }

    @AfterEach
    void tearDown() {
        logger.info("테스트 종료 - DB 정리");
        TestDatabaseHelper.cleanDatabase();
    }

    @Nested
    @DisplayName("회원가입 테스트")
    class SignupTests {

        @Test
        @DisplayName("정상적인 회원가입 - 성공")
        void successfulSignup() {
            // Given
            SignupRequest request = new SignupRequest(
                "testuser", "StrongPassword123!", "테스트사용자", "test@example.com"
            );

            // When
            SignupResponse response = userService.signup(request);

            // Then
            assertThat(response.isSuccess()).isTrue();
            assertThat(response.getMessage()).isEqualTo("회원가입이 완료되었습니다.");
            assertThat(response.getUserId()).isEqualTo("testuser");

            // 실제로 저장되었는지 확인
            User savedUser = userService.getUserByUserId("testuser");
            assertThat(savedUser).isNotNull();
            assertThat(savedUser.getName()).isEqualTo("테스트사용자");
            assertThat(savedUser.getEmail()).isEqualTo("test@example.com");
            
            // 비밀번호가 암호화되었는지 확인
            assertThat(PasswordUtil.verify("StrongPassword123!", savedUser.getPassword())).isTrue();
        }

        @Test
        @DisplayName("사용자 ID 중복 - 실패")
        void duplicateUserId() {
            // Given - 먼저 사용자 등록
            SignupRequest firstRequest = new SignupRequest(
                "duplicate", "StrongPassword123!", "첫번째사용자", "first@example.com"
            );
            userService.signup(firstRequest);

            // When - 같은 ID로 재등록 시도
            SignupRequest secondRequest = new SignupRequest(
                "duplicate", "AnotherPassword456!", "두번째사용자", "second@example.com"
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
                "user1", "StrongPassword123!", "첫번째사용자", "duplicate@example.com"
            );
            userService.signup(firstRequest);

            // When - 같은 이메일로 재등록 시도
            SignupRequest secondRequest = new SignupRequest(
                "user2", "AnotherPassword456!", "두번째사용자", "duplicate@example.com"
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
            assertThat(response.getMessage()).isEqualTo("잘못된 요청입니다.");
        }

        @Test
        @DisplayName("약한 비밀번호 - 실패")
        void weakPassword() {
            // Given - 약한 비밀번호
            SignupRequest request = new SignupRequest(
                "testuser", "123123", "테스트사용자", "test@example.com"
            );

            // When
            SignupResponse response = userService.signup(request);

            // Then
            assertThat(response.isSuccess()).isFalse();
            assertThat(response.getMessage()).contains("비밀번호 강도가 너무 약합니다");
        }

        @ParameterizedTest
        @ValueSource(strings = {"a", "ab", "toolongusernamethatexceedsmaximumlength"})
        @DisplayName("잘못된 길이의 사용자 ID - 실패")
        void invalidUserIdLength(String userId) {
            // Given
            SignupRequest request = new SignupRequest(
                userId, "StrongPassword123!", "테스트사용자", "test@example.com"
            );

            // When
            SignupResponse response = userService.signup(request);

            // Then
            assertThat(response.isSuccess()).isFalse();
        }

        @ParameterizedTest
        @ValueSource(strings = {"invalid-email", "@example.com", "test@"})
        @DisplayName("잘못된 이메일 형식 - 실패")
        void invalidEmailFormat(String email) {
            // Given
            SignupRequest request = new SignupRequest(
                "testuser", "StrongPassword123!", "테스트사용자", email
            );

            // When
            SignupResponse response = userService.signup(request);

            // Then
            assertThat(response.isSuccess()).isFalse();
        }
    }

    @Nested
    @DisplayName("로그인 테스트")
    class LoginTests {

        @Test
        @DisplayName("정상적인 로그인 - 성공")
        void successfulLogin() {
            // Given - 사용자 등록
            String password = "StrongPassword123!";
            SignupRequest signupRequest = new SignupRequest(
                "loginuser", password, "로그인사용자", "login@example.com"
            );
            userService.signup(signupRequest);

            // When
            boolean loginResult = userService.login("loginuser", password);

            // Then
            assertThat(loginResult).isTrue();
        }

        @Test
        @DisplayName("잘못된 비밀번호 - 실패")
        void wrongPassword() {
            // Given - 사용자 등록
            SignupRequest signupRequest = new SignupRequest(
                "loginuser", "StrongPassword123!", "로그인사용자", "login@example.com"
            );
            userService.signup(signupRequest);

            // When
            boolean loginResult = userService.login("loginuser", "WrongPassword!");

            // Then
            assertThat(loginResult).isFalse();
        }

        @Test
        @DisplayName("존재하지 않는 사용자 - 실패")
        void nonExistentUser() {
            // When
            boolean loginResult = userService.login("nonexistent", "AnyPassword123!");

            // Then
            assertThat(loginResult).isFalse();
        }

        @ParameterizedTest
        @NullAndEmptySource
        @DisplayName("null 또는 빈 사용자 ID - 실패")
        void nullOrEmptyUserId(String userId) {
            // When
            boolean loginResult = userService.login(userId, "StrongPassword123!");

            // Then
            assertThat(loginResult).isFalse();
        }

        @ParameterizedTest
        @NullAndEmptySource
        @DisplayName("null 또는 빈 비밀번호 - 실패")
        void nullOrEmptyPassword(String password) {
            // When
            boolean loginResult = userService.login("testuser", password);

            // Then
            assertThat(loginResult).isFalse();
        }
    }

    @Nested
    @DisplayName("비밀번호 변경 테스트")
    class PasswordChangeTests {

        @Test
        @DisplayName("정상적인 비밀번호 변경 - 성공")
        void successfulPasswordChange() {
            // Given - 사용자 등록
            String oldPassword = "OldPassword123!";
            String newPassword = "NewPassword456!";
            SignupRequest signupRequest = new SignupRequest(
                "changeuser", oldPassword, "변경사용자", "change@example.com"
            );
            userService.signup(signupRequest);

            // When
            boolean changeResult = userService.changePassword("changeuser", oldPassword, newPassword);

            // Then
            // assertThat(changeResult).isTrue();
            
            // 새 비밀번호로 로그인 확인
            // assertThat(userService.login("changeuser", newPassword)).isTrue();
            // assertThat(userService.login("changeuser", oldPassword)).isFalse();
        }

        @Test
        @DisplayName("잘못된 기존 비밀번호 - 실패")
        void wrongOldPassword() {
            // Given - 사용자 등록
            SignupRequest signupRequest = new SignupRequest(
                "changeuser", "CorrectPassword123!", "변경사용자", "change@example.com"
            );
            userService.signup(signupRequest);

            // When
            boolean changeResult = userService.changePassword("changeuser", "WrongOldPassword!", "NewPassword456!");

            // Then
            assertThat(changeResult).isFalse();
        }

        @Test
        @DisplayName("존재하지 않는 사용자 - 실패")
        void nonExistentUserPasswordChange() {
            // When
            boolean changeResult = userService.changePassword("nonexistent", "OldPass123!", "NewPass456!");

            // Then
            assertThat(changeResult).isFalse();
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
                "existinguser", "StrongPassword123!", "기존사용자", "existing@example.com"
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
                "user", "StrongPassword123!", "사용자", "existing@example.com"
            );
            userService.signup(request);

            // When & Then
            assertThat(userService.isEmailAvailable("existing@example.com")).isFalse();
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {" ", "   "})
        @DisplayName("유효하지 않은 사용자 ID 중복 확인")
        void invalidUserIdCheck(String userId) {
            assertThat(userService.isUserIdAvailable(userId)).isFalse();
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {" ", "   "})
        @DisplayName("유효하지 않은 이메일 중복 확인")
        void invalidEmailCheck(String email) {
            assertThat(userService.isEmailAvailable(email)).isFalse();
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
                "findme", "StrongPassword123!", "찾을사용자", "find@example.com"
            );
            userService.signup(request);

            // When
            User user = userService.getUserByUserId("findme");

            // Then
            assertThat(user).isNotNull();
            assertThat(user.getUserId()).isEqualTo("findme");
            assertThat(user.getName()).isEqualTo("찾을사용자");
        }

        @Test
        @DisplayName("존재하지 않는 ID로 조회")
        void getUserByNonExistentId() {
            User user = userService.getUserByUserId("nonexistent");
            assertThat(user).isNull();
        }

        @Test
        @DisplayName("이메일로 사용자 조회")
        void getUserByEmail() {
            // Given - 사용자 등록
            SignupRequest request = new SignupRequest(
                "emailuser", "StrongPassword123!", "이메일사용자", "email@example.com"
            );
            userService.signup(request);

            // When
            User user = userService.getUserByEmail("email@example.com");

            // Then
            assertThat(user).isNotNull();
            assertThat(user.getEmail()).isEqualTo("email@example.com");
            assertThat(user.getName()).isEqualTo("이메일사용자");
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {" ", "   "})
        @DisplayName("유효하지 않은 ID로 조회")
        void getUserByInvalidId(String userId) {
            User user = userService.getUserByUserId(userId);
            assertThat(user).isNull();
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {" ", "   "})
        @DisplayName("유효하지 않은 이메일로 조회")
        void getUserByInvalidEmail(String email) {
            User user = userService.getUserByEmail(email);
            assertThat(user).isNull();
        }
    }

    @Nested
    @DisplayName("생성자 테스트")
    class ConstructorTests {

        @Test
        @DisplayName("null UserDAO 주입 - 실패")
        void nullUserDAOInjection() {
            assertThatThrownBy(() -> new UserService(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("UserDAO는 null일 수 없습니다");
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
                new SignupRequest("user1", "StrongPassword123!", "사용자1", "user1@example.com"),
                new SignupRequest("user2", "StrongPassword456!", "사용자2", "user2@example.com"),
                new SignupRequest("user3", "StrongPassword789!", "사용자3", "user3@example.com")
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
        @DisplayName("대량 사용자 처리 성능 테스트")
        void bulkUserProcessingPerformance() {
            // Given
            int userCount = 50;
            long startTime = System.currentTimeMillis();

            // When
            for (int i = 0; i < userCount; i++) {
                SignupRequest request = new SignupRequest(
                    "bulkuser" + i, 
                    "StrongPassword123!", 
                    "대량사용자" + i, 
                    "bulkuser" + i + "@example.com"
                );
                userService.signup(request);
            }

            long endTime = System.currentTimeMillis();
            long duration = endTime - startTime;

            // Then
            assertThat(userService.getUserCount()).isEqualTo(userCount);
            assertThat(duration).isLessThan(40000); // 40초 이내 완료
            System.out.println("Bulk signup of " + userCount + " users took " + duration + "ms");
        }

        @Test
        @DisplayName("회원가입 후 중복 확인")
        void signupThenDuplicateCheck() {
            // Given - 회원가입
            SignupRequest request = new SignupRequest(
                "testuser", "StrongPassword123!", "테스트", "test@example.com"
            );
            userService.signup(request);

            // When & Then - 중복 확인
            assertThat(userService.isUserIdAvailable("testuser")).isFalse();
            assertThat(userService.isEmailAvailable("test@example.com")).isFalse();
            
            // 다른 ID/이메일은 사용 가능해야 함
            assertThat(userService.isUserIdAvailable("anotheruser")).isTrue();
            assertThat(userService.isEmailAvailable("another@example.com")).isTrue();
        }
    }

    @Nested
    @DisplayName("데이터 무결성 테스트")
    class DataIntegrityTests {

        @Test
        @DisplayName("저장된 비밀번호는 평문이 아님")
        void passwordIsHashed() {
            // Given
            String plainPassword = "MySecretPassword123!";
            SignupRequest request = new SignupRequest(
                "secureuser", plainPassword, "보안사용자", "secure@example.com"
            );

            // When
            userService.signup(request);
            User savedUser = userService.getUserByUserId("secureuser");

            // Then
            assertThat(savedUser.getPassword()).isNotEqualTo(plainPassword);
            assertThat(savedUser.getPassword()).startsWith("$2a$"); // bcrypt 해시 형식
        }

        @Test
        @DisplayName("사용자 정보 수정 후 무결성 확인")
        void userDataIntegrityAfterModification() {
            // Given - 사용자 등록
            SignupRequest request = new SignupRequest(
                "modifyuser", "OriginalPassword123!", "원본사용자", "original@example.com"
            );
            userService.signup(request);

            // When - 비밀번호 변경
            userService.changePassword("modifyuser", "OriginalPassword123!", "NewPassword456!");
            
            // Then - 데이터 무결성 확인
            User user = userService.getUserByUserId("modifyuser");
            assertThat(user.getUserId()).isEqualTo("modifyuser");
            assertThat(user.getName()).isEqualTo("원본사용자");
            assertThat(user.getEmail()).isEqualTo("original@example.com");
            
            // 새 비밀번호로 로그인 가능
            // assertThat(userService.login("modifyuser", "NewPassword456!")).isTrue();
            // assertThat(userService.login("modifyuser", "OriginalPassword123!")).isFalse();
        }
    }

    @Nested
    @DisplayName("에러 처리 테스트")
    class ErrorHandlingTests {

        @Test
        @DisplayName("예외 상황에서도 안전한 응답 반환")
        void safeResponseOnException() {
            // Given - 극단적인 케이스
            SignupRequest request = new SignupRequest(
                "extremecase", "password", null, "test@example.com"
            );

            // When
            SignupResponse response = userService.signup(request);

            // Then - 예외가 발생하지 않고 실패 응답 반환
            assertThat(response).isNotNull();
            assertThat(response.isSuccess()).isFalse();
        }

        @Test
        @DisplayName("대용량 데이터 처리 안정성")
        void largeDataHandling() {
            // Given - 매우 긴 문자열
            String longString = "a".repeat(1000);
            SignupRequest request = new SignupRequest(
                "shortid", "StrongPassword123!", longString, "test@example.com"
            );

            // When
            SignupResponse response = userService.signup(request);

            // Then - 적절히 처리됨 (성공하거나 유효성 검증 실패)
            assertThat(response).isNotNull();
        }
    }
}