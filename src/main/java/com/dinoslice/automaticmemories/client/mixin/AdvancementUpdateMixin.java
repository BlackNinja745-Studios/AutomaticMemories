package com.dinoslice.automaticmemories.client.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.protocol.game.ClientboundUpdateAdvancementsPacket;
import com.dinoslice.automaticmemories.client.ScreenshotRecorderExt;
import com.dinoslice.automaticmemories.client.config.Configuration;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientboundUpdateAdvancementsPacket.class)
public class AdvancementUpdateMixin {
    @Inject(method = "<init>(Lnet/minecraft/network/RegistryFriendlyByteBuf;)V", at = @At("RETURN"))
    public void init(RegistryFriendlyByteBuf buf, CallbackInfo ci) {
        Minecraft client = Minecraft.getInstance();
        ClientboundUpdateAdvancementsPacket packet = (ClientboundUpdateAdvancementsPacket) (Object) this;

        if (packet.shouldReset())
            return;

        boolean containsRealAdvancement = packet.getAdded().stream().anyMatch(entry -> !entry.value().isRoot());

        if (client != null && Configuration.ENABLED && Configuration.SCREENSHOT_ADVANCEMENT && containsRealAdvancement)
            client.execute(() -> ScreenshotRecorderExt.saveScreenshot(
                    Configuration.getFullDirectory(client.gameDirectory, Configuration.SAVE_DIRECTORY),
                    Configuration.ADVANCEMENT_PREFIX,
                    client.gameRenderer.mainRenderTarget(),
                    "automaticmemories.screenshot.success.special.advancement",
                    ScreenshotRecorderExt.DEFAULT_FAILURE_KEY,
                    msg -> client.execute(() -> {
                        if (Configuration.NOTIFY_PLAYER && client.gui != null && client.level != null)
                            client.gui.hud.getChat().addClientSystemMessage(msg);
                    })
            ));
    }
}
