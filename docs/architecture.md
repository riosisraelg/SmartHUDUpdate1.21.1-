# Architecture Design (ISO/IEC/IEEE 42010:2022)

## 1. Introduction & Stakeholders
- **System Name:** Smart HUD
- **Context:** A Fabric mod for Minecraft 1.21.1 that renders additional HUD elements to provide utility without replacing core functionality.
- **Stakeholders:** Players (end-users), Modpack Creators (configuration), Developers (contributors).
- **Concerns:**
  - Performance: Must render seamlessly without hurting game FPS.
  - Compatibility: Must coexist with other HUD and inventory mods (e.g. Trinkets).
  - Configurability: Users must be able to visually customize their experience.

## 2. System Context (C4 Level 1)
```mermaid
C4Context
    title System Context for Smart HUD
    Person(player, "Player", "A Minecraft player.")
    System(smartHud, "Smart HUD", "Client-side mod that renders extra UI.")
    System_Ext(minecraftClient, "Minecraft Client", "The main game client.")
    System_Ext(trinkets, "Trinkets API", "External API for accessory slots.")
    
    Rel(player, smartHud, "Views extra HUD / configures mod")
    Rel(smartHud, minecraftClient, "Hooks rendering events and reads inventory")
    Rel(smartHud, trinkets, "Reads accessory inventory state")
```

## 3. Container Architecture (C4 Level 2)
```mermaid
C4Container
    title Container Diagram for Smart HUD
    Container(config, "Configuration Module", "Cloth Config / JSON", "Handles settings and whitelist persistence")
    Container(cache, "Inventory Cache", "Java", "Caches player inventory to optimize rendering")
    Container(renderer, "HUD Renderers", "Fabric Rendering API", "Draws the Ticker and Slots on screen")
    Container_Ext(minecraft, "Minecraft Engine", "Fabric API", "Provides game ticks and rendering pipelines")
    
    Rel(renderer, config, "Reads styles and rules")
    Rel(renderer, cache, "Reads cached items to display")
    Rel(cache, minecraft, "Monitors inventory changes")
```

## 4. Component Architecture (C4 Level 3)
- **Config Components:** `SmartHudConfig`, `SlotsConfig`, `TickerConfig`, `SlotWhitelist`
- **Cache Components:** `InventoryCache`, `TrinketCache`
- **Render Components:** `SlotRenderer`, `TickerRenderer`, `CubicBezierInterpolator`
- **Core Engine:** `SmartHudClient` (Entrypoint)

## 5. Architectural Views
- **Structural View:** The mod strictly isolates configuration models from rendering logic. Data flows from configuration JSON -> Memory Config POJO -> Renderer.
- **Behavioral View (Ticker):** An `ItemPickupMixin` injects into Minecraft's native pickup event. It passes the picked item to the `TickerQueue`. The `TickerRenderer` polls this queue and animates the items out of the screen using a bezier interpolator.

## 6. Quality Attributes (ISO 25010)
- **Performance Efficiency:** The `InventoryCache` prevents iterating over the entire player inventory on every single render frame.
- **Usability:** ModMenu integration ensures configuration is accessible directly inside the game client without restarting.
- **Maintainability:** Separation of styles and behavioral logic into dedicated configuration classes allows easy future expansions.
