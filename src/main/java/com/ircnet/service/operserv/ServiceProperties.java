package com.ircnet.service.operserv;

import com.ircnet.library.service.ServiceConfigurationModel;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "service")
@Data
public class ServiceProperties extends ServiceConfigurationModel {
    private String dateFormat = "dd.MM.yyyy HH:mm:ss";
    private int forgetCachedUsersTime;
    private SQuery squery;
    private KlineWebservice klineWebservice;
    private String channel;
    private String clientsChannel;

    @Data
    public static class SQuery {
        private String admin;
        private String info;
    }

    @Data
    public static class KlineWebservice {
        private String url;
        private String username;
        private String password;
    }
}
