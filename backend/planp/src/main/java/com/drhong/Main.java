package com.drhong;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.drhong.server.PlanPServer;
import com.drhong.service.UserService;

/**
 * PlanP 백엔드 애플리케이션의 메인 엔트리 포인트 클래스
 * <p>
 * 이 클래스는 PlanP 백엔드 서버의 시작점 역할을 담당한다.
 * 시스템 초기화, 설정 로딩, 서버 생성 및 시작을 관리하며,
 * 다양한 환경(개발, 테스트, 프로덕션)에서의 실행을 지원한다.
 * </p>
 * 
 * <h3>주요 기능:</h3>
 * <ul>
 *   <li>커맨드라인 인수 및 환경변수를 통한 설정 관리</li>
 *   <li>HTTP 서버 초기화 및 의존성 주입</li>
 *   <li>Graceful 서버 시작 및 오류 처리</li>
 *   <li>개발/프로덕션 환경별 설정 적용</li>
 * </ul>
 * 
 * <h3>실행 방법:</h3>
 * <pre>{@code
 * # 기본 설정으로 실행
 * java -jar planp-backend.jar
 * 
 * # 커스텀 포트로 실행
 * java -jar planp-backend.jar 3000
 * 
 * # 환경변수로 설정
 * export PLANP_HOST=0.0.0.0
 * export PLANP_PORT=8080
 * java -jar planp-backend.jar
 * 
 * # Docker 환경
 * docker run -e PLANP_PORT=8080 -p 8080:8080 planp-backend
 * }</pre>
 * 
 * <h3>설정 우선순위:</h3>
 * <ol>
 *   <li>커맨드라인 인수 (최우선)</li>
 *   <li>환경변수 (PLANP_HOST, PLANP_PORT)</li>
 *   <li>기본값 (localhost:8080)</li>
 * </ol>
 * 
 * <h3>종료 방법:</h3>
 * <ul>
 *   <li><strong>일반 종료:</strong> Ctrl+C (SIGTERM)</li>
 *   <li><strong>강제 종료:</strong> Ctrl+\ 또는 kill -9 (권장하지 않음)</li>
 * </ul>
 * 
 * @author bang9634
 * @since 2025-11-10
 * 
 * @see com.drhong.server.PlanPServer
 * @see com.drhong.service.UserService
 * 
 * @implNote JVM 종료 시 자동으로 서버 리소스가 정리되도록 설계됨
 */
public class Main {
    
    /** SLF4J 로거 인스턴스 - 메인 애플리케이션 시작 및 설정 로깅 */
    private static final Logger logger = LoggerFactory.getLogger(Main.class);
    
    /**
     * 기본 포트 번호
     * <p>
     * 웹 애플리케이션에서 일반적으로 사용되는 8080 포트를 기본값으로 설정했다.
     * 프로덕션에서는 80 또는 443 포트 사용을 고려할 수 있으나,
     * 개발 환경에서의 편의성을 위해 8080을 선택했다.
     * </p>
     * 
     * @implNote 포트 1024 이하는 관리자 권한이 필요하므로 개발 시 불편함
     */
    private static final int DEFAULT_PORT = 8080;

    /**
     * 기본 호스트 주소
     * <p>
     * localhost(127.0.0.1)는 로컬 개발 환경에 적합하다.
     * 프로덕션에서는 0.0.0.0으로 설정하여 모든 네트워크 인터페이스에서
     * 접속을 허용할 수 있다.
     * </p>
     * 
     * <h4>호스트 주소 옵션:</h4>
     * <ul>
     *   <li><strong>localhost/127.0.0.1:</strong> 로컬 접속만 허용</li>
     *   <li><strong>0.0.0.0:</strong> 모든 IP에서 접속 허용 (프로덕션)</li>
     *   <li><strong>특정 IP:</strong> 지정된 네트워크 인터페이스만 바인딩</li>
     * </ul>
     */
    private static final String DEFAULT_HOST = "localhost";
    
    /**
     * 서버 시작 메시지 템플릿
     * <p>
     * 사용자에게 시각적으로 명확한 서버 시작 정보를 제공한다.
     * 로그와 구분되는 독립적인 메시지로 빠른 확인이 가능하다.
     * </p>
     */
    private static final String STARTUP_BANNER = """
            ╔══════════════════════════════════════════════════════════════╗
            ║                         PlanP 백엔드                          ║
            ║                     서버 시작 중...                           ║
            ╚══════════════════════════════════════════════════════════════╝
            """;


    /**
     * 애플리케이션의 메인 엔트리 포인트
     * <p>
     * JVM이 애플리케이션을 시작할 때 가장 먼저 호출되는 메서드이다.
     * 시스템 초기화부터 서버 시작까지의 전체 과정을 관리하며,
     * 예외 발생 시 적절한 에러 처리와 시스템 종료를 수행한다.
     * </p>
     * 
     * <h4>실행 단계:</h4>
     * <ol>
     *   <li>시작 배너 출력</li>
     *   <li>설정값 파싱 (포트, 호스트)</li>
     *   <li>의존성 객체 초기화</li>
     *   <li>HTTP 서버 생성</li>
     *   <li>서버 시작 및 대기</li>
     * </ol>
     * 
     * <h4>종료 코드:</h4>
     * <ul>
     *   <li><strong>0:</strong> 정상 종료</li>
     *   <li><strong>1:</strong> 서버 시작 실패</li>
     * </ul>
     * 
     * <h4>커맨드라인 인수:</h4>
     * <pre>{@code
     * java -jar planp.jar [포트번호]
     * 
     * 예시:
     * java -jar planp.jar 3000     # 포트 3000으로 시작
     * java -jar planp.jar          # 기본 포트(8080)로 시작
     * }</pre>
     * 
     * @param args 커맨드라인 인수 배열 (선택적 포트 번호 포함)
     * 
     * @apiNote 이 메서드는 블로킹되며, 서버가 종료될 때까지 반환되지 않음
     */
    public static void main(String[] args) {
        // 시작 배너 출력
        System.out.println(STARTUP_BANNER);
        System.out.println("PlanP 백엔드 서버 초기화 중...\n");
        
        try {
            // 포트 설정 (프로그램 인수 또는 환경 변수로 변경 가능)
            int port = getPort(args);
            String host = getHost(args);
            
            System.out.printf("서버 설정:\n");
            System.out.printf("├─ 호스트: %s\n", host);
            System.out.printf("├─ 포트: %d\n", port);
            System.out.printf("└─ 환경: %s\n\n", getEnvironmentType());

            // 서비스 초기화
            System.out.println("서비스 컴포넌트 초기화...");
            UserService userService = new UserService();
            System.out.println("UserService 초기화 완료");
            
            // HTTP 서버 생성 및 시작
            System.out.println("\nHTTP 서버 생성 중...");
            PlanPServer server = new PlanPServer(host, port, userService);

            // Shutdown Hook 등록 (Graceful Shutdown)
            registerShutdownHook(server);

            
            
            System.out.printf("\n서버가 http://%s:%d 에서 시작되었습니다\n", host, port);
            System.out.println("API 문서: http://" + host + ":" + port + "/health");
            System.out.println("종료하려면 Ctrl+C를 누르세요.\n");
            
            // 서버 시작 (블로킹 - 여기서 프로그램이 대기)
            server.start();
            
        } catch (IllegalArgumentException e) {
            logger.error("서버 시작 중 IllegalArgument 오류 발생", e);
            System.err.println("\n서버 시작 중 IllegalArgument 오류 발생");
            System.err.println("오류 메시지: " + e.getMessage());
            System.err.println("오류 타입: " + e.getClass().getSimpleName());
            logger.error("애플리케이션 비정상 종료 - 종료 코드: 1");
            System.exit(1);
        } catch (SecurityException | IOException e) {
            logger.error("서버 시작 중 Security, IO 오류 발생", e);
            System.err.println("\n서버 시작 중 Security, IO 오류 발생");
            System.err.println("오류 메시지: " + e.getMessage());
            System.err.println("오류 타입: " + e.getClass().getSimpleName());
            logger.error("애플리케이션 비정상 종료 - 종료 코드: 1");
            System.exit(1);
        }  catch (RuntimeException e) {
            logger.error("서버 시작 중 Runtime 오류 발생", e);
            System.err.println("\n서버 시작 중 Runtime 오류 발생");
            System.err.println("오류 메시지: " + e.getMessage());
            System.err.println("오류 타입: " + e.getClass().getSimpleName());
            logger.error("애플리케이션 비정상 종료 - 종료 코드: 1");
            System.exit(1);
        }
    }
    
    /**
     * 포트 번호를 결정하는 메서드
     * <p>
     * 우선순위에 따라 포트 번호를 결정한다:
     * 1. 커맨드라인 인수 > 2. 환경변수 > 3. 기본값
     * 잘못된 포트 번호가 제공된 경우 경고 메시지와 함께 기본값을 사용한다.
     * </p>
     * 
     * <h4>검증 규칙:</h4>
     * <ul>
     *   <li>1-65535 범위의 정수여야 함</li>
     *   <li>1024 미만은 관리자 권한 필요 (경고 표시)</li>
     *   <li>잘못된 형식은 기본값으로 대체</li>
     * </ul>
     * 
     * <h4>환경변수:</h4>
     * <ul>
     *   <li><code>PLANP_PORT</code>: 포트 번호 (예: 3000, 8080)</li>
     * </ul>
     * 
     * @param args 커맨드라인 인수 배열
     * @return 사용할 포트 번호 (1-65535 범위)
     * 
     * @apiNote 시스템 포트(1-1023) 사용 시 관리자 권한이 필요할 수 있음
     */
    private static int getPort(String[] args) {
        // 1. 프로그램 인수에서 포트 확인
        if (args.length > 0) {
            try {
                int port = Integer.parseInt(args[0]);

                // 포트 범위 검증
                if (port < 1 || port > 65535) {
                    System.err.println("포트 범위 오류: " + port + " (1-65535 사용), 기본값 사용");
                    return DEFAULT_PORT;
                }

                // 시스템 포트 경고
                if (port < 1024) {
                    System.out.println("시스템 포트 사용: " + port + " (관리자 권한 필요할 수 있음)");
                }

                System.out.println("커맨드라인 포트 사용: " + port);
                return port;

            } catch (NumberFormatException e) {
                System.err.println("잘못된 포트 번호: " + args[0] + ", 기본값 사용");
            }
        }
        
        // 2. 환경 변수에서 포트 확인
        String envPort = System.getenv("PLANP_PORT");
        if (envPort != null && !envPort.trim().isEmpty()) {
            try {
                int port = Integer.parseInt(envPort.trim());

                if (port < 1 || port > 65535) {
                    System.err.println("환경변수 포트 범위 오류: " + port + ", 기본값 사용");
                    return DEFAULT_PORT;
                }
                
                System.out.println("환경변수 포트 사용: " + port + " (PLANP_PORT)");
                return port;

            } catch (NumberFormatException e) {
                System.err.println("잘못된 환경변수 포트: " + envPort + ", 기본값 사용");
            }
        }
        
        // 3. 기본값 반환
         System.out.println("기본 포트 사용: " + DEFAULT_PORT);
        return DEFAULT_PORT;
    }
        
    /**
     * 호스트 주소를 결정하는 메서드
     * <p>
     * 우선순위에 따라 호스트 주소를 결정한다:
     * 1. 커맨드라인 인수 > 2. 환경변수 > 3. 기본값
     * 보안상 기본적으로 localhost를 사용하며, 프로덕션에서는 설정으로 변경 가능하다.
     * </p>
     * 
     * <h4>커맨드라인 사용법:</h4>
     * <pre>{@code
     * java -jar planp.jar [포트] [호스트]
     * 
     * 예시:
     * java -jar planp.jar 8080 localhost
     * java -jar planp.jar 3000 0.0.0.0
     * }</pre>
     * 
     * @param args 커맨드라인 인수 배열 (두 번째 인수로 호스트 지정 가능)
     * @return 사용할 호스트 주소 문자열
     */
    private static String getHost(String[] args) {
        // 커맨드라인 인수에서 호스트 확인 (두 번째 인수)
        if (args.length > 1) {
            String host = args[1].trim();
            
            if (!host.isEmpty()) {
                // 보안 경고 표시
                if ("0.0.0.0".equals(host)) {
                    System.out.println("모든 IP에서 접속 허용: " + host + " (보안 주의!)");
                }
                
                System.out.println("커맨드라인 호스트 사용: " + host);
                return host;
            }
        }

        // 환경 변수에서 호스트 확인
        String envHost = System.getenv("PLANP_HOST");
        if (envHost != null && !envHost.trim().isEmpty()) {
            String host = envHost.trim();
            
            // 보안 경고 표시
            if ("0.0.0.0".equals(host)) {
                System.out.println("모든 IP에서 접속 허용: " + host + " (보안 주의!)");
            }
            
            System.out.println("환경변수 호스트 사용: " + host + " (PLANP_HOST)");
            return host;
        }
        
        // 기본값 반환
        System.out.println("기본 호스트 사용: " + DEFAULT_HOST + " (로컬 접속만)");
        return DEFAULT_HOST;
    }

    /**
     * 현재 실행 환경 타입을 감지하는 메서드
     * <p>
     * 환경변수나 시스템 속성을 기반으로 현재 실행 환경을 판단한다.
     * 로깅 레벨, 보안 설정, 성능 최적화 등에 활용할 수 있다.
     * </p>
     * 
     * <h4>환경 감지 기준:</h4>
     * <ul>
     *   <li><code>PLANP_ENV=production</code> → 프로덕션</li>
     *   <li><code>PLANP_ENV=test</code> → 테스트</li>
     *   <li>기타 → 개발 환경</li>
     * </ul>
     * 
     * @return 환경 타입 문자열 ("Development", "Test", "Production")
     */
    private static String getEnvironmentType() {
        String env = System.getenv("PLANP_ENV");
        if (env == null) {
            return "Development";
        }
        
        return switch (env.toLowerCase()) {
            case "production", "prod" -> "Production";
            case "test", "testing" -> "Test";
            default -> "Development";
        };
    }

    /**
     * Graceful Shutdown을 위한 JVM 종료 훅을 등록하는 메서드
     * <p>
     * Ctrl+C나 SIGTERM 신호를 받았을 때 서버를 안전하게 종료한다.
     * 진행 중인 요청들이 완료될 때까지 대기한 후 리소스를 정리한다.
     * </p>
     * 
     * <h4>종료 과정:</h4>
     * <ol>
     *   <li>종료 신호 수신</li>
     *   <li>새로운 요청 수신 중단</li>
     *   <li>진행 중인 요청 완료 대기</li>
     *   <li>서버 리소스 정리</li>
     *   <li>종료 완료 메시지 출력</li>
     * </ol>
     * 
     * @param server 종료할 PlanPServer 인스턴스
     * 
     * @implNote Runtime.addShutdownHook()을 사용하여 JVM 레벨에서 관리
     */
    private static void registerShutdownHook(PlanPServer server) {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("\n서버 종료 신호 수신...");
            System.out.println("진행 중인 요청 완료 대기 중...");
            
            try {
                server.stop();
                System.out.println("서버가 안전하게 종료되었습니다.");
                System.out.println("PlanP 백엔드를 사용해 주셔서 감사합니다!");
            } catch (Exception e) {
                System.err.println("서버 종료 중 오류: " + e.getMessage());
            }
        }, "shutdown-hook"));
        
        System.out.println("Graceful shutdown 훅 등록 완료");
    }


}