package dev.sapphic.smarthud.mixin;

import dev.sapphic.smarthud.render.SlotRenderer;
import dev.sapphic.smarthud.render.TickerRenderer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.render.RenderTickCounter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Renders SmartHUD overlays at the end of {@link InGameHud#render} on NeoForge.
 */
@Mixin(InGameHud.class)
public abstract class InGameHudMixin {

  @Inject(method = "render", at = @At("TAIL"))
  private void smarthud$onHudRender(
      final DrawContext drawContext, final RenderTickCounter tickCounter, final CallbackInfo ci) {
    final MinecraftClient client = MinecraftClient.getInstance();
    if (!client.options.hudHidden) {
      SlotRenderer.render(drawContext, tickCounter);
      TickerRenderer.render(drawContext, tickCounter);
    }
  }
}
