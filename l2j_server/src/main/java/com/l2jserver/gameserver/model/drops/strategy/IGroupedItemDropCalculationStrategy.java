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
package com.l2jserver.gameserver.model.drops.strategy;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.l2jserver.Config;
import com.l2jserver.gameserver.model.actor.L2Character;
import com.l2jserver.gameserver.model.drops.GeneralDropItem;
import com.l2jserver.gameserver.model.drops.GroupedGeneralDropItem;
import com.l2jserver.gameserver.model.drops.IDropItem;
import com.l2jserver.gameserver.model.items.L2Item;
import com.l2jserver.gameserver.datatables.ItemTable;
import com.l2jserver.gameserver.model.holders.ItemHolder;
import com.l2jserver.util.Rnd;

/**
 * @author Battlecruiser
 */
public interface IGroupedItemDropCalculationStrategy
{
	/**
	 * The default strategy used in L2J to calculate drops. When the group's chance raises over 100% and group has precise calculation, the dropped item's amount increases.
	 */
	public static final IGroupedItemDropCalculationStrategy DEFAULT_STRATEGY = new IGroupedItemDropCalculationStrategy()
	{
		private final Map<GroupedGeneralDropItem, GeneralDropItem> singleItemCache = new ConcurrentHashMap<>();

		private GeneralDropItem getSingleItem(GroupedGeneralDropItem dropItem)
		{
			final GeneralDropItem item1 = dropItem.getItems().iterator().next();
			singleItemCache.putIfAbsent(dropItem, new GeneralDropItem(item1.getItemId(), item1.getMin(), item1.getMax(), (item1.getChance() * dropItem.getChance()) / 100, item1.getAmountStrategy(), item1.getChanceStrategy(), dropItem.getPreciseStrategy(), dropItem.getKillerChanceModifierStrategy(), item1.getDropCalculationStrategy()));
			return singleItemCache.get(dropItem);
		}

		@Override
		public List<ItemHolder> calculateDrops(GroupedGeneralDropItem dropItem, L2Character victim, L2Character killer)
		{
			if (dropItem.getItems().size() == 1)
			{
				return getSingleItem(dropItem).calculateDrops(victim, killer);
			}

			GroupedGeneralDropItem normalized = dropItem.normalizeMe(victim, killer);
			double random = (Rnd.nextDouble() * 100);
			if (normalized.getChance() >= 100 || normalized.getChance() > random) {

				long multiplier = 1L;
				if (normalized.getChance() >= 100) {
					multiplier = Math.round(normalized.getChance() / 100);
				}

				List<ItemHolder> drop = new ArrayList<>();
				for (GeneralDropItem item : normalized.getItems()) {

					List<ItemHolder> itemDrop = new ArrayList<>();
					L2Item itemTemplate = ItemTable.getInstance().getTemplate(item.getItemId());
					if (Config.VALUABLE_ITEMS_DETAILED_CALCULATION && IDropCalculationStrategy.isValuable(itemTemplate)) {
						itemDrop = IDropCalculationStrategy.calculateValuableDrop(item, victim, item.getChance(victim, killer), multiplier);
					} else {
						itemDrop = IDropCalculationStrategy.calculateDrop(item, victim, item.getChance(victim, killer), multiplier);
					}

					if (itemDrop == null) {
						continue;
					}

					if (Config.ALL_CATEGORY_ITEMS_DROP_CHANCE) {
						drop.addAll(itemDrop);
					}	else {
						return itemDrop;
					}
				}
				return drop.isEmpty() ? null : drop;
			}
			return null;
		}
	};

	public List<ItemHolder> calculateDrops(GroupedGeneralDropItem item, L2Character victim, L2Character killer);
}
