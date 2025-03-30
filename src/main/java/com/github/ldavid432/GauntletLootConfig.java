package com.github.ldavid432;

import com.github.ldavid432.config.GauntletChestColor;
import com.github.ldavid432.config.GauntletTitle;
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;

@ConfigGroup(GauntletLootConfig.GROUP)
public interface GauntletLootConfig extends Config
{
	String GROUP = "gauntletchestpopup";

	@ConfigItem(
		name = "Click outside to dismiss",
		description = "Allows clicking outside the popup to dismiss it",
		keyName = "clickOutsideToDismiss",
		position = 0
	)
	default boolean isClickOutsideToDismissEnabled()
	{
		return true;
	}

	@ConfigItem(
		name = "Examine items",
		description = "Adds a menu option to examine loot items",
		keyName = "examineItems",
		position = 1
	)
	default boolean isExamineEnabled()
	{
		return true;
	}

	@ConfigSection(
		name = "Chest theme",
		description = "Configure the look of the chest loot UI",
		position = 2
	)
	String chestSection = "chestSection";

	@ConfigItem(
		name = "Chest Sprite Color",
		description = "Select the color for the chest sprite on the UI (Does not recolor the physical chest object)",
		keyName = "chestSpriteColor",
		section = chestSection,
		position = 0
	)
	default GauntletChestColor getChestSpriteColor()
	{
		return GauntletChestColor.ORIGINAL;
	}

	@ConfigItem(
		name = "Chest Title",
		description = "Customize the title text",
		keyName = "chestTitleText",
		section = chestSection,
		position = 1
	)
	default GauntletTitle getChestTitle()
	{
		return GauntletTitle.GAUNTLET;
	}

	@ConfigItem(
		name = "Custom title",
		description = "Custom title text, used if 'Chest Title' is set to 'Custom'",
		keyName = "chestCustomTitleText",
		section = chestSection,
		position = 2
	)
	default String getChestCustomTitle()
	{
		return "Red Prison";
	}

	@ConfigSection(
		name = "Rare item sounds",
		description = "Choose which items to play the rare item sound for",
		position = 3
	)
	String soundsSection = "soundsSection";

	@ConfigItem(
		name = "Weapon seed",
		description = "Play rare item sound for Weapon seeds",
		keyName = "playWeaponSeedSound",
		section = soundsSection,
		position = 0
	)
	default boolean shouldPlayWeaponSeedSound()
	{
		return false;
	}

	@ConfigItem(
		name = "Armour seed",
		description = "Play rare item sound for Armour seeds",
		keyName = "playArmourSeedSound",
		section = soundsSection,
		position = 1
	)
	default boolean shouldPlayArmourSeedSound()
	{
		return true;
	}

	@ConfigItem(
		name = "Enhanced Weapon seed",
		description = "Play rare item sound for Enhanced Weapon seeds",
		keyName = "playEnhancedSeedSound",
		section = soundsSection,
		position = 2
	)
	default boolean shouldPlayEnhancedSeedSound()
	{
		return true;
	}

}
