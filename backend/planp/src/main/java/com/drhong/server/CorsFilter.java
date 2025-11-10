package com.drhong.server;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.net.httpserver.Filter;
import com.sun.net.httpserver.HttpExchange;

/**
 * 모든 요청에 CORS 헤더를 추가하는 필터
 */
public class CorsFilter extends Filter {
    
    private static final Logger logger = LoggerFactory.getLogger(CorsFilter.class);

    @Override
    public void doFilter(HttpExchange exchange, Chain chain) throws IOException {
        String origin = exchange.getRequestHeaders().getFirst("Origin");
        String method = exchange.getRequestMethod();
        String path = exchange.getRequestURI().getPath();
        
        logger.debug("CORS 필터 처리: {} {}, Origin: {}", method, path, origin);
        
        // CORS 헤더 설정
        setCorsHeaders(exchange, origin);
        
        // OPTIONS 요청은 여기서 바로 처리
        if ("OPTIONS".equals(method)) {
            logger.debug("OPTIONS 요청 처리: {}", path);
            exchange.sendResponseHeaders(200, -1);
            return;
        }
        
        // 다른 요청은 다음 핸들러로 전달
        chain.doFilter(exchange);
    }
    
    private void setCorsHeaders(HttpExchange exchange, String origin) {
        // 개발 환경에서는 localhost origin 허용
        if (origin != null && (
            origin.equals("http://localhost:3000") || 
            origin.equals("http://localhost:8080") ||
            origin.startsWith("http://localhost:") ||
            origin.startsWith("http://127.0.0.1:")
        )) {
            exchange.getResponseHeaders().set("Access-Control-Allow-Origin", origin);
        } else {
            exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        }
        
        exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type, Accept, Authorization, X-Requested-With, Origin");
        exchange.getResponseHeaders().set("Access-Control-Allow-Credentials", "false");
        exchange.getResponseHeaders().set("Access-Control-Max-Age", "3600");
    }

    @Override
    public String description() {
        return "CORS Filter for PlanP API";
    }
}