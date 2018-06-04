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
package ai.individual.Freya;

import ai.npc.AbstractNpcAI;
import com.l2jserver.Config;
import com.l2jserver.gameserver.ai.CtrlIntention;
import com.l2jserver.gameserver.data.xml.impl.NpcData;
import com.l2jserver.gameserver.enums.MountType;
import com.l2jserver.gameserver.instancemanager.GrandBossManager;
import com.l2jserver.gameserver.instancemanager.ZoneManager;
import com.l2jserver.gameserver.model.Location;
import com.l2jserver.gameserver.model.PcCondOverride;
import com.l2jserver.gameserver.model.StatsSet;
import com.l2jserver.gameserver.model.actor.L2Attackable;
import com.l2jserver.gameserver.model.actor.L2Character;
import com.l2jserver.gameserver.model.actor.L2Npc;
import com.l2jserver.gameserver.model.actor.instance.L2GrandBossInstance;
import com.l2jserver.gameserver.model.actor.instance.L2NpcInstance;
import com.l2jserver.gameserver.model.actor.instance.L2PcInstance;
import com.l2jserver.gameserver.model.actor.instance.L2QuestGuardInstance;
import com.l2jserver.gameserver.model.actor.instance.L2RaidBossInstance;
import com.l2jserver.gameserver.model.holders.SkillHolder;
import com.l2jserver.gameserver.model.quest.QuestState;
import com.l2jserver.gameserver.model.quest.State;
import com.l2jserver.gameserver.model.skills.Skill;
import com.l2jserver.gameserver.model.variables.NpcVariables;
import com.l2jserver.gameserver.model.zone.type.L2NoRestartZone;
import com.l2jserver.gameserver.network.NpcStringId;
import com.l2jserver.gameserver.network.clientpackets.Say2;
import com.l2jserver.gameserver.network.serverpackets.ActionFailed;
import com.l2jserver.gameserver.network.serverpackets.ExChangeClientEffectInfo;
import com.l2jserver.gameserver.network.serverpackets.ExSendUIEvent;
import com.l2jserver.gameserver.taskmanager.DecayTaskManager;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import quests.Q10286_ReunionWithSirra.Q10286_ReunionWithSirra;

/**
 * Ice Queen's Castle (Normal Battle) instance zone.
 *
 * @author St3eT
 */
public final class Freya extends AbstractNpcAI {

    protected class FreyaState {

        protected List<L2Npc> knightStatues = new ArrayList<>();
        protected List<L2Attackable> spawnedMobs = new CopyOnWriteArrayList<>();
        protected L2NpcInstance controller = null;
        protected L2GrandBossInstance freya = null;
        protected L2QuestGuardInstance supp_Jinia = null;
        protected L2QuestGuardInstance supp_Kegor = null;
        protected boolean isSupportActive = false;
        protected boolean canSpawnMobs = true;
        private L2NpcInstance sirra;
        private long lastAttackTime = 0;

        int getInstanceId() {
            return 0;
        }

        void setStatus(int status) {
            GrandBossManager.getInstance().setBossStatus(FREYA_STAND, status);
        }

        int getStatus() {
            return GrandBossManager.getInstance().getBossStatus(FREYA_STAND);
        }

        boolean isStatus(int status) {
            return getStatus() == status;
        }

        boolean isStatusNot(int status) {
            return getStatus() != status;
        }

        private void setRespawn(long respawnTime) {
            StatsSet statsSet = GrandBossManager.getInstance().getStatsSet(FREYA_STAND);
            statsSet.set("respawn_time", (System.currentTimeMillis() + respawnTime));
            GrandBossManager.getInstance().setStatsSet(FREYA_STAND, statsSet);
        }

        private int getRespawn() {
            return GrandBossManager.getInstance().getStatsSet(FREYA_STAND).getInt("respawn_time");
        }

        L2NpcInstance spawnSirra() {
            if (sirra == null) {
                sirra = (L2NpcInstance) addSpawn(SIRRA, SIRRA_LOCATION, false, 0, true,
                    state.getInstanceId());
            }
            return sirra;
        }

        public void updateLastAttackTime() {
            lastAttackTime = System.currentTimeMillis();
        }

        public long getLastAttackTime() {
            return lastAttackTime;
        }
    }

    private static final L2NoRestartZone zone = ZoneManager.getInstance().getZoneById(90573, L2NoRestartZone.class);

    // Npcs
    private static final int FREYA_THRONE = 29177; // First freya
    private static final int FREYA_SPELLING = 29178; // Second freya
    private static final int FREYA_STAND = 29179; // Last freya
    private static final int INVISIBLE_NPC = 18919;
    private static final int KNIGHT = 18855; // Archery Knight
    private static final int GLACIER = 18853; // Glacier
    private static final int BREATH = 18854; // Archer's Breath
    private static final int GLAKIAS = 25699; // Glakias (Archery Knight Captain)
    private static final int SIRRA = 32762; // Sirra
    private static final int JINIA = 32781; // Jinia
    private static final int SUPP_JINIA = 18850; // Jinia
    private static final int SUPP_KEGOR = 18851; // Kegor

    // Skills
    private static final SkillHolder BLIZZARD = new SkillHolder(6274, 1); // Eternal Blizzard
    private static final SkillHolder BLIZZARD_BREATH = new SkillHolder(6299, 1); // Breath of Ice Palace - Ice Storm
    private static final SkillHolder SUICIDE_BREATH = new SkillHolder(6300, 1); // Self-Destruction
    private static final SkillHolder JINIA_SUPPORT = new SkillHolder(6288, 1); // Jinia's Prayer
    private static final SkillHolder KEGOR_SUPPORT = new SkillHolder(6289, 1); // Kegor's Courage
    private static final SkillHolder ICE_STONE = new SkillHolder(6301, 1); // Cold Mana's Fragment
    private static final SkillHolder CANCEL = new SkillHolder(4618, 1); // NPC Cancel PC Target
    private static final SkillHolder POWER_STRIKE = new SkillHolder(6293, 1); // Power Strike
    private static final SkillHolder POINT_TARGET = new SkillHolder(6295, 1); // Point Target
    private static final SkillHolder CYLINDER_THROW = new SkillHolder(6297, 1); // Cylinder Throw
    private static final SkillHolder SelfRangeBuff = new SkillHolder(6294, 1); // Leader's Roar
    private static final SkillHolder LEADER_RUSH = new SkillHolder(6296, 1); // Rush
    private static final SkillHolder ANTI_STRIDER = new SkillHolder(4258, 1); // Hinder Strider
    private static final SkillHolder ICE_BALL = new SkillHolder(6278, 1); // Ice Ball
    private static final SkillHolder SUMMON_ELEMENTAL = new SkillHolder(6277, 1); // Summon Spirits
    private static final SkillHolder SELF_NOVA = new SkillHolder(6279, 1); // Attack Nearby Range
    private static final SkillHolder REFLECT_MAGIC = new SkillHolder(6282, 1); // Reflect Magic

    // Locations
    private static final Location FREYA_SPAWN = new Location(114720, -117085, -11088, 15956);
    private static final Location FREYA_SPELLING_SPAWN = new Location(114723, -117502, -10672, 15956);
    private static final Location FREYA_CORPSE = new Location(114767, -114795, -11200, 0);
    private static final Location MIDDLE_POINT = new Location(114730, -114805, -11200);
    private static final Location KEGOR_FINISH = new Location(114659, -114796, -11205);
    private static final Location GLAKIAS_SPAWN = new Location(114707, -114799, -11199, 15956);
    private static final Location SUPP_JINIA_SPAWN = new Location(114751, -114781, -11205);
    private static final Location SUPP_KEGOR_SPAWN = new Location(114659, -114796, -11205);
    private static final Location BATTLE_PORT = new Location(114694, -113700, -11200);
    private static final Location CONTROLLER_LOC = new Location(114394, -112383, -11200);
    private static final Location SIRRA_LOCATION = new Location(114766, -113141, -11200);

    private static final Location[] ENTER_LOC = {
        new Location(114185, -112435, -11210),
        new Location(114183, -112280, -11210),
        new Location(114024, -112435, -11210),
        new Location(114024, -112278, -11210),
        new Location(113865, -112435, -11210),
        new Location(113865, -112276, -11210),

    };

    private static final Location[] STATUES_LOC = {
        new Location(113845, -116091, -11168, 8264),
        new Location(113381, -115622, -11168, 8264),
        new Location(113380, -113978, -11168, -8224),
        new Location(113845, -113518, -11168, -8224),
        new Location(115591, -113516, -11168, -24504),
        new Location(116053, -113981, -11168, -24504),
        new Location(116061, -115611, -11168, 24804),
        new Location(115597, -116080, -11168, 24804),
        new Location(112942, -115480, -10960, 52),
        new Location(112940, -115146, -10960, 52),
        new Location(112945, -114453, -10960, 52),
        new Location(112945, -114123, -10960, 52),
        new Location(116497, -114117, -10960, 32724),
        new Location(116499, -114454, -10960, 32724),
        new Location(116501, -115145, -10960, 32724),
        new Location(116502, -115473, -10960, 32724),
    };

    private static Location[] KNIGHTS_LOC = {
        new Location(114502, -115315, -11205, 15451),
        new Location(114937, -115323, -11205, 18106),
        new Location(114722, -115185, -11205, 16437),
    };

    private static final int DOOR_ID = 23140101;

    // Statuses
    private static final int ALIVE = 0;
    private static final int NPC_BATTLE = 1;
    private static final int GLAKIAS_BATTLE = 2;
    private static final int KNIGHT_SLAUGHTER = 3;
    private static final int FREYA_FIGHT = 4;
    private static final int DEAD = 5;

    private static final long CHECK_ACTIVITY_DELAY = 60000;
    //TODO RESTORE private static final long CHECK_ACTIVITY_THRESHOLD = 60000 * 5;
    private static final long CHECK_ACTIVITY_THRESHOLD = 60000 * 1;

    private final FreyaState state;

    private final L2GrandBossInstance fakeFreyaInstance = new L2GrandBossInstance(NpcData.getInstance().getTemplate(FREYA_STAND));

    private Freya() {
        super(Freya.class.getSimpleName(), "ai/individual");
        addStartNpc(SIRRA, SUPP_KEGOR, SUPP_JINIA);
        addFirstTalkId(SUPP_KEGOR, SUPP_JINIA);
        addTalkId(SIRRA, JINIA, SUPP_KEGOR);
        addAttackId(FREYA_THRONE, FREYA_STAND, GLAKIAS, GLACIER, BREATH, KNIGHT);
        addKillId(GLAKIAS, FREYA_STAND, KNIGHT, GLACIER, BREATH);
        addSpawnId(GLAKIAS, FREYA_STAND, KNIGHT, GLACIER, BREATH);
        addSpellFinishedId(GLACIER, BREATH);

        //TODO REMOVE
        Config.FREYA_CONTEST_TIME = 1;

        state = new FreyaState();
        GrandBossManager.getInstance().addBoss(fakeFreyaInstance);

        if (state.isStatusNot(DEAD)) {
            state.spawnSirra();
        } else {
            final long remain = state.getRespawn() - System.currentTimeMillis();
            if (remain > 0) {
                startQuestTimer("RESPAWN", remain, null, null);
            } else {
                state.setStatus(ALIVE);
                state.spawnSirra();
            }
        }
    }

    private void resetState() {
        closeDoor(DOOR_ID, state.getInstanceId());
        state.sirra.setScriptValue(0);
        manageDespawnMinions();
        state.freya.decayMe();
        state.supp_Jinia.decayMe();
        state.supp_Kegor.decayMe();
        state.isSupportActive = false;
        state.canSpawnMobs = true;
        state.setStatus(ALIVE);
        teleportPlayersOut();
    }

    private void teleportPlayersOut() {

    }

    @Override
    public String onAdvEvent(String event, L2Npc npc, L2PcInstance player) {
        switch (event) {
            case "enter": {
                managePlayerEnter(player);
                break;
            }
            case "openDoor": {
                if (npc.isScriptValue(0)) {
                    npc.setScriptValue(1);
                    openDoor(DOOR_ID, state.getInstanceId());
                    state.controller = (L2NpcInstance) addSpawn(INVISIBLE_NPC, CONTROLLER_LOC, false, 0, true,
                        state.getInstanceId());
                    for (Location loc : STATUES_LOC) {
                        if (loc.getZ() == -11168) {
                            final L2Npc statue = addSpawn(INVISIBLE_NPC, loc, false, 0, false,
                                state.getInstanceId());
                            state.knightStatues.add(statue);
                        }
                    }
                    for (L2PcInstance players : zone.getPlayersInside()) {
                        if (players != null && !players.isDead()) {
                            final QuestState qs = player
                                .getQuestState(Q10286_ReunionWithSirra.class.getSimpleName());
                            if ((qs != null) && (qs.getState() == State.STARTED) && qs.isCond(5)) {
                                qs.setCond(6, true);
                            }
                        }
                    }

                    manageTimer(Config.FREYA_CONTEST_TIME * 60);
                    startQuestTimer("STAGE_1_MOVIE", Config.FREYA_CONTEST_TIME * 60000, state.controller, null);
                }
                break;
            }
            case "portInside": {
                teleportPlayer(player, BATTLE_PORT, state.getInstanceId());
                break;
            }
            case "killFreya": {
                final QuestState qs = player.getQuestState(Q10286_ReunionWithSirra.class.getSimpleName());
                if ((qs != null) && (qs.getState() == State.STARTED) && qs.isCond(6)) {
                    qs.setMemoState(10);
                    qs.setCond(7, true);
                }
                state.supp_Kegor.deleteMe();
                state.freya.decayMe();
                manageMovie(20);
                cancelQuestTimer("FINISH_WORLD", state.controller, null);
                startQuestTimer("FINISH_WORLD", 58500, state.controller, null);
                break;
            }
            case "18851-01.html": {
                return event;
            }
            case "STAGE_1_MOVIE": {
                closeDoor(DOOR_ID, state.getInstanceId());
                state.setStatus(NPC_BATTLE);
                manageMovie(15);
                startQuestTimer("STAGE_1_START", 53500, state.controller, null);
                break;
            }
            case "STAGE_1_START": {
                state.freya = (L2GrandBossInstance) addSpawn(FREYA_THRONE, FREYA_SPAWN, false, 0, true,
                    state.getInstanceId());
                state.freya.setIsMortal(false);
                manageScreenMsg(NpcStringId.BEGIN_STAGE_1);
                startQuestTimer("CAST_BLIZZARD", 50000, state.controller, null);
                startQuestTimer("STAGE_1_SPAWN", 2000, state.freya, null);
                startQuestTimer("CHECK_ACTIVITY", CHECK_ACTIVITY_DELAY, state.controller, null);
                break;
            }
            case "STAGE_1_SPAWN": {
                notifyEvent("START_SPAWN", state.controller, null);
                break;
            }
            case "STAGE_1_FINISH": {
                state.freya.deleteMe();
                state.freya = null;
                manageDespawnMinions();
                manageMovie(16);
                startQuestTimer("STAGE_1_PAUSE", 24100 - 1000, state.controller, null);
                break;
            }
            case "STAGE_1_PAUSE": {
                state.freya = (L2GrandBossInstance) addSpawn(FREYA_SPELLING, FREYA_SPELLING_SPAWN, false, 0,
                    true, state.getInstanceId());
                state.freya.setIsInvul(true);
                state.freya.disableCoreAI(true);
                manageTimer(60);
                state.setStatus(GLAKIAS_BATTLE);
                startQuestTimer("STAGE_2_START", 60000, state.controller, null);
                break;
            }
            case "STAGE_2_START": {
                state.canSpawnMobs = true;
                notifyEvent("START_SPAWN", state.controller, null);
                manageScreenMsg(NpcStringId.BEGIN_STAGE_2);
                break;
            }
            case "STAGE_2_MOVIE": {
                manageMovie(23);
                startQuestTimer("STAGE_2_GLAKIAS", 7000, state.controller, null);
                break;
            }
            case "STAGE_2_GLAKIAS": {
                for (Location loc : STATUES_LOC) {
                    if (loc.getZ() == -10960) {
                        final L2Npc statue = addSpawn(INVISIBLE_NPC, loc, false, 0, false,
                            state.getInstanceId());
                        state.knightStatues.add(statue);
                        startQuestTimer("SPAWN_KNIGHT", 5000, statue, null);
                    }
                }
                final L2RaidBossInstance glakias = (L2RaidBossInstance) addSpawn(GLAKIAS, GLAKIAS_SPAWN, false,
                    0, true, state.getInstanceId());
                state.spawnedMobs.add(glakias);
                startQuestTimer("LEADER_DELAY", 5000, glakias, null);
                break;
            }
            case "STAGE_3_MOVIE": {
                manageMovie(17);
                startQuestTimer("STAGE_3_START", 21500, state.controller, null);
                break;
            }
            case "STAGE_3_START": {
                for (L2PcInstance players : zone.getPlayersInside()) {
                    if (players != null) {
                        players.broadcastPacket(ExChangeClientEffectInfo.STATIC_FREYA_DESTROYED);
                    }
                }
                state.setStatus(FREYA_FIGHT);
                state.freya.deleteMe();
                state.canSpawnMobs = true;
                state.freya = (L2GrandBossInstance) addSpawn(FREYA_STAND, FREYA_SPAWN, false, 0, true,
                    state.getInstanceId());
                state.controller.getVariables().set("FREYA_MOVE", 0);
                notifyEvent("START_SPAWN", state.controller, null);
                startQuestTimer("START_MOVE", 10000, state.controller, null);
                startQuestTimer("CAST_BLIZZARD", 50000, state.controller, null);
                manageScreenMsg(NpcStringId.BEGIN_STAGE_3);
                break;
            }
            case "START_MOVE": {
                if (npc.getVariables().getInt("FREYA_MOVE") == 0) {
                    state.controller.getVariables().set("FREYA_MOVE", 1);
                    state.freya.setIsRunning(true);
                    state.freya.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, MIDDLE_POINT);
                }
                break;
            }
            case "CAST_BLIZZARD": {
                if (!state.freya.isInvul()) {
                    state.freya.doCast(BLIZZARD.getSkill());
                    manageScreenMsg(NpcStringId.STRONG_MAGIC_POWER_CAN_BE_FELT_FROM_SOMEWHERE);
                }

                for (L2Attackable minion : state.spawnedMobs) {
                    if ((minion != null) && !minion.isDead() && !minion.isInCombat()) {
                        manageRandomAttack(minion);
                    }
                }
                startQuestTimer("CAST_BLIZZARD", getRandom(55, 60) * 1000, state.controller, null);
                break;
            }
            case "SPAWN_SUPPORT": {
                for (L2PcInstance players : zone.getPlayersInside()) {
                    if ((players != null) && (players.getInstanceId() == state.getInstanceId())) {
                        players.setIsInvul(false);
                    }
                }
                state.freya.setIsInvul(false);
                state.freya.disableCoreAI(false);
                manageScreenMsg(NpcStringId.BEGIN_STAGE_4);
                state.supp_Jinia = (L2QuestGuardInstance) addSpawn(SUPP_JINIA, SUPP_JINIA_SPAWN, false, 0, true,
                    state.getInstanceId());
                state.supp_Jinia.setIsRunning(true);
                state.supp_Jinia.setIsInvul(true);
                state.supp_Jinia.setCanReturnToSpawnPoint(false);
                state.supp_Kegor = (L2QuestGuardInstance) addSpawn(SUPP_KEGOR, SUPP_KEGOR_SPAWN, false, 0, true,
                    state.getInstanceId());
                state.supp_Kegor.setIsRunning(true);
                state.supp_Kegor.setIsInvul(true);
                state.supp_Kegor.setCanReturnToSpawnPoint(false);
                startQuestTimer("ATTACK_FREYA", 5000, state.supp_Jinia, null);
                startQuestTimer("ATTACK_FREYA", 5000, state.supp_Kegor, null);
                startQuestTimer("GIVE_SUPPORT", 1000, state.controller, null);
                break;
            }
            case "GIVE_SUPPORT": {
                if (state.isSupportActive) {
                    state.supp_Jinia.doCast(JINIA_SUPPORT.getSkill());
                    state.supp_Kegor.doCast(KEGOR_SUPPORT.getSkill());
                    startQuestTimer("GIVE_SUPPORT", 25000, state.controller, null);
                }
                break;
            }
            case "FINISH_STAGE": {
                state.supp_Jinia.deleteMe();
                state.supp_Jinia = null;
                state.freya.teleToLocation(FREYA_CORPSE);
                state.supp_Kegor.teleToLocation(KEGOR_FINISH);
                break;
            }
            case "START_SPAWN": {
                for (L2Npc statues : state.knightStatues) {
                    notifyEvent("SPAWN_KNIGHT", statues, null);
                }

                for (Location loc : KNIGHTS_LOC) {
                    final L2Attackable knight = (L2Attackable) addSpawn(KNIGHT, loc, false, 0, false,
                        state.getInstanceId());
                    knight.disableCoreAI(true);
                    knight.setDisplayEffect(1);
                    knight.getSpawn().setLocation(loc);
                    state.spawnedMobs.add(knight);
                    startQuestTimer("ICE_RUPTURE", getRandom(2, 5) * 1000, knight, null);
                }

                for (int i = 0; i < state.getStatus(); i++) {
                    notifyEvent("SPAWN_GLACIER", state.controller, null);
                }
                break;
            }
            case "SPAWN_KNIGHT": {
                if (state.canSpawnMobs) {
                    final Location loc = new Location(MIDDLE_POINT.getX() + getRandom(-1000, 1000),
                        MIDDLE_POINT.getY() + getRandom(-1000, 1000), MIDDLE_POINT.getZ());
                    final L2Attackable knight = (L2Attackable) addSpawn(KNIGHT, npc.getLocation(), false, 0,
                        false, state.getInstanceId());
                    knight.getVariables().set("SPAWNED_NPC", npc);
                    knight.disableCoreAI(true);
                    knight.setIsImmobilized(true);
                    knight.setDisplayEffect(1);
                    knight.getSpawn().setLocation(loc);
                    state.spawnedMobs.add(knight);
                    startQuestTimer("ICE_RUPTURE", getRandom(5, 10) * 1000, knight, null);
                }
                break;
            }
            case "SPAWN_GLACIER": {
                if (state.canSpawnMobs) {
                    final Location loc = new Location(MIDDLE_POINT.getX() + getRandom(-1000, 1000),
                        MIDDLE_POINT.getY() + getRandom(-1000, 1000), MIDDLE_POINT.getZ());
                    final L2Attackable glacier = (L2Attackable) addSpawn(GLACIER, loc, false, 0, false,
                        state.getInstanceId());
                    glacier.setDisplayEffect(1);
                    glacier.disableCoreAI(true);
                    glacier.setIsImmobilized(true);
                    state.spawnedMobs.add(glacier);
                    startQuestTimer("CHANGE_STATE", 1400, glacier, null);
                }
                break;
            }
            case "ICE_RUPTURE": {
                if (npc.isCoreAIDisabled()) {
                    npc.disableCoreAI(false);
                    npc.setIsImmobilized(false);
                    npc.setDisplayEffect(2);
                    manageRandomAttack((L2Attackable) npc);
                }
                break;
            }
            case "FIND_TARGET": {
                manageRandomAttack((L2Attackable) npc);
                break;
            }
            case "CHANGE_STATE": {
                npc.setDisplayEffect(2);
                startQuestTimer("CAST_SKILL", 20000, npc, null);
                break;
            }
            case "CAST_SKILL": {
                if (npc.isScriptValue(0) && !npc.isDead()) {
                    npc.setTarget(npc);
                    npc.doCast(ICE_STONE.getSkill());
                    npc.setScriptValue(1);
                }
                break;
            }
            case "SUICIDE": {
                npc.setDisplayEffect(3);
                npc.setIsMortal(true);
                npc.doDie(null);
                break;
            }
            case "BLIZZARD": {
                npc.getVariables().set("SUICIDE_COUNT", npc.getVariables().getInt("SUICIDE_COUNT") + 1);

                if (npc.getVariables().getInt("SUICIDE_ON") == 0) {
                    if (npc.getVariables().getInt("SUICIDE_COUNT") == 2) {
                        startQuestTimer("ELEMENTAL_SUICIDE", 20000, npc, null);
                    } else {
                        if (npc.checkDoCastConditions(BLIZZARD_BREATH.getSkill()) && !npc.isCastingNow()) {
                            npc.setTarget(npc);
                            npc.doCast(BLIZZARD_BREATH.getSkill());
                        }
                        startQuestTimer("BLIZZARD", 20000, npc, null);
                    }
                }
                break;
            }
            case "ELEMENTAL_SUICIDE": {
                npc.setTarget(npc);
                npc.doCast(SUICIDE_BREATH.getSkill());
                break;
            }
            case "ELEMENTAL_KILLED": {
                if (npc.getVariables().getInt("SUICIDE_ON") == 1) {
                    npc.setTarget(npc);
                    npc.doCast(SUICIDE_BREATH.getSkill());
                }
                break;
            }
            case "ATTACK_FREYA": {
                final SkillHolder skill = npc.getTemplate().getParameters()
                    .getObject("Skill01_ID", SkillHolder.class);
                if (npc.isInsideRadius(state.freya, 100, true, false)) {
                    if (npc.checkDoCastConditions(skill.getSkill()) && !npc.isCastingNow()) {
                        npc.setTarget(state.freya);
                        npc.doCast(skill.getSkill());
                        startQuestTimer("ATTACK_FREYA", 20000, npc, null);
                    } else {
                        startQuestTimer("ATTACK_FREYA", 5000, npc, null);
                    }
                } else {
                    npc.getAI().setIntention(CtrlIntention.AI_INTENTION_FOLLOW, state.freya);
                    startQuestTimer("ATTACK_FREYA", 5000, npc, null);
                }
                break;
            }
            case "FINISH_WORLD": {
                if (state.freya != null) {
                    state.freya.decayMe();
                }

                for (L2PcInstance players : zone.getPlayersInside()) {
                    if ((players != null)) {
                        players.broadcastPacket(ExChangeClientEffectInfo.STATIC_FREYA_DEFAULT);
                    }
                }

                long respawnDelay = getRespawnDelay();
                state.setStatus(DEAD);
                state.setRespawn(respawnDelay);
                startQuestTimer("RESPAWN", respawnDelay, state.controller, null);
                break;
            }
            case "LEADER_RANGEBUFF": {
                if (npc.checkDoCastConditions(SelfRangeBuff.getSkill()) && !npc.isCastingNow()) {
                    npc.setTarget(npc);
                    npc.doCast(SelfRangeBuff.getSkill());
                } else {
                    startQuestTimer("LEADER_RANGEBUFF", 30000, npc, null);
                }
                break;
            }
            case "LEADER_RANDOMIZE": {
                final L2Attackable mob = (L2Attackable) npc;
                mob.clearAggroList();

                for (L2Character characters : npc.getKnownList().getKnownPlayersInRadius(1000)) {
                    if ((characters != null)) {
                        mob.addDamageHate(characters, 0, getRandom(10000, 20000));
                    }
                }
                startQuestTimer("LEADER_RANDOMIZE", 25000, npc, null);
                break;
            }
            case "LEADER_DASH": {
                final L2Character mostHated = ((L2Attackable) npc).getMostHated();
                if (getRandomBoolean() && !npc.isCastingNow() && (mostHated != null) && !mostHated.isDead() && (
                    npc.calculateDistance(mostHated, true, false) < 1000)) {
                    npc.setTarget(mostHated);
                    npc.doCast(LEADER_RUSH.getSkill());
                }
                startQuestTimer("LEADER_DASH", 10000, npc, null);
                break;
            }
            case "LEADER_DESTROY": {
                final L2Attackable mob = (L2Attackable) npc;
                if (npc.getVariables().getInt("OFF_SHOUT") == 0) {
                    manageScreenMsg(NpcStringId.THE_SPACE_FEELS_LIKE_ITS_GRADUALLY_STARTING_TO_SHAKE);

                    switch (getRandom(4)) {
                        case 0: {
                            broadcastNpcSay(npc, Say2.SHOUT,
                                NpcStringId.ARCHER_GIVE_YOUR_BREATH_FOR_THE_INTRUDER);
                            break;
                        }
                        case 1: {
                            broadcastNpcSay(npc, Say2.SHOUT, NpcStringId.MY_KNIGHTS_SHOW_YOUR_LOYALTY);
                            break;
                        }
                        case 2: {
                            broadcastNpcSay(npc, Say2.SHOUT, NpcStringId.I_CAN_TAKE_IT_NO_LONGER);
                            break;
                        }
                        case 3: {
                            broadcastNpcSay(npc, Say2.SHOUT, NpcStringId.ARCHER_HEED_MY_CALL);
                            for (int i = 0; i < 3; i++) {
                                final L2Attackable breath = (L2Attackable) addSpawn(BREATH, npc.getLocation(),
                                    true, 0, false, state.getInstanceId());
                                breath.setIsRunning(true);
                                breath.addDamageHate(mob.getMostHated(), 0, 999);
                                breath.getAI()
                                    .setIntention(CtrlIntention.AI_INTENTION_ATTACK, mob.getMostHated());
                                startQuestTimer("BLIZZARD", 20000, breath, null);
                                state.spawnedMobs.add(breath);
                            }
                            break;
                        }
                    }
                }
                break;
            }
            case "LEADER_DELAY": {
                if (npc.getVariables().getInt("DELAY_VAL") == 0) {
                    npc.getVariables().set("DELAY_VAL", 1);
                }
                break;
            }
            case "RESPAWN": {
                state.setStatus(ALIVE);
                state.spawnSirra();
                break;
            }
            case "CHECK_ACTIVITY": {
                if (System.currentTimeMillis() - state.getLastAttackTime() > CHECK_ACTIVITY_THRESHOLD) {
                    cancelQuestTimer("CHECK_ACTIVITY", state.controller, null);
                    resetState();
                } else {
                    startQuestTimer("CHECK_ACTIVITY", CHECK_ACTIVITY_DELAY, state.controller, null);
                }
            }
        }

        return super.onAdvEvent(event, npc, player);
    }

    @Override
    public String onSpawn(L2Npc npc) {
        ((L2Attackable) npc).setOnKillDelay(0);
        return super.onSpawn(npc);
    }

    @Override
    public String onFirstTalk(L2Npc npc, L2PcInstance player) {
        if (npc.getId() == SUPP_JINIA) {
            player.sendPacket(ActionFailed.STATIC_PACKET);
            return null;
        } else if (npc.getId() == SUPP_KEGOR) {
            if (state.isSupportActive) {
                player.sendPacket(ActionFailed.STATIC_PACKET);
                return null;
            }
            return "18851.html";
        }

        player.sendPacket(ActionFailed.STATIC_PACKET);
        return null;
    }

    @Override
    public String onAttack(L2Npc npc, L2PcInstance attacker, int damage, boolean isSummon, Skill skill) {
        state.updateLastAttackTime();
        switch (npc.getId()) {
            case FREYA_THRONE: {
                if ((state.controller.getVariables().getInt("FREYA_MOVE") == 0) && state.isStatus(NPC_BATTLE)) {
                    state.controller.getVariables().set("FREYA_MOVE", 1);
                    manageScreenMsg(NpcStringId.FREYA_HAS_STARTED_TO_MOVE);
                    state.freya.setIsRunning(true);
                    state.freya.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, MIDDLE_POINT);
                }

                if (npc.getCurrentHp() < (npc.getMaxHp() * 0.02)) {
                    notifyEvent("STAGE_1_FINISH", state.controller, null);
                    cancelQuestTimer("CAST_BLIZZARD", state.controller, null);
                } else {
                    if ((attacker.getMountType() == MountType.STRIDER) && !attacker
                        .isAffectedBySkill(ANTI_STRIDER.getSkillId()) && !npc.isCastingNow()) {
                        if (!npc.isSkillDisabled(ANTI_STRIDER.getSkill())) {
                            npc.setTarget(attacker);
                            npc.doCast(ANTI_STRIDER.getSkill());
                        }
                    }

                    final L2Character mostHated = ((L2Attackable) npc).getMostHated();
                    final boolean canReachMostHated =
                        (mostHated != null) && !mostHated.isDead() && (npc.calculateDistance(mostHated, true, false)
                            <= 800);

                    if (getRandom(10000) < 3333) {
                        if (getRandomBoolean()) {
                            if ((npc.calculateDistance(attacker, true, false) <= 800) && npc
                                .checkDoCastConditions(ICE_BALL.getSkill()) && !npc.isCastingNow()) {
                                npc.setTarget(attacker);
                                npc.doCast(ICE_BALL.getSkill());
                            }
                        } else {
                            if (canReachMostHated && npc.checkDoCastConditions(ICE_BALL.getSkill()) && !npc
                                .isCastingNow()) {
                                npc.setTarget(mostHated);
                                npc.doCast(ICE_BALL.getSkill());
                            }
                        }
                    } else if (getRandom(10000) < 800) {
                        if (getRandomBoolean()) {
                            if ((npc.calculateDistance(attacker, true, false) <= 800) && npc
                                .checkDoCastConditions(SUMMON_ELEMENTAL.getSkill()) && !npc.isCastingNow()) {
                                npc.setTarget(attacker);
                                npc.doCast(SUMMON_ELEMENTAL.getSkill());
                            }
                        } else {
                            if (canReachMostHated && npc.checkDoCastConditions(SUMMON_ELEMENTAL.getSkill()) && !npc
                                .isCastingNow()) {
                                npc.setTarget(mostHated);
                                npc.doCast(SUMMON_ELEMENTAL.getSkill());
                            }
                        }
                    } else if (getRandom(10000) < 1500) {
                        if (!npc.isAffectedBySkill(SELF_NOVA.getSkillId()) && npc
                            .checkDoCastConditions(SELF_NOVA.getSkill()) && !npc.isCastingNow()) {
                            npc.setTarget(npc);
                            npc.doCast(SELF_NOVA.getSkill());
                        }
                    }
                }
                break;
            }
            case FREYA_STAND: {
                if (state.controller.getVariables().getInt("FREYA_MOVE") == 0) {
                    state.controller.getVariables().set("FREYA_MOVE", 1);
                    state.freya.setIsRunning(true);
                    state.freya.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, MIDDLE_POINT);
                }

                if ((npc.getCurrentHp() < (npc.getMaxHp() * 0.2)) && !state.isSupportActive) {
                    state.isSupportActive = true;
                    state.freya.setIsInvul(true);
                    state.freya.disableCoreAI(true);
                    for (L2PcInstance players : zone.getPlayersInside()) {
                        players.setIsInvul(true);
                        players.abortAttack();
                    }
                    manageMovie(18);
                    startQuestTimer("SPAWN_SUPPORT", 27000, state.controller, null);
                }

                if ((attacker.getMountType() == MountType.STRIDER) && !attacker
                    .isAffectedBySkill(ANTI_STRIDER.getSkillId()) && !npc.isCastingNow()) {
                    if (!npc.isSkillDisabled(ANTI_STRIDER.getSkill())) {
                        npc.setTarget(attacker);
                        npc.doCast(ANTI_STRIDER.getSkill());
                    }
                }

                final L2Character mostHated = ((L2Attackable) npc).getMostHated();
                final boolean canReachMostHated =
                    (mostHated != null) && !mostHated.isDead() && (npc.calculateDistance(mostHated, true, false)
                        <= 800);

                if (getRandom(10000) < 3333) {
                    if (getRandomBoolean()) {
                        if ((npc.calculateDistance(attacker, true, false) <= 800) && npc
                            .checkDoCastConditions(ICE_BALL.getSkill()) && !npc.isCastingNow()) {
                            npc.setTarget(attacker);
                            npc.doCast(ICE_BALL.getSkill());
                        }
                    } else {
                        if (canReachMostHated && npc.checkDoCastConditions(ICE_BALL.getSkill()) && !npc
                            .isCastingNow()) {
                            npc.setTarget(mostHated);
                            npc.doCast(ICE_BALL.getSkill());
                        }
                    }
                } else if (getRandom(10000) < 1333) {
                    if (getRandomBoolean()) {
                        if ((npc.calculateDistance(attacker, true, false) <= 800) && npc
                            .checkDoCastConditions(SUMMON_ELEMENTAL.getSkill()) && !npc.isCastingNow()) {
                            npc.setTarget(attacker);
                            npc.doCast(SUMMON_ELEMENTAL.getSkill());
                        }
                    } else {
                        if (canReachMostHated && npc.checkDoCastConditions(SUMMON_ELEMENTAL.getSkill()) && !npc
                            .isCastingNow()) {
                            npc.setTarget(mostHated);
                            npc.doCast(SUMMON_ELEMENTAL.getSkill());
                        }
                    }
                } else if (getRandom(10000) < 1500) {
                    if (!npc.isAffectedBySkill(SELF_NOVA.getSkillId()) && npc
                        .checkDoCastConditions(SELF_NOVA.getSkill()) && !npc.isCastingNow()) {
                        npc.setTarget(npc);
                        npc.doCast(SELF_NOVA.getSkill());
                    }
                } else if (getRandom(10000) < 1333) {
                    if (!npc.isAffectedBySkill(REFLECT_MAGIC.getSkillId()) && npc
                        .checkDoCastConditions(REFLECT_MAGIC.getSkill()) && !npc.isCastingNow()) {
                        npc.setTarget(npc);
                        npc.doCast(REFLECT_MAGIC.getSkill());
                    }
                }
                break;
            }
            case GLACIER: {
                if (npc.isScriptValue(0) && (npc.getCurrentHp() < (npc.getMaxHp() * 0.5))) {
                    npc.setTarget(attacker);
                    npc.doCast(ICE_STONE.getSkill());
                    npc.setScriptValue(1);
                }
                break;
            }
            case BREATH: {
                if ((npc.getCurrentHp() < (npc.getMaxHp() / 20)) && (npc.getVariables().getInt("SUICIDE_ON", 0)
                    == 0)) {
                    npc.getVariables().set("SUICIDE_ON", 1);
                    startQuestTimer("ELEMENTAL_KILLED", 1000, npc, null);
                }
                break;
            }
            case KNIGHT: {
                if (npc.isCoreAIDisabled()) {
                    manageRandomAttack((L2Attackable) npc);
                    npc.disableCoreAI(false);
                    npc.setIsImmobilized(false);
                    npc.setDisplayEffect(2);
                    cancelQuestTimer("ICE_RUPTURE", npc, null);
                }
                break;
            }
            case GLAKIAS: {
                if (npc.getCurrentHp() < (npc.getMaxHp() * 0.02)) {
                    if (npc.getVariables().getInt("OFF_SHOUT") == 0) {
                        npc.getVariables().set("OFF_SHOUT", 1);
                        npc.getVariables().set("DELAY_VAL", 2);
                        npc.setTarget(attacker);
                        npc.doCast(CANCEL.getSkill());
                    } else if (npc.getVariables().getInt("OFF_SHOUT") == 1) {
                        npc.setTarget(attacker);
                        npc.doCast(CANCEL.getSkill());
                    }
                } else if ((npc.getVariables().getInt("OFF_SHOUT") == 0) && (npc.getVariables().getInt("DELAY_VAL")
                    == 1)) {
                    final L2Character mostHated = ((L2Attackable) npc).getMostHated();
                    final boolean canReachMostHated =
                        (mostHated != null) && !mostHated.isDead() && (npc.calculateDistance(mostHated, true, false)
                            < 1000);

                    if (npc.getVariables().getInt("TIMER_ON") == 0) {
                        npc.getVariables().set("TIMER_ON", 1);
                        startQuestTimer("LEADER_RANGEBUFF", getRandom(5, 30) * 1000, npc, null);
                        startQuestTimer("LEADER_RANDOMIZE", 25000, npc, null);
                        startQuestTimer("LEADER_DASH", 5000, npc, null);
                        startQuestTimer("LEADER_DESTROY", 60000, npc, null);
                    }

                    if (getRandom(10000) < 2500) {
                        if (getRandom(10000) < 2500) {
                            if (npc.checkDoCastConditions(POWER_STRIKE.getSkill()) && !npc.isCastingNow()) {
                                npc.setTarget(attacker);
                                npc.doCast(POWER_STRIKE.getSkill());
                            }
                        } else if (npc.checkDoCastConditions(POWER_STRIKE.getSkill()) && !npc.isCastingNow()
                            && canReachMostHated) {
                            npc.setTarget(((L2Attackable) npc).getMostHated());
                            npc.doCast(POWER_STRIKE.getSkill());
                        }
                    } else if (getRandom(10000) < 1500) {
                        if (getRandomBoolean()) {
                            if (npc.checkDoCastConditions(POINT_TARGET.getSkill()) && !npc.isCastingNow()) {
                                npc.setTarget(attacker);
                                npc.doCast(POINT_TARGET.getSkill());
                            }
                        } else if (npc.checkDoCastConditions(POINT_TARGET.getSkill()) && !npc.isCastingNow()
                            && canReachMostHated) {
                            npc.setTarget(((L2Attackable) npc).getMostHated());
                            npc.doCast(POINT_TARGET.getSkill());
                        }
                    } else if (getRandom(10000) < 1500) {
                        if (getRandomBoolean()) {
                            if (npc.checkDoCastConditions(CYLINDER_THROW.getSkill()) && !npc.isCastingNow()) {
                                npc.setTarget(attacker);
                                npc.doCast(CYLINDER_THROW.getSkill());
                            }
                        } else if (npc.checkDoCastConditions(CYLINDER_THROW.getSkill()) && !npc.isCastingNow()
                            && canReachMostHated) {
                            npc.setTarget(((L2Attackable) npc).getMostHated());
                            npc.doCast(CYLINDER_THROW.getSkill());
                        }
                    }
                }
                break;
            }
        }
        return super.onAttack(npc, attacker, damage, isSummon, skill);
    }

    @Override
    public String onSpellFinished(L2Npc npc, L2PcInstance player, Skill skill) {
        switch (npc.getId()) {
            case GLACIER: {
                if (skill == ICE_STONE.getSkill()) {
                    if (getRandom(100) < 75) {
                        final L2Attackable breath = (L2Attackable) addSpawn(BREATH, npc.getLocation(), false, 0,
                            false, 0);
                        if (player != null) {
                            breath.setIsRunning(true);
                            breath.addDamageHate(player, 0, 999);
                            breath.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, player);
                        } else {
                            manageRandomAttack(breath);
                        }
                        state.spawnedMobs.add(breath);
                        startQuestTimer("BLIZZARD", 20000, breath, null);
                    }
                    notifyEvent("SUICIDE", npc, null);
                }
                break;
            }
            case BREATH: {
                if (skill == SUICIDE_BREATH.getSkill()) {
                    npc.doDie(null);
                }
                break;
            }
        }
        return super.onSpellFinished(npc, player, skill);
    }

    private long getRespawnDelay() {
        return 60000;
        //TODO restore (Config.FREYA_SPAWN_INTERVAL + getRandom(-Config.FREYA_SPAWN_RANDOM, Config.FREYA_SPAWN_RANDOM)) * 3600000;
    }

    @Override
    public String onKill(L2Npc npc, L2PcInstance killer, boolean isSummon) {
        switch (npc.getId()) {
            case GLAKIAS: {
                manageDespawnMinions();
                manageTimer(60);
                startQuestTimer("STAGE_3_MOVIE", 60000, state.controller, null);
                break;
            }
            case FREYA_STAND: {
                state.isSupportActive = false;
                manageMovie(19);
                manageDespawnMinions();
                DecayTaskManager.getInstance().cancel(state.freya);
                cancelQuestTimer("ATTACK_FREYA", state.supp_Jinia, null);
                cancelQuestTimer("ATTACK_FREYA", state.supp_Kegor, null);
                cancelQuestTimer("GIVE_SUPPORT", state.controller, null);
                cancelQuestTimer("CAST_BLIZZARD", state.controller, null);
                cancelQuestTimer("CHECK_ACTIVITY", state.controller, null);
                startQuestTimer("FINISH_STAGE", 16000, state.controller, null);
                startQuestTimer("FINISH_WORLD", 300000, state.controller, null);
                break;
            }
            case KNIGHT: {
                final L2Npc spawnedBy = npc.getVariables().getObject("SPAWNED_NPC", L2Npc.class);
                final NpcVariables var = state.controller.getVariables();
                int knightCount = var.getInt("KNIGHT_COUNT");

                if ((var.getInt("FREYA_MOVE") == 0) && state.isStatus(NPC_BATTLE)) {
                    var.set("FREYA_MOVE", 1);
                    manageScreenMsg(NpcStringId.FREYA_HAS_STARTED_TO_MOVE);
                    state.freya.setIsRunning(true);
                    state.freya.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, MIDDLE_POINT);
                }

                if ((knightCount < 10) && (state.isStatus(GLAKIAS_BATTLE))) {
                    knightCount++;
                    var.set("KNIGHT_COUNT", knightCount);

                    if (knightCount == 10) {
                        notifyEvent("STAGE_2_MOVIE", state.controller, null);
                        state.setStatus(KNIGHT_SLAUGHTER);
                    }
                }

                if (spawnedBy != null) {
                    startQuestTimer("SPAWN_KNIGHT", getRandom(30, 60) * 1000, spawnedBy, null);
                }
                state.spawnedMobs.remove(npc);
                break;
            }
            case GLACIER: {
                startQuestTimer("SPAWN_GLACIER", getRandom(30, 60) * 1000, state.controller, null);
                state.spawnedMobs.remove(npc);
                break;
            }
            case BREATH: {
                state.spawnedMobs.remove(npc);
                break;
            }
        }
        return super.onKill(npc, killer, isSummon);
    }

    private void managePlayerEnter(L2PcInstance player) {
        if (checkConditions(player)) {
            teleportPlayer(player, ENTER_LOC[getRandom(ENTER_LOC.length)], 0, false);
        } else {
            _log.warning("Player " + player + " cannot enter Freya's Castle");
        }
    }

    private boolean checkConditions(L2PcInstance player) {
        if (player.isGM() && player.canOverrideCond(PcCondOverride.INSTANCE_CONDITIONS)) {
            return true;
        }

        return state.isStatus(ALIVE);
    }

    private void manageRandomAttack(L2Attackable mob) {
        final List<L2PcInstance> players = new ArrayList<>();
        for (L2PcInstance player : zone.getPlayersInside()) {
            if ((player != null) && !player.isDead() && !player.isInvisible()) {
                players.add(player);
            }
        }

        Collections.shuffle(players);
        final L2PcInstance target = (!players.isEmpty()) ? players.get(0) : null;
        if (target != null) {
            mob.addDamageHate(target, 0, 999);
            mob.setIsRunning(true);
            mob.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, target);
        } else {
            startQuestTimer("FIND_TARGET", 10000, mob, null);
        }
    }

    private void manageDespawnMinions() {
        state.canSpawnMobs = false;
        for (L2Attackable mobs : state.spawnedMobs) {
            if ((mobs != null) && !mobs.isDead()) {
                mobs.doDie(null);
            }
        }
    }

    private void manageTimer(int time) {
        for (L2PcInstance player : zone.getPlayersInside()) {
            if (player != null) {
                player.sendPacket(new ExSendUIEvent(player, false, false, time, 0, "Time remaining until battle"));
            }
        }
    }

    private void manageScreenMsg(NpcStringId stringId) {
        for (L2PcInstance player : zone.getPlayersInside()) {
            if (player != null) {
                showOnScreenMsg(player, stringId, 2, 6000);
            }
        }
    }

    private void manageMovie(int movie) {
        for (L2PcInstance player : zone.getPlayersInside()) {
            if (player != null) {
                player.showQuestMovie(movie);
            }
        }
    }

    public static void main(String[] args) {
        new Freya();
    }

}
