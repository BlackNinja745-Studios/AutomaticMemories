package com.dinoslice.automaticmemories.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.dinoslice.automaticmemories.client.config.Configuration;

public class AutomaticMemories implements ClientModInitializer {
    public static final Logger LOGGER = LogManager.getLogger();

    @Override
    public void onInitializeClient() {

        Configuration.loadFromFile(Configuration.CONFIG_PATH);

        if (Configuration.RESTART_TIMER_EACH_SESSION) {
            ScreenshotTimerSingleton.restartOrStartTimer(Configuration.INTERVAL_MS, Configuration.INTERVAL_MS);
        } else {
            ScreenshotTimerSingleton.restartOrStartTimer(Configuration.LEFTOVER_INTERVAL_MS, Configuration.INTERVAL_MS);
            Configuration.LEFTOVER_INTERVAL_MS = 0;
            Configuration.saveToFile(Configuration.CONFIG_PATH);
        }

        ClientLifecycleEvents.CLIENT_STOPPING.register(client -> {
            Configuration.LEFTOVER_INTERVAL_MS = Configuration.INTERVAL_MS - ScreenshotTimerSingleton.timeSinceLastScreenshot();
            Configuration.saveToFile(Configuration.CONFIG_PATH);
        });
    }

    public static MutableComponent addChatPrefix(Component text) {
        return Component.literal("")
            .append(Component.literal("[").withStyle(ChatFormatting.DARK_GRAY))
            .append(Component.translatable("automaticmemories.chat_prefix.first").withStyle(ChatFormatting.LIGHT_PURPLE))
            .append(Component.translatable("automaticmemories.chat_prefix.second").withStyle(ChatFormatting.BLUE))
            .append(Component.literal("] ").withStyle(ChatFormatting.DARK_GRAY))
            .append(text);
    }
}
