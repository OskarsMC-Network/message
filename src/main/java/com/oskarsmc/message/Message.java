package com.oskarsmc.message;

import cloud.commandframework.execution.CommandExecutionCoordinator;
import cloud.commandframework.velocity.CloudInjectionModule;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.oskarsmc.message.command.MessageCommand;
import com.oskarsmc.message.command.ReplyBrigadier;
import com.oskarsmc.message.command.SocialSpyBrigadier;
import com.oskarsmc.message.configuration.MessageSettings;
import com.oskarsmc.message.event.MessageEvent;
import com.oskarsmc.message.util.MessageModule;
import com.oskarsmc.message.util.StatsUtils;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import org.bstats.charts.SingleLineChart;
import org.bstats.velocity.Metrics;
import org.slf4j.Logger;

import java.nio.file.Path;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

public class Message {

    @Inject
    private Logger logger;

    @Inject
    private ProxyServer proxyServer;

    @Inject
    private @DataDirectory
    Path dataDirectory;

    @Inject
    private Metrics.Factory metricsFactory;

    @Inject
    private Injector injector;

    private AtomicInteger messagesSent = new AtomicInteger(0);

    private MessageSettings messageSettings;
    private MessageCommand messageCommand;
    private SocialSpyBrigadier socialSpyBrigadier;
    private ReplyBrigadier replyBrigadier;

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        this.messageSettings = new MessageSettings(dataDirectory.toFile(), logger);

        if (messageSettings.isEnabled()) {
            Metrics metrics = metricsFactory.make(this, StatsUtils.PLUGIN_ID);
            metrics.addCustomChart(new SingleLineChart("messages_sent", new Callable<Integer>() {
                @Override
                public Integer call() {
                    int ret = messagesSent.get();
                    messagesSent.set(0);
                    return ret;
                }
            }));

            final Injector childInjector = injector.createChildInjector(
                    new CloudInjectionModule<>(
                            CommandSource.class,
                            CommandExecutionCoordinator.simpleCoordinator(),
                            Function.identity(),
                            Function.identity()
                    ),
                    new MessageModule(this.messageSettings, this)
            );

            this.messageCommand = childInjector.getInstance(MessageCommand.class);

            this.socialSpyBrigadier = new SocialSpyBrigadier(this.proxyServer, this.messageSettings);
//            this.replyBrigadier = new ReplyBrigadier(this.proxyServer, this.messageSettings, this.messageBrigadier);

            this.proxyServer.getEventManager().register(this, this.socialSpyBrigadier);
            this.proxyServer.getEventManager().register(this, this.replyBrigadier);
        }
    }

    @Subscribe
    public void messageEvent(MessageEvent event) {
        messagesSent.incrementAndGet();
    }
}
