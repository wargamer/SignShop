package org.wargamer2010.signshop.operations;

import org.bukkit.Material;
import org.bukkit.block.BlockState;
import org.bukkit.material.MaterialData;
import org.bukkit.material.Lever;
import org.bukkit.block.Block;
import org.bukkit.Bukkit;
import org.wargamer2010.signshop.util.signshopUtil;
import org.wargamer2010.signshop.configuration.SignShopConfig;
import org.wargamer2010.signshop.util.lagSetter;
import org.wargamer2010.signshop.SignShop;

public class setRedStoneOnTemp implements SignShopOperation {
    @Override
    public Boolean setupOperation(SignShopArguments ssArgs) {
        Boolean foundLever = false;
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
        if(!setupOperation(ssArgs))
            return false;
        Boolean bReturn = false;
        Block bLever;

        for(int i = 0; i < ssArgs.getActivatables().get().size(); i++) {
            bLever = ssArgs.getActivatables().get().get(i);

            if(bLever.getType() == Material.getMaterial("LEVER")) {
                BlockState state = bLever.getState();
                MaterialData data = state.getData();
                Lever lever = (Lever)data;
                if(!lever.isPowered())
                    bReturn = true;
            }
        }
        if(!bReturn)
            ssArgs.sendFailedRequirementsMessage("already_on");
        return bReturn;
    }

    @Override
    public Boolean runOperation(SignShopArguments ssArgs) {
        if(!setupOperation(ssArgs))
            return false;

        Block bLever;

        Integer delay = 20;
        if(!ssArgs.getFirstOperationParameter().isEmpty()) {
            try {
                delay = Integer.parseInt(ssArgs.getFirstOperationParameter());
                if(delay <= 0)
                    delay = 20;
            } catch(NumberFormatException ex) {

            }
        }

        for(int i = 0; i < ssArgs.getActivatables().get().size(); i++) {
            bLever = ssArgs.getActivatables().get().get(i);
            if(bLever.getType() == Material.getMaterial("LEVER")) {
                BlockState state = bLever.getState();
                MaterialData data = state.getData();
                Lever lever = (Lever)data;
                if(!lever.isPowered()) {
                    lever.setPowered(true);
                    state.setData(lever);
                    state.update();
                    signshopUtil.generateInteractEvent(bLever, ssArgs.getPlayer().get().getPlayer(), ssArgs.getBlockFace().get());
                    Bukkit.getServer().getScheduler().runTaskLater(SignShop.getInstance(),new lagSetter(bLever),10*delay);
                }
            }
        }

        return true;
    }
}
