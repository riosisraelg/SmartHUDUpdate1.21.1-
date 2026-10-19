package dev.sapphic.smarthud.mixin;

import dev.sapphic.smarthud.render.SlotRenderer;
import dev.sapphic.smarthud.render.TickerRenderer;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
public abstract class InGameHudMixin {

  @Inject(method = "render", at = @At("TAIL"))
  private void smarthud$onHudRender(
      final GuiGraphics drawContext, final DeltaTracker tickCounter, final CallbackInfo ci) {
    final Minecraft client = Minecraft.getInstance();
    if (!client.options.hideGui) {
      SlotRenderer.render(drawContext, tickCounter);
      TickerRenderer.render(drawContext, tickCounter);
    }
  }
}
