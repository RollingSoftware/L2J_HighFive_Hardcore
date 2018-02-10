/*
 * Copyright (C) 2004-2016 L2J Server
 *
 * This file is part of L2J Server.
 *
 * L2J Server is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * L2J Server is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.l2jserver.gameserver.model.actor.stat;

import com.l2jserver.Config;
import com.l2jserver.gameserver.model.actor.L2Npc;
import com.l2jserver.gameserver.model.actor.L2Character;
import com.l2jserver.gameserver.model.skills.Skill;
import com.l2jserver.gameserver.model.stats.Stats;
import com.l2jserver.gameserver.model.stats.Formulas;
import com.l2jserver.gameserver.model.actor.instance.L2MonsterInstance;
import com.l2jserver.gameserver.model.actor.instance.L2GuardInstance;

public class NpcStat extends CharStat {

	public NpcStat(L2Npc activeChar) {
		super(activeChar);
	}

	@Override
	public byte getLevel() {
		return getActiveChar().getTemplate().getLevel();
	}

	@Override
	public L2Npc getActiveChar() {
		return (L2Npc) super.getActiveChar();
	}

	@Override
	public double getPDef(L2Character target) {
		if (getActiveChar() == null) {
			return 1;
		}

		double pdef = Formulas.calculateMultipliers(getActiveChar(), getActiveChar().getTemplate().getBasePDef(), Config.RAID_PDEFENCE_MULTIPLIER, Config.MONSTER_PDEFENCE_MULTIPLIER, Config.GUARD_PDEFENCE_MULTIPLIER);
		return (int) calcStat(Stats.POWER_DEFENCE, pdef, target, null);
	}

	@Override
	public double getMDef(L2Character target, Skill skill) {
		if (getActiveChar() == null) {
			return 1;
		}

		double defence = Formulas.calculateMultipliers(getActiveChar(), getActiveChar().getTemplate().getBaseMDef(), Config.RAID_MDEFENCE_MULTIPLIER, Config.MONSTER_MDEFENCE_MULTIPLIER, Config.GUARD_MDEFENCE_MULTIPLIER);
		return (int) calcStat(Stats.MAGIC_DEFENCE, defence, target, skill);
	}

	@Override
	public double getPAtk(L2Character target) {
    if (getActiveChar() == null) {
    	return 1;
		}
    double bonusAtk = 1;
		if (Config.L2JMOD_CHAMPION_ENABLE && getActiveChar().isChampion()) {
			bonusAtk = Config.L2JMOD_CHAMPION_ATK;
		}

		bonusAtk *= Formulas.calculateMultipliers(getActiveChar(), bonusAtk, Config.RAID_PATTACK_MULTIPLIER, Config.MONSTER_PATTACK_MULTIPLIER, Config.GUARD_PATTACK_MULTIPLIER);
		return calcStat(Stats.POWER_ATTACK, getActiveChar().getTemplate().getBasePAtk() * bonusAtk, target, null);
	}

	@Override
	public double getMAtk(L2Character target, Skill skill) {
		float bonusAtk = 1;
		if (Config.L2JMOD_CHAMPION_ENABLE && getActiveChar().isChampion()) {
			bonusAtk = Config.L2JMOD_CHAMPION_ATK;
		}

		bonusAtk *= Formulas.calculateMultipliers(getActiveChar(), bonusAtk, Config.RAID_MATTACK_MULTIPLIER, Config.MONSTER_MATTACK_MULTIPLIER, Config.GUARD_MATTACK_MULTIPLIER);
		return calcStat(Stats.MAGIC_ATTACK, getActiveChar().getTemplate().getBaseMAtk() * bonusAtk, target, skill);
	}

	@Override
	public int getMaxHp()	{
		double maxHp = getActiveChar().getTemplate().getBaseHpMax();
		if (L2MonsterInstance.class.isAssignableFrom(getActiveChar().getClass()) && !getActiveChar().isRaid()) {
			maxHp *= Config.MONSTER_HP_MULTIPLIER;
		} else if (L2GuardInstance.class.isAssignableFrom(getActiveChar().getClass())) {
			maxHp *= Config.GUARD_HP_MULTIPLIER;
		}

		return (int) calcStat(Stats.MAX_HP, maxHp);
	}

	@Override
	public int getMaxMp() {
		double maxMp = getActiveChar().getTemplate().getBaseMpMax();
		if (L2MonsterInstance.class.isAssignableFrom(getActiveChar().getClass()) && !getActiveChar().isRaid()) {
			maxMp *= Config.MONSTER_MP_MULTIPLIER;
		} else if (L2GuardInstance.class.isAssignableFrom(getActiveChar().getClass())) {
			maxMp *= Config.GUARD_MP_MULTIPLIER;
		}
		return (int) calcStat(Stats.MAX_MP, maxMp);
	}

}
