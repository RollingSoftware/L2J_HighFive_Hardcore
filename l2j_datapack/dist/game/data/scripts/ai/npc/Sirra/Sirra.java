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
package ai.npc.Sirra;

import ai.npc.AbstractNpcAI;

import com.l2jserver.gameserver.instancemanager.GrandBossManager;
import com.l2jserver.gameserver.instancemanager.InstanceManager;
import com.l2jserver.gameserver.model.actor.L2Npc;
import com.l2jserver.gameserver.model.actor.instance.L2PcInstance;
import com.l2jserver.gameserver.model.instancezone.InstanceWorld;

/**
 * Sirra AI.
 * @author St3eT
 */
public final class Sirra extends AbstractNpcAI {
	private static final int SIRRA = 32762;
	private static final int FREYA_STAND = 29179;

	private static final int ALIVE = 0;

	private Sirra()	{
		super(Sirra.class.getSimpleName(), "ai/npc");
		addFirstTalkId(SIRRA);
	}
	
	@Override
	public String onFirstTalk(L2Npc npc, L2PcInstance player) {
		return GrandBossManager.getInstance().getBossStatus(FREYA_STAND) == ALIVE ? "32762-open.html" : "32762-teleport.html";
	}
	
	public static void main(String[] args) {
		new Sirra();
	}

}