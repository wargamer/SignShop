package org.wargamer2010.signshop.specialops;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.block.Sign;
import org.bukkit.event.player.PlayerInteractEvent;
import java.util.List;
import org.bukkit.Material;
import org.wargamer2010.signshop.util.*;
import org.wargamer2010.signshop.player.SignShopPlayer;

public class convertChestshop implements SignShopSpecialOp {
    @Override
    public Boolean runOperation(List<Block> clickedBlocks, PlayerInteractEvent event) {
        Player player = event.getPlayer();
        SignShopPlayer ssPlayer = new SignShopPlayer(player);
        Block sign = event.getClickedBlock();
        if(!itemUtil.clickedSign(sign))
            return false;
        
        Block emptySign = null;
        for(Block tempBlock : clickedBlocks) {
            if(this.emptySign(tempBlock)) {
                emptySign = tempBlock;
                break;
            }
        }
        
        Sign signblock = ((Sign)sign.getState());
        String[] sLines = signblock.getLines();        
        Integer iPrice = -1;
        String sPrice = "";
        String sAmount = sLines[1];
        String sMaterial = sLines[3].toUpperCase().replace(" ", "_");
        if(!player.isOp())
            return false; 
        if(Material.getMaterial(sMaterial) == null)
            return false;
        
        Integer from;
        Integer to;
        
        Sign emptyBlock = null;
        if(emptySign != null)
            emptyBlock = ((Sign)emptySign.getState());
        if((sLines[2].contains("B")) && sLines[2].contains("S")) {
            if(emptyBlock == null) {
                ssPlayer.sendMessage("ChestShop sign detected, punch an empty sign first!");
                return false;
            }
            if(sLines[2].indexOf(":") == -1)
                return false;
            String bits[] = sLines[2].split(":");
            if(bits[0].contains("S"))
                iPrice = Math.round(economyUtil.parsePrice(bits[0]));
            else if(bits[1].contains("S"))
                iPrice = Math.round(economyUtil.parsePrice(bits[1]));
            else
                return false;
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
                return false;
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
                return false;
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
                return false;
            }                
            signblock.setLine(0, "[Sell]");
        } else 
            return false;
        
        signblock.setLine(1, (sAmount + " of"));
        signblock.setLine(2, sLines[3]);
        signblock.setLine(3, sPrice);
        signblock.update();
        
        ssPlayer.sendMessage("ChestShop sign detected and successfully converted!");
                        
        return true;
    }
    
    private Boolean emptySign(Block sign) {
        if(!itemUtil.clickedSign(sign))
            return false;
        String[] sLines = ((Sign) sign.getState()).getLines();
        for(int i = 0; i < 4; i++)
            if(!sLines[i].equals(""))
                return false;
        return true;
    }
}
