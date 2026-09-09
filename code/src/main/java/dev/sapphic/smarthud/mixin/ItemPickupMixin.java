package dev.sapphic.smarthud.mixin;

import dev.sapphic.smarthud.item.TickerQueue;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.network.packet.s2c.play.ItemPickupAnimationS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayNetworkHandler.class)
public abstract class ItemPickupMixin {

  @Inject(method = "onItemPickupAnimation", at = @At("TAIL"))
  private void smarthud$onItemPickup(final ItemPickupAnimationS2CPacket packet, final CallbackInfo ci) {
    final MinecraftClient client = MinecraftClient.getInstance();
    final ClientWorld world = client.world;

    if (world == null || client.player == null) {
      return;
    }

    // Check if the collector is the local player
    final Entity collector = world.getEntityById(packet.getCollectorEntityId());
    if (collector == null || collector.getId() != client.player.getId()) {
      return;
    }

    // Get the item entity that was picked up
    final Entity itemEntity = world.getEntityById(packet.getEntityId());

    if (itemEntity instanceof ItemEntity) {
      TickerQueue.queueItem(((ItemEntity) itemEntity).getStack());
    }
  }
}
