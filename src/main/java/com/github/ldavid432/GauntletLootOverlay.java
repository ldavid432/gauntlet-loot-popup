package com.github.ldavid432;

import static com.github.ldavid432.GauntletLootUtil.*;
import com.github.ldavid432.config.GauntletChestColor;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import javax.annotation.Nullable;
import javax.inject.Inject;
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
	private final GauntletLootConfig config;

	private BufferedImage closeButtonImage;
	private BufferedImage closeButtonHoveredImage;
	private final BufferedImage backgroundImage;
	private final BufferedImage[] chestImageCache = new BufferedImage[GauntletChestColor.values().length];

	private Rectangle closeButtonBounds;
	private final Map<Integer, Rectangle> itemBounds = new HashMap<>();

	@Inject
	public GauntletLootOverlay(GauntletLootPlugin plugin, Client client, ItemManager itemManager, SpriteManager spriteManager, GauntletLootConfig config)
	{
		super(plugin);
		this.plugin = plugin;
		this.client = client;
		this.itemManager = itemManager;
		this.spriteManager = spriteManager;
		this.config = config;

		setPosition(OverlayPosition.DYNAMIC);
		setLayer(OverlayLayer.ABOVE_WIDGETS);
		setPriority(200.0f);
		setMovable(true);
		// Start at the correct x and y so the overlay doesn't jump the first time it's opened
		setBounds(getOverlayBounds(0, 0));

		backgroundImage = ImageUtil.loadImageResource(getClass(), "background.png");
	}

	@Nullable
	private BufferedImage getCloseButtonImage()
	{
		if (isInCloseButtonBounds(getMousePosition(client)))
		{
			if (closeButtonHoveredImage == null)
			{
				closeButtonHoveredImage = spriteManager.getSprite(1732, 0);
			}
			return closeButtonHoveredImage;
		}
		else
		{
			if (closeButtonImage == null)
			{
				closeButtonImage = spriteManager.getSprite(1731, 0);
			}
			return closeButtonImage;
		}
	}

	@Nullable
	private BufferedImage getChestImage(GauntletChestColor color, String lootSource)
	{
		BufferedImage image = chestImageCache[color.ordinal()];
		if (image == null)
		{
			image = ImageUtil.loadImageResource(getClass(), color.getPath(lootSource));
			chestImageCache[color.getCacheInt(lootSource)] = image;
		}
		return image;
	}

	// Originally based on https://github.com/lalochazia/missed-clues
	@Override
	public Dimension render(Graphics2D graphics)
	{
		if (!plugin.isDisplayed())
		{
			resetBounds();
			return null;
		}

		if (backgroundImage != null)
		{
			setBounds(getOverlayBounds(BACKGROUND_WIDTH, BACKGROUND_HEIGHT));
			graphics.drawImage(backgroundImage, 0, 0, null);

			BufferedImage chestImage = getChestImage(config.getChestSpriteColor(), plugin.getLoot().getSource());
			if (chestImage != null)
			{
				graphics.drawImage(chestImage, CHEST_OFFSET, BACKGROUND_HEIGHT - CHEST_HEIGHT - CHEST_OFFSET, null);
			}

			renderTitle(graphics, plugin.getLoot().getSource());

			final BufferedImage closeButtonImage = getCloseButtonImage();
			if (closeButtonImage != null)
			{
				renderCloseButton(graphics, closeButtonImage);
			}

			renderItems(graphics, plugin.getLoot().getItems());
		}

		return getBounds().getSize();
	}

	private void renderTitle(Graphics2D graphics, String lootSource)
	{
		String title = config.getChestTitle2().getText(config, lootSource);
		graphics.setFont(FontManager.getRunescapeBoldFont());

		// Measure
		Rectangle titleBounds = graphics.getFontMetrics().getStringBounds(title, graphics).getBounds();
		int titleX = (backgroundImage.getWidth() / 2) - ((int) titleBounds.getWidth() / 2);
		int titleY = 25;

		// Draw shadow
		graphics.setColor(Color.BLACK);
		graphics.drawString(title, titleX + 1, titleY + 1);

		// Draw actual text
		graphics.setColor(JagexColors.DARK_ORANGE_INTERFACE_TEXT);
		graphics.drawString(title, titleX, titleY);
	}

	private void renderCloseButton(Graphics2D graphics, BufferedImage closeButtonImage)
	{
		int closeX = backgroundImage.getWidth() - closeButtonImage.getWidth() - 8;
		int closeY = 7;

		closeButtonBounds = rectangleFromImage(
			closeX,
			closeY,
			closeButtonImage
		);

		graphics.drawImage(closeButtonImage, closeX, closeY, null);
	}

	private void renderItems(Graphics2D graphics, List<ItemStack> items)
	{
		int x = 110;
		int y = 40;

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

				itemBounds.put(itemId, rectangleFromImage(x, y, itemImage));

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

	private void resetBounds()
	{
		setBounds(getOverlayBounds(0, 0));
		closeButtonBounds = null;
		itemBounds.clear();
	}

	public boolean isInBounds(Point point)
	{
		return getBounds() != null && getBounds().contains(point);
	}

	public boolean isInCloseButtonBounds(Point point)
	{
		return closeButtonBounds != null && getBounds() != null && getOffsetBounds(closeButtonBounds).contains(point);
	}

	public Integer getItemClicked(Point point)
	{
		AtomicReference<Integer> id = new AtomicReference<>();

		if (getBounds() == null)
		{
			return null;
		}

		itemBounds.forEach(
			(key, bounds) -> {
				if (getOffsetBounds(bounds).contains(point))
				{
					id.set(key);
				}
			}
		);

		return id.get();
	}

	// Translate bounds from inside the overlay to their global position in the window/canvas
	private Rectangle getOffsetBounds(Rectangle boundsInOverlay)
	{
		return new Rectangle(
			boundsInOverlay.x + getBounds().x,
			boundsInOverlay.y + getBounds().y,
			boundsInOverlay.width,
			boundsInOverlay.height
		);
	}

	private Rectangle getOverlayBounds(int width, int height)
	{
		int x;
		int y;

		// Default positon is centered-ish
		if (getPreferredLocation() == null)
		{
			// Technically `(client.getCanvasWidth() - BACKGROUND_WIDTH) / 2` is more correctly centered but
			//  since the inventory is usually on the right we can do this to keep it more to the left
			x = (client.getCanvasWidth() / 2) - BACKGROUND_WIDTH;
			y = (client.getCanvasHeight() / 2) - BACKGROUND_HEIGHT;
		}
		else
		{
			x = getPreferredLocation().x;
			y = getPreferredLocation().y;
		}

		return new Rectangle(x, y, width, height);
	}
}
