package com.drhong.handler;

import java.io.IOException;
import java.util.Map;

import com.drhong.controller.MusicController;
import com.drhong.dto.ApiResponse;
import com.sun.net.httpserver.HttpExchange;

/**
 * 음악 추천 HTTP 핸들러
 * 
 * @author bang9634
 * @since 2025-12-06
 */
public class MusicHandler extends BaseHandler {
    private final MusicController musicController;

    public MusicHandler(MusicController musicController) {
        this.musicController = musicController;
    }

    @Override
    protected void registerRoutes() {
        get("/recommend-by-genre", this::handleRecommendByGenre);
        get("/discover", this::handleDiscoverMusic);
    }

    @Override
    protected String extractEndpoint(String fullPath) {
        String basePath = "/api/music";
        if (fullPath.startsWith(basePath)) {
            return fullPath.substring(basePath.length());
        }
        return fullPath;
    }

    /**
     * GET /api/music/recommend-by-genre?genre=KPOP&count=10
     */
    private void handleRecommendByGenre(HttpExchange exchange) throws IOException {
        logger.debug("음악 추천 요청");

        try {
            String userId = (String) exchange.getAttribute("userId");
            if (userId == null) {
                sendErrorResponse(exchange, 401, "로그인이 필요합니다");
                return;
            }
            // 쿼리 파라미터 파싱
            String query = exchange.getRequestURI().getQuery();
            Map<String, String> params = parseQueryParams(query);

            String genre = params.getOrDefault("genre", "POP");
            int count = Integer.parseInt(params.getOrDefault("count", "3"));

            // 컨트롤러 호출
            ApiResponse<?> response = musicController.recommendByGenre(userId, genre, count);

            // 응답 전송
            int statusCode = response.isSuccess() ? 200 : 400;
            sendResponse(exchange, statusCode, response);

        } catch (NumberFormatException e) {
            logger.warn("잘못된 count 파라미터", e);
            sendErrorResponse(exchange, 400, "count는 숫자여야 합니다");
        } catch (Exception e) {
            logger.error("음악 추천 처리 중 오류", e);
            sendErrorResponse(exchange, 500, "서버 오류가 발생했습니다");
        }
    }

    /**
     * GET /api/music/discover?mood=KPOP&count=10
     */
    private void handleDiscoverMusic(HttpExchange exchange) throws IOException {
        logger.debug("음악 분위기별 추천 요청");

        try {
            String userId = (String) exchange.getAttribute("userId");
            if (userId == null) {
                sendErrorResponse(exchange, 401, "로그인이 필요합니다");
                return;
            }
            // 쿼리 파라미터 파싱
            String query = exchange.getRequestURI().getQuery();
            Map<String, String> params = parseQueryParams(query);

            String energy = params.getOrDefault("energy", "HAPPY");
            String activity = params.getOrDefault("activity", "WORKOUT");
            String preference = params.getOrDefault("preference", "POP");
            int count = Integer.parseInt(params.getOrDefault("count", "3"));

            // 컨트롤러 호출
            ApiResponse<?> response = musicController.discoverSongs(userId,energy, activity, preference, count);
            // 응답 전송
            int statusCode = response.isSuccess() ? 200 : 400;
            sendResponse(exchange, statusCode, response);

        } catch (NumberFormatException e) {
            logger.warn("잘못된 count 파라미터", e);
            sendErrorResponse(exchange, 400, "count는 숫자여야 합니다");
        } catch (Exception e) {
            logger.error("음악 추천 처리 중 오류", e);
            sendErrorResponse(exchange, 500, "서버 오류가 발생했습니다");
        }
    }
}