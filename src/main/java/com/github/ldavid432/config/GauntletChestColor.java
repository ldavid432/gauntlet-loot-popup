package com.github.ldavid432.config;

import lombok.Getter;

@Getter
public enum GauntletChestColor
{
	ORIGINAL,
	CORRUPTED,
	BLUE,
	GREEN,
	PURPLE,
	WHITE,
	YELLOW;

	private final String path = "chest_" + name().toLowerCase() + ".png";
}
