package com.github.ldavid432;

import static com.github.ldavid432.GauntletLootPlugin.THE_GAUNTLET;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.List;
import javax.annotation.Nullable;
import javax.inject.Inject;
import lombok.Getter;
import net.runelite.api.Client;
import net.runelite.client.game.ItemManager;
import net.runelite.client.game.ItemStack;
import net.runelite.client.game.SpriteManager;
import net.runelite.client.ui.FontManager;
import net.runelite.client.ui.JagexColors;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.util.ImageUtil;

public class GauntletLootOverlay extends Overlay
{
	private final GauntletLootPlugin plugin;
	private final Client client;
	private final ItemManager itemManager;
	private final SpriteManager spriteManager;

	private BufferedImage closeButtonImage;
	private final BufferedImage backgroundImage;

	@Getter
	private Rectangle closeButtonBounds;

	@Inject
	public GauntletLootOverlay(@Nullable GauntletLootPlugin plugin, Client client, ItemManager itemManager, SpriteManager spriteManager)
	{
		super(plugin);
		this.plugin = plugin;
		this.client = client;
		this.itemManager = itemManager;
		this.spriteManager = spriteManager;

		setPosition(OverlayPosition.DYNAMIC);
		setLayer(OverlayLayer.ABOVE_WIDGETS);
		setPriority(200.0f);

		backgroundImage = ImageUtil.loadImageResource(getClass(), "background.png");
	}

	@Nullable
	public BufferedImage getCloseButtonImage()
	{
		if (closeButtonImage == null)
		{
			closeButtonImage = spriteManager.getSprite(1731, 0);
		}
		return closeButtonImage;
	}

	// Based on https://github.com/lalochazia/missed-clues
	@Override
	public Dimension render(Graphics2D graphics)
	{
		if (plugin.getLootedItems().isEmpty())
		{
			return null;
		}

		final int canvasWidth = client.getCanvasWidth();
		final int canvasHeight = client.getCanvasHeight();

		final int startX;
		final int startY;

		if (canvasWidth <= 1000 && canvasHeight <= 650)
		{
			startX = (canvasWidth - 230) / 2;
			startY = (canvasHeight - 200) / 2;
		}
		else
		{
			startX = canvasWidth / 2 - 24;
			startY = canvasHeight / 3 - 24;
		}

		if (backgroundImage != null)
		{
			int incX = startX - 110;
			int incY = startY - 40;
			graphics.drawImage(backgroundImage, incX, incY, null);

			renderTitle(graphics, incX, incY);

			final BufferedImage closeButtonImage = getCloseButtonImage();
			if (closeButtonImage != null)
			{
				renderCloseButton(graphics, closeButtonImage, incX, incY);
			}
		}

		renderItems(graphics, plugin.getLootedItems(), startX, startY);

		return null;
	}

	private void renderTitle(Graphics2D graphics, int incX, int incY)
	{
		graphics.setFont(FontManager.getRunescapeBoldFont());
		Rectangle titleBounds = graphics.getFontMetrics().getStringBounds(THE_GAUNTLET, graphics).getBounds();
		int titleX = incX + (backgroundImage.getWidth() / 2) - ((int) titleBounds.getWidth() / 2);
		int titleY = incY + 25;
		graphics.setColor(Color.BLACK);
		graphics.drawString(THE_GAUNTLET, titleX + 1, titleY + 1);
		graphics.setColor(JagexColors.DARK_ORANGE_INTERFACE_TEXT);
		graphics.drawString(THE_GAUNTLET, incX + (backgroundImage.getWidth() / 2) - ((int) titleBounds.getWidth() / 2), incY + 25);
	}

	private void renderCloseButton(Graphics2D graphics, BufferedImage closeButtonImage, int incX, int incY)
	{
		int closeX = incX + backgroundImage.getWidth() - closeButtonImage.getWidth() - 8;
		int closeY = incY + 7;

		closeButtonBounds = new Rectangle(
			closeX,
			closeY,
			closeButtonImage.getWidth(),
			closeButtonImage.getHeight()
		);

		graphics.drawImage(closeButtonImage, closeX, closeY, null);
	}

	private void renderItems(Graphics2D graphics, List<ItemStack> items, int x, int y)
	{
		for (int i = 0; i < items.size(); i++)
		{
			ItemStack stack = items.get(i);
			int itemId = stack.getId();
			int quantity = stack.getQuantity();

			BufferedImage itemImage;
			if (quantity > 1)
			{
				itemImage = itemManager.getImage(itemId, quantity, true);
			}
			else
			{
				itemImage = itemManager.getImage(itemId);
			}

			if (itemImage != null)
			{
				graphics.drawImage(itemImage, x, y, null);

				if ((i + 1) % 3 == 0)
				{
					x = x - (itemImage.getWidth() + 5) * 2;
					y += itemImage.getHeight() + 5;
				}
				else
				{
					x += itemImage.getWidth() + 5;
				}
			}
		}
	}
}
