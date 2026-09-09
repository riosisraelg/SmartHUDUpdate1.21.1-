package dev.sapphic.smarthud.item.cache;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import dev.sapphic.smarthud.item.SlotItem;
import java.util.ArrayList;
import java.util.Collection;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public final class InventoryCache {
  private static ImmutableCollection<SlotItem> cache = ImmutableList.of();

  private InventoryCache() {}

  public static ImmutableCollection<SlotItem> get() {
    return cache;
  }

  public static void tick(final Minecraft client) {
    final Player player = client.player;

    if (player == null) {
      return;
    }

    final Inventory inventory = player.getInventory();
    final String dimension = player.level().dimension().location().toString();
    final Collection<SlotItem> cache = new ArrayList<>(0);

    // Slots 0-8 are hotbar, 9-35 are main inventory
    for (int slot = Inventory.getSelectionSize(); slot < 36; ++slot) {
      final ItemStack stack = inventory.getItem(slot);
      SlotItem.account(cache, stack, dimension);
    }

    if (TrinketCache.isAvailable()) {
      cache.addAll(TrinketCache.get());
    }

    InventoryCache.cache = ImmutableList.copyOf(cache);
  }
}
