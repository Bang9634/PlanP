package com.drhong.repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.drhong.database.QueryExecutor;

/**
 * AI 사용량 데이터 접근 객체
 * 
 * @author bang9634
 * @since 2025-12-09
 */
public class AIUsageRepository {
    private static final Logger logger = LoggerFactory.getLogger(AIUsageRepository.class);
    private final QueryExecutor queryExecutor;

    public AIUsageRepository(QueryExecutor queryExecutor) {
        this.queryExecutor = queryExecutor;
    }

    /**
     * 오늘 사용량 조회
     */
    public int getTodayUsage(String userId) {
        String today = LocalDate.now().toString();
        String sql = "SELECT usage_count FROM ai_usage WHERE user_id = ? AND usage_date = ?";
        
        try {
            return queryExecutor.executeQuerySingle(sql, this::mapToUsageCount, userId, today)
                                .orElse(0);
        } catch (SQLException e) {
            logger.error("AI 사용량 조회 실패", e);
            return 0;
        }
    }

    /**
     * 사용량 증가
     */
    public void incrementUsage(String userId) {
        String today = LocalDate.now().toString();
        String sql = 
            "INSERT INTO ai_usage (user_id, usage_date, usage_count) " +
            "VALUES (?, ?, 1) " +
            "ON DUPLICATE KEY UPDATE usage_count = usage_count + 1";
        
        try {
            queryExecutor.executeUpdate(sql, userId, today);
            logger.debug("AI 사용량 증가: userId={}", userId);
        } catch (SQLException e) {
            logger.error("AI 사용량 증가 실패", e);
        }
    }

    private Integer mapToUsageCount(ResultSet rs) throws SQLException {
        return rs.getInt("usage_count");
    }
}