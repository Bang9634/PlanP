package com.drhong.util;

import org.mindrot.jbcrypt.BCrypt;

/**
 * BCrypt 알고리즘을 사용한 비밀번호 보안 처리 유틸리티 클래스
 * <p>
 * 이 클래스는 사용자 비밀번호의 안전한 해싱, 검증, 강도 분석 등을 제공한다.
 * BCrypt는 현재 가장 안전한 비밀번호 해싱 알고리즘 중 하나로, 
 * 솔트(salt) 자동 생성과 적응형 비용 조절을 통해 무차별 대입 공격을 방어한다.
 * </p>
 * 
 * <h3>주요 기능:</h3>
 * <ul>
 *   <li>BCrypt를 이용한 안전한 비밀번호 해싱</li>
 *   <li>평문 비밀번호와 해시의 안전한 검증</li>
 *   <li>비밀번호 강도 분석 및 평가</li>
 *   <li>임시 비밀번호 안전 생성</li>
 *   <li>BCrypt 해시 형식 및 라운드 수 검증</li>
 * </ul>
 * 
 * <h3>보안 특징:</h3>
 * <ul>
 *   <li><strong>솔트 자동 생성:</strong> 레인보우 테이블 공격 방어</li>
 *   <li><strong>적응형 비용:</strong> 하드웨어 발전에 따른 보안 강도 조절</li>
 *   <li><strong>타이밍 공격 방어:</strong> 일정한 처리 시간으로 정보 누출 방지</li>
 *   <li><strong>입력 검증:</strong> null, 빈 문자열 등 예외 상황 처리</li>
 * </ul>
 * 
 * <h3>사용 예시:</h3>
 * <pre>{@code
 * // 회원가입 시 비밀번호 해싱
 * String plainPassword = "mySecurePassword123!";
 * String hashedPassword = PasswordUtil.hash(plainPassword);
 * 
 * // 로그인 시 비밀번호 검증
 * boolean isValid = PasswordUtil.verify(plainPassword, hashedPassword);
 * 
 * // 비밀번호 강도 확인
 * int strength = PasswordUtil.getPasswordStrength("myPassword123!");
 * String strengthText = PasswordUtil.getPasswordStrengthText("myPassword123!");
 * }</pre>
 * 
 * @author bang9634
 * @since 2025-11-10
 * 
 * @implNote 모든 메서드는 static으로 설계되어 인스턴스 생성 없이 사용 가능
 */
public class PasswordUtil {
    
    /**
     * 기본 BCrypt 해싱 라운드 수
     * <p>
     * 보안과 성능의 균형을 고려하여 설정된 값이다.
     * 라운드가 높을수록 보안은 강화되지만 CPU 처리 시간이 증가한다.
     * 현재 하드웨어 성능을 고려하여 적절한 수준으로 설정되었다.
     * </p>
     * 
     * <h4>라운드 수별 예상 처리 시간 (일반적인 서버):</h4>
     * <ul>
     *   <li>10 rounds: ~65ms</li>
     *   <li>12 rounds: ~250ms (권장값)</li>
     *   <li>15 rounds: ~2초</li>
     * </ul>
     * 
     * @implNote 주기적으로 하드웨어 성능 향상에 따라 조정 필요
     */
    private static final int DEFAULT_ROUNDS = 12;

    /**
     * 평문 비밀번호를 BCrypt 알고리즘으로 해싱하는 메서드
     * <p>
     * 기본 라운드 수(12)를 사용하여 비밀번호를 안전하게 해싱한다.
     * 매번 새로운 솔트가 자동으로 생성되므로 같은 비밀번호라도
     * 다른 해시값이 생성된다.
     * </p>
     * 
     * <h4>해싱 과정:</h4>
     * <ol>
     *   <li>입력값 유효성 검증</li>
     *   <li>임의의 솔트 생성 (BCrypt.gensalt)</li>
     *   <li>비밀번호 + 솔트를 12라운드로 해싱</li>
     *   <li>솔트 포함된 해시 문자열 반환</li>
     * </ol>
     * 
     * @param plainPassword 해싱할 평문 비밀번호 (null, 빈 문자열 불가)
     * @return BCrypt 해시 문자열 (솔트 포함, 60자 길이)
     * 
     * @throws IllegalArgumentException 비밀번호가 null이거나 빈 문자열인 경우
     * 
     * @apiNote 반환된 해시는 $2a$12$[솔트][해시] 형태의 60자 문자열
     */
    public static String hash(String plainPassword) {
        if (plainPassword == null || plainPassword.trim().isEmpty()) {
            throw new IllegalArgumentException("비밀번호는 null이거나 빈 문자열일 수 없습니다.");
        }
        
        return BCrypt.hashpw(plainPassword, BCrypt.gensalt(DEFAULT_ROUNDS));
    }

    /**
     * 커스텀 라운드 수로 비밀번호를 해싱하는 메서드
     * <p>
     * 특별한 보안 요구사항이나 성능 고려사항이 있을 때 사용한다.
     * 높은 라운드는 더 강한 보안을 제공하지만 처리 시간이 증가한다.
     * </p>
     * 
     * <h4>라운드 수 선택 가이드:</h4>
     * <ul>
     *   <li><strong>4-8:</strong> 테스트 환경 (빠른 처리)</li>
     *   <li><strong>10-12:</strong> 일반적인 웹 애플리케이션 (권장)</li>
     *   <li><strong>13-15:</strong> 높은 보안이 필요한 시스템</li>
     *   <li><strong>16+:</strong> 극도로 민감한 데이터 (매우 느림)</li>
     * </ul>
     * 
     * @param plainPassword 해싱할 평문 비밀번호
     * @param rounds 해싱 라운드 수 (4-31 사이, BCrypt 제한)
     * @return BCrypt 해시 문자열
     * 
     * @throws IllegalArgumentException 비밀번호가 유효하지 않거나 라운드가 범위를 벗어난 경우
     * 
     * @apiNote 프로덕션에서는 일관된 라운드 사용을 권장
     */
    public static String hash(String plainPassword, int rounds) {
        if (plainPassword == null || plainPassword.trim().isEmpty()) {
            throw new IllegalArgumentException("비밀번호는 null이거나 빈 문자열일 수 없습니다.");
        }
        
        if (rounds < 4 || rounds > 31) {
            throw new IllegalArgumentException("라운드는 4-31 사이여야 합니다.");
        }
        
        return BCrypt.hashpw(plainPassword, BCrypt.gensalt(rounds));
    }

    /**
     * 평문 비밀번호와 해시된 비밀번호를 안전하게 검증하는 메서드
     * <p>
     * BCrypt의 내장 검증 기능을 사용하여 타이밍 공격에 안전하게 설계되었다.
     * 해시에 포함된 솔트와 라운드 정보를 자동으로 추출하여 검증한다.
     * </p>
     * 
     * <h4>검증 과정:</h4>
     * <ol>
     *   <li>null 값 체크</li>
     *   <li>해시에서 솔트와 라운드 정보 추출</li>
     *   <li>평문 비밀번호를 동일한 솔트와 라운드로 해싱</li>
     *   <li>두 해시값의 안전한 비교 (타이밍 공격 방어)</li>
     * </ol>
     * 
     * @param plainPassword 검증할 평문 비밀번호
     * @param hashedPassword 저장된 해시된 비밀번호
     * @return 비밀번호가 일치하면 true, 불일치하거나 오류 시 false
     * 
     * @apiNote null 값이나 잘못된 해시 형식에 대해서도 안전하게 false 반환
     */
    public static boolean verify(String plainPassword, String hashedPassword) {
        if (plainPassword == null || hashedPassword == null) {
            return false;
        }
        
        try {
            return BCrypt.checkpw(plainPassword, hashedPassword);
        } catch (Exception e) {
            // 잘못된 해시 형식이나 기타 BCrypt 오류
            System.err.println("비밀번호 검증 중 오류 발생: " + e.getMessage());
            return false;
        }
    }

    /**
     * 주어진 문자열이 유효한 BCrypt 해시 형식인지 확인하는 메서드
     * <p>
     * BCrypt 해시의 표준 형식을 검증하여 데이터베이스 무결성을 확인한다.
     * 정규표현식을 사용하여 BCrypt 해시의 구조적 유효성을 검사한다.
     * </p>
     * 
     * <h4>BCrypt 해시 형식:</h4>
     * <pre>{@code
     * $2a$12$abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ012345
     * ├─┘├┘├─────────────────────────────────────────────────────────┘
     * │  │  └─ 해시값 (53자)
     * │  └─ 라운드 수 (2자)
     * └─ BCrypt 버전 ($2a$, $2b$, $2x$, $2y$)
     * }</pre>
     * 
     * @param hashedPassword 검증할 해시 문자열
     * @return 유효한 BCrypt 해시면 true, 그렇지 않으면 false
     * 
     * @implNote 이 메서드는 형식만 검증하며 실제 해시 계산 정확성은 확인하지 않음
     */
    public static boolean isValidHash(String hashedPassword) {
        if (hashedPassword == null || hashedPassword.isEmpty()) {
            return false;
        }
        
        // BCrypt 해시는 '$2a$', '$2b$', '$2x$', '$2y$'로 시작하고 60자여야 함
        return hashedPassword.matches("^\\$2[abxy]\\$\\d{2}\\$.{53}$");
    }

    /**
     * BCrypt 해시에서 사용된 라운드 수를 추출하는 메서드
     * <p>
     * 해시 문자열에 인코딩된 라운드 정보를 파싱하여 반환한다.
     * 시스템 업그레이드나 보안 정책 변경 시 기존 해시의 라운드를 확인하는 용도로 사용한다.
     * </p>
     * 
     * <h4>추출 과정:</h4>
     * <ol>
     *   <li>해시 형식 유효성 검증</li>
     *   <li>달러($) 기호로 문자열 분할</li>
     *   <li>세 번째 부분에서 라운드 수 파싱</li>
     *   <li>정수로 변환하여 반환</li>
     * </ol>
     * 
     * @param hashedPassword BCrypt 해시 문자열
     * @return 해시에 사용된 라운드 수, 유효하지 않으면 -1
     * 
     * @apiNote 반환된 라운드 수로 해시 재생성 필요 여부를 판단할 수 있음
     */
    public static int getRounds(String hashedPassword) {
        if (!isValidHash(hashedPassword)) {
            return -1;
        }
        
        try {
            // $2a$12$... 형태에서 12 부분 추출
            String[] parts = hashedPassword.split("\\$");
            return Integer.parseInt(parts[2]);
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    /**
     * 비밀번호의 강도를 수치적으로 평가하는 메서드
     * <p>
     * 다양한 기준을 종합하여 0-100점 사이의 점수로 비밀번호 강도를 평가한다.
     * 회원가입 시 실시간 피드백이나 비밀번호 정책 시행에 활용할 수 있다.
     * </p>
     * 
     * <h4>평가 기준 (총 100점):</h4>
     * <ul>
     *   <li><strong>길이 (25점):</strong> 12자 이상(25점), 8자 이상(20점), 6자 이상(15점), 4자 이상(5점)</li>
     *   <li><strong>소문자 (15점):</strong> a-z 포함 시</li>
     *   <li><strong>대문자 (15점):</strong> A-Z 포함 시</li>
     *   <li><strong>숫자 (15점):</strong> 0-9 포함 시</li>
     *   <li><strong>특수문자 (15점):</strong> !@#$%^&*(),.?":{}|<> 포함 시</li>
     *   <li><strong>다양성 보너스 (10점):</strong> 3종류 이상 조합(10점), 2종류 조합(5점)</li>
     * </ul>
     * 
     * @param password 평가할 비밀번호 (null이나 빈 문자열은 0점)
     * @return 강도 점수 (0-100 사이의 정수)
     * 
     * @see #getPasswordStrengthText(String) 점수를 텍스트로 변환
     */
    public static int getPasswordStrength(String password) {
        if (password == null || password.isEmpty()) {
            return 0;
        }
        
        int score = 0;
        
        // 길이 점수 (최대 25점)
        if (password.length() >= 12) score += 25;
        else if (password.length() >= 8) score += 20;
        else if (password.length() >= 6) score += 15;
        else if (password.length() >= 4) score += 5;
        
        // 소문자 포함 (15점)
        if (password.matches(".*[a-z].*")) score += 15;
        
        // 대문자 포함 (15점)
        if (password.matches(".*[A-Z].*")) score += 15;
        
        // 숫자 포함 (15점)
        if (password.matches(".*\\d.*")) score += 15;
        
        // 특수문자 포함 (15점)
        if (password.matches(".*[!@#$%^&*(),.?\":{}|<>].*")) score += 15;
        
        // 다양한 문자 조합 보너스 (최대 10점)
        int variety = 0;
        if (password.matches(".*[a-z].*")) variety++;
        if (password.matches(".*[A-Z].*")) variety++;
        if (password.matches(".*\\d.*")) variety++;
        if (password.matches(".*[!@#$%^&*(),.?\":{}|<>].*")) variety++;
        
        if (variety >= 3) score += 10;
        else if (variety >= 2) score += 5;
        
        return Math.min(score, 100);
    }

    /**
     * 비밀번호 강도를 사용자 친화적인 텍스트로 변환하는 메서드
     * <p>
     * 수치적 강도 점수를 일반 사용자가 이해하기 쉬운 5단계 텍스트로 변환한다.
     * UI에서 비밀번호 강도를 표시할 때 사용한다.
     * </p>
     * 
     * <h4>강도 분류:</h4>
     * <ul>
     *   <li><strong>80-100점:</strong> "매우 강함" (녹색 표시 권장)</li>
     *   <li><strong>60-79점:</strong> "강함" (파란색 표시 권장)</li>
     *   <li><strong>40-59점:</strong> "보통" (노란색 표시 권장)</li>
     *   <li><strong>20-39점:</strong> "약함" (주황색 표시 권장)</li>
     *   <li><strong>0-19점:</strong> "매우 약함" (빨간색 표시 권장)</li>
     * </ul>
     * 
     * @param password 평가할 비밀번호
     * @return 강도를 나타내는 한국어 텍스트
     * 
     * @see #getPasswordStrength(String) 수치적 강도 점수 계산
     */
    public static String getPasswordStrengthText(String password) {
        int strength = getPasswordStrength(password);
        
        if (strength >= 80) return "매우 강함";
        else if (strength >= 60) return "강함";
        else if (strength >= 40) return "보통";
        else if (strength >= 20) return "약함";
        else return "매우 약함";
    }

    /**
     * 안전한 임시 비밀번호를 생성하는 메서드
     * <p>
     * 계정 복구, 관리자 초기화 등의 상황에서 사용할 임시 비밀번호를 생성한다.
     * 모든 문자 종류(대문자, 소문자, 숫자, 특수문자)를 포함하여 높은 강도를 보장한다.
     * </p>
     * 
     * <h4>생성 과정:</h4>
     * <ol>
     *   <li>각 문자 종류에서 최소 1개씩 선택 (4개)</li>
     *   <li>나머지 자리는 모든 문자에서 랜덤 선택</li>
     *   <li>Fisher-Yates 알고리즘으로 문자 순서 섞기</li>
     *   <li>최종 임시 비밀번호 반환</li>
     * </ol>
     * 
     * <h4>포함되는 문자:</h4>
     * <ul>
     *   <li><strong>대문자:</strong> A-Z (26개)</li>
     *   <li><strong>소문자:</strong> a-z (26개)</li>
     *   <li><strong>숫자:</strong> 0-9 (10개)</li>
     *   <li><strong>특수문자:</strong> !@#$%^&* (8개)</li>
     * </ul>
     * 
     * @param length 생성할 비밀번호 길이 (최소 8자 이상)
     * @return 안전한 임시 비밀번호 문자열
     * 
     * @throws IllegalArgumentException 길이가 8보다 작은 경우
     * 
     * @apiNote 생성된 임시 비밀번호는 반드시 사용자에게 안전한 방법으로 전달해야 함
     */
    public static String generateTemporaryPassword(int length) {
        if (length < 8) {
            throw new IllegalArgumentException("임시 비밀번호는 최소 8자 이상이어야 합니다.");
        }
        
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*";
        StringBuilder password = new StringBuilder();
        
        // 각 카테고리에서 최소 1개씩 포함하여 강도 보장
        password.append("ABCDEFGHIJKLMNOPQRSTUVWXYZ".charAt((int) (Math.random() * 26))); // 대문자
        password.append("abcdefghijklmnopqrstuvwxyz".charAt((int) (Math.random() * 26))); // 소문자
        password.append("0123456789".charAt((int) (Math.random() * 10))); // 숫자
        password.append("!@#$%^&*".charAt((int) (Math.random() * 8))); // 특수문자
        
        // 나머지 길이만큼 전체 문자 집합에서 랜덤 선택
        for (int i = 4; i < length; i++) {
            password.append(chars.charAt((int) (Math.random() * chars.length())));
        }
        
        // Fisher-Yates 알고리즘을 사용한 문자열 섞기
        char[] array = password.toString().toCharArray();
        for (int i = array.length - 1; i > 0; i--) {
            int index = (int) (Math.random() * (i + 1));
            char temp = array[index];
            array[index] = array[i];
            array[i] = temp;
        }
        
        return new String(array);
    }
}