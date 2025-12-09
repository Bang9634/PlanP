package com.drhong.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.drhong.dto.ApiResponse;
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

    public MusicController(MusicRecommendationService musicService) {
        this.musicService = musicService;
    }

    /**
     * 장르별 음악 추천
     * 
     * @param genre 음악 장르
     * @param count 추천 곡 수 (기본: 10)
     * @return 추천된 곡 제목 리스트
     */
    public ApiResponse<?> recommendByGenre(String genre, int count) {
        logger.info("🎵 장르별 추천 요청: genre={}, count={}", genre, count);

        try {
            // 유효성 검증
            if (genre == null || genre.trim().isEmpty()) {
                return ApiResponse.fail("장르를 지정해주세요");
            }

            if (count < 1 || count > 50) {
                return ApiResponse.fail("추천 곡 수는 1~50 사이여야 합니다");
            }

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
}