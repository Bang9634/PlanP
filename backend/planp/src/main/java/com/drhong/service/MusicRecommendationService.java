package com.drhong.service;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.drhong.util.GeminiUtil;
import com.google.gson.Gson;

/**
 * AI 기반 음악 추천 서비스
 * 
 * <p>
 * OpenAI API를 사용하여 장르에 맞는 음악을 추천한다.
 * </p>
 * 
 * @author bang9634
 * @since 2025-12-06
 */
public class MusicRecommendationService {
    private static final Logger logger = LoggerFactory.getLogger(MusicRecommendationService.class);
    private static final String PROMPT_HEADER = 
            "You are a music recommendation expert with deep knowledge of diverse artists." 
            + "Focus on variety and include lesser-known artists alongside popular ones. "
            + "Avoid recommending only chart-toppers or the same famous artists repeatedly. ";
    private static final String PROMPT_FORMAT = 
            "Return ONLY the song titles, one per line, without numbering or extra text. "
            + "Format: 'Artist - Song Title'";

    public MusicRecommendationService() {
        logger.info("MusicRecommendationService 초기화 완료");
    }

    /**
     * 장르에 맞는 노래 제목 추천
     * 
     * @param genre 음악 장르 (예: KPOP, ROCK, JAZZ)
     * @param count 추천받을 곡 수 (기본: 10)
     * @return 추천된 노래 제목 리스트
     */
    public List<String> recommendSongsByGenre(String genre, int count) {
        logger.info("🎵 AI 음악 추천 시작: genre={}, count={}", genre, count);
        
        try {
            // OpenAI API 호출
            String prompt = String.format(
            PROMPT_HEADER +
            "Recommend %d popular %s songs. " +
            PROMPT_FORMAT,
            count, genre
        );
            String response = GeminiUtil.askGemini(prompt);
            
            // 응답 파싱
            List<String> songs = parseAIResponse(response);
            
            logger.info("AI 추천 완료: {} 곡", songs.size());
            return songs;
            
        } catch (Exception e) {
            logger.error("AI 추천 실패, 목업 데이터 반환", e);
            return getMockRecommendations(genre, count);
        }
    }

    public List<String> discoverSongs(String energy, String activity, String preference, int count) {
        logger.info("🎵 AI 음악 추천 시작: energy={}, activity={}, preference={}, count={}", energy, activity, preference, count);
        
        try {
            String prompt = String.format(
                PROMPT_HEADER +
                "Recommend %d songs that match the following criteria:\n" +
                "- Energy level: %s\n" +
                "- Listening situation: %s\n" +
                "- Preferred genre: %s\n\n" +
                PROMPT_FORMAT,
                count, energy, activity, preference
            );
            String response = GeminiUtil.askGemini(prompt);
            
            // 응답 파싱
            List<String> songs = parseAIResponse(response);
            
            logger.info("AI 추천 완료: {} 곡", songs.size());
            return songs;
            
        } catch (Exception e) {
            logger.error("AI 추천 실패, 목업 데이터 반환", e);
            return getMockRecommendations("POP", count); // 무드 기반 추천은 POP 장르로 대체
        }
    }

    /**
     * AI 응답 파싱
     */
    private List<String> parseAIResponse(String response) {
        List<String> songs = new ArrayList<>();
        
        try {
            // ✅ Null 체크
        if (response == null || response.trim().isEmpty()) {
            logger.warn("Gemini 응답이 비어있습니다");
            return songs;
        }
        
            // 줄 단위로 분리
            String[] lines = response.split("\n");
            logger.debug("파싱할 줄 수: {}", lines.length);

            for (String line : lines) {
                String trimmed = line.trim();
                
                // 빈 줄 건너뛰기
                if (trimmed.isEmpty()) {
                    continue;
                }
                
                // 번호 제거 (예: "1. Artist - Song" → "Artist - Song")
                trimmed = trimmed.replaceFirst("^\\d+\\.\\s*", "");
                
                // "Artist - Song" 형식 검증
                if (trimmed.contains(" - ")) {
                    songs.add(trimmed);
                    logger.debug("파싱된 곡: {}", trimmed);
                }
            }
            logger.info("총 {}곡 파싱 완료", songs.size());
        } catch (Exception e) {
            logger.error("Gemini 응답 파싱 실패", e);
        }
        
        return songs;
    }

    /**
     * 목업 데이터 생성 (API 키가 없거나 오류 시)
     */
    private List<String> getMockRecommendations(String genre, int count) {
        List<String> mockSongs = new ArrayList<>();
        
        switch (genre.toUpperCase()) {
            case "KPOP":
                mockSongs.add("BTS - Dynamite");
                mockSongs.add("BLACKPINK - DDU-DU DDU-DU");
                mockSongs.add("NewJeans - Ditto");
                mockSongs.add("IVE - LOVE DIVE");
                mockSongs.add("Stray Kids - God's Menu");
                mockSongs.add("TWICE - Feel Special");
                mockSongs.add("(G)I-DLE - Tomboy");
                mockSongs.add("Seventeen - HOT");
                mockSongs.add("aespa - Next Level");
                mockSongs.add("LE SSERAFIM - ANTIFRAGILE");
                break;
            case "POP":
                mockSongs.add("Taylor Swift - Anti-Hero");
                mockSongs.add("The Weeknd - Blinding Lights");
                mockSongs.add("Dua Lipa - Levitating");
                mockSongs.add("Harry Styles - As It Was");
                mockSongs.add("Ed Sheeran - Shape of You");
                mockSongs.add("Billie Eilish - bad guy");
                mockSongs.add("Ariana Grande - 7 rings");
                mockSongs.add("Post Malone - Circles");
                mockSongs.add("Olivia Rodrigo - drivers license");
                mockSongs.add("Justin Bieber - Peaches");
                break;
            case "ROCK":
                mockSongs.add("Queen - Bohemian Rhapsody");
                mockSongs.add("Led Zeppelin - Stairway to Heaven");
                mockSongs.add("Nirvana - Smells Like Teen Spirit");
                mockSongs.add("AC/DC - Back in Black");
                mockSongs.add("Metallica - Enter Sandman");
                mockSongs.add("Guns N' Roses - Sweet Child O' Mine");
                mockSongs.add("Pink Floyd - Comfortably Numb");
                mockSongs.add("The Beatles - Hey Jude");
                mockSongs.add("Red Hot Chili Peppers - Californication");
                mockSongs.add("Foo Fighters - Everlong");
                break;
            case "HIPHOP":
                mockSongs.add("Drake - God's Plan");
                mockSongs.add("Kendrick Lamar - HUMBLE.");
                mockSongs.add("Post Malone - Rockstar");
                mockSongs.add("Travis Scott - SICKO MODE");
                mockSongs.add("Eminem - Lose Yourself");
                mockSongs.add("Kanye West - Stronger");
                mockSongs.add("Jay-Z - Empire State of Mind");
                mockSongs.add("Lil Nas X - MONTERO");
                mockSongs.add("Cardi B - WAP");
                mockSongs.add("21 Savage - a lot");
                break;
            case "JAZZ":
                mockSongs.add("Louis Armstrong - What a Wonderful World");
                mockSongs.add("Miles Davis - So What");
                mockSongs.add("John Coltrane - A Love Supreme");
                mockSongs.add("Ella Fitzgerald - Summertime");
                mockSongs.add("Frank Sinatra - Fly Me to the Moon");
                mockSongs.add("Billie Holiday - Strange Fruit");
                mockSongs.add("Duke Ellington - Take the A Train");
                mockSongs.add("Nina Simone - Feeling Good");
                mockSongs.add("Chet Baker - My Funny Valentine");
                mockSongs.add("Dave Brubeck - Take Five");
                break;
            default:
                // 기본 인기곡
                mockSongs.add("The Weeknd - Blinding Lights");
                mockSongs.add("BTS - Dynamite");
                mockSongs.add("Ed Sheeran - Shape of You");
                mockSongs.add("Taylor Swift - Shake It Off");
                mockSongs.add("Queen - Bohemian Rhapsody");
                mockSongs.add("Imagine Dragons - Believer");
                mockSongs.add("Billie Eilish - bad guy");
                mockSongs.add("Bruno Mars - Uptown Funk");
                mockSongs.add("Adele - Rolling in the Deep");
                mockSongs.add("Maroon 5 - Sugar");
        }
        
        // count에 맞게 자르기
        return mockSongs.subList(0, Math.min(count, mockSongs.size()));
    }
}