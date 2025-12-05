package com.drhong;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.drhong.config.EnvironmentConfig;
import com.drhong.context.ApplicationContext;
import com.drhong.database.DatabaseInitializer;
import com.drhong.server.PlanPServer;

/**
 * PlanP 백엔드 애플리케이션의 메인 엔트리 포인트 클래스
 * <p>
 * 이 클래스는 PlanP 백엔드 서버의 시작점 역할을 담당한다.
 * 시스템 초기화, 설정 로딩, 서버 생성 및 시작을 관리하며,
 * </p>
 * 
 * <h3>실행 방법:</h3>
 * <pre>{@code
 * # 기본 설정으로 실행
 * java -jar planp-backend.jar
 * 
 * # 커스텀 포트로 실행
 * java -jar planp-backend.jar [포트번호] [호스트주소]
 * 
 * # Linux Ubuntu 환경에서 백그라운드로 실행
 * nohup java -jar planp-backend.jar &  # nohup.out 생성 및 로그 기록
 * 
 * # 환경변수로 설정
 * export PLANP_HOST=0.0.0.0
 * export PLANP_PORT=8080
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
     * <p>
     * 애플리케이션 실행 시, 다음과 같은 우선순위로 포트 번호와
     * 호스트 주소를 결정한다.
     * 1. 커맨드라인 인수 > 2. 환경변수 > 3. 기본값
     * 커맨드라인 인수가 잘못된 값이면 예외를 던진다.
     * 커맨드라인 인수 사용법은 아래에 후술한다.
     * </p>
     * 
     * <h3>종료 코드:</h3>
     * <ul>
     *   <li><strong>0:</strong> 정상 종료</li>
     *   <li><strong>1:</strong> 서버 시작 실패</li>
     * </ul>
     * 
     * @param args 커맨드라인 인수 배열 (선택적 포트 번호 포함)
     * 
     * @exception IllegalArgumentException 포트 번호 및 호스트주소가 유효하지않거나,
     *                                     데이터 베이스 연결 설정이 잘못된 경우
     * 
     * @exception SecurityException 프로그램의 권한이 부족한 경우
     * 
     * @exception IOException 포트가 이미 사용 중 혹은 호스트 주소를 찾을 수 없거나
     *                        서버 소켓 생성 실패, 네트워크 인터페이스가 사용 불가능한 경우
     * 
     * @exception RuntimeException 데이터베이스 초기화 실패 (MySQL 연결 불가) 또는
     *                             서비스 초기화 실패, 예상지 못한 시스템 오류가 발생하는 경우
     * 
     * @apiNote 이 메서드는 블로킹되며, 서버가 종료될 때까지 반환되지 않음
     */
    public static void main(String[] args) {
        // 시작 배너 출력
        System.out.println(STARTUP_BANNER);
        System.out.println("PlanP 백엔드 서버 초기화 중...\n");

        ApplicationContext applicationContext;
        
        try {
            // 환경 설정 출력
            EnvironmentConfig.printConfig();

            // 호스트 및 포트 설정 (프로그램 인수 또는 환경 변수로 변경 가능)
            String host = getHostFromArgs(args);
            int port = getPortFromArgs(args);
            
            logger.info("서버 시작: {}:{}", host, port);
            
            System.out.printf("서버 설정:\n");
            System.out.printf("├─ 호스트: %s\n", host);
            System.out.printf("├─ 포트: %d\n", port);
            System.out.printf("└─  허용 오리진: %s\n",
                String.join(", ", EnvironmentConfig.getAllowedOrigins()));

            applicationContext = initializeContext();
            
            // HTTP 서버 생성 및 시작
            System.out.println("\nHTTP 서버 생성 중...");
            PlanPServer server = new PlanPServer.Builder()
                .host(host).port(port)
                .allowedOrigins(Arrays.asList(EnvironmentConfig.getAllowedOrigins()))
                .addPublicRoute("/health", applicationContext.getHealthCheckHandler())
                .addProtectedRoute("/api/users", applicationContext.getUserHandler(), applicationContext.getAuthenticationFilter())
                .build();


            System.out.println("\nHTTP 서버 생성 완료");

            // Shutdown Hook 등록 (Graceful Shutdown)
            registerShutdownHook(server, applicationContext);
            
            System.out.printf("\n서버가 http://%s:%d 에서 시작되었습니다\n", host, port);
            System.out.println("Health Check: http://" + host + ":" + port + "/health");
            System.out.println("API Endpoint: http://" + host + ":" + port + "/api/");
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
     * 의존성을 초기화하는 메서드
     * 
     * <p>
     * 의존성 주입이 필요한 클래스들을 초기화한다.
     * </p>
     * @return 의존성 주입된 클래스
     */
    private static ApplicationContext initializeContext() {
        System.out.println("\n=== 의존성 초기화 시작 ===\n");
    
        try {
            // Dependencies 생성 (내부에서 모든 의존성 초기화)
            ApplicationContext context = new ApplicationContext();
            
            // 데이터베이스 초기화
            System.out.println("데이터베이스 초기화 중...");
            DatabaseInitializer dbInit = new DatabaseInitializer(context.getDatabaseConfig());
            dbInit.initialize();
            System.out.println("✓ 데이터베이스 초기화 완료\n");
            
            System.out.println("=== 의존성 초기화 완료 ===\n");
            
            return context;
            
        } catch (Exception e) {
            logger.error("의존성 초기화 중 오류 발생", e);
            System.err.println("\n의존성 초기화 실패: " + e.getMessage());
            throw new RuntimeException("Dependencies initialization failed", e);
        }

    }
    
    /**
     * 포트 번호를 결정하는 메서드
     * <p>
     * 우선순위에 따라 포트 번호를 결정한다:
     * 1. 커맨드라인 인수 > 2. 환경변수 > 3. 기본값
     * 잘못된 포트 번호가 제공된 경우 경고 메시지와 함께 예외를 던진다.
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
     * @exception NumberFormatException 잘못된 포트 번호 형식이 인자로 들어오는 경우
     * 
     * @apiNote 시스템 포트(1-1023) 사용 시 관리자 권한이 필요할 수 있음
     * @apiNote 프론트엔드와 동일한 포트 사용 시 충돌 발생할 수 있음
     */
    private static int getPortFromArgs(String[] args) {
        // 1. 프로그램 인수에서 포트 확인
        if (args.length > 0) {
            try {
                int port = Integer.parseInt(args[0]);
                validatePort(port);

                // 시스템 포트 경고
                if (port < 1024) {
                    System.out.println("시스템 포트 사용: " + port + " (관리자 권한 필요할 수 있음)");
                }

                System.out.println("커맨드라인 포트 사용: " + port);
                return port;

            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("잘못된 포트 번호 형식: " + args[0]);
            }
        }
        
        // EnvironmentConfig에서 포트 가져오기 (환경변수 + 기본값 처리)
        int envPort = EnvironmentConfig.getPort();

        // 환경변수와 기본값 구분해서 로깅
        String portSource = System.getenv("PLANP_PORT");
        if (portSource != null) {
            System.out.println("환경변수 포트 사용: " + envPort + " (PLANP_PORT)");
        } else {
            System.out.println("기본 포트 사용: " + envPort);
        }
        
        return envPort;
    }
        
    /**
     * 호스트 주소를 결정하는 메서드
     * <p>
     * 우선순위에 따라 호스트 주소를 결정한다:
     * 1. 커맨드라인 인수 > 2. 환경변수 > 3. 기본값
     * 기본값으로 localhost를 사용하며, 프로덕션에서는 환경변수 설정으로 변경 가능하다.
     * </p>
     * 
     * <h4>커맨드라인 사용법:</h4>
     * <pre>{@code
     * java -jar planp-backend.jar [포트] [호스트]
     * 
     * 예시:
     * java -jar planp-backend.jar 8080 localhost
     * java -jar planp-backend.jar 3000 0.0.0.0
     * }</pre>
     * 
     * @param args 커맨드라인 인수 배열 (두 번째 인수로 호스트 지정 가능)
     * @return 사용할 호스트 주소 문자열
     */
    private static String getHostFromArgs(String[] args) {
        // 커맨드라인 인수에서 호스트 확인 (두 번째 인수)
        if (args.length > 1) {
            String host = args[1].trim();
            
            if (!host.isEmpty()) {
                validateHost(host);
                
                // 보안 경고 표시
                if ("0.0.0.0".equals(host)) {
                    System.out.println("모든 IP에서 접속 허용: " + host + " (보안 주의!)");
                }
                
                System.out.println("커맨드라인 호스트 사용: " + host);
                return host;
            }
        }

        // EnvironmentConfig에서 호스트 가져오기 (환경변수 + 기본값 처리)
        String envHost = EnvironmentConfig.getHost();
        
        // 환경변수와 기본값 구분해서 로깅
        String hostSource = System.getenv("PLANP_HOST");
        if (hostSource != null) {
            if ("0.0.0.0".equals(envHost)) {
                System.out.println("환경변수 호스트 - 모든 IP 허용: " + envHost + " (PLANP_HOST)");
            } else {
                System.out.println("환경변수 호스트 사용: " + envHost + " (PLANP_HOST)");
            }
        } else {
            System.out.println("기본 호스트 사용: " + envHost + " (로컬 접속만)");
        }

        return envHost;
    }

    /**
     * 포트 번호의 유효성을 검증한다.
     * <p>
     * 포트번호가 1-65535 범위를 넘어가면 예외를 던진다.
     * </p>
     * @param port 검증할 포트번호
     * 
     * @exception IllegalArgumentException 범위를 벗어난 유효하지 않은 포트 번호인 경우
     */
    private static void validatePort(int port) {
        if (port < 1 || port > 65535) {
            throw new IllegalArgumentException("포트 번호는 1-65535 범위여야 합니다.: " + port);
        }
    }

    /**
     * 호스트 주소의 유효성을 검증한다.
     * <p>
     * 호스트 주소가 비어있거나 공백이 포함되어 있으면
     * 예외를 던진다.
     * </p>
     * @param host 검증할 호스트 주소
     * 
     * @exception IllegalArgumentException 호스트 주소가 비어있거나, 공백이 포함되어 있는 경우
     */
    private static void validateHost(String host) {
        if (host == null || host.trim().isEmpty()) {
            throw new IllegalArgumentException("호스트 주소가 비어있습니다.");
        }

        if (host.contains(" ")) {
            throw new IllegalArgumentException("호스트 주소에 공백이 포함되어 있습니다.: " + host);
        }
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
     * @exception Exception 서버 종류 중 예상치 못한 오류가 발생한 경우
     * 
     * @implNote Runtime.addShutdownHook()을 사용하여 JVM 레벨에서 관리
     */
    private static void registerShutdownHook(PlanPServer server, ApplicationContext context) {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("\n서버 종료 신호 수신...");
            System.out.println("진행 중인 요청 완료 대기 중...");
            
            try {
                server.stop();
                System.out.println("HTTP 서버 종료 완료");

                if (context != null) {
                    context.shutdown();
                    System.out.println("의존성 정리 완료");
                }

                System.out.println("서버가 안전하게 종료되었습니다.");
                System.out.println("PlanP 백엔드를 사용해 주셔서 감사합니다!");
            } catch (SQLException e) {
                System.err.println("의존성 정리 중 오류: " + e.getMessage());
            } catch (Exception e) {
                System.err.println("서버 종료 중 오류: " + e.getMessage());
            }
        }, "shutdown-hook"));
        
        System.out.println("Graceful shutdown 훅 등록 완료");
    }
}