package io.github.scaredsmods.scaredsfactionmod.faction;

import net.minecraft.core.BlockPos;

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
