package com.george.springbootinit.config;

import io.github.briqt.spark4j.SparkClient;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "xunfei.client")
@Data
public class XingHuoAiConfig {
    private String appid;
    private String apiSecret;
    private String apiKey;

    @Bean
    public SparkClient sparkClient() {
        SparkClient sparkClient = new SparkClient();
        sparkClient.appid = appid;
        sparkClient.apiSecret = apiSecret;
        sparkClient.apiKey = apiKey;
        return sparkClient;
    }
}
