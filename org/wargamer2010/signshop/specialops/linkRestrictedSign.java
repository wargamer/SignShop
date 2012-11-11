package org.wargamer2010.signshop.specialops;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.Material;
import org.bukkit.block.Sign;
import java.util.List;
import java.util.LinkedList;
import org.wargamer2010.signshop.SignShop;
import org.wargamer2010.signshop.configuration.SignShopConfig;
import org.wargamer2010.signshop.Seller;
import org.wargamer2010.signshop.player.SignShopPlayer;
import org.wargamer2010.signshop.util.*;

public class linkRestrictedSign implements SignShopSpecialOp {
    @Override
    public Boolean runOperation(List<Block> clickedBlocks, PlayerInteractEvent event, Boolean ranSomething) {
        Player player = event.getPlayer();
        SignShopPlayer ssPlayer = new SignShopPlayer(player);
        Block shopSign = event.getClickedBlock();
        Seller seller = SignShop.Storage.getSeller(shopSign.getLocation());
        String sOperation = signshopUtil.getOperation(((Sign)shopSign.getState()).getLine(0));
        if(seller == null)
            return false;
        if(ssPlayer.getPlayer().getItemInHand() == null || ssPlayer.getPlayer().getItemInHand().getType() != Material.getMaterial("REDSTONE"))
            return false;
        if(!itemUtil.clickedSign(shopSign) || sOperation.equals("restricted") || sOperation.equals("share"))
            return false;

        List<Block> restrictedSigns = new LinkedList();

        List<Block> currentSigns = signshopUtil.getSignsFromMisc(seller, "restrictedsigns");
        Boolean bUnlinked = false;
        for(Block bTemp : clickedBlocks) {
            if(currentSigns.contains(bTemp)) {
                ssPlayer.sendMessage(SignShopConfig.getError("unlinked_restricted_sign", null));
                bUnlinked = true;
                currentSigns.remove(bTemp);
            } else if(itemUtil.clickedSign(bTemp)) {
                Sign sign = (Sign)bTemp.getState();
                if(signshopUtil.getOperation(sign.getLine(0)).equals("restricted"))
                    restrictedSigns.add(bTemp);
            }
        }

        if((bUnlinked && restrictedSigns.isEmpty()) || !restrictedSigns.isEmpty()) {
            if(!seller.getOwner().equals(player.getName()) && !player.isOp()) {
                ssPlayer.sendMessage(SignShopConfig.getError("not_allowed_to_link_restrictedsigns", null));
                return true;
            }
        }


        if(!bUnlinked && restrictedSigns.isEmpty())
            return false;
        else if(restrictedSigns.isEmpty()) {
            seller.getMisc().remove("restrictedsigns");
            SignShop.Storage.SafeSave();
            return true;
        }
        restrictedSigns.addAll(currentSigns);

        String locations = "";
        if((locations = signshopUtil.validateRestrictSign(restrictedSigns, ssPlayer)).isEmpty())
            return true;
        else {
            ssPlayer.sendMessage(SignShopConfig.getError("linked_restricted_sign", null));
            seller.getMisc().put("restrictedsigns", locations);
            SignShop.Storage.SafeSave();
        }

        return true;
    }
}
