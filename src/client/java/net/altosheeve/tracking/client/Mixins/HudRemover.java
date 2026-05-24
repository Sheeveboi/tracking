package net.altosheeve.tracking.client.Mixins;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.InGameHud;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)
public class HudRemover {
    @Shadow
    @Final
    MinecraftClient client;

    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    public void renderBackground(CallbackInfo ci) {
        //if (Mapping.renderMap) ci.cancel();
    }
}
