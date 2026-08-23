package com.dinoslice.automaticmemories.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.pipeline.RenderTarget;
import net.minecraft.client.Screenshot;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import net.minecraft.util.Util;
import com.dinoslice.automaticmemories.client.config.Configuration;

import java.io.File;
import java.util.function.Consumer;

public class ScreenshotRecorderExt {
    public static final String DEFAULT_SUCCESS_KEY = "automaticmemories.screenshot.success.full";
    public static final String DEFAULT_FAILURE_KEY = "automaticmemories.screenshot.failure";

    public static void saveScreenshot(File saveDirectory, String prefix, RenderTarget framebuffer, Consumer<Component> messageReceiver) {
        saveScreenshot(saveDirectory, prefix, framebuffer, DEFAULT_SUCCESS_KEY, DEFAULT_FAILURE_KEY, messageReceiver);
    }

    public static void saveScreenshot(File saveDirectory, String prefix, RenderTarget framebuffer, String successKey, String failureKey, Consumer<Component> messageReceiver) {
        RenderSystem.assertOnRenderThread();

        saveScreenshotInner(saveDirectory, prefix, framebuffer, successKey, failureKey, messageReceiver);
    }

    private static void saveScreenshotInner(File saveDirectory, String prefix, RenderTarget framebuffer, String successKey, String failureKey, Consumer<Component> messageReceiver) {
        Screenshot.takeScreenshot(framebuffer, (nativeImage) -> {
            boolean ignored = saveDirectory.mkdirs();

            File screenshotFile = assignScreenshotFilename(saveDirectory, prefix);

            Util.ioPool().execute(() -> {
                try {
                    nativeImage.writeToFile(screenshotFile);

                    Component text = Component.translatable("automaticmemories.screenshot.success.clickable")
                            .withStyle(ChatFormatting.UNDERLINE)
                            .withStyle(style -> style.withClickEvent(
                                    new ClickEvent.OpenFile(screenshotFile.getAbsolutePath())
                            ));

                messageReceiver.accept(AutomaticMemories.addChatPrefix(
                    Component.translatable(
                        successKey, text,
                        ScreenshotTimerSingleton.formatTime(Configuration.INTERVAL_MS)
                    )
                ));

                    AutomaticMemories.LOGGER.info("Saved automatic screenshot as {}, next screenshot in {} ms.", screenshotFile.toString(), Configuration.INTERVAL_MS);

                } catch (Exception e) {
                    AutomaticMemories.LOGGER.error("Couldn't save screenshot", e);

                messageReceiver.accept(AutomaticMemories.addChatPrefix(
                    Component.translatable(failureKey, e.getMessage()).withStyle(ChatFormatting.RED)
                ));
            } finally {
                nativeImage.close();
            }

            });
        });
    }

    private static File assignScreenshotFilename(File directory, String prefix) {
        String name = prefix + Util.getFilenameFormattedDateTime();

        int i = 1;
        File file;

        while ((file = new File(directory, name + (i == 1 ? "" : "_" + i) + ".png")).exists()) {
            ++i;
        }

        return file;
    }
}
