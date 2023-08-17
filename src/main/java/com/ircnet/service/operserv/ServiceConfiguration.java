package com.ircnet.service.operserv;

import com.ircnet.library.service.IRCServiceTask;
import com.ircnet.service.operserv.irc.IRCUser;
import com.ircnet.service.operserv.kline.KLine;
import com.ircnet.service.operserv.squery.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.util.LinkedCaseInsensitiveMap;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Spring configuration.
 */
@Configuration
@EnableScheduling
@ComponentScan(basePackages = {"com.ircnet.library.common", "com.ircnet.library.service"})
public class ServiceConfiguration {
    @Autowired
    private ServiceProperties properties;

    /**
     * Creates a new IRC service.
     *
     * @return An IRC service
     */
    @Bean
    public IRCServiceTask ircServiceTask() {
        return new IRCServiceTask(properties);
    }

    @Bean
    public SQueryCommandAdmin squeryCommandAdmin() {
        return new SQueryCommandAdmin("ADMIN");
    }

    @Bean
    public SQueryCommandVersion squeryCommandVersion() {
        return new SQueryCommandVersion("VERSION");
    }

    @Bean
    public SQueryCommandInfo squeryCommandInfo() {
        return new SQueryCommandInfo("INFO");
    }

    @Bean
    public SQueryCommandHelp squeryCommandHelp() {
        return new SQueryCommandHelp("HELP");
    }

    @Bean("squeryCommandMap")
    public Map<String, SQueryCommand> squeryCommandMap(SQueryCommandAdmin squeryCommandAdmin,
                                                       SQueryCommandVersion squeryCommandVersion,
                                                       SQueryCommandInfo squeryCommandInfo,
                                                       SQueryCommandHelp squeryCommandHelp) {
        Map<String, SQueryCommand> commandMap = new LinkedCaseInsensitiveMap<>();
        addCommand(commandMap, squeryCommandAdmin);
        addCommand(commandMap, squeryCommandInfo);
        addCommand(commandMap, squeryCommandVersion);
        addCommand(commandMap, squeryCommandHelp);
        squeryCommandHelp.setSqueryCommandMap(commandMap);
        return commandMap;
    }

    private void addCommand(Map<String, SQueryCommand> commandMap, SQueryCommand squeryCommand) {
        commandMap.put(squeryCommand.getCommandName(), squeryCommand);
    }

    /**
     * A map containing all irc users mapped by UID.
     */
    @Bean("userMapByUID")
    public Map<String, IRCUser> userMapByUID() {
        return new ConcurrentHashMap<>();
    }

    /**
     * A map containing all IRC users mapped by nick.
     */
    @Bean("userMapByNick")
    public Map<String, IRCUser> userMapByNick() {
        return new ConcurrentHashMap<>();
    }

    /**
     * List of K-Lines.
     */
    @Bean("klineList")
    public List<KLine> klineList() {
        return new CopyOnWriteArrayList();
    }

    /**
     * REST client.
     */
    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder restTemplateBuilder) {
        return restTemplateBuilder.basicAuthentication(properties.getKlineWebservice().getUsername(), properties.getKlineWebservice().getPassword()).build();
    }
}
