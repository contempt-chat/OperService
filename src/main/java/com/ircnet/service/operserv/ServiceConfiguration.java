package com.ircnet.service.operserv;

import com.ircnet.library.common.configuration.IRCServerModel;
import com.ircnet.library.common.configuration.ServerModel;
import com.ircnet.library.service.IRCServiceTask;
import com.ircnet.library.service.ServiceConfigurationModel;
import com.ircnet.service.operserv.irc.IRCUser;
import com.ircnet.service.operserv.kline.KLine;
import com.ircnet.service.operserv.squery.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.util.LinkedCaseInsensitiveMap;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Spring configuration.
 */
@Configuration
@EnableScheduling
@ComponentScan(basePackages = {"com.ircnet.library.common", "com.ircnet.library.service"})
public class ServiceConfiguration {
    @Value("${service.name}")
    private String serviceName;

    @Value("${service.distributionMask}")
    private String serviceDistributionMask;

    @Value("${service.info}")
    private String serviceInfo;

    @Value("${service.password}")
    private String servicePassword;

    @Value("${service.type}")
    private int serviceType;

    @Value("${service.dataFlags}")
    private int serviceDataFlags;

    @Value("${service.burstFlags}")
    private int serviceBurstFlags;

    @Value("${ircserver.host}")
    private String ircServerHost;

    @Value("${ircserver.port}")
    private int ircServerPort;

    @Value("${ircserver.protocol:#{null}}")
    private String ircServerProtocol;

    @Value("${sasl-webservice.kline.username}")
    private String apiUsername;

    @Value("${sasl-webservice.kline.password}")
    private String apiPassword;

    /**
     * Creates a new IRC service.
     *
     * @return An IRC service
     */
    @Bean
    public IRCServiceTask ircServiceTask() {
        IRCServerModel ircServerModel = new IRCServerModel();
        ircServerModel.setHostname(ircServerHost);
        ircServerModel.setPort(ircServerPort);

        if (!StringUtils.isEmpty(ircServerProtocol)) {
            ircServerModel.setProtocol(ServerModel.Protocol.valueOf(ircServerProtocol.toUpperCase()));
        }

        ServiceConfigurationModel serviceConfiguration = new ServiceConfigurationModel();
        serviceConfiguration.setServiceName(serviceName);
        serviceConfiguration.setDistributionMask(serviceDistributionMask);
        serviceConfiguration.setServiceType(serviceType);
        serviceConfiguration.setDataFlags(serviceDataFlags);
        serviceConfiguration.setBurstFlags(serviceBurstFlags);
        serviceConfiguration.setInfo(serviceInfo);
        serviceConfiguration.setPassword(servicePassword);
        serviceConfiguration.setIrcServers(Collections.singletonList(ircServerModel));

        return new IRCServiceTask(serviceConfiguration);
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
        return restTemplateBuilder.basicAuthentication(apiUsername, apiPassword).build();
    }
}
