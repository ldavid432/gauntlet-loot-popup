package com.github.ldavid432;

import static com.github.ldavid432.GauntletLootUtil.CLOSE_BUTTON;
import static com.github.ldavid432.GauntletLootUtil.CLOSE_BUTTON_HOVERED;
import static com.github.ldavid432.GauntletLootUtil.CLOSE_OFFSET_X;
import static com.github.ldavid432.GauntletLootUtil.CLOSE_OFFSET_Y;
import static com.github.ldavid432.GauntletLootUtil.DEFAULT_CHEST_HEIGHT;
import static com.github.ldavid432.GauntletLootUtil.DEFAULT_CHEST_WIDTH;
import static com.github.ldavid432.GauntletLootUtil.DIVIDER;
import static com.github.ldavid432.GauntletLootUtil.DIVIDER_OFFSET_Y;
import static com.github.ldavid432.GauntletLootUtil.EDGE_BOT;
import static com.github.ldavid432.GauntletLootUtil.EDGE_IMAGE_THICKNESS;
import static com.github.ldavid432.GauntletLootUtil.EDGE_LEFT;
import static com.github.ldavid432.GauntletLootUtil.EDGE_RIGHT;
import static com.github.ldavid432.GauntletLootUtil.EDGE_TOP;
import static com.github.ldavid432.GauntletLootUtil.IMAGE_CACHE_LIMIT;
import static com.github.ldavid432.GauntletLootUtil.ITEM_LAST_OFFSET_X;
import static com.github.ldavid432.GauntletLootUtil.ITEM_SPACING;
import static com.github.ldavid432.GauntletLootUtil.ITEM_START_X;
import static com.github.ldavid432.GauntletLootUtil.ITEM_START_Y;
import static com.github.ldavid432.GauntletLootUtil.KC_FORMAT;
import static com.github.ldavid432.GauntletLootUtil.MIN_SIZE;
import static com.github.ldavid432.GauntletLootUtil.TITLE_OFFSET_Y;
import static com.github.ldavid432.GauntletLootUtil.getMousePosition;
import static com.github.ldavid432.GauntletLootUtil.rectangleFromImage;
import static com.github.ldavid432.GauntletLootUtil.trimTransparentBorder;
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
import java.awt.Shape;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.imageio.ImageIO;
import javax.inject.Inject;
import lombok.SneakyThrows;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
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

@Slf4j
public class GauntletLootOverlay extends Overlay
{
	private final GauntletLootPlugin plugin;
	private final Client client;
	private final ItemManager itemManager;
	private final SpriteManager spriteManager;

	// Wraps a cache or file image key and returns it without its transparent border
	//  Handles resource packs not removing the transparent borders that sprites use while RuneLite does
	@Value
	static class WithoutTransparentBorder
	{
		Object wrappedKey;
	}

	private final LoadingCache<Object, BufferedImage> imageCache = CacheBuilder.newBuilder()
		.maximumSize(IMAGE_CACHE_LIMIT)
		.build(
			new CacheLoader<>()
			{
				@Override
				public BufferedImage load(@Nonnull Object key)
				{
					if (key instanceof String)
					{
						return ImageUtil.loadImageResource(getClass(), (String) key);
					}
					else if (key instanceof Integer)
					{
						if (plugin.isResourcePacksIntegrationEnabled() && client.getSpriteOverrides().containsKey(key))
						{
							return client.getSpriteOverrides().get(key).toBufferedImage();
						}
						else
						{
							return spriteManager.getSprite((Integer) key, 0);
						}
					}
					else if (key instanceof WithoutTransparentBorder)
					{
						return trimTransparentBorder(load(((WithoutTransparentBorder) key).getWrappedKey()));
					}
					else
					{
						throw new IllegalArgumentException("Invalid image key");
					}
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
		setMinimumSize(MIN_SIZE);
	}

	@SneakyThrows(ExecutionException.class)
	@Nullable
	private BufferedImage getCloseButtonImage()
	{
		if (isInCloseButtonBounds(getMousePosition(client)))
		{
			// Hovered
			return imageCache.get(CLOSE_BUTTON_HOVERED);
		}
		else
		{
			// Non-hovered
			return imageCache.get(CLOSE_BUTTON);
		}
	}

	@SneakyThrows(ExecutionException.class)
	@Nullable
	private BufferedImage getBackgroundImage()
	{
		return imageCache.get("background", () -> {
			try (InputStream inputStream = plugin.getBackgroundImageFilepath().openInputStream())
			{
				BufferedImage backgroundImage = ImageIO.read(inputStream);
				if (backgroundImage != null)
				{
					return backgroundImage;
				}
			}
			catch (Exception e)
			{
				log.debug("Could not load custom background", e);
			}
			return null;
		});
	}

	// Originally based on https://github.com/lalochazia/missed-clues
	@SneakyThrows(ExecutionException.class)
	@Override
	public Dimension render(Graphics2D graphics)
	{
		Loot loot = plugin.getLoot();

		if (loot == null)
		{
			return null;
		}

		BufferedImage backgroundImage = plugin.isCustomBackgroundEnabled() ? getBackgroundImage() : null;

		renderBackground(graphics, backgroundImage);

		LootImage lootImage = loot.getImage();
		BufferedImage image = imageCache.get(lootImage.getPath());
		if (image != null)
		{
			lootImage.renderImage(graphics, image, getBounds().height);
		}

		final BufferedImage closeButtonImage = getCloseButtonImage();

		renderTitle(graphics, loot.getTitle(), loot.getKillCount(), closeButtonImage);

		if (closeButtonImage != null)
		{
			renderCloseButton(graphics, closeButtonImage);
		}

		renderItems(graphics, loot.getItems());

		// TODO: Can maybe just return getBounds() w + h ?
		if (getPreferredSize() == null)
		{
			return getSize(backgroundImage);
		}
		return getPreferredSize();
	}

	private void renderBackground(Graphics2D graphics, @Nullable BufferedImage backgroundImage)
	{
		if (backgroundImage != null)
		{
			// Custom background image
			graphics.drawImage(backgroundImage, 0, 0, null);
		}
		else
		{
			try
			{
				renderSpriteBackground(graphics);
			}
			catch (ExecutionException e)
			{
				throw new RuntimeException(e);
			}
		}
	}

	// TODO: Maybe get rid of all these asserts?
	private void renderSpriteBackground(Graphics2D graphics) throws ExecutionException
	{
		Shape originalClip = graphics.getClip();

		// Background
		renderBacking(graphics);

		// Divider
		renderDivider(graphics);

		// Corners
		graphics.setClip(originalClip);

		BufferedImage topLeft = imageCache.get(SpriteID.Steelborder.TOP_LEFT);
		assert topLeft != null;
		graphics.drawImage(topLeft, 0, 0, null);

		BufferedImage topRight = imageCache.get(SpriteID.Steelborder.TOP_RIGHT);
		assert topRight != null;
		graphics.drawImage(topRight, getBounds().width - topRight.getWidth(), 0, null);

		BufferedImage botLeft = imageCache.get(SpriteID.Steelborder.BOTTOM_LEFT);
		assert botLeft != null;
		graphics.drawImage(botLeft, 0, getBounds().height - botLeft.getHeight(), null);

		BufferedImage botRight = imageCache.get(SpriteID.Steelborder.BOTTOM_RIGHT);
		assert botRight != null;
		graphics.drawImage(botRight, getBounds().width - botRight.getWidth(), getBounds().height - botRight.getHeight(), null);

		// Edges
		renderBackgroundEdges(graphics, topLeft, botLeft, topRight, botRight);

		// restore clip
		graphics.setClip(originalClip);
	}

	private void renderDivider(Graphics2D graphics) throws ExecutionException
	{
		BufferedImage divider = imageCache.get(DIVIDER);
		assert divider != null;

		graphics.setClip(
			new Rectangle(
				0,
				DIVIDER_OFFSET_Y,
				getBounds().width,
				EDGE_IMAGE_THICKNESS
			)
		);

		for (int x = 0;
		     x < getBounds().width;
		     x += divider.getWidth())
		{
			graphics.drawImage(divider, x, DIVIDER_OFFSET_Y, null);
		}
	}

	private void renderBacking(Graphics2D graphics) throws ExecutionException
	{
		graphics.setClip(new Rectangle(0, 0, getBounds().width, getBounds().height));

		BufferedImage bg = imageCache.get(SpriteID.TRADEBACKING);
		assert bg != null;

		int x1 = 0;
		int y1 = 0;

		while (x1 < getBounds().width || y1 < getBounds().height)
		{
			if (x1 > getBounds().width)
			{
				x1 = 0;
				y1 += bg.getHeight();
			}
			else if (y1 > getBounds().height)
			{
				break;
			}
			else
			{
				graphics.drawImage(bg, x1, y1, null);
				x1 += bg.getWidth();
			}
		}
	}

	private void renderBackgroundEdges(Graphics2D graphics, BufferedImage topLeft, BufferedImage botLeft,
	                                   BufferedImage topRight, BufferedImage botRight) throws ExecutionException
	{
		// left edge

		BufferedImage edgeLeft = imageCache.get(EDGE_LEFT);
		assert edgeLeft != null;

		graphics.setClip(
			new Rectangle(
				0,
				topLeft.getHeight(),
				EDGE_IMAGE_THICKNESS,
				getBounds().height - topLeft.getHeight() - botLeft.getHeight()
			)
		);

		for (int y = topLeft.getHeight();
		     y < getBounds().height - botLeft.getHeight();
		     y += edgeLeft.getHeight())
		{
			graphics.drawImage(edgeLeft, 0, y, null);
		}

		//  right edge

		BufferedImage edgeRight = imageCache.get(EDGE_RIGHT);
		assert edgeRight != null;

		graphics.setClip(
			new Rectangle(
				getBounds().width - 6,
				topRight.getHeight(),
				EDGE_IMAGE_THICKNESS,
				getBounds().height - topRight.getHeight() - botRight.getHeight()
			)
		);

		for (int y = topRight.getHeight();
		     y < getBounds().height - botRight.getHeight();
		     y += edgeRight.getHeight())
		{
			graphics.drawImage(edgeRight, getBounds().width - EDGE_IMAGE_THICKNESS, y, null);
		}

		//  top edge

		BufferedImage edgeTop = imageCache.get(EDGE_TOP);
		assert edgeTop != null;

		graphics.setClip(
			new Rectangle(
				topLeft.getWidth(),
				0,
				getBounds().width - topLeft.getWidth() - topRight.getWidth(),
				EDGE_IMAGE_THICKNESS
			)
		);

		for (int x = topLeft.getWidth();
		     x < getBounds().width - topRight.getWidth();
		     x += edgeTop.getWidth())
		{
			graphics.drawImage(edgeTop, x, 0, null);
		}

		//  bottom edge

		BufferedImage edgeBot = imageCache.get(EDGE_BOT);
		assert edgeBot != null;

		graphics.setClip(
			new Rectangle(
				0,
				getBounds().height - EDGE_IMAGE_THICKNESS,
				getBounds().width - botRight.getWidth(),
				EDGE_IMAGE_THICKNESS
			)
		);

		for (int x = botLeft.getWidth();
		     x < getBounds().width - topRight.getWidth();
		     x += edgeBot.getWidth())
		{
			graphics.drawImage(edgeBot, x, getBounds().height - EDGE_IMAGE_THICKNESS, null);
		}
	}

	private void renderTitle(Graphics2D graphics, String title, int killCount, BufferedImage closeButtonImage)
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
		int titleAreaWidth = showKillCount ? (getBounds().width - closeButtonImage.getWidth()) / 2 : getBounds().width / 2;
		int titleX = titleAreaWidth - ((int) titleBounds.getWidth() / 2);

		// Draw shadow
		graphics.setColor(Color.BLACK);
		graphics.drawString(title, titleX + 1, TITLE_OFFSET_Y + 1);

		// Draw actual text
		graphics.setColor(JagexColors.DARK_ORANGE_INTERFACE_TEXT);
		graphics.drawString(title, titleX, TITLE_OFFSET_Y);
	}

	private void renderCloseButton(Graphics2D graphics, BufferedImage closeButtonImage)
	{
		int closeX = getBounds().width - closeButtonImage.getWidth() - CLOSE_OFFSET_X;

		closeButtonBounds = rectangleFromImage(
			closeX,
			CLOSE_OFFSET_Y,
			closeButtonImage
		);

		graphics.drawImage(closeButtonImage, closeX, CLOSE_OFFSET_Y, null);
	}

	private void renderItems(Graphics2D graphics, List<LootItem> items)
	{
		int x = ITEM_START_X;
		int y = ITEM_START_Y;

		itemBounds.clear();

		int furthestItemX = getBounds().width - ITEM_LAST_OFFSET_X;

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
					y += itemImage.getHeight() + ITEM_SPACING;
				}
				else
				{
					x += itemImage.getWidth() + ITEM_SPACING;
				}
			}
		}
	}

	public void clearBounds()
	{
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

	public void shutDown()
	{
		clearBounds();
		clearCache();
	}

	public void clearBackgroundImage()
	{
		imageCache.invalidate("background");
	}

	public void clearCache()
	{
		imageCache.invalidateAll();
	}

	@Override
	public void revalidate()
	{
		if (getPreferredLocation() != null)
		{
			return;
		}

		// called after Overlay.reset() is called

		BufferedImage backgroundImage = plugin.isCustomBackgroundEnabled() ? getBackgroundImage() : null;
		if (backgroundImage != null)
		{
			setPreferredSize(true);
			setPreferredLocation();
		}
		else
		{
			setPreferredSize(false);
			setPreferredLocation(DEFAULT_CHEST_WIDTH, DEFAULT_CHEST_HEIGHT);
		}
	}

	// can only be called after setPreferredSize
	public void setPreferredLocation()
	{
		setPreferredLocation(getPreferredSize().width, getPreferredSize().height);
	}

	public void setPreferredLocation(int width, int height)
	{
		setPreferredLocation(
			// Technically `(canvasWidth - width) / 2` is more correctly centered but,
			//  since the inventory is usually on the right we can do this to keep it more to the left
			new Point(
				(client.getCanvasWidth() / 2) - width,
				(client.getCanvasHeight() / 2) - height
			)
		);
	}

	public void setPreferredSize(boolean isCustomBackgroundEnabled)
	{
		if (getPreferredSize() == null || isCustomBackgroundEnabled)
		{
			BufferedImage backgroundImage = isCustomBackgroundEnabled ? getBackgroundImage() : null;
			Dimension size = getSize(backgroundImage);
			setPreferredSize(size);
			setResizable(backgroundImage == null);
		}
		else
		{
			setResizable(true);
		}
	}

	private Dimension getSize(@Nullable BufferedImage backgroundImage)
	{
		if (backgroundImage != null)
		{
			return new Dimension(backgroundImage.getWidth(), backgroundImage.getHeight());
		}
		else
		{
			return new Dimension(DEFAULT_CHEST_WIDTH, DEFAULT_CHEST_HEIGHT);
		}
	}
}
