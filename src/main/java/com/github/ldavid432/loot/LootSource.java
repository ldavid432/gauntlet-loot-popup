package com.github.ldavid432.loot;

import com.github.ldavid432.GauntletLootConfig;
import static com.github.ldavid432.GauntletLootUtil.BACKGROUND_HEIGHT;
import static com.github.ldavid432.GauntletLootUtil.CHEST_HEIGHT;
import static com.github.ldavid432.GauntletLootUtil.CHEST_OFFSET;
import static com.github.ldavid432.GauntletLootUtil.CORRUPTED_HUNLLEF;
import static com.github.ldavid432.GauntletLootUtil.GAUNTLET_ITEMS;
import static com.github.ldavid432.GauntletLootUtil.HUNLLEF;
import com.github.ldavid432.config.GauntletChestColor;
import java.util.List;
import java.util.function.Function;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.runelite.api.gameval.SpriteID;

/**
 * Sources to get loot
 */
@AllArgsConstructor
public enum LootSource
{
	GAUNTLET(
		HUNLLEF,
		config -> config.getChestTitle2().getText(config, HUNLLEF, "The Gauntlet"),
		config -> new LootImage(config.getChestSpriteColor().getPath(GauntletChestColor.ORIGINAL), CHEST_OFFSET, BACKGROUND_HEIGHT - CHEST_HEIGHT - CHEST_OFFSET),
		GAUNTLET_ITEMS
	),
	CORRUPTED_GAUNTLET(
		CORRUPTED_HUNLLEF,
		config -> config.getChestTitle2().getText(config, CORRUPTED_HUNLLEF, "The Corrupted Gauntlet"),
		config -> new LootImage(config.getChestSpriteColor().getPath(GauntletChestColor.CORRUPTED), CHEST_OFFSET, BACKGROUND_HEIGHT - CHEST_HEIGHT - CHEST_OFFSET),
		GAUNTLET_ITEMS
	),
	;

	@Getter
	private final String sourceName;
	private final Function<GauntletLootConfig, String> getTitle;
	@Getter
	private final Function<GauntletLootConfig, LootImage> getImage;
	@Getter
	private final List<Item> items;

	LootSource(String sourceName, LootImage image, Item... items)
	{
		this(sourceName, c -> sourceName, c -> image, List.of(items));
	}

	LootSource(String sourceName, Function<GauntletLootConfig, String> getTitle, Function<GauntletLootConfig, LootImage> getImage, Item... items)
	{
		this(sourceName, getTitle, getImage, List.of(items));
	}

	public String getTitle(GauntletLootConfig config)
	{
		return getTitle.apply(config);
	}

	public LootImage getImage(GauntletLootConfig config)
	{
		return getImage.apply(config);
	}
}
