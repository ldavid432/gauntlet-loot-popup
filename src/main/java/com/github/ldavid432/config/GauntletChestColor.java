package com.github.ldavid432.config;

import java.util.Random;

public enum GauntletChestColor
{
	ORIGINAL,
	CORRUPTED,
	BLUE,
	GREEN,
	PURPLE,
	WHITE,
	YELLOW,
	AUTO {
		@Override
		protected String getPathPart(GauntletChestColor autoColor)
		{
			// autoColor should never be AUTO, but just in case to prevent recursion
			return autoColor != AUTO ? autoColor.getPathPart(autoColor) : ORIGINAL.getPathPart(autoColor);
		}
	},
	RANDOM {
		@Override
		protected String getPathPart(GauntletChestColor autoColor)
		{
			// should never return AUTO so the autoColor doesn't actually matter here
			return values()[random.nextInt(values().length - 2)].getPathPart(ORIGINAL);
		}
	};

	private static final Random random = new Random();

	protected String getPathPart(GauntletChestColor autoColor)
	{
		return name().toLowerCase();
	}

	public String getPath(GauntletChestColor autoColor)
	{
		return "chest_" + getPathPart(autoColor) + ".png";
	}

}
