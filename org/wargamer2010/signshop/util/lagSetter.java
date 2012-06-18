package org.wargamer2010.signshop.util;

import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.material.MaterialData;
import org.bukkit.material.Lever;
import org.bukkit.Material;

public class lagSetter implements Runnable{
    private final Block blockToChange;        

    public lagSetter(Block blockToChange){
        this.blockToChange = blockToChange;
    }

    @Override
    public void run(){
        if(blockToChange.getType() == Material.LEVER) {                    
            BlockState state = blockToChange.getState();
            MaterialData data = state.getData();                                        
            Lever lever = (Lever)data;                    
            if(lever.isPowered()) {
                lever.setPowered(false);
                state.setData(lever);
                state.update();                    
            }                    
        }            
    }
}
