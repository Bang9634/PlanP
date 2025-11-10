package com.drhong.server;

import java.io.IOException;
import java.io.OutputStream;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

/**
 * 서버 상태 확인 핸들러
 */
public class HealthCheckHandler implements HttpHandler {
    
    private final Gson gson = new Gson();
    
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        // CORS 헤더 설정
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        
        Map<String, Object> healthStatus = new HashMap<>();
        healthStatus.put("status", "UP");
        healthStatus.put("timestamp", LocalDateTime.now().toString());
        healthStatus.put("service", "PlanP Backend");
        healthStatus.put("version", "1.0.0");
        
        String jsonResponse = gson.toJson(healthStatus);
        byte[] responseBytes = jsonResponse.getBytes();
        
        exchange.sendResponseHeaders(200, responseBytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(responseBytes);
        }
    }
}