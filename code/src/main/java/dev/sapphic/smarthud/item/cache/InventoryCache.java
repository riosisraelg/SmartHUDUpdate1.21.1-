package dev.sapphic.smarthud.item.cache;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import dev.sapphic.smarthud.item.SlotItem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.BundleContentsComponent;
import net.minecraft.component.type.ContainerComponent;
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

    // Slots 0-8 are hotbar: check inside bundles/containers carried on the hotbar
    final int hotbarSize = PlayerInventory.getHotbarSize();
    for (int slot = 0; slot < hotbarSize && slot < inventory.main.size(); ++slot) {
      final ItemStack stack = inventory.main.get(slot);
      if (!stack.isEmpty()) {
        accountStackAndContents(cache, stack, dimension, false);
      }
    }

    // Offhand: check inside bundles/containers held in offhand
    for (final ItemStack offhandStack : inventory.offHand) {
      if (!offhandStack.isEmpty()) {
        accountStackAndContents(cache, offhandStack, dimension, false);
      }
    }

    // Slots 9-35 are main inventory: check both the item itself and any bundle/container contents
    for (int slot = hotbarSize; slot < inventory.main.size(); ++slot) {
      final ItemStack stack = inventory.main.get(slot);
      if (!stack.isEmpty()) {
        accountStackAndContents(cache, stack, dimension, true);
      }
    }

    // Accessory slots (Trinkets, Accessories API, Curios API)
    if (TrinketCache.isAvailable()) {
      for (final SlotItem trinketItem : TrinketCache.get()) {
        SlotItem.account(cache, trinketItem.stack(), dimension);
      }
    }

    InventoryCache.cache = ImmutableList.copyOf(cache);
  }

  /**
   * Accounts for an {@link ItemStack} and recursively inspects any items stored
   * inside it via {@code minecraft:bundle_contents} or {@code minecraft:container}.
   */
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

    final BundleContentsComponent bundleContents =
        stack.get(DataComponentTypes.BUNDLE_CONTENTS);
    if (bundleContents != null) {
      for (final ItemStack inner : bundleContents.iterate()) {
        if (!inner.isEmpty()) {
          SlotItem.account(cache, inner, dimension);
        }
      }
    }

    final ContainerComponent containerContents =
        stack.get(DataComponentTypes.CONTAINER);
    if (containerContents != null) {
      for (final ItemStack inner : containerContents.iterateNonEmpty()) {
        if (!inner.isEmpty()) {
          SlotItem.account(cache, inner, dimension);
        }
      }
    }
  }
}
