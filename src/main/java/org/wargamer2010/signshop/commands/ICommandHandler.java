
package org.wargamer2010.signshop.commands;

import org.wargamer2010.signshop.player.SignShopPlayer;

/**
 * Interface for /signshop subcommand handlers.
 *
 * @see CommandDispatcher
 */
public interface ICommandHandler {

    boolean handle(String command, String[] args, SignShopPlayer player);

}
