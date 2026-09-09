package dev.sapphic.smarthud.item;

import dev.sapphic.smarthud.config.SlotWhitelist;
import dev.sapphic.smarthud.config.SmartHudConfig;
import java.util.Collection;
import net.minecraft.world.item.ItemStack;

public final class SlotItem extends CountableItem {
  private final boolean ignoreNbt;

  private SlotItem(final ItemStack stack, final boolean ignoreNbt) {
    super(stack);
    this.ignoreNbt = ignoreNbt;
  }

  public static void account(
      final Collection<SlotItem> items, final ItemStack stack, final String dimension) {
    for (final SlotWhitelist.Entry entry : SlotWhitelist.entries()) {
      if (!entry.equals(stack, dimension)) {
        continue;
      }

      if (SmartHudConfig.getInstance().slots.cumulative) {
        for (final SlotItem item : items) {
          if (areSimilar(item, stack)) {
            item.count(stack);
            return;
          }
        }
      }

      items.add(new SlotItem(stack, entry.ignoresNbt()));
      return;
    }
  }

  private static boolean areSimilar(final SlotItem item, final ItemStack stack) {
    if (!item.stack().is(stack.getItem())) {
      return false;
    }
    if (item.ignoreNbt) {
      return true;
    }
    return ItemStack.matches(item.stack(), stack);
  }
}
