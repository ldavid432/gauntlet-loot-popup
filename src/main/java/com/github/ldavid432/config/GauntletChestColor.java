package com.github.ldavid432.config;

import static com.github.ldavid432.GauntletLootUtil.CORRUPTED_HUNLLEF;
import java.util.Objects;
import java.util.Random;
import java.util.function.Function;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public enum GauntletChestColor
{
	ORIGINAL(ChestColor.ORIGINAL),
	CORRUPTED(ChestColor.CORRUPTED),
	BLUE(ChestColor.BLUE),
	GREEN(ChestColor.GREEN),
	PURPLE(ChestColor.PURPLE),
	WHITE(ChestColor.WHITE),
	YELLOW(ChestColor.YELLOW),
	AUTO(source -> {
		if (Objects.equals(source, CORRUPTED_HUNLLEF))
		{
			return ChestColor.CORRUPTED;
		}
		else
		{
			return ChestColor.ORIGINAL;
		}
	}),
	RANDOM(s -> ChestColor.random());

	private final Function<String, ChestColor> getTrueColor;

	GauntletChestColor(ChestColor color) {
		this.getTrueColor = s -> color;
	}

	public enum ChestColor {
		// Light Blue
		ORIGINAL,
		// Red
		CORRUPTED,
		BLUE,
		GREEN,
		PURPLE,
		WHITE,
		YELLOW;

		@Getter
		private final String path = "chest_" + name().toLowerCase() + ".png";

		private static final Random random = new Random();

		public static ChestColor random() {
			return values()[random.nextInt(values().length)];
		}
	}

	public ChestColor getTrueColor(String source)
	{
		return getTrueColor.apply(source);
	}

}
