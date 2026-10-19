package dev.sapphic.smarthud.item.cache;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import dev.sapphic.smarthud.item.SlotItem;
import dev.sapphic.smarthud.platform.PlatformUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

public final class TrinketCache {
  private static final boolean TRINKETS_LOADED = PlatformUtil.isModLoaded("trinkets");
  private static final boolean ACCESSORIES_LOADED = PlatformUtil.isModLoaded("accessories");
  private static final boolean CURIOS_LOADED = PlatformUtil.isModLoaded("curios");

  private TrinketCache() {}

  public static boolean isAvailable() {
    return TRINKETS_LOADED || ACCESSORIES_LOADED || CURIOS_LOADED;
  }

  public static ImmutableCollection<SlotItem> get() {
    final Minecraft client = Minecraft.getInstance();
    final Player player = client.player;

    if (player == null) {
      return ImmutableList.of();
    }

    final String dimension = player.level().dimension().location().toString();
    final Collection<SlotItem> items = new ArrayList<>(0);

    if (TRINKETS_LOADED) {
      TrinketsHelper.collectItems(player, dimension, items);
    }

    if (ACCESSORIES_LOADED) {
      AccessoriesHelper.collectItems(player, dimension, items);
    }

    if (CURIOS_LOADED) {
      CuriosHelper.collectItems(player, dimension, items);
    }

    return ImmutableList.copyOf(items);
  }

  private static final class TrinketsHelper {
    static void collectItems(
        final Player player, final String dimension, final Collection<SlotItem> items) {
      try {
        dev.emi.trinkets.api.TrinketsApi.getTrinketComponent(player).ifPresent(component -> {
          component.forEach((slotReference, stack) -> {
            if (!stack.isEmpty()) {
              InventoryCache.accountStackAndContents(items, stack, dimension, true);
            }
          });
        });
      } catch (final Throwable ignored) {
      }
    }
  }

  private static final class AccessoriesHelper {
    private static boolean initialized = false;
    private static Method getCapabilityMethod;
    private static Method getAllEquippedMethod;
    private static Method stackMethod;

    private static void init() {
      if (initialized) {
        return;
      }
      initialized = true;
      try {
        final Class<?> capClass =
            Class.forName("io.wispforest.accessories.api.AccessoriesCapability");
        getCapabilityMethod = capClass.getMethod("get", LivingEntity.class);
        getAllEquippedMethod = capClass.getMethod("getAllEquipped");
        final Class<?> slotEntryRefClass =
            Class.forName("io.wispforest.accessories.api.slot.SlotEntryReference");
        stackMethod = slotEntryRefClass.getMethod("stack");
      } catch (final Throwable ignored) {
      }
    }

    static void collectItems(
        final Player player, final String dimension, final Collection<SlotItem> items) {
      init();
      if (getCapabilityMethod == null || getAllEquippedMethod == null || stackMethod == null) {
        return;
      }
      try {
        final Object capability = getCapabilityMethod.invoke(null, player);
        if (capability == null) {
          return;
        }
        final Object equippedList = getAllEquippedMethod.invoke(capability);
        if (equippedList instanceof List<?> list) {
          for (final Object entry : list) {
            if (entry != null) {
              final Object stackObj = stackMethod.invoke(entry);
              if (stackObj instanceof ItemStack stack && !stack.isEmpty()) {
                InventoryCache.accountStackAndContents(items, stack, dimension, true);
              }
            }
          }
        }
      } catch (final Throwable ignored) {
      }
    }
  }

  private static final class CuriosHelper {
    private static boolean initialized = false;
    private static Method getCuriosInventoryMethod;
    private static Method findCuriosMethod;
    private static Method stackMethod;

    private static void init() {
      if (initialized) {
        return;
      }
      initialized = true;
      try {
        final Class<?> curiosApiClass = Class.forName("top.theillusivec4.curios.api.CuriosApi");
        getCuriosInventoryMethod =
            curiosApiClass.getMethod("getCuriosInventory", LivingEntity.class);
        final Class<?> handlerClass =
            Class.forName("top.theillusivec4.curios.api.type.capability.ICuriosItemHandler");
        findCuriosMethod = handlerClass.getMethod("findCurios", Predicate.class);
        final Class<?> slotResultClass = Class.forName("top.theillusivec4.curios.api.SlotResult");
        stackMethod = slotResultClass.getMethod("stack");
      } catch (final Throwable ignored) {
      }
    }

    static void collectItems(
        final Player player, final String dimension, final Collection<SlotItem> items) {
      init();
      if (getCuriosInventoryMethod == null || findCuriosMethod == null || stackMethod == null) {
        return;
      }
      try {
        final Object optionalHandler = getCuriosInventoryMethod.invoke(null, player);
        if (optionalHandler instanceof Optional<?> opt && opt.isPresent()) {
          final Object handler = opt.get();
          final Predicate<ItemStack> nonEmpty = stack -> stack != null && !stack.isEmpty();
          final Object results = findCuriosMethod.invoke(handler, nonEmpty);
          if (results instanceof List<?> list) {
            for (final Object slotResult : list) {
              if (slotResult != null) {
                final Object stackObj = stackMethod.invoke(slotResult);
                if (stackObj instanceof ItemStack stack && !stack.isEmpty()) {
                  InventoryCache.accountStackAndContents(items, stack, dimension, true);
                }
              }
            }
          }
        }
      } catch (final Throwable ignored) {
      }
    }
  }
}
