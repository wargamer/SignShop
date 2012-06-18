package org.wargamer2010.signshop.operations;

import org.bukkit.inventory.ItemStack;
import org.wargamer2010.signshop.util.itemUtil;
import java.util.Random;

public class RandomItem implements SignShopOperation {    
    @Override
    public Boolean setupOperation(SignShopArguments ssArgs) {        
        return true;
    }
    
    @Override
    public Boolean checkRequirements(SignShopArguments ssArgs, Boolean activeCheck) {                
        ssArgs.special.activate(ssArgs);
        if(ssArgs.special.props.isItems == null)
            ssArgs.special.props.isItems = new ItemStack[1];
        return true;
    }
    
    @Override
    public Boolean runOperation(SignShopArguments ssArgs) {                
        ItemStack isRandom = ssArgs.isItems[(new Random()).nextInt(ssArgs.isItems.length)];
        ItemStack isRandoms[] = new ItemStack[1]; isRandoms[0] = isRandom;                
        ssArgs.special.activate(ssArgs);
        ssArgs.special.props.isItems = isRandoms;
        ssArgs.special.props.sItems = itemUtil.itemStackToString(isRandoms);
        return true;
    }
}
