package dev.sapphic.smarthud.render;

import com.google.common.collect.ImmutableCollection;
import com.mojang.blaze3d.systems.RenderSystem;
import dev.sapphic.smarthud.config.SlotsConfig;
import dev.sapphic.smarthud.config.SmartHudConfig;
import dev.sapphic.smarthud.item.SlotItem;
import dev.sapphic.smarthud.item.cache.InventoryCache;
import dev.sapphic.smarthud.platform.PlatformUtil;
import net.minecraft.client.AttackIndicatorStatus;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public final class SlotRenderer {
  private static final ResourceLocation SLOTS_TEXTURE =
      ResourceLocation.fromNamespaceAndPath("smarthud", "textures/hud/elements.png");

  private static final int BASE_ORIGIN = 98;
  private static final int OFFHAND_SLOT_SHIFT = 29;
  private static final int ATTACK_INDICATOR_SHIFT = 24;
  private static final boolean INVENTORY_HUD_LOADED = PlatformUtil.isInventoryHudLoaded();

  private SlotRenderer() {}

  public static void render(final GuiGraphics drawContext, final DeltaTracker tickCounter) {
    final SlotsConfig slotsConfig = SmartHudConfig.getInstance().slots;
    if (!slotsConfig.enabled) {
      return;
    }

    final Minecraft client = Minecraft.getInstance();

    if (client.player == null) {
      return;
    }

    final boolean mainArmLeft = client.player.getMainArm() == HumanoidArm.LEFT;
    final boolean hasOffhandItem = !client.player.getOffhandItem().isEmpty();
    final boolean invHudActive = slotsConfig.inventoryHudCompat && INVENTORY_HUD_LOADED;
    final boolean hotbarAttackIndicator =
        client.options.attackIndicator().get() == AttackIndicatorStatus.HOTBAR
            && client.player.getAttackStrengthScale(0.0F) < 1.0F;

    final ImmutableCollection<SlotItem> cache = InventoryCache.get();
    final List<SlotItem> allItems = cache.asList();
    final int maxSlots = Math.max(1, slotsConfig.size);
    final int totalItemsToShow = Math.min(maxSlots, allItems.size());

    final int height = client.getWindow().getGuiScaledHeight();
    final int center = client.getWindow().getGuiScaledWidth() / 2;

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

  private static int computeSideOrigin(
      final boolean sideIsLeft,
      final boolean mainArmLeft,
      final boolean hasOffhandItem,
      final boolean hotbarAttackIndicator,
      final boolean invHudActive,
      final SlotsConfig slotsConfig) {
    int origin = BASE_ORIGIN;

    final boolean isOffhandSide = (sideIsLeft != mainArmLeft);

    if (isOffhandSide) {
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
      final GuiGraphics drawContext,
      final Minecraft client,
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

      drawContext.blit(SLOTS_TEXTURE, x, height - 22, 0f, (float) v, 11, 22, 256, 256);

      for (int i = 0; i < ((slots - 1) * 2); ++i) {
        final int slotX = x + 11 + (10 * i);
        final int slotU = ((i % 2) == 0) ? 32 : 22;

        drawContext.blit(SLOTS_TEXTURE, slotX, height - 22, (float) slotU, (float) v, 10, 22, 256, 256);
      }

      drawContext.blit(SLOTS_TEXTURE, (x + (20 * slots)) - 9, height - 22, 11f, (float) v, 11, 22, 256, 256);
    }

    final int count = Math.min(slots, items.size());
    for (int slot = 0; slot < count; ++slot) {
      final SlotItem item = items.get(slot);
      final ItemStack stack = item.stack();
      final int stackOffset = origin + 3 + (20 * slot);
      final int stackX = left ? (center - stackOffset - 16) : (center + stackOffset);
      final int stackY = height - (16 + 3);

      drawContext.renderItem(stack, stackX, stackY);

      if (!stack.isStackable() && slotsConfig.overlays) {
        drawContext.renderItemDecorations(client.font, stack, stackX, stackY);
      }

      if ((item.count() > 1) && slotsConfig.counts) {
        final String label = item.abbreviatedCount();
        final int width = client.font.width(label);
        final int labelOrigin = origin + (20 - width) + (20 * slot);
        final int labelX = left ? ((-labelOrigin - width) + (18 - width)) : labelOrigin;
        final int labelY = height - client.font.lineHeight - 1;

        RenderSystem.disableDepthTest();
        drawContext.drawString(client.font, label, center + labelX, labelY, 0xFFFFFFFF, true);
        RenderSystem.enableDepthTest();
      }
    }
  }
}
