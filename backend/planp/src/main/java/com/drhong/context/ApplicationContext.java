package com.drhong.context;

import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.drhong.config.DatabaseConfig;
import com.drhong.controller.UserController;
import com.drhong.database.ConnectionManager;
import com.drhong.database.QueryExecutor;
import com.drhong.handler.HealthCheckHandler;
import com.drhong.handler.UserHandler;
import com.drhong.repository.UserRepository;
import com.drhong.server.AuthenticationFilter;
import com.drhong.service.GoogleOAuthService;
import com.drhong.service.JwtService;
import com.drhong.service.UserService;
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

    private final UserRepository userRepository;
    private final UserService userService;
    private final UserController userController;
    private final UserHandler userHandler;

    public ApplicationContext() {
        logger.debug("의존성 초기화 시작");
        try {
            this.databaseConfig = new DatabaseConfig();
            this.connectionManager = ConnectionManager.getInstance(databaseConfig);
            this.queryExecutor = new QueryExecutor(connectionManager);

            // 레포지토리 계층
            this.userRepository = new UserRepository(queryExecutor);
            
            // 서비스 계층
            this.jwtService = new JwtService();
            this.userService = new UserService(userRepository);
            this.googleOAuthService = new GoogleOAuthService();
            
            // 컨트롤러 계층
            this.userController = new UserController(userService, jwtService,googleOAuthService);

            // 핸들러 계층
            this.healthCheckHandler = new HealthCheckHandler();
            this.userHandler = new UserHandler(userController);

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