package kkennib.net.mining.sceretsmanager.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueRequest;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueResponse;
import software.amazon.awssdk.services.secretsmanager.model.SecretsManagerException;

import java.io.IOException;
import java.util.Map;
import java.util.logging.Logger;

@Service
public class SecretsManagerService {

  private static final Logger logger = Logger.getLogger(SecretsManagerService.class.getName());
  private final SecretsManagerClient secretsClient;
  private final ObjectMapper objectMapper;

  public SecretsManagerService(SecretsManagerClient secretsClient) {
    this.secretsClient = secretsClient;
    this.objectMapper = new ObjectMapper();
  }

  public Map<String, String> getSecret(String secretName) {
    try {
      GetSecretValueRequest request = GetSecretValueRequest.builder()
              .secretId(secretName)
              .build();
      GetSecretValueResponse response = secretsClient.getSecretValue(request);
      String secretJson = response.secretString();

      return objectMapper.readValue(secretJson, new TypeReference<Map<String, String>>() {});
    } catch (SecretsManagerException e) {
      logger.severe("AWS Secrets Manager error while fetching secret [" + secretName + "]: " + e.getMessage());
      throw e;
    } catch (IOException e) {
      throw new RuntimeException("Failed to parse secrets for " + secretName, e);
    }
  }
}