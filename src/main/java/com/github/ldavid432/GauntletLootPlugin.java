/*
 * Copyright (c) 2018, Seth <Sethtroll3@gmail.com>
 * Copyright (c) 2026, Lake <ldavid432@gmail.com>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.github.ldavid432;

import static com.github.ldavid432.GauntletLootConfig.CHEST_TITLE;
import static com.github.ldavid432.GauntletLootUtil.BACKGROUND_IMAGE_NAME;
import static com.github.ldavid432.GauntletLootUtil.KC_PATTERN;
import static com.github.ldavid432.GauntletLootUtil.anyMenuEntry;
import static com.github.ldavid432.GauntletLootUtil.getMousePosition;
import com.github.ldavid432.config.GauntletTitle;
import com.github.ldavid432.loot.Loot;
import com.github.ldavid432.loot.LootSource;
import com.github.ldavid432.loot.item.LootItem;
import com.google.common.collect.ImmutableList;
import com.google.inject.Provides;
import java.awt.Color;
import java.awt.Point;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import javax.imageio.ImageIO;
import javax.inject.Inject;
import javax.swing.SwingUtilities;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.Menu;
import net.runelite.api.MenuAction;
import net.runelite.api.MenuEntry;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.ClientTick;
import net.runelite.api.events.CommandExecuted;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.MenuShouldLeftClick;
import net.runelite.api.gameval.ItemID;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.chat.ChatColorType;
import net.runelite.client.chat.ChatMessageBuilder;
import net.runelite.client.chat.ChatMessageManager;
import net.runelite.client.chat.QueuedMessage;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.events.ServerNpcLoot;
import net.runelite.client.game.ItemManager;
import net.runelite.client.game.ItemStack;
import net.runelite.client.input.MouseAdapter;
import net.runelite.client.input.MouseListener;
import net.runelite.client.input.MouseManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.PluginManager;
import net.runelite.client.plugins.loottracker.LootReceived;
import net.runelite.client.ui.JagexColors;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.util.ColorUtil;
import net.runelite.client.util.Filepath;
import net.runelite.client.util.ImageUtil;
import net.runelite.client.util.QuantityFormatter;

@Slf4j
@PluginDescriptor(
	name = "Gauntlet Chest Popup",
	description = "Barrows chest style UI for the gauntlet chest!",
	tags = {"gauntlet", "loot", "chest", "sound", "hunllef", "hunlef"},
	legacyDataDirectory = "gauntlet-chest-popup",
	internalName = "gauntlet-chest-popup"
)
public class GauntletLootPlugin extends Plugin
{
	@Inject
	private Client client;

	@Inject
	private MouseManager mouseManager;

	@Inject
	private ItemManager itemManager;

	@Inject
	private OverlayManager overlayManager;

	@Inject
	private ChatMessageManager chatMessageManager;

	@Inject
	private ConfigManager configManager;

	@Inject
	private ClientThread clientThread;

	@Inject
	private PluginManager pluginManager;

	@Inject
	private GauntletLootOverlay overlay;

	@Inject
	private GauntletLootConfig config;

	@Getter
	@Setter
	private Loot loot = null;

	/**
	 * Last kill count seen. Since this message appears as soon as you complete the gauntlet we are safe to store it
	 * and show it in the UI without bothering to check which particular type of gauntlet it is.
	 * <br>
	 * The only time this causes an issue is if the user closes RL before opening the chest
	 * <br>
	 * As of later versions of gauntlet chest popup this is now just a backup as we now get the KC from the chat commands config if possible.
	 */
	private int lastKillCount = 0;

	@Getter
	private boolean isShowKillCountEnabled = false;

	@Getter
	private boolean isCustomBackgroundEnabled = false;

	@Getter
	private boolean isResourcePacksIntegrationEnabled = true;

	private ExecutorService executor = null;

	public Filepath getBackgroundImageFilepath() throws IOException
	{
		return getPluginDirectory().joinSegment(BACKGROUND_IMAGE_NAME);
	}

	@Override
	protected void startUp() throws Exception
	{
		overlayManager.add(overlay);
		mouseManager.registerMouseListener(mouseListener);
		client.getCanvas().addKeyListener(keyListener);

		ensureCustomBackgroundExists();

		String legacyTitle = configManager.getConfiguration(GauntletLootConfig.GROUP, "chestTitleText");
		if (legacyTitle != null)
		{
			// Migrate if custom
			if (legacyTitle.equals("CUSTOM"))
			{
				configManager.setConfiguration(GauntletLootConfig.GROUP, CHEST_TITLE, GauntletTitle.CUSTOM);
			}

			configManager.unsetConfiguration(GauntletLootConfig.GROUP, "chestTitleText");
		}

		// will be null for new installs
		Integer lastSeenVersion = configManager.getConfiguration(GauntletLootConfig.GROUP, "lastSeenVersion", Integer.class);
		if (lastSeenVersion != null && lastSeenVersion < GauntletLootConfig.CURRENT_VERSION)
		{
			chatMessageManager.queue(
				QueuedMessage.builder()
					.type(ChatMessageType.CONSOLE)
					.runeLiteFormattedMessage(
						ColorUtil.wrapWithColorTag("Gauntlet Chest Popup has been updated!<br>", Color.RED) +
							ColorUtil.wrapWithColorTag("* The popup is now resizable! (unless you have a custom background enabled)<br>", Color.RED) +
							ColorUtil.wrapWithColorTag("* The Resource packs plugin can now change the look of the popup", Color.RED) +
							ColorUtil.wrapWithColorTag("* Added a config to enable a chest value chat message", Color.RED)
					)
					.build()
			);
		}
		configManager.setConfiguration(GauntletLootConfig.GROUP, "lastSeenVersion", GauntletLootConfig.CURRENT_VERSION);

		isShowKillCountEnabled = config.isShowKillCountEnabled();
		isCustomBackgroundEnabled = config.isCustomChestBackgroundEnabled();
		isResourcePacksIntegrationEnabled = config.isResourcePacksIntegrationEnabled();
	}

	@Override
	protected void shutDown() throws Exception
	{
		clearLoot();
		client.getCanvas().removeKeyListener(keyListener);
		mouseManager.unregisterMouseListener(mouseListener);
		overlay.shutDown();
		overlayManager.remove(overlay);
		if (executor != null)
		{
			executor.shutdownNow();
			executor = null;
		}
	}

	private void ensureCustomBackgroundExists() throws IOException
	{
		boolean pluginFolderExists = getPluginDirectory().exists();

		if (!pluginFolderExists || !getBackgroundImageFilepath().exists())
		{
			if (executor == null)
			{
				executor = Executors.newFixedThreadPool(1);
			}
			executor.submit(() -> {
				try (OutputStream outputStream = getBackgroundImageFilepath().openOutputStream())
				{
					if (!pluginFolderExists)
					{
						getPluginDirectory().createDirectory();
					}

					BufferedImage defaultImage = ImageUtil.loadImageResource(GauntletLootPlugin.class, BACKGROUND_IMAGE_NAME);

					ImageIO.write(defaultImage, "png", outputStream);
				}
				catch (Exception ignored)
				{
				}
				finally
				{
					executor.shutdown();
					executor = null;
				}
			});
		}
	}

	@Subscribe
	public void onConfigChanged(ConfigChanged configChanged)
	{
		if (Objects.equals(configChanged.getGroup(), GauntletLootConfig.GROUP))
		{
			if (isDisplayed())
			{
				if (Objects.equals(configChanged.getKey(), GauntletLootConfig.CHEST_COLOR))
				{
					loot.updateImage(config);
				}
				else if (Objects.equals(configChanged.getKey(), CHEST_TITLE))
				{
					loot.updateTitle(config);
				}
			}

			if (Objects.equals(configChanged.getKey(), GauntletLootConfig.SHOW_KC))
			{
				isShowKillCountEnabled = config.isShowKillCountEnabled();
			}
			if (Objects.equals(configChanged.getKey(), GauntletLootConfig.RESOURCE_PACKS))
			{
				isResourcePacksIntegrationEnabled = config.isResourcePacksIntegrationEnabled();
				clientThread.invokeLater(() -> overlay.clearCache());
			}
			else if (Objects.equals(configChanged.getKey(), GauntletLootConfig.CUSTOM_BACKGROUND))
			{
				isCustomBackgroundEnabled = config.isCustomChestBackgroundEnabled();
				overlay.clearBackgroundImage();
				overlay.setPreferredSize(isCustomBackgroundEnabled);
				overlay.setResizable(!isCustomBackgroundEnabled);
			}
		}
		// Resource Packs enabled
		else if (Objects.equals(configChanged.getGroup(), "runelite") && Objects.equals(configChanged.getKey(), "resourcepacksplugin")) {
			boolean newValue = Boolean.parseBoolean(configChanged.getNewValue());

			Plugin resourcePacksPlugin = pluginManager.getPlugins().stream()
				.filter(plugin -> Objects.equals(plugin.getName(), "Resource packs"))
				.findFirst()
				.orElse(null);

			if (resourcePacksPlugin == null)
			{
				return;
			}

			clientThread.invokeLater(() -> {
				if (pluginManager.isPluginActive(resourcePacksPlugin) == newValue)
				{
					overlay.clearCache();
					return true;
				}
				else
				{
					// Wait for plugin to startup since at this configChanged call it is not actually initialized
					return pluginManager.isPluginActive(resourcePacksPlugin) == newValue;
				}
			});
		}
		// Resource pack changed
		else if (Objects.equals(configChanged.getGroup(), "resourcepacks") &&
			(Objects.equals(configChanged.getKey(), "selectedHubPack") || Objects.equals(configChanged.getKey(), "resourcePack")))
		{
			clientThread.invokeLater(() -> overlay.clearCache());
		}
	}

	boolean isDisplayed()
	{
		return loot != null;
	}

	private void clearLoot()
	{
		loot = null;
		lastKillCount = 0;
		overlay.clearBackgroundImage();
		overlay.clearBounds();
	}

	@Subscribe
	public void onCommandExecuted(CommandExecuted event)
	{
		// For debugging and previewing your theme
		if (event.getCommand().equals("gauntletlootpreview") || event.getCommand().equals("gauntlet-chest"))
		{
			log.debug("Displaying Gauntlet loot preview");

			LootSource source = LootSource.GAUNTLET;
			if (event.getArguments().length >= 1 &&
				(event.getArguments()[0].equalsIgnoreCase("corrupted") || event.getArguments()[0].equalsIgnoreCase("-c")))
			{
				source = LootSource.CORRUPTED_GAUNTLET;
			}

			processLoot(
				source.getSourceName(),
				ImmutableList.of(
					new ItemStack(ItemID.NATURERUNE, 130),
					new ItemStack(ItemID.PRIF_CRYSTAL_SHARD, 8),
					new ItemStack(ItemID.RUNE_FULL_HELM + 1, 4),
					new ItemStack(ItemID.RUNE_PICKAXE + 1, 3)
				)
			);
		}
	}

	@Subscribe
	public void onChatMessage(ChatMessage event)
	{
		if (event.getType() == ChatMessageType.GAMEMESSAGE && event.getMessage() != null)
		{
			Matcher matcher = KC_PATTERN.matcher(event.getMessage());
			if (matcher.matches())
			{
				Integer kc;
				try
				{
					kc = Integer.parseInt(matcher.group(1).replace(",", ""));
				}
				catch (Exception ignored)
				{
					kc = null;
				}

				if (kc != null)
				{
					lastKillCount = kc;
				}
			}
		}
	}

	@Subscribe
	public void onServerNpcLoot(ServerNpcLoot event)
	{
		if (event.getComposition() == null ||
			event.getComposition().getName() == null
		)
		{
			return;
		}

		processLoot(event.getComposition().getName(), event.getItems());
	}

	@Subscribe
	public void onLootReceived(LootReceived event)
	{
		if (event.getName() == null)
		{
			return;
		}

		processLoot(event.getName(), event.getItems());
	}

	private void processLoot(String sourceName, Collection<ItemStack> lootItems)
	{
		Arrays.stream(LootSource.values())
			.filter(source -> source.getSourceName().equals(sourceName))
			.findFirst()
			.ifPresent(source -> {
				log.debug("Displaying Gauntlet popup. Source: {}", source.getSourceName());

				// Get KC from chat commands plugin if possible
				Integer kc = configManager.getRSProfileConfiguration("killcount", source.name().toLowerCase().replace("_", " "), Integer.class);
				if (kc == null || kc < 0)
				{
					kc = lastKillCount;
				}

				overlay.setPreferredSize(isCustomBackgroundEnabled);
				if (overlay.getPreferredLocation() == null) overlay.setPreferredLocation();

				loot = Loot.of(source, lootItems, kc, config, itemManager, () -> {
					log.debug("Playing rare item sound for Gauntlet loot");
					// Rare item sound
					client.playSoundEffect(6765);
				});

				if (config.showChatMessage())
				{
					sendChatMessage(lootItems);
				}
			});
	}
	
	private void sendChatMessage(Collection<ItemStack> lootItems)
	{
		lootItems.stream()
			.map(stack -> {
				if (config.showHighAlchValue())
				{
					return itemManager.getItemComposition(stack.getId()).getHaPrice() * stack.getQuantity();
				}
				else
				{
					return itemManager.getItemPrice(stack.getId()) * stack.getQuantity();
				}
			})
			.reduce(Integer::sum)
			.ifPresent(sum -> {
				// Message structure copied from RuneLite BarrowsPlugin.java
				final ChatMessageBuilder message = new ChatMessageBuilder()
					.append(ChatColorType.HIGHLIGHT)
					.append("Your chest is worth around ")
					.append(QuantityFormatter.formatNumber(sum))
					.append(" coins.")
					.append(ChatColorType.NORMAL);

				chatMessageManager.queue(QueuedMessage.builder()
					.type(ChatMessageType.ITEM_EXAMINE)
					.runeLiteFormattedMessage(message.build())
					.build());
			});
	}

	@Provides
	GauntletLootConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(GauntletLootConfig.class);
	}

	private final KeyListener keyListener = new KeyAdapter()
	{
		@Override
		public void keyPressed(KeyEvent e)
		{
			if (isDisplayed() && e.getKeyCode() == KeyEvent.VK_ESCAPE)
			{
				clearLoot();
			}
		}
	};

	private final MouseListener mouseListener = new MouseAdapter()
	{
		@Override
		public MouseEvent mousePressed(MouseEvent event)
		{
			if (isDisplayed() && SwingUtilities.isLeftMouseButton(event))
			{
				if (overlay.isInBounds(event.getPoint()))
				{
					if (!client.isMenuOpen())
					{
						if (overlay.isInCloseButtonBounds(event.getPoint()))
						{
							log.debug("Gauntlet popup closed");
							clearLoot();
						}
						else
						{
							LootItem item = overlay.getItemClicked(event.getPoint());
							if (item != null)
							{
								// Don't consume event so the menu can be triggered
								return event;
							}
						}

						// Either Random click somewhere on the popup or close button
						event.consume();
					}
				}
				else if (config.isClickOutsideToDismissEnabled() &&
					anyMenuEntry(client, (entry -> Objects.equals(entry.getOption(), "Walk here"))))
				{
					// Dismiss if clicked outside in the world (somewhere with 'Walk here')
					//  This prevents dismissing when clicking UI elements like the inventory or chat
					log.debug("Dismissing Gauntlet popup");
					clearLoot();
				}
			}

			return event;
		}
	};

	@Subscribe
	public void onClientTick(ClientTick clientTick)
	{
		if (isDisplayed() && !client.isMenuOpen())
		{
			Point mousePos = getMousePosition(client);

			LootItem item = overlay.getItemClicked(mousePos);

			if (item != null)
			{
				final Menu menu = client.getMenu();

				MenuEntry examine = menu.createMenuEntry(-1)
					.setOption("Examine")
					.setTarget(ColorUtil.wrapWithColorTag(item.getItemName(), JagexColors.MENU_TARGET))
					.setType(MenuAction.RUNELITE)
					.setItemId(item.getId())
					.onClick(
						entry -> {
							log.debug("Examining Gauntlet popup item");
							client.addChatMessage(ChatMessageType.ITEM_EXAMINE, "", item.getExamineText(), "");
						}
					);

				MenuEntry cancel = menu.createMenuEntry(-1)
					.setOption("Cancel")
					.setType(MenuAction.CANCEL);

				menu.setMenuEntries(new MenuEntry[]{cancel, examine});
			}
			else if (overlay.isInCloseButtonBounds(mousePos))
			{
				final Menu menu = client.getMenu();

				MenuEntry close = menu.createMenuEntry(-1)
					.setOption("Close")
					.setType(MenuAction.RUNELITE)
					.onClick(entry -> clearLoot());

				MenuEntry cancel = menu.createMenuEntry(-1)
					.setOption("Cancel")
					.setType(MenuAction.CANCEL);

				menu.setMenuEntries(new MenuEntry[]{cancel, close});
			}
			else if (overlay.isInBounds(mousePos))
			{
				// Prevent actions from behind the overlay from showing
				final Menu menu = client.getMenu();

				MenuEntry cancel = menu.createMenuEntry(-1)
					.setOption("Cancel")
					.setType(MenuAction.CANCEL);

				menu.setMenuEntries(new MenuEntry[]{cancel});
			}
		}
	}

	@Subscribe
	public void onMenuShouldLeftClick(MenuShouldLeftClick event)
	{
		// Make the menu open on a left click when over an item
		if (isDisplayed() && !client.isMenuOpen())
		{
			Point mousePos = getMousePosition(client);

			LootItem item = overlay.getItemClicked(mousePos);

			if (item != null)
			{
				event.setForceRightClick(true);
			}
		}
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged event)
	{
		// Reset on logout
		if (event.getGameState() == GameState.LOGIN_SCREEN && isDisplayed())
		{
			clearLoot();
		}
	}

}
