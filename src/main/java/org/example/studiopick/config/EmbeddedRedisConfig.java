package org.example.studiopick.config;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import redis.embedded.RedisServer;

import java.io.IOException;
//@Profile("local") // MAC은 이거 주석 풀고 쓰면됨
//@Configuration
public class EmbeddedRedisConfig {

    private RedisServer redisServer;

    public EmbeddedRedisConfig() throws IOException {
        this.redisServer = new RedisServer(6379); // 포트 맞춰야 함
    }

    @PostConstruct
    public void startRedis() {
        redisServer.start();
    }

    @PreDestroy
    public void stopRedis() {
        redisServer.stop();
    }
}
