package com.drhong.dao;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.drhong.model.User;

/**
 * 사용자 데이터 접근 객체 (Data Access Object)
 * <p>
 * 사용자 정보의 저장, 조회, 수정, 삭제 등 데이터베이스 관련 작업을 담당하는 DAO 클래스이다.
 * 현재는 개발 편의를 위해 메모리 기반의 임시 저장소를 사용하고 있으며,
 * 향후 실제 데이터베이스(MySQL)로 교체할 예정이다.
 * </p>
 * 
 * <h3>주요 기능:</h3>
 * <ul>
 *   <li>사용자 정보 저장 및 조회</li>
 *   <li>사용자 ID 및 이메일 기반 검색</li>
 *   <li>중복 확인 (ID, 이메일)</li>
 *   <li>사용자 데이터 관리 (삭제, 전체 조회)</li>
 * </ul>
 * 
 * 
 * @author bang9634
 * @since 2025-11-10
 * @implNote 현재 메모리 기반 저장소 사용 중 - 추후 DB 연동 예정
 */
public class UserDAO {

    /** SLF4J Logger 인스턴스 - 데이터 접근 로그 기록 */
    private static final Logger logger = LoggerFactory.getLogger(UserDAO.class);
    
    // 임시 메모리 저장소 (thread-safe) 
    private static final ConcurrentMap<String, User> userStorage = new ConcurrentHashMap<>();
    private static final ConcurrentMap<String, User> emailStorage = new ConcurrentHashMap<>();

    /**
     * 새로운 사용자 정보를 저장하는 메서드
     * <p>
     * 사용자 ID와 이메일을 각각 다른 저장소에 키로 저장하여
     * 빠른 검색이 가능하도록 한다. 두 저장소 모두에 성공적으로 저장되어야
     * 데이터 일관성이 보장된다.
     * </p>
     * 
     * @param user 저장할 사용자 객체 (null이면 안됨)
     * @return 저장 성공 시 true, 실패 시 false
     * 
     * @throws NullPointerException user가 null인 경우
     * @throws IllegalArgumentException user의 필수 필드가 누락된 경우
     * 
     * @implNote 현재는 메모리에 저장되므로 애플리케이션 재시작 시 데이터 손실됨
     */
    public boolean save(User user) {
        if (user == null) {
            logger.error("저장할 사용자 객체가 null입니다");
            throw new NullPointerException("사용자 객체는 null일 수 없습니다");
        }
        
        if (user.getUserId() == null || user.getEmail() == null) {
            logger.error("사용자 필수 정보 누락: userId={}, email={}", user.getUserId(), user.getEmail());
            throw new IllegalArgumentException("사용자 ID와 이메일은 필수입니다");
        }

        try {
            logger.debug("사용자 저장 시도: userId={}, email={}", user.getUserId(), user.getEmail());
           
            // @TODO: 임시 스토리지가 아닌 DB연동으로 수정해야함.
            userStorage.put(user.getUserId(), user);
            emailStorage.put(user.getEmail(), user);

            logger.info("사용자 저장 성공: userId={}, email={}, 전체사용자수={}", 
                user.getUserId(), user.getEmail(), userStorage.size());

            return true;
        } catch (Exception e) {
            logger.error("사용자 저장 중 예기치 않은 오류: userId={}, email={}", 
                user.getUserId(), user.getEmail(), e);
            
            // 부분 저장으로 인한 데이터 불일치 방지를 위해 롤백
            // TODO: DB 연동으로 수정
            userStorage.remove(user.getUserId());
            emailStorage.remove(user.getEmail());

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
        
        logger.debug("사용자 ID로 조회 시도: userId={}", userId);
        
        // TODO: DB연동으로 수정
        User user = userStorage.get(userId);
        
        if (user != null) {
            logger.debug("사용자 조회 성공: userId={}, name={}", userId, user.getName());
        } else {
            logger.debug("사용자를 찾을 수 없음: userId={}", userId);
        }
        
        return user;
    }

    /**
     * 이메일 주소를 기준으로 사용자 정보를 조회하는 메서드
     * <p>
     * 주어진 이메일 주소에 해당하는 사용자 객체를 반환한다.
     * 로그인이나 비밀번호 찾기 기능에서 주로 사용된다.
     * </p>
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
        
        logger.debug("이메일로 조회 시도: email={}", email);
        
        // TODO: DB 연동으로 수정
        User user = emailStorage.get(email);
        
        if (user != null) {
            logger.debug("이메일로 사용자 조회 성공: email={}, userId={}", email, user.getUserId());
        } else {
            logger.debug("해당 이메일의 사용자를 찾을 수 없음: email={}", email);
        }
        
        return user;
    }

    /**
     * 사용자 ID의 중복 여부를 확인하는 메서드
     * <p>
     * 회원가입 시 사용자 ID가 이미 존재하는지 검사할 때 사용한다.
     * 빠른 응답을 위해 containsKey() 메서드를 사용한다.
     * </p>
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
        
        // TODO: DB연동으로 수정
        boolean exists = userStorage.containsKey(userId);
        
        logger.debug("사용자 ID 중복 확인: userId={}, exists={}", userId, exists);
        
        return exists;
    }

    /**
     * 이메일 주소의 중복 여부를 확인하는 메서드
     * <p>
     * 회원가입 시 이메일 주소가 이미 다른 사용자에 의해 사용되고 있는지 검사한다.
     * 한 명의 사용자가 여러 계정을 만드는 것을 방지하기 위한 용도이다.
     * </p>
     * 
     * @param email 중복 확인할 이메일 주소
     * @return 이미 존재하면 true, 사용 가능하면 false
     * 
     * @throws IllegalArgumentException email이 null이거나 빈 문자열인 경우
     */
    public boolean existsByEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            logger.warn("잘못된 이메일로 중복 확인 시도: email={}", email);
            throw new IllegalArgumentException("이메일은 null이거나 빈 문자열일 수 없습니다");
        }
        
        // TODO: DB 연동으로 수정
        boolean exists = emailStorage.containsKey(email);
        
        logger.debug("이메일 중복 확인: email={}, exists={}", email, exists);
        
        return exists;
    }

    /**
     * 등록된 모든 사용자 목록을 조회하는 메서드
     * <p>
     * 관리자 기능이나 테스트 목적으로 전체 사용자 목록이 필요할 때 사용한다.
     * 대량의 사용자가 있을 경우 성능에 주의해야 한다.
     * </p>
     * 
     * @return 모든 사용자 객체를 담은 리스트 (빈 리스트일 수 있음)
     * 
     * @apiNote 사용자 수가 많을 경우 메모리 사용량이 클 수 있으니 주의
     */
    public List<User> findAll() {
        logger.debug("전체 사용자 조회 요청: 사용자수={}", userStorage.size());
        
        // TODO: DB 연동으로 수정
        List<User> allUsers = new ArrayList<>(userStorage.values());
        
        logger.info("전체 사용자 조회 완료: 반환된사용자수={}", allUsers.size());
        
        return allUsers;
    }

    /**
     * 등록된 전체 사용자 수를 반환하는 메서드
     * <p>
     * 시스템 통계나 관리 목적으로 사용자 수를 확인할 때 사용한다.
     * 실제 DB에서는 COUNT(*) 쿼리에 해당하는 기능이다.
     * </p>
     * 
     * @return 현재 등록된 사용자의 총 개수
     */
    public int count() {
        // TODO: DB연동으로 수정
        int userCount = userStorage.size();
        
        logger.debug("사용자 수 조회: count={}", userCount);
        
        return userCount;
    }

    /**
     * 모든 사용자 데이터를 삭제하는 메서드
     * <p>
     * <strong>주의:</strong> 이 메서드는 주로 테스트 목적으로 사용된다.
     * 운영 환경에서는 매우 신중하게 사용해야 하며, 일반적으로 사용하지 않는다.
     * </p>
     * 
     * @apiNote 테스트 환경에서만 사용 권장 - 운영 환경에서는 위험
     */
    public void deleteAll() {
        // TODO: DB연동으로 수정
        int beforeCount = userStorage.size();
        
        logger.warn("전체 사용자 데이터 삭제 시작: 삭제될사용자수={}", beforeCount);
        
        // 두 저장소 모두 초기화
        // TODO: DB연동으로 수정
        userStorage.clear();
        emailStorage.clear();
        
        logger.warn("전체 사용자 데이터 삭제 완료: 삭제된사용자수={}", beforeCount);
    }

    /**
     * 특정 사용자를 삭제하는 메서드
     * <p>
     * 주어진 사용자 ID에 해당하는 사용자를 시스템에서 완전히 제거한다.
     * 사용자 ID 기반 저장소와 이메일 기반 저장소에서 모두 제거하여
     * 데이터 일관성을 유지한다.
     * </p>
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
        
        // 사용자 ID 기반 저장소에서 제거
        // TODO: DB연동으로 수정
        User removedUser = userStorage.remove(userId);
        
        if (removedUser != null) {
            // 이메일 기반 저장소에서도 제거하여 일관성 유지
            // TODO: DB연동으로 수정
            emailStorage.remove(removedUser.getEmail());
            
            logger.info("사용자 삭제 성공: userId={}, email={}, 남은사용자수={}", 
                userId, removedUser.getEmail(), userStorage.size());
            
            return true;
        } else {
            logger.warn("삭제할 사용자를 찾을 수 없음: userId={}", userId);
            return false;
        }
    }
}