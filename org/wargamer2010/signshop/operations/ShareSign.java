package org.wargamer2010.signshop.operations;

import org.bukkit.block.Block;
import org.bukkit.Location;
import org.bukkit.block.Sign;
import org.wargamer2010.signshop.SignShop;
import org.wargamer2010.signshop.Seller;
import org.wargamer2010.signshop.util.signshopUtil;
import org.wargamer2010.signshop.configuration.SignShopConfig;
import java.util.List;
import java.util.LinkedList;

public class ShareSign implements SignShopOperation {    
    @Override
    public Boolean setupOperation(SignShopArguments ssArgs) {        
        List<Block> shops = SignShop.Storage.getShopsWithMiscSetting("sharesigns", signshopUtil.convertLocationToString(ssArgs.get_bSign().getLocation()));        
        if(!shops.isEmpty()) {
            for(Block bTemp : shops) {                
                Seller seller = SignShop.Storage.getSeller(bTemp.getLocation());
                if(seller != null && seller.getMisc().containsKey("sharesigns")) {
                    if(signshopUtil.validateShareSign(signshopUtil.getSignsFromMisc(seller, "sharesigns"), ssArgs.get_ssPlayer()).equals(""))            
                        return false;        
                }
            }
        } else {
            signshopUtil.registerClickedMaterial(ssArgs.get_bSign(), ssArgs.get_ssPlayer());            
            ssArgs.bDoNotClearClickmap = true;
            ssArgs.get_ssPlayer().sendMessage(SignShopConfig.getError("registered_share_sign", null));
        }
        return true;
    }
    
    @Override
    public Boolean checkRequirements(SignShopArguments ssArgs, Boolean activeCheck) {
        List<Block> shops = SignShop.Storage.getShopsWithMiscSetting("sharesigns", signshopUtil.convertLocationToString(ssArgs.get_bSign().getLocation()));        
        if(shops.isEmpty()) {
            ssArgs.get_ssPlayer().sendMessage(SignShopConfig.getError("no_shop_linked_to_sharesign", null));
        } else {
            String profitshops = "";
            Boolean first = true;
            Block bLast = shops.get(shops.size()-1);
            for(Block bTemp : shops) {
                Location loc = bTemp.getLocation();
                if(first) first = false;
                else if(bTemp != bLast) profitshops += ", ";
                else if(bTemp == bLast) profitshops += " and ";
                profitshops += ("(" + loc.getX() + ", " + loc.getY() + ", " + loc.getZ() + ")");
            }
            ssArgs.messageParts.put("!profitshops", profitshops);
            String profits = "";
            List<String> names = new LinkedList<String>();
            Sign sign = (Sign)ssArgs.get_bSign().getState();
            String[] lines = sign.getLines();
            if(!signshopUtil.lineIsEmpty(lines[1])) names.add(lines[1]);
            if(!signshopUtil.lineIsEmpty(lines[2])) names.add(lines[2]);
            names.add("the Shop's respective owners");
            first = true;
            String sLast = names.get(names.size()-1);
            for(String sTemp : names) {                
                if(first) first = false;
                else if(!sLast.equals(sTemp)) profits += ", ";
                else if(sLast.equals(sTemp)) profits += " and ";
                profits += sTemp;
            }
            ssArgs.messageParts.put("!profits", profits);
            ssArgs.get_ssPlayer().sendMessage(SignShopConfig.getError("share_sign_splits_profit", ssArgs.messageParts));
        }
        return true;
    }
    
    @Override
    public Boolean runOperation(SignShopArguments ssArgs) {  
        return true;
    }
}
