package com.github.ldavid432.loot;

import com.github.ldavid432.GauntletLootConfig;
import static com.github.ldavid432.GauntletLootUtil.CORRUPTED_HUNLLEF;
import static com.github.ldavid432.GauntletLootUtil.GAUNTLET_ITEMS;
import static com.github.ldavid432.GauntletLootUtil.HUNLLEF;
import com.github.ldavid432.config.GauntletChestColor;
import com.github.ldavid432.loot.image.GauntletLootImage;
import com.github.ldavid432.loot.image.LootImage;
import com.github.ldavid432.loot.item.RareItem;
import java.util.List;
import java.util.function.Function;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Sources to get loot
 */
@AllArgsConstructor
public enum LootSource
{
	GAUNTLET(
		HUNLLEF,
		config -> config.getChestTitle2().getText(config, HUNLLEF, "The Gauntlet"),
		config -> new GauntletLootImage(config, GauntletChestColor.ORIGINAL),
		GAUNTLET_ITEMS
	),
	CORRUPTED_GAUNTLET(
		CORRUPTED_HUNLLEF,
		config -> config.getChestTitle2().getText(config, CORRUPTED_HUNLLEF, "The Corrupted Gauntlet"),
		config -> new GauntletLootImage(config, GauntletChestColor.CORRUPTED),
		GAUNTLET_ITEMS
	),
	;

	@Getter
	private final String sourceName;
	private final Function<GauntletLootConfig, String> getTitle;
	@Getter
	private final Function<GauntletLootConfig, LootImage> getImage;
	@Getter
	private final List<RareItem> rareItems;

	LootSource(String sourceName, LootImage image, RareItem... rareItems)
	{
		this(sourceName, c -> sourceName, c -> image, List.of(rareItems));
	}

	LootSource(String sourceName, Function<GauntletLootConfig, String> getTitle, Function<GauntletLootConfig, LootImage> getImage, RareItem... rareItems)
	{
		this(sourceName, getTitle, getImage, List.of(rareItems));
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
