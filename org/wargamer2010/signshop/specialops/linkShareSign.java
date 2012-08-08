package org.wargamer2010.signshop.specialops;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import java.util.List;
import java.util.LinkedList;
import org.bukkit.block.Sign;
import org.wargamer2010.signshop.SignShop;
import org.wargamer2010.signshop.Seller;
import org.wargamer2010.signshop.player.SignShopPlayer;
import org.wargamer2010.signshop.util.*;

public class linkShareSign implements SignShopSpecialOp {
    @Override
    public Boolean runOperation(List<Block> clickedBlocks, PlayerInteractEvent event) {
        Player player = event.getPlayer();
        SignShopPlayer ssPlayer = new SignShopPlayer(player);
        Block shopSign = event.getClickedBlock();
        Seller seller = SignShop.Storage.getSeller(shopSign.getLocation());
        if(seller == null)
            return false;
        
        List<Block> shareSigns = new LinkedList();
        for(Block bTemp : clickedBlocks) {
            if(itemUtil.clickedSign(bTemp)) {
                Sign sign = (Sign)bTemp.getState();
                if(signshopUtil.getOperation(sign.getLine(0)).equals("Share"))
                    shareSigns.add(bTemp);
            }
        }
        
        if(shareSigns.isEmpty())
            return false;
        
        String locations = "";
        if((locations = signshopUtil.validateShareSign(shareSigns, ssPlayer)).equals(""))
            return true;
        else
            seller.getMisc().put("sharesigns", locations);
        
        ssPlayer.sendMessage("Succesfully linked Share sign to Shop.");
        return true;
    }
}
