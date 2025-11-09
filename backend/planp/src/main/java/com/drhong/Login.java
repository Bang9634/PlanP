package com.drhong;

// MySQL 데이터베이스 작업에 필요한 클래스들을 가져옵니다.
import java.sql.Connection;           // 데이터베이스 연결을 담당
import java.sql.DriverManager;        // 데이터베이스 연결 관리자
import java.sql.PreparedStatement;    // SQL 쿼리문을 실행하기 위한 객체
import java.sql.ResultSet;           // SQL 쿼리 실행 결과를 담는 객체

/**
 * 로그인 기능을 처리하는 클래스
 * 사용자가 입력한 아이디와 비밀번호를 데이터베이스와 비교하여 로그인을 처리합니다.
 */
public class Login {
    // 데이터베이스 연결에 필요한 정보들
    // TODO: 실제 데이터베이스 정보로 변경해야 합니다!
    private static final String JDBC_URL = "jdbc:mysql://localhost:3306/your_database";  // 데이터베이스 주소
    private static final String DB_USER = "your_username";      // 데이터베이스 사용자 이름
    private static final String DB_PASSWORD = "your_password";  // 데이터베이스 비밀번호
    
    // 로그인 확인을 위한 SQL 쿼리문
    // membership 테이블에서 입력받은 id와 pw가 일치하는 회원을 찾습니다.
    private static final String SQL_LOGIN = "SELECT * FROM membership WHERE id=? AND pw=?";
    
    /**
     * 로그인 처리를 수행하는 메서드
     * @param id 사용자가 입력한 아이디
     * @param password 사용자가 입력한 비밀번호
     * @return 로그인 성공하면 true, 실패하면 false 반환
     */
    public boolean login(String id, String password) {
        // 데이터베이스 작업에 필요한 객체들을 선언합니다.
        Connection conn = null;          // 데이터베이스 연결 객체
        PreparedStatement pstmt = null;  // SQL 실행을 위한 객체
        ResultSet rs = null;            // SQL 실행 결과를 담을 객체
        
        try {
            // 1. MySQL 드라이버 로드
            // MySQL 데이터베이스와 연결하기 위해 필요한 드라이버를 불러옵니다.
            Class.forName("com.mysql.cj.jdbc.Driver");
            
            // 2. 데이터베이스에 연결
            // JDBC_URL, DB_USER, DB_PASSWORD를 사용하여 MySQL 서버에 연결합니다.
            conn = DriverManager.getConnection(JDBC_URL, DB_USER, DB_PASSWORD);
            
            // 3. SQL 쿼리문 준비
            // PreparedStatement를 사용하여 SQL 인젝션 공격을 방지합니다.
            pstmt = conn.prepareStatement(SQL_LOGIN);
            pstmt.setString(1, id);        // 첫 번째 ?에 id 값을 넣습니다.
            pstmt.setString(2, password);  // 두 번째 ?에 password 값을 넣습니다.
            
            // 4. SQL 쿼리 실행 및 결과 확인
            rs = pstmt.executeQuery();     // 준비된 쿼리를 실행하고 결과를 받습니다.
            // rs.next()가 true이면 일치하는 회원이 있다는 뜻(로그인 성공)
            // false이면 일치하는 회원이 없다는 뜻(로그인 실패)
            return rs.next();
            
        } catch (Exception e) {
            // 데이터베이스 작업 중 오류가 발생하면 오류 내용을 출력하고
            // false를 반환하여 로그인 실패로 처리합니다.
            e.printStackTrace();
            return false;
            
        } finally {
            // 5. 사용한 자원들을 정리합니다.
            // 연결을 제대로 닫지 않으면 메모리 누수가 발생할 수 있습니다.
            try {
                // 생성한 순서의 반대 순서로 닫습니다.
                if (rs != null) rs.close();      // 결과셋 닫기
                if (pstmt != null) pstmt.close(); // SQL 문 닫기
                if (conn != null) conn.close();   // 데이터베이스 연결 닫기
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    
    /* 데이터베이스 테이블 생성 SQL문 (관리자가 MySQL에서 실행해야 함)
    CREATE TABLE membership (
        id VARCHAR(50) PRIMARY KEY,  -- 아이디(중복 불가)
        pw VARCHAR(100) NOT NULL     -- 비밀번호
    );
    
    -- 테스트용 계정 추가
    INSERT INTO membership (id, pw) VALUES ('test', '1234');
    */
}


