package org.wargamer2010.signshop.operations;

import org.bukkit.block.Block;
import org.bukkit.Location;
import org.wargamer2010.signshop.util.signshopUtil;
import org.wargamer2010.signshop.configuration.SignShopConfig;
import java.util.List;
import org.wargamer2010.signshop.configuration.Storage;

public class RestrictedSign implements SignShopOperation {
    @Override
    public Boolean setupOperation(SignShopArguments ssArgs) {
        signshopUtil.registerClickedMaterial(ssArgs.get_bSign(), ssArgs.get_ssPlayer());
        ssArgs.bDoNotClearClickmap = true;
        ssArgs.get_ssPlayer().sendMessage(SignShopConfig.getError("registered_restricted_sign", null));
        return true;
    }

    @Override
    public Boolean checkRequirements(SignShopArguments ssArgs, Boolean activeCheck) {
        List<Block> shops = Storage.get().getShopsWithMiscSetting("restrictedsigns", signshopUtil.convertLocationToString(ssArgs.get_bSign().getLocation()));
        if(shops.isEmpty()) {
            ssArgs.get_ssPlayer().sendMessage(SignShopConfig.getError("no_shop_linked_to_restrictedsign", null));
        } else {
            String restrictedshops = "";
            Boolean first = true;
            Block bLast = shops.get(shops.size()-1);
            for(Block bTemp : shops) {
                Location loc = bTemp.getLocation();
                if(first) first = false;
                else if(bTemp != bLast) restrictedshops += ", ";
                else if(bTemp == bLast) restrictedshops += " and ";
                restrictedshops += ("(" + loc.getX() + ", " + loc.getY() + ", " + loc.getZ() + ")");
            }
            ssArgs.setMessagePart("!restrictedshops", restrictedshops);
            ssArgs.get_ssPlayer().sendMessage(SignShopConfig.getError("restricted_sign_restricts", ssArgs.getMessageParts()));
        }
        return true;
    }

    @Override
    public Boolean runOperation(SignShopArguments ssArgs) {
        return true;
    }
}
