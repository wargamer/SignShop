package org.wargamer2010.signshop.specialops;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import java.util.List;
import java.util.LinkedList;
import org.bukkit.Material;
import org.bukkit.block.Sign;
import org.wargamer2010.signshop.configuration.SignShopConfig;
import org.wargamer2010.signshop.Seller;
import org.wargamer2010.signshop.configuration.Storage;
import org.wargamer2010.signshop.player.SignShopPlayer;
import org.wargamer2010.signshop.util.*;

public class linkShareSign implements SignShopSpecialOp {
    @Override
    public Boolean runOperation(List<Block> clickedBlocks, PlayerInteractEvent event, Boolean ranSomething) {
        Player player = event.getPlayer();
        SignShopPlayer ssPlayer = new SignShopPlayer(player);
        Block shopSign = event.getClickedBlock();
        Seller seller = Storage.get().getSeller(shopSign.getLocation());
        String sOperation = signshopUtil.getOperation(((Sign)shopSign.getState()).getLine(0));
        if(seller == null)
            return false;
        if(ssPlayer.getPlayer().getItemInHand() == null || ssPlayer.getPlayer().getItemInHand().getType() != SignShopConfig.getLinkMaterial())
            return false;
        if(!itemUtil.clickedSign(shopSign) || sOperation.equals("restricted") || sOperation.equals("share"))
            return false;

        List<Block> shareSigns = new LinkedList<Block>();

        List<Block> currentSigns = signshopUtil.getSignsFromMisc(seller, "sharesigns");
        Boolean bUnlinked = false;
        for(Block bTemp : clickedBlocks) {
            if(currentSigns.contains(bTemp)) {
                ssPlayer.sendMessage(SignShopConfig.getError("unlinked_share_sign", null));
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
                ssPlayer.sendMessage(SignShopConfig.getError("not_allowed_to_link_sharesigns", null));
                return true;
            }
        }


        if(!bUnlinked && shareSigns.isEmpty())
            return false;
        else if(shareSigns.isEmpty()) {
            seller.getMisc().remove("sharesigns");
            Storage.get().SafeSave();
            return true;
        }
        shareSigns.addAll(currentSigns);

        String locations = signshopUtil.validateShareSign(shareSigns, ssPlayer);
        if(locations.isEmpty())
            return true;
        else {
            ssPlayer.sendMessage(SignShopConfig.getError("linked_share_sign", null));
            seller.getMisc().put("sharesigns", locations);
            Storage.get().SafeSave();
        }

        return true;
    }
}
