# Flashback Containers

An addon for [Flashback](https://modrinth.com/mod/flashback) that records the containers you open
(chests, furnaces, crafting tables, your own inventory...) and shows them in the replay.

Flashback does not record open containers, so in a replay you only see the player standing in
front of a chest. With this addon, when the camera is in first person as the recording player,
the container window appears on screen just like it did in the game, with the items moving as
they were moved.

## Features

- **Any container.** Chests, barrels, shulker boxes, furnaces, anvils, crafting tables, hoppers
  and every other container with a vanilla-style screen, including your own inventory (E).
- **Looks exactly like the game.** The addon uses the real vanilla screens, so textures, titles and
  resource packs are the same as when you played.
- **Progress bars and indicators.** Furnace flame and arrow, brewing stand bubbles, enchantment
  costs, anvil cost, beacon level and so on.
- **Recorded mouse.** The slot under the mouse is highlighted and the item held on the cursor
  follows it, smoothly between ticks.
- **Item animation.** Items fly between slots when they are moved. With the mouse shown, only
  moves that don't go through the cursor fly (shift-click, number keys), the rest is shown by the
  cursor itself.
- **Item tooltips** (optional): name and lore of the item under the recorded mouse.
- **Works with seeking, pausing and exporting.** The window follows the timeline and is drawn
  together with the hotbar, so the "Hotbar" option in the editor and "No GUI" in the export
  also hide it.
- **Safe for your replays.** Replays recorded with the addon still open normally without it,
  Flashback just skips the extra data.
- Translated into 18 languages.

## Settings

- In the Flashback editor: **Containers** in the top menu bar opens a small window, which can be
  docked as a tab next to the other editor windows.
- In [Mod Menu](https://modrinth.com/mod/modmenu), if it is installed.

| Setting            | Default | What it does                                                           |
|--------------------|---------|------------------------------------------------------------------------|
| Show mouse         | on      | Slot highlight and the item held on the cursor                         |
| Animate items      | on      | Items fly between slots                                                |
| Show item tooltips | off     | Tooltip of the item under the mouse (needs "Show mouse")               |

Settings only change how replays are drawn, everything is always recorded.
They are stored in `config/flashback_containers.json`.

## Requirements

- Minecraft 26.2
- Fabric Loader 0.19.3 or newer
- Fabric API
- Flashback 0.43.6 (0.43.0 or newer)
- Mod Menu is optional

The addon is client-side only and works on any server. It has to be installed **while recording**,
replays recorded without it contain no container data.

## Limitations

- Only the recording player's containers are known. They are shown when the camera is in first
  person as that player.
- Not recorded yet: the creative inventory, horse/llama inventories, villager trades, lectern.

## Known issues

- Stonecutter: the grid of possible results is empty. Flashback doesn't record the recipe list the
  server sends, so the replay doesn't know which recipes the server had.

## Building

1. Install a JDK 21 or newer to run Gradle (JDK 25 for the mod itself is downloaded by Gradle,
   Flashback and Mod Menu are downloaded from their Maven repositories).
2. Run `./gradlew build`. The mod is in `build/libs/`.

## License

[GPL-3.0](LICENSE). Not affiliated with Flashback or its author.
