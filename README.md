# Gauntlet Chest Popup ![Install Count](https://img.shields.io/endpoint?url=https://api.runelite.net/pluginhub/shields/installs/plugin/gauntlet-chest-popup) ![Install Count](https://img.shields.io/endpoint?url=https://api.runelite.net/pluginhub/shields/rank/plugin/gauntlet-chest-popup)

- Displays a barrows chest style UI when you loot the Gauntlet chest!
- Plays a sounds when you get a rare item

## Customization

- Chest sprite color
  - Auto: Automatically switches to original or corrupted depending on which one you completed
  - Standard colors: original (light blue), corrupted (red), blue, green, yellow, white, purple
- Chest title
  - Gauntlet: Automatically switches to The Gauntlet or The Corrupted Gauntlet depending on which one you completed
  - Hunllef: Automatically switches to Crystalline or Corrupted Hunllef depending on which one you completed
  - Custom: Uses custom title
- Which items the rare item sound is played for
  - Weapon seed, Armour seed, Enhanced Weapon seed, Youngllef (Pet) and/or Elite scroll boxes
- Chest Background
  - The background image can be customized by turning on `Custom Chest Background`
  - The image is located at:
    - Windows: `%userprofile%\.runelite\gauntlet-chest-popup\background.png`
    - Mac/Linux: `~/.runelite/gauntlet-chest-popup/background.png`
  - By default, the image is 230px by 200px. You _can_ adjust the size, however not all UI elements may work properly with different size images
  - After changing the image file you can toggle the `Custom Chest Background` or re-show the popup to refresh the image
  - It must be a **PNG** file and named `background.png` - a default file will be automatically generated so prefer editing that one

If you'd like to preview your customization you can run the command (type in chat) `::gauntlet-chest`.
This will bring up the popup with some fake loot.
For a preview of the corrupted gauntlet do `::gauntlet-chest -c`

### Other Notes

- Can be closed with the close icon, by pressing ESC or by clicking outside the popup (if that setting is enabled).
- The popup *Should* be included in screenshots taken by the `Screenshots` plugin.
- The popup position is moveable
  - Hold Alt (or whatever your Drag Hotkey is set to) and left-click drag to move
  - Hold Alt and right-click the popup to reset the position to default

### Screenshots

![image](/readme_pic.png)

Example of customization:

![image](/readme_pic_2.png)
