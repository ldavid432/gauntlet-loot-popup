# Gauntlet Chest Popup ![Install Count](https://img.shields.io/endpoint?url=https://api.runelite.net/pluginhub/shields/installs/plugin/gauntlet-chest-popup) ![Install Count](https://img.shields.io/endpoint?url=https://api.runelite.net/pluginhub/shields/rank/plugin/gauntlet-chest-popup)

- Displays a barrows chest style UI when you loot the Gauntlet chest!
- Plays a sounds when you get a rare item

![image](/readme_pic.png)

## Customization

- Chest sprite color
  - Auto: Automatically switches to original or corrupted depending on which one you completed
  - Standard colors: original (light blue), corrupted (red), blue, green, yellow, white, purple
- Chest title
  - Gauntlet: Automatically switches to `Gauntlet` or `Corrupted Gauntlet` depending on which one you completed
  - Hunllef: Automatically switches to `Crystalline Hunllef` or `Corrupted Hunllef` depending on which one you completed
  - Custom: Uses custom title text
  - Show Kill Count: Shows your current kill count in the title (default: `false`)
- Which items the rare item sound is played for
  - Weapon seed, Armour seed, Enhanced Weapon seed, Youngllef (Pet) and/or Elite scroll boxes
- Chest Background
  - The background image can be customized by turning on `Custom Chest Background`
  - The image is located at:
    - Windows: `%userprofile%\.runelite\plugin-data\gauntlet-chest-popup\background.png`
    - Mac/Linux: `~/.runelite/plugin-data/gauntlet-chest-popup/background.png`
  - By default, the image is 250px by 200px. You _can_ adjust the size if you want but, any smaller than 200x200 may cause issues.
  - After changing the image file you can toggle the `Custom Chest Background` or re-show the popup to refresh the image
  - It must be a **PNG** file and named `background.png` - a default file will be automatically generated so prefer editing that one
- Resource packs integration
  - The resource packs plugin can change the look of the popup
  - Since it is based on the barrows chest UI, as long as your resource pack changes those sprites then it should work with the gauntlet chest popup too
  - This can be turned off if you don't want your resource pack to change the look
- Chest value chat message
  - Turn on to get send a chat message with the value of the chest.
  - By default, it uses the grand exchange value, but can be changed to High Alchemy value.

If you'd like to preview your customization you can run the command (type in chat) `::gauntlet-chest`.
This will bring up the popup with some fake loot.
For a preview of the corrupted gauntlet do `::gauntlet-chest -c`

Example of customization:

![image](/readme_pic_2.png)

### Other Notes

- Can be closed with the close icon, by pressing ESC or by clicking outside the popup (if that setting is enabled).
- The popup *should* be included in screenshots taken by the `Screenshots` plugin.
- The popup position is moveable
  - Hold `Alt` (or whatever your Drag Hotkey is set to) and left-click drag to move
  - Hold `Alt` and right-click the popup to reset the position to default
- The popup is resizable (unless you have a custom background enabled)
  - Hold `Alt` and drag the corners or edges to resize
