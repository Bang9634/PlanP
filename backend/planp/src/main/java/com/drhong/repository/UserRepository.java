package com.drhong.repository;

import java.sql.ResultSet;         // SELECT 쿼리 결과를 받는 객체
import java.sql.SQLException;      // DB 관련 예외 처리
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.drhong.database.QueryExecutor;
import com.drhong.model.User;

/**
 * 사용자 데이터 접근 객체 (Data Access Object)
 * <p>
 * 사용자 정보의 저장, 조회, 수정, 삭제 등 데이터베이스 관련 작업을 담당하는 DAO 클래스이다.
 * </p>
 *
 * <h3>주요 기능:</h3>
 * <ul>
 *   <li>사용자 정보 저장 및 조회 (DB 연동)</li>
 *   <li>사용자 ID 및 이메일 기반 검색</li>
 *   <li>중복 확인 (ID, 이메일)</li>
 *   <li>사용자 데이터 관리 (삭제, 전체 조회)</li>
 * </ul>
 * }</pre>
 *
 * @author bang9634
 * @since 2025-11-10
 * 
 * @see com.drhong.util.ConfigUtil
 */
public class UserRepository {

    /** SLF4J Logger 인스턴스 - 데이터 접근 로그 기록 */
    private static final Logger logger = LoggerFactory.getLogger(UserRepository.class);
    
    private final QueryExecutor queryExecutor;

    public UserRepository(QueryExecutor queryExecutor) {
        this.queryExecutor = queryExecutor;
    }

    /**
     * 새로운 사용자 정보를 저장하는 메서드
     * <p>
     * 주어진 사용자 객체를 데이터베이스 users 테이블에 INSERT한다.
     * 로컬 계정과 Google OAuth 계정 모두 지원한다.
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
     */
    public void save(User user) {
        if (user == null) {
            logger.error("저장할 사용자 객체가 null입니다");
            throw new NullPointerException("사용자 객체는 null일 수 없습니다");
        }
        
        if (user.getUserId() == null || user.getEmail() == null) {
            logger.error("사용자 필수 정보 누락: userId={}, email={}", 
                user.getUserId(), user.getEmail());
            throw new IllegalArgumentException("사용자 ID, 이메일은 필수입니다");
        }

        // SQL 쿼리: Google ID 필드 포함하여 새 사용자를 users 테이블에 삽입
        String sql = "INSERT INTO users (user_id, username, password_hash, email, google_id) VALUES (?, ?, ?, ?, ?)";

        try {
            logger.debug("사용자 정보 sql문 실행: user_id={}, username={}, email={}, googleId={}",
                user.getUserId(), user.getName(), user.getEmail(), user.getGoogleId()    
            );
            queryExecutor.executeUpdate(sql,
                user.getUserId(),
                user.getName(),
                user.getPassword(), // Google 계정의 경우 null일 수 있음
                user.getEmail(),
                user.getGoogleId()  // 로컬 계정의 경우 null일 수 있음
            );
            
            logger.info("사용자 저장: {}", user.getUserId());
        } catch (SQLException e) {
            logger.warn("사용자 저장 실패: {}", user.getUserId());
            throw new RuntimeException("사용자 저장 중 예상치 못한 오류 발생");
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
     * @author bang9634
     * 
     * @param userId 조회할 사용자 ID (null이거나 빈 문자열이면 안됨)
     * @return 조회된 사용자 객체, 존재하지 않으면 null
     *
     * @throws IllegalArgumentException userId가 null이거나 빈 문자열인 경우
     */
    public Optional<User> findByUserId(String userId) {
        if (userId == null || userId.trim().isEmpty()) {
            logger.warn("잘못된 사용자 ID로 조회 시도: userId={}", userId);
            throw new IllegalArgumentException("사용자 ID는 null이거나 빈 문자열일 수 없습니다");
        }
                
        String sql = "SELECT user_id, username, password_hash, email, google_id, created_at, is_active FROM users WHERE user_id = ?";
        
        try {
            logger.debug("사용자 ID로 조회 시도: userId={}", userId);
            Optional<User> user = queryExecutor.executeQuerySingle(sql, this::mapToUser, userId);
            if (user.isEmpty()) {
                logger.debug("사용자를 찾을 수 없음");
            } else {
                logger.debug("사용자 ID로 조회 성공: userId={}", user.get().getUserId());
            }
            return user;
        } catch (SQLException e) {
            logger.warn("사용자 ID로 조회 실패: userId={}", userId);
            return Optional.empty();
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
    public Optional<User> findByEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            logger.warn("잘못된 이메일로 조회 시도: email={}", email);
            throw new IllegalArgumentException("이메일은 null이거나 빈 문자열일 수 없습니다");
        }
        
        // [변경됨]: DB에서 해당 이메일의 사용자 정보를 조회하는 SQL 쿼리
        String sql = "SELECT user_id, username, password_hash, email, google_id, created_at, is_active FROM users WHERE email = ?";
        
        try {
            logger.debug("사용자 이메일로 조회 시도: email={}", email);
            Optional<User> user = queryExecutor.executeQuerySingle(sql, this::mapToUser, email);
            if (user.isEmpty()) {
                logger.debug("사용자를 찾을 수 없음");    
             } else {
                logger.debug("사용자 이메일로 조회 성공: email={}", user.get().getEmail());
            }
            return user;
        } catch (SQLException e) {
            logger.warn("사용자 이메일로 조회 실패: email={}", email);
            return Optional.empty();
        }
    }
    
    /**
     * Google ID를 기준으로 사용자 정보를 조회하는 메서드
     * <p>
     * Google OAuth 로그인에서 사용되며, Google 계정과 연동된 사용자를 찾는다.
     * 기존에 Google로 가입한 사용자인지 확인할 때 사용된다.
     * </p>
     * 
     * @author wnwoghd
     * 
     * @param googleId 조회할 Google ID (null이거나 빈 문자열이면 안됨)
     * @return 조회된 사용자 객체, 존재하지 않으면 Optional.empty()
     * 
     * @throws IllegalArgumentException googleId가 null이거나 빈 문자열인 경우
     */
    public Optional<User> findByGoogleId(String googleId) {
        if (googleId == null || googleId.trim().isEmpty()) {
            logger.warn("잘못된 Google ID로 조회 시도: googleId={}", googleId);
            throw new IllegalArgumentException("Google ID는 null이거나 빈 문자열일 수 없습니다");
        }
        
        String sql = "SELECT user_id, username, password_hash, email, google_id, created_at, is_active FROM users WHERE google_id = ?";
        
        try {
            logger.debug("Google ID로 조회 시도: googleId={}", googleId);
            Optional<User> user = queryExecutor.executeQuerySingle(sql, this::mapToUser, googleId);
            if (user.isEmpty()) {
                logger.debug("Google ID로 사용자를 찾을 수 없음");
            } else {
                logger.debug("Google ID로 조회 성공: email={}", user.get().getEmail());
            }
            return user;
        } catch (SQLException e) {
            logger.warn("Google ID로 조회 실패: googleId={}", googleId);
            return Optional.empty();
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
        
        // 모든 사용자 레코드를 조회하는 SQL
        String sql = "SELECT user_id, email, username, password_hash, google_id, created_at, is_active FROM users";
        
        try  {
            List<User>allUsers = queryExecutor.executeQueryList(sql, this::mapToUser);
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
        int userCount;
        
        try {
            
            userCount = queryExecutor.executeCount(sql);
            
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
     * @author bang9634
     * 
     * @apiNote 테스트 환경에서만 사용 권장 - 운영 환경에서는 위험
     */
    public void deleteAll() {
        // [변경됨]: 전체 레코드를 삭제하는 SQL
        String sql = "DELETE FROM users";
        
        try {
            queryExecutor.executeUpdate(sql);
            
            logger.warn("전체 사용자 데이터 삭제 완료");
            
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
     * @author bang9634
     * 
     * @param userId 삭제할 사용자의 ID
     * @return 삭제 성공 시 true, 해당 사용자가 존재하지 않으면 false
     * 
     * @throws IllegalArgumentException userId가 null이거나 빈 문자열인 경우
     * @throws SQLException 사용자 삭제 실패시
     * 
     * @apiNote 계정 탈퇴나 관리자의 사용자 제재 기능에서 사용
     */
    public boolean deleteByUserId(String userId) {
        if (userId == null || userId.trim().isEmpty()) {
            logger.error("잘못된 사용자 ID로 삭제 시도: userId={}", userId);
            throw new IllegalArgumentException("사용자 ID는 null이거나 빈 문자열일 수 없습니다");
        }
        
        logger.info("사용자 삭제 시도: userId={}", userId);
        
        // 특정 user_id를 가진 레코드를 삭제하는 SQL
        String sql = "DELETE FROM users WHERE user_id = ?";
        
        try {
            queryExecutor.executeUpdate(sql, userId);
            return true;
        } catch (SQLException e) {
            logger.warn("사용자 삭제 실패: userId={}", userId);
            return false;
        }
    }

    private User mapToUser(ResultSet rs) {
        User user = new User();
        try {
            user.setUserId(rs.getString("user_id"));
            user.setName(rs.getString("username"));
            user.setPassword(rs.getString("password_hash"));
            user.setEmail(rs.getString("email"));
            user.setGoogleId(rs.getString("google_id")); // Google ID 매핑 추가
            user.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
            user.setActive(rs.getBoolean("is_active"));
        } catch (SQLException e) {
            logger.warn("mapToUser 실패, null 반환");
            return null;
        }
        return user;
    }
}