package org.wargamer2010.signshop.operations;
import org.bukkit.inventory.ItemStack;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import org.bukkit.entity.Player;
import org.wargamer2010.signshop.player.SignShopPlayer;
import org.wargamer2010.signshop.util.itemUtil;
import org.wargamer2010.signshop.util.signshopUtil;

public class SignShopArguments {
    public SignShopArguments(float pfPrice, ItemStack[] pisItems, List<Block> pContainables, List<Block> pActivatables,
                                SignShopPlayer pssPlayer, SignShopPlayer pssOwner, Block pbSign, String psOperation, BlockFace pbfBlockFace) {
        fPrice = pfPrice;
        if(pisItems != null && pisItems.length > 0)
            isItems = pisItems;
        containables = pContainables;
        activatables = pActivatables;
        if(pssPlayer != null)
            ssPlayer = pssPlayer;
        else
            ssPlayer = new SignShopPlayer((Player)null);
        if(pssOwner != null)
            ssOwner = pssOwner;
        else
            ssOwner = new SignShopPlayer((Player)null);
        bSign = pbSign;
        sOperation = psOperation;
        bfBlockFace = pbfBlockFace;
        special = new SpecialArguments();
    }

    public SignShopArguments() {

    }
    public static String seperator = "~";

    private Float fPrice = -1.0f;
    public Float get_fPrice() { return (special.bActive && special.props.fPrice != -1.0f ? special.props.fPrice : fPrice); }
    public Float get_fPrice_root() { return fPrice; }
    public void set_fPrice(Float pfPrice) { special.activate(this); special.props.fPrice = pfPrice; }
    public void set_root_fPrice(Float pfPrice) { fPrice = pfPrice; }

    private ItemStack[] isItems = null;
    public ItemStack[] get_isItems() { return (special.bActive && special.props.isItems != null ? special.props.isItems : isItems); }
    public ItemStack[] get_isItems_root() { return isItems; }
    public void set_isItems(ItemStack[] pisItems) {
        if(this.forceMessageKeys.containsKey("!items"))
            this.miscSettings.put(this.forceMessageKeys.get("!items").replace("!", ""), signshopUtil.implode(itemUtil.convertItemStacksToString(pisItems), seperator));
        special.activate(this); special.props.isItems = pisItems;
    }

    private List<Block> containables = null;
    public List<Block> get_containables() { return (special.bActive && special.props.containables != null ? special.props.containables : containables); }
    public List<Block> get_containables_root() { return containables; }
    public void set_containables(List<Block> pcontainables) { special.activate(this); special.props.containables = pcontainables; }

    private List<Block> activatables = null;
    public List<Block> get_activatables() { return (special.bActive && special.props.activatables != null ? special.props.activatables : activatables); }
    public List<Block> get_activatables_root() { return activatables; }
    public void set_activatables(List<Block> pactivatables) { special.activate(this); special.props.activatables = pactivatables; }

    private SignShopPlayer ssPlayer = null;
    public SignShopPlayer get_ssPlayer() { return (special.bActive && special.props.ssPlayer != null ? special.props.ssPlayer : ssPlayer); }
    public SignShopPlayer get_ssPlayer_root() { return ssPlayer; }
    public void set_activatables(SignShopPlayer pssPlayer) { special.activate(this); special.props.ssPlayer = pssPlayer; }

    private SignShopPlayer ssOwner = null;
    public SignShopPlayer get_ssOwner() { return (special.bActive && special.props.ssOwner != null ? special.props.ssOwner : ssOwner); }
    public SignShopPlayer get_ssOwner_root() { return ssOwner; }
    public void set_ssOwner(SignShopPlayer pssOwner) { special.activate(this); special.props.ssOwner = pssOwner; }

    private Block bSign = null;
    public Block get_bSign() { return (special.bActive && special.props.bSign != null ? special.props.bSign : bSign); }
    public Block get_bSign_root() { return bSign; }
    public void set_bSign(Block pbSign) { special.activate(this); special.props.bSign = pbSign; }

    private String sOperation = "";
    public String get_sOperation() { return (special.bActive && !special.props.sOperation.isEmpty() ? special.props.sOperation : sOperation); }
    public String get_sOperation_root() { return sOperation; }
    public void set_sOperation(String psOperation) { special.activate(this); special.props.sOperation = psOperation; }

    private String sEnchantments = "";
    public String get_sEnchantments() { return (special.bActive && !special.props.sEnchantments.isEmpty() ? special.props.sEnchantments : sEnchantments); }
    public String get_sEnchantments_root() { return sEnchantments; }
    public void set_sEnchantments(String psEnchantments) { special.activate(this); special.props.sEnchantments = psEnchantments; }

    private BlockFace bfBlockFace = null;
    public BlockFace get_bfBlockFace() { return (special.bActive && special.props.bfBlockFace != null ? special.props.bfBlockFace : bfBlockFace); }
    public BlockFace get_bfBlockFace_root() { return bfBlockFace; }
    public void set_bfBlockFace(BlockFace pbfBlockFace) { special.activate(this); special.props.bfBlockFace = pbfBlockFace; }

    private List<String> operationParameters = null;
    public void set_operationParameters(List<String> pOperationParameters) { operationParameters = pOperationParameters; }
    public boolean isOperationParameter(String sOperationParameter) { return operationParameters.contains(sOperationParameter); }
    public boolean hasOperationParameters() { return !operationParameters.isEmpty(); }
    public String getFirstOperationParameter() { return hasOperationParameters() ? operationParameters.get(0) : ""; }

    public Map<String, String> miscSettings = new HashMap<String, String>();
    public Map<String, String> forceMessageKeys = new HashMap<String, String>();
    public boolean bDoNotClearClickmap = false;
    public boolean bPriceModApplied = false;
    public boolean bRunCommandAsUser = false;

    public boolean tryToApplyPriceMod() {
        if(bPriceModApplied)
            return false;
        return (bPriceModApplied = true);
    }

    public Map<String, String> messageParts = new HashMap<String, String>();
    public void setMessagePart(String name, String value) {
        messageParts.put(name, value);
        if(forceMessageKeys.containsKey(name))
            name = forceMessageKeys.get(name);
        messageParts.put(name, value);
    }

    public SpecialArguments special = null;

    public static class SpecialArguments {
        public Boolean bActive = false;
        public SignShopArguments props = new SignShopArguments();

        private SpecialArguments() {
        }

        public void activate(SignShopArguments ssArgs) {
            if(!bActive) {
                ssArgs.special = new SpecialArguments();
                ssArgs.special.bActive = true;
            }
        }

        public void deactivate() {
            bActive = false;
        }
    }

}
