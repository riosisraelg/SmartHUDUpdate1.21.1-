package dev.sapphic.smarthud.neoforge;

import dev.sapphic.smarthud.SmartHudClient;
import dev.sapphic.smarthud.config.SlotWhitelist;
import dev.sapphic.smarthud.config.SmartHudConfig;
import dev.sapphic.smarthud.item.TickerQueue;
import dev.sapphic.smarthud.platform.PlatformUtil;
import net.minecraft.client.gui.screen.Screen;
import net.neoforged.fml.common.Mod;

import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Supplier;

/**
 * NeoForge 1.21.1 entrypoint for SmartHUD.
 */
@Mod(SmartHudClient.MOD_ID)
public final class SmartHudNeoForge {

  public SmartHudNeoForge() {
    final Path configDir = PlatformUtil.getConfigDir().resolve(SmartHudClient.MOD_ID);
    try {
      Files.createDirectories(configDir);
    } catch (final IOException e) {
      SmartHudClient.LOGGER.error("Failed to create config directory", e);
    }

    final SmartHudConfig config = SmartHudConfig.getInstance();
    SlotWhitelist.rebuild(config.whitelistEnabled);
    TickerQueue.initialize();

    registerConfigScreen();

    SmartHudClient.LOGGER.info("SmartHUD initialized on NeoForge");
  }

  private static void registerConfigScreen() {
    if (!PlatformUtil.isModLoaded("cloth_config")) {
      return;
    }
    try {
      final Class<?> modLoadingContextClass =
          Class.forName("net.neoforged.fml.ModLoadingContext");
      final Object context = modLoadingContextClass.getMethod("get").invoke(null);
      final Class<?> factoryClass =
          Class.forName("net.neoforged.neoforge.client.gui.IConfigScreenFactory");
      final Object factoryProxy =
          Proxy.newProxyInstance(
              factoryClass.getClassLoader(),
              new Class<?>[] {factoryClass},
              (proxy, method, args) -> {
                if (args != null && args.length == 2 && args[1] instanceof Screen parent) {
                  return SmartHudConfig.getInstance().getConfigScreen(parent);
                }
                if (args != null && args.length == 2 && args[0] instanceof Screen parent) {
                  return SmartHudConfig.getInstance().getConfigScreen(parent);
                }
                return null;
              });
      final Method registerExt =
          modLoadingContextClass.getMethod("registerExtensionPoint", Class.class, Supplier.class);
      registerExt.invoke(context, factoryClass, (Supplier<Object>) () -> factoryProxy);
    } catch (final Throwable e) {
      SmartHudClient.LOGGER.debug("Could not register NeoForge config screen extension", e);
    }
  }
}
