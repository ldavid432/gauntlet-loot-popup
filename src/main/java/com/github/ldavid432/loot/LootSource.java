package com.github.ldavid432.loot;

import com.github.ldavid432.GauntletLootConfig;
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
		config -> config.getChestSpriteColor().getPath(GauntletChestColor.ORIGINAL)
	),
	CORRUPTED_GAUNTLET(
		CORRUPTED_HUNLLEF,
		config -> config.getChestTitle2().getText(config, CORRUPTED_HUNLLEF, "The Corrupted Gauntlet"),
		config -> config.getChestSpriteColor().getPath(GauntletChestColor.CORRUPTED)
	);

	@Getter
	private final String sourceName;
	private final Function<GauntletLootConfig, String> getTitle;
	private final Function<GauntletLootConfig, String> getImagePath;

	LootSource(String name, String imagePath)
	{
		this(name, c -> name, c -> imagePath);
	}

	public String getTitle(GauntletLootConfig config)
	{
		return getTitle.apply(config);
	}

	public String getImagePath(GauntletLootConfig config)
	{
		return getImagePath.apply(config);
	}
}
