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

    protected class ZakenWorld {
        L2GrandBossInstance _zaken;
        int _zakenRoom;
        AtomicInteger _blueFound = new AtomicInteger();
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
    private static final int VORPAL_RING = 15763; // Sealed Vorpal Ring
    private static final int VORPAL_EARRING = 15764; // Sealed Vorpal Earring
    private static final int FIRE = 15280; // Transparent 1HS (for NPC)
    private static final int RED = 15281; // Transparent 1HS (for NPC)
    private static final int BLUE = 15302; // Transparent Bow (for NPC)

    private static final L2NoRestartZone zone = ZoneManager.getInstance()
        .getZoneById(70049, L2NoRestartZone.class);

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

    private static final int ALIVE = 0;
    private static final int FIGHTING = 1;
    private static final int DEAD = 3;

    private static final int MIN_LV_83 = 78;
    private static final int PLAYERS_83_MAX = 27;
    private static final int TEMPLATE_ID_83 = 135;

        /*new Location(53313, 220133, -3498),
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
        new Location(57215, 218079, -3498),*/

    private static final List<ShipRoom> ROOM_DATA = Arrays.asList(
        // Floor 1
        new ShipRoom(1, new Location(54240, 220133, -3498), new AdjacentCandles(1, 3, 4, 6)),
        new ShipRoom(2, new Location(54240, 218073, -3498), new AdjacentCandles(2, 4, 5, 7)),
        new ShipRoom(3, new Location(55265, 219095, -3498), new AdjacentCandles(4, 9, 6, 7)),
        new ShipRoom(4, new Location(56289, 220133, -3498), new AdjacentCandles(8, 11, 6, 9)),
        new ShipRoom(5, new Location(56289, 218073, -3498), new AdjacentCandles(10, 12, 7, 9)),
        // Floor 2
        {54240, 220133, -3226, 13, 15, 16, 18},
        {54240, 218073, -3226, 14, 17, 16, 19},
        {55265, 219095, -3226, 21, 16, 19, 18},
        {56289, 220133, -3226, 20, 23, 21, 18},
        {56289, 218073, -3226, 22, 24, 19, 21},
        // Floor 3
        {54240, 220133, -2954, 25, 27, 28, 30},
        {54240, 218073, -2954, 26, 29, 28, 31},
        {55265, 219095, -2954, 33, 28, 31, 30},
        {56289, 220133, -2954, 32, 35, 30, 33},
        {56289, 218073, -2954, 34, 36, 31, 33}
    );

    private ZakenWorld world;

    public Zaken() {
        super(Zaken.class.getSimpleName(), "ai/individual");
        addStartNpc(PATHFINDER);
        addTalkId(PATHFINDER);
        addKillId(ZAKEN_83);
        addFirstTalkId(CANDLE);

        world = new ZakenWorld();
        final StatsSet bossInfo = GrandBossManager.getInstance().getStatsSet(ZAKEN_83);

        switch (getStatus()) {
            case ALIVE: {
                world._blueFound.set(0);
                spawnAllNpcs(world);
                restoreZakenState(bossInfo);
                break;
            }
            case FIGHTING: {
                world._blueFound.set(4);
                spawnZaken(roomId);
                spawnZakenGuards(roomId);
                restoreZakenState(bossInfo);
                showZaken();
                break;
            }
            case DEAD: {
                final long respawnTime = bossInfo.getLong("respawn_time");
                final long remain = respawnTime - System.currentTimeMillis();
                if (remain > 0) {
                    startQuestTimer("RESPAWN", remain, null, null);
                } else {
                    setStatus(ALIVE);
                    spawnAllNpcs(world);
                }
                break;
            }
        }

    }

    private void restoreZakenState(StatsSet bossInfo) {
        final int curr_hp = bossInfo.getInt("currentHP");
        final int curr_mp = bossInfo.getInt("currentMP");
        final int loc_x = bossInfo.getInt("loc_x");
        final int loc_y = bossInfo.getInt("loc_y");
        final int loc_z = bossInfo.getInt("loc_z");
        final int heading = bossInfo.getInt("heading");
    }

    private void teleportPlayerInside(L2PcInstance player) {
        player.teleToLocation(ENTER_LOC[getRandom(ENTER_LOC.length)]);
    }

    private int getStatus() {
        return GrandBossManager.getInstance().getBossStatus(ZAKEN_83);
    }

    private void setStatus(int status)
    {
        GrandBossManager.getInstance().setBossStatus(ZAKEN_83, status);
    }

    private void setRespawn(long respawnTime)
    {
        GrandBossManager.getInstance().getStatsSet(ZAKEN_83).set("respawn_time", (System.currentTimeMillis() + respawnTime));
    }

    protected boolean checkEntranceConditions(L2PcInstance player) {
        if (player.canOverrideCond(PcCondOverride.INSTANCE_CONDITIONS)) {
            return true;
        }

        if (!player.isInParty()) {
            broadcastSystemMessage(player, null, SystemMessageId.NOT_IN_PARTY_CANT_ENTER, false);
            return false;
        }

        final boolean is83 = templateId == TEMPLATE_ID_83;
        final L2Party party = player.getParty();
        final boolean isInCC = party.isInCommandChannel();
        final List<L2PcInstance> members =
            (isInCC) ? party.getCommandChannel().getMembers() : party.getMembers();
        final boolean isPartyLeader =
            (isInCC) ? party.getCommandChannel().isLeader(player) : party.isLeader(player);

        if (!isPartyLeader) {
            broadcastSystemMessage(player, null, SystemMessageId.ONLY_PARTY_LEADER_CAN_ENTER,
                false);
            return false;
        }

        if ((members.size() < (is83 ? PLAYERS_83_MIN : PLAYERS_60_MIN)) || (members.size() > (is83
            ? PLAYERS_83_MAX : PLAYERS_60_MAX))) {
            broadcastSystemMessage(player, null,
                SystemMessageId.PARTY_EXCEEDED_THE_LIMIT_CANT_ENTER, false);
            return false;
        }

        for (L2PcInstance groupMembers : members) {
            if (groupMembers.getLevel() < (is83 ? MIN_LV_83 : MIN_LV_60)) {
                broadcastSystemMessage(player, groupMembers,
                    SystemMessageId.C1_S_LEVEL_REQUIREMENT_IS_NOT_SUFFICIENT_AND_CANNOT_BE_ENTERED,
                    true);
                return false;
            }

            if (!player.isInsideRadius(groupMembers, 1000, true, true)) {
                broadcastSystemMessage(player, groupMembers,
                    SystemMessageId.C1_IS_IN_A_LOCATION_WHICH_CANNOT_BE_ENTERED_THEREFORE_IT_CANNOT_BE_PROCESSED,
                    true);
                return false;
            }

            final Long reentertime = InstanceManager.getInstance()
                .getInstanceTime(groupMembers.getObjectId(),
                    (is83 ? TEMPLATE_ID_83 : TEMPLATE_ID_60));
            if (System.currentTimeMillis() < reentertime) {
                broadcastSystemMessage(player, groupMembers,
                    SystemMessageId.C1_MAY_NOT_RE_ENTER_YET, true);
                return false;
            }
        }
        return true;
    }

    private void broadcastSystemMessage(L2PcInstance player, L2PcInstance member,
        SystemMessageId msgId, boolean toGroup) {
        final SystemMessage sm = SystemMessage.getSystemMessage(msgId);

        if (toGroup) {
            sm.addPcName(member);

            if (player.getParty().isInCommandChannel()) {
                player.getParty().getCommandChannel().broadcastPacket(sm);
            } else {
                player.getParty().broadcastPacket(sm);
            }
        } else {
            player.broadcastPacket(sm);
        }
    }

    @Override
    public String onAdvEvent(String event, L2Npc npc, L2PcInstance player) {
        switch (event) {
            case "enter": {
                if (checkEntranceConditions(player)) {
                    teleportPlayerInside(player);
                }
                break;
            }
            case "BURN_BLUE": {
                burnBlueFirstTime(npc, player);
                break;
            }
            case "BURN_BLUE2": {
                burnBlueTwice(npc, player);
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
            if (world._blueFound.get() == 4) {
                startQuestTimer("SHOW_ZAKEN", 5000, npc, player);
            }
        }
    }

    private void burnBlueTwice(L2Npc npc, L2PcInstance player) {
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
        world._zaken.setInvisible(false);
        world._zaken.setIsParalyzed(false);
    }

    private void spawnZakenGuards() {
        spawnNpc(DOLL_BLADER_83, world._zakenRoom);
        spawnNpc(PIRATES_ZOMBIE_83, world._zakenRoom);
        spawnNpc(PIRATES_ZOMBIE_CAPTAIN_83, world._zakenRoom);
    }

    private void spawnZakenGuardsOn(L2PcInstance player) {
        spawnNpcAggroedOnPlayer(DOLL_BLADER_83, world._zakenRoom, player);
        spawnNpcAggroedOnPlayer(PIRATES_ZOMBIE_83, world._zakenRoom, player);
        spawnNpcAggroedOnPlayer(PIRATES_ZOMBIE_CAPTAIN_83, world._zakenRoom, player);
    }

    @Override
    public String onKill(L2Npc npc, L2PcInstance killer, boolean isSummon) {
        //TODO Grandboss Timer
        world._blueFound.set(0);
        return super.onKill(npc, killer, isSummon);
    }

    @Override
    public String onFirstTalk(L2Npc npc, L2PcInstance player) {
        final boolean isBlue = npc.getVariables().getInt("isBlue", 0) == 1;

        if (npc.isScriptValue(0)) {
            if (isBlue) {
                world._blueFound.incrementAndGet();
                startQuestTimer("BURN_BLUE", 500, npc, player);
            } else {
                startQuestTimer("BURN_RED", 500, npc, player);
            }
            npc.setScriptValue(1);
        }
        return null;
    }

    private int getRoomByCandle(L2Npc npc) {
        final int candleId = npc.getVariables().getInt("candleId", 0);

        for (int i = 0; i < 15; i++) {
            if ((ROOM_DATA[i][3] == candleId) || (ROOM_DATA[i][4] == candleId)) {
                return i + 1;
            }
        }

        if ((candleId == 6) || (candleId == 7)) {
            return 3;
        } else if ((candleId == 18) || (candleId == 19)) {
            return 8;
        } else if ((candleId == 30) || (candleId == 31)) {
            return 13;
        }
        return 0;
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
        return (L2GrandBossInstance) addSpawn(npcId, ROOM_DATA[roomId - 1][0], ROOM_DATA[roomId - 1][1],
            ROOM_DATA[roomId - 1][2], 0, false, 0, false);
    }

    private L2Attackable spawnNpcAggroedOnPlayer(int npcId, int roomId, L2PcInstance player) {
        final L2Attackable mob = (L2Attackable) addSpawn(npcId,
            ROOM_DATA[roomId - 1][0] + getRandom(350),
            ROOM_DATA[roomId - 1][1] + getRandom(350), ROOM_DATA[roomId - 1][2],
            0, false, 0, false);
        addAttackPlayerDesire(mob, player);
        return mob;
    }

    private void spawnAllNpcs(ZakenWorld world) {
        final List<L2Npc> candles = new ArrayList<>();
        world._zakenRoom = getRandom(1, 15);

        for (int i = 0; i < 36; i++) {
            final L2Npc candle = addSpawn(CANDLE, CANDLE_LOC[i], false, 0, false);
            candle.getVariables().set("candleId", i + 1);
            candles.add(candle);
        }

        for (int i = 3; i < 7; i++) {
            candles.get(ROOM_DATA[world._zakenRoom - 1][i] - 1).getVariables().set("isBlue", 1);
        }
        world._zaken = spawnZaken(world._zakenRoom);
        world._zaken.setInvisible(true);
        world._zaken.setIsParalyzed(true);
    }

}
