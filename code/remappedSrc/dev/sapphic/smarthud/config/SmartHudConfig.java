package dev.sapphic.smarthud.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Central configuration manager for SmartHUD.
 *
 * <p>Persists to {@code config/smarthud.json} using Gson and exposes a
 * Cloth Config API settings screen that can be reached via ModMenu.</p>
 */
public final class SmartHudConfig {
  private static final Logger LOGGER = LoggerFactory.getLogger("SmartHUD");

  private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

  private static final Path CONFIG_PATH =
      FabricLoader.getInstance().getConfigDir().resolve("smarthud.json");

  private static SmartHudConfig instance;

  // ---- Serialized fields ----

  public SlotsConfig slots = new SlotsConfig();
  public TickerConfig ticker = new TickerConfig();

  /** Whether the slot whitelist is enabled. When false, only clocks and compasses are shown. */
  public boolean whitelistEnabled = true;

  // ---- Lifecycle ----

  private SmartHudConfig() {}

  /** Returns the global config instance, loading from disk on first access. */
  public static SmartHudConfig getInstance() {
    if (instance == null) {
      instance = load();
    }
    return instance;
  }

  /** Loads the configuration from disk, returning defaults if the file is missing or corrupt. */
  private static SmartHudConfig load() {
    if (Files.exists(CONFIG_PATH)) {
      try (final Reader reader = Files.newBufferedReader(CONFIG_PATH)) {
        final SmartHudConfig loaded = GSON.fromJson(reader, SmartHudConfig.class);
        if (loaded != null) {
          LOGGER.info("Loaded SmartHUD config from {}", CONFIG_PATH);
          return loaded;
        }
      } catch (final Exception e) {
        LOGGER.error("Failed to load SmartHUD config, using defaults", e);
      }
    }
    final SmartHudConfig defaults = new SmartHudConfig();
    defaults.save();
    return defaults;
  }

  /** Writes the current configuration to disk. */
  public void save() {
    try (final Writer writer = Files.newBufferedWriter(CONFIG_PATH)) {
      GSON.toJson(this, writer);
    } catch (final IOException e) {
      LOGGER.error("Failed to save SmartHUD config", e);
    }
  }

  // ---- Cloth Config screen ----

  /**
   * Builds and returns a Cloth Config settings screen.
   *
   * @param parent the parent screen to return to when this screen is closed
   * @return the configuration {@link Screen}
   */
  public Screen getConfigScreen(final Screen parent) {
    final ConfigBuilder builder = ConfigBuilder.create()
        .setParentScreen(parent)
        .setTitle(Component.translatable("smarthud.config.title"))
        .setSavingRunnable(() -> {
          save();
          SlotWhitelist.rebuild(whitelistEnabled);
        });

    final ConfigEntryBuilder entryBuilder = builder.entryBuilder();

    // ---- Item Slots category ----
    final ConfigCategory slotsCategory =
        builder.getOrCreateCategory(Component.translatable("smarthud.config.slots"));

    slotsCategory.addEntry(entryBuilder
        .startBooleanToggle(Component.translatable("smarthud.config.slots.enabled"), slots.enabled)
        .setDefaultValue(true)
        .setTooltip(Component.translatable("smarthud.config.slots.enabled.tooltip"))
        .setSaveConsumer(val -> slots.enabled = val)
        .build());

    slotsCategory.addEntry(entryBuilder
        .startBooleanToggle(Component.translatable("smarthud.config.slots.includeEmpty"), slots.includeEmpty)
        .setDefaultValue(false)
        .setTooltip(Component.translatable("smarthud.config.slots.includeEmpty.tooltip"))
        .setSaveConsumer(val -> slots.includeEmpty = val)
        .build());

    slotsCategory.addEntry(entryBuilder
        .startEnumSelector(Component.translatable("smarthud.config.slots.style"), SlotsStyle.class, slots.style)
        .setDefaultValue(SlotsStyle.OFFHAND)
        .setTooltip(Component.translatable("smarthud.config.slots.style.tooltip"))
        .setSaveConsumer(val -> slots.style = val)
        .build());

    slotsCategory.addEntry(entryBuilder
        .startBooleanToggle(Component.translatable("smarthud.config.slots.cumulative"), slots.cumulative)
        .setDefaultValue(true)
        .setTooltip(Component.translatable("smarthud.config.slots.cumulative.tooltip"))
        .setSaveConsumer(val -> slots.cumulative = val)
        .build());

    slotsCategory.addEntry(entryBuilder
        .startBooleanToggle(Component.translatable("smarthud.config.slots.overlays"), slots.overlays)
        .setDefaultValue(true)
        .setTooltip(Component.translatable("smarthud.config.slots.overlays.tooltip"))
        .setSaveConsumer(val -> slots.overlays = val)
        .build());

    slotsCategory.addEntry(entryBuilder
        .startBooleanToggle(Component.translatable("smarthud.config.slots.counts"), slots.counts)
        .setDefaultValue(false)
        .setTooltip(Component.translatable("smarthud.config.slots.counts.tooltip"))
        .setSaveConsumer(val -> slots.counts = val)
        .build());

    slotsCategory.addEntry(entryBuilder
        .startIntSlider(Component.translatable("smarthud.config.slots.size"), slots.size, 1, 9)
        .setDefaultValue(3)
        .setTooltip(Component.translatable("smarthud.config.slots.size.tooltip"))
        .setSaveConsumer(val -> slots.size = val)
        .build());

    // ---- Whitelist toggle (under slots) ----
    slotsCategory.addEntry(entryBuilder
        .startBooleanToggle(Component.translatable("smarthud.config.slots.whitelist"), whitelistEnabled)
        .setDefaultValue(true)
        .setTooltip(Component.translatable("smarthud.config.slots.whitelist.tooltip"))
        .setSaveConsumer(val -> whitelistEnabled = val)
        .build());

    // ---- Item Ticker category ----
    final ConfigCategory tickerCategory =
        builder.getOrCreateCategory(Component.translatable("smarthud.config.ticker"));

    tickerCategory.addEntry(entryBuilder
        .startBooleanToggle(Component.translatable("smarthud.config.ticker.enabled"), ticker.enabled)
        .setDefaultValue(true)
        .setTooltip(Component.translatable("smarthud.config.ticker.enabled.tooltip"))
        .setSaveConsumer(val -> ticker.enabled = val)
        .build());

    tickerCategory.addEntry(entryBuilder
        .startIntField(Component.translatable("smarthud.config.ticker.duration"), ticker.durationMillis)
        .setDefaultValue(3000)
        .setMin(500)
        .setMax(30000)
        .setTooltip(Component.translatable("smarthud.config.ticker.duration.tooltip"))
        .setSaveConsumer(val -> ticker.durationMillis = val)
        .build());

    tickerCategory.addEntry(entryBuilder
        .startEnumSelector(Component.translatable("smarthud.config.ticker.style"), TickerStyle.class, ticker.style)
        .setDefaultValue(TickerStyle.BOTH)
        .setTooltip(Component.translatable("smarthud.config.ticker.style.tooltip"))
        .setSaveConsumer(val -> ticker.style = val)
        .build());

    tickerCategory.addEntry(entryBuilder
        .startIntField(Component.translatable("smarthud.config.ticker.size"), ticker.size)
        .setDefaultValue(10)
        .setMin(1)
        .setMax(50)
        .setTooltip(Component.translatable("smarthud.config.ticker.size.tooltip"))
        .setSaveConsumer(val -> ticker.size = val)
        .build());

    tickerCategory.addEntry(entryBuilder
        .startIntSlider(Component.translatable("smarthud.config.ticker.behavior"), ticker.behavior, 0, 1)
        .setDefaultValue(0)
        .setTooltip(Component.translatable("smarthud.config.ticker.behavior.tooltip"))
        .setSaveConsumer(val -> ticker.behavior = val)
        .build());

    return builder.build();
  }
}
