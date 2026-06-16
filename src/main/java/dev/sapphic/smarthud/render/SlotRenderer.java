package dev.sapphic.smarthud.render;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.UnmodifiableListIterator;
import com.mojang.blaze3d.systems.RenderSystem;
import dev.sapphic.smarthud.config.SmartHudConfig;
import dev.sapphic.smarthud.item.SlotItem;
import dev.sapphic.smarthud.item.cache.InventoryCache;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Arm;
import net.minecraft.util.Identifier;

public final class SlotRenderer {
  private static final Identifier SLOTS_TEXTURE =
      Identifier.of("smarthud", "textures/hud/elements.png");

  private SlotRenderer() {}

  public static void render(final DrawContext drawContext, final RenderTickCounter tickCounter) {
    if (!SmartHudConfig.getInstance().slots.enabled) {
      return;
    }

    final MinecraftClient client = MinecraftClient.getInstance();

    if (client.player == null) {
      return;
    }

    final boolean left = client.player.getMainArm() == Arm.LEFT;
    final ImmutableCollection<SlotItem> cache = InventoryCache.get();
    final int slots = Math.max(1, Math.min(SmartHudConfig.getInstance().slots.size, cache.size()));
    final int height = client.getWindow().getScaledHeight();
    final int center = client.getWindow().getScaledWidth() / 2;
    final int origin = 98;

    if (SmartHudConfig.getInstance().slots.style.isTextured() && (!cache.isEmpty() || SmartHudConfig.getInstance().slots.includeEmpty)) {
      final int x = center + (left ? (-origin - ((44 + (20 * (slots - 2))) - 2)) : origin);
      final int v = SmartHudConfig.getInstance().slots.style.getTextureV();

      // Draw the left cap of the slot background
      drawContext.drawTexture(SLOTS_TEXTURE, x, height - 22, 0, v, 11, 22, 256, 256);

      // Draw the middle segments
      for (int i = 0; i < ((slots - 1) * 2); ++i) {
        final int slotX = x + 11 + (10 * i);
        final int slotU = ((i % 2) == 0) ? 32 : 22;

        drawContext.drawTexture(SLOTS_TEXTURE, slotX, height - 22, slotU, v, 10, 22, 256, 256);
      }

      // Draw the right cap of the slot background
      drawContext.drawTexture(SLOTS_TEXTURE, (x + (20 * slots)) - 9, height - 22, 11, v, 11, 22, 256, 256);
    }

    final UnmodifiableListIterator<SlotItem> it =
        cache.asList().listIterator(left ? cache.size() : 0);

    for (int slot = 0; (left ? it.hasPrevious() : it.hasNext()) && (slot < slots); ++slot) {
      final SlotItem item = left ? it.previous() : it.next();
      final ItemStack stack = item.stack();
      final int stackOffset = origin + 3 + (20 * slot);
      final int stackX = left ? (center - stackOffset - 16) : (center + stackOffset);
      final int stackY = height - (16 + 3);

      drawContext.drawItem(stack, stackX, stackY);

      if (!stack.isStackable() && SmartHudConfig.getInstance().slots.overlays) {
        drawContext.drawItemInSlot(client.textRenderer, stack, stackX, stackY);
      }

      if ((item.count() > 1) && SmartHudConfig.getInstance().slots.counts) {
        final String label = item.abbreviatedCount();
        final int width = client.textRenderer.getWidth(label);
        final int labelOrigin = origin + (20 - width) + (20 * slot);
        final int labelX = left ? ((-labelOrigin - width) + (18 - width)) : labelOrigin;
        final int labelY = height - client.textRenderer.fontHeight - 1;

        RenderSystem.disableDepthTest();
        drawContext.drawText(client.textRenderer, label, center + labelX, labelY, 0xFFFFFFFF, true);
        RenderSystem.enableDepthTest();
      }
    }
  }
}
