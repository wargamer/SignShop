package org.wargamer2010.signshop.specialops;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.block.Sign;
import org.bukkit.event.player.PlayerInteractEvent;
import java.util.List;
import java.util.Map;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.wargamer2010.signshop.SignShop;
import org.wargamer2010.signshop.Seller;
import org.wargamer2010.signshop.player.SignShopPlayer;
import org.wargamer2010.signshop.util.*;
import org.wargamer2010.signshop.operations.*;

public class copySign implements SignShopSpecialOp {
    @Override
    public Boolean runOperation(List<Block> clickedBlocks, PlayerInteractEvent event) {
        Player player = event.getPlayer();
        Block shopSign = event.getClickedBlock();
        if(!itemUtil.clickedSign(shopSign))
            return false;
        if(player.getItemInHand().getType() != Material.INK_SACK)
            return false;
        
        Sign signNewSign = null;
        for(Block tempBlock : clickedBlocks) {
            if(itemUtil.clickedSign(tempBlock)) {
                signNewSign = ((Sign) tempBlock.getState());
                break;
            }
        }
        if(signNewSign == null || SignShop.Storage.getSeller(signNewSign.getLocation()) != null)
            return false;
        
        Sign signToChange = ((Sign) shopSign.getState());
        String[] sNewSign = signNewSign.getLines();
        String[] sToChange = signToChange.getLines();        
        Seller seller = SignShop.Storage.getSeller(shopSign.getLocation());
        if(seller == null)
            return false;
        SignShopPlayer ssPlayer = new SignShopPlayer(player);
        if((!seller.getOwner().equals(player.getName()) || !ssPlayer.hasPerm("SignShop.CopyPaste", true)) && !ssPlayer.hasPerm("SignShop.CopyPaste.Others", true)) {
            ssPlayer.sendMessage(SignShop.Errors.get("no_permission"));
            return false;
        }
                
        if(sNewSign[0] != null && sNewSign[0].length() > 0) {
            if(SignShop.Operations.containsKey(signshopUtil.getOperation(sNewSign[0]))) {
                String sOperation = signshopUtil.getOperation(sNewSign[0]);
                List<String> operation = SignShop.Operations.get(sOperation);                
                if(!operation.contains("playerisop") && !ssPlayer.hasPerm(("SignShop.Signs."+sOperation), false)) {
                    ssPlayer.sendMessage(SignShop.Errors.get("no_permission"));
                    return false;
                }
                Map<SignShopOperation, List> SignShopOperations = signshopUtil.getSignShopOps(operation);
                if(SignShopOperations == null)
                    return false;
                SignShopArguments ssArgs = new SignShopArguments(economyUtil.parsePrice(sToChange[3]), null, seller.getContainables(), seller.getActivatables(), 
                        ssPlayer, ssPlayer, shopSign, sOperation, event.getBlockFace());
                
                Boolean bSetupOK = false;
                for(Map.Entry<SignShopOperation, List> ssOperation : SignShopOperations.entrySet()) {
                    ssArgs.operationParameters = ssOperation.getValue();
                    bSetupOK = ssOperation.getKey().setupOperation(ssArgs);
                    if(!bSetupOK)
                        return false;
                }
                if(!bSetupOK) {
                    ssPlayer.sendMessage("The new and old operation are not compatible.");
                    return false;
                }                
                signToChange.setLine(0, sNewSign[0]);                
            }
        }
        if(sNewSign[1] != null && sNewSign[1].length() > 0)
            signToChange.setLine(1, sNewSign[1]);
        if(sNewSign[2] != null && sNewSign[2].length() > 0)
            signToChange.setLine(2, sNewSign[2]);
        if(sNewSign[3] != null && sNewSign[3].length() > 0)
            signToChange.setLine(3, sNewSign[3]);
        
        signToChange.update();
        itemUtil.setSignStatus(shopSign, ChatColor.DARK_BLUE);
        
        ssPlayer.sendMessage("The sign has been succesfully updated.");
        return true;
    }
}
