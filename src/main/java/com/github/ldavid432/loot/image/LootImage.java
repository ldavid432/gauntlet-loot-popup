package com.github.ldavid432.loot.image;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import lombok.Data;

@Data
public class LootImage
{
	private final String path;
	private final int x;
	private final int y;

	// render function so we can adjust the offset/positioning of the image as needed
	public void renderImage(Graphics2D graphics, BufferedImage image)
	{
		graphics.drawImage(image, x, y, null);
	}
}
