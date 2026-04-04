package kkennib.net.mining.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import kkennib.net.mining.sceretsmanager.service.SecretsManagerService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;
import java.util.Map;


@Configuration
public class DataSourceConfig {

    private final SecretsManagerService secretsManagerService;

    public DataSourceConfig(SecretsManagerService secretsManagerService) {
        this.secretsManagerService = secretsManagerService;
    }

    @Bean
    public DataSource dataSource() {
        Map<String, String> secrets = secretsManagerService.getSecret("neon-kkennibdb-postgreSQL");

        String url = String.format("jdbc:postgresql://%s:%s/%s",
                secrets.get("host"),
                secrets.get("port"),
                secrets.get("dbname"));

        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(url);
        config.setUsername(secrets.get("username"));
        config.setPassword(secrets.get("password"));
        config.setDriverClassName("org.postgresql.Driver");

        return new HikariDataSource(config);
    }
}