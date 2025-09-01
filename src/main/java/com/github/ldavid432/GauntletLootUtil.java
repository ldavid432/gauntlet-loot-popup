package com.github.ldavid432;

import com.google.common.collect.ImmutableList;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;
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

	public static final int BACKGROUND_WIDTH = 230;
	public static final int BACKGROUND_HEIGHT = 200;

	public static final int CHEST_WIDTH = 112;
	public static final int CHEST_HEIGHT = 126;
	public static final int CHEST_OFFSET = 7;

	public static final String HUNLLEF = "Crystalline Hunllef";
	public static final String CORRUPTED_HUNLLEF = "Corrupted Hunllef";

}
