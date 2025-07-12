package com.dinoslice.automaticmemories.client;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.util.ScreenshotRecorder;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Util;
import com.dinoslice.automaticmemories.client.config.Configuration;

import java.io.File;
import java.util.function.Consumer;

public class ScreenshotRecorderExt {
    public static final String DEFAULT_SUCCESS_KEY = "automaticmemories.screenshot.success.full";
    public static final String DEFAULT_FAILURE_KEY = "automaticmemories.screenshot.failure";

    public static void saveScreenshot(File saveDirectory, String prefix, Framebuffer framebuffer, Consumer<Text> messageReceiver) {
        saveScreenshot(saveDirectory, prefix, framebuffer, DEFAULT_SUCCESS_KEY, DEFAULT_FAILURE_KEY, messageReceiver);
    }

    public static void saveScreenshot(File saveDirectory, String prefix, Framebuffer framebuffer, String successKey, String failureKey, Consumer<Text> messageReceiver) {
        if (RenderSystem.isOnRenderThread())
            saveScreenshotInner(saveDirectory, prefix, framebuffer, successKey, failureKey, messageReceiver);
        else
            RenderSystem.recordRenderCall(() -> saveScreenshotInner(saveDirectory, prefix, framebuffer, successKey, failureKey, messageReceiver));
    }

    private static void saveScreenshotInner(File saveDirectory, String prefix, Framebuffer framebuffer, String successKey, String failureKey, Consumer<Text> messageReceiver) {
        NativeImage nativeImage = ScreenshotRecorder.takeScreenshot(framebuffer);
        boolean ignored = saveDirectory.mkdirs();

        File screenshotFile = assignScreenshotFilename(saveDirectory, prefix);

        Util.getIoWorkerExecutor().execute(() -> {
            try {
                nativeImage.writeTo(screenshotFile);

                Text text = Text.translatable("automaticmemories.screenshot.success.clickable")
                    .formatted(Formatting.UNDERLINE)
                    .styled(style -> style.withClickEvent(
                        new ClickEvent(ClickEvent.Action.OPEN_FILE, screenshotFile.getAbsolutePath())
                    ));

                messageReceiver.accept(AutomaticMemories.addChatPrefix(
                    Text.translatable(
                        successKey, text,
                        ScreenshotTimerSingleton.formatTime(Configuration.INTERVAL_MS)
                    )
                ));

                AutomaticMemories.LOGGER.info("Saved automatic screenshot as {}, next screenshot in {} ms.", screenshotFile.toString(), Configuration.INTERVAL_MS);

            } catch (Exception e) {
                AutomaticMemories.LOGGER.error("Couldn't save screenshot", e);

                messageReceiver.accept(AutomaticMemories.addChatPrefix(
                    Text.translatable(failureKey, e.getMessage()).formatted(Formatting.RED)
                ));
            } finally {
                nativeImage.close();
            }

        });
    }

    private static File assignScreenshotFilename(File directory, String prefix) {
        String name = prefix + Util.getFormattedCurrentTime();

        int i = 1;
        File file;

        while ((file = new File(directory, name + (i == 1 ? "" : "_" + i) + ".png")).exists()) {
            ++i;
        }

        return file;
    }
}
