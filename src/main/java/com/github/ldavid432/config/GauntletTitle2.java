package com.github.ldavid432.config;

import com.github.ldavid432.GauntletLootConfig;
import com.github.ldavid432.GauntletLootUtil;
import javax.annotation.Nonnull;

public enum GauntletTitle2
{
	GAUNTLET,
	HUNLLEF,
	CUSTOM;

	@Nonnull
	public String getText(GauntletLootConfig config, String source)
	{
		switch (this)
		{
			case GAUNTLET:
				if (source.equals(GauntletLootUtil.CORRUPTED_HUNLLEF))
				{
					return "The Corrupted Gauntlet";
				}
				else
				{
					return "The Gauntlet";
				}
			case CUSTOM:
				return config.getChestCustomTitle();
			case HUNLLEF:
				// Fall-through
			default:
				return source;
		}
	}
}
