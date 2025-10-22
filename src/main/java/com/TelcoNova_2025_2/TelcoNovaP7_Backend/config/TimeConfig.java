package com.TelcoNova_2025_2.TelcoNovaP7_Backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

@Configuration
public class TimeConfig {

  @Bean
  public Clock clock() {
    // UTC para consistencia;
    return Clock.systemUTC();
  }
}

