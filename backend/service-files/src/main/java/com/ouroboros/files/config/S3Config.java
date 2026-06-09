package com.ouroboros.files.config;

import java.net.URI;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3ClientBuilder;

/** Cliente S3. Com endpoint definido (LocalStack/dev), usa path-style e credenciais estaticas. */
@Configuration
public class S3Config {

  @Bean
  public S3Client s3Client(
      @Value("${ouroboros.s3.region}") String region,
      @Value("${ouroboros.s3.endpoint:}") String endpoint,
      @Value("${ouroboros.s3.access-key}") String accessKey,
      @Value("${ouroboros.s3.secret-key}") String secretKey) {
    S3ClientBuilder builder =
        S3Client.builder()
            .region(Region.of(region))
            .credentialsProvider(
                StaticCredentialsProvider.create(AwsBasicCredentials.create(accessKey, secretKey)));
    if (StringUtils.hasText(endpoint)) {
      builder.endpointOverride(URI.create(endpoint)).forcePathStyle(true);
    }
    return builder.build();
  }
}
