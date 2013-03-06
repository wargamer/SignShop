package org.wargamer2010.signshop.operations;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.wargamer2010.signshop.player.SignShopPlayer;
import org.wargamer2010.signshop.util.itemUtil;
import org.wargamer2010.signshop.util.signshopUtil;

public class SignShopArguments {
    
    public SignShopArguments() {

    }
    
    public SignShopArguments(float pfPrice, ItemStack[] pisItems, List<Block> pContainables, List<Block> pActivatables,
                                SignShopPlayer pssPlayer, SignShopPlayer pssOwner, Block pbSign, String psOperation, BlockFace pbfBlockFace) {
        fPrice.setRoot(pfPrice);        
        isItems.setRoot(pisItems);
        containables.setRoot(pContainables);
        activatables.setRoot(pActivatables);
        if(pssPlayer != null)
            ssPlayer.setRoot(pssPlayer);
        else
            ssPlayer.setRoot(new SignShopPlayer((Player)null));
        if(pssOwner != null)
            ssOwner.setRoot(pssOwner);
        else
            ssOwner.setRoot(new SignShopPlayer((Player)null));
        bSign.setRoot(pbSign);
        sOperation.setRoot(psOperation);
        bfBlockFace.setRoot(pbfBlockFace);
    }
    
    public void reset() {
        fPrice.setSpecial(false);
        isItems.setSpecial(false);
        containables.setSpecial(false);
        activatables.setSpecial(false);
        ssPlayer.setSpecial(false);
        ssOwner.setSpecial(false);
        bSign.setSpecial(false);
        sOperation.setSpecial(false);
        bfBlockFace.setSpecial(false);
    }
    
    public static String seperator = "~";

    private SignShopArgument<Float> fPrice = new SignShopArgument<Float>(this);
    public SignShopArgument<Float> getPrice() {
        return fPrice;
    }
    
    private SignShopArgument<ItemStack[]> isItems = new SignShopArgument<ItemStack[]>(this) {
        @Override
        public void set(ItemStack[] pItems) { 
            if(getCollection().forceMessageKeys.containsKey("!items"))
                getCollection().miscSettings.put(getCollection().forceMessageKeys.get("!items").replace("!", ""), 
                        signshopUtil.implode(itemUtil.convertItemStacksToString(pItems), seperator));
            super.set(pItems);
        }
    };
    public SignShopArgument<ItemStack[]> getItems() {
        return isItems;
    }

    private SignShopArgument<List<Block>> containables = new SignShopArgument<List<Block>>(this);
    public SignShopArgument<List<Block>> getContainables() {
        return containables;
    }
    
    private SignShopArgument<List<Block>> activatables = new SignShopArgument<List<Block>>(this);
    public SignShopArgument<List<Block>> getActivatables() {
        return activatables;
    }

    private SignShopArgument<SignShopPlayer> ssPlayer = new SignShopArgument<SignShopPlayer>(this);
    public SignShopArgument<SignShopPlayer> getPlayer() {
        return ssPlayer;
    }

    private SignShopArgument<SignShopPlayer> ssOwner = new SignShopArgument<SignShopPlayer>(this);
    public SignShopArgument<SignShopPlayer> getOwner() {
        return ssOwner;
    }

    private SignShopArgument<Block> bSign = new SignShopArgument<Block>(this);
    public SignShopArgument<Block> getSign() {
        return bSign;
    }

    private SignShopArgument<String> sOperation = new SignShopArgument<String>(this);
    public SignShopArgument<String> getOperation() {
        return sOperation;
    }

    private SignShopArgument<String> sEnchantments = new SignShopArgument<String>(this);
    public SignShopArgument<String> getEnchantments() {
        return sEnchantments;
    }

    private SignShopArgument<BlockFace> bfBlockFace = new SignShopArgument<BlockFace>(this);
    public SignShopArgument<BlockFace> getBlockFace() {
        return bfBlockFace;
    }
    
    private List<String> operationParameters = new LinkedList<String>();
    public void setOperationParameters(List<String> pOperationParameters) { operationParameters.clear(); operationParameters.addAll(pOperationParameters); }
    public boolean isOperationParameter(String sOperationParameter) { return operationParameters.contains(sOperationParameter); }
    public boolean hasOperationParameters() { return !operationParameters.isEmpty(); }
    public String getFirstOperationParameter() { return hasOperationParameters() ? operationParameters.get(0) : ""; }

    public Map<String, String> miscSettings = new HashMap<String, String>();
    public Map<String, String> forceMessageKeys = new HashMap<String, String>();
    public boolean bDoNotClearClickmap = false;
    public boolean bPriceModApplied = false;
    public boolean bRunCommandAsUser = false;
    
    public void ignoreEmptyChest() {
        if(!isOperationParameter("allowemptychest"))
            operationParameters.add("allowemptychest");
    }
    
    public boolean tryToApplyPriceMod() {
        if(bPriceModApplied)
            return false;
        return (bPriceModApplied = true);
    }

    private Map<String, String> messageParts = new HashMap<String, String>();
    
    public void setMessagePart(String name, String value) {
        messageParts.put(name, value);
        if(forceMessageKeys.containsKey(name))
            name = forceMessageKeys.get(name);
        messageParts.put(name, value);
    }
    
    public boolean hasMessagePart(String name) {
        return messageParts.containsKey(name);
    }
    
    public String getMessagePart(String name) {
        if(hasMessagePart(name))
            return messageParts.get(name);
        return "";
    }
    
    public Map<String, String> getMessageParts() {
        return Collections.unmodifiableMap(messageParts);
    }
    
    public Map<String, String> getRawMessageParts() {
        return messageParts;
    }
}
