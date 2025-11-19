package com.drhong.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.drhong.config.DatabaseConfig;


/**
 * 데이터베이스 Connection Pool을 관리하는 싱글톤 클래스
 * 
 * <p>
 * 이 클래스는 MySQL 데이터베이스 연결을 효율적으로 관리하기 위한 Connection Pool을 제공한다.
 * 매번 새로운 연결을 생성하는 오버헤드를 줄이고, 연결을 재사용하여 성능을 최적화한다.
 * 싱글톤 패턴을 사용하여 애플리케이션 전체에서 하나의 풀만 유지한다.
 * </p>
 * 
 * <h3>주요 기능</h3>
 * <ul>
 *   <li><strong>Connection Pool 관리:</strong> 초기 5개, 최대 20개의 연결 유지</li>
 *   <li><strong>자동 연결 생성:</strong> 필요 시 최대 크기까지 동적으로 연결 생성</li>
 *   <li><strong>연결 상태 검증:</strong> 유효하지 않은 연결 자동 교체</li>
 *   <li><strong>스레드 안전:</strong> synchronized로 동시성 제어</li>
 *   <li><strong>Graceful Shutdown:</strong> 모든 연결 안전하게 종료</li>
 * </ul>
 * 
 * <h3>Connection Pool 구조</h3>
 * <pre>
 * ConnectionManager (싱글톤)
 * ├─ availableConnections (사용 가능한 연결)
 * │  └─ Connection 1, 2, 3, 4, 5 (초기)
 * ├─ usedConnections (사용 중인 연결)
 * │  └─ 애플리케이션에서 사용 중
 * └─ 설정
 *    ├─ INITIAL_POOL_SIZE: 5
 *    └─ MAX_POOL_SIZE: 20
 * </pre>
 * 
 * <h3>동작 방식</h3>
 * <pre>
 * 1. 애플리케이션 시작
 *    └─ getInstance() → ConnectionManager 생성
 *       └─ initializePool() → 5개 연결 미리 생성
 * 
 * 2. 연결 요청 (QueryExecutor 등)
 *    └─ getConnection()
 *       ├─ availableConnections에서 하나 꺼냄
 *       ├─ usedConnections로 이동
 *       └─ Connection 반환
 * 
 * 3. 연결 반납
 *    └─ releaseConnection(conn)
 *       ├─ usedConnections에서 제거
 *       └─ availableConnections로 이동
 * 
 * 4. Pool 부족 시
 *    └─ getConnection()
 *       ├─ availableConnections 비어있음
 *       ├─ usedConnections < MAX_POOL_SIZE 확인
 *       └─ 새 연결 생성 (최대 20개까지)
 * 
 * 5. 애플리케이션 종료
 *    └─ shutdown()
 *       └─ 모든 연결 닫기
 * </pre>
 * 
 * <h3>주의사항</h3>
 * <ul>
 *   <li>getConnection() 후 반드시 releaseConnection() 호출 필수</li>
 *   <li>finally 블록에서 연결 반납하여 누수 방지</li>
 *   <li>MAX_POOL_SIZE 초과 시 SQLException 발생</li>
 *   <li>장시간 연결을 점유하지 말 것 (다른 요청 블로킹)</li>
 * </ul>
 * 
 * @author bang9634
 * @since 2025-11-18
 * 
 * @see DatabaseConfig
 * @see QueryExecutor
 */
public class ConnectionManager {
    private static final Logger logger = LoggerFactory.getLogger(ConnectionManager.class);
    
    /**
     * 싱글톤 인스턴스
     * 
     * <p>
     * 애플리케이션 전체에서 하나의 ConnectionManager만 존재하도록 보장한다.
     * getInstance()를 통해서만 접근 가능하다.
     * </p>
     */
    private static ConnectionManager instance;

    private final DatabaseConfig databaseConfig;

    /**
     * 사용 가능한(대기 중인) 연결 목록
     * <p>
     * 현재 사용되지 않고 있어서 즉시 할당 가능한 Connection들을 보관한다.
     * getConnection() 호출 시 여기서 꺼내서 usedConnections로 이동한다.
     * </p>
     */
    private final List<Connection> availableConnections;

    /**
     * 현재 사용 중인 연결 목록
     * <p>
     * 애플리케이션에서 현재 사용하고 있는 Connection들을 추적한다.
     * releaseConnection() 호출 시 availableConnections로 돌아간다.
     * </p>
     */
    private final List<Connection> usedConnections;
    
    /**
     * 초기 Connection Pool 크기
     * <p>
     * 애플리케이션 시작 시 미리 생성하는 연결의 개수.
     * 대부분의 요청을 처리하기에 충분한 기본값이다.
     * </p>
     */
    private static final int INITIAL_POOL_SIZE = 5;

    /**
     * 최대 Connection Pool 크기
     * <p>
     * 동시에 유지할 수 있는 최대 연결 개수.
     * 이 값을 초과하면 SQLException이 발생한다.
     * MySQL의 max_connections 설정보다 작아야 한다.
     * </p>
     */
    private static final int MAX_POOL_SIZE = 20;

    /**
     * private 생성자로 외부 인스턴스 생성 방지 (싱글톤 패턴)
     * 
     * <p>
     * DatabaseConfig를 로드하고 Connection Pool을 초기화한다.
     * 초기화 실패 시 RuntimeException을 발생시켜 애플리케이션 시작을 중단한다.
     * </p>
     * 
     * @throws RuntimeException 다음과 같은 경우 발생:
     *         <ul>
     *         <li>MySQL JDBC Driver를 찾을 수 없음</li>
     *         <li>MySQL 서버에 연결할 수 없음</li>
     *         <li>인증 실패 (잘못된 username/password)</li>
     *         <li>데이터베이스가 존재하지 않음</li>
     *         </ul>
     * 
     * @see #initializePool()
     */
    private ConnectionManager() {
        this.databaseConfig = new DatabaseConfig();
        this.availableConnections = new ArrayList<>(INITIAL_POOL_SIZE);
        this.usedConnections = new ArrayList<>();
        
        initializePool();
    }

    /**
     * 의존성 주입 생성자 (테스트용)
     * 
     * <p>
     * 싱글톤을 사용하지 않고 직접 인스턴스를 생성한다.
     * 테스트에서 H2 같은 다른 DB를 사용할 때 유용하다.
     * </p>
     * 
     * <pre>{@code
     * // 테스트 예시
     * TestDatabaseConfig testConfig = new TestDatabaseConfig();
     * ConnectionManager testManager = new ConnectionManager(testConfig);
     * QueryExecutor executor = new QueryExecutor(testManager);
     * }</pre>
     * 
     * @param databaseConfig 데이터베이스 설정
     */
    private ConnectionManager(DatabaseConfig databaseConfig) {
        this.databaseConfig = databaseConfig;
        this.availableConnections = new ArrayList<>(INITIAL_POOL_SIZE);
        this.usedConnections = new ArrayList<>();
        
        initializePool();
    }


    /**
     * ConnectionManager의 싱글톤 인스턴스를 반환한다.
     * 
     * <p>
     * 첫 호출 시 ConnectionManager를 생성하고, 이후 호출에서는 동일한 인스턴스를 반환한다.
     * 스레드 안전성을 위해 synchronized 키워드를 사용한다.
     * </p>
     * 
     * <h3>사용 예시</h3>
     * <pre>{@code
     * // QueryExecutor에서
     * public class QueryExecutor {
     *     private final ConnectionManager connectionManager;
     *     
     *     public QueryExecutor() {
     *         this.connectionManager = ConnectionManager.getInstance();
     *     }
     * }
     * 
     * // UserDAO에서
     * ConnectionManager manager = ConnectionManager.getInstance();
     * Connection conn = manager.getConnection();
     * }</pre>
     * 
     * @return ConnectionManager 싱글톤 인스턴스 (항상 동일한 객체)
     * 
     * @throws RuntimeException 첫 초기화 시 Connection Pool 생성 실패
     * 
     * @apiNote 스레드 안전: 여러 스레드에서 동시 호출 가능
     * @implNote Double-Checked Locking이 아닌 단순 synchronized 사용
     */
    public static synchronized ConnectionManager getInstance() {
        if (instance == null) {
            instance = new ConnectionManager();
        }
        return instance;
    }
    /**
     * 싱글톤 인스턴스를 특정 설정으로 초기화한다 (프로덕션용)
     * 
     * <p>
     * 첫 번째 호출에서만 설정이 적용된다.
     * 이미 초기화된 경우 기존 인스턴스를 반환하며 설정은 무시된다.
     * </p>
     * 
     * @param databaseConfig 데이터베이스 설정
     * @return 싱글톤 ConnectionManager 인스턴스
     */
    public static synchronized ConnectionManager getInstance(DatabaseConfig databaseConfig) {
        if (instance == null) {
            logger.info("싱글톤 ConnectionManager 생성 (커스텀 설정)");
            instance = new ConnectionManager(databaseConfig);
        } else {
            logger.warn("ConnectionManager가 이미 초기화됨. 기존 인스턴스 반환");
        }
        return instance;
    }

     /**
     * 싱글톤 인스턴스를 재설정한다 (테스트/재시작용)
     * 
     * <p>
     * 기존 싱글톤 인스턴스를 종료하고 새로운 설정으로 재생성한다.
     * 주의: 프로덕션에서는 사용하지 말 것!
     * </p>
     * 
     * @param databaseConfig 새 데이터베이스 설정
     * @return 새로운 싱글톤 인스턴스
     * @throws SQLException 기존 인스턴스 종료 실패 시
     */
    public static synchronized ConnectionManager resetInstance(DatabaseConfig databaseConfig) 
            throws SQLException {
        if (instance != null) {
            logger.info("기존 싱글톤 인스턴스 종료");
            instance.shutdown();
            instance = null;
        }
        logger.info("새 싱글톤 인스턴스 생성");
        instance = new ConnectionManager(databaseConfig);
        return instance;
    }

    /**
     * Connection Pool을 초기화하고 초기 연결들을 미리 생성한다.
     * 
     * <p>
     * MySQL JDBC Driver를 로드하고, INITIAL_POOL_SIZE만큼의 Connection을 미리 생성하여
     * availableConnections에 저장한다. 애플리케이션 시작 시 한 번만 실행된다.
     * </p>
     * 
     * <h3>초기화 단계</h3>
     * <ol>
     *   <li>MySQL JDBC Driver 로드 (com.mysql.cj.jdbc.Driver)</li>
     *   <li>MySQL 서버 연결 테스트</li>
     *   <li>초기 5개 연결 생성 및 Pool에 추가</li>
     *   <li>초기화 완료 로그 출력</li>
     * </ol>
     * 
     * <h3>로그 출력 예시</h3>
     * <pre>
     * Connection Pool 초기화 시작...
     * JDBC URL: jdbc:mysql://localhost:3306/planp
     * Username: planp_user
     * MySQL JDBC Driver 로드 성공
     * MySQL 연결 테스트 중...
     * MySQL 연결 성공
     * Connection Pool 초기화: 5 개 연결
     * </pre>
     * 
     * <h3>오류 시 로그 예시</h3>
     * <pre>
     * MySQL 연결 실패
     * 연결 정보:
     *   - Host: localhost
     *   - Port: 3306
     *   - Database: planp
     *   - Username: planp_user
     *   - Password: ****
     * SQL State: 08001
     * Error Code: 0
     * Message: Communications link failure
     * </pre>
     * 
     * @throws RuntimeException 다음과 같은 경우 발생:
     *         <ul>
     *           <li><strong>ClassNotFoundException:</strong> MySQL JDBC Driver 미설치</li>
     *           <li><strong>SQLException (08001):</strong> MySQL 서버 연결 불가</li>
     *           <li><strong>SQLException (28000):</strong> 인증 실패</li>
     *           <li><strong>SQLException (42000):</strong> 데이터베이스 없음</li>
     *         </ul>
     * 
     * @apiNote private 메서드로 생성자에서만 호출됨
     * @implNote 테스트 연결 성공 후 초기 연결 생성
     * 
     * @see #createConnection()
     */
    private void initializePool() {
        logger.info("Connection Pool 초기화 시작...");
        logger.info("JDBC URL: {}", databaseConfig.getJdbcUrl());
        logger.info("Username: {}", databaseConfig.getUsername());
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            logger.info("MySQL JDBC Driver 로드 성공");

            logger.info("MySQL 연결 테스트 중...");
            Connection testConn = createConnection();
            logger.info("MySQL 연결 성공");
            availableConnections.add(testConn);
            
            for (int i = 0; i < INITIAL_POOL_SIZE; i++) {
                availableConnections.add(createConnection());
            }
            logger.info("Connection Pool 초기화: {} 개 연결", INITIAL_POOL_SIZE);
        } catch (SQLException e) {
            logger.error("MySQL 연결 실패", e);
            logger.error("연결 정보:");
            logger.error("  - Host: {}", databaseConfig.getHost());
            logger.error("  - Port: {}", databaseConfig.getPort());
            logger.error("  - Database: {}", databaseConfig.getDatabase());
            logger.error("  - Username: {}", databaseConfig.getUsername());
            logger.error("  - Password: {}", databaseConfig.getPassword().isEmpty() ? "(empty)" : "****");
            logger.error("SQL State: {}", e.getSQLState());
            logger.error("Error Code: {}", e.getErrorCode());
            logger.error("Message: {}", e.getMessage());
            throw new RuntimeException("Connection pool initialization failed", e);
        } catch (ClassNotFoundException e) {
            logger.error("MySQL JDBC Driver를 찾을 수 없음", e);
            throw new RuntimeException("MySQL JDBC Driver not found", e);
        }
    }

    /**
     * 새로운 MySQL Connection을 생성한다.
     * 
     * <p>
     * DriverManager를 사용하여 DatabaseConfig의 설정으로 MySQL 서버에 연결한다.
     * Pool 초기화 또는 동적 연결 생성 시 호출된다.
     * </p>
     * 
     * <h3>연결 파라미터</h3>
     * <pre>
     * URL: jdbc:mysql://localhost:3306/planp?
     *      useSSL=false&
     *      serverTimezone=Asia/Seoul&
     *      allowPublicKeyRetrieval=true
     * User: planp_user
     * Password: ****
     * </pre>
     * 
     * @return 새로 생성된 MySQL Connection
     * 
     * @throws SQLException 다음과 같은 경우 발생:
     *         <ul>
     *         <li>MySQL 서버에 연결할 수 없음</li>
     *         <li>인증 실패</li>
     *         <li>데이터베이스가 존재하지 않음</li>
     *         <li>네트워크 오류</li>
     *         </ul>
     * 
     * @apiNote private 메서드로 내부에서만 사용
     * @implNote DriverManager.getConnection() 직접 호출
     */
    private Connection createConnection() throws SQLException {
        return DriverManager.getConnection(
            databaseConfig.getJdbcUrl(),
            databaseConfig.getUsername(),
            databaseConfig.getPassword()
        );
    }


    /**
     * Connection Pool에서 사용 가능한 연결을 가져온다.
     * 
     * <p>
     * availableConnections에서 하나를 꺼내 usedConnections로 이동시킨 후 반환한다.
     * Pool이 비어있으면 MAX_POOL_SIZE까지 새 연결을 생성한다.
     * 반환받은 연결은 반드시 releaseConnection()으로 반납해야 한다.
     * </p>
     * 
     * <h3>동작 흐름</h3>
     * <pre>
     * 1. availableConnections 확인
     *    ├─ 연결 있음 → 꺼내서 반환
     *    └─ 연결 없음
     *       ├─ usedConnections < MAX_POOL_SIZE → 새 연결 생성
     *       └─ usedConnections >= MAX_POOL_SIZE → SQLException
     * 
     * 2. 연결 유효성 검사
     *    ├─ connection.isValid(2) == true → 정상
     *    └─ connection.isValid(2) == false → 새 연결 생성
     * 
     * 3. usedConnections에 추가 및 반환
     * </pre>
     * 
     * <h3>사용 예시</h3>
     * <pre>{@code
     * ConnectionManager manager = ConnectionManager.getInstance();
     * Connection conn = null;
     * 
     * try {
     *     // 연결 획득
     *     conn = manager.getConnection();
     *     
     *     // DB 작업
     *     PreparedStatement pstmt = conn.prepareStatement("SELECT * FROM users");
     *     ResultSet rs = pstmt.executeQuery();
     *     // ...
     *     
     * } catch (SQLException e) {
     *     logger.error("DB 작업 실패", e);
     * } finally {
     *     // 연결 반납 (필수!)
     *     if (conn != null) {
     *         manager.releaseConnection(conn);
     *     }
     * }
     * }</pre>
     * 
     * <h3>로그 출력 예시</h3>
     * <pre>
     * # 정상 케이스
     * 연결 할당 (사용 중: 3, 대기: 2)
     * 
     * # Pool 확장
     * 새 연결 생성 (총: 6)
     * 연결 할당 (사용 중: 6, 대기: 0)
     * 
     * # 유효하지 않은 연결
     * Invalid connection detected, creating new one
     * 연결 할당 (사용 중: 3, 대기: 2)
     * </pre>
     * 
     * @return 사용 가능한 MySQL Connection
     * 
     * @throws SQLException 다음과 같은 경우 발생:
     *         <ul>
     *         <li><strong>Pool 고갈:</strong> 최대 크기(20개) 초과
     *             <pre>
     *             원인: 연결을 반납하지 않음 (releaseConnection 누락)
     *             해결: finally 블록에서 반드시 releaseConnection 호출
     *             </pre>
     *         </li>
     *         <li><strong>연결 생성 실패:</strong> MySQL 서버 문제
     *             <pre>
     *             원인: MySQL 서버 중지, 네트워크 오류
     *             해결: MySQL 서버 상태 확인
     *             </pre>
     *         </li>
     *         <li><strong>유효성 검사 실패:</strong> 연결 끊김
     *             <pre>
     *             원인: MySQL 타임아웃, 네트워크 불안정
     *             해결: 자동으로 새 연결 생성 (내부 처리)
     *             </pre>
     *         </li>
     *         </ul>
     * 
     * @apiNote synchronized로 스레드 안전 보장
     * @implNote connection.isValid(2)로 2초 내 유효성 검사
     * 
     * @see #releaseConnection(Connection)
     * @see #createConnection()
     */
    public synchronized Connection getConnection() throws SQLException {
        if (availableConnections.isEmpty()) {
            if (usedConnections.size() < MAX_POOL_SIZE) {
                availableConnections.add(createConnection());
                logger.debug("새 연결 생성 (총: {})", usedConnections.size() + 1);
            } else {
                throw new SQLException("Maximum pool size reached!");
            }
        }

        Connection connection = availableConnections.remove(availableConnections.size() - 1);
        
        if (!connection.isValid(2)) {
            logger.warn("Invalid connection detected, creating new one");
            connection = createConnection();
        }
        
        usedConnections.add(connection);
        logger.debug("연결 할당 (사용 중: {}, 대기: {})", 
            usedConnections.size(), 
            availableConnections.size()
        );
        return connection;
    }

    /**
     * 사용이 끝난 Connection을 Pool에 반납한다.
     * 
     * <p>
     * usedConnections에서 제거하고 availableConnections로 이동시킨다.
     * 연결을 닫지 않고 재사용을 위해 보관한다.
     * getConnection() 호출 후 반드시 호출해야 한다.
     * </p>
     * 
     * <h3>동작 흐름</h3>
     * <pre>
     * 1. usedConnections에서 connection 제거
     *    ├─ 성공 → availableConnections에 추가
     *    └─ 실패 → 아무 작업 안 함 (이미 반납됨)
     * 
     * 2. 다른 요청에서 재사용 가능
     * </pre>
     * 
     * <h3>사용 예시</h3>
     * <pre>{@code
     * ConnectionManager manager = ConnectionManager.getInstance();
     * Connection conn = null;
     * 
     * try {
     *     conn = manager.getConnection();
     *     
     *     // DB 작업
     *     Statement stmt = conn.createStatement();
     *     stmt.executeUpdate("INSERT INTO users ...");
     *     
     * } finally {
     *     // 반드시 finally에서 반납!
     *     if (conn != null) {
     *         manager.releaseConnection(conn);
     *     }
     * }
     * 
     * // 나쁜 예시 (누수 발생)
     * Connection conn = manager.getConnection();
     * // ... DB 작업
     * // releaseConnection 호출 안 함 → Pool 고갈!
     * }</pre>
     * 
     * <h3>주의사항</h3>
     * <ul>
     *   <li>반드시 finally 블록에서 호출하여 예외 발생 시에도 반납 보장</li>
     *   <li>동일한 연결을 두 번 반납하지 말 것 (무시됨)</li>
     *   <li>Connection을 close()하지 말 것 (Pool에서 관리)</li>
     * </ul>
     * 
     * @param connection 반납할 Connection (null이면 무시)
     * 
     * @apiNote synchronized로 스레드 안전 보장
     * @implNote 이미 반납된 연결은 조용히 무시
     * 
     * @see #getConnection()
     */
    public synchronized void releaseConnection(Connection connection) {
        if (usedConnections.remove(connection)) {
            availableConnections.add(connection);
        }
    }


    /**
     * Connection Pool을 종료하고 모든 연결을 닫는다.
     * 
     * <p>
     * 애플리케이션 종료 시 호출하여 모든 MySQL 연결을 정리한다.
     * 사용 중인 연결과 대기 중인 연결을 모두 닫고 Pool을 비운다.
     * Graceful shutdown을 위해 shutdown hook에서 호출된다.
     * </p>
     * 
     * <h3>종료 단계</h3>
     * <ol>
     *   <li>usedConnections의 모든 연결 닫기</li>
     *   <li>availableConnections의 모든 연결 닫기</li>
     *   <li>두 리스트 비우기</li>
     *   <li>종료 완료 로그 출력</li>
     * </ol>
     * 
     * <h3>로그 출력 예시</h3>
     * <pre>
     * Shutting down connection pool...
     * Connection pool shutdown complete
     * </pre>
     * 
     * <h3>사용 예시</h3>
     * <pre>{@code
     * // Main.java의 shutdown hook에서
     * Runtime.getRuntime().addShutdownHook(new Thread(() -> {
     *     try {
     *         ConnectionManager manager = ConnectionManager.getInstance();
     *         manager.shutdown();
     *         System.out.println("✅ Connection Pool 종료 완료");
     *     } catch (SQLException e) {
     *         System.err.println("❌ Connection Pool 종료 중 오류");
     *     }
     * }));
     * 
     * // 또는 명시적 종료
     * public static void main(String[] args) {
     *     try {
     *         // 애플리케이션 실행
     *         server.start();
     *     } finally {
     *         // 종료 시 Pool 정리
     *         ConnectionManager.getInstance().shutdown();
     *     }
     * }
     * }</pre>
     * 
     * <h3>주의사항</h3>
     * <ul>
     *   <li>한 번 shutdown하면 새 연결 생성 불가 (getInstance 재호출 필요)</li>
     *   <li>사용 중인 연결도 강제 종료되므로 작업 완료 후 호출</li>
     *   <li>shutdown 후 getConnection() 호출 시 NullPointerException</li>
     * </ul>
     * 
     * @throws SQLException Connection을 닫는 중 오류 발생 시
     *         <ul>
     *         <li>이미 닫힌 연결 (무시 가능)</li>
     *         <li>네트워크 오류 (무시 가능)</li>
     *         </ul>
     * 
     * @apiNote 애플리케이션 종료 시 한 번만 호출
     * @implNote 오류 발생해도 모든 연결 닫기 시도
     * 
     * @see Runtime#addShutdownHook(Thread)
     */
    public void shutdown() throws SQLException {
        logger.info("Shutting down connection pool...");
        
        for (Connection conn : usedConnections) {
            conn.close();
        }
        for (Connection conn : availableConnections) {
            conn.close();
        }
        
        usedConnections.clear();
        availableConnections.clear();
        
        logger.info("Connection pool shutdown complete");
    }
}
