package com.github.ldavid432;

import com.github.ldavid432.config.GauntletChestColor;
import com.google.common.collect.ImmutableList;
import java.util.Collection;
import java.util.List;
import javax.annotation.Nonnull;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import net.runelite.client.game.ItemStack;

@Data
@RequiredArgsConstructor
public class GauntletLoot
{
	// Crystalline or Corrupted Hunllef
	@Nonnull
	private final String source;
	@Nonnull
	private final List<ItemStack> items;

	@Nonnull
	private GauntletChestColor.ChestColor color;
	@Nonnull
	private String title;

	public void updateColor(GauntletLootConfig config)
	{
		setColor(config.getChestSpriteColor().getTrueColor(source));
	}

	public void updateTitle(GauntletLootConfig config)
	{
		setTitle(config.getChestTitle2().getText(config, source));
	}

	public static GauntletLoot of(String source, Collection<ItemStack> items, GauntletLootConfig config)
	{
		return new GauntletLoot(
			source,
			ImmutableList.copyOf(items),
			config.getChestSpriteColor().getTrueColor(source),
			config.getChestTitle2().getText(config, source)
		);
	}
}
