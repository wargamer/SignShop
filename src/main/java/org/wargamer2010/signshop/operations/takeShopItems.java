package org.wargamer2010.signshop.operations;

import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.wargamer2010.signshop.SignShop;
import org.wargamer2010.signshop.util.ItemMessagePart;
import org.wargamer2010.signshop.util.itemUtil;

/**
 * Shop operation that removes items from shop chests for transactions.
 */
public class takeShopItems implements SignShopOperation {
    @Override
    public Boolean setupOperation(SignShopArguments ssArgs) {
        if(ssArgs.getContainables().isEmpty()) {
            ssArgs.getPlayer().get().sendMessage(SignShop.getInstance().getSignShopConfig().getError("chest_missing", ssArgs.getMessageParts()));
            return false;
        }
        ItemStack[] isTotalItems = itemUtil.getAllItemStacksForContainables(ssArgs.getContainables().get());

        if(!ssArgs.isOperationParameter("allowemptychest") && isTotalItems.length == 0) {
            ssArgs.getPlayer().get().sendMessage(SignShop.getInstance().getSignShopConfig().getError("chest_empty", ssArgs.getMessageParts()));
            return false;
        }
        if(isTotalItems.length > 0)
            ssArgs.getItems().set(isTotalItems);
        ssArgs.setMessagePart("!items", ItemMessagePart.fromItems(ssArgs.getItems().get()));
        return true;
    }

    @Override
    public Boolean checkRequirements(SignShopArguments ssArgs, Boolean activeCheck) {
        if(ssArgs.getItems().get() == null) {
            ssArgs.getPlayer().get().sendMessage(SignShop.getInstance().getSignShopConfig().getError("no_items_defined_for_shop", ssArgs.getMessageParts()));
            return false;
        }

        Boolean bStockOK = itemUtil.stockOKForContainables(ssArgs.getContainables().get(), ssArgs.getItems().get(), true);
        ssArgs.setMessagePart("!items", ItemMessagePart.fromItems(ssArgs.getItems().get()));
        if(!bStockOK)
            ssArgs.sendFailedRequirementsMessage("out_of_stock");
        if(!bStockOK && activeCheck)
            itemUtil.updateStockStatus(ssArgs.getSign().get(), SignShop.getInstance().getSignShopConfig().getOutOfStockColor());
        else if(activeCheck)
            itemUtil.updateStockStatus(ssArgs.getSign().get(), SignShop.getInstance().getSignShopConfig().getInStockColor());

        return bStockOK;
    }

    @Override
    public Boolean runOperation(SignShopArguments ssArgs) {
        InventoryHolder Holder = itemUtil.getFirstStockOKForContainables(ssArgs.getContainables().get(), ssArgs.getItems().get(), true);
        if(Holder == null)
            return false;
        Holder.getInventory().removeItem(ssArgs.getItems().get());
        if(!itemUtil.stockOKForContainables(ssArgs.getContainables().get(), ssArgs.getItems().get(), true))
            itemUtil.updateStockStatus(ssArgs.getSign().get(), SignShop.getInstance().getSignShopConfig().getOutOfStockColor());
        else
            itemUtil.updateStockStatus(ssArgs.getSign().get(), SignShop.getInstance().getSignShopConfig().getInStockColor());
        ssArgs.setMessagePart("!items", ItemMessagePart.fromItems(ssArgs.getItems().get()));
        return true;
    }
}
