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

import io.github.scaredsmods.scaredsfactions.common.ModConfigs;
import io.github.scaredsmods.scaredsfactions.api.common.faction.setting.AbstractFactionSetting;
import io.github.scaredsmods.scaredsfactions.api.common.faction.setting.BooleanFactionSetting;
import net.minecraft.core.BlockPos;
import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;

public class Faction {

	private String name;
	private UUID owner;
	private BlockPos beaconPos;
	private final Map<UUID, Rank> members = new HashMap<>();
	private final List<String> allies = new ArrayList<>();
	private final List<AbstractFactionSetting<?, ?>> settings;
	private UUID pendingTransfer = null;
	private final Set<UUID> eliminatedPlayers = new HashSet<>();
	private final Map<UUID, Long> playersOnHomeCooldown = new HashMap<>();

	public Faction(String name, UUID owner, List<AbstractFactionSetting<?, ?>> settings) {
		this.name = name;
		this.owner = owner;
		this.settings = settings;
	}

	public String getName() {
		return this.name;
	}

	public List<AbstractFactionSetting<?, ?>> getSettings() {
		return settings;
	}

	public UUID getOwner() {
		return this.owner;
	}

	public void setName(String newName) {
		this.name = newName;
	}

	public void setOwner(UUID newOwner) {
		this.owner = newOwner;
		Faction.Rank ownerRank = switch (ModConfigs.commonConfig.defaultOwnerRank.get()) {
			case PREFER_STADHOUDER -> Faction.Rank.STADHOUDER;
			case PREFER_GENERALISSIMUS -> Faction.Rank.GENERALISSIMUS;
		};
		this.members.put(newOwner, ownerRank);
	}

	public Map<UUID, Rank> getMembers() {
		return this.members;
	}

	public void setRank(UUID target, Rank rank) {
		this.members.put(target, rank);
	}

	public void setRankById(UUID target, int id) {
		this.members.put(target, Rank.getRankById(id));
	}

	public BlockPos getBeaconPos() {
		return this.beaconPos;
	}

	public void setBeaconPos(BlockPos beaconPos) {
		this.beaconPos = beaconPos;
	}

	public boolean hasBeacon() {
		return this.beaconPos != null;
	}

	public void removeBeacon() {
		this.beaconPos = null;
	}

	public UUID getPendingTransfer() { return pendingTransfer; }
	public void setPendingTransfer(UUID uuid) { this.pendingTransfer = uuid; }

	public Set<UUID> getEliminatedPlayers() {
		return this.eliminatedPlayers;
	}

	public void eliminatePlayer(UUID uuid) {
		this.eliminatedPlayers.add(uuid);
	}

	public boolean isEliminated(UUID uuid) {
		return this.eliminatedPlayers.contains(uuid);
	}

	public void setHomeCooldown(UUID target, long time) {
		this.playersOnHomeCooldown.put(target, time);
	}

	public long getHomeCooldown(UUID target) {
		return this.playersOnHomeCooldown.getOrDefault(target, 0L);
	}

	public boolean isOnHomeCooldown(UUID target) {
		return this.playersOnHomeCooldown.containsKey(target);
	}

	public void removeHomeCooldown(UUID target) {
		this.playersOnHomeCooldown.remove(target);
	}

	public Map<UUID, Long> getPlayersOnHomeCooldown() {
		return this.playersOnHomeCooldown;
	}
	public List<String> getAllies() {
		return this.allies;
	}

	public void addAlly(String factionName) {
		if(!this.isAlliedWith(factionName)) {
			this.allies.add(factionName);
		}
	}

	public void removeAlly(String factionName) {
		this.allies.remove(factionName);
	}

	public boolean isAlliedWith(String factionName) {
		return this.allies.contains(factionName);
	}

	public static List<AbstractFactionSetting<?, ?>> createDefaultSettings() {
		return FactionSettings.settings.stream()
				.map(setting -> (AbstractFactionSetting<?, ?>) setting.copy())
				.collect(Collectors.toList());
	}

	public AbstractFactionSetting<?, ?> getSetting(String nbtId) {
		return this.settings.stream()
				.filter(s -> s.getNbtId().equals(nbtId))
				.findFirst()
				.orElse(null);
	}

	public AbstractFactionSetting<?, ?> getSettingByModId(String modId) {
		return this.settings.stream()
				.filter(setting -> setting.getModId().equals(modId))
				.findFirst()
				.orElse(null);
	}

	public boolean getBooleanSetting(String nbtId) {
		AbstractFactionSetting<?, ?> setting = getSetting(nbtId);
		if (setting instanceof BooleanFactionSetting boolSetting) {
			return boolSetting.get();
		}
		return false;
	}

	public boolean getBooleanSettingByModId(String modId) {
		AbstractFactionSetting<?, ?> setting = getSettingByModId(modId);
		if (setting instanceof BooleanFactionSetting bool) {
			return bool.get();
		}
		return false;
	}


	public enum Rank implements StringRepresentable {
		// These ranks have largely been inspired by the US Army ranks found on <a href="https://www.army.mil/ranks/">
		PRIVATE("Private", 0),
		CORPORAL("Corporal",1),
		STAFF_SERGEANT("Staff Sergeant", 2),
		SERGEANT("Sergeant",3 ),
		LIEUTENANT("Lieutenant", 4),
		CAPTAIN("Captain", 5),
		MAJOR("Major", 6),
		COLONEL("Colonel", 7),
		GENERAL("General", 8),
		FIELD_MARSHAL("Field Marshal", 9),
		GENERALISSIMUS("Generalissimus",10),
		STADHOUDER("Stadhouder", 10); // The army commander of the 16th century Netherlands

		private final String name;
		private final int id;

		Rank(String name, int id) {
			this.name = name;
			this.id = id;
		}

		public String getName() {
			return this.name;
		}

		public int getId() {
			return this.id;
		}

		@Override
		public @NotNull String getSerializedName() {
			return this.getName();
		}

		public Rank[] getManageableRanks() {
			return Arrays.stream(Rank.values())
					.filter(r -> r.getId() < this.getId())
					.toArray(Rank[]::new);
		}

		public static Rank getRankById(int id) {
			return Arrays.stream(Rank.values())
					.filter(rank -> rank.getId() == id)
					.findFirst()
					.orElseThrow();
		}

		public static Rank getRankByName(String name) {
			return Arrays.stream(Rank.values())
					.filter(rank -> rank.getName().equalsIgnoreCase(name))
					.findFirst()
					.orElseThrow();
		}
	}
}
