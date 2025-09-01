package com.github.ldavid432.loot;

import com.github.ldavid432.GauntletLootConfig;
import com.google.common.collect.ImmutableList;
import java.util.Collection;
import java.util.List;
import javax.annotation.Nonnull;
import lombok.AccessLevel;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import net.runelite.client.game.ItemStack;

/**
 * Items received and their source
 * <p>
 * Image and title are decided upon loot being received in order to support things like random images
 */
@Data
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class Loot
{
	@Nonnull
	private final LootSource source;
	@Nonnull
	private final List<ItemStack> items;

	@Nonnull
	private LootImage image;
	@Nonnull
	private String title;

	public void updateImage(GauntletLootConfig config)
	{
		setImage(source.getImage(config));
	}

	public void updateTitle(GauntletLootConfig config)
	{
		setTitle(source.getTitle(config));
	}

	public static Loot of(LootSource source, Collection<ItemStack> items, GauntletLootConfig config)
	{
		return new Loot(
			source,
			ImmutableList.copyOf(items),
			source.getImage(config),
			source.getTitle(config)
		);
	}
}
