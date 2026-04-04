package kkennib.net.mining.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.ses.SesClient;

@Configuration
public class AwsS3Config {

  @Value("${cloud.aws.region.static}")
  private String region;

  @Bean
  public S3Client s3Client() {
    return S3Client.builder()
            .region(Region.of(region))
            .build();
  }

  @Bean
  public SesClient sesClient() {
    return SesClient.builder()
            .region(Region.of("ap-northeast-2"))
            .build();  // 환경변수나 프로파일에서 자동 인식
  }

}
