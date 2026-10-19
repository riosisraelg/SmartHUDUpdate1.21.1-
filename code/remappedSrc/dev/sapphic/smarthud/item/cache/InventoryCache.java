package dev.sapphic.smarthud.item.cache;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import dev.sapphic.smarthud.item.SlotItem;
import net.minecraft.client.Minecraft;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.BundleContents;
import net.minecraft.world.item.component.ItemContainerContents;

import java.util.ArrayList;
import java.util.Collection;

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

    final int hotbarSize = Inventory.getSelectionSize();
    for (int slot = 0; slot < hotbarSize && slot < inventory.items.size(); ++slot) {
      final ItemStack stack = inventory.items.get(slot);
      if (!stack.isEmpty()) {
        accountStackAndContents(cache, stack, dimension, false);
      }
    }

    for (final ItemStack offhandStack : inventory.offhand) {
      if (!offhandStack.isEmpty()) {
        accountStackAndContents(cache, offhandStack, dimension, false);
      }
    }

    for (int slot = hotbarSize; slot < inventory.items.size(); ++slot) {
      final ItemStack stack = inventory.items.get(slot);
      if (!stack.isEmpty()) {
        accountStackAndContents(cache, stack, dimension, true);
      }
    }

    if (TrinketCache.isAvailable()) {
      for (final SlotItem trinketItem : TrinketCache.get()) {
        SlotItem.account(cache, trinketItem.stack(), dimension);
      }
    }

    InventoryCache.cache = ImmutableList.copyOf(cache);
  }

  public static void accountStackAndContents(
      final Collection<SlotItem> cache,
      final ItemStack stack,
      final String dimension,
      final boolean includeRoot) {
    if (stack.isEmpty()) {
      return;
    }

    if (includeRoot) {
      SlotItem.account(cache, stack, dimension);
    }

    final BundleContents bundleContents = stack.get(DataComponents.BUNDLE_CONTENTS);
    if (bundleContents != null) {
      for (final ItemStack inner : bundleContents.items()) {
        if (!inner.isEmpty()) {
          SlotItem.account(cache, inner, dimension);
        }
      }
    }

    final ItemContainerContents containerContents = stack.get(DataComponents.CONTAINER);
    if (containerContents != null) {
      for (final ItemStack inner : containerContents.nonEmptyItems()) {
        if (!inner.isEmpty()) {
          SlotItem.account(cache, inner, dimension);
        }
      }
    }
  }
}
