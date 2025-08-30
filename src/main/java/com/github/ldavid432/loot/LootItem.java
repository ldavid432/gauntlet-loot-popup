package com.github.ldavid432.loot;

import com.github.ldavid432.GauntletLootConfig;
import java.util.function.Function;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.runelite.api.gameval.ItemID;

/**
 * Notable items to play a sound for and/or make examinable in the poupup
 */
@AllArgsConstructor
public enum LootItem
{
	// Gauntlet
	CRYSTAL_WEAPON_SEED(ItemID.CRYSTAL_SEED_OLD, GauntletLootConfig::shouldPlayWeaponSeedSound, "A seed to be sung into the finest crystal weapons."),
	CRYSTAL_ARMOUR_SEED(ItemID.PRIF_ARMOUR_SEED, GauntletLootConfig::shouldPlayArmourSeedSound, "A seed to be sung into the finest crystal armour."),
	ENHANCED_WEAPON_SEED(ItemID.PRIF_WEAPON_SEED_ENHANCED, GauntletLootConfig::shouldPlayEnhancedSeedSound, "A seed to be sung into the most powerful crystal weaponry."),
	YOUNGLLEF(ItemID.GAUNTLETPET, GauntletLootConfig::shouldPlayPetSound, "Looks like a bit of a nightmare."),
	ELITE_SCROLL_BOX(ItemID.LEAGUE_CLUE_BOX_ELITE, GauntletLootConfig::shouldPlayEliteClueSound, "Contains an elite clue scroll."),
	GAUNTLET_CAPE(ItemID.GAUNTLET_CRYSTALLINE_CAPE, "Earned by only the most accomplished warriors of Prifddinas."),
	CRYSTAL_SHARD(ItemID.PRIF_CRYSTAL_SHARD, "A shard of the finest crystal, from the crystal city itself."),
	;

	@Getter
	private final int itemId;
	private final Function<GauntletLootConfig, Boolean> shouldPlaySound;
	@Getter
	private final String examineText;

	public boolean shouldPlaySound(GauntletLootConfig config)
	{
		return shouldPlaySound.apply(config);
	}

	// Examine text only constructor
	LootItem(int itemId, String examineText)
	{
		this(itemId, c -> false, examineText);
	}
}
