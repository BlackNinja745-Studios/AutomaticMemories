package org.blackninja745studios.automaticmemories.client.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.packet.s2c.play.AdvancementUpdateS2CPacket;
import org.blackninja745studios.automaticmemories.client.ScreenshotRecorderExt;
import org.blackninja745studios.automaticmemories.client.config.Configuration;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AdvancementUpdateS2CPacket.class)
public class AdvancementUpdateMixin {
    @Inject(method = "<init>(Lnet/minecraft/network/RegistryByteBuf;)V", at = @At("RETURN"))
    public void init(RegistryByteBuf buf, CallbackInfo ci) {
        MinecraftClient client = MinecraftClient.getInstance();
        AdvancementUpdateS2CPacket packet = (AdvancementUpdateS2CPacket) (Object) this;

        if (packet.shouldClearCurrent())
            return;

        boolean containsRealAdvancement = packet.getAdvancementsToEarn().stream().anyMatch(entry -> !entry.value().isRoot());

        if (client != null && Configuration.ENABLED && Configuration.SCREENSHOT_ADVANCEMENT && containsRealAdvancement)
            client.execute(() -> ScreenshotRecorderExt.saveScreenshot(
                    Configuration.getFullDirectory(client.runDirectory, Configuration.SAVE_DIRECTORY),
                    Configuration.ADVANCEMENT_PREFIX,
                    client.getFramebuffer(),
                    "automaticmemories.screenshot.success.special.advancement",
                    ScreenshotRecorderExt.DEFAULT_FAILURE_KEY,
                    msg -> client.execute(() -> {
                        if (Configuration.NOTIFY_PLAYER && client.inGameHud != null && client.world != null)
                            client.inGameHud.getChatHud().addMessage(msg);
                    })
            ));
    }
}
