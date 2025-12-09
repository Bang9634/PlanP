package com.drhong.context;

import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.drhong.config.DatabaseConfig;
import com.drhong.controller.MusicController;
import com.drhong.controller.UserController;
import com.drhong.database.ConnectionManager;
import com.drhong.database.QueryExecutor;
import com.drhong.handler.HealthCheckHandler;
import com.drhong.handler.MusicHandler;
import com.drhong.handler.UserHandler;
import com.drhong.repository.AIUsageRepository;
import com.drhong.repository.UserRepository;
import com.drhong.server.AuthenticationFilter;
import com.drhong.service.AIUsageService;
import com.drhong.service.GoogleOAuthService;
import com.drhong.service.JwtService;
import com.drhong.service.MusicRecommendationService;
import com.drhong.service.UserService;

/**
 * 애플리케이션의 주요 컴포넌트들을 초기화하고 관리하는 컨텍스트 클래스
 * 
 * <p>
 * ApplicationContext는 데이터베이스 설정, 레포지토리, 서비스, 컨트롤러, 핸들러 등
 * 애플리케이션의 핵심 구성 요소들을 생성하고 연결하는 역할을 담당한다.
 * </p>
 * 
 * <h3>주요 기능</h3>
 * <ul>
 *   <li><strong>의존성 주입:</strong> 각 계층 간의 의존성을 관리하고 주입</li>
 *   <li><strong>컴포넌트 초기화:</strong> 데이터베이스 연결, 서비스 로직, 컨트롤러 라우팅 설정</li>
 *   <li><strong>애플리케이션 전반 관리:</strong> 애플리케이션 실행에 필요한 모든 구성 요소 제공</li>
 * </ul>
 * 
 * @author bang9634
 * @since 2025-12-07
 */
public class ApplicationContext{
    protected final Logger logger = LoggerFactory.getLogger(getClass());
    
    private final DatabaseConfig databaseConfig;
    private final ConnectionManager connectionManager;
    private final QueryExecutor queryExecutor;

    private final AuthenticationFilter authenticationFilter;

    private final JwtService jwtService;

    private final HealthCheckHandler healthCheckHandler;

    // OAuth 관련 의존성
    private final GoogleOAuthService googleOAuthService;

    private final AIUsageRepository aiUsageRepository; 
    private final AIUsageService aiUsageService;

    private final UserRepository userRepository;
    private final UserService userService;
    private final UserController userController;
    private final UserHandler userHandler;

    private final MusicRecommendationService musicRecommendationService;
    private final MusicController musicController;
    private final MusicHandler musicHandler;

    public ApplicationContext() {
        logger.debug("의존성 초기화 시작");
        try {
            this.databaseConfig = new DatabaseConfig();
            this.connectionManager = ConnectionManager.getInstance(databaseConfig);
            this.queryExecutor = new QueryExecutor(connectionManager);

            // 레포지토리 계층
            this.userRepository = new UserRepository(queryExecutor);
            this.aiUsageRepository = new AIUsageRepository(queryExecutor);
            
            // 서비스 계층
            this.jwtService = new JwtService();
            this.userService = new UserService(userRepository);
            this.googleOAuthService = new GoogleOAuthService();
            this.musicRecommendationService = new MusicRecommendationService();
            this.aiUsageService = new AIUsageService(aiUsageRepository);
            
            // 컨트롤러 계층
            this.userController = new UserController(userService, jwtService,googleOAuthService);
            this.musicController = new MusicController(musicRecommendationService, aiUsageService);
            
            // 핸들러 계층
            this.healthCheckHandler = new HealthCheckHandler();
            this.userHandler = new UserHandler(userController);
            this.musicHandler = new MusicHandler(musicController);

            // 필터 계층
            this.authenticationFilter = new AuthenticationFilter(jwtService);

            logger.debug("의존성 초기화 완료");
        } catch (Exception e) {
            logger.error("의존성 초기화 실패", e);
            throw new RuntimeException("의존성 초기화 실패", e);
        }
    }

    public DatabaseConfig getDatabaseConfig() { return databaseConfig; }
    public UserHandler getUserHandler() { return userHandler; }
    public HealthCheckHandler getHealthCheckHandler() { return healthCheckHandler; }
    public AuthenticationFilter getAuthenticationFilter() { return authenticationFilter; }
    public MusicHandler getMusicHandler() { return musicHandler; }

    public void shutdown() throws SQLException {
        try {
            if (connectionManager != null) {
                logger.info("ConnectionManager 종료 중...");
                connectionManager.shutdown();
                logger.info("ConnectionManager 종료 완료");
            }
        } catch (SQLException e) {
            logger.warn("ConnectionManager 종료 중 오류 발생: {}", e);
        }
    }

}