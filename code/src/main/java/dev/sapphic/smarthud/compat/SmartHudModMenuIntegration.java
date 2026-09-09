package dev.sapphic.smarthud.compat;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import dev.sapphic.smarthud.config.SmartHudConfig;

/**
 * ModMenu integration point. Provides a config screen factory that
 * delegates to the Cloth Config-based settings screen in {@link SmartHudConfig}.
 */
public final class SmartHudModMenuIntegration implements ModMenuApi {
  @Override
  public ConfigScreenFactory<?> getModConfigScreenFactory() {
    return parent -> SmartHudConfig.getInstance().getConfigScreen(parent);
  }
}
