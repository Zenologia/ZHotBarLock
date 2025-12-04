# ZHotbarLock

A lightweight Paper plugin for managing persistent, locked hotbar/offhand items (such as server selectors).  
Designed for cross-version compatibility (1.20–1.21+) and fully configurable behavior.

---

## ✨ Features

- Automatically gives defined selector items on:
  - Player join  
  - Respawn  
  - World change  
- Only allow in the worlds you want it to work in (whitelist)
  - Worlds should be listed how they appear in the `/world` command
  - If a world is not listed, most plugin functions will not work, including the reissue command.  However, items issued by the command will still be subhect to previous locks.
- Either have the plugin apply in all game modes, or only choose the modes you want it to apply in
- Full item locking system:
  - Prevent moving items in inventory
  - Prevent container interactions
  - Prevent dropping
  - Prevent offhand swapping
- Optional:
  - Force item into offhand
  - Use preferred hotbar slot
  - Use next free hotbar slot
  - Fallback to offhand
- Items never drop on death (unless bypassed)
- Items identified via NBT (not by name or lore)
- Per-item reissue command
- Full permission support
- Safe to reload during runtime

---

## 📥 Installation

1. Download the latest `ZHotbarLock.jar`.
2. Place it in your server’s `/plugins/` folder.
3. Start the server to generate `config.yml`.
4. Edit `config.yml` to define your locked items.
5. Run `/zhotbarlock reload` to apply changes.

Compatible with:
- **Paper 1.20+** (including 1.21)
- Java 17+

---

## 🔧 Configuration Overview

All items are defined under the `items:` section.

```yaml
enabled-worlds:
  - world
  - world_nether
  - world_the_end

all-gamemodes: true 
allowed-gamemodes:
  - SURVIVAL
  - ADVENTURE

events: 
  check-on-join: true
  check-on-respawn: true
  check-on-world-change: true

items:
  selector-main:
    enabled: true
    material: "COMPASS"
    name: "&aServer Selector"
    lore:
      - "&7Right-click to open the server selector."
    glow: false
    nbt-id: "selector_main"

  
    preferred-slot: 0
    use-next-free-hotbar-slot: true 
    allow-offhand-fallback: true
    force-offhand: false 
    log-on-placement-failure: true 


    lock-inventory-movement: true
    block-container-move: true
    block-drop: true
    block-offhand-swap: true

  selector-main2:
    enabled: true
    material: "STICK"
    name: "&aServer Selector"
    lore:
      - "&7Right-click to open the server selector."
    glow: false
    nbt-id: "selector_main2"

    preferred-slot: 1
    use-next-free-hotbar-slot: true
    allow-offhand-fallback: true
    force-offhand: false 
    log-on-placement-failure: true 

    lock-inventory-movement: true
    block-container-move: true
    block-drop: true
    block-offhand-swap: true
```

### Notes

- If a world is not listed, the plugin will not work there at all - not even the reissue command.
- Custom worlds work.  If it appears in `/world` then its registered and you should use the name as it appears there.
- `nbt-id` must be unique per item.
- `preferred-slot` supports values **0–8** (hotbar only).
- `force-offhand` overrides all slot logic unless the offhand is occupied.
- Display name and lore can be freely edited.
- Comments (`#`) are safe anywhere in the config.

---

## 🧠 How It Works

ZHotbarLock tags each configured item using a persistent NBT key.  
This ensures:

- The plugin can always identify its items.
- Items cannot be faked by players.
- Lock rules apply reliably across all events.

When players join, respawn, or change worlds, the plugin ensures each required item is present and placed according to its configured rules.

Death behavior:
- Selector items **never drop** (unless player has `zhotbarlock.bypass`).

---

## 🔐 Permissions

| Permission | Description | Default |
|-----------|-------------|---------|
| `zhotbarlock.admin` | Access to all admin commands | op |
| `zhotbarlock.bypass` | Bypass **all** automatic enforcement & lock rules | op |

### Notes

- `zhotbarlock.bypass` does **not** prevent being targeted by `/zhotbarlock reissue`.
- OPs naturally bypass enforcement unless you change the default.

---

## 🕹️ Commands

### `/zhotbarlock reload`
Reloads the configuration.

### `/zhotbarlock clear <player>`
Removes all locked selector items from the target player.

### `/zhotbarlock reissue <player>`
Reissues **all configured items** to the player.

### `/zhotbarlock reissue <player> <item-id>`
Reissues **a specific item** to the player.

Tab-completion updates automatically after `/zhotbarlock reload`.

---

## ⚙️ Recommended Build Setup

Compile against Paper **1.20.x** for maximum compatibility:

```xml
<paper-api.version>1.20-R0.1-SNAPSHOT</paper-api.version>
```

And use:

```yaml
api-version: "1.20"
```

in `plugin.yml`.

This ensures the plugin runs safely on both **1.20 and 1.21+** servers.

---

## 🗺️ Roadmap: 

Make admin facing messages configurable.
Fix logic of "use next free hotbar slot" to not start back at 0 every time.
Add per-item event toggles.
Consider unlocking or deleting items if moving to a world not listed in the config
Consider blocking certain items from being used (those with built in right click functionality such as a book & Quil, or valuable items/totems etc)
Right now, if a player who can bypass permissions drops an item, and then they are picked up (by that player or another) they can stack (like a compass) or the player will have multiple if non-stackable (totems).  Consider checking for/suppressing duplicates on `EntityPickupItemEvent`

---

## 🧑‍💻 Author

- **Zenologia**
- [GitHub Repository](https://github.com/Zenologia/ZHotBarLock)
- [License](https://github.com/Zenologia/ZHotBarLock/blob/main/LICENSE)
