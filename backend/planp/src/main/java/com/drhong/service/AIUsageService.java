package com.drhong.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.drhong.repository.AIUsageRepository;
import com.drhong.model.AIUsageStatus;

/**
 * AI 사용량 관리 서비스
 * 
 * @author bang9634
 * @since 2025-12-09
 */
public class AIUsageService {
    private static final Logger logger = LoggerFactory.getLogger(AIUsageService.class);
    
    // ✅ 일일 사용 제한 (기본 10회)
    private static final int DAILY_LIMIT = 200;
    
    private final AIUsageRepository aiUsageRepository;

    public AIUsageService(AIUsageRepository aiUsageRepository) {
        this.aiUsageRepository = aiUsageRepository;
        logger.info("AIUsageService 초기화 완료");
    }

    /**
     * AI 사용 가능 여부 확인
     * 
     * @param userId 사용자 ID
     * @return true: 사용 가능, false: 제한 초과
     */
    public boolean canUseAI(String userId) {
        try {
            int currentUsage = aiUsageRepository.getTodayUsage(userId);
            
            boolean canUse = currentUsage < DAILY_LIMIT;
            
            if (canUse) {
                logger.debug("✅ 사용자 {} AI 사용 가능: {}/{}", userId, currentUsage, DAILY_LIMIT);
            } else {
                logger.warn("❌ 사용자 {} AI 일일 제한 초과: {}/{}", userId, currentUsage, DAILY_LIMIT);
            }
            
            return canUse;
            
        } catch (Exception e) {
            logger.error("AI 사용 가능 여부 확인 실패: userId={}", userId, e);
            // 오류 발생 시 안전하게 false 반환 (사용 제한)
            return false;
        }
    }

    /**
     * AI 사용량 증가
     * 
     * @param userId 사용자 ID
     * @return true: 증가 성공, false: 증가 실패
     */
    public boolean incrementUsage(String userId) {
        try {
            aiUsageRepository.incrementUsage(userId);
            
            int currentUsage = aiUsageRepository.getTodayUsage(userId);
            int remaining = getRemainingUsage(userId);
            
            logger.info("✅ 사용자 {} AI 사용량 증가: {}/{} (남은 횟수: {})", 
                        userId, currentUsage, DAILY_LIMIT, remaining);
            
            return true;
            
        } catch (Exception e) {
            logger.error("AI 사용량 증가 실패: userId={}", userId, e);
            return false;
        }
    }

    /**
     * 현재 사용량 조회
     * 
     * @param userId 사용자 ID
     * @return 오늘 사용한 횟수 (0~DAILY_LIMIT)
     */
    public int getCurrentUsage(String userId) {
        try {
            int usage = aiUsageRepository.getTodayUsage(userId);
            logger.debug("사용자 {} 현재 AI 사용량: {}", userId, usage);
            return usage;
            
        } catch (Exception e) {
            logger.error("AI 사용량 조회 실패: userId={}", userId, e);
            return 0;
        }
    }

    /**
     * 남은 사용 가능 횟수 조회
     * 
     * @param userId 사용자 ID
     * @return 오늘 남은 사용 가능 횟수 (0~DAILY_LIMIT)
     */
    public int getRemainingUsage(String userId) {
        int currentUsage = getCurrentUsage(userId);
        int remaining = Math.max(0, DAILY_LIMIT - currentUsage);
        
        logger.debug("사용자 {} 남은 AI 사용 가능 횟수: {}", userId, remaining);
        return remaining;
    }

    /**
     * 일일 사용 제한 값 조회
     * 
     * @return 일일 최대 사용 가능 횟수
     */
    public int getDailyLimit() {
        return DAILY_LIMIT;
    }

    /**
     * 사용 가능 여부와 남은 횟수를 포함한 상태 정보 조회
     * 
     * @param userId 사용자 ID
     * @return AI 사용 상태 정보
     */
    public AIUsageStatus getUsageStatus(String userId) {
        int current = getCurrentUsage(userId);
        int remaining = getRemainingUsage(userId);
        boolean canUse = current < DAILY_LIMIT;
        
        return new AIUsageStatus(current, remaining, DAILY_LIMIT, canUse);
    }
}