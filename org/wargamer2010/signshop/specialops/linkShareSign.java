package org.wargamer2010.signshop.specialops;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import java.util.List;
import java.util.LinkedList;
import org.bukkit.Material;
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
        if(ssPlayer.getPlayer().getItemInHand() == null || ssPlayer.getPlayer().getItemInHand().getType() != Material.REDSTONE)
            return false;
        if(!itemUtil.clickedSign(shopSign) || signshopUtil.getOperation(((Sign)shopSign.getState()).getLine(0)).equals("share"))
            return false;
        
        List<Block> shareSigns = new LinkedList();
        
        List<Block> currentSigns = signshopUtil.getCurrentShareSigns(seller);
        Boolean bUnlinked = false;
        for(Block bTemp : clickedBlocks) {
            if(currentSigns.contains(bTemp)) {                
                ssPlayer.sendMessage(signshopUtil.getError("unlinked_share_sign", null));
                bUnlinked = true;
                currentSigns.remove(bTemp);
            } else if(itemUtil.clickedSign(bTemp)) {
                Sign sign = (Sign)bTemp.getState();
                if(signshopUtil.getOperation(sign.getLine(0)).equals("share"))
                    shareSigns.add(bTemp);
            }
        }
        
        if((bUnlinked && shareSigns.isEmpty()) || !shareSigns.isEmpty()) {
            if(!seller.getOwner().equals(player.getName()) && !player.isOp()) {
                ssPlayer.sendMessage(signshopUtil.getError("not_allowed_to_link_sharesigns", null));
                return true;
            }
        }
        
        
        if(!bUnlinked && shareSigns.isEmpty())
            return false;
        else if(shareSigns.isEmpty()) {
            seller.getMisc().remove("sharesigns");
            SignShop.Storage.DelayedSave();
            return true;
        }
        shareSigns.addAll(currentSigns);
        
        String locations = "";
        if((locations = signshopUtil.validateShareSign(shareSigns, ssPlayer)).equals(""))
            return true;
        else {
            ssPlayer.sendMessage(signshopUtil.getError("linked_share_sign", null));
            seller.getMisc().put("sharesigns", locations);
            SignShop.Storage.DelayedSave();
        }
        
        return true;
    }
}
