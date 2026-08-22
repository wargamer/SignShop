package org.wargamer2010.signshop.commands;

import org.bukkit.ChatColor;
import org.wargamer2010.signshop.player.SignShopPlayer;

/**
 * Command handler for /signshop ignore.
 * Allows players to toggle whether they receive SignShop transaction messages.
 */
public class IgnoreHandler implements ICommandHandler{
    private static final ICommandHandler instance = new IgnoreHandler();

    private IgnoreHandler(){

    }

    public static ICommandHandler getInstance() {
        return instance;
    }

    @Override
    public boolean handle(String command, String[] args, SignShopPlayer signShopPlayer) {
        boolean wasIgnoring = signShopPlayer.isIgnoreMessages();

        if (!wasIgnoring) {
            signShopPlayer.sendNonDelayedMessage(ChatColor.RED + "You are now ignoring SignShop messages.");
        }

        signShopPlayer.setIgnoreMessages(!wasIgnoring);

        if (wasIgnoring) {
            signShopPlayer.sendNonDelayedMessage(ChatColor.GREEN + "You are no longer ignoring SignShop messages.");
        }

        return true;
    }
}
