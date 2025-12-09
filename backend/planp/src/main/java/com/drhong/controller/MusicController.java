package com.drhong.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.drhong.dto.ApiResponse;
import com.drhong.service.AIUsageService;
import com.drhong.service.MusicRecommendationService;

/**
 * 음악 추천 컨트롤러
 * 
 * @author bang9634
 * @since 2025-12-06
 */
public class MusicController {
    private static final Logger logger = LoggerFactory.getLogger(MusicController.class);
    
    private final MusicRecommendationService musicService;
    private final AIUsageService aiUsageService;

    public MusicController(MusicRecommendationService musicService, AIUsageService aiUsageService) {
        this.musicService = musicService;
        this.aiUsageService = aiUsageService;   
    }

    /**
     * 장르별 음악 추천
     * 
     * @param genre 음악 장르
     * @param count 추천 곡 수 (기본: 10)
     * @return 추천된 곡 제목 리스트
     */
    public ApiResponse<?> recommendByGenre(String userId, String genre, int count) {
        logger.info("🎵 장르별 추천 요청: genre={}, count={}", genre, count);

        try {
            // 유효성 검증
            if (genre == null || genre.trim().isEmpty()) {
                return ApiResponse.fail("장르를 지정해주세요");
            }

            if (count < 1 || count > 50) {
                return ApiResponse.fail("추천 곡 수는 1~50 사이여야 합니다");
            }

            // AI 사용량 체크
            if (!aiUsageService.canUseAI(userId)) {
                int currentUsage = aiUsageService.getCurrentUsage(userId);
                logger.warn("사용자 {} AI 일일 제한 초과: {}", userId, currentUsage);
                return ApiResponse.fail("AI 일일 사용 제한을 초과했습니다. 내일 다시 시도해주세요.");
            }

            // 성공 시 사용량 증가
            aiUsageService.incrementUsage(userId);
            // AI 추천
            List<String> songs = musicService.recommendSongsByGenre(genre, count);

            if (songs.isEmpty()) {
                return ApiResponse.fail("추천 결과가 없습니다");
            }

            Map<String, Object> data = new HashMap<>();
            data.put("genre", genre);
            data.put("count", songs.size());
            data.put("songs", songs);

            return ApiResponse.success("추천 완료", data);

        } catch (Exception e) {
            logger.error("음악 추천 중 오류 발생", e);
            return ApiResponse.fail("추천 중 오류가 발생했습니다");
        }
    }

    /**
     * 분위기별 음악 추천
     * 
     * @param mood 분위기
     * @param count 추천 곡 수 (기본: 10)
     * @return 추천된 곡 제목 리스트
     */
    public ApiResponse<?> discoverSongs(String userId,String energy, String activity, String preference, int count) {
        logger.info("🎵 분위기별 추천 요청: energy={}, activity={}, preference={}, count={}", energy, activity, preference, count);

        try {
            // 유효성 검증
            if (energy == null || energy.trim().isEmpty()) {
                return ApiResponse.fail("에너지를 지정해주세요");
            }
            if (activity == null || activity.trim().isEmpty()) {
                return ApiResponse.fail("활동을 지정해주세요");
            }
            if (preference == null || preference.trim().isEmpty()) {
                return ApiResponse.fail("선호도를 지정해주세요");
            }
            if (count < 1 || count > 50) {
                return ApiResponse.fail("추천 곡 수는 1~50 사이여야 합니다");
            }

             // AI 사용량 체크
            if (!aiUsageService.canUseAI(userId)) {
                int currentUsage = aiUsageService.getCurrentUsage(userId);
                int remaining = aiUsageService.getRemainingUsage(userId);
                logger.warn("❌ 사용자 {} AI 일일 제한 초과: {}/10 (남은 횟수: {})", 
                           userId, currentUsage, remaining);
                return ApiResponse.fail("AI 일일 사용 제한(10회)을 초과했습니다. 내일 다시 시도해주세요.");
            }
            aiUsageService.incrementUsage(userId);
            // AI 추천
            List<String> songs = musicService.discoverSongs(energy, activity, preference, count);
            
            if (songs.isEmpty()) {
                return ApiResponse.fail("추천 결과가 없습니다");
            }

            Map<String, Object> data = new HashMap<>();
            data.put("energy", energy);
            data.put("activity", activity);
            data.put("preference", preference);
            data.put("count", songs.size());
            data.put("songs", songs);

            return ApiResponse.success("추천 완료", data);

        } catch (Exception e) {
            logger.error("음악 추천 중 오류 발생", e);
            return ApiResponse.fail("추천 중 오류가 발생했습니다");
        }
    }
}