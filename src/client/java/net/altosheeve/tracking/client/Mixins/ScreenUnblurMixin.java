package net.altosheeve.tracking.client.Mixins;

import net.altosheeve.tracking.client.Mapping.Mapping;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Screen.class)
public class ScreenUnblurMixin {
    @Shadow
    @Final
    MinecraftClient client;

    @Inject(method = "renderBackground", at = @At("HEAD"), cancellable = true)
    public void renderBackground(CallbackInfo ci) {
        if (Mapping.renderMap) ci.cancel();
    }

    @Inject(method = "setInitialFocus()V", at = @At("HEAD"), cancellable = true)
    public void setInitialFocus(CallbackInfo ci) {
        if (Mapping.renderMap) ci.cancel();
    }
}
