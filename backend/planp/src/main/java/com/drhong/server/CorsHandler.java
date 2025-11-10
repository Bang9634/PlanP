package com.drhong.server;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

/**
 * CORS 처리를 위한 핸들러
 */
public class CorsHandler implements HttpHandler {
    
    private static final Logger logger = LoggerFactory.getLogger(CorsHandler.class);

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        String method = exchange.getRequestMethod();
        String origin = exchange.getRequestHeaders().getFirst("Origin");
        
        logger.debug("CORS 핸들러 호출: {} {}, Origin: {}", method, path, origin);

        // 강력한 CORS 헤더 설정
        setCorsHeaders(exchange, origin);

        // OPTIONS 요청 (preflight) 처리
        if ("OPTIONS".equals(method)) {
            logger.debug("CORS Preflight 요청 처리: {}, Origin: {}", path, origin);
            exchange.sendResponseHeaders(200, -1);
            return;
        }

        // 루트 경로는 기본 응답 제공
        if ("/".equals(path)) {
            String response = "PlanP Backend Server is running!";
            exchange.getResponseHeaders().set("Content-Type", "text/plain; charset=UTF-8");
            exchange.sendResponseHeaders(200, response.getBytes(StandardCharsets.UTF_8).length);
            
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(response.getBytes(StandardCharsets.UTF_8));
            }
            return;
        }

        // 다른 경로는 404 응답
        logger.debug("404 - 찾을 수 없는 경로: {} {}", method, path);
        String notFoundResponse = "404 - Path not found: " + path;
        exchange.getResponseHeaders().set("Content-Type", "text/plain; charset=UTF-8");
        exchange.sendResponseHeaders(404, notFoundResponse.getBytes(StandardCharsets.UTF_8).length);
        
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(notFoundResponse.getBytes(StandardCharsets.UTF_8));
        }
    }
    
    /**
     * 강력한 CORS 헤더 설정
     */
    private void setCorsHeaders(HttpExchange exchange, String origin) {
        // Origin 확인 및 허용
        if (origin != null && (origin.startsWith("http://localhost:") || origin.startsWith("http://127.0.0.1:"))) {
            exchange.getResponseHeaders().set("Access-Control-Allow-Origin", origin);
        } else {
            exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        }
        
        exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type, Accept, Authorization, X-Requested-With, Origin");
        exchange.getResponseHeaders().set("Access-Control-Allow-Credentials", "false");
        exchange.getResponseHeaders().set("Access-Control-Max-Age", "3600");
        
        logger.debug("CORS 헤더 설정 완료: Origin={}", origin);
    }
}