package net.altosheeve.tracking.client.Mixins;

import net.altosheeve.tracking.client.Render.Map;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.render.GameRenderer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Screen.class)
public class ScreenUnblurMapMixin {
    @Shadow
    @Final
    MinecraftClient client;

    @Inject(method = "renderBackground", at = @At("HEAD"), cancellable = true)
    public void renderBackground(CallbackInfo ci) {
        if (Map.renderMap) ci.cancel();
        else System.out.println("rendering background?");
    }

    @Inject(method = "setInitialFocus()V", at = @At("HEAD"), cancellable = true)
    public void setInitialFocus(CallbackInfo ci) {
        if (Map.renderMap) ci.cancel();
        else System.out.println("setting initial focus?");
    }
}
