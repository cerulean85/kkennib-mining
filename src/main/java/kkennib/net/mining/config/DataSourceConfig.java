package kkennib.net.mining.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import kkennib.net.mining.sceretsmanager.service.SecretsManagerService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;
import java.util.Map;
import java.util.Optional;


@Configuration
public class DataSourceConfig {

    private final SecretsManagerService secretsManagerService;

    @Value("${spring.datasource.url:}")
    private String dbUrl;

    @Value("${spring.datasource.username:}")
    private String dbUsername;

    @Value("${spring.datasource.password:}")
    private String dbPassword;

    public DataSourceConfig(SecretsManagerService secretsManagerService) {
        this.secretsManagerService = secretsManagerService;
    }

    @Bean
    public DataSource dataSource() {
        HikariConfig config = new HikariConfig();
        config.setDriverClassName("org.postgresql.Driver");

        // Try to get from Secrets Manager first if not provided via properties
        if (dbUrl.isEmpty() || dbUsername.isEmpty() || dbPassword.isEmpty()) {
            try {
                Map<String, String> secrets = secretsManagerService.getSecret("kkennib/mining/postgresql");

                String url = String.format("jdbc:postgresql://%s:%s/%s",
                        secrets.get("host"),
                        secrets.get("port"),
                        secrets.get("dbname"));

                config.setJdbcUrl(url);
                config.setUsername(secrets.get("username"));
                config.setPassword(secrets.get("password"));
            } catch (Exception e) {
                // If secrets manager fails and no properties provided, let it fail with a better message
                if (dbUrl.isEmpty()) {
                    throw new RuntimeException("Failed to initialize DataSource: AWS Secrets Manager error and no fallback spring.datasource.url provided.", e);
                }
                // If it failed but we somehow have partial properties, use what we have (unlikely due to if condition)
                config.setJdbcUrl(dbUrl);
                config.setUsername(dbUsername);
                config.setPassword(dbPassword);
            }
        } else {
            // Use properties directly if provided
            config.setJdbcUrl(dbUrl);
            config.setUsername(dbUsername);
            config.setPassword(dbPassword);
        }

        return new HikariDataSource(config);
    }
}