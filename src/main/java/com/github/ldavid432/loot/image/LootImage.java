package com.github.ldavid432.loot.image;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import lombok.Data;

@Data
public class LootImage
{
	private final String path;
	private final int xOffset;
	private final int yOffset;

	// render function so we can adjust the offset/positioning of the image as needed
	public void renderImage(Graphics2D graphics, BufferedImage image, int backgroundHeight)
	{
		graphics.drawImage(image, xOffset, backgroundHeight - image.getHeight() - yOffset, null);
	}
}
