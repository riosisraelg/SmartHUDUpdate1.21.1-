package dev.sapphic.smarthud.render;

import com.google.common.collect.ImmutableCollection;
import com.mojang.blaze3d.systems.RenderSystem;
import dev.sapphic.smarthud.config.SlotsConfig;
import dev.sapphic.smarthud.config.SmartHudConfig;
import dev.sapphic.smarthud.item.SlotItem;
import dev.sapphic.smarthud.item.cache.InventoryCache;
import dev.sapphic.smarthud.platform.PlatformUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.option.AttackIndicator;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Arm;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class SlotRenderer {
  private static final Identifier SLOTS_TEXTURE =
      Identifier.of("smarthud", "textures/hud/elements.png");

  private static final int BASE_ORIGIN = 98;
  private static final int OFFHAND_SLOT_SHIFT = 29;
  private static final int ATTACK_INDICATOR_SHIFT = 24;
  private static final boolean INVENTORY_HUD_LOADED = PlatformUtil.isInventoryHudLoaded();

  private SlotRenderer() {}

  public static void render(final DrawContext drawContext, final RenderTickCounter tickCounter) {
    final SlotsConfig slotsConfig = SmartHudConfig.getInstance().slots;
    if (!slotsConfig.enabled) {
      return;
    }

    final MinecraftClient client = MinecraftClient.getInstance();

    if (client.player == null) {
      return;
    }

    final boolean mainArmLeft = client.player.getMainArm() == Arm.LEFT;
    final boolean hasOffhandItem = !client.player.getOffHandStack().isEmpty();
    final boolean invHudActive = slotsConfig.inventoryHudCompat && INVENTORY_HUD_LOADED;
    final boolean hotbarAttackIndicator =
        client.options.getAttackIndicator().getValue() == AttackIndicator.HOTBAR
            && client.player.getAttackCooldownProgress(0.0F) < 1.0F;

    final ImmutableCollection<SlotItem> cache = InventoryCache.get();
    final List<SlotItem> allItems = cache.asList();
    final int maxSlots = Math.max(1, slotsConfig.size);
    final int totalItemsToShow = Math.min(maxSlots, allItems.size());

    final int height = client.getWindow().getScaledHeight();
    final int center = client.getWindow().getScaledWidth() / 2;

    if (!slotsConfig.bothSides) {
      final int slots =
          slotsConfig.includeEmpty
              ? maxSlots
              : Math.max(1, totalItemsToShow);
      final List<SlotItem> sideItems = allItems.subList(0, totalItemsToShow);
      final int origin =
          computeSideOrigin(
              mainArmLeft,
              mainArmLeft,
              hasOffhandItem,
              hotbarAttackIndicator,
              invHudActive,
              slotsConfig);
      renderSide(
          drawContext,
          client,
          sideItems,
          slots,
          mainArmLeft,
          origin,
          height,
          center,
          slotsConfig);
      return;
    }

    // Split items across primary side (opposite offhand) and secondary side (offhand side)
    final List<SlotItem> primaryItems = new ArrayList<>();
    final List<SlotItem> secondaryItems = new ArrayList<>();
    for (int i = 0; i < totalItemsToShow; ++i) {
      if ((i % 2) == 0) {
        primaryItems.add(allItems.get(i));
      } else {
        secondaryItems.add(allItems.get(i));
      }
    }

    final int primarySlots =
        slotsConfig.includeEmpty
            ? ((maxSlots + 1) / 2)
            : primaryItems.size();
    final int secondarySlots =
        slotsConfig.includeEmpty
            ? (maxSlots / 2)
            : secondaryItems.size();

    // Primary side: left if main arm is LEFT, right if main arm is RIGHT
    final boolean primaryIsLeft = mainArmLeft;
    if (primarySlots > 0) {
      final int primaryOrigin =
          computeSideOrigin(
              primaryIsLeft,
              mainArmLeft,
              hasOffhandItem,
              hotbarAttackIndicator,
              invHudActive,
              slotsConfig);
      renderSide(
          drawContext,
          client,
          primaryItems,
          primarySlots,
          primaryIsLeft,
          primaryOrigin,
          height,
          center,
          slotsConfig);
    }

    // Secondary side: opposite to primary side (shares side with offhand slot)
    final boolean secondaryIsLeft = !mainArmLeft;
    if (secondarySlots > 0) {
      final int secondaryOrigin =
          computeSideOrigin(
              secondaryIsLeft,
              mainArmLeft,
              hasOffhandItem,
              hotbarAttackIndicator,
              invHudActive,
              slotsConfig);
      renderSide(
          drawContext,
          client,
          secondaryItems,
          secondarySlots,
          secondaryIsLeft,
          secondaryOrigin,
          height,
          center,
          slotsConfig);
    }
  }

  /**
   * Computes the distance from screen center to the inner edge of the SmartHUD slot bar
   * on the given side of the hotbar, accounting for the vanilla offhand slot, hotbar attack
   * indicator, Inventory HUD+ armor/status columns, and user-configured manual offset.
   */
  private static int computeSideOrigin(
      final boolean sideIsLeft,
      final boolean mainArmLeft,
      final boolean hasOffhandItem,
      final boolean hotbarAttackIndicator,
      final boolean invHudActive,
      final SlotsConfig slotsConfig) {
    int origin = BASE_ORIGIN;

    // Offhand is rendered on the right when mainArmLeft is true, or on the left when false.
    final boolean isOffhandSide = (sideIsLeft != mainArmLeft);

    if (isOffhandSide) {
      // When Inventory HUD+ is installed, it always reserves the offhand slot area plus its quiver/status column
      if (hasOffhandItem || invHudActive) {
        origin += OFFHAND_SLOT_SHIFT;
      }
    } else if (hotbarAttackIndicator && !invHudActive) {
      origin += ATTACK_INDICATOR_SHIFT;
    }

    if (invHudActive) {
      origin += Math.max(0, slotsConfig.inventoryHudOffset);
    }

    origin += slotsConfig.offsetX;

    return Math.max(92, origin);
  }

  private static void renderSide(
      final DrawContext drawContext,
      final MinecraftClient client,
      final List<SlotItem> items,
      final int slots,
      final boolean left,
      final int origin,
      final int height,
      final int center,
      final SlotsConfig slotsConfig) {
    if (items.isEmpty() && !slotsConfig.includeEmpty) {
      return;
    }

    if (slotsConfig.style.isTextured()) {
      final int totalBoxWidth = (20 * slots) + 2;
      final int x = center + (left ? (-origin - totalBoxWidth) : origin);
      final int v = slotsConfig.style.getTextureV();

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

    final int count = Math.min(slots, items.size());
    for (int slot = 0; slot < count; ++slot) {
      final SlotItem item = items.get(slot);
      final ItemStack stack = item.stack();
      final int stackOffset = origin + 3 + (20 * slot);
      final int stackX = left ? (center - stackOffset - 16) : (center + stackOffset);
      final int stackY = height - (16 + 3);

      drawContext.drawItem(stack, stackX, stackY);

      if (!stack.isStackable() && slotsConfig.overlays) {
        drawContext.drawItemInSlot(client.textRenderer, stack, stackX, stackY);
      }

      if ((item.count() > 1) && slotsConfig.counts) {
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
