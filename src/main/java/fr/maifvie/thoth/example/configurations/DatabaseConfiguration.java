package fr.maifvie.thoth.example.configurations;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "eventsourcing.database")
public class DatabaseConfiguration {

    public String host;
    public int port;
    public String name;
    public CredentialConfiguration credential;

    public void setHost(String host) {
        this.host = host;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setCredential(CredentialConfiguration credential) {
        this.credential = credential;
    }
}
