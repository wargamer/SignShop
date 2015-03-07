/*
 * Essentials - a bukkit plugin
 * Copyright (C) 2011  Essentials Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.wargamer2010.essentials;

import org.bukkit.entity.Player;

public class SetExpFix {

    /**
     * This method is used to update both the recorded total experience and displayed total experience.
     * We reset both types to prevent issues.
     *
     * @param player Player to set xp for
     * @param exp Total amount of xp to set
     */
    public static void setTotalExperience(final Player player, final int exp) {
        if (exp < 0) {
            throw new IllegalArgumentException("Experience is negative!");
        }
        player.setExp(0);
        player.setLevel(0);
        player.setTotalExperience(0);

        // This following code is technically redundant now, as bukkit now calulcates levels more or less correctly
        // At larger numbers however... player.getExp(3000), only seems to give 2999, putting the below calculations off.
        int amount = exp;
        while (amount > 0) {
            final int expToLevel = getExpAtLevel(player);
            amount -= expToLevel;
            if (amount >= 0) {
                // give until next level
                player.giveExp(expToLevel);
            } else {
                // give the rest
                amount += expToLevel;
                player.giveExp(amount);
                amount = 0;
            }
        }
    }

    /**
     * This method is required because the bukkit player.getTotalExperience() method, shows exp that has been 'spent'.
     * Without this people would be able to use exp and then still sell it.
     *
     * @param player Player to get xp for
     * @return Total Experience player has
     */
    public static int getTotalExperience(final Player player) {
        int exp = Math.round(getExpAtLevel(player) * player.getExp());
        int currentLevel = player.getLevel();

        while (currentLevel > 0) {
            currentLevel--;
            exp += getExpAtLevel(currentLevel);
        }
        if (exp < 0) {
            exp = Integer.MAX_VALUE;
        }
        return exp;
    }

    private static int getExpAtLevel(final Player player) {
        return getExpAtLevel(player.getLevel());
    }

    private static int getExpAtLevel(final int level) {
        // Stole calculation from: http://minecraft.gamepedia.com/Experience#Useful_Numbers
        if (level <= 15) {
            return (level * 2) + 7;
        } else if (level > 15 && level <= 30) {
            return (level * 5) - 38;
        } else {
            return (level * 9) - 158;
        }
    }

    private SetExpFix() {
    }
}
