package kkennib.net.mining.config;

import kkennib.net.mining.util.JwtUtil;
import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TestConfig {
  @Bean
  public JwtUtil jwtService() {
    return Mockito.mock(JwtUtil.class);
  }
}