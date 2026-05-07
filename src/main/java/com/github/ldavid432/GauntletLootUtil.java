package com.github.ldavid432;

import com.github.ldavid432.loot.item.RareItem;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import net.runelite.api.Client;
import net.runelite.api.MenuEntry;

public class GauntletLootUtil
{
	static Rectangle rectangleFromImage(int x, int y, BufferedImage image)
	{
		return new Rectangle(x, y, image.getWidth(), image.getHeight());
	}

	static boolean anyMenuEntry(Client client, Predicate<MenuEntry> predicate)
	{
		return Arrays.stream(client.getMenu().getMenuEntries()).anyMatch(predicate);
	}

	static Point getMousePosition(Client client)
	{
		net.runelite.api.Point rlMousePos = client.getMouseCanvasPosition();
		return new Point(rlMousePos.getX(), rlMousePos.getY());
	}

	public static boolean isVowel(char c) {
		return "AEIOUaeiou".indexOf(c) != -1;
	}

	public static final int BACKGROUND_WIDTH = 250;
	public static final int BACKGROUND_HEIGHT = 200;

	public static final int CHEST_WIDTH = 112;
	public static final int CHEST_HEIGHT = 126;
	public static final int CHEST_OFFSET = 7;

	public static final String HUNLLEF = "Crystalline Hunllef";
	public static final String CORRUPTED_HUNLLEF = "Corrupted Hunllef";
	public static final List<RareItem> GAUNTLET_ITEMS = List.of(
		RareItem.CRYSTAL_WEAPON_SEED,
		RareItem.CRYSTAL_ARMOUR_SEED,
		RareItem.ENHANCED_WEAPON_SEED,
		RareItem.YOUNGLLEF,
		RareItem.ELITE_SCROLL_BOX,
		RareItem.GAUNTLET_CAPE,
		RareItem.CRYSTAL_SHARD
	);

	public static final Pattern KC_PATTERN = Pattern.compile("Your (?:<col=[0-9a-f]{6}>)?(?:(?:Corrupted )?Gauntlet|Corrupted Hunllef \\(Echo\\))(?:</col>)? (?:kill|completion) count is: <col=[0-9a-f]{6}>([0-9,]+)</col>\\.?");
	public static final NumberFormat KC_FORMAT = NumberFormat.getNumberInstance(Locale.UK);
	@SafeVarargs
	public static <T> List<T> staticListOf(List<T> list, T... items) {
		List<T> newList = new ArrayList<>(list);
		newList.addAll(List.of(items));
		return newList;
	}


	public static final int IMAGE_CACHE_LIMIT = 10;
}
