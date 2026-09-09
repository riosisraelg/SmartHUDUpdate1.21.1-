package dev.sapphic.smarthud.item.cache;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import dev.sapphic.smarthud.item.SlotItem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.Collection;

public final class InventoryCache {
  private static ImmutableCollection<SlotItem> cache = ImmutableList.of();

  private InventoryCache() {}

  public static ImmutableCollection<SlotItem> get() {
    return cache;
  }

  public static void tick(final MinecraftClient client) {
    final PlayerEntity player = client.player;

    if (player == null) {
      return;
    }

    final PlayerInventory inventory = player.getInventory();
    final String dimension = player.getWorld().getRegistryKey().getValue().toString();
    final Collection<SlotItem> cache = new ArrayList<>(0);

    // Slots 0-8 are hotbar, 9-35 are main inventory
    for (int slot = PlayerInventory.getHotbarSize(); slot < inventory.main.size(); ++slot) {
      final ItemStack stack = inventory.main.get(slot);
      SlotItem.account(cache, stack, dimension);
    }

    if (TrinketCache.isAvailable()) {
      cache.addAll(TrinketCache.get());
    }

    InventoryCache.cache = ImmutableList.copyOf(cache);
  }
}
