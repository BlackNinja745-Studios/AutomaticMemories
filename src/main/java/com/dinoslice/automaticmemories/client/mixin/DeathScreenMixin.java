package com.dinoslice.automaticmemories.client.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.DeathScreen;
import com.dinoslice.automaticmemories.client.ScreenshotRecorderExt;
import com.dinoslice.automaticmemories.client.config.Configuration;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DeathScreen.class)
public class DeathScreenMixin {
    @Unique
    private static boolean TOOK_FOR_DEATH = false;

    @Inject(method = "init", at = @At("TAIL"))
    public void init(CallbackInfo info) {
        TOOK_FOR_DEATH = false;
    }

    @Inject(method = "extractRenderState", at = @At("TAIL"))
    public void extractRenderState(GuiGraphicsExtractor extractor, int mouseX, int mouseY, float delta, CallbackInfo info) {
        Minecraft client = Minecraft.getInstance();

        if (client != null && Configuration.ENABLED && Configuration.SCREENSHOT_DEATH && !TOOK_FOR_DEATH) {
            client.execute(() -> ScreenshotRecorderExt.saveScreenshot(
                    Configuration.getFullDirectory(client.gameDirectory, Configuration.SAVE_DIRECTORY),
                    Configuration.DEATH_PREFIX,
                    client.gameRenderer.mainRenderTarget(),
                    "automaticmemories.screenshot.success.special.death",
                    ScreenshotRecorderExt.DEFAULT_FAILURE_KEY,
                    msg -> client.execute(() -> {
                        if (Configuration.NOTIFY_PLAYER && client.gui != null && client.level != null)
                            client.gui.hud.getChat().addClientSystemMessage(msg);
                    })
                    )
            );
            TOOK_FOR_DEATH = true;
        }
    }
}
