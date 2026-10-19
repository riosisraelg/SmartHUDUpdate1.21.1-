package dev.sapphic.smarthud.config;

public final class SlotsConfig {
  public boolean enabled = true;
  public boolean includeEmpty = false;
  public SlotsStyle style = SlotsStyle.OFFHAND;
  public boolean cumulative = true;
  public boolean overlays = true;
  public boolean counts = false;
  public int size = 6;
  public boolean bothSides = true;
  public boolean inventoryHudCompat = true;
  public int inventoryHudOffset = 52;
  public int offsetX = 0;

  public SlotsConfig() {}

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

  public static SlotsConfig defaults() {
    return new SlotsConfig();
  }
}
