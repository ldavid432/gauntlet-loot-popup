package com.github.ldavid432.loot;

import com.github.ldavid432.GauntletLootConfig;
import com.github.ldavid432.loot.image.LootImage;
import com.github.ldavid432.loot.item.LootItem;
import com.github.ldavid432.loot.item.RareItem;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.runelite.client.game.ItemManager;
import net.runelite.client.game.ItemStack;

/**
 * Items received and their source
 * <p>
 * Image and title are decided upon loot being received in order to support things like random images
 */
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Loot
{
	@Nonnull
	private final LootSource source;
	@Nonnull
	@Getter
	private final List<LootItem> items;
	@Nonnull
	@Getter
	private LootImage image;
	@Nonnull
	@Getter
	private String title;

	@Getter
	private int killCount;

	public void updateImage(GauntletLootConfig config)
	{
		image = source.getImage(config);
	}

	public void updateTitle(GauntletLootConfig config)
	{
		title = source.getTitle(config);
	}

	public static Loot of(LootSource source, Collection<ItemStack> items, int killCount, GauntletLootConfig config, ItemManager itemManager, Runnable playSound)
	{
		AtomicBoolean playedSound = new AtomicBoolean(false);

		return new Loot(
			source,
			items.stream()
				.map(stack -> {
					String itemName = itemManager.getItemComposition(stack.getId()).getName();
					RareItem rareItem = source.getRareItems()
						.stream()
						.filter(item -> item.getItemId() == stack.getId())
						.findFirst()
						.orElse(null);

					if (rareItem != null && rareItem.shouldPlaySound(config) && !playedSound.getAndSet(true))
					{
						playSound.run();
					}

					return rareItem != null ? LootItem.fromRareItem(rareItem, stack.getQuantity(), itemName) : LootItem.fromBasicItem(stack.getId(), stack.getQuantity(), itemName);
				})
				.collect(Collectors.toList()),
			source.getImage(config),
			source.getTitle(config),
			killCount
		);
	}
}
