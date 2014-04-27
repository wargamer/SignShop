package org.wargamer2010.signshop.specialops;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.block.Sign;
import org.bukkit.event.player.PlayerInteractEvent;
import java.util.List;
import org.bukkit.Material;
import org.wargamer2010.signshop.util.*;
import org.wargamer2010.signshop.player.SignShopPlayer;

public class ConvertChestshop implements SignShopSpecialOp {
    @Override
    public Boolean runOperation(List<Block> clickedBlocks, PlayerInteractEvent event, Boolean ranSomething) {
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
        long iPrice;
        String sAmount = sLines[1];
        String sMaterial = sLines[3].toUpperCase().replace(" ", "_");
        if(!ssPlayer.isOp())
            return false;
        if(Material.getMaterial(sMaterial) == null)
            return false;

        Sign emptyBlock = null;
        if(emptySign != null)
            emptyBlock = ((Sign)emptySign.getState());
        if((sLines[2].contains("B")) && sLines[2].contains("S")) {
            if(emptyBlock == null) {
                ssPlayer.sendMessage("ChestShop sign detected, punch an empty sign first!");
                return false;
            }
            if(sLines[2].indexOf(':') == -1)
                return false;
            String bits[] = sLines[2].split(":");
            if(bits[0].contains("S"))
                iPrice = Math.round(economyUtil.parsePrice(bits[0]));
            else if(bits[1].contains("S"))
                iPrice = Math.round(economyUtil.parsePrice(bits[1]));
            else
                return false;

            emptyBlock.setLine(0, "[Sell]");
            emptyBlock.setLine(1, (sAmount + " of"));
            emptyBlock.setLine(2, sLines[3]);
            emptyBlock.setLine(3, Long.toString(iPrice));
            emptyBlock.update();

            if(bits[0].contains("B"))
                iPrice = Math.round(economyUtil.parsePrice(bits[0]));
            else if(bits[1].contains("B"))
                iPrice = Math.round(economyUtil.parsePrice(bits[1]));
            else
                return false;
            signblock.setLine(0, "[Buy]");
        } else if(sLines[2].contains("B")) {
            iPrice = Math.round(economyUtil.parsePrice(sLines[2]));
            if(iPrice == 0.0f)
                return false;
            signblock.setLine(0, "[Buy]");
        } else if(sLines[2].contains("S")) {
            iPrice = Math.round(economyUtil.parsePrice(sLines[2]));
            if(iPrice == 0.0f)
                return false;
            signblock.setLine(0, "[Sell]");
        } else
            return false;

        signblock.setLine(1, (sAmount + " of"));
        signblock.setLine(2, sLines[3]);
        signblock.setLine(3, Long.toString(iPrice));
        signblock.update();

        ssPlayer.sendMessage("ChestShop sign detected and successfully converted!");

        return true;
    }

    private Boolean emptySign(Block sign) {
        if(!itemUtil.clickedSign(sign))
            return false;
        String[] sLines = ((Sign) sign.getState()).getLines();
        for(int i = 0; i < 4; i++)
            if(!sLines[i].isEmpty())
                return false;
        return true;
    }
}
