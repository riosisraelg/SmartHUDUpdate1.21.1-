package dev.sapphic.smarthud.mixin;

import dev.sapphic.smarthud.item.TickerItem;
import dev.sapphic.smarthud.item.cache.InventoryCache;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public abstract class MinecraftClientMixin {

  @Inject(method = "tick", at = @At("TAIL"))
  private void smarthud$onClientTick(final CallbackInfo ci) {
    final Minecraft client = (Minecraft) (Object) this;
    TickerItem.tick(client);
    InventoryCache.tick(client);
  }
}
