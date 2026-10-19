package dev.sapphic.smarthud.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dev.sapphic.smarthud.platform.PlatformUtil;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

public final class SmartHudConfig {
  private static final Logger LOGGER = LoggerFactory.getLogger("SmartHUD");

  private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

  private static final Path CONFIG_PATH =
      PlatformUtil.getConfigDir().resolve("smarthud.json");

  private static SmartHudConfig instance;

  public SlotsConfig slots = new SlotsConfig();
  public TickerConfig ticker = new TickerConfig();
  public boolean whitelistEnabled = true;

  private SmartHudConfig() {}

  public static SmartHudConfig getInstance() {
    if (instance == null) {
      instance = load();
    }
    return instance;
  }

  private static SmartHudConfig load() {
    if (Files.exists(CONFIG_PATH)) {
      try (final Reader reader = Files.newBufferedReader(CONFIG_PATH)) {
        final SmartHudConfig loaded = GSON.fromJson(reader, SmartHudConfig.class);
        if (loaded != null) {
          if (loaded.slots == null) {
            loaded.slots = new SlotsConfig();
          }
          if (loaded.ticker == null) {
            loaded.ticker = new TickerConfig();
          }
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

  public void save() {
    try (final Writer writer = Files.newBufferedWriter(CONFIG_PATH)) {
      GSON.toJson(this, writer);
    } catch (final IOException e) {
      LOGGER.error("Failed to save SmartHUD config", e);
    }
  }

  public Screen getConfigScreen(final Screen parent) {
    final ConfigBuilder builder = ConfigBuilder.create()
        .setParentScreen(parent)
        .setTitle(Component.translatable("smarthud.config.title"))
        .setSavingRunnable(() -> {
          save();
          SlotWhitelist.rebuild(whitelistEnabled);
        });

    final ConfigEntryBuilder entryBuilder = builder.entryBuilder();

    final ConfigCategory slotsCategory =
        builder.getOrCreateCategory(Component.translatable("smarthud.config.slots"));

    slotsCategory.addEntry(entryBuilder
        .startBooleanToggle(Component.translatable("smarthud.config.slots.enabled"), slots.enabled)
        .setDefaultValue(true)
        .setTooltip(Component.translatable("smarthud.config.slots.enabled.tooltip"))
        .setSaveConsumer(val -> slots.enabled = val)
        .build());

    slotsCategory.addEntry(entryBuilder
        .startBooleanToggle(Component.translatable("smarthud.config.slots.bothSides"), slots.bothSides)
        .setDefaultValue(true)
        .setTooltip(Component.translatable("smarthud.config.slots.bothSides.tooltip"))
        .setSaveConsumer(val -> slots.bothSides = val)
        .build());

    slotsCategory.addEntry(entryBuilder
        .startBooleanToggle(Component.translatable("smarthud.config.slots.inventoryHudCompat"), slots.inventoryHudCompat)
        .setDefaultValue(true)
        .setTooltip(Component.translatable("smarthud.config.slots.inventoryHudCompat.tooltip"))
        .setSaveConsumer(val -> slots.inventoryHudCompat = val)
        .build());

    slotsCategory.addEntry(entryBuilder
        .startIntSlider(Component.translatable("smarthud.config.slots.inventoryHudOffset"), slots.inventoryHudOffset, 0, 120)
        .setDefaultValue(52)
        .setTooltip(Component.translatable("smarthud.config.slots.inventoryHudOffset.tooltip"))
        .setSaveConsumer(val -> slots.inventoryHudOffset = val)
        .build());

    slotsCategory.addEntry(entryBuilder
        .startIntSlider(Component.translatable("smarthud.config.slots.offsetX"), slots.offsetX, -100, 200)
        .setDefaultValue(0)
        .setTooltip(Component.translatable("smarthud.config.slots.offsetX.tooltip"))
        .setSaveConsumer(val -> slots.offsetX = val)
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
        .startIntSlider(Component.translatable("smarthud.config.slots.size"), slots.size, 1, 18)
        .setDefaultValue(6)
        .setTooltip(Component.translatable("smarthud.config.slots.size.tooltip"))
        .setSaveConsumer(val -> slots.size = val)
        .build());

    slotsCategory.addEntry(entryBuilder
        .startBooleanToggle(Component.translatable("smarthud.config.slots.whitelist"), whitelistEnabled)
        .setDefaultValue(true)
        .setTooltip(Component.translatable("smarthud.config.slots.whitelist.tooltip"))
        .setSaveConsumer(val -> whitelistEnabled = val)
        .build());

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
