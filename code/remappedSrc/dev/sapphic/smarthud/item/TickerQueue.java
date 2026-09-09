package dev.sapphic.smarthud.item;

import com.google.common.collect.EvictingQueue;
import dev.sapphic.smarthud.config.SmartHudConfig;
import net.minecraft.world.item.ItemStack;

public final class TickerQueue {
  private static EvictingQueue<TickerItem> queue;

  private TickerQueue() {}

  public static EvictingQueue<TickerItem> get() {
    return queue;
  }

  public static void initialize() {
    queue = EvictingQueue.create(SmartHudConfig.getInstance().ticker.size);
  }

  public static void rebuild() {
    final EvictingQueue<TickerItem> queue = EvictingQueue.create(SmartHudConfig.getInstance().ticker.size);

    queue.addAll(TickerQueue.queue);
    TickerQueue.queue = queue;
  }

  public static void queueItem(final ItemStack stack) {
    if (!stack.isEmpty()) {
      boolean unique = true;

      for (final TickerItem item : queue) {
        if (item.stack().is(stack.getItem())) {
          item.renewTimestamp();
          item.count(stack);

          if (SmartHudConfig.getInstance().ticker.behavior == 0) {
            queue.remove(item);
            queue.add(item);
          }

          unique = false;
          break;
        }
      }

      if (unique) {
        queue.add(new TickerItem(stack));
      }
    }
  }
}
