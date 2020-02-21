package org.wargamer2010.signshop.operations;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.material.Lever;
import org.bukkit.material.MaterialData;
import org.wargamer2010.signshop.configuration.SignShopConfig;
import org.wargamer2010.signshop.util.signshopUtil;

public class toggleRedstone implements SignShopOperation {
    @Override
    public Boolean setupOperation(SignShopArguments ssArgs) {
        boolean foundLever = false;
        for(Block block : ssArgs.getActivatables().get())
            if(block.getType() == Material.getMaterial("LEVER"))
                foundLever = true;
        if(!foundLever) {
            ssArgs.getPlayer().get().sendMessage(SignShopConfig.getError("lever_missing", ssArgs.getMessageParts()));
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
            if(bLever.getType() == Material.getMaterial("LEVER")) {
                BlockState state = bLever.getState();
                MaterialData data = state.getData();
                Lever lever = (Lever)data;
                if(!lever.isPowered())
                    lever.setPowered(true);
                else
                    lever.setPowered(false);
                state.setData(lever);
                state.update();
                signshopUtil.generateInteractEvent(bLever, ssArgs.getPlayer().get().getPlayer(), ssArgs.getBlockFace().get());
            }
        }

        return true;
    }
}
