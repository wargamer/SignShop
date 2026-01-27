package org.wargamer2010.signshop.listeners.sslisteners;

import org.bukkit.ChatColor;
import org.bukkit.block.Sign;
import org.bukkit.block.sign.Side;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.wargamer2010.signshop.SignShop;
import org.wargamer2010.signshop.configuration.SignShopConfig;
import org.wargamer2010.signshop.events.SSCreatedEvent;

import java.util.Arrays;
import java.util.Collection;

/**
 * Internal listener that validates both sides of signs to prevent blacklisted text and conflicting shop types.
 */
public class SignSidesValidator implements Listener {

    SignShopConfig config;
    Collection<String> operations;


    public SignSidesValidator() {
        config = SignShop.getInstance().getSignShopConfig();
        operations = config.getOperations();
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onSSCreatedEvent(SSCreatedEvent event) {
        if (event.isCancelled()) return;


        if (event.getSign().getState() instanceof Sign sign) {
            String[] frontLines = sign.getSide(Side.FRONT).getLines();
            String[] backLines = sign.getSide(Side.BACK).getLines();

            // Return if back lines are empty
            boolean firstIsEmpty = backLines[0].isEmpty();
            boolean secondIsEmpty = backLines[1].isEmpty();
            boolean thirdIsEmpty = backLines[2].isEmpty();
            boolean fourthIsEmpty = backLines[3].isEmpty();
            if (firstIsEmpty && secondIsEmpty && thirdIsEmpty && fourthIsEmpty) {
                return;
            }

            // Check if any of the lines contain blacklisted text
            for (int i = 0; i <= 3; i++) {
                if (blackListContains(backLines[i])) {
                    event.setCancelled(true);
                    event.setMessagePart("!blacklisted_text", backLines[i]);
                    event.getPlayer().sendMessage(config.getError("text_on_blacklist", event.getMessageParts()));
                }
            }


            // Check if front and back are the same, if not the back cannot have any shop types on it
            if (!Arrays.equals(frontLines, backLines)) {
                String backTopLine = backLines[0];
                if (oplistListContains(backTopLine)) {
                    event.setCancelled(true);
                    event.getPlayer().sendMessage(config.getError("shop_on_sign_back_must_match_front", null));

                }
                for (int i = 1; i <= 3; i++) {
                    if (oplistListContains(backLines[i])) {
                        event.setCancelled(true);
                        event.setMessagePart("!blacklisted_text", backLines[i]);
                        event.getPlayer().sendMessage(config.getError("text_on_blacklist", event.getMessageParts()));
                    }
                }
            }


        }


    }


    private boolean blackListContains(String string) {
        return SignShop.getInstance().getSignShopConfig().stringIsOnBackOfSignTextBlacklist(ChatColor.stripColor(string.toLowerCase()));
    }

    private boolean oplistListContains(String string) {
        String cleanString = ChatColor.stripColor(string.toLowerCase());
        if (cleanString.length() < 3) {
            return operations.contains(cleanString);
        }
        return operations.contains(cleanString)
                || operations.contains(cleanString.substring(1, cleanString.length() - 1));
    }

}

