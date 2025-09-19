package com.github.ldavid432.loot.item;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Item to display in the popup, could be rare or just a basic item
 */
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class LootItem
{
	@Getter
	private final int id;
	@Getter
	private final String examineText;
	@Getter
	private final int quantity;
	@Getter
	private final String itemName;

	public static LootItem fromRareItem(RareItem item, int quantity, String itemName)
	{
		return new LootItem(item.getItemId(), item.getExamineText(), quantity, itemName);
	}

	public static LootItem fromBasicItem(int itemId, int quantity, String itemName){
		return new LootItem(itemId, itemName, quantity, itemName);
	}
}
