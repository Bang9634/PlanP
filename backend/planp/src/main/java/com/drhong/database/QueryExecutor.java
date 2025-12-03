package com.drhong.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.drhong.repository.UserRepository;

/**
 * 데이터베이스 쿼리 실행을 위한 클래스
 * <p>
 * JDBC를 사용한 데이터베이스 작업에서 반복되는 코드를 제거하고,
 * Connection 관리를 자동화하여 안전하고 일관된 데이터베이스 접근을 제공한다.
 * </p>
 * 
 * <h3>주요 기능</h3>
 * <ul>
 *   <li><strong>자동 리소스 관리:</strong> Connection, PreparedStatement, ResultSet 자동 해제</li>
 *   <li><strong>SQL Injection 방지:</strong> PreparedStatement를 통한 파라미터 바인딩</li>
 * </ul>
 * 
 * <h3>지원하는 쿼리 유형</h3>
 * <ul>
 *   <li><strong>DML:</strong> INSERT, UPDATE, DELETE (executeUpdate)</li>
 *   <li><strong>SELECT:</strong> 단일 결과 조회 (executeQuerySingle)</li>
 *   <li><strong>SELECT:</strong> 다중 결과 조회 (executeQueryList)</li>
 *   <li><strong>COUNT:</strong> 개수 조회 (executeCount)</li>
 *   <li><strong>EXISTS:</strong> 존재 여부 확인 (exists)</li>
 * <ul>
 * 
 * <h3>사용 예시</h3>
* <pre>{@code
 * QueryExecutor queryExecutor = new QueryExecutor();
 * 
 * // INSERT 실행
 * String insertSql = "INSERT INTO users (user_id, name) VALUES (?, ?)";
 * queryExecutor.executeUpdate(insertSql, "user123", "홍길동");
 * 
 * // SELECT 단일 결과
 * String selectSql = "SELECT * FROM users WHERE user_id = ?";
 * User user = queryExecutor.executeQuerySingle(selectSql, rs -> {
 *     User u = new User();
 *     u.setUserId(rs.getString("user_id"));
 *     u.setName(rs.getString("name"));
 *     return u;
 * }, "user123");
 * 
 * // SELECT 리스트 결과
 * String selectAllSql = "SELECT * FROM users";
 * List<User> users = queryExecutor.executeQueryList(selectAllSql, rs -> {
 *     User u = new User();
 *     u.setUserId(rs.getString("user_id"));
 *     u.setName(rs.getString("name"));
 *     return u;
 * });
 * 
 * // COUNT 조회
 * String countSql = "SELECT COUNT(*) FROM users WHERE is_active = ?";
 * int count = queryExecutor.executeCount(countSql, true);
 * 
 * // EXISTS 확인
 * String existsSql = "SELECT COUNT(*) FROM users WHERE user_id = ?";
 * boolean exists = queryExecutor.exists(existsSql, "user123");
 * }</pre>
 * 
 * 
 * 
 * @author bang9634
 * @since 2025-11-18
 * 
 * @see ConnectionManager
 * @see UserRepository
 */
public class QueryExecutor {
    private static final Logger logger = LoggerFactory.getLogger(QueryExecutor.class);
    private final ConnectionManager connectionManager;

    /**
     * QueryExecutor 인스턴스를 생성한다.
     * 
     * <p>
     * ConnectionManager 인스턴스를 가져와 초기화한다.
     * </p>
     * 
     * @implNote ConnectionManager.getInstance()를 통해 Connection Pool에 접근
     */
    // @Deprecated
    // public QueryExecutor() {
    //    this.connectionManager = ConnectionManager.getInstance();
    //}

    public QueryExecutor(ConnectionManager connectionManager) {
        this.connectionManager = connectionManager;
    }

    /**
     * INSERT, UPDATE, DELETE 쿼리를 실행한다.
     * 
     * <p>
     * DML(Data Manipulation Language) 쿼리를 실행하고 영향받은 행의 개수를 반환한다.
     * Connection은 자동으로 획득 및 반환되며, PreparedStatement를 통해 SQLInjection을 방지한다.
     * </p>
     * 
     * <h3>지원하는 쿼리:</h3>
     * <ul>
     *   <li><strong>INSERT:</strong> 새 레코드 삽입</li>
     *   <li><strong>UPDATE:</strong> 기존 레코드 수정</li>
     *   <li><strong>DELETE:</strong> 레코드 삭제</li>
     * </ul>
     * 
     * @param sql 실행할 SQL 문 (?로 파라미터의 위치 표시)
     * @param params SQL 파라미터 값들 (가변 인자, SQL의 ? 순서대로)
     * @return 영향받은 행의 개수 (INSERT, UPDATE, DELETE된 행의 개수)
     * 
     * @throws SQLException 다음과 같은 경우 발생:
     *         <ul>
     *         <li>SQL 문법 오류</li>
     *         <li>테이블이나 컬럼이 존재하지 않음</li>
     *         <li>제약 조건 위반 (UNIQUE, FOREIGN KEY 등)</li>
     *         <li>데이터베이스 연결 실패</li>
     *         <li>파라미터 개수 불일치</li>
     *         </ul>
     * 
     * @apiNote 트랜잭션 관리는 호출자가 별도 처리
     * @implNote Connection은 finally 블록에서 항상 반환
     */
    public int executeUpdate(String sql, Object... params) throws SQLException {
        Connection conn = null;
        try {
            conn = connectionManager.getConnection();
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                setParameters(pstmt, params);
                int result = pstmt.executeUpdate();
                logger.debug("Query executed: {} (affected rows: {})", sql, result);
                return result;
            }
        } finally {
            if (conn != null) {
                connectionManager.releaseConnection(conn);
            }
        }
    }

    /**
     * SELECT 쿼리를 실행하여 단일 결과를 반환한다.
     * 
     * <p>
     * 조회 결과가 없으면 null을 반환하고, 여러 행이 있으면 첫 번째 행만 반환한다.
     * ResultSetMapper를 통해 ResultSet을 원하는 객체 타입으로 변환한다.
     * </p>
     * 
     * @param <T> 반환할 객체의 타입
     * @param sql 실행할 SELECT 문 (?로 파라미터 위치 표시)
     * @param mapper ResultSet을 T 타입으로 변환하는 함수
     * @param params SQL 파라미터 값들 (가변 인자)
     * @return 매핑된 객체 또는 결과가 없으면 null
     * 
     * @throws SQLException 다음과 같은 경우 발생:
     *         <ul>
     *         <li>SQL 문법 오류</li>
     *         <li>테이블이나 컬럼이 존재하지 않음</li>
     *         <li>데이터베이스 연결 실패</li>
     *         <li>ResultSet 매핑 중 오류 (잘못된 컬럼명, 타입 불일치 등)</li>
     *         </ul>
     * 
     * @apiNote 여러 행이 조회되어도 첫 번째 행만 반환됨
     * @implNote ResultSet은 try-with-resources로 자동 close됨
     */
    public <T> Optional<T> executeQuerySingle(String sql, ResultSetMapper<T> mapper, Object... params) throws SQLException {
        Connection conn = null;
        try {
            conn = connectionManager.getConnection();
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                setParameters(pstmt, params);
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        T result = mapper.map(rs);
                        return Optional.ofNullable(result);
                    }
                    return Optional.empty();
                }
            }
        } finally {
            if (conn != null) {
                connectionManager.releaseConnection(conn);
            }
        }
    }

    /**
     * SELECT 쿼리를 실행하여 여러 결과를 리스트로 반환한다.
     * 
     * <p>
     * 조회 결과의 모든 행을 순회하며 ResultSetMapper를 통해 객체로 변환한 후
     * List에 담아 반환한다. 결과가 없으면 빈 리스트를 반환한다.
     * </p>
     * 
     * @param <T> 반환할 객체의 타입
     * @param sql 실행할 SELECT 문 (?로 파라미터 위치 표시)
     * @param mapper ResultSet을 T 타입으로 변환하는 함수
     * @param params SQL 파라미터 값들 (가변 인자, 없어도 됨)
     * @return 매핑된 객체들의 리스트 (결과 없으면 빈 리스트, null 아님)
     * 
     * @throws SQLException 다음과 같은 경우 발생:
     *         <ul>
     *         <li>SQL 문법 오류</li>
     *         <li>테이블이나 컬럼이 존재하지 않음</li>
     *         <li>데이터베이스 연결 실패</li>
     *         <li>ResultSet 매핑 중 오류</li>
     *         </ul>
     * 
     * @apiNote 결과가 많을 경우 메모리 사용량 주의
     * @implNote 결과가 없으면 null이 아닌 빈 ArrayList 반환
     */
    public <T> List<T> executeQueryList(String sql, ResultSetMapper<T> mapper, Object... params) throws SQLException {
        Connection conn = null;
        try {
            conn = connectionManager.getConnection();
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                setParameters(pstmt, params);
                try (ResultSet rs = pstmt.executeQuery()) {
                    List<T> results = new ArrayList<>();
                    while (rs.next()) {
                        results.add(mapper.map(rs));
                    }
                    return results;
                }
            }
        } finally {
            if (conn != null) {
                connectionManager.releaseConnection(conn);
            }
        }
    }


    /**
     * COUNT 쿼리를 실행하여 개수를 반환한다.
     * 
     * <p>
     * SELECT COUNT(*) 형태의 쿼리를 실행하고 첫 번째 컬럼의 정수값을 반환한다.
     * 결과가 없으면 0을 반환한다.
     * </p>
     * 
     * @param sql COUNT(*) 또는 COUNT(컬럼) 쿼리
     * @param params SQL 파라미터 값들 (가변 인자, 없어도 됨)
     * @return 조회된 개수 (결과 없으면 0)
     * 
     * @throws SQLException 다음과 같은 경우 발생:
     *         <ul>
     *         <li>SQL 문법 오류</li>
     *         <li>테이블이 존재하지 않음</li>
     *         <li>데이터베이스 연결 실패</li>
     *         <li>첫 번째 컬럼이 정수 타입이 아님</li>
     *         </ul>
     * 
     * @apiNote COUNT(*) 형태의 쿼리 사용 권장
     * @implNote ResultSet의 첫 번째 컬럼(index 1)을 정수로 읽음
     */
    public int executeCount(String sql, Object... params) throws SQLException {
        Connection conn = null;
        try {
            conn = connectionManager.getConnection();
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                setParameters(pstmt, params);
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        return rs.getInt(1);
                    }
                    return 0;
                }
            }
        } finally {
            if (conn != null) {
                connectionManager.releaseConnection(conn);
            }
        }
    }

    /**
     * 레코드 존재 여부를 확인한다.
     * 
     * <p>
     * SELECT COUNT(*) 쿼리를 실행하여 결과가 0보다 크면 true를 반환한다.
     * 중복 체크나 존재 여부 확인에 최적화되어 있다.
     * </p>
     * 
     * <h3>executeCount와의 차이:</h3>
     * <ul>
     *   <li><strong>executeCount:</strong> 개수 자체가 필요할 때 (예: "총 10명")</li>
     *   <li><strong>exists:</strong> 존재 여부만 확인할 때 (예: "있다/없다")</li>
     * </ul>
     * 
     * @param sql COUNT(*) 쿼리 (결과가 0보다 크면 존재)
     * @param params SQL 파라미터 값들 (가변 인자)
     * @return 레코드가 존재하면 true, 없으면 false
     * 
     * @throws SQLException 다음과 같은 경우 발생:
     *         <ul>
     *         <li>SQL 문법 오류</li>
     *         <li>테이블이 존재하지 않음</li>
     *         <li>데이터베이스 연결 실패</li>
     *         </ul>
     * 
     * @apiNote 중복 체크 시 executeQuerySingle보다 성능이 좋음
     * @implNote COUNT(*) > 0 조건으로 판단
     */
    public boolean exists(String sql, Object... params) throws SQLException {
        Connection conn = null;
        try {
            conn = connectionManager.getConnection();
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                setParameters(pstmt, params);
                try (ResultSet rs = pstmt.executeQuery()) {
                    return rs.next() && rs.getInt(1) > 0;
                }
            }
        } finally {
            if (conn != null) {
                connectionManager.releaseConnection(conn);
            }
        }
    }

    /**
     * PreparedStatement에 파라미터를 설정한다.
     * 
     * <p>
     * SQL 문의 ? 위치에 순서대로 파라미터를 바인딩한다.
     * 이를 통해 SQL Injection 공격을 방지한다.
     * </p>
     * 
     * <h3>지원하는 타입:</h3>
     * <ul>
     *   <li>String, Integer, Long, Boolean, Double, Float</li>
     *   <li>Date, Timestamp, LocalDate, LocalDateTime</li>
     *   <li>기타 JDBC가 지원하는 모든 타입</li>
     * </ul>
     * 
     * <h3>사용 예시:</h3>
     * <pre>{@code
     * PreparedStatement pstmt = conn.prepareStatement(
     *     "INSERT INTO users (user_id, name, age, is_active) VALUES (?, ?, ?, ?)"
     * );
     * 
     * setParameters(pstmt, "user123", "홍길동", 30, true);
     * // 결과:
     * // pstmt.setObject(1, "user123");   // String
     * // pstmt.setObject(2, "홍길동");     // String
     * // pstmt.setObject(3, 30);          // Integer
     * // pstmt.setObject(4, true);        // Boolean
     * }</pre>
     * 
     * @param pstmt 파라미터를 설정할 PreparedStatement
     * @param params 바인딩할 파라미터 값들 (가변 인자, SQL의 ? 순서대로)
     * 
     * @throws SQLException 다음과 같은 경우 발생:
     *         <ul>
     *         <li>파라미터 개수가 SQL의 ? 개수와 다름</li>
     *         <li>파라미터 타입이 컬럼 타입과 호환되지 않음</li>
     *         <li>null 허용 안 되는 컬럼에 null 전달</li>
     *         </ul>
     * 
     * @apiNote setObject()를 사용하여 타입을 자동으로 변환
     * @implNote 파라미터 인덱스는 1부터 시작 (JDBC 스펙)
     */
    private void setParameters(PreparedStatement pstmt, Object... params) throws SQLException {
        for (int i = 0; i < params.length; i++) {
            pstmt.setObject(i + 1, params[i]);
        }
    }

    /**
     * ResultSet을 특정 타입의 객체로 매핑하는 함수형 인터페이스
     * 
     * <p>
     * 데이터베이스 조회 결과(ResultSet)를 Java 객체로 변환하는 람다 함수를 정의한다.
     * 함수형 인터페이스이므로 람다 표현식이나 메서드 참조로 간결하게 사용할 수 있다.
     * </p>
     * 
     * <h4>1. 람다 표현식 (익명 함수)</h4>
     * <pre>{@code
     * // User 객체로 매핑
     * ResultSetMapper<User> userMapper = rs -> {
     *     User user = new User();
     *     user.setUserId(rs.getString("user_id"));
     *     user.setName(rs.getString("name"));
     *     user.setEmail(rs.getString("email"));
     *     user.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
     *     user.setActive(rs.getBoolean("is_active"));
     *     return user;
     * };
     * User user = executor.executeQuerySingle(sql, userMapper, "user123");
     * }</pre>
     * 
     * <h4>2. 인라인 람다 (가장 일반적)</h4>
     * <pre>{@code
     * User user = executor.executeQuerySingle(
     *     "SELECT * FROM users WHERE user_id = ?",
     *     rs -> {
     *         User u = new User();
     *         u.setUserId(rs.getString("user_id"));
     *         u.setName(rs.getString("name"));
     *         return u;
     *     },
     *     "user123"
     * );
     * }</pre>
     * 
     * <h4>3. 메서드 참조</h4>
     * <pre>{@code
     * public class UserDAO {
     *     private User mapToUser(ResultSet rs) throws SQLException {
     *         User user = new User();
     *         user.setUserId(rs.getString("user_id"));
     *         user.setName(rs.getString("name"));
     *         return user;
     *     }
     *     
     *     public User findById(String userId) {
     *         return executor.executeQuerySingle(sql, this::mapToUser, userId);
     *     }
     * }
     * }</pre>
     * 
     * <h4>4. 단순 타입 매핑</h4>
     * <pre>{@code
     * // String 매핑
     * String name = executor.executeQuerySingle(
     *     "SELECT name FROM users WHERE user_id = ?",
     *     rs -> rs.getString("name"),
     *     "user123"
     * );
     * 
     * // Integer 매핑
     * Integer age = executor.executeQuerySingle(
     *     "SELECT age FROM users WHERE user_id = ?",
     *     rs -> rs.getInt("age"),
     *     "user123"
     * );
     * 
     * // LocalDateTime 매핑
     * LocalDateTime createdAt = executor.executeQuerySingle(
     *     "SELECT created_at FROM users WHERE user_id = ?",
     *     rs -> rs.getTimestamp("created_at").toLocalDateTime(),
     *     "user123"
     * );
     * }</pre>
     * 
     * 
     * <h3>주의사항</h3>
     * <ul>
     *   <li>ResultSet의 컬럼명은 SQL 쿼리와 정확히 일치해야 함</li>
     *   <li>null 값 처리에 주의 (rs.wasNull() 확인 또는 wrapper 타입 사용)</li>
     *   <li>타입 변환 오류 시 SQLException 발생</li>
     * </ul>
     * 
     * @param <T> 매핑할 객체의 타입
     * 
     * @apiNote 함수형 인터페이스이므로 람다 표현식 사용 권장
     */
    @FunctionalInterface
    public interface ResultSetMapper<T> {
        /**
         * ResultSet의 현재 행을 T 타입 객체로 변환한다.
         * 
         * <p>
         * 이 메서드는 ResultSet의 커서가 유효한 행을 가리킬 때 호출된다.
         * rs.next() 호출은 QueryExecutor가 처리하므로 직접 호출하지 않는다.
         * </p>
         * 
         * @param rs 현재 행을 가리키는 ResultSet (rs.next() 이미 호출됨)
         * @return 매핑된 객체 (null 가능)
         * 
         * @throws SQLException ResultSet에서 데이터를 읽는 중 오류 발생 시:
         *         <ul>
         *         <li>존재하지 않는 컬럼명 접근</li>
         *         <li>타입 변환 실패 (예: 문자열을 정수로)</li>
         *         <li>데이터 읽기 오류</li>
         *         </ul>
         * 
         * @apiNote rs.next()는 호출하지 말 것 (이미 처리됨)
         */
        T map(ResultSet rs) throws SQLException;
    }
}