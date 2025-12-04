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
import com.drhong.service.GoogleOAuthService;
import com.drhong.service.UserService;


public class ApplicationContext{
    protected final Logger logger = LoggerFactory.getLogger(getClass());
    
    private final DatabaseConfig databaseConfig;
    private final ConnectionManager connectionManager;
    private final QueryExecutor queryExecutor;

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

            this.healthCheckHandler = new HealthCheckHandler();

            // 사용자 관련 의존성
            this.userRepository = new UserRepository(queryExecutor);
            this.userService = new UserService(userRepository);
            
            // OAuth 관련 의존성 (순환 참조 방지를 위해 UserService와 분리)
            this.googleOAuthService = new GoogleOAuthService();
            
            // 컨트롤러 및 핸들러
            this.userController = new UserController(userService, googleOAuthService);
            this.userHandler = new UserHandler(userController);

        logger.debug("의존성 초기화 완료");
        } catch (Exception e) {
            logger.error("의존성 초기화 실패", e);
            throw new RuntimeException("의존성 초기화 실패", e);
        }
    }

    public DatabaseConfig getDatabaseConfig() { return databaseConfig; }
    public UserHandler getUserHandler() { return userHandler; }
    public HealthCheckHandler getHealthCheckHandler() { return healthCheckHandler; }

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