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

  /** The maximum number of item slots to draw (1–9). */
  public int size = 3;

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
  }

  /** Returns a fresh instance with all default values. */
  public static SlotsConfig defaults() {
    return new SlotsConfig();
  }
}
