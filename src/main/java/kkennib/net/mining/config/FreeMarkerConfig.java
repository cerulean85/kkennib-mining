package kkennib.net.mining.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

@Configuration
public class FreeMarkerConfig {

  @Bean
  public freemarker.template.Configuration freemarkerConfiguration() throws IOException {
    freemarker.template.Configuration cfg = new freemarker.template.Configuration(freemarker.template.Configuration.VERSION_2_3_32);
    cfg.setClassForTemplateLoading(this.getClass(), "/templates"); // 경로는 상황에 맞게 조정
    cfg.setDefaultEncoding("UTF-8");
    return cfg;
  }
}
