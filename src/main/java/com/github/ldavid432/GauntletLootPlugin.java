package com.github.ldavid432;

import static com.github.ldavid432.GauntletLootUtil.CORRUPTED_HUNLLEF;
import static com.github.ldavid432.GauntletLootUtil.HUNLLEF;
import static com.github.ldavid432.GauntletLootUtil.LOOT_SOURCES;
import static com.github.ldavid432.GauntletLootUtil.anyMenuEntry;
import static com.github.ldavid432.GauntletLootUtil.getMousePosition;
import com.github.ldavid432.config.GauntletTitle;
import com.github.ldavid432.config.GauntletTitle2;
import com.google.common.collect.ImmutableList;
import com.google.inject.Provides;
import java.awt.Color;
import java.awt.Point;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.util.Objects;
import javax.inject.Inject;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.Menu;
import net.runelite.api.MenuAction;
import net.runelite.api.MenuEntry;
import net.runelite.api.events.ClientTick;
import net.runelite.api.events.CommandExecuted;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.MenuShouldLeftClick;
import net.runelite.api.gameval.ItemID;
import net.runelite.client.chat.ChatMessageManager;
import net.runelite.client.chat.QueuedMessage;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ServerNpcLoot;
import net.runelite.client.game.ItemManager;
import net.runelite.client.game.ItemStack;
import net.runelite.client.input.MouseAdapter;
import net.runelite.client.input.MouseListener;
import net.runelite.client.input.MouseManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.JagexColors;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.util.ColorUtil;

@Slf4j
@PluginDescriptor(
	name = "Gauntlet Chest Popup",
	description = "Barrows chest style UI for the gauntlet chest!",
	tags = {"gauntlet", "loot", "chest", "sound", "hunllef", "hunlef"}
)
public class GauntletLootPlugin extends Plugin
{
	private static final int MENU_EXAMINE_ID = -1_337_000;

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
	private GauntletLootOverlay overlay;

	@Inject
	private GauntletLootConfig config;

	@Getter
	@Setter
	private GauntletLoot loot = null;

	@Override
	protected void startUp() throws Exception
	{
		overlayManager.add(overlay);
		mouseManager.registerMouseListener(mouseListener);
		client.getCanvas().addKeyListener(keyListener);

		if (config.getChestTitleLegacy() != GauntletTitle.UNSET)
		{
			// Migrate if custom
			if (config.getChestTitleLegacy() == GauntletTitle.CUSTOM)
			{
				config.setChestTitle2(GauntletTitle2.CUSTOM);
			}

			config.setChestTitleLegacy(GauntletTitle.UNSET);
		}

		// Since last seen version wasn't in 1.0 checking for only it will trigger for everyone who installs the plugin.
		//  By only triggering this during startup while not logged in we can "better" attempt to determine if this is a previous install or not.
		//  Still not totally accurate but better than nothing.
		if (config.getLastSeenVersion() < GauntletLootConfig.CURRENT_VERSION)
		{
			if (client.getGameState() != GameState.LOGGED_IN)
			{
				chatMessageManager.queue(
					QueuedMessage.builder()
						.type(ChatMessageType.CONSOLE)
						.runeLiteFormattedMessage(ColorUtil.wrapWithColorTag("Gauntlet Chest Popup has been updated! The popup is now movable!", Color.RED))
						.build()
				);
			}
			config.setLastSeenVersion(GauntletLootConfig.CURRENT_VERSION);
		}
	}

	@Override
	protected void shutDown() throws Exception
	{
		client.getCanvas().removeKeyListener(keyListener);
		mouseManager.unregisterMouseListener(mouseListener);
		overlayManager.remove(overlay);
	}

	boolean isDisplayed()
	{
		return loot != null;
	}

	private void clearLoot()
	{
		loot = null;
	}

	@Subscribe
	public void onCommandExecuted(CommandExecuted event)
	{
		// For debugging and previewing your theme
		if (event.getCommand().equals("gauntletlootpreview"))
		{
			log.debug("Displaying Gauntlet loot preview");

			String source = HUNLLEF;
			if (event.getArguments().length >= 1 && event.getArguments()[0].equalsIgnoreCase("corrupted"))
			{
				source = CORRUPTED_HUNLLEF;
			}

			loot = new GauntletLoot(
				source,
				ImmutableList.of(
					new ItemStack(ItemID.NATURERUNE, 130),
					new ItemStack(ItemID.PRIF_CRYSTAL_SHARD, 8),
					new ItemStack(ItemID.RUNE_FULL_HELM + 1, 4),
					new ItemStack(ItemID.RUNE_PICKAXE + 1, 3)
				)
			);

			checkSound();
		}
	}

	@Subscribe
	public void onServerNpcLoot(ServerNpcLoot event)
	{
		if (event.getComposition() == null ||
			event.getComposition().getName() == null ||
			!LOOT_SOURCES.contains(event.getComposition().getName())
		)
		{
			return;
		}

		log.debug("Displaying Gauntlet popup. Source: {}", event.getComposition().getName());

		loot = new GauntletLoot(event.getComposition().getName(), ImmutableList.copyOf(event.getItems()));

		checkSound();
	}

	private void checkSound()
	{
		if (loot.getItems().stream().anyMatch(this::shouldPlayRareSound))
		{
			log.debug("Playing rare item sound for Gauntlet loot");
			// Muspah rare item sound
			client.playSoundEffect(6765);
		}
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
			if (isDisplayed() && event.getButton() == 1)
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
						else if (config.isExamineEnabled())
						{
							Integer itemId = overlay.getItemClicked(event.getPoint());
							if (itemId != null)
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

			Integer itemId = overlay.getItemClicked(mousePos);
			if (config.isExamineEnabled() && itemId != null)
			{
				final String itemName = itemManager.getItemComposition(itemId).getName();

				final Menu menu = client.getMenu();

				MenuEntry examine = menu.createMenuEntry(-1)
					.setOption("Examine")
					.setTarget(ColorUtil.wrapWithColorTag(itemName, JagexColors.MENU_TARGET))
					.setType(MenuAction.RUNELITE)
					.setItemId(itemId)
					.setIdentifier(MENU_EXAMINE_ID)
					.onClick(
						entry -> {
							log.debug("Examining Gauntlet popup item");
							client.addChatMessage(ChatMessageType.ITEM_EXAMINE, "", getExamineText(entry.getItemId(), itemName), "");
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
		// Make the menu open on a left click when over on an item
		if (anyMenuEntry(client, entry -> entry.getIdentifier() == MENU_EXAMINE_ID))
		{
			event.setForceRightClick(true);
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

	private boolean shouldPlayRareSound(ItemStack stack)
	{
		switch (stack.getId())
		{
			case ItemID.CRYSTAL_SEED_OLD:
				return config.shouldPlayWeaponSeedSound();
			case ItemID.PRIF_ARMOUR_SEED:
				return config.shouldPlayArmourSeedSound();
			case ItemID.PRIF_WEAPON_SEED_ENHANCED:
				return config.shouldPlayEnhancedSeedSound();
			case ItemID.GAUNTLETPET:
				return config.shouldPlayPetSound();
			default:
				return false;
		}
	}

	// For the few notable items return their actual examine text, otherwise just return the item name
	private String getExamineText(int itemId, String itemName)
	{
		switch (itemId)
		{
			case ItemID.PRIF_CRYSTAL_SHARD:
				return "A shard of the finest crystal, from the crystal city itself.";
			case ItemID.CRYSTAL_SEED_OLD:
				return "A seed to be sung into the finest crystal weapons.";
			case ItemID.PRIF_ARMOUR_SEED:
				return "A seed to be sung into the finest crystal armour.";
			case ItemID.PRIF_WEAPON_SEED_ENHANCED:
				return "A seed to be sung into the most powerful crystal weaponry.";
			case ItemID.GAUNTLET_CRYSTALLINE_CAPE:
				return "Earned by only the most accomplished warriors of Prifddinas.";
			case ItemID.GAUNTLETPET:
				return "Looks like a bit of a nightmare.";
			default:
				return itemName;
		}
	}
}
