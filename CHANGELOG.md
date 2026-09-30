# Changelog

## 0.3.0 — first release

### Added
- Recording of open containers: chests, barrels, shulker boxes, furnaces, anvils, crafting tables,
  hoppers and other vanilla-style containers.
- Recording of the player's own inventory (E).
- Playback in first person as the recording player, using the real vanilla screens (resource packs
  supported).
- Recorded mouse: hovered slot highlight and the item on the cursor.
- Item animation: items fly between slots when moved.
- Optional item tooltips under the recorded mouse.
- Settings in the Flashback editor ("Containers" in the top menu bar) and in Mod Menu.
- Translations into 18 languages.

### Notes
- Works with seeking, pausing and video export.
- Replays recorded with the addon still open without it.
- Not recorded yet: creative inventory, horse/llama inventories, furnace progress, brewing stand
  bubbles.


## 0.3.1

### Changed
- The mouse is now recorded as often as Flashback's "Local player updates per second" setting says,
  the same way Flashback records the camera. At 20 it is the same as before, at 60 or 120 fast mouse
  movements are replayed much more precisely.
- The "Disable increased first person updates" option of Flashback also applies to the mouse.
- In Mod Menu, Flashback Containers is now listed inside Flashback.

### Notes
- Replays recorded with 0.3.0 still work. Replays recorded with 0.3.1 open fine in 0.3.0, but
  without the mouse.


## 0.3.2 — indicators fix

### Fixed
- Progress bars and indicators are now recorded and shown:
  - furnace, blast furnace and smoker: flame and arrow;
  - brewing stand: bubbles, arrow and blaze powder fuel;
  - enchanting table: level costs, enchantment hints and the enchanting text;
  - anvil: repair cost and "Too Expensive!";
  - beacon: pyramid level and selected effects;
  - loom: selected pattern;
  - crafter: disabled slots and redstone power.
- Containers from other mods that use the same vanilla mechanism work too.
- Screens now use the xp level and game mode of the player you are watching instead of the replay
  viewer: available enchantments are highlighted correctly, and the anvil cost is red only when that
  player couldn't afford it.

### Notes
- Replays recorded with 0.3.2 open fine in 0.3.0 and 0.3.1, without the indicators.


## 0.3.3 — smithing table hints

### Fixed
- Smithing table: the hints in empty slots (template, ingot, armor outlines) are shown and cycle
  like in the game.

### Known issues
- Stonecutter: the grid of possible results is empty. Flashback doesn't record the recipe list the
  server sends, so the replay doesn't know which recipes the server had.
