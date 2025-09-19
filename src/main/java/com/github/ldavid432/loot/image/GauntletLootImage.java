package com.github.ldavid432.loot.image;

import com.github.ldavid432.GauntletLootConfig;
import static com.github.ldavid432.GauntletLootUtil.BACKGROUND_HEIGHT;
import static com.github.ldavid432.GauntletLootUtil.CHEST_HEIGHT;
import static com.github.ldavid432.GauntletLootUtil.CHEST_OFFSET;
import com.github.ldavid432.config.GauntletChestColor;

public class GauntletLootImage extends LootImage
{
	public GauntletLootImage(GauntletLootConfig config, GauntletChestColor autoColor)
	{
		super(config.getChestSpriteColor().getPath(autoColor), CHEST_OFFSET, BACKGROUND_HEIGHT - CHEST_HEIGHT - CHEST_OFFSET);
	}
}
