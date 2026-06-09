package io.github.scaredsmods.scaredsfactions.faction;

import io.github.scaredsmods.scaredsfactions.ScaredsFactionMod;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.saveddata.SavedData;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Stream;

public class Faction {

    private final String name;
    private UUID owner;
    private final Map<UUID, Rank> members = new HashMap<>();
    private final List<String> allies = new ArrayList<>();
    private boolean isInfoVisible;

    public Faction(String name, UUID owner, boolean infoVisible) {
        this.name = name;
        this.owner = owner;
        this.isInfoVisible = infoVisible;
    }

    public String getName() {
        return this.name;
    }

    public UUID getOwner() {
        return this.owner;
    }

    public void setOwner(UUID newOwner) {
        this.owner = newOwner;
    }

    public List<String> getAllies() {
        return this.allies;
    }

    public Map<UUID, Rank> getMembers() {
        return this.members;
    }

    public boolean isInfoVisible() {
        return this.isInfoVisible;
    }

    public void setRank(UUID target, Rank rank) {
        this.members.put(target, rank);
    }

    public void setRankById(UUID target, int id) {
        this.members.put(target, Rank.getRankById(id));
    }

    public static class FactionSavedData extends SavedData {
        private final Map<String, Faction> factions = new HashMap<>();
        public static final ResourceLocation DATA_LOCATION = ScaredsFactionMod.id("data/faction_data");
        public static final String DATA_NAME = "scaredsfactions_faction_data";
        private final Map<UUID, Long> playersOnHomeCooldown =  new HashMap<>();
        private final Map<String, BlockPos> beaconPositions = new HashMap<>();
        private final Set<String> hardcoredFactions = new HashSet<>();
        private final Set<UUID> eliminatedPlayers = new HashSet<>();
        private final Map<String, String> alliedFactions = new HashMap<>();


        public static FactionSavedData load(@NotNull CompoundTag tag) {
            FactionSavedData data = create();
            ListTag factions = tag.getList("factions", Tag.TAG_COMPOUND);
            for (int i = 0; i < factions.size(); i++) {
                CompoundTag factionTag = factions.getCompound(i);
                String name = factionTag.getString("name");
                UUID owner = factionTag.getUUID("owner");
                boolean isInfoVisible = factionTag.getBoolean("isInfoVisible");

                Faction faction = new Faction(name, owner, isInfoVisible);

                ListTag membersTag = factionTag.getList("members", Tag.TAG_COMPOUND);
                for (int j = 0; j < membersTag.size(); j++) {
                    CompoundTag memberTag = membersTag.getCompound(j);
                    UUID uuid = memberTag.getUUID("uuid");
                    CompoundTag rankTag = memberTag.getCompound("rank");
                    Rank rank = Rank.getRankById(rankTag.getInt("rank_id"));
                    faction.members.put(uuid, rank);
                }
                data.addFaction(faction);
            }
            ListTag cooldownsList = tag.getList("players_on_home_cooldown", Tag.TAG_COMPOUND);
            for (int i = 0; i < cooldownsList.size(); i++) {
                CompoundTag cooldownTag = cooldownsList.getCompound(i);
                UUID uuid = cooldownTag.getUUID("uuid");
                long cooldown = cooldownTag.getLong("time_remaining");
                data.playersOnHomeCooldown.put(uuid, cooldown);
            }

            ListTag beaconPositions = tag.getList("beacons", Tag.TAG_COMPOUND);
            for (int i = 0; i < beaconPositions.size(); i++) {
                CompoundTag beaconTag = beaconPositions.getCompound(i);
                String factionName = beaconTag.getString("name");
                int x = beaconTag.getInt("x");
                int y = beaconTag.getInt("y");
                int z = beaconTag.getInt("z");

                data.beaconPositions.put(factionName, new BlockPos(x, y, z));
            }

            ListTag hardcoredFactions = tag.getList("hardcored_factions", Tag.TAG_COMPOUND);
            for (int i = 0; i < hardcoredFactions.size(); i++) {
                CompoundTag hardcoredTag = hardcoredFactions.getCompound(i);
                String factionName = hardcoredTag.getString("name");
                data.hardcoredFactions.add(factionName);
            }

            ListTag eliminatedPlayers = tag.getList("eliminated_players", Tag.TAG_COMPOUND);
            for (int i = 0; i < eliminatedPlayers.size(); i++) {
                CompoundTag eliminatedTag = eliminatedPlayers.getCompound(i);
                UUID uuid = eliminatedTag.getUUID("uuid");
                data.eliminatedPlayers.add(uuid);
            }

            ListTag alliedFaction = tag.getList("allied_factions", Tag.TAG_COMPOUND);
            for (int i = 0; i < alliedFaction.size(); i++) {
                CompoundTag alliedTag = alliedFaction.getCompound(i);
                String factionA = alliedTag.getString("faction_a");
                String factionB = alliedTag.getString("faction_b");
                if (!factionA.isEmpty()) {
                    data.alliedFactions.put(factionA, factionB);
                }
            }

            return data;
        }

        @Override
        public @NotNull CompoundTag save(CompoundTag tag) {
            ListTag factions = new ListTag();
            for (Faction faction : this.factions.values()) {
                CompoundTag factionsTag = new CompoundTag();
                factionsTag.putString("name", faction.getName());
                factionsTag.putUUID("owner", faction.getOwner());
                factionsTag.putBoolean("isInfoVisible", faction.isInfoVisible());

                ListTag members = new ListTag();
                for (Map.Entry<UUID, Rank> entry : faction.getMembers().entrySet()) {
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

            ListTag cooldownList = new ListTag();
            for (Map.Entry<UUID, Long> entry : playersOnHomeCooldown.entrySet()) {
                CompoundTag cooldownTag = new CompoundTag();

                cooldownTag.putUUID("uuid", entry.getKey());
                cooldownTag.putLong("time_remaining", entry.getValue());
                cooldownList.add(cooldownTag);
            }

            ListTag beaconPositions = new ListTag();
            for (Map.Entry<String, BlockPos> entry : this.beaconPositions.entrySet()) {
                CompoundTag beaconTag = new CompoundTag();
                beaconTag.putString("name", entry.getKey());
                beaconTag.putInt("x", entry.getValue().getX());
                beaconTag.putInt("y", entry.getValue().getY());
                beaconTag.putInt("z", entry.getValue().getZ());

                beaconPositions.add(beaconTag);
            }

            ListTag hardcoredFactions = new ListTag();
            for (String faction : this.hardcoredFactions) {
                CompoundTag hardcoredFactionTag = new CompoundTag();
                hardcoredFactionTag.putString("name", faction);
                hardcoredFactions.add(hardcoredFactionTag);
            }

            ListTag eliminatedPlayers = new ListTag();
            for (UUID player : this.eliminatedPlayers) {
                CompoundTag eliminatedPlayerTag = new CompoundTag();
                eliminatedPlayerTag.putUUID("uuid", player);
                eliminatedPlayers.add(eliminatedPlayerTag);
            }

            ListTag alliedFactions = new ListTag();
            for (Map.Entry<String, String> entry : this.alliedFactions.entrySet()) {
                CompoundTag alliedFactionTag = new CompoundTag();
                alliedFactionTag.putString("faction_a", entry.getKey());
                alliedFactionTag.putString("faction_b", entry.getValue());
                alliedFactions.add(alliedFactionTag);
            }

            tag.put("factions", factions);
            tag.put("players_on_home_cooldown", cooldownList);
            tag.put("beacons", beaconPositions);
            tag.put("hardcored_factions", hardcoredFactions);
            tag.put("eliminated_players", eliminatedPlayers);
            tag.put("allied_factions", alliedFactions);
            return tag;
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
            setDirty();
        }

        public void removeFaction(Faction faction) {
            this.factions.remove(faction.getName());
            setDirty();
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

        public BlockPos getBeaconPosition(String name) {
            return this.beaconPositions.get(name);
        }

        public void setBeaconPosition(String name, BlockPos pos) {
            this.beaconPositions.put(name, pos);
        }

        public void removeBeacon(String factionName) {
            this.beaconPositions.remove(factionName);
            setDirty();
        }

        public boolean hasBeacon(String name) {
            return this.beaconPositions.containsKey(name);
        }

        public String getFactionByBeaconPosition(BlockPos pos) {
            for (Map.Entry<String, BlockPos> entry : this.beaconPositions.entrySet()) {
                if (entry.getValue().equals(pos)) {
                    return entry.getKey();
                }
            }
            return null;
        }

        public void hardcoreFaction(String factionName) {
            this.hardcoredFactions.add(factionName);
            setDirty();
        }

        public boolean isHardcored(String factionName) {
            return this.hardcoredFactions.contains(factionName);
        }

        public void eliminatePlayer(UUID target) {
            this.eliminatedPlayers.add(target);
            setDirty();
        }

        public boolean isEliminated(UUID target) {
            return this.eliminatedPlayers.contains(target);
        }

        public void setHomeCooldown(UUID target, long time) {
            this.playersOnHomeCooldown.put(target, time);
            setDirty();
        }

        public long getHomeCooldown(UUID target) {
            return this.playersOnHomeCooldown.get(target);
        }

        public boolean isOnHomeCooldown(UUID target) {
            return this.playersOnHomeCooldown.containsKey(target);
        }

        public boolean isAlliedFaction(String targetFactionName) {
            return this.alliedFactions.containsValue(targetFactionName);
        }

        public Map<String, String> getAlliedFactions() {
            return this.alliedFactions;
        }

        public Map<String, BlockPos> getBeaconPositions() {
            return this.beaconPositions;
        }

        public Map<UUID, Long> getPlayersOnHomeCooldown() {
            return this.playersOnHomeCooldown;
        }

        public Set<String> getHardcoredFactions() {
            return this.hardcoredFactions;
        }

        public Set<UUID> getEliminatedPlayers() {
            return this.eliminatedPlayers;
        }
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
        STADHOUDER("Stadhouder", 10);

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
        public String getSerializedName() {
            return this.getName();
        }

        public Rank[] getPromotableRanks() {
            return Arrays.stream(Rank.values())
                    .filter(r -> r.getId() < this.getId())
                    .toArray(Rank[]::new);
        }

        public Rank[] getDemotableRanks() {
            return Arrays.stream(Rank.values())
                    .filter(r -> r.getId() > this.getId())
                    .toArray(Rank[]::new);
        }

        public static List<Rank> ranksThatCannotPromoteSomeone() {
            return List.of(PRIVATE, CORPORAL);
        }

        public static Rank getRankById(int id) {
            return Arrays.stream(Rank.values())
                    .filter(rank -> rank.getId() == id)
                    .findFirst()
                    .orElseThrow();
        }
    }
}
