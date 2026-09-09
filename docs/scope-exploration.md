# Scope Exploration: Smart HUD

## 1. Big Picture
**Project Name:** Smart HUD
**Purpose:** A Minecraft mod that adds an inventory item slot hotbar and an item pickup ticker to the player's HUD. It provides utility by freeing up hotbar space for tools like compasses and clocks, without them taking up one of the 9 quick-access slots.
**Status:** Existing codebase being retroactively documented. 

## 2. Operational Environment
- **Platform/Loader:** Fabric (version 0.16.0).
- **Minecraft Version:** 1.21.1.
- **Environment:** Client-Side only (HUD rendering). 

## 3. Features (Core Functionality)
- **Extra Virtual Slots:**
  - Displays specific items from the inventory (like a compass or a clock) on a custom HUD element.
  - Helps keep the main 9-slot hotbar uncluttered.
  - Automatically configured to show Clocks and Compasses by default.
  - Supports custom item whitelists via a `whitelist.json` file.
  - Can accumulate and group items.
- **Trinkets Integration:**
  - Native support for the Fabric `Trinkets` API, allowing items equipped in Trinket slots to be read and displayed on the Smart HUD.
- **Item Pickup Ticker:**
  - A notification system that displays items recently picked up, with configurable duration, size, and sorting behavior.

## 4. Configuration & Customization
- **In-Game Menus:**
  - Fully integrated with `ModMenu` and `Cloth Config` for real-time in-game adjustments.
- **Customizable Attributes:**
  - **Slots:** Enable/disable, include empty slots, cumulative grouping, overlays, count toggles, and size (1-9).
  - **Ticker:** Enable/disable, duration (default 3 seconds), visual style, max size (up to 50), and sorting behavior.

## 5. Future Features (Out of Scope for Now)
- **Broader Accessory Compatibility:** Support for other accessory/equipment APIs in the Fabric ecosystem beyond just `Trinkets`.
