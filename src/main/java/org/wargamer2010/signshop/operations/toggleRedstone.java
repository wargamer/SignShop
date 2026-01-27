package org.wargamer2010.signshop.operations;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.type.Switch;
import org.wargamer2010.signshop.SignShop;
import org.wargamer2010.signshop.util.signshopUtil;

/**
 * Shop operation that toggles the power state of linked lever blocks.
 */
public class toggleRedstone implements SignShopOperation {
    @Override
    public Boolean setupOperation(SignShopArguments ssArgs) {
        boolean foundLever = false;
        for(Block block : ssArgs.getActivatables().get())
            if(block.getType() == Material.getMaterial("LEVER"))
                foundLever = true;
        if(!foundLever) {
            ssArgs.getPlayer().get().sendMessage(SignShop.getInstance().getSignShopConfig().getError("lever_missing", ssArgs.getMessageParts()));
            return false;
        }
        return true;
    }

    @Override
    public Boolean checkRequirements(SignShopArguments ssArgs, Boolean activeCheck) {
        return setupOperation(ssArgs);
    }

    @Override
    public Boolean runOperation(SignShopArguments ssArgs) {
        Block bLever;

        for(int i = 0; i < ssArgs.getActivatables().get().size(); i++) {
            bLever = ssArgs.getActivatables().get().get(i);
            if(bLever.getType() == Material.getMaterial("LEVER") && bLever.getBlockData() instanceof Switch) {
                Switch switchLever = (Switch) bLever.getBlockData();
                switchLever.setPowered(!switchLever.isPowered());
                bLever.setBlockData(switchLever);
                signshopUtil.generateInteractEvent(bLever, ssArgs.getPlayer().get().getPlayer(), ssArgs.getBlockFace().get());
            }
        }

        return true;
    }
}
