package com.github.ldavid432.loot;

import com.github.ldavid432.GauntletLootConfig;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.runelite.client.game.ItemStack;

/**
 * Items received and their source
 * <p>
 * Image and title are decided upon loot being received in order to support things like random images
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
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

	public void updateImage(GauntletLootConfig config)
	{
		image = source.getImage(config);
	}

	public void updateTitle(GauntletLootConfig config)
	{
		title = source.getTitle(config);
	}

	// For important loot return their actual examine text, otherwise just return the item name
	public String getExamineText(int itemId, String itemName)
	{
		return getItems().stream()
			.filter(item -> item.getId() == itemId).findFirst()
			.map(LootItem::getExamineText).orElse(itemName);
	}

	public static Loot of(LootSource source, Collection<ItemStack> items, GauntletLootConfig config, Runnable playSound)
	{
		AtomicBoolean playedSound = new AtomicBoolean(false);

		return new Loot(
			source,
			items.stream()
				.map(stack -> {
						Item item = source.getItems().stream().filter(it -> it.getItemId() == stack.getId()).findFirst().orElse(null);

						if (item != null && item.shouldPlaySound(config) && !playedSound.getAndSet(true))
						{
							playSound.run();
						}

						return item != null ? new LootItem(item, stack.getQuantity()) : new LootItem(stack.getId(), null, stack.getQuantity());
					}
				)
				.collect(Collectors.toList()),
			source.getImage(config),
			source.getTitle(config)
		);
	}
}
