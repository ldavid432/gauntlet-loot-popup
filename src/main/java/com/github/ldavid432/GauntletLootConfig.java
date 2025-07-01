package com.github.ldavid432;

import com.github.ldavid432.config.GauntletChestColor;
import com.github.ldavid432.config.GauntletTitle;
import com.github.ldavid432.config.GauntletTitle2;
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;

@ConfigGroup(GauntletLootConfig.GROUP)
public interface GauntletLootConfig extends Config
{
	String GROUP = "gauntletchestpopup";
	String CHEST_COLOR = "chestSpriteColor";
	int CURRENT_VERSION = 1;

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
		description = "Select the color for the chest sprite on the UI (Does not recolor the physical chest object)<br>" +
			"'Auto' selects either original or corrupted based on what you completed<br>" +
			"'Random' selects a random solid color each time you open the chest",
		keyName = CHEST_COLOR,
		section = chestSection,
		position = 0
	)
	default GauntletChestColor getChestSpriteColor()
	{
		return GauntletChestColor.AUTO;
	}

	@ConfigItem(
		name = "Chest Title (legacy)",
		description = "Customize the title text (legacy)",
		keyName = "chestTitleText",
		section = chestSection,
		position = 1,
		hidden = true
	)
	default GauntletTitle getChestTitleLegacy()
	{
		return GauntletTitle.UNSET;
	}

	@ConfigItem(
		name = "",
		description = "",
		keyName = "chestTitleText"
	)
	void setChestTitleLegacy(GauntletTitle title);

	@ConfigItem(
		name = "Chest Title",
		description = "Customize the title text<br>" +
			"Gauntlet - Either 'The Gauntlet' or 'The Corrupted Gauntlet'<br>" +
			"Hunllef - Either 'Crystalline Hunllef' or 'Corrupted Hunllef'<br>" +
			"Custom - Title set in 'Custom title' below ",
		keyName = "chestTitleText2",
		section = chestSection,
		position = 1
	)
	default GauntletTitle2 getChestTitle2()
	{
		return GauntletTitle2.GAUNTLET;
	}

	@ConfigItem(
		name = "",
		description = "",
		keyName = "chestTitleText2"
	)
	void setChestTitle2(GauntletTitle2 title);

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

	@ConfigItem(
		name = "Youngllef",
		description = "Play rare item sound for the Youngllef pet",
		keyName = "playPetSound",
		section = soundsSection,
		position = 4
	)
	default boolean shouldPlayPetSound()
	{
		return true;
	}

	@ConfigItem(
		name = "",
		description = "",
		keyName = "lastSeenVersion",
		hidden = true
	)
	default int getLastSeenVersion()
	{
		return -1;
	}

	@ConfigItem(
		name = "",
		description = "",
		keyName = "lastSeenVersion",
		hidden = true
	)
	void setLastSeenVersion(int version);

}
