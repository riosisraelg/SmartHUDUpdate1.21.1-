package dev.sapphic.smarthud.render;

import com.google.common.collect.EvictingQueue;
import dev.sapphic.smarthud.SmartHudClient;
import dev.sapphic.smarthud.config.SmartHudConfig;
import dev.sapphic.smarthud.item.TickerItem;
import dev.sapphic.smarthud.item.TickerQueue;
import dev.sapphic.smarthud.render.interpolation.CubicBezierInterpolator;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.util.Arm;

import java.util.Iterator;

public final class TickerRenderer {
  private static final CubicBezierInterpolator ANIMATION =
      new CubicBezierInterpolator(0.42F, 0.0F, 0.58F, 1.0F);
  private static final float ANIMATION_DURATION = 10.0F;

  private TickerRenderer() {}

  public static void render(final DrawContext drawContext, final RenderTickCounter tickCounter) {
    if (!SmartHudConfig.getInstance().ticker.enabled) {
      return;
    }

    final EvictingQueue<TickerItem> queue = TickerQueue.get();

    if (queue == null || queue.isEmpty()) {
      return;
    }

    final MinecraftClient client = MinecraftClient.getInstance();

    if (client.player == null) {
      return;
    }

    final boolean left = client.player.getMainArm() == Arm.LEFT;
    final int x = SmartHudConfig.getInstance().ticker.style.hasIcons() ? 17 : 4;
    final int fontHeight = client.textRenderer.fontHeight;
    final int screenHeight = client.getWindow().getScaledHeight();
    final int screenWidth = client.getWindow().getScaledWidth();
    final int y = screenHeight - (fontHeight * queue.size()) - (2 * queue.size());
    final Iterator<TickerItem> iterator = queue.iterator();

    for (int index = 0; iterator.hasNext(); ++index) {
      final TickerItem item = iterator.next();
      final int oy = y + (fontHeight * index) + (2 * index);
      final boolean named = SmartHudConfig.getInstance().ticker.style.hasLabels();
      final String locale = SmartHudClient.MOD_ID + ".ticker.label." + (named ? "long" : "short");
      final String itemCount = item.abbreviatedCount();
      final String label = I18n.translate(locale, itemCount, item.stack().getName().getString());
      final int labelWidth = client.textRenderer.getWidth(label);
      float labelX = left ? (screenWidth + (-x - labelWidth)) : x;
      float iconX = left ? (screenWidth + (-(x - 14.0F) - 10.72F)) : (x - 14.0F);
      final long ticks = item.remainingTime();

      if (ticks < 0) {
        final float time = Math.abs(ticks) + tickCounter.getTickDelta(true);

        if (time > ANIMATION_DURATION) {
          iterator.remove();
          return;
        }

        final float endX = (float) x + labelWidth;
        final float interpolation = ANIMATION.interpolate(0, ANIMATION_DURATION, time) * endX;

        labelX += left ? interpolation : -interpolation;
        iconX += left ? interpolation : -interpolation;
      }

      drawContext.drawText(client.textRenderer, label, (int) labelX, oy, 0xFFFFFF, true);

      if (SmartHudConfig.getInstance().ticker.style.hasIcons()) {
        drawContext.getMatrices().push();
        drawContext.getMatrices().translate(iconX, (float) oy - 1.5F, 0.0F);
        drawContext.getMatrices().scale(0.67F, 0.67F, 0.67F);

        drawContext.drawItem(item.stack(), 0, 0);

        drawContext.getMatrices().pop();
      }
    }
  }
}
