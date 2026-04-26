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


import java.util.*;

public class Faction {

	private final String name;
	private UUID owner;
	private final List<UUID> members = new ArrayList<>();
	private final Map<UUID, FactionRank> ranks = new HashMap<>();
	private final List<String> allies = new ArrayList<>();

	public Faction(String name, UUID owner) {
		this.name = name;
		this.owner = owner;
	}

	public Faction(String name) {
		this.name = name;
		this.owner = null;
	}

	public List<UUID> getMembers() {
		return this.members;
	}

	public String getName() {
		return this.name;
	}

	public Map<UUID, FactionRank> getRanks() {
		return this.ranks;
	}

	public List<String> getAllies() {
		return this.allies;
	}

	public UUID getOwner() {
		return this.owner;
	}

	public void setOwner(UUID owner) {
		this.owner = owner;
		setOwnerRank(owner);
	}

	public void setPlayerRank(UUID uuid, FactionRank rank) {
		this.ranks.put(uuid, rank);
	}

	private void setOwnerRank(UUID uuid) {
		this.ranks.put(uuid, FactionRank.GENERAL);
	}



}
