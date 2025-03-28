package com.github.ldavid432;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class GauntletLootTest
{
	public static void main(String[] args) throws Exception
	{
		ExternalPluginManager.loadBuiltin(GauntletLootPlugin.class);
		RuneLite.main(args);
	}
}
