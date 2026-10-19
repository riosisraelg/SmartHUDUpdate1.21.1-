package dev.sapphic.smarthud.mixin;

import dev.sapphic.smarthud.item.TickerQueue;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundTakeItemEntityPacket;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPacketListener.class)
public abstract class ItemPickupMixin {

  @Inject(method = "handleTakeItemEntity", at = @At("TAIL"))
  private void smarthud$onItemPickup(final ClientboundTakeItemEntityPacket packet, final CallbackInfo ci) {
    final Minecraft client = Minecraft.getInstance();
    final ClientLevel world = client.level;

    if (world == null || client.player == null) {
      return;
    }

    final Entity collector = world.getEntity(packet.getPlayerId());
    if (collector == null || collector.getId() != client.player.getId()) {
      return;
    }

    final Entity itemEntity = world.getEntity(packet.getItemId());

    if (itemEntity instanceof ItemEntity) {
      TickerQueue.queueItem(((ItemEntity) itemEntity).getItem());
    }
  }
}
