package com.github.ldavid432;

import com.google.common.collect.ImmutableList;
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
import net.runelite.api.Client;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.game.ItemStack;
import net.runelite.client.input.MouseAdapter;
import net.runelite.client.input.MouseListener;
import net.runelite.client.input.MouseManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.loottracker.LootReceived;
import net.runelite.client.ui.overlay.OverlayManager;

@Slf4j
@PluginDescriptor(
	name = "Gauntlet Chest Popup",
	description = "Barrows-chest style UI for the gauntlet chest!",
	tags = {"gauntlet", "loot", "chest"}
)
public class GauntletLootPlugin extends Plugin
{
	static final String THE_GAUNTLET = "The Gauntlet";

	@Inject
	private Client client;

	@Inject
	private MouseManager mouseManager;

	@Inject
	private OverlayManager overlayManager;

	@Inject
	private GauntletLootOverlay overlay;

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

	@Subscribe
	public void onLootReceived(LootReceived event)
	{
		if (!Objects.equals(event.getName(), THE_GAUNTLET))
		{
			return;
		}

		log.debug("Displaying Gauntlet Loot");

		lootedItems = ImmutableList.copyOf(event.getItems());
	}

	private final KeyListener keyListener = new KeyAdapter()
	{
		@Override
		public void keyPressed(KeyEvent e)
		{
			if (!lootedItems.isEmpty() && e.getKeyCode() == KeyEvent.VK_ESCAPE)
			{
				lootedItems = Collections.emptyList();
			}
		}
	};

	private final MouseListener mouseListener = new MouseAdapter()
	{
		@Override
		public MouseEvent mousePressed(MouseEvent event)
		{
			if (!lootedItems.isEmpty() && overlay.getCloseButtonBounds() != null && overlay.getCloseButtonBounds().contains(event.getPoint()))
			{
				lootedItems = Collections.emptyList();
				event.consume();
			}
			return event;
		}
	};
}
