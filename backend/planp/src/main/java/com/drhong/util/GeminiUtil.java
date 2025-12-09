package com.drhong.util;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.drhong.config.EnvironmentConfig;
import com.drhong.service.UserService;

public class GeminiUtil {
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);
    private static final String MODEL_NAME = "gemini-2.5-flash";
    private static final String apiKey = EnvironmentConfig.getGeminiApiKey();

    public static String askGemini(String prompt) {
        logger.info("제미나이 api 호출: prompt={}", prompt);


        String endpoint = "https://generativelanguage.googleapis.com/v1beta/models/" + MODEL_NAME + ":generateContent?key=" + apiKey;
            try {
                String payload = "{ \"contents\": [{ \"parts\": [{ \"text\": \"" + prompt.replace("\"", "\\\"") + "\" }] }] }";
                URL url = new URL(endpoint);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setDoOutput(true);
                try (OutputStream os = conn.getOutputStream()) {
                    os.write(payload.getBytes());
                }
                int responseCode = conn.getResponseCode();
                InputStream is;
                if (responseCode >= 200 && responseCode < 300) {
                    is = conn.getInputStream();
                } else {
                    is = conn.getErrorStream();
                }
                BufferedReader br = new BufferedReader(new InputStreamReader(is));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    response.append(line);
                }
                br.close();
                if (responseCode >= 200 && responseCode < 300) {
                    String answer = extractGeminiAnswer(response.toString());
                    logger.debug("제미나이 API 답변: " + answer);
                    return answer;
                } else {
                    logger.warn("제미나이 API 오류 응답: " + response.toString());
                    return null;
                }
            } catch (Exception e) {
                logger.warn("오류 발생: " + e.getMessage());
                throw new RuntimeException("제미나이 API 오류 발생", e);
            }
    }

    // Gemini 응답에서 답변 텍스트만 추출하는 간단한 메서드 (실제 응답 구조에 따라 수정 필요)
    private static String extractGeminiAnswer(String response) {
    try {
        // "text":"..." 부분만 추출
        int idx = response.indexOf("\"text\":");
        if (idx == -1) {
            logger.warn("⚠️ 응답에서 text 필드를 찾을 수 없음");
            return null;
        }
        
        int start = response.indexOf('"', idx + 7);
        int end = -1;
        
        // ✅ 이스케이프된 따옴표를 고려한 끝 위치 찾기
        for (int i = start + 1; i < response.length(); i++) {
            if (response.charAt(i) == '"' && response.charAt(i - 1) != '\\') {
                end = i;
                break;
            }
        }
        
        if (start == -1 || end == -1) {
            logger.warn("⚠️ text 필드 파싱 실패");
            return null;
        }
        
        // ✅ 이스케이프 문자 복원
        String text = response.substring(start + 1, end);
        text = text.replace("\\n", "\n")      // 줄바꿈
                   .replace("\\\"", "\"")     // 따옴표
                   .replace("\\\\", "\\")     // 백슬래시
                   .replace("\\t", "\t");     // 탭
        
        logger.debug("✅ 파싱된 텍스트:\n{}", text);
        return text;
        
    } catch (Exception e) {
        logger.error("Gemini 응답 파싱 실패: {}", e.getMessage());
        return null;
    }
}


    
}
