package org.wargamer2010.signshop.operations;

import org.bukkit.inventory.ItemStack;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import org.wargamer2010.signshop.player.SignShopPlayer;
import org.wargamer2010.signshop.util.itemUtil;

public class SignShopArguments {
    public SignShopArguments(float pfPrice, ItemStack[] pisItems, List<Block> pContainables, List<Block> pActivatables, 
                                SignShopPlayer pssPlayer, SignShopPlayer pssOwner, Block pbSign, String psOperation, BlockFace pbfBlockFace) {
        fPrice = pfPrice;
        isItems = pisItems;        
        containables = pContainables;
        activatables = pActivatables;        
        if(pssPlayer != null)
            ssPlayer = pssPlayer;
        else
            ssPlayer = new SignShopPlayer();        
        if(ssOwner != null)            
            ssOwner = pssOwner;
        else
            ssOwner = new SignShopPlayer();
        bSign = pbSign;
        sOperation = psOperation;
        bfBlockFace = pbfBlockFace;
        if(isItems != null)
            sItems = itemUtil.itemStackToString(isItems);
        special = SpecialArguments.getInstance();
    }
    public SignShopArguments() {
        
    }
    
    public float fPrice = -1.0f;
    public ItemStack[] isItems = null;
    public String sItems = "";
    public List<Block> containables = null;
    public List<Block> activatables = null;
    public SignShopPlayer ssPlayer = null;
    public SignShopPlayer ssOwner = null;
    public Block bSign = null;
    public String sOperation = "";
    public String sEnchantments = "";
    public BlockFace bfBlockFace = null;
    public Map<String, String> miscSettings = new HashMap<String, String>();
    public SpecialArguments special = null;
    
    public static class SpecialArguments {        
        public Boolean bActive = false;        
        public SignShopArguments props = new SignShopArguments();
        private static SpecialArguments instance = null;
        
        private SpecialArguments() {            
        }
        
        public static SpecialArguments getInstance() {
            if(instance == null)
                instance = new SpecialArguments();
            return instance;
        }
        
        public void activate(SignShopArguments ssArgs) {            
            if(!bActive) {
                SpecialArguments.instance = null;
                ssArgs.special = SpecialArguments.getInstance();
                ssArgs.special.bActive = true;
            }
        }
        
        public void deactivate() {
            bActive = false;
        }
    }
}
