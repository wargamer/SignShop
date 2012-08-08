package org.wargamer2010.signshop.specialops;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import java.util.List;
import java.util.Map;
import org.wargamer2010.signshop.SignShop;
import org.wargamer2010.signshop.Seller;
import org.wargamer2010.signshop.player.SignShopPlayer;
import org.wargamer2010.signshop.util.*;

public class changeOwner implements SignShopSpecialOp {
    @Override
    public Boolean runOperation(List<Block> clickedBlocks, PlayerInteractEvent event) {
        Player player = event.getPlayer();
        SignShopPlayer ssPlayer = new SignShopPlayer(player);
        Block shopSign = event.getClickedBlock();
        Seller seller = SignShop.Storage.getSeller(shopSign.getLocation());
        if(seller == null)
            return false;
        if(!ssPlayer.hasPerm("SignShop.ChangeOwner", true))
            return false;
        if(!seller.getOwner().equals(player.getName()) && !ssPlayer.hasPerm("SignShop.ChangeOwner.Others", true))
            return false;
        if(!clicks.mClicksPerPlayername.containsValue(player))
            return false;
        String sNewOwner = "";
        for(Map.Entry<String, Player> entry : clicks.mClicksPerPlayername.entrySet()) {
            if(entry.getValue() == player) {
                sNewOwner = entry.getKey();
                break;
            }
        }
        if(sNewOwner.equals(""))
            return false;
        
        seller.setOwner(sNewOwner);
        SignShop.Storage.Save();
        ssPlayer.sendMessage("Succesfully changed ownership of shop to " + sNewOwner);
        clicks.mClicksPerPlayername.remove(sNewOwner);
        return true;
    }
}
