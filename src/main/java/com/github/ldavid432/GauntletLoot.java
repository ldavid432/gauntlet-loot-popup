package com.github.ldavid432;

import java.util.List;
import javax.annotation.Nonnull;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import net.runelite.client.game.ItemStack;

@Data
@RequiredArgsConstructor
public class GauntletLoot
{
	// Crystalline or Corrupted Hunllef
	@Nonnull
	private final String source;
	@Nonnull
	private final List<ItemStack> items;
}
