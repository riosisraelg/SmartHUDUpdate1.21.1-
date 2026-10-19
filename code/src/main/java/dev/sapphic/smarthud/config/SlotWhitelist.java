package dev.sapphic.smarthud.config;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableSet;
import com.google.common.reflect.TypeToken;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.annotations.JsonAdapter;
import dev.sapphic.smarthud.platform.PlatformUtil;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Manages the item whitelist that controls which items appear in the HUD slots.
 * When the whitelist is disabled, only clocks and compasses are shown.
 */
public final class SlotWhitelist {
  private static final Logger LOGGER = LoggerFactory.getLogger("SmartHUD");

  private static final Gson GSON =
      new GsonBuilder()
          .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
          .setPrettyPrinting()
          .setLenient()
          .create();

  @SuppressWarnings("serial")
  private static final Type TYPE =
      new TypeToken<Set<Entry>>() {}.getType();

  private static ImmutableCollection<Entry> allEntries = ImmutableSet.of();
  private static ImmutableCollection<Entry> entries = ImmutableSet.of();
  private static boolean currentWhitelistEnabled = true;
  private static int lastRegistrySize = -1;

  private SlotWhitelist() {}

  public static ImmutableCollection<Entry> allEntries() {
    return allEntries;
  }

  public static ImmutableCollection<Entry> entries() {
    final int currentSize = Registries.ITEM.size();
    if (currentWhitelistEnabled && currentSize != lastRegistrySize && !allEntries.isEmpty()) {
      lastRegistrySize = currentSize;
      entries =
          allEntries.stream()
              .filter(SlotWhitelist::ensureRegistered)
              .collect(ImmutableSet.toImmutableSet());
    }
    return entries;
  }

  /**
   * Rebuilds the whitelist from the config file (or uses defaults when disabled).
   *
   * @param whitelistEnabled whether the whitelist feature is enabled
   */
  public static void rebuild(boolean whitelistEnabled) {
    currentWhitelistEnabled = whitelistEnabled;
    lastRegistrySize = Registries.ITEM.size();
    if (whitelistEnabled) {
      final Set<Entry> builtInDefaults = loadBuiltInDefaults();
      final Path configPath = generateConfigs();
      try (final Reader reader = Files.newBufferedReader(configPath, StandardCharsets.UTF_8)) {
        final Set<Entry> loaded = GSON.fromJson(reader, TYPE);
        final Set<Entry> merged = new LinkedHashSet<>();
        if (loaded != null) {
          merged.addAll(loaded);
        }
        // Ensure all built-in defaults (e.g. newly added mod compatibility items) are present
        boolean updated = false;
        for (final Entry def : builtInDefaults) {
          final boolean alreadyHasItem = merged.stream().anyMatch(e -> e.item.equals(def.item));
          if (!alreadyHasItem) {
            merged.add(def);
            updated = true;
          }
        }
        if (updated) {
          try (final Writer writer = Files.newBufferedWriter(configPath, StandardCharsets.UTF_8)) {
            GSON.toJson(merged, TYPE, writer);
          } catch (final IOException e) {
            LOGGER.warn("Failed to update whitelist with new default items", e);
          }
        }
        allEntries = ImmutableSet.copyOf(merged);
        entries =
            allEntries.stream()
                .filter(SlotWhitelist::ensureRegistered)
                .collect(ImmutableSet.toImmutableSet());
      } catch (final Exception e) {
        LOGGER.error("Failed to load whitelist, falling back to built-in defaults", e);
        allEntries = ImmutableSet.copyOf(builtInDefaults);
        entries =
            allEntries.stream()
                .filter(SlotWhitelist::ensureRegistered)
                .collect(ImmutableSet.toImmutableSet());
      }
    } else {
      entries = ImmutableSet.of(
          new Entry(Registries.ITEM.getId(Items.COMPASS), false),
          new Entry(Registries.ITEM.getId(Items.RECOVERY_COMPASS), true),
          new Entry(Registries.ITEM.getId(Items.CLOCK), true)
      );
    }
  }

  private static Set<Entry> loadBuiltInDefaults() {
    try (final @Nullable InputStream stream =
        SlotWhitelist.class.getResourceAsStream("/defaults.json")) {
      if (stream != null) {
        try (final Reader reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
          final Set<Entry> defaults = GSON.fromJson(reader, TYPE);
          if (defaults != null) {
            return defaults;
          }
        }
      }
    } catch (final Exception e) {
      LOGGER.error("Failed to read built-in defaults.json", e);
    }
    return Collections.emptySet();
  }

  /** Returns the config directory for SmartHUD whitelist files. */
  private static Path configDir() {
    return PlatformUtil.getConfigDir().resolve("smarthud");
  }

  /**
   * Copies the built-in defaults.json to the user config directory if it does not
   * already exist, then returns the path to the user-editable whitelist file.
   */
  private static Path generateConfigs() {
    final Path dir = configDir();

    try {
      Files.createDirectories(dir);
    } catch (final IOException e) {
      LOGGER.error("Failed to create config directory", e);
    }

    final Path userDefined = dir.resolve("whitelist.json");

    try (final @Nullable InputStream stream =
        SlotWhitelist.class.getResourceAsStream("/defaults.json")) {
      Objects.requireNonNull(stream, "defaults.json");
      Files.copy(stream, userDefined);
    } catch (final FileAlreadyExistsException ignored) {
    } catch (final IOException e) {
      LOGGER.error("Failed to copy default whitelist", e);
    }

    return userDefined;
  }

  private static boolean ensureRegistered(final Entry entry) {
    if (!Registries.ITEM.containsId(entry.item)) {
      LOGGER.debug("Skipping unregistered item '{}'", entry.item);
      return false;
    }
    return true;
  }

  // ---- Entry inner class ----

  public static final class Entry {
    @JsonAdapter(IdentifierDeserializer.class)
    private final Identifier item;

    private final boolean ignoreNbt;

    @JsonAdapter(DimensionsDeserializer.class)
    private final @Nullable Set<String> dimensions;

    /** Default / no-arg constructor for Gson. */
    public Entry() {
      this(Identifier.of("minecraft", "air"), true);
    }

    /** Creates an entry for a specific item with default settings. */
    public Entry(final Identifier item) {
      this(item, true);
    }

    /** Creates an entry for a specific item with explicit NBT matching behavior. */
    public Entry(final Identifier item, final boolean ignoreNbt) {
      this.item = item;
      this.ignoreNbt = ignoreNbt;
      this.dimensions = null;
    }

    /**
     * Checks whether the given ItemStack matches this entry, optionally
     * filtering by dimension.
     *
     * @param stack        the item stack to test
     * @param dimensionKey the registry key of the current dimension,
     *                     e.g. "minecraft:overworld"
     */
    public boolean equals(final ItemStack stack, final String dimensionKey) {
      if (this.dimensions != null && !this.dimensions.contains(dimensionKey)) {
        return false;
      }
      return this.item.equals(Registries.ITEM.getId(stack.getItem()));
    }

    public Identifier item() {
      return this.item;
    }

    public boolean ignoresNbt() {
      return this.ignoreNbt;
    }

    public @Nullable Set<String> dimensions() {
      return this.dimensions;
    }

    @Override
    public int hashCode() {
      int hashCode = 1;
      hashCode = (31 * hashCode) + this.item.hashCode();
      hashCode = (31 * hashCode) + Boolean.hashCode(this.ignoreNbt);
      hashCode = (31 * hashCode) + Objects.hashCode(this.dimensions);
      return hashCode;
    }

    @Override
    public boolean equals(final @Nullable Object o) {
      if (this == o) {
        return true;
      }
      if (o == null || this.getClass() != o.getClass()) {
        return false;
      }
      final Entry that = (Entry) o;
      return this.ignoreNbt == that.ignoreNbt
          && this.item.equals(that.item)
          && Objects.equals(this.dimensions, that.dimensions);
    }

    // ---- JSON deserializers ----

    /** Deserializes a JSON string to an {@link Identifier}. */
    public static final class IdentifierDeserializer implements JsonDeserializer<Identifier> {
      @Override
      public Identifier deserialize(
          final JsonElement json,
          final Type typeOfT,
          final JsonDeserializationContext context) {
        final String raw = context.deserialize(json, String.class);
        final Identifier id = Identifier.tryParse(raw);
        return id != null ? id : Identifier.of("minecraft", "air");
      }
    }

    /**
     * Deserializes a JSON array of dimension strings (e.g. ["minecraft:overworld"])
     * into an unmodifiable {@link Set} of strings.
     */
    public static final class DimensionsDeserializer implements JsonDeserializer<Set<String>> {
      @Override
      public Set<String> deserialize(
          final JsonElement json,
          final Type typeOfT,
          final JsonDeserializationContext context) {
        final String[] dims = context.deserialize(json, String[].class);
        final Set<String> result = new HashSet<>();
        for (final String dim : dims) {
          if (dim != null && !dim.isEmpty()) {
            result.add(dim);
          }
        }
        if (result.isEmpty()) {
          return Collections.emptySet();
        }
        return Collections.unmodifiableSet(result);
      }
    }
  }
}
