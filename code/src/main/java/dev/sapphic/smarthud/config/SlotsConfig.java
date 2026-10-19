package dev.sapphic.smarthud.config;

/**
 * Configuration for the item slots HUD module.
 * Plain POJO — serialized/deserialized by Gson.
 */
public final class SlotsConfig {
  /** Enable the item slots. */
  public boolean enabled = true;

  /** Always draw a slot regardless of items present. */
  public boolean includeEmpty = false;

  /** The look and feel of the item slots. */
  public SlotsStyle style = SlotsStyle.OFFHAND;

  /** Merge similar items into the same slot. */
  public boolean cumulative = true;

  /** Draw the item overlays, such as enchantment glints. */
  public boolean overlays = true;

  /** Draw the (cumulative) counts of items. */
  public boolean counts = false;

  /** The maximum number of item slots to draw (1–18). */
  public int size = 6;

  /** Distribute slots across both the left and right sides of the hotbar. */
  public boolean bothSides = true;

  /** Automatically reserve horizontal space when Inventory HUD+ is loaded. */
  public boolean inventoryHudCompat = true;

  /** Extra horizontal distance (px) reserved when Inventory HUD+ is active. */
  public int inventoryHudOffset = 52;

  /** Manual horizontal offset (px) applied outward from the hotbar on both sides. */
  public int offsetX = 0;

  public SlotsConfig() {}

  /** Copy constructor used for building a working copy in the config screen. */
  public SlotsConfig(SlotsConfig other) {
    this.enabled = other.enabled;
    this.includeEmpty = other.includeEmpty;
    this.style = other.style;
    this.cumulative = other.cumulative;
    this.overlays = other.overlays;
    this.counts = other.counts;
    this.size = other.size;
    this.bothSides = other.bothSides;
    this.inventoryHudCompat = other.inventoryHudCompat;
    this.inventoryHudOffset = other.inventoryHudOffset;
    this.offsetX = other.offsetX;
  }

  /** Returns a fresh instance with all default values. */
  public static SlotsConfig defaults() {
    return new SlotsConfig();
  }
}
