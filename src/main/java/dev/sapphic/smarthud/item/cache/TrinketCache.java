package dev.sapphic.smarthud.item.cache;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import dev.sapphic.smarthud.item.SlotItem;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.Collection;

public final class TrinketCache {
  private static final boolean TRINKETS_LOADED;

  static {
    TRINKETS_LOADED = FabricLoader.getInstance().isModLoaded("trinkets");
  }

  private TrinketCache() {}

  public static boolean isAvailable() {
    return TRINKETS_LOADED;
  }

  public static ImmutableCollection<SlotItem> get() {
    final MinecraftClient client = MinecraftClient.getInstance();
    final PlayerEntity player = client.player;

    if (player == null) {
      return ImmutableList.of();
    }

    final String dimension = player.getWorld().getRegistryKey().getValue().toString();

    return TrinketsHelper.getItems(player, dimension);
  }

  /**
   * Isolated inner class so that Trinkets API classes are only loaded when
   * this class is actually referenced — which only happens after we've
   * confirmed Trinkets is present via {@link #isAvailable()}.
   */
  private static final class TrinketsHelper {
    static ImmutableCollection<SlotItem> getItems(final PlayerEntity player, final String dimension) {
      final Collection<SlotItem> items = new ArrayList<>(0);

      dev.emi.trinkets.api.TrinketsApi.getTrinketComponent(player).ifPresent(component -> {
        component.forEach((slotReference, stack) -> {
          if (!stack.isEmpty()) {
            SlotItem.account(items, stack, dimension);
          }
        });
      });

      return ImmutableList.copyOf(items);
    }
  }
}
