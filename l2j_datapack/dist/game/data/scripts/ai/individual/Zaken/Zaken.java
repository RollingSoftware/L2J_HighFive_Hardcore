/*
 * Copyright (C) 2004-2016 L2J DataPack
 *
 * This file is part of L2J DataPack.
 *
 * L2J DataPack is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * L2J DataPack is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package ai.individual.Zaken;

import ai.npc.AbstractNpcAI;
import com.l2jserver.Config;
import com.l2jserver.gameserver.instancemanager.GrandBossManager;
import com.l2jserver.gameserver.instancemanager.InstanceManager;
import com.l2jserver.gameserver.instancemanager.ZoneManager;
import com.l2jserver.gameserver.model.L2Party;
import com.l2jserver.gameserver.model.Location;
import com.l2jserver.gameserver.model.PcCondOverride;
import com.l2jserver.gameserver.model.StatsSet;
import com.l2jserver.gameserver.model.actor.L2Attackable;
import com.l2jserver.gameserver.model.actor.L2Npc;
import com.l2jserver.gameserver.model.actor.instance.L2GrandBossInstance;
import com.l2jserver.gameserver.model.actor.instance.L2PcInstance;
import com.l2jserver.gameserver.model.zone.type.L2NoRestartZone;
import com.l2jserver.gameserver.network.NpcStringId;
import com.l2jserver.gameserver.network.SystemMessageId;
import com.l2jserver.gameserver.network.serverpackets.SystemMessage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Cavern Of The Pirate Captain (Day Dream) instance Zone.
 *
 * @author St3eT
 */
public final class Zaken extends AbstractNpcAI {

    protected class ZakenState {

        int zakenRoom;
        AtomicInteger blueFound = new AtomicInteger();
        L2GrandBossInstance zaken;
        List<L2Npc> candles = new ArrayList<>();

        public ZakenState(int zakenRoom) {
            this.zakenRoom = zakenRoom;
        }

        public void blueCandleFound() {
            blueFound.incrementAndGet();
        }

        public void resetCandles() {
            blueFound.set(0);
        }

        public void allCandlesFound() {
            blueFound.set(4);
        }

        public void save() {
            setRoom(zakenRoom);
        }

        public boolean allBlueFound() {
            return blueFound.get() == 4;
        }

    }

    // NPCs
    private static final int PATHFINDER = 32713; // Pathfinder Worker
    private static final int ZAKEN_83 = 29181; // Zaken
    private static final int CANDLE = 32705; // Zaken's Candle
    private static final int DOLL_BLADER_83 = 29182; // Doll Blader
    private static final int VALE_MASTER_83 = 29183; // Veil Master
    private static final int PIRATES_ZOMBIE_83 = 29185; // Pirate Zombie
    private static final int PIRATES_ZOMBIE_CAPTAIN_83 = 29184; // Pirate Zombie Captain

    // Items
    private static final int FIRE = 15280; // Transparent 1HS (for NPC)
    private static final int RED = 15281; // Transparent 1HS (for NPC)
    private static final int BLUE = 15302; // Transparent Bow (for NPC)

    private static final L2NoRestartZone zone = ZoneManager.getInstance().getZoneById(70049, L2NoRestartZone.class);

    // Locations
    private static final Location[] ENTER_LOC = {
        new Location(52684, 219989, -3496),
        new Location(52669, 219120, -3224),
        new Location(52672, 219439, -3312),
    };

    private static final Location[] CANDLE_LOC = {
        // Floor 1
        new Location(53313, 220133, -3498),
        new Location(53313, 218079, -3498),
        new Location(54240, 221045, -3498),
        new Location(54325, 219095, -3498),
        new Location(54240, 217155, -3498),
        new Location(55257, 220028, -3498),
        new Location(55257, 218172, -3498),
        new Location(56280, 221045, -3498),
        new Location(56195, 219095, -3498),
        new Location(56280, 217155, -3498),
        new Location(57215, 220133, -3498),
        new Location(57215, 218079, -3498),

        // Floor 2
        new Location(53313, 220133, -3226),
        new Location(53313, 218079, -3226),
        new Location(54240, 221045, -3226),
        new Location(54325, 219095, -3226),
        new Location(54240, 217155, -3226),
        new Location(55257, 220028, -3226),
        new Location(55257, 218172, -3226),
        new Location(56280, 221045, -3226),
        new Location(56195, 219095, -3226),
        new Location(56280, 217155, -3226),
        new Location(57215, 220133, -3226),
        new Location(57215, 218079, -3226),

        // Floor 3
        new Location(53313, 220133, -2954),
        new Location(53313, 218079, -2954),
        new Location(54240, 221045, -2954),
        new Location(54325, 219095, -2954),
        new Location(54240, 217155, -2954),
        new Location(55257, 220028, -2954),
        new Location(55257, 218172, -2954),
        new Location(56280, 221045, -2954),
        new Location(56195, 219095, -2954),
        new Location(56280, 217155, -2954),
        new Location(57215, 220133, -2954),
        new Location(57215, 218079, -2954),
    };

    private static final int MAX_PEOPLE = 100;

    private static final int ALIVE = 0;
    private static final int FIGHTING = 1;
    private static final int DEAD = 2;

    private static final Ship SHIP = new Ship(Arrays.asList(
        // Floor 1
        new ShipRoom(1, new Location(54240, 220133, -3498), new AdjacentCandles(1, 3, 4, 6)),
        new ShipRoom(2, new Location(54240, 218073, -3498), new AdjacentCandles(2, 4, 5, 7)),
        new ShipRoom(3, new Location(55265, 219095, -3498), new AdjacentCandles(4, 6, 7, 9)),
        new ShipRoom(4, new Location(56289, 220133, -3498), new AdjacentCandles(6, 8, 9, 11)),
        new ShipRoom(5, new Location(56289, 218073, -3498), new AdjacentCandles(7, 9, 10, 12)),

        // Floor 2
        new ShipRoom(6, new Location(54240, 220133, -3226), new AdjacentCandles(13, 15, 16, 18)),
        new ShipRoom(7, new Location(54240, 218073, -3226), new AdjacentCandles(14, 16, 17, 19)),
        new ShipRoom(8, new Location(55265, 219095, -3226), new AdjacentCandles(16, 18, 19, 21)),
        new ShipRoom(9, new Location(56289, 220133, -3226), new AdjacentCandles(18, 20, 21, 23)),
        new ShipRoom(10, new Location(56289, 218073, -3226), new AdjacentCandles(19, 21, 22, 24)),

        // Floor 3
        new ShipRoom(11, new Location(54240, 220133, -2954), new AdjacentCandles(25, 27, 28, 30)),
        new ShipRoom(12, new Location(54240, 218073, -2954), new AdjacentCandles(26, 28, 29, 31)),
        new ShipRoom(13, new Location(55265, 219095, -2954), new AdjacentCandles(28, 30, 31, 33)),
        new ShipRoom(14, new Location(56289, 220133, -2954), new AdjacentCandles(30, 32, 33, 35)),
        new ShipRoom(15, new Location(56289, 218073, -2954), new AdjacentCandles(31, 33, 34, 36))
    ));

    private ZakenState state;

    public Zaken() {
        super(Zaken.class.getSimpleName(), "ai/individual");
        addStartNpc(PATHFINDER);
        addTalkId(PATHFINDER);
        addKillId(ZAKEN_83);
        addFirstTalkId(CANDLE);

        final StatsSet bossInfo = GrandBossManager.getInstance().getStatsSet(ZAKEN_83);
        state = new ZakenState(getRoom());

        switch (getStatus()) {
            case ALIVE: {
                freshSpawnZaken();
                break;
            }
            case FIGHTING: {
                state.allCandlesFound();
                setBlueCandlesBurning();
                spawnZaken(state.zakenRoom);
                spawnZakenGuards(state.zakenRoom);
                spawnCandles(state.zakenRoom);
                restoreZakenState(bossInfo);
                showZaken();
                break;
            }
            case DEAD: {
                final long remain = getRespawn() - System.currentTimeMillis();
                if (remain > 0) {
                    startQuestTimer("RESPAWN", remain, null, null);
                } else {
                    setStatus(ALIVE);
                    freshSpawnZaken();
                }
                break;
            }
        }
    }

    private void freshSpawnZaken() {
        state.resetCandles();
        firstSpawnAllNpcs(state);
    }

    private void restoreZakenState(StatsSet bossInfo) {
        final int curr_hp = bossInfo.getInt("currentHP");
        final int curr_mp = bossInfo.getInt("currentMP");
        final int loc_x = bossInfo.getInt("loc_x");
        final int loc_y = bossInfo.getInt("loc_y");
        final int loc_z = bossInfo.getInt("loc_z");
        final int heading = bossInfo.getInt("heading");

        state.zaken.setCurrentHp(curr_hp);
        state.zaken.setCurrentMp(curr_mp);
        state.zaken.teleToLocation(loc_x, loc_y, loc_z, heading);
    }

    private void teleportPlayerInside(L2PcInstance player) {
        player.teleToLocation(ENTER_LOC[getRandom(ENTER_LOC.length)]);
    }

    @Override
    public String onAdvEvent(String event, L2Npc npc, L2PcInstance player) {
        switch (event) {
            case "enter": {
                if (zone.getPlayersInside().size() >= MAX_PEOPLE) {
                    return "32713-1.html";
                } else {
                    teleportPlayerInside(player);
                }
                break;
            }
            case "RESPAWN": {
                setStatus(ALIVE);
                freshSpawnZaken();
                break;
            }
            case "BURN_BLUE": {
                burnBlueFirstTime(npc, player);
                break;
            }
            case "BURN_BLUE2": {
                burnBlueTwice(npc);
                break;
            }
            case "BURN_RED": {
                burnRedFirstTime(npc, player);
                break;
            }
            case "BURN_RED2": {
                burnRedTwice(npc, player);
                break;
            }
            case "SHOW_ZAKEN": {
                setStatus(FIGHTING);
                showZaken();
                spawnZakenGuardsOn(player);
                break;
            }
        }

        return super.onAdvEvent(event, npc, player);
    }

    private void burnBlueFirstTime(L2Npc npc, L2PcInstance player) {
        if (npc.getRightHandItem() == 0) {
            npc.setRHandId(FIRE);
            startQuestTimer("BURN_BLUE2", 3000, npc, player);
            if (state.allBlueFound()) {
                startQuestTimer("SHOW_ZAKEN", 5000, npc, player);
            }
        }
    }

    private void setBlueCandlesBurning() {
        state.candles.stream().filter(candle -> candle.getVariables().getBoolean("isBlue")).forEach(candle -> candle.setRHandId(BLUE));
    }

    private void burnBlueTwice(L2Npc npc) {
        if (npc.getRightHandItem() == FIRE) {
            npc.setRHandId(BLUE);
        }
    }

    private void burnRedFirstTime(L2Npc npc, L2PcInstance player) {
        if (npc.getRightHandItem() == 0) {
            npc.setRHandId(FIRE);
            startQuestTimer("BURN_RED2", 3000, npc, player);
        }
    }

    private void burnRedTwice(L2Npc npc, L2PcInstance player) {
        if (npc.getRightHandItem() == FIRE) {
            final int room = getRoomByCandle(npc);
            npc.setRHandId(RED);
            manageScreenMsg(NpcStringId.THE_CANDLES_CAN_LEAD_YOU_TO_ZAKEN_DESTROY_HIM);
            spawnNpcAggroedOnPlayer(DOLL_BLADER_83, room, player);
            spawnNpcAggroedOnPlayer(VALE_MASTER_83, room, player);
            spawnNpcAggroedOnPlayer(PIRATES_ZOMBIE_83, room, player);
            spawnNpcAggroedOnPlayer(PIRATES_ZOMBIE_CAPTAIN_83, room, player);
        }
    }

    private void showZaken() {
        manageScreenMsg(NpcStringId.WHO_DARES_AWKAWEN_THE_MIGHTY_ZAKEN);
        state.zaken.setInvisible(false);
        state.zaken.setIsParalyzed(false);
    }

    private void spawnZakenGuards(int roomNumber) {
        spawnNpc(DOLL_BLADER_83, roomNumber);
        spawnNpc(PIRATES_ZOMBIE_83, roomNumber);
        spawnNpc(PIRATES_ZOMBIE_CAPTAIN_83, roomNumber);
    }

    private void spawnZakenGuardsOn(L2PcInstance player) {
        spawnNpcAggroedOnPlayer(DOLL_BLADER_83, state.zakenRoom, player);
        spawnNpcAggroedOnPlayer(PIRATES_ZOMBIE_83, state.zakenRoom, player);
        spawnNpcAggroedOnPlayer(PIRATES_ZOMBIE_CAPTAIN_83, state.zakenRoom, player);
    }

    @Override
    public String onKill(L2Npc npc, L2PcInstance killer, boolean isSummon) {
        long respawnTime =
            (Config.ZAKEN_SPAWN_INTERVAL + getRandom(-Config.ZAKEN_SPAWN_RANDOM, Config.ZAKEN_SPAWN_RANDOM)) * 3600000;
        setRespawn(respawnTime);
        setStatus(DEAD);

        despawnNpcs();
        state.resetCandles();

        return super.onKill(npc, killer, isSummon);
    }

    @Override
    public String onFirstTalk(L2Npc npc, L2PcInstance player) {
        final boolean isBlue = npc.getVariables().getBoolean("isBlue", false);

        if (npc.isScriptValue(0)) {
            if (isBlue) {
                state.blueCandleFound();
                startQuestTimer("BURN_BLUE", 500, npc, player);
            } else {
                startQuestTimer("BURN_RED", 500, npc, player);
            }
            npc.setScriptValue(1);
        }
        return null;
    }

    private void despawnNpcs() {
        state.candles.forEach(L2Npc::deleteMe);
    }

    private int getRoomByCandle(L2Npc npc) {
        final int candleId = npc.getVariables().getInt("candleId", 0);
        List<ShipRoom> rooms = SHIP.findRoomsByCandleId(candleId);
        return rooms.get(getRandom(rooms.size() - 1)).getRoomNumber();
    }

    private void manageScreenMsg(NpcStringId stringId) {
        for (L2PcInstance player : zone.getPlayersInside()) {
            if (player != null) {
                showOnScreenMsg(player, stringId, 2, 6000);
            }
        }
    }

    private L2GrandBossInstance spawnZaken(int roomId) {
        return spawnNpc(ZAKEN_83, roomId);
    }

    private L2GrandBossInstance spawnNpc(int npcId, int roomId) {
        return (L2GrandBossInstance) addSpawn(npcId, SHIP.findRoom(roomId).getRoomCenter());
    }

    private L2Attackable spawnNpcAggroedOnPlayer(int npcId, int roomId, L2PcInstance player) {
        final L2Attackable mob = (L2Attackable) addSpawn(npcId, SHIP.findRoom(roomId).getRoomCenter(), true, 0, false);
        addAttackPlayerDesire(mob, player);
        return mob;
    }

    private void makeCandlesAroundRoomBlue(int roomNumber, List<L2Npc> candles) {
        SHIP.findRoom(roomNumber)
            .getAdjacentCandles()
            .getCandleIds()
            .forEach(candleId -> candles.get(candleId).getVariables().set("isBlue", true));
    }

    private void spawnCandles(int zakenRoomNumber) {
        state.candles.forEach(L2Npc::deleteMe);

        List<L2Npc> candles = new ArrayList<>();
        for (int i = 0; i < 36; i++) {
            final L2Npc candle = addSpawn(CANDLE, CANDLE_LOC[i], false, 0, false);
            candle.getVariables().set("candleId", i + 1);
            candles.add(candle);
        }

        makeCandlesAroundRoomBlue(zakenRoomNumber, candles);

        state.candles = candles;
    }

    private void firstSpawnAllNpcs(ZakenState state) {
        state.zakenRoom = getRandom(1, 15);
        state.save();

        spawnCandles(state.zakenRoom);

        state.zaken = spawnZaken(state.zakenRoom);
        state.zaken.setInvisible(true);
        state.zaken.setIsParalyzed(true);
    }

    private void setRespawn(long respawnTime) {
        GrandBossManager.getInstance().getStatsSet(ZAKEN_83)
            .set("respawn_time", (System.currentTimeMillis() + respawnTime));
    }

    private int getRespawn() {
        return GrandBossManager.getInstance().getStatsSet(ZAKEN_83).getInt("respawn_time");
    }

    private int getStatus() {
        return GrandBossManager.getInstance().getBossStatus(ZAKEN_83);
    }

    private void setStatus(int status) {
        GrandBossManager.getInstance().setBossStatus(ZAKEN_83, status);
    }

    private void setRoom(int roomNumber) {
        GrandBossManager.getInstance().getStatsSet(ZAKEN_83).set("room_number", roomNumber);
    }

    private int getRoom() {
        return GrandBossManager.getInstance().getStatsSet(ZAKEN_83).getInt("room_number");
    }

}
