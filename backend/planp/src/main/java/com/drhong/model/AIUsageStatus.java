package com.drhong.model;

/**
 * AI 사용량 상태를 나타내는 모델 클래스
 * <p>
 * 사용자의 현재 AI 사용량, 남은 사용량, 일일 제한 등을 포함한다.
 * 비즈니스 로직을 포함하지 않는 순수 데이터 모델이다.
 * </p>
 * 
 * @author bang9634
 * @since 2025-12-09
 */
public class AIUsageStatus {
    private final int currentUsage;
    private final int remainingUsage;
    private final int dailyLimit;
    private final boolean canUse;

    public AIUsageStatus(int currentUsage, int remainingUsage, int dailyLimit, boolean canUse) {
        this.currentUsage = currentUsage;
        this.remainingUsage = remainingUsage;
        this.dailyLimit = dailyLimit;
        this.canUse = canUse;
    }

    public int getCurrentUsage() {
        return currentUsage;
    }

    public int getRemainingUsage() {
        return remainingUsage;
    }

    public int getDailyLimit() {
        return dailyLimit;
    }

    public boolean canUse() {
        return canUse;
    }

    @Override
    public String toString() {
        return String.format("AIUsageStatus[current=%d, remaining=%d, limit=%d, canUse=%s]",
                currentUsage, remainingUsage, dailyLimit, canUse);
    }
}