package com.github.ldavid432.config;

import com.github.ldavid432.GauntletLootConfig;
import javax.annotation.Nonnull;

public enum GauntletTitle
{
	GAUNTLET,
	CORRUPTED_GAUNTLET,
	CUSTOM;

	@Nonnull
	public String getText(GauntletLootConfig config)
	{
		switch (this)
		{
			case CORRUPTED_GAUNTLET:
				return "The Corrupted Gauntlet";
			case CUSTOM:
				return config.getChestCustomTitle();
			default:
				return "The Gauntlet";
		}
	}
}
