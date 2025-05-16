package com.github.ldavid432.config;

import static com.github.ldavid432.GauntletLootUtil.CORRUPTED_HUNLLEF;
import java.util.Objects;

public enum GauntletChestColor
{
	ORIGINAL,
	CORRUPTED,
	BLUE,
	GREEN,
	PURPLE,
	WHITE,
	YELLOW,
	AUTO;

	private final String path = "chest_" + name().toLowerCase() + ".png";

	public String getPath(String source)
	{
		return getTrueColor(source).path;
	}

	public int getCacheInt(String source)
	{
		return getTrueColor(source).ordinal();
	}

	private GauntletChestColor getTrueColor(String source)
	{
		if (this == AUTO)
		{
			if (Objects.equals(source, CORRUPTED_HUNLLEF))
			{
				return CORRUPTED;
			}
			else
			{
				return ORIGINAL;
			}
		}
		else
		{
			return this;
		}
	}

}
