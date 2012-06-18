package org.wargamer2010.signshop.specialops;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.block.Sign;
import org.bukkit.event.player.PlayerInteractEvent;
import java.util.List;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.wargamer2010.signshop.SignShop;
import org.wargamer2010.signshop.Seller;
import org.wargamer2010.signshop.player.SignShopPlayer;
import org.wargamer2010.signshop.util.*;
import org.wargamer2010.signshop.operations.*;

public class convertChestshop implements SignShopSpecialOp {
    @Override
    public Boolean runOperation(List<Block> clickedBlocks, PlayerInteractEvent event) {
        Player player = event.getPlayer();
        Block shopSign = event.getClickedBlock();
        /*
        String[] sLines = ((Sign) sign.getState()).getLines();
        Integer iAmount = -1;
        Integer iPrice = -1;
        String sPrice = "";
        String sAmount = sLines[1];
        String sMaterial = sLines[3].toUpperCase().replace(" ", "_");
        if(!admin.isOp())
            return iAmount;        
        if(Material.getMaterial(sMaterial) == null)
            return iAmount;
        try {
            iAmount = Integer.parseInt(sLines[1]);
        } catch(NumberFormatException e) {                        
            return -1;
        }
        if(alter) {
            Integer from;
            Integer to;
            Sign signblock = ((Sign)sign.getState());
            Sign emptyBlock = null;
            if(emptySign != null)
                emptyBlock = ((Sign)emptySign.getState());
            if((sLines[2].contains("B")) && sLines[2].contains("S")) {
                if(emptyBlock == null) {
                    admin.sendMessage("Punch an empty sign first!");
                    return -1;
                }
                if(sLines[2].indexOf(":") == -1)
                    return -1;
                String bits[] = sLines[2].split(":");
                if(bits[0].contains("S"))
                    iPrice = Math.round(economyUtil.parsePrice(bits[0]));
                else if(bits[1].contains("S"))
                    iPrice = Math.round(economyUtil.parsePrice(bits[1]));
                else
                    return -1;
                sPrice = Integer.toString(iPrice);
                
                emptyBlock.setLine(0, "[Sell]");
                emptyBlock.setLine(1, (sAmount + " of"));
                emptyBlock.setLine(2, sLines[3]);
                emptyBlock.setLine(3, sPrice);
                emptyBlock.update();
                
                if(bits[0].contains("B"))
                    iPrice = Math.round(economyUtil.parsePrice(bits[0]));
                else if(bits[1].contains("B"))
                    iPrice = Math.round(economyUtil.parsePrice(bits[1]));
                else
                    return -1;
                sPrice = Integer.toString(iPrice);
                signblock.setLine(0, "[Buy]");
            } else if(sLines[2].contains("B")) {                
                from = sLines[2].indexOf("B");
                if(sLines[2].indexOf(":", from+2) > from+2)
                    to = sLines[2].indexOf(":", from+2);
                else if(sLines[2].indexOf(" ", from+2) > from+2)
                    to = sLines[2].indexOf(" ", from+2);
                else
                    to = sLines[2].length();
                sPrice = sLines[2].substring(from+2, to);
                try {
                    iPrice = Integer.parseInt(sPrice);
                } catch(NumberFormatException e) {
                    return -1;
                }                
                signblock.setLine(0, "[Buy]");
            } else if(sLines[2].contains("S")) {
                from = sLines[2].indexOf("S");
                if(sLines[2].indexOf(":", from+2) > from+2)
                    to = sLines[2].indexOf(":", from+2);
                else if(sLines[2].indexOf(" ", from+2) > from+2)
                    to = sLines[2].indexOf(" ", from+2);
                else
                    to = sLines[2].length();
                sPrice = sLines[2].substring(from+2, to);
                try {
                    iPrice = Integer.parseInt(sPrice);
                } catch(NumberFormatException e) {
                    return -1;
                }                
                signblock.setLine(0, "[Sell]");
            } else
                return -1;
            signblock.setLine(1, (sAmount + " of"));
            signblock.setLine(2, sLines[3]);
            signblock.setLine(3, sPrice);
            signblock.update();
        }        
        return iAmount;*/
        return true;
    }
    
    private Boolean emptySign(Block sign) {
        String[] sLines = ((Sign) sign.getState()).getLines();
        for(int i = 0; i < 4; i++)
            if(!sLines[i].equals(""))
                return false;
        return true;
    }
}
