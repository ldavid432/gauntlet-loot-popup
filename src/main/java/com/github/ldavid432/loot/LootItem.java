package com.github.ldavid432.loot;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Item to display in the popup
 */
@AllArgsConstructor
public class LootItem
{
	@Getter
	private final int id;
	@Getter
	private final String examineText;
	@Getter
	private final int quantity;

	public LootItem(Item item, int quantity)
	{
		this(item.getItemId(), item.getExamineText(), quantity);
	}
}
