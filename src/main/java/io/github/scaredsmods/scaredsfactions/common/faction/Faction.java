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

import io.github.scaredsmods.scaredsfactions.api.common.faction.setting.BooleanFactionSetting;
import io.github.scaredsmods.scaredsfactions.common.ModConfigs;
import io.github.scaredsmods.scaredsfactions.api.common.faction.setting.AbstractFactionSetting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
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

	public void setOwner(UUID newOwner, Rank ownerRank) {
		this.owner = newOwner;
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

	public void setEliminatedPlayers(Set<UUID> eliminatedPlayers) {
		this.eliminatedPlayers.clear();
		this.eliminatedPlayers.addAll(eliminatedPlayers);
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
	public void setPlayersOnHomeCooldown(Map<UUID, Long> playersOnHomeCooldown) {
		this.playersOnHomeCooldown.clear();
		this.playersOnHomeCooldown.putAll(playersOnHomeCooldown);
	}

	public List<String> getAllies() {
		return this.allies;
	}

	public void setAllies(List<String> newAllies) {
		this.allies.clear();
		this.allies.addAll(newAllies);
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

	public <V, T extends AbstractFactionSetting<V, T>> T getSetting(String nbtId, Class<T> settingClass) {
		return this.settings.stream()
				.filter(s -> s.getNbtId().equals(nbtId) && settingClass.isInstance(s))
				.map(settingClass::cast)
				.findFirst()
				.orElse(null);
	}

	public <V, T extends AbstractFactionSetting<V, T>> T getSettingByModId(String modId, Class<T> settingClass) {
		return this.settings.stream()
				.filter(s -> s.getModId().equals(modId) && settingClass.isInstance(s))
				.map(settingClass::cast)
				.findFirst()
				.orElse(null);
	}

	public <V, T extends AbstractFactionSetting<V, T>> V getSettingValue(String nbtId, Class<T> settingClass) {
		T setting = getSetting(nbtId, settingClass);
		return setting != null ? setting.get() : null;
	}

	public <V, T extends AbstractFactionSetting<V, T>> V getSettingValueByModId(String modId, Class<T> settingClass) {
		T setting = getSettingByModId(modId, settingClass);
		return setting != null ? setting.get() : null;
	}

	public boolean getBooleanSettingValue(String nbtId, boolean defaultValue) {
		BooleanFactionSetting setting = getSetting(nbtId, BooleanFactionSetting.class);
		return setting != null ? setting.get() : defaultValue;
	}

	public void write(FriendlyByteBuf buf) {
		buf.writeUtf(this.name);
		buf.writeUUID(this.owner);

		CompoundTag settingsTag = new CompoundTag();
		ListTag listSettings = new ListTag();
		for (AbstractFactionSetting<?, ?> setting : this.getSettings()) {
			CompoundTag settings = new CompoundTag();
			setting.save(settings);
			listSettings.add(settings);
		}
		settingsTag.put("settings", listSettings);
		buf.writeNbt(settingsTag);

		buf.writeBoolean(this.beaconPos != null);
		if (this.beaconPos != null) buf.writeBlockPos(this.beaconPos);

		buf.writeMap(this.members, FriendlyByteBuf::writeUUID, FriendlyByteBuf::writeEnum);
		buf.writeCollection(this.allies, FriendlyByteBuf::writeUtf);

		buf.writeBoolean(this.pendingTransfer != null);
		if (this.pendingTransfer != null) buf.writeUUID(this.pendingTransfer);

		buf.writeCollection(this.eliminatedPlayers, FriendlyByteBuf::writeUUID);
		buf.writeMap(this.playersOnHomeCooldown, FriendlyByteBuf::writeUUID, FriendlyByteBuf::writeLong);
	}

	public static Faction read(FriendlyByteBuf buf) {
		String name = buf.readUtf();
		UUID owner = buf.readUUID();
		CompoundTag settingsTag = buf.readNbt();
		assert settingsTag != null;
		ListTag factionSettings = settingsTag.getList("settings", Tag.TAG_COMPOUND);
		List<AbstractFactionSetting<?, ?>> settings = Faction.createDefaultSettings();
		for (int j = 0; j < factionSettings.size(); j++) {
			CompoundTag settingTag = factionSettings.getCompound(j);
			for (int k = 0; k < settings.size(); k++) {
				AbstractFactionSetting<?, ?> setting = settings.get(k);
				if (settingTag.contains(setting.getNbtId())) {
					settings.set(k, setting.load(settingTag));
				}
			}
		}

		Faction faction = new Faction(name, owner, settings);

		boolean hasBeaconPos = buf.readBoolean();
		if (hasBeaconPos) {
			BlockPos beaconPos = buf.readBlockPos();
			faction.setBeaconPos(beaconPos);
		}

		faction.getMembers().clear();
		Map<UUID, Rank> members = buf.readMap(FriendlyByteBuf::readUUID, fBuf -> fBuf.readEnum(Rank.class));
		faction.getMembers().putAll(members);

		List<String> allies = buf.readList(FriendlyByteBuf::readUtf);
		faction.getAllies().clear();
		faction.setAllies(allies);

		boolean pendingTransfer = buf.readBoolean();
		if (pendingTransfer) {
			faction.setPendingTransfer(buf.readUUID());
		}

		Set<UUID> eliminatedPlayers = new HashSet<>(buf.readList(FriendlyByteBuf::readUUID));
		faction.setEliminatedPlayers(eliminatedPlayers);

		Map<UUID, Long> homeCooldowns = buf.readMap(FriendlyByteBuf::readUUID, FriendlyByteBuf::readLong);
		faction.setPlayersOnHomeCooldown(homeCooldowns);
		return faction;
	}
	@Override
	public boolean equals(Object obj) {
		if (obj == null) return false;
		if (this == obj) return true;
		if (this.getClass() != obj.getClass()) return false;
		Faction other = (Faction) obj;
		return Objects.equals(this.name, other.name);
	}


	@Override
	public int hashCode() {
		return Objects.hashCode(this.name);
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
