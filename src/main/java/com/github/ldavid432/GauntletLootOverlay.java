package com.github.ldavid432;

import static com.github.ldavid432.GauntletLootUtil.BACKGROUND_HEIGHT;
import static com.github.ldavid432.GauntletLootUtil.BACKGROUND_WIDTH;
import static com.github.ldavid432.GauntletLootUtil.IMAGE_CACHE_LIMIT;
import static com.github.ldavid432.GauntletLootUtil.KC_FORMAT;
import static com.github.ldavid432.GauntletLootUtil.CUSTOM_BACKGROUND_IMAGE;
import static com.github.ldavid432.GauntletLootUtil.ITEM_START_X;
import static com.github.ldavid432.GauntletLootUtil.ITEM_START_Y;
import static com.github.ldavid432.GauntletLootUtil.getMousePosition;
import static com.github.ldavid432.GauntletLootUtil.rectangleFromImage;
import com.github.ldavid432.loot.Loot;
import com.github.ldavid432.loot.image.LootImage;
import com.github.ldavid432.loot.item.LootItem;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import javax.annotation.Nullable;
import javax.imageio.ImageIO;
import javax.inject.Inject;
import lombok.Value;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.imageio.ImageIO;
import javax.inject.Inject;
import lombok.SneakyThrows;
import net.runelite.api.Client;
import net.runelite.api.gameval.SpriteID;
import net.runelite.client.game.ItemManager;
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

	private final LoadingCache<String, BufferedImage> imageCache = CacheBuilder.newBuilder()
		.maximumSize(IMAGE_CACHE_LIMIT)
		.build(
			new CacheLoader<>()
			{
				@Override
				public BufferedImage load(@Nonnull String imagePath) throws Exception
				{
					return ImageUtil.loadImageResource(getClass(), imagePath);
				}
			}
		);

	private Rectangle closeButtonBounds;
	private final List<LootItemBounds> itemBounds = new ArrayList<>();

	@Value
	private static class LootItemBounds
	{
		LootItem item;
		Rectangle bounds;
	}

	@Inject
	public GauntletLootOverlay(GauntletLootPlugin plugin, Client client, ItemManager itemManager, SpriteManager spriteManager)
	{
		super(plugin);
		this.plugin = plugin;
		this.client = client;
		this.itemManager = itemManager;
		this.spriteManager = spriteManager;

		setPosition(OverlayPosition.DYNAMIC);
		setLayer(OverlayLayer.ABOVE_WIDGETS);
		setPriority(200.0f);
		setMovable(true);
		// Start at the correct x and y so the overlay doesn't jump the first time it's opened
		setBounds(getOverlayBounds(0, 0));
	}

	@SneakyThrows
	@Nullable
	private BufferedImage getCloseButtonImage()
	{
		if (isInCloseButtonBounds(getMousePosition(client)))
		{
			return imageCache.get("closeHovered", () -> spriteManager.getSprite(SpriteID.SteelborderCloseButton._1, 0));
		}
		else
		{
			return imageCache.get("close", () -> spriteManager.getSprite(SpriteID.SteelborderCloseButton._0, 0));
		}
	}

	@SneakyThrows
	@Nullable
	private BufferedImage getBackgroundImage()
	{
		return imageCache.get("background", () -> {
			if (plugin.getLoot().isUseCustomBackground())
			{
				try
				{
					BufferedImage backgroundImage = ImageIO.read(CUSTOM_BACKGROUND_IMAGE);
					if (backgroundImage != null)
					{
						return backgroundImage;
					}
				}
				catch (IOException ignored)
				{
				}
			}
			return ImageUtil.loadImageResource(GauntletLootPlugin.class, "background.png");
		});
	}

	// Originally based on https://github.com/lalochazia/missed-clues
	@SneakyThrows
	@Override
	public Dimension render(Graphics2D graphics)
	{
		Loot loot = plugin.getLoot();

		if (loot == null)
		{
			resetBounds();
			return null;
		}

		BufferedImage backgroundImage = getBackgroundImage();
		if (backgroundImage != null)
		{
			setBounds(getOverlayBounds(BACKGROUND_WIDTH, BACKGROUND_HEIGHT));
			graphics.drawImage(backgroundImage, 0, 0, null);

			LootImage lootImage = loot.getImage();
			BufferedImage image = imageCache.get(lootImage.getPath());
			if (image != null)
			{
				lootImage.renderImage(graphics, image, backgroundImage.getHeight());
			}

			final BufferedImage closeButtonImage = getCloseButtonImage();

			renderTitle(graphics, loot.getTitle(), plugin.getLastKillCount(), closeButtonImage, backgroundImage);

			if (closeButtonImage != null)
			{
				renderCloseButton(graphics, closeButtonImage, backgroundImage);
			}

			renderItems(graphics, loot.getItems(), backgroundImage);
		}

		return getBounds().getSize();
	}

	private void renderTitle(Graphics2D graphics, String title, int killCount, BufferedImage closeButtonImage, BufferedImage backgroundImage)
	{
		boolean showKillCount = plugin.isShowKillCountEnabled() && killCount > 0;
		if (showKillCount)
		{
			title = title + " - " + KC_FORMAT.format(killCount) + " KC";
		}

		graphics.setFont(FontManager.getRunescapeBoldFont());

		// Measure
		Rectangle titleBounds = graphics.getFontMetrics().getStringBounds(title, graphics).getBounds();
		// Center on background when not showing KC, center on available space when showing KC
		int titleAreaWidth = showKillCount ? (backgroundImage.getWidth() - closeButtonImage.getWidth()) / 2 : backgroundImage.getWidth() / 2;
		int titleX = titleAreaWidth - ((int) titleBounds.getWidth() / 2);
		int titleY = 25;

		// Draw shadow
		graphics.setColor(Color.BLACK);
		graphics.drawString(title, titleX + 1, titleY + 1);

		// Draw actual text
		graphics.setColor(JagexColors.DARK_ORANGE_INTERFACE_TEXT);
		graphics.drawString(title, titleX, titleY);
	}

	private void renderCloseButton(Graphics2D graphics, BufferedImage closeButtonImage, BufferedImage backgroundImage)
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

	private void renderItems(Graphics2D graphics, List<LootItem> items, BufferedImage backgroundImage)
	{
		int x = ITEM_START_X;
		int y = ITEM_START_Y;

		itemBounds.clear();

		int furthestItemX = backgroundImage.getWidth() - 6;

		for (LootItem item : items)
		{
			int itemId = item.getId();
			int quantity = item.getQuantity();

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

				itemBounds.add(new LootItemBounds(item, rectangleFromImage(x, y, itemImage)));

				int nextItemX = x + (itemImage.getWidth() * 2);
				if (nextItemX > furthestItemX)
				{
					// Drop to next line
					x = ITEM_START_X;
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

	public LootItem getItemClicked(Point point)
	{
		if (getBounds() == null)
		{
			return null;
		}

		return itemBounds.stream()
			.filter(item -> getOffsetBounds(item.getBounds()).contains(point))
			.findFirst()
			.map(LootItemBounds::getItem)
			.orElse(null);
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

	public void shutDown()
	{
		resetBounds();
		imageCache.invalidateAll();
	}

	public void clearBackgroundImage()
	{
		imageCache.invalidate("background");
	}
}
