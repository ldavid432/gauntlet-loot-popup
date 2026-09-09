package com.github.ldavid432.config;

import com.github.ldavid432.GauntletLootConfig;
import javax.annotation.Nonnull;

public enum GauntletTitle
{
	GAUNTLET,
	HUNLLEF,
	CUSTOM;

	@Nonnull
	public String getText(GauntletLootConfig config, String hunllefText, String gauntletText)
	{
		switch (this)
		{
			case GAUNTLET:
				return gauntletText;
			case CUSTOM:
				return config.getChestCustomTitle();
			case HUNLLEF:
				// Fall-through
			default:
				return hunllefText;
		}
	}
}
