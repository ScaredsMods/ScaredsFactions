/*
*  Copyright (C) 2026 ScaredRabbitNL
*
*  This program is free software: you can redistribute it and/or modify
*  it under the terms of the GNU Lesser General Public License as published by
*  the Free Software Foundation, either version 3 of the License, or
*  (at your option) any later version.
*
*  This program is distributed in the hope that it will be useful,
*  but WITHOUT ANY WARRANTY; without even the implied warranty of
*  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
*  GNU Lesser General Public License for more details.
*
*  You should have received a copy of the GNU Lesser General Public License
*  along with this program. If not, see <https://www.gnu.org/licenses/>.
*/
package io.github.scaredsmods.scaredsfactions.common.faction;

import io.github.scaredsmods.scaredsfactions.common.ScaredsFactionMod;
import io.github.scaredsmods.scaredsfactions.api.common.faction.setting.AbstractFactionSetting;
import io.github.scaredsmods.scaredsfactions.api.common.faction.setting.EnumFactionSetting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class FactionSavedData extends SavedData {

	private final Map<String, Faction> factions = new HashMap<>();
	public static final String DATA_NAME = "scaredsfactions_faction_data";
	public static final ResourceLocation DATA_LOCATION = ScaredsFactionMod.id(DATA_NAME);
	private final Set<String> hardcoredFactions = new HashSet<>();
	private final Map<String, List<String>> alliedFactions = new HashMap<>();

	public static FactionSavedData load(@NotNull CompoundTag tag) {
		FactionSavedData data = create();
		ListTag factions = tag.getList("factions", Tag.TAG_COMPOUND);
		for (int i = 0; i < factions.size(); i++) {
			CompoundTag factionTag = factions.getCompound(i);
			String name = factionTag.getString("name");
			UUID owner = factionTag.getUUID("owner");

			List<AbstractFactionSetting<?, ?>> settings = Faction.createDefaultSettings();
			Faction faction = new Faction(name, owner, settings);

			if (factionTag.hasUUID("pending_transfer")) {
				faction.setPendingTransfer(factionTag.getUUID("pending_transfer"));
			}

			if (factionTag.contains("beacon")) {
				CompoundTag beaconTag = factionTag.getCompound("beacon");
				faction.setBeaconPos(new BlockPos(
						beaconTag.getInt("x"),
						beaconTag.getInt("y"),
						beaconTag.getInt("z")
				));
			}

			ListTag eliminatedPlayersTag = factionTag.getList("eliminated_players", Tag.TAG_COMPOUND);
			for (int j = 0; j < eliminatedPlayersTag.size(); j++) {
				faction.eliminatePlayer(eliminatedPlayersTag.getCompound(j).getUUID("uuid"));
			}

			ListTag factionSettings = factionTag.getList("settings", Tag.TAG_COMPOUND);
			for (int j = 0; j < factionSettings.size(); j++) {
				CompoundTag settingTag = factionSettings.getCompound(j);
				for (int k = 0; k < settings.size(); k++) {
					AbstractFactionSetting<?, ?> setting = settings.get(k);
					if (settingTag.contains(setting.getNbtId())) {
						settings.set(k, setting.load(settingTag));
					}
				}
			}

			ListTag cooldownsTag = factionTag.getList("players_on_home_cooldown", Tag.TAG_COMPOUND);
			for (int j = 0; j < cooldownsTag.size(); j++) {
				CompoundTag cooldownTag = cooldownsTag.getCompound(j);
				faction.setHomeCooldown(cooldownTag.getUUID("uuid"), cooldownTag.getLong("time_remaining"));
			}

			ListTag membersTag = factionTag.getList("members", Tag.TAG_COMPOUND);
			for (int j = 0; j < membersTag.size(); j++) {
				CompoundTag memberTag = membersTag.getCompound(j);
				UUID uuid = memberTag.getUUID("uuid");
				CompoundTag rankTag = memberTag.getCompound("rank");
				Faction.Rank rank = Faction.Rank.getRankByName(rankTag.getString("name"));
				faction.getMembers().put(uuid, rank);
			}

			ListTag alliesTag = factionTag.getList("allies", Tag.TAG_COMPOUND);
			for (int j = 0; j < alliesTag.size(); j++) {
				faction.addAlly(alliesTag.getCompound(j).getString("name"));
			}
			data.addFaction(faction);
		}

		ListTag hardcoredFactions = tag.getList("hardcored_factions", Tag.TAG_COMPOUND);
		for (int i = 0; i < hardcoredFactions.size(); i++) {
			CompoundTag hardcoredTag = hardcoredFactions.getCompound(i);
			String factionName = hardcoredTag.getString("name");
			data.hardcoredFactions.add(factionName);
		}

		for (Faction faction : data.factions.values()) {
			data.alliedFactions.put(faction.getName(), new ArrayList<>(faction.getAllies()));
		}

		for (Faction faction : data.factions.values()) {
			AbstractFactionSetting<?, ?> ownerRankSetting = faction.getSetting(FactionSettings.OWNER_RANK.getNbtId());
			if (ownerRankSetting instanceof EnumFactionSetting<?> enumSetting) {
				Object value = enumSetting.get();
				if (value instanceof Faction.Rank rank) {
					faction.setRank(faction.getOwner(), rank);
				}
			}
		}

		return data;
	}

	@Override
	public @NotNull CompoundTag save(@NotNull CompoundTag tag) {
		ListTag factions = new ListTag();
		for (Faction faction : this.factions.values()) {
			CompoundTag factionsTag = new CompoundTag();
			factionsTag.putString("name", faction.getName());
			factionsTag.putUUID("owner", faction.getOwner());

			if (faction.getPendingTransfer() != null) {
				factionsTag.putUUID("pending_transfer", faction.getPendingTransfer());
			}

			if (faction.hasBeacon()) {
				CompoundTag beaconTag = new CompoundTag();
				beaconTag.putInt("x", faction.getBeaconPos().getX());
				beaconTag.putInt("y", faction.getBeaconPos().getY());
				beaconTag.putInt("z", faction.getBeaconPos().getZ());
				factionsTag.put("beacon", beaconTag);
			}

			ListTag eliminatedPlayersTag = new ListTag();
			for (UUID uuid : faction.getEliminatedPlayers()) {
				CompoundTag eliminatedTag = new CompoundTag();
				eliminatedTag.putUUID("uuid", uuid);
				eliminatedPlayersTag.add(eliminatedTag);
			}
			factionsTag.put("eliminated_players", eliminatedPlayersTag);

			ListTag cooldownList = new ListTag();
			for (Map.Entry<UUID, Long> entry : faction.getPlayersOnHomeCooldown().entrySet()) {
				CompoundTag cooldownTag = new CompoundTag();
				cooldownTag.putUUID("uuid", entry.getKey());
				cooldownTag.putLong("time_remaining", entry.getValue());
				cooldownList.add(cooldownTag);
			}
			factionsTag.put("players_on_home_cooldown", cooldownList);

			ListTag settingsTag = new ListTag();
			for (AbstractFactionSetting<?, ?> setting : faction.getSettings()) {
				CompoundTag settings = new CompoundTag();
				setting.save(settings);
				settingsTag.add(settings);
			}

			factionsTag.put("settings", settingsTag);

			ListTag members = new ListTag();
			for (Map.Entry<UUID, Faction.Rank> entry : faction.getMembers().entrySet()) {
				CompoundTag membersTag = new CompoundTag();
				membersTag.putUUID("uuid", entry.getKey());

				CompoundTag rankTag = new CompoundTag();
				rankTag.putString("name", entry.getValue().getName());
				rankTag.putInt("rank_id", entry.getValue().getId());
				membersTag.put("rank", rankTag);
				members.add(membersTag);
			}
			factionsTag.put("members", members);

			ListTag alliesTag = new ListTag();
			for (String ally : faction.getAllies()) {
				CompoundTag allyTag = new CompoundTag();
				allyTag.putString("name", ally);
				alliesTag.add(allyTag);
			}
			factionsTag.put("allies", alliesTag);
			factions.add(factionsTag);
		}


		ListTag hardcoredFactions = new ListTag();
		for (String faction : this.hardcoredFactions) {
			CompoundTag hardcoredFactionTag = new CompoundTag();
			hardcoredFactionTag.putString("name", faction);
			hardcoredFactions.add(hardcoredFactionTag);
		}

		tag.put("factions", factions);
		tag.put("hardcored_factions", hardcoredFactions);
		return tag;
	}


	public void markDirty(ServerLevel level) {
		this.setDirty();
		level.getDataStorage().save();
	}

	public static FactionSavedData create() {
		return new FactionSavedData();
	}

	public static FactionSavedData getSavedData(ServerLevel level) {
		return level.getDataStorage().computeIfAbsent(
				FactionSavedData::load,
				FactionSavedData::new,
				DATA_NAME
		);
	}

	public Map<String, Faction> getFactions() {
		return this.factions;
	}

	public Faction getFaction(String factionName) {
		return this.factions.get(factionName);
	}


	public void addFaction(Faction faction) {
		this.factions.put(faction.getName(), faction);
	}

	public void addFaction(Faction faction, ServerLevel level) {
		this.factions.put(faction.getName(), faction);
		markDirty(level);
	}

	public void removeFaction(Faction faction, ServerLevel level) {
		this.factions.remove(faction.getName());
		markDirty(level);
	}

	public Faction getFactionFromPlayer(UUID uuid) {
		for (Faction faction : this.factions.values()) {
			if (faction.getMembers().containsKey(uuid)) {
				return faction;
			}
		}
		return null;
	}

	public Faction getFactionByStrippedName(String strippedName) {
		return this.factions.values().stream()
				.filter(f -> f.getName().replaceAll("§[0-9a-fk-or]", "").equalsIgnoreCase(strippedName))
				.findFirst()
				.orElse(null);
	}

	public String getFactionByBeaconPosition(BlockPos pos) {
		return this.factions.values().stream()
				.filter(faction -> faction.hasBeacon() && faction.getBeaconPos().equals(pos))
				.map(Faction::getName)
				.findFirst()
				.orElse(null);
	}

	public void hardcoreFaction(String factionName, ServerLevel level) {
		this.hardcoredFactions.add(factionName);
		markDirty(level);
	}

	@SuppressWarnings("BooleanMethodIsAlwaysInverted")
	public boolean isHardcored(String factionName) {
		return this.hardcoredFactions.contains(factionName);
	}

	public Set<String> getHardcoredFactions() {
		return this.hardcoredFactions;
	}

	public void addAlliance(String factionA, String factionB) {
		getFaction(factionA).addAlly(factionB);
		getFaction(factionB).addAlly(factionA);
		alliedFactions.computeIfAbsent(factionA, k -> new ArrayList<>()).add(factionB);
		alliedFactions.computeIfAbsent(factionB, k -> new ArrayList<>()).add(factionA);
		setDirty();
	}

	public void removeAlliance(String factionA, String factionB) {
		getFaction(factionA).removeAlly(factionB);
		getFaction(factionB).removeAlly(factionA);
		alliedFactions.getOrDefault(factionA, new ArrayList<>()).remove(factionB);
		alliedFactions.getOrDefault(factionB, new ArrayList<>()).remove(factionA);
		setDirty();
	}

	public boolean isAllied(String factionA, String factionB) {
		return alliedFactions.getOrDefault(factionA, new ArrayList<>()).contains(factionB);
	}

	public Map<String, List<String>> getAlliedFactions() {
		return this.alliedFactions;
	}



}
