package dev.sapphic.smarthud.item;

import dev.sapphic.smarthud.config.SmartHudConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.ItemStack;

public final class TickerItem extends CountableItem {
  private static long gameTicks;

  private transient long timestamp = gameTicks;

  public TickerItem(final ItemStack stack) {
    super(stack);
  }

  public static void tick(final MinecraftClient client) {
    if (!client.isPaused()) {
      ++gameTicks;
    }
  }

  public void renewTimestamp() {
    this.timestamp = gameTicks;
  }

  public long remainingTime() {
    return (this.timestamp + SmartHudConfig.getInstance().ticker.durationTicks()) - gameTicks;
  }
}
