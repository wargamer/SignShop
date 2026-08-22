package org.wargamer2010.signshop.specialops;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.wargamer2010.signshop.Seller;
import org.wargamer2010.signshop.SignShop;
import org.wargamer2010.signshop.data.Storage;
import org.wargamer2010.signshop.player.PlayerCache;
import org.wargamer2010.signshop.player.PlayerIdentifier;
import org.wargamer2010.signshop.player.SignShopPlayer;
import org.wargamer2010.signshop.util.clicks;

import java.util.List;
import java.util.Map;

/**
 * Special operation that transfers shop ownership to another player.
 * Requires the new owner to be in the clickmap and appropriate permissions for the current owner.
 */
public class ChangeOwner implements SignShopSpecialOp {
    @Override
    public Boolean runOperation(List<Block> clickedBlocks, PlayerInteractEvent event, Boolean ranSomething) {
        Player player = event.getPlayer();
        SignShopPlayer ssPlayer = PlayerCache.getPlayer(player);
        Block shopSign = event.getClickedBlock();
        Seller seller = Storage.get().getSeller(shopSign.getLocation());
        if(seller == null)
            return false;
        if(!clicks.mClicksPerPlayerId.containsValue(player))
            return false;
        if(!ssPlayer.hasPerm("SignShop.ChangeOwner", true)) {
            ssPlayer.sendMessage(SignShop.getInstance().getSignShopConfig().getError("no_permission_changeowner", null));
            return false;
        }
        if(!seller.isOwner(ssPlayer) && !ssPlayer.hasPerm("SignShop.ChangeOwner.Others", true)) {
            ssPlayer.sendMessage(SignShop.getInstance().getSignShopConfig().getError("no_permission_changeowner", null));
            return false;
        }
        PlayerIdentifier newOwner = null;
        for(Map.Entry<PlayerIdentifier, Player> entry : clicks.mClicksPerPlayerId.entrySet()) {
            if(entry.getValue() == player) {
                newOwner = entry.getKey();
                break;
            }
        }
        if(newOwner == null)
            return false;

        seller.setOwner(PlayerCache.getPlayer(newOwner));
        Storage.get().Save();
        ssPlayer.sendMessage("Succesfully changed ownership of shop to " + newOwner.getName());
        clicks.mClicksPerPlayerId.remove(newOwner);
        return true;
    }
}
