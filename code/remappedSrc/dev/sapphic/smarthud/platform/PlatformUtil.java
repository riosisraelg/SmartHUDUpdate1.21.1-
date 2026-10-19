package dev.sapphic.smarthud.platform;

import java.lang.reflect.Method;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Cross-loader utility for querying config paths and loaded mods across
 * Fabric and NeoForge without hard compile-time loader coupling.
 */
public final class PlatformUtil {
  private static final Path CONFIG_DIR = resolveConfigDir();

  private PlatformUtil() {}

  public static Path getConfigDir() {
    return CONFIG_DIR;
  }

  public static boolean isModLoaded(final String modId) {
    try {
      final Class<?> fabricLoaderClass = Class.forName("net.fabricmc.loader.api.FabricLoader");
      final Object instance = fabricLoaderClass.getMethod("getInstance").invoke(null);
      final Method isModLoaded = fabricLoaderClass.getMethod("isModLoaded", String.class);
      return (boolean) isModLoaded.invoke(instance, modId);
    } catch (final Throwable ignored) {
    }

    try {
      final Class<?> modListClass = Class.forName("net.neoforged.fml.ModList");
      final Object modList = modListClass.getMethod("get").invoke(null);
      if (modList != null) {
        final Method isLoaded = modListClass.getMethod("isLoaded", String.class);
        return (boolean) isLoaded.invoke(modList, modId);
      }
    } catch (final Throwable ignored) {
    }

    try {
      final Class<?> fmlLoaderClass = Class.forName("net.neoforged.fml.loading.FMLLoader");
      final Object loadingModList = fmlLoaderClass.getMethod("getLoadingModList").invoke(null);
      if (loadingModList != null) {
        final Method getModFileById = loadingModList.getClass().getMethod("getModFileById", String.class);
        return getModFileById.invoke(loadingModList, modId) != null;
      }
    } catch (final Throwable ignored) {
    }

    return false;
  }

  public static boolean isInventoryHudLoaded() {
    return isModLoaded("inventoryhud") || isModLoaded("inventory_hud");
  }

  private static Path resolveConfigDir() {
    try {
      final Class<?> fabricLoaderClass = Class.forName("net.fabricmc.loader.api.FabricLoader");
      final Object instance = fabricLoaderClass.getMethod("getInstance").invoke(null);
      return (Path) fabricLoaderClass.getMethod("getConfigDir").invoke(instance);
    } catch (final Throwable ignored) {
    }

    try {
      final Class<?> fmlPathsClass = Class.forName("net.neoforged.fml.loading.FMLPaths");
      final Object configDirEnum = Enum.valueOf((Class<Enum>) fmlPathsClass.asSubclass(Enum.class), "CONFIGDIR");
      return (Path) fmlPathsClass.getMethod("get").invoke(configDirEnum);
    } catch (final Throwable ignored) {
    }

    return Paths.get("config");
  }
}
