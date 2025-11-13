package com.drhong.dao;

import java.sql.Connection;        // DB 연결 객체
import java.sql.DriverManager;     // DB 연결 관리
import java.sql.PreparedStatement; // SQL 쿼리를 실행하고 매개변수를 설정하는 객체
import java.sql.ResultSet;         // SELECT 쿼리 결과를 받는 객체
import java.sql.SQLException;      // DB 관련 예외 처리

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.drhong.model.User;

/**
 * 사용자 데이터 접근 객체 (Data Access Object)
 * <p>
 * 사용자 정보의 저장, 조회, 수정, 삭제 등 데이터베이스 관련 작업을 담당하는 DAO 클래스이다.
 * MySQL 데이터베이스와 연동하여 JDBC를 통해 데이터를 관리한다.
 * 데이터베이스 연결 정보는 환경변수를 통해 동적으로 로드되므로
 * 개발, 테스트, 운영 환경에 맞게 유연하게 설정할 수 있다.
 * </p>
 *
 * <h3>주요 기능:</h3>
 * <ul>
 *   <li>사용자 정보 저장 및 조회 (DB 연동)</li>
 *   <li>사용자 ID 및 이메일 기반 검색</li>
 *   <li>중복 확인 (ID, 이메일)</li>
 *   <li>사용자 데이터 관리 (삭제, 전체 조회)</li>
 * </ul>
 *
 * <h3>환경변수 설정:</h3>
 * <pre>{@code
 * export DB_HOST="49.50.133.229"
 * export DB_PORT="3306"
 * export DB_NAME="planp_db"
 * export DB_USER="planp_user"
 * export DB_PASSWORD="your_password"
 * 
 * // 또는 Java 시스템 속성
 * java -Ddb.host=49.50.133.229 -Ddb.port=3306 -Ddb.name=planp_db App
 * }</pre>
 *
 * @author bang9634
 * @since 2025-11-10
 * @implNote MySQL 데이터베이스 연동 - 환경변수를 통해 연결 정보 관리
 * 
 * @see com.drhong.util.ConfigUtil
 */
public class UserDAO {

    /** SLF4J Logger 인스턴스 - 데이터 접근 로그 기록 */
    private static final Logger logger = LoggerFactory.getLogger(UserDAO.class);
    
    /** 데이터베이스 연결 URL - 환경변수 기반으로 동적 생성 */
    private static final String DB_URL = buildDbUrl();
    
    /**
     * Static 초기화 블록 - JDBC 드라이버 로드
     * <p>
     * UserDAO 클래스가 로드될 때 MySQL JDBC 드라이버를 명시적으로 로드한다.
     * 현대 JDBC 드라이버(MySQL Connector/J 8.0+)는 자동 로드를 지원하지만,
     * 명시적 로드를 통해 드라이버 로드 실패 시 오류를 명확하게 감지할 수 있다.
     * </p>
     */
    static {
        try {
            // MySQL JDBC 드라이버 명시적 로드
            // Class.forName()을 통해 com.mysql.cj.jdbc.Driver 클래스를 로드하면
            // 드라이버가 자동으로 DriverManager에 등록된다.
            Class.forName("com.mysql.cj.jdbc.Driver");
            logger.info("MySQL JDBC 드라이버 로드 성공");
        } catch (ClassNotFoundException e) {
            logger.error("MySQL JDBC 드라이버 로드 실패 - classpath에 mysql-connector-java JAR이 있는지 확인하세요", e);
            throw new ExceptionInInitializerError(
                "MySQL JDBC 드라이버 로드 실패: " + e.getMessage()
            );
        }
    }

    /**
     * 데이터베이스 URL을 환경변수 기반으로 동적으로 생성하는 메서드
     * <p>
     * 환경변수 또는 Java 시스템 속성에서 호스트, 포트, DB명을 로드하여
     * JDBC 연결 문자열을 구성한다. 모든 환경변수가 설정되지 않았을 경우
     * 기본값을 사용한다.
     * </p>
     * 
     * @return JDBC MySQL 연결 URL 문자열
     */
    private static String buildDbUrl() {
        // 환경변수에서 DB 연결 정보 로드 (또는 기본값 사용)
        String dbHost = getEnvOrProperty("DB_HOST", "db.host", "49.50.133.229");
        String dbPort = getEnvOrProperty("DB_PORT", "db.port", "3306");
        String dbName = getEnvOrProperty("DB_NAME", "db.name", "planp_db");
        
        // JDBC URL 구성, 운영 환경 배포 시에만 useSSL=true&requireSSL=true로 변경
        String url = String.format(
            "jdbc:mysql://%s:%s/%s?useSSL=false&serverTimezone=UTC&characterEncoding=UTF-8",
            dbHost, dbPort, dbName
        );
        
        logger.info("데이터베이스 URL 생성 완료: {}", url);
        return url;
    }
    
    /**
     * 환경변수 또는 Java 시스템 속성에서 값을 로드하는 헬퍼 메서드
     * <p>
     * 우선순위:
     * 1. 시스템 환경변수 (System.getenv)
     * 2. Java 시스템 속성 (System.getProperty)
     * 3. 기본값 (defaultValue)
     * </p>
     * 
     * @author wnwoghd
     * 
     * @param envName 환경변수명
     * @param propName Java 시스템 속성명
     * @param defaultValue 기본값
     * @return 로드된 값 또는 기본값
     */
    private static String getEnvOrProperty(String envName, String propName, String defaultValue) {
        // 1. 환경변수에서 먼저 확인
        String value = System.getenv(envName);
        if (value != null && !value.trim().isEmpty()) {
            logger.debug("환경변수에서 로드: {}={}", envName, value);
            return value;
        }
        
        // 2. Java 시스템 속성에서 확인
        value = System.getProperty(propName);
        if (value != null && !value.trim().isEmpty()) {
            logger.debug("시스템 속성에서 로드: {}={}", propName, value);
            return value;
        }
        
        // 3. 기본값 사용
        logger.warn("환경변수와 시스템 속성을 찾을 수 없음: env={}, prop={}, 기본값 사용: {}", 
                    envName, propName, defaultValue);
        return defaultValue;
    }

    /**
     * 데이터베이스 연결을 획득하는 메서드
     * <p>
     * JDBC를 사용하여 MySQL 데이터베이스에 연결한다.
     * 환경변수에서 설정된 사용자명과 비밀번호를 사용한다.
     * </p>
     * 
     * @author wnwoghd
     * 
     * @return 데이터베이스 Connection 객체
     * @throws SQLException DB 연결 실패 시
     * 
     * @apiNote try-with-resources 문과 함께 사용하여 자동으로 연결을 닫도록 권장
     */
    private Connection getConnection() throws SQLException {
        // 환경변수에서 DB 사용자명과 비밀번호 로드
        String dbUser = getEnvOrProperty("DB_USER", "db.user", "planp_user");
        String dbPassword = getEnvOrProperty("DB_PASSWORD", "db.password", "planp_password");
        
        // JDBC 드라이버 로드는 보통 프로젝트 설정(Maven/Gradle)에 의해 자동 처리됨
        logger.debug("DB 연결 시도: URL={}, User={}", DB_URL, dbUser);
        
        try {
            Connection conn = DriverManager.getConnection(DB_URL, dbUser, dbPassword);
            logger.info("데이터베이스 연결 성공: {}", DB_URL);
            return conn;
        } catch (SQLException e) {
            logger.error("데이터베이스 연결 실패: URL={}, User={}", DB_URL, dbUser, e);
            throw e;
        }
    }
    /**
     * 새로운 사용자 정보를 저장하는 메서드
     * <p>
     * 주어진 사용자 객체를 데이터베이스 users 테이블에 INSERT한다.
     * SQL Injection 방지를 위해 PreparedStatement를 사용하여
     * 매개변수를 안전하게 설정한다.
     * </p>
     * @author wnwoghd
     * 
     * @param user 저장할 사용자 객체 (null이면 안됨)
     * @return 저장 성공 시 true, 실패 시 false
     *
     * @throws NullPointerException user가 null인 경우
     * @throws IllegalArgumentException user의 필수 필드가 누락된 경우
     *
     * @apiNote 데이터베이스의 트랜잭션이 자동으로 처리됨
     * @see java.sql.PreparedStatement
     */
    public boolean save(User user) {
        if (user == null) {
            logger.error("저장할 사용자 객체가 null입니다");
            throw new NullPointerException("사용자 객체는 null일 수 없습니다");
        }
        
        if (user.getUserId() == null || user.getEmail() == null) {
            logger.error("사용자 필수 정보 누락: userId={}, email={}", 
                user.getUserId(), user.getEmail());
            throw new IllegalArgumentException("사용자 ID, 이메일은 필수입니다");
        }

        // SQL 쿼리: 새 사용자를 users 테이블에 삽입
        // 매개변수: (user_id, email, name, password)
        String sql = "INSERT INTO users (user_id, email, name, password) VALUES (?, ?, ?, ?)";

        try (Connection conn = getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            logger.debug("사용자 저장 시도: userId={}, email={}", 
                user.getUserId(), user.getEmail());
            
            // 쿼리 매개변수 설정 (SQL Injection 방지)
            pstmt.setString(1, user.getUserId());
            pstmt.setString(2, user.getEmail());
            pstmt.setString(3, user.getName());
            pstmt.setString(4, user.getPassword());

            int affectedRows = pstmt.executeUpdate(); // INSERT 쿼리 실행, 영향받은 행 수 반환
            
            if (affectedRows > 0) {
                logger.info("사용자 저장 성공: userId={}, email={}", user.getUserId(), user.getEmail());
                return true;
            }
            return false;
            
        } catch (SQLException e) {
            // DB 오류 처리 (예: 중복 키 제약 조건 위반, DB 연결 오류 등)
            // 데이터베이스의 자동 롤백 기능이 활성화되어 있으므로 수동 롤백 불필요
            logger.error("데이터베이스 저장 오류 발생: userId={}, email={}", 
                user.getUserId(), user.getEmail(), e);

            return false;
        }
    }
        
    /**
     * 사용자 ID를 기준으로 사용자 정보를 조회하는 메서드
     * <p>
     * 주어진 사용자 ID에 해당하는 사용자 객체를 반환한다.
     * 존재하지 않는 ID인 경우 null을 반환한다.
     * </p>
     *
     * @author wnwoghd
     * 
     * @param userId 조회할 사용자 ID (null이거나 빈 문자열이면 안됨)
     * @return 조회된 사용자 객체, 존재하지 않으면 null
     *
     * @throws IllegalArgumentException userId가 null이거나 빈 문자열인 경우
     */
    public User findByUserId(String userId) {
        if (userId == null || userId.trim().isEmpty()) {
            logger.warn("잘못된 사용자 ID로 조회 시도: userId={}", userId);
            throw new IllegalArgumentException("사용자 ID는 null이거나 빈 문자열일 수 없습니다");
        }
                
        String sql = "SELECT user_id, email, name, password FROM users WHERE user_id = ?";
        
        try (Connection conn = getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            logger.debug("사용자 ID로 조회 시도: userId={}", userId);
            pstmt.setString(1, userId);
            
            try (ResultSet rs = pstmt.executeQuery()) { // SELECT 쿼리 실행
                if (rs.next()) {
                    // 결과셋(ResultSet)에서 데이터 추출하여 User 객체 생성
                    User user = new User();
                    user.setUserId(rs.getString("user_id"));
                    user.setEmail(rs.getString("email"));
                    user.setName(rs.getString("name"));
                    user.setPassword(rs.getString("password")); // 로그인 시 비밀번호 확인에 사용
                    
                    logger.debug("사용자 조회 성공: userId={}, name={}", userId, user.getName());
                    return user;
                }
            }
            // 사용자를 찾지 못한 경우
            logger.debug("사용자를 찾을 수 없음: userId={}", userId);
            return null;
            
        } catch (SQLException e) {
            logger.error("데이터베이스 조회 오류 발생: userId={}", userId, e);
            return null;
        }
    }

    /**
     * 이메일 주소를 기준으로 사용자 정보를 조회하는 메서드
     * <p>
     * 주어진 이메일 주소에 해당하는 사용자 객체를 반환한다.
     * 로그인이나 비밀번호 찾기 기능에서 주로 사용된다.
     * </p>
     * 
     * @author wnwoghd
     * 
     * @param email 조회할 이메일 주소 (null이거나 빈 문자열이면 안됨)
     * @return 조회된 사용자 객체, 존재하지 않으면 null
     * 
     * @throws IllegalArgumentException email이 null이거나 빈 문자열인 경우
     */
    public User findByEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            logger.warn("잘못된 이메일로 조회 시도: email={}", email);
            throw new IllegalArgumentException("이메일은 null이거나 빈 문자열일 수 없습니다");
        }
        
        // [변경됨]: DB에서 해당 이메일의 사용자 정보를 조회하는 SQL 쿼리
        String sql = "SELECT user_id, email, name, password FROM users WHERE email = ?";
        
        try (Connection conn = getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            logger.debug("이메일로 조회 시도: email={}", email);
            pstmt.setString(1, email);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    User user = new User();
                    user.setUserId(rs.getString("user_id"));
                    user.setEmail(rs.getString("email"));
                    user.setName(rs.getString("name"));
                    user.setPassword(rs.getString("password"));                    
                    logger.debug("이메일로 사용자 조회 성공: email={}, userId={}", email, user.getUserId());
                    return user;
                }
            }
            logger.debug("해당 이메일의 사용자를 찾을 수 없음: email={}", email);
            return null;
            
        } catch (SQLException e) {
            logger.error("데이터베이스 이메일 조회 오류 발생: email={}", email, e);
            return null;
        }
    }

    /**
     * 사용자 ID의 중복 여부를 확인하는 메서드
     * <p>
     * 회원가입 시 사용자 ID가 이미 존재하는지 검사할 때 사용한다.
     * 빠른 응답을 위해 containsKey() 메서드를 사용한다.
     * </p>
     *
     * @author wnwoghd
     * 
     * @param userId 중복 확인할 사용자 ID
     * @return 이미 존재하면 true, 사용 가능하면 false
     *
     * @throws IllegalArgumentException userId가 null이거나 빈 문자열인 경우
     */
    public boolean existsByUserId(String userId) {
        if (userId == null || userId.trim().isEmpty()) {
            logger.warn("잘못된 사용자 ID로 중복 확인 시도: userId={}", userId);
            throw new IllegalArgumentException("사용자 ID는 null이거나 빈 문자열일 수 없습니다");
        }
        
        String sql = "SELECT COUNT(*) FROM users WHERE user_id = ?";
        boolean exists = false;
        
        try (Connection conn = getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, userId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    // COUNT(*) 결과가 1 이상이면 존재함
                    if (rs.getInt(1) > 0) {
                        exists = true;
                    }
                }
            }
            
            logger.debug("사용자 ID 중복 확인: userId={}, exists={}", userId, exists);
            return exists;
            
        } catch (SQLException e) {
            logger.error("데이터베이스 중복 확인 오류 발생: userId={}", userId, e);
            // DB 오류 시 false 반환 (연결 오류라면 true를 반환하여 안전하게 중복을 막을 수도 있음)
            return false;
        }
    }

    /**
     * 이메일 주소의 중복 여부를 확인하는 메서드 (DB 연동 로직으로 수정됨)
     * @author wnwoghd
     */
    public boolean existsByEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            logger.warn("잘못된 이메일로 중복 확인 시도: email={}", email);
            throw new IllegalArgumentException("이메일은 null이거나 빈 문자열일 수 없습니다");
        }
        
        // [변경됨]: 해당 이메일을 가진 레코드 수를 세는 SQL
        String sql = "SELECT COUNT(*) FROM users WHERE email = ?";
        boolean exists = false;
        
        try (Connection conn = getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, email);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    if (rs.getInt(1) > 0) {
                        exists = true;
                    }
                }
            }
            
            logger.debug("이메일 중복 확인: email={}, exists={}", email, exists);
            return exists;
            
        } catch (SQLException e) {
            logger.error("데이터베이스 이메일 중복 확인 오류 발생: email={}", email, e);
            return false;
        }
    }

    /**
     * 등록된 모든 사용자 목록을 조회하는 메서드
     * <p>
     * [변경됨]: 메모리 조회 대신 **DB에 SELECT * 쿼리**를 실행한다.
     * </p>
     * 
     * @author wnwoghd
     * 
     * @return 모든 사용자 객체를 담은 리스트 (빈 리스트일 수 있음)
     * @apiNote 사용자 수가 많을 경우 메모리 사용량이 클 수 있으니 주의
     */
    public List<User> findAll() {
        logger.debug("전체 사용자 조회 요청");
        List<User> allUsers = new ArrayList<>();
        
        // [변경됨]: 모든 사용자 레코드를 조회하는 SQL
        String sql = "SELECT user_id, email, name, password FROM users";
        
        try (Connection conn = getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            ResultSet rs = pstmt.executeQuery()) {
            
            // 모든 행을 반복하며 User 객체로 변환하여 리스트에 추가
            while (rs.next()) {
                User user = new User();
                user.setUserId(rs.getString("user_id"));
                user.setEmail(rs.getString("email"));
                user.setName(rs.getString("name"));
                user.setPassword(rs.getString("password"));
                allUsers.add(user);
            }
            
            logger.info("전체 사용자 조회 완료: 반환된사용자수={}", allUsers.size());
            return allUsers;
            
        } catch (SQLException e) {
            logger.error("데이터베이스 전체 조회 오류 발생", e);
            return new ArrayList<>(); // 오류 발생 시 빈 리스트 반환
        }
    }

    /**
     * 등록된 전체 사용자 수를 반환하는 메서드
     * <p>
     * [변경됨]: 메모리 사이즈 대신 **DB에 COUNT(*) 쿼리**를 실행한다.
     * </p>
     * 
     * @author wnwoghd
     * 
     * @return 현재 등록된 사용자의 총 개수
     */
    public int count() {
        // [변경됨]: 전체 레코드 수를 세는 SQL
        String sql = "SELECT COUNT(*) FROM users";
        int userCount = 0;
        
        try (Connection conn = getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            ResultSet rs = pstmt.executeQuery()) {
            
            if (rs.next()) {
                userCount = rs.getInt(1); // COUNT(*)의 결과는 첫 번째 컬럼
            }
            
            logger.debug("사용자 수 조회: count={}", userCount);
            return userCount;
            
        } catch (SQLException e) {
            logger.error("데이터베이스 사용자 수 조회 오류 발생", e);
            return 0; // 오류 발생 시 0 반환
        }
    }

    /**
     * 모든 사용자 데이터를 삭제하는 메서드
     * <p>
     * [변경됨]: 메모리 clear 대신 **DB에 DELETE 쿼리**를 실행한다.
     * </p>
     * 
     * @author wnwoghd
     * 
     * @apiNote 테스트 환경에서만 사용 권장 - 운영 환경에서는 위험
     */
    public void deleteAll() {
        // [변경됨]: 전체 레코드를 삭제하는 SQL
        String sql = "DELETE FROM users";
        
        try (Connection conn = getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            int deletedCount = pstmt.executeUpdate();
            
            logger.warn("전체 사용자 데이터 삭제 완료: 삭제된사용자수={}", deletedCount);
            
        } catch (SQLException e) {
            logger.error("데이터베이스 전체 삭제 오류 발생", e);
        }
    }

    /**
     * 특정 사용자를 삭제하는 메서드
     * <p>
     * [변경됨]: 메모리 remove 대신 **DB에 DELETE 쿼리**를 실행한다.
     * </p>
     * 
     * @author wnwoghd
     * 
     * @param userId 삭제할 사용자의 ID
     * @return 삭제 성공 시 true, 해당 사용자가 존재하지 않으면 false
     * 
     * @throws IllegalArgumentException userId가 null이거나 빈 문자열인 경우
     * 
     * @apiNote 계정 탈퇴나 관리자의 사용자 제재 기능에서 사용
     */
    public boolean deleteByUserId(String userId) {
        if (userId == null || userId.trim().isEmpty()) {
            logger.error("잘못된 사용자 ID로 삭제 시도: userId={}", userId);
            throw new IllegalArgumentException("사용자 ID는 null이거나 빈 문자열일 수 없습니다");
        }
        
        logger.info("사용자 삭제 시도: userId={}", userId);
        
        // [변경됨]: 특정 user_id를 가진 레코드를 삭제하는 SQL
        String sql = "DELETE FROM users WHERE user_id = ?";
        
        try (Connection conn = getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, userId);
            int affectedRows = pstmt.executeUpdate(); // 삭제된 행 수
            
            if (affectedRows > 0) {
                logger.info("사용자 삭제 성공: userId={}", userId);
                return true;
            } else {
                logger.warn("삭제할 사용자를 찾을 수 없음: userId={}", userId);
                return false;
            }
            
        } catch (SQLException e) {
            logger.error("데이터베이스 사용자 삭제 오류 발생: userId={}", userId, e);
            return false;
        }
    }
}