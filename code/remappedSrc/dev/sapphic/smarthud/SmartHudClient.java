package dev.sapphic.smarthud;

import dev.sapphic.smarthud.config.SlotWhitelist;
import dev.sapphic.smarthud.config.SmartHudConfig;
import dev.sapphic.smarthud.item.TickerItem;
import dev.sapphic.smarthud.item.TickerQueue;
import dev.sapphic.smarthud.item.cache.InventoryCache;
import dev.sapphic.smarthud.platform.PlatformUtil;
import dev.sapphic.smarthud.render.SlotRenderer;
import dev.sapphic.smarthud.render.TickerRenderer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class SmartHudClient implements ClientModInitializer {
    public static final String MOD_ID = "smarthud";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    private static Path configDir;

    public static Path configDir() {
        return configDir;
    }

    @Override
    public void onInitializeClient() {
        configDir = PlatformUtil.getConfigDir().resolve(MOD_ID);
        try {
            Files.createDirectories(configDir);
        } catch (final IOException e) {
            LOGGER.error("Failed to create config directory", e);
        }

        final SmartHudConfig config = SmartHudConfig.getInstance();

        SlotWhitelist.rebuild(config.whitelistEnabled);
        TickerQueue.initialize();

        HudRenderCallback.EVENT.register(SlotRenderer::render);
        HudRenderCallback.EVENT.register(TickerRenderer::render);

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            TickerItem.tick(client);
            InventoryCache.tick(client);
        });

        LOGGER.info("SmartHUD initialized");
    }
}
