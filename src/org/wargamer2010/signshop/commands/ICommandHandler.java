
package org.wargamer2010.signshop.commands;

import org.wargamer2010.signshop.player.SignShopPlayer;

public interface ICommandHandler {

    public boolean handle(String command, String[] args, SignShopPlayer player);

}
