package org.wargamer2010.signshop.operations;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.block.sign.Side;
import org.wargamer2010.signshop.Seller;
import org.wargamer2010.signshop.SignShop;
import org.wargamer2010.signshop.data.Storage;
import org.wargamer2010.signshop.util.signshopUtil;

import java.util.LinkedList;
import java.util.List;

/**
 * Shop operation that registers share signs for profit splitting among multiple players.
 */
public class ShareSign implements SignShopOperation {
    @Override
    public Boolean setupOperation(SignShopArguments ssArgs) {
        List<Block> shops = Storage.get().getShopsWithMiscSetting("sharesigns", signshopUtil.convertLocationToString(ssArgs.getSign().get().getLocation()));
        if(!shops.isEmpty()) {
            for(Block bTemp : shops) {
                Seller seller = Storage.get().getSeller(bTemp.getLocation());
                if(seller != null && seller.hasMisc("sharesigns")) {
                    if(signshopUtil.validateShareSign(signshopUtil.getSignsFromMisc(seller, "sharesigns"), ssArgs.getPlayer().get()).isEmpty())
                        return false;
                }
            }
        } else {
            signshopUtil.registerClickedMaterial(ssArgs.getSign().get(), ssArgs.getPlayer().get());
            ssArgs.bDoNotClearClickmap = true;
            ssArgs.getPlayer().get().sendMessage(SignShop.getInstance().getSignShopConfig().getError("registered_share_sign", null));
        }
        return true;
    }

    @SuppressWarnings("DuplicateCondition")
    @Override
    public Boolean checkRequirements(SignShopArguments ssArgs, Boolean activeCheck) {
        List<Block> shops = Storage.get().getShopsWithMiscSetting("sharesigns", signshopUtil.convertLocationToString(ssArgs.getSign().get().getLocation()));
        if(shops.isEmpty()) {
            ssArgs.getPlayer().get().sendMessage(SignShop.getInstance().getSignShopConfig().getError("no_shop_linked_to_sharesign", null));
        } else {
            StringBuilder profitshops = new StringBuilder();
            boolean first = true;
            Block bLast = shops.getLast();
            for(Block bTemp : shops) {
                Location loc = bTemp.getLocation();
                if(first) first = false;
                else if (bTemp != bLast) profitshops.append(", ");
                else if (bTemp == bLast) profitshops.append(" and ");
                profitshops.append("(").append(loc.getX()).append(", ").append(loc.getY()).append(", ").append(loc.getZ()).append(")");
            }
            ssArgs.setMessagePart("!profitshops", profitshops.toString());
            StringBuilder profits = new StringBuilder();
            List<String> names = new LinkedList<>();
            Sign sign = (Sign)ssArgs.getSign().get().getState();
            String[] lines = sign.getSide(Side.FRONT).getLines();
            if(!signshopUtil.lineIsEmpty(lines[1])) names.add(lines[1]);
            if(!signshopUtil.lineIsEmpty(lines[2])) names.add(lines[2]);
            names.add("the Shop's respective owners");
            first = true;
            String sLast = names.getLast();
            for(String sTemp : names) {
                if(first) first = false;
                else if (!sLast.equals(sTemp)) profits.append(", ");
                else if (sLast.equals(sTemp)) profits.append(" and ");
                profits.append(sTemp);
            }
            ssArgs.setMessagePart("!profits", profits.toString());
            ssArgs.getPlayer().get().sendMessage(SignShop.getInstance().getSignShopConfig().getError("share_sign_splits_profit", ssArgs.getMessageParts()));
        }
        return true;
    }

    @Override
    public Boolean runOperation(SignShopArguments ssArgs) {
        return true;
    }
}
