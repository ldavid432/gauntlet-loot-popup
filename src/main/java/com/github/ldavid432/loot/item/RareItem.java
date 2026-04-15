package com.github.ldavid432.loot.item;

import com.github.ldavid432.GauntletLootConfig;
import java.util.function.Function;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.runelite.api.gameval.ItemID;

/**
 * "Rare" or notable items to play a sound for and/or make examinable in the popup
 */
@AllArgsConstructor
public enum RareItem
{
	// Gauntlet
	CRYSTAL_WEAPON_SEED(ItemID.CRYSTAL_SEED_OLD, "A seed to be sung into the finest crystal weapons.", GauntletLootConfig::shouldPlayWeaponSeedSound),
	CRYSTAL_ARMOUR_SEED(ItemID.PRIF_ARMOUR_SEED, "A seed to be sung into the finest crystal armour.", GauntletLootConfig::shouldPlayArmourSeedSound),
	ENHANCED_WEAPON_SEED(ItemID.PRIF_WEAPON_SEED_ENHANCED, "A seed to be sung into the most powerful crystal weaponry.", GauntletLootConfig::shouldPlayEnhancedSeedSound),
	YOUNGLLEF(ItemID.GAUNTLETPET, "Looks like a bit of a nightmare.", GauntletLootConfig::shouldPlayPetSound),
	ELITE_SCROLL_BOX(ItemID.LEAGUE_CLUE_BOX_ELITE, "Contains an elite clue scroll.", GauntletLootConfig::shouldPlayEliteClueSound),
	GAUNTLET_CAPE(ItemID.GAUNTLET_CRYSTALLINE_CAPE, "Earned by only the most accomplished warriors of Prifddinas."),
	CRYSTAL_SHARD(ItemID.PRIF_CRYSTAL_SHARD, "A shard of the finest crystal, from the crystal city itself."),
	// Leagues
	ECHO_ORB(ItemID.BESTIAL_ECHOING_ORB, "Crystallised, bestial rage swirls within it...", GauntletLootConfig::shouldPlayEchoOrbSound),
	CRYSTAL_BLESSING(ItemID.CRYSTAL_BLESSING, "A powerful crystal blessing that warps the crystalline nature of reality.", GauntletLootConfig::shouldPlayCrystalBlessingSound),
	;

	@Getter
	private final int itemId;
	@Getter
	private final String examineText;
	private final Function<GauntletLootConfig, Boolean> shouldPlaySound;

	public boolean shouldPlaySound(GauntletLootConfig config)
	{
		return shouldPlaySound.apply(config);
	}

	// Examine text only constructor
	RareItem(int itemId, String examineText)
	{
		this(itemId, examineText, c -> false);
	}

}
