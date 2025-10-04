package com.example.pypygo;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.sql.Connection;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * @author: tcsow
 * @date: 2025/10/4
 */
@Slf4j
@RestController
@RequestMapping("/api/health")
@RequiredArgsConstructor
public class HealthCheckController {

    private final DataSource dataSource;
    private final RedisTemplate<String, Object> redisTemplate;
    private final RabbitTemplate rabbitTemplate;

    /**
     * 綜合健康檢查
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> healthCheck() {
        Map<String, Object> result = new HashMap<>();
        result.put("timestamp", LocalDateTime.now());
        result.put("application", "pypygo");

        // 檢查各服務狀態
        result.put("mysql", checkMySQL());
        result.put("redis", checkRedis());
        result.put("rabbitmq", checkRabbitMQ());

        boolean allHealthy = (boolean) result.get("mysql")
                && (boolean) result.get("redis")
                && (boolean) result.get("rabbitmq");

        result.put("status", allHealthy ? "UP" : "DOWN");

        return ResponseEntity.ok(result);
    }

    /**
     * MySQL 連線測試
     */
    @GetMapping("/mysql")
    public ResponseEntity<Map<String, Object>> testMySQL() {
        Map<String, Object> result = new HashMap<>();
        result.put("service", "MySQL");

        try (Connection conn = dataSource.getConnection()) {
            boolean isValid = conn.isValid(3);
            result.put("status", isValid ? "UP" : "DOWN");
            result.put("connected", true);
            result.put("catalog", conn.getCatalog());
            result.put("autoCommit", conn.getAutoCommit());

            log.info("MySQL health check: SUCCESS");
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            result.put("status", "DOWN");
            result.put("connected", false);
            result.put("error", e.getMessage());

            log.error("MySQL health check: FAILED - {}", e.getMessage());
            return ResponseEntity.status(503).body(result);
        }
    }

    /**
     * Redis 連線測試
     */
    @GetMapping("/redis")
    public ResponseEntity<Map<String, Object>> testRedis() {
        Map<String, Object> result = new HashMap<>();
        result.put("service", "Redis");

        try {
            // 測試寫入
            String testKey = "health:check:" + UUID.randomUUID();
            String testValue = "test-" + System.currentTimeMillis();
            redisTemplate.opsForValue().set(testKey, testValue);

            // 測試讀取
            Object retrieved = redisTemplate.opsForValue().get(testKey);

            // 測試刪除
            redisTemplate.delete(testKey);

            boolean success = testValue.equals(retrieved);
            result.put("status", success ? "UP" : "DOWN");
            result.put("write", true);
            result.put("read", true);
            result.put("delete", true);

            log.info("Redis health check: SUCCESS");
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            result.put("status", "DOWN");
            result.put("error", e.getMessage());

            log.error("Redis health check: FAILED - {}", e.getMessage());
            return ResponseEntity.status(503).body(result);
        }
    }

    /**
     * RabbitMQ 連線測試
     */
    @GetMapping("/rabbitmq")
    public ResponseEntity<Map<String, Object>> testRabbitMQ() {
        Map<String, Object> result = new HashMap<>();
        result.put("service", "RabbitMQ");

        try {
            // 測試發送訊息到預設 exchange
            String testMessage = "health-check-" + System.currentTimeMillis();
            rabbitTemplate.convertAndSend("", "test.health.queue", testMessage);

            result.put("status", "UP");
            result.put("connected", true);
            result.put("messageSent", true);

            log.info("RabbitMQ health check: SUCCESS");
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            result.put("status", "DOWN");
            result.put("connected", false);
            result.put("error", e.getMessage());

            log.error("RabbitMQ health check: FAILED - {}", e.getMessage());
            return ResponseEntity.status(503).body(result);
        }
    }

    /**
     * Redis 詳細資訊測試
     */
    @GetMapping("/redis/info")
    public ResponseEntity<Map<String, Object>> redisInfo() {
        Map<String, Object> result = new HashMap<>();

        try {
            // 測試不同的 Redis 資料結構
            String prefix = "test:" + UUID.randomUUID() + ":";

            // String
            redisTemplate.opsForValue().set(prefix + "string", "value");
            Object stringValue = redisTemplate.opsForValue().get(prefix + "string");

            // Hash
            redisTemplate.opsForHash().put(prefix + "hash", "field1", "value1");
            Object hashValue = redisTemplate.opsForHash().get(prefix + "hash", "field1");

            // List
            redisTemplate.opsForList().rightPush(prefix + "list", "item1");
            Object listValue = redisTemplate.opsForList().index(prefix + "list", 0);

            // Set
            redisTemplate.opsForSet().add(prefix + "set", "member1");
            Boolean setMember = redisTemplate.opsForSet().isMember(prefix + "set", "member1");

            // 清理測試資料
            redisTemplate.delete(prefix + "string");
            redisTemplate.delete(prefix + "hash");
            redisTemplate.delete(prefix + "list");
            redisTemplate.delete(prefix + "set");

            result.put("status", "UP");
            result.put("operations", Map.of(
                    "string", stringValue != null,
                    "hash", hashValue != null,
                    "list", listValue != null,
                    "set", Boolean.TRUE.equals(setMember)
            ));

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            result.put("status", "DOWN");
            result.put("error", e.getMessage());
            return ResponseEntity.status(503).body(result);
        }
    }

    // 私有輔助方法
    private boolean checkMySQL() {
        try (Connection conn = dataSource.getConnection()) {
            return conn.isValid(3);
        } catch (Exception e) {
            log.error("MySQL check failed: {}", e.getMessage());
            return false;
        }
    }

    private boolean checkRedis() {
        try {
            String testKey = "health:check";
            redisTemplate.opsForValue().set(testKey, "ok");
            redisTemplate.delete(testKey);
            return true;
        } catch (Exception e) {
            log.error("Redis check failed: {}", e.getMessage());
            return false;
        }
    }

    private boolean checkRabbitMQ() {
        try {
            rabbitTemplate.convertAndSend("", "test.health.queue", "health-check");
            return true;
        } catch (Exception e) {
            log.error("RabbitMQ check failed: {}", e.getMessage());
            return false;
        }
    }
}