package com.github.ldavid432;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.function.Predicate;
import net.runelite.api.Client;
import net.runelite.api.MenuEntry;

public class Util
{
	static Rectangle rectangleFromImage(int x, int y, BufferedImage image)
	{
		return new Rectangle(x, y, image.getWidth(), image.getHeight());
	}

	static boolean anyMenuEntry(Client client, Predicate<MenuEntry> predicate)
	{
		return Arrays.stream(client.getMenu().getMenuEntries()).anyMatch(predicate);
	}

	static final int BACKGROUND_WIDTH = 230;
	static final int BACKGROUND_HEIGHT = 200;

	static final int CHEST_WIDTH = 112;
	static final int CHEST_HEIGHT = 126;
	static final int CHEST_OFFSET = 7;
}
