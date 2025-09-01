package com.github.ldavid432.loot;

import com.github.ldavid432.GauntletLootConfig;
import static com.github.ldavid432.GauntletLootUtil.BACKGROUND_HEIGHT;
import static com.github.ldavid432.GauntletLootUtil.CHEST_HEIGHT;
import static com.github.ldavid432.GauntletLootUtil.CHEST_OFFSET;
import static com.github.ldavid432.GauntletLootUtil.CORRUPTED_HUNLLEF;
import static com.github.ldavid432.GauntletLootUtil.HUNLLEF;
import com.github.ldavid432.config.GauntletChestColor;
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
		config -> new LootImage(config.getChestSpriteColor().getPath(GauntletChestColor.ORIGINAL), CHEST_OFFSET, BACKGROUND_HEIGHT - CHEST_HEIGHT - CHEST_OFFSET)
	),
	CORRUPTED_GAUNTLET(
		CORRUPTED_HUNLLEF,
		config -> config.getChestTitle2().getText(config, CORRUPTED_HUNLLEF, "The Corrupted Gauntlet"),
		config -> new LootImage(config.getChestSpriteColor().getPath(GauntletChestColor.CORRUPTED), CHEST_OFFSET, BACKGROUND_HEIGHT - CHEST_HEIGHT - CHEST_OFFSET)
	);

	@Getter
	private final String sourceName;
	private final Function<GauntletLootConfig, String> getTitle;
	@Getter
	private final Function<GauntletLootConfig, LootImage> getImage;

	LootSource(String name, LootImage image)
	{
		this(name, c -> name, c -> image);
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
