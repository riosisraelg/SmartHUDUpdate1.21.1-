package dev.sapphic.smarthud.mixin;

import dev.sapphic.smarthud.item.TickerItem;
import dev.sapphic.smarthud.item.cache.InventoryCache;
import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Drives SmartHUD client tick updates at the end of {@link MinecraftClient#tick} on NeoForge.
 */
@Mixin(MinecraftClient.class)
public abstract class MinecraftClientMixin {

  @Inject(method = "tick", at = @At("TAIL"))
  private void smarthud$onClientTick(final CallbackInfo ci) {
    final MinecraftClient client = (MinecraftClient) (Object) this;
    TickerItem.tick(client);
    InventoryCache.tick(client);
  }
}
