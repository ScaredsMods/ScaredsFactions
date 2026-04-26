/*
*  Copyright (C) 2025 ScaredRabbitNL
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
package io.github.scaredsmods.scaredsfactionmod.faction;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.*;

public class PersistentData extends SavedData {

	private final Map<String, Faction> factions = new HashMap<>();
	private static final String DATA_NAME = "factions";
	public static PersistentData get(ServerLevel level) {
		return level.getDataStorage().computeIfAbsent(
				PersistentData::load,
				PersistentData::new,
				DATA_NAME
		);
	}

	private final Map<String, BlockPos> beaconPositions = new HashMap<>();
	private final Set<String> hardcoredFactions = new HashSet<>();
	private final Set<UUID> eliminatedPlayers = new HashSet<>();
	@Override
	public CompoundTag save(CompoundTag tag) {
		ListTag factionList = new ListTag();
		for (Faction faction : factions.values()) {
			CompoundTag factionTag = new CompoundTag();
			factionTag.putString("name", faction.getName());
			factionTag.putUUID("owner", faction.getOwner());

			ListTag memberList = new ListTag();
			for (UUID member : faction.getMembers()) {
				CompoundTag memberTag = new CompoundTag();
				memberTag.putUUID("uuid", member);
				memberList.add(memberTag);
			}
			factionTag.put("members", memberList);

			ListTag rankList = new ListTag();
			for (Map.Entry<UUID, FactionRank> entry : faction.getRanks().entrySet()) {
				CompoundTag rankTag = new CompoundTag();
				rankTag.putUUID("uuid", entry.getKey());
				rankTag.putString("rank", entry.getValue().name());
				rankList.add(rankTag);
			}
			factionTag.put("ranks", rankList);

			ListTag allyList = new ListTag();
			for (String ally : faction.getAllies()) {
				CompoundTag allyTag = new CompoundTag();
				allyTag.putString("name", ally);
				allyList.add(allyTag);
			}
			factionTag.put("allies", allyList);

			factionList.add(factionTag);
		}

		ListTag beaconList = new ListTag();
		for (Map.Entry<String, BlockPos> entry : beaconPositions.entrySet()) {
			CompoundTag beaconTag = new CompoundTag();
			beaconTag.putString("faction", entry.getKey());
			beaconTag.putInt("x", entry.getValue().getX());
			beaconTag.putInt("y", entry.getValue().getY());
			beaconTag.putInt("z", entry.getValue().getZ());
			beaconList.add(beaconTag);
		}
		tag.put("beacons", beaconList);

		ListTag hardcoredList = new ListTag();
		for (String faction : hardcoredFactions) {
			CompoundTag hardcoredTag = new CompoundTag();
			hardcoredTag.putString("faction", faction);
			hardcoredList.add(hardcoredTag);
		}

		ListTag eliminatedPlayerList = new ListTag();
		for (UUID uuid : eliminatedPlayers) {
			CompoundTag eliminatedPlayersTag = new CompoundTag();
			eliminatedPlayersTag.putUUID("uuid", uuid);
			eliminatedPlayerList.add(eliminatedPlayersTag);
		}
		tag.put("eliminated", eliminatedPlayerList);
		tag.put("hardcored", hardcoredList);
		tag.put("factions", factionList);
		return tag;
	}

	public static PersistentData load(CompoundTag tag) {
		PersistentData data = new PersistentData();
		ListTag factionList = tag.getList("factions", Tag.TAG_COMPOUND);
		for (int i = 0; i < factionList.size(); i++) {
			CompoundTag factionTag = factionList.getCompound(i);
			String name = factionTag.getString("name");
			UUID owner = factionTag.getUUID("owner");

			Faction faction = new Faction(name, owner);

			ListTag memberList = factionTag.getList("members", Tag.TAG_COMPOUND);
			for (int j = 0; j < memberList.size(); j++) {
				faction.getMembers().add(memberList.getCompound(j).getUUID("uuid"));
			}

			ListTag rankList = factionTag.getList("ranks", Tag.TAG_COMPOUND);
			for (int j = 0; j < rankList.size(); j++) {
				CompoundTag rankTag = rankList.getCompound(j);
				faction.getRanks().put(rankTag.getUUID("uuid"), FactionRank.valueOf(rankTag.getString("rank")));
			}

			ListTag allyList = factionTag.getList("allies", Tag.TAG_COMPOUND);
			for (int j = 0; j < allyList.size(); j++) {
				faction.getAllies().add(allyList.getCompound(j).getString("name"));
			}

			data.addFaction(faction);
		}
		ListTag beaconList = tag.getList("beacons", Tag.TAG_COMPOUND);
		for (int i = 0; i < beaconList.size(); i++) {
			CompoundTag beaconTag = beaconList.getCompound(i);
			data.beaconPositions.put(beaconTag.getString("faction"),
					new BlockPos(beaconTag.getInt("x"), beaconTag.getInt("y"), beaconTag.getInt("z")));
		}

		ListTag hardcoredList = tag.getList("hardcored", Tag.TAG_COMPOUND);
		for (int i = 0; i < hardcoredList.size(); i++) {
			data.hardcoredFactions.add(hardcoredList.getCompound(i).getString("faction"));
		}

		ListTag eliminatedList = tag.getList("eliminated", Tag.TAG_COMPOUND);
		for (int i = 0; i < eliminatedList.size(); i++) {
			data.eliminatedPlayers.add(eliminatedList.getCompound(i).getUUID("uuid"));
		}
		return data;
	}

	public Map<String, Faction> getFactions() {
		return this.factions;
	}

	public Faction getFaction(String name) {
		return this.factions.get(name);
	}

	public void addFaction(Faction faction) {
		factions.put(faction.getName(), faction);
		setDirty();
	}
	public void removeFaction(String name) {
		factions.remove(name);
		setDirty();
	}

	public Faction getFactionByPlayer(UUID uuid) {
		for (Faction faction : factions.values()) {
			if (faction.getMembers().contains(uuid)) {
				return faction;
			}
		}
		return null;
	}

	public void setBeacon(String factionName, BlockPos pos) {
		beaconPositions.put(factionName, pos);
		setDirty();
	}

	public void removeBeacon(String factionName) {
		beaconPositions.remove(factionName);
		setDirty();
	}

	public boolean hasBeacon(String factionName) {
		return beaconPositions.containsKey(factionName);
	}

	public BlockPos getBeaconPos(String factionName) {
		return beaconPositions.get(factionName);
	}

	public String getBeaconFaction(BlockPos pos) {
		for (Map.Entry<String, BlockPos> entry : beaconPositions.entrySet()) {
			if (entry.getValue().equals(pos)) {
				return entry.getKey();
			}
		}
		return null;
	}

	public void addHardcored(String factionName) {
		hardcoredFactions.add(factionName);
		setDirty();
	}

	public boolean isHardcored(String factionName) {
		return hardcoredFactions.contains(factionName);
	}

	public void eliminatePlayer(UUID uuid) {
		eliminatedPlayers.add(uuid);
		setDirty();
	}

	public boolean isEliminated(UUID uuid) {
		return eliminatedPlayers.contains(uuid);
	}
}
