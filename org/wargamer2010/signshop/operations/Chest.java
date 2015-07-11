package org.wargamer2010.signshop.operations;

import org.bukkit.block.Block;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import java.util.logging.Level;
import java.util.List;
import java.util.LinkedList;
import java.util.Arrays;
import org.wargamer2010.signshop.SignShop;
import org.wargamer2010.signshop.util.itemUtil;
import org.wargamer2010.signshop.Seller;
import org.wargamer2010.signshop.configuration.SignShopConfig;
import static org.wargamer2010.signshop.operations.SignShopArguments.seperator;
import org.wargamer2010.signshop.util.signshopUtil;

public class Chest implements SignShopOperation {
    private Boolean incorrectPar(SignShopArguments ssArgs) {
        ssArgs.getPlayer().get().sendMessage(SignShopConfig.getError("could_not_complete_operation", null));
        SignShop.log("Invalid Chest{}, check your config.yml!", Level.WARNING);
        return false;
    }

    private Block checkChestAmount(SignShopArguments ssArgs, Integer iChestnumber) {
        Block bHolder = null;
        int iCount = 0;
        for(Block bTemp : ssArgs.getContainables().getRoot()) {
            if(bTemp.getState() instanceof InventoryHolder) {
                iCount++;
                if(iCount == iChestnumber)
                    bHolder = bTemp;
            }
        }
        return bHolder;
    }

    @Override
    public Boolean setupOperation(SignShopArguments ssArgs) {
        if(!ssArgs.hasOperationParameters())
            return incorrectPar(ssArgs);
        Integer iChestnumber;
        try {
            iChestnumber = Integer.parseInt(ssArgs.getFirstOperationParameter());
        } catch(NumberFormatException ex) {
            return incorrectPar(ssArgs);
        }
        if(iChestnumber < 1)
            return incorrectPar(ssArgs);

        ssArgs.forceMessageKeys.put("!items", ("!chest" + iChestnumber));

        Block bHolder = checkChestAmount(ssArgs, iChestnumber);
        if(bHolder == null) {
            if(ssArgs.isOperationParameter("allowNoChests")) {
                List<Block> containables = new LinkedList<Block>();
                ssArgs.getContainables().set(containables);
                return true;
            }

            ssArgs.getPlayer().get().sendMessage("You need at least " + (iChestnumber) + " chest(s) to setup this shop!");
            return false;
        }

        List<Block> containables = new LinkedList<Block>();
        containables.add(bHolder);
        ssArgs.getContainables().set(containables);

        // In case the next operation doesn't write to !items, in other cases it will be overwritten (by f.e. takePlayerItems)
        ItemStack[] isTotalItems = itemUtil.getAllItemStacksForContainables(ssArgs.getContainables().get());
        if(isTotalItems.length > 0) {
            ssArgs.setMessagePart("!items", itemUtil.itemStackToString(isTotalItems));
            ssArgs.miscSettings.put("chest" + iChestnumber, signshopUtil.implode(itemUtil.convertItemStacksToString(isTotalItems), seperator));
        }

        // Since we'll be requesting the MetaID before the Seller is created, we need to register the items here
        Seller.storeMeta(itemUtil.getAllItemStacksForContainables(containables));

        return true;
    }

    @Override
    public Boolean checkRequirements(SignShopArguments ssArgs, Boolean activeCheck) {
        return this.runOperation(ssArgs);
    }

    @Override
    public Boolean runOperation(SignShopArguments ssArgs) {
        Integer iChestnumber;
        try {
            iChestnumber = Integer.parseInt(ssArgs.getFirstOperationParameter());
        } catch(NumberFormatException ex) {
            return incorrectPar(ssArgs);
        }
        if(iChestnumber < 1)
            return incorrectPar(ssArgs);
        if(!ssArgs.miscSettings.containsKey(("chest" + iChestnumber)))
            return incorrectPar(ssArgs);

        ssArgs.forceMessageKeys.put("!items", ("!chest" + iChestnumber));
        String misc = ssArgs.miscSettings.get(("chest" + iChestnumber));
        String[] sItemss;
        if(!misc.contains(SignShopArguments.seperator)) {
            sItemss = new String[1];
            sItemss[0] = misc;
        } else
            sItemss = misc.split(SignShopArguments.seperator);
        ItemStack[] isItemss;

        isItemss = itemUtil.convertStringtoItemStacks(Arrays.asList(sItemss));
        ssArgs.getItems().set(isItemss);

        Block bHolder = checkChestAmount(ssArgs, iChestnumber);
        if(bHolder != null) {
            LinkedList<Block> tempContainables = new LinkedList<Block>();
            tempContainables.add(bHolder);
            ssArgs.getContainables().set(tempContainables);
        }

        return true;
    }
}
