# Software Requirements Specification (ISO/IEC/IEEE 29148:2018)

## 1. Introduction
This document outlines the software requirements for the Smart HUD Fabric Mod (v1.21.1).

## 2. Overall Description
- **Product Perspective:** Smart HUD is a standalone client-side modification for Minecraft that hooks into the Fabric rendering pipeline.
- **User Classes:** Players.
- **Operating Environment:** Minecraft 1.21.1, Fabric Loader 0.16.0+, Java 21.

## 3. Functional Requirements

### REQ-FUN-001: Virtual HUD Slots
The system shall display virtual item slots on the player's screen representing specific items in the player's inventory without requiring those items to be on the main hotbar.

**BDD Scenario:**
```gherkin
Feature: Virtual Slots
  Scenario: Displaying a whitelisted item
    Given the player has a Clock in their inventory
    And the Clock is not in the active hotbar
    When the HUD is rendered
    Then a virtual slot showing the Clock is drawn on the screen
```

### REQ-FUN-002: Trinket Integration
The system shall query the Fabric `Trinkets` API (if installed) and display items equipped in Trinket slots as virtual slots.

### REQ-FUN-003: Item Pickup Ticker
The system shall intercept item pickup events and queue them to be rendered as temporary, animated notifications on the HUD.

### REQ-FUN-004: In-Game Configuration
The system shall allow users to modify mod parameters via Cloth Config / ModMenu integration, persisting changes to `config/smarthud.json`.

### REQ-FUN-005: Future Accessory API Compatibility (Planned)
The system shall be designed with abstractions allowing the future integration of other accessory APIs beyond Trinkets.

## 4. Non-Functional Requirements
- **REQ-NFR-001 (Performance):** The system shall not introduce a drop of more than 5% in average FPS during heavy item pickup scenarios.
- **REQ-NFR-002 (Compatibility):** The mod shall not crash the game if the optional `Trinkets` dependency is missing.

## 5. Requirements Traceability Matrix (RTM)
| Req ID | Component | Status |
|---|---|---|
| REQ-FUN-001 | `SlotRenderer`, `InventoryCache` | Implemented |
| REQ-FUN-002 | `TrinketCache` | Implemented |
| REQ-FUN-003 | `TickerRenderer`, `ItemPickupMixin` | Implemented |
| REQ-FUN-004 | `SmartHudConfig` | Implemented |
| REQ-FUN-005 | `InventoryCache` (Abstraction) | Planned |
