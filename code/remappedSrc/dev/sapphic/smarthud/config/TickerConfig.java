package dev.sapphic.smarthud.config;

/**
 * Configuration for the item ticker HUD module.
 * Plain POJO — serialized/deserialized by Gson.
 */
public final class TickerConfig {
  /** Enable the item ticker. */
  public boolean enabled = true;

  /** The duration in milliseconds that items should be shown for. */
  public int durationMillis = 3000;

  /** The look and feel of the item ticker. */
  public TickerStyle style = TickerStyle.BOTH;

  /** The maximum number of items to show at once. */
  public int size = 10;

  /**
   * The behavior of the item ticker.
   * 0 = sort by most recent pickup, 1 = sort by initial pickup.
   */
  public int behavior = 0;

  public TickerConfig() {}

  /** Copy constructor used for building a working copy in the config screen. */
  public TickerConfig(TickerConfig other) {
    this.enabled = other.enabled;
    this.durationMillis = other.durationMillis;
    this.style = other.style;
    this.size = other.size;
    this.behavior = other.behavior;
  }

  /** Returns a fresh instance with all default values. */
  public static TickerConfig defaults() {
    return new TickerConfig();
  }

  /** Converts the configured duration from milliseconds to game ticks. */
  public long durationTicks() {
    return (durationMillis / 1000L) * 20L;
  }
}
