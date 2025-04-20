package com.github.ldavid432;

import static com.github.ldavid432.Util.anyMenuEntry;
import com.google.common.collect.ImmutableList;
import com.google.inject.Provides;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.inject.Inject;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.Menu;
import net.runelite.api.MenuAction;
import net.runelite.api.MenuEntry;
import net.runelite.api.Point;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.ClientTick;
import net.runelite.api.events.CommandExecuted;
import net.runelite.api.events.MenuShouldLeftClick;
import net.runelite.api.gameval.ItemID;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.game.ItemManager;
import net.runelite.client.game.ItemStack;
import net.runelite.client.input.MouseAdapter;
import net.runelite.client.input.MouseListener;
import net.runelite.client.input.MouseManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.loottracker.LootReceived;
import net.runelite.client.ui.JagexColors;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.util.ColorUtil;
import net.runelite.http.api.loottracker.LootRecordType;

@Slf4j
@PluginDescriptor(
	name = "Gauntlet Chest Popup",
	description = "Barrows-chest style UI for the gauntlet chest!",
	tags = {"gauntlet", "loot", "chest", "sound"}
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
	private GauntletLootOverlay overlay;

	@Inject
	private GauntletLootConfig config;

	@Getter
	@Setter
	@Nonnull
	private List<ItemStack> lootedItems = Collections.emptyList();

	@Override
	protected void startUp() throws Exception
	{
		overlayManager.add(overlay);
		mouseManager.registerMouseListener(mouseListener);
		client.getCanvas().addKeyListener(keyListener);
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
		return !lootedItems.isEmpty();
	}

	void clearLoot()
	{
		lootedItems = Collections.emptyList();
	}

	@Subscribe
	public void onCommandExecuted(CommandExecuted event)
	{
		// For debugging and previewing your theme
		if (event.getCommand().equals("gauntletlootpreview"))
		{
			log.debug("Displaying Gauntlet loot preview");

			onLootReceived(
				new LootReceived(
					"The Gauntlet",
					-1,
					null,
					List.of(
						new ItemStack(ItemID.NATURERUNE, 130),
						new ItemStack(ItemID.PRIF_CRYSTAL_SHARD, 8),
						new ItemStack(ItemID.RUNE_FULL_HELM + 1, 4),
						new ItemStack(ItemID.RUNE_PICKAXE + 1, 3)
					),
					1
				)
			);
		}
	}

	@Subscribe
	public void onLootReceived(LootReceived event)
	{
		if (!Objects.equals(event.getName(), "The Gauntlet"))
		{
			return;
		}

		log.debug("Displaying Gauntlet popup");

		lootedItems = ImmutableList.copyOf(event.getItems());

		if (lootedItems.stream().anyMatch(this::shouldPlayRareSound))
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
		if (isDisplayed() && config.isExamineEnabled() && !client.isMenuOpen())
		{
			Point rlMousePos = client.getMouseCanvasPosition();
			java.awt.Point mousePos = new java.awt.Point(rlMousePos.getX(), rlMousePos.getY());

			Integer itemId = overlay.getItemClicked(mousePos);
			if (itemId != null)
			{
				final String itemName = itemManager.getItemComposition(itemId).getName();

				final Menu menu = client.getMenu();

				MenuEntry examine = menu.createMenuEntry(0)
					.setOption("Examine")
					.setTarget(ColorUtil.wrapWithColorTag(itemName, JagexColors.MENU_TARGET))
					.setType(MenuAction.RUNELITE)
					.setItemId(itemId)
					.setIdentifier(MENU_EXAMINE_ID)
					.onClick(
						(entry) -> {
							log.debug("Examining Gauntlet popup item");
							client.addChatMessage(ChatMessageType.ITEM_EXAMINE, "", getExamineText(entry.getItemId(), itemName), "");
						}
					);

				MenuEntry cancel = menu.createMenuEntry(1)
					.setOption("Cancel")
					.setType(MenuAction.CANCEL);

				menu.setMenuEntries(new MenuEntry[]{cancel, examine});
			}
			else if (overlay.isInCloseButtonBounds(mousePos))
			{
				final Menu menu = client.getMenu();

				MenuEntry close = menu.createMenuEntry(0)
					.setOption("Close")
					.setType(MenuAction.RUNELITE)
					.onClick((entry) -> closing = true);

				MenuEntry cancel = menu.createMenuEntry(1)
					.setOption("Cancel")
					.setType(MenuAction.CANCEL);

				menu.setMenuEntries(new MenuEntry[]{cancel, close});
			}
		}
	}

	@Subscribe
	public void onMenuShouldLeftClick(MenuShouldLeftClick event)
	{
		if (anyMenuEntry(client, (entry) -> entry.getIdentifier() == MENU_EXAMINE_ID))
		{
			event.setForceRightClick(true);
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
			default:
				return itemName;
		}
	}
}
