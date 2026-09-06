package com.github.ldavid432;

import static com.github.ldavid432.GauntletLootUtil.CLOSE_OFFSET_X;
import static com.github.ldavid432.GauntletLootUtil.CLOSE_OFFSET_Y;
import static com.github.ldavid432.GauntletLootUtil.CUSTOM_BACKGROUND_IMAGE;
import static com.github.ldavid432.GauntletLootUtil.DEFAULT_CHEST_HEIGHT;
import static com.github.ldavid432.GauntletLootUtil.DEFAULT_CHEST_WIDTH;
import static com.github.ldavid432.GauntletLootUtil.DIVIDER_OFFSET_Y;
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
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
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

	private final LoadingCache<String, BufferedImage> imageCache = CacheBuilder.newBuilder()
		.maximumSize(IMAGE_CACHE_LIMIT)
		.build(
			new CacheLoader<>()
			{
				@Override
				public BufferedImage load(@Nonnull String imagePath)
				{
					return ImageUtil.loadImageResource(getClass(), imagePath);
				}
			}
		);

	private Rectangle closeButtonBounds;
	private final List<LootItemBounds> itemBounds = new ArrayList<>();
	private boolean initialPositionApplied = false;

	@Value
	private static class LootItemBounds
	{
		LootItem item;
		Rectangle bounds;
	}

	@Inject
	public GauntletLootOverlay(GauntletLootPlugin plugin, Client client, ItemManager itemManager, SpriteManager spriteManager,
							   GauntletLootConfig config)
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
		setResizable(true);

		setPreferredSize(config.isCustomChestBackgroundEnabled());
	}

	@SneakyThrows(ExecutionException.class)
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

	@SneakyThrows(ExecutionException.class)
	@Nullable
	private BufferedImage getBackgroundImage()
	{
		return imageCache.get("background", () -> {
			try
			{
				BufferedImage backgroundImage = ImageIO.read(CUSTOM_BACKGROUND_IMAGE);
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

		// lazily set preferred location once the canvas has a valid size
		if (!initialPositionApplied && getPreferredLocation() == null)
		{
			setPreferredLocation();
			setPreferredSize(plugin.isCustomBackgroundEnabled());
			initialPositionApplied = true;
		}

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

		return new Dimension(getBounds().width, getBounds().height);
	}

	private void renderBackground(Graphics2D graphics, BufferedImage backgroundImage)
	{
		if (backgroundImage != null)
		{
			// Custom background image
			graphics.drawImage(backgroundImage, 0, 0, null);
		}
		else
		{
			renderSpriteBackground(graphics);
		}
	}

	private void renderSpriteBackground(Graphics2D graphics)
	{
		Shape originalClip = graphics.getClip();

		// --Background--

		renderBacking(graphics);

		// --Frame--

		// -Corners-
		BufferedImage topLeft = spriteManager.getSprite(SpriteID.Steelborder.TOP_LEFT, 0);
		assert topLeft != null;
		graphics.drawImage(topLeft, 0, 0, null);

		BufferedImage topRight = spriteManager.getSprite(SpriteID.Steelborder.TOP_RIGHT, 0);
		assert topRight != null;
		graphics.drawImage(topRight, getBounds().width - topRight.getWidth(), 0, null);

		BufferedImage botLeft = spriteManager.getSprite(SpriteID.Steelborder.BOTTOM_LEFT, 0);
		assert botLeft != null;
		graphics.drawImage(botLeft, 0, getBounds().height - botLeft.getHeight(), null);

		BufferedImage botRight = spriteManager.getSprite(SpriteID.Steelborder.BOTTOM_RIGHT, 0);
		assert botRight != null;
		graphics.drawImage(botRight, getBounds().width - botRight.getWidth(), getBounds().height - botRight.getHeight(), null);

		// -Edges-
		BufferedImage edge = spriteManager.getSprite(SpriteID.Steelborder2.EDGE_RIGHT, 0);
		assert edge != null;

		renderBackgroundEdges(graphics, edge, topLeft, botLeft, topRight, botRight);

		//  divider
		BufferedImage divider = spriteManager.getSprite(SpriteID.SteelborderDivider._0, 0);
		assert divider != null;

		graphics.setClip(
			new Rectangle(
				edge.getWidth(),
				DIVIDER_OFFSET_Y,
				getBounds().width - edge.getWidth() - edge.getWidth(),
				// Use width since the sprite is actually vertical
				edge.getWidth()
			)
		);

		for (int x = edge.getWidth();
		     x < getBounds().width - edge.getWidth();
		     x += divider.getWidth())
		{
			drawRotated(graphics, divider, x, DIVIDER_OFFSET_Y, 0);
		}

		// reset clip
		graphics.setClip(originalClip);
	}

	private void renderBacking(Graphics2D graphics)
	{
		// TODO: setClip(getBounds)?
		graphics.setClip(
			new Rectangle(
				0,
				0,
				getBounds().width,
				getBounds().height
			)
		);

		BufferedImage bg = spriteManager.getSprite(SpriteID.TRADEBACKING, 0);
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

	private void renderBackgroundEdges(Graphics2D graphics, BufferedImage edge, BufferedImage topLeft,
	                                   BufferedImage botLeft, BufferedImage topRight, BufferedImage botRight)
	{
		//  left edge

		graphics.setClip(
			new Rectangle(
				0,
				topLeft.getHeight(),
				edge.getWidth(),
				getBounds().height - topLeft.getHeight() - botLeft.getHeight()
			)
		);

		for (int y = topLeft.getHeight();
		     y < getBounds().height - botLeft.getHeight();
		     y += edge.getHeight())
		{
			drawRotated(graphics, edge, 0, y, 180);
		}

		//  right edge

		graphics.setClip(
			new Rectangle(
				getBounds().width - edge.getWidth(),
				topRight.getHeight(),
				edge.getWidth(),
				getBounds().height - topRight.getHeight() - botRight.getHeight()
			)
		);

		for (int y = topRight.getHeight();
		     y < getBounds().height - botRight.getHeight();
		     y += edge.getHeight())
		{
			drawRotated(graphics, edge, getBounds().width - edge.getWidth(), y, 0);
		}

		//  top edge

		graphics.setClip(
			new Rectangle(
				topLeft.getWidth(),
				0,
				getBounds().width - topLeft.getWidth() - topRight.getWidth(),
				// Use width since the sprite is actually vertical
				edge.getWidth()
			)
		);

		for (int x = topLeft.getWidth();
		     x < getBounds().width - topRight.getWidth();
		     x += edge.getHeight())
		{
			drawRotated(graphics, edge, x, 0, 270);
		}

		//  bottom edge

		graphics.setClip(
			new Rectangle(
				0,
				getBounds().height - edge.getWidth(),
				getBounds().width - botRight.getWidth(),
				// Use width since the sprite is actually vertical
				edge.getWidth()
			)
		);

		for (int x = botLeft.getWidth();
		     x < getBounds().width - topRight.getWidth();
		     x += edge.getHeight())
		{
			drawRotated(graphics, edge, x, getBounds().height - edge.getWidth(), 90);
		}
	}

	// Draw an image rotated in a 90 degree increment
	public static void drawRotated(Graphics2D graphics, BufferedImage img, int destX, int destY, int k) {
		double theta;
		double tx;
		double ty;
		int w = img.getWidth();
		int h = img.getHeight();

		switch (k) {
			case 0: // 0 degrees
				theta = 0;
				tx = destX;
				ty = destY;
				break;
			case 90: // +90° CCW
				theta = Math.PI / 2.0;
				// translate by +h in x so rotated top-left lands at destX,destY
				tx = destX + h;
				ty = destY;
				break;
			case 180: // 180°
				theta = Math.PI;
				// translate by +w,+h
				tx = destX + w;
				ty = destY + h;
				break;
			case 270: // -90° (or 270° CCW)
				theta = -Math.PI / 2.0;
				// translate by 0, +w
				tx = destX;
				ty = destY + w;
				break;
			default:
				throw new AssertionError("unreachable");
		}

		AffineTransform rotate = AffineTransform.getRotateInstance(theta);
		AffineTransform at = AffineTransform.getTranslateInstance(tx, ty);
		at.concatenate(rotate); // final = Translate * Rotate
		graphics.drawImage(img, at, null);
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

	public void resetBounds()
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
		resetBounds();
		imageCache.invalidateAll();
	}

	public void clearBackgroundImage()
	{
		imageCache.invalidate("background");
	}

	@Override
	public void revalidate()
	{
		BufferedImage backgroundImage = plugin.isCustomBackgroundEnabled() ? getBackgroundImage() : null;
		if (backgroundImage != null && getPreferredLocation() == null)
		{
			setPreferredLocation();
			setPreferredSize(plugin.isCustomBackgroundEnabled());
		}
		else
		{
			setPreferredLocation(
				// Technically `(client.getCanvasWidth() - backgroundImage.getWidth()) / 2` is more correctly centered but
				//  since the inventory is usually on the right we can do this to keep it more to the left
				new Point(
					(client.getCanvasWidth() / 2) - DEFAULT_CHEST_WIDTH,
					(client.getCanvasHeight() / 2) - DEFAULT_CHEST_HEIGHT
				)
			);
			setPreferredSize(new Dimension(DEFAULT_CHEST_WIDTH, DEFAULT_CHEST_HEIGHT));
		}
	}

	private void setPreferredLocation()
	{
		setPreferredLocation(
			// Technically `(client.getCanvasWidth() - backgroundImage.getWidth()) / 2` is more correctly centered but
			//  since the inventory is usually on the right we can do this to keep it more to the left
			new Point(
				(client.getCanvasWidth() / 2) - getBounds().width,
				(client.getCanvasHeight() / 2) - getBounds().width
			)
		);
	}

	private void setPreferredSize(boolean isCustomBackgroundEnabled)
	{
		if (getPreferredSize() != null) return;

		BufferedImage backgroundImage = isCustomBackgroundEnabled ? getBackgroundImage() : null;
		if (backgroundImage != null)
		{
			setPreferredSize(new Dimension(backgroundImage.getWidth(), backgroundImage.getHeight()));
			setResizable(false);
		}
		else
		{
			setPreferredSize(new Dimension(DEFAULT_CHEST_WIDTH, DEFAULT_CHEST_HEIGHT));
			setResizable(true);
		}
	}
}
