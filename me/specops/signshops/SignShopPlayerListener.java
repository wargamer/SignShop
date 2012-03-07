package me.specops.signshops;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import java.util.Map;
import java.util.HashMap;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.block.Chest;
import org.bukkit.inventory.ItemStack;
import java.util.List;
import java.util.ArrayList;
import org.bukkit.Location;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import org.bukkit.Bukkit;
import org.bukkit.material.MaterialData;
import org.bukkit.event.block.Action;
import java.util.Random;
import org.bukkit.inventory.PlayerInventory;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.material.SimpleAttachableMaterialData;
import org.bukkit.material.Lever;
import org.bukkit.block.BlockState;
import org.bukkit.inventory.Inventory;

//TODO: copy durability of tools over when buying/selling items. 

public class SignShopPlayerListener implements Listener {
    private final SignShop plugin;
    private static Map<String, Location> mClicks  = new HashMap<String,Location>();
    private static Map<String,Location> mConfirms = new HashMap<String,Location>();
    private static HashMap<Integer, String> discs;

    private int takePlayerMoney = 1;
    private int givePlayerMoney = 2;
    private int takePlayerItems = 3;
    private int givePlayerItems = 4;
    private int takeOwnerMoney = 5;
    private int giveOwnerMoney = 6;
    private int takeShopItems = 7;
    private int giveShopItems = 8;
    private int givePlayerRandomItem = 10;
    private int playerIsOp = 11;
    private int setDayTime = 12;
    private int setNightTime = 13;
    private int setRaining = 14;
    private int setClearSkies = 16;
    private int setRedstoneOn = 17;
    private int setRedstoneOff = 18;
    private int setRedStoneOnTemp = 19;
    private int toggleRedstone = 20;
    private int usesChest = 21;
    private int usesLever = 22;
    private int healPlayer = 23;
    private int repairPlayerHeldItem = 24;

    public SignShopPlayerListener(SignShop instance){
        this.plugin = instance;
        initDiscs();
    }
    
    private void initDiscs() {
        // This is pretty ugly but I really couldn't find another way in
        // bukkit's source to get this via a native function
        // Source: http://www.minecraftwiki.net/wiki/Data_values
        discs = new HashMap<Integer, String>();
        discs.put(2256, "13 Disc");
        discs.put(2257, "Cat Disc");
        discs.put(2258, "Blocks Disc");
        discs.put(2259, "Chirp Disc");
        discs.put(2260, "Far Disc");
        discs.put(2261, "Mall Disc");
        discs.put(2262, "Mellohi Disc");
        discs.put(2263, "Stal Disc");
        discs.put(2264, "Strad Disc");
        discs.put(2265, "Ward Disc");
        discs.put(2266, "11 Disc");
    }

    public static String getOperation(String sSignOperation){
        if(sSignOperation.length() < 3){
            return "";
        }        
        sSignOperation = ChatColor.stripColor(sSignOperation);
        return sSignOperation.substring(1,sSignOperation.length()-1);
    }

    private String getMessage(String sType,String sOperation,String sItems,float fPrice,String sCustomer,String sOwner){
        if(!SignShop.Messages.get(sType).containsKey(sOperation) || SignShop.Messages.get(sType).get(sOperation) == null){
            return "";
        }
        return SignShop.Messages.get(sType).get(sOperation)
            .replace("\\!","!")
            .replace("!price", this.formatMoney(fPrice))
            .replace("!items", sItems)
            .replace("!customer", sCustomer)
            .replace("!owner", sOwner);
    }

    //msg a player object
    private void msg(Player player,String msg){
        if(msg == null || msg.trim().equals("")){
            return;
        }
        player.sendMessage(ChatColor.GOLD+"[SignShop] "+ChatColor.WHITE+msg);
    }

    private void msg(List<Player> players,String msg){
        for(Player player : players){
            msg(player,msg);
        }
    }

    //look up a player by player.getName()
    private boolean msg(String sPlayer,String msg){
        Player[] players = Bukkit.getServer().getOnlinePlayers();

        for(Player player : players){
            if(player.getName().equals(sPlayer)){
                msg(player,msg);
                return true;
            }
        }
        return false;
    }
    
    private Boolean checkDistance(Block a, Block b, int maxdistance) {
        if(maxdistance <= 0)
            return true;
        int xdiff = Math.abs(a.getX() - b.getX());
        int ydiff = Math.abs(a.getY() - b.getY());
        int zdiff = Math.abs(a.getZ() - b.getZ());
        if(xdiff > maxdistance || ydiff > maxdistance || zdiff > maxdistance)
            return false;
        else
            return true;
    }
    
    private String lookupDisc(int id) {        
        if(discs.containsKey(id))
            return discs.get(id);
        else
            return "";
    }
    
    private String formatData(MaterialData data) {
        // For some reason running tostring on data when it's from an attachable material
        // will cause a NullPointerException, thus if we're dealing with an attachable, go the easy way :)
        if(data instanceof SimpleAttachableMaterialData)
            return stringFormat(data.getItemType());
        String sData;
        if(!(sData = lookupDisc(data.getItemTypeId())).equals(""))            
            return sData;
        else
            sData = data.toString().toLowerCase();
        Pattern p = Pattern.compile("\\(-?[0-9]+\\)");
        Matcher m = p.matcher(sData);
        sData = m.replaceAll("");
        sData = sData.replace("_", " ");
                
        StringBuffer sb = new StringBuffer(sData.length());
        p = Pattern.compile("(^|\\W)([a-z])");
        m = p.matcher(sData);
        while(m.find()) {
            m.appendReplacement(sb, m.group(1) + m.group(2).toUpperCase() );
        }
        
        m.appendTail(sb);
        
        return sb.toString();
    }

    private String stringFormat(Material material){
        String sMaterial = material.name().replace("_"," ");
        Pattern p = Pattern.compile("(^|\\W)([a-z])");
        Matcher m = p.matcher(sMaterial.toLowerCase());
        StringBuffer sb = new StringBuffer(sMaterial.length());

        while(m.find()){
            m.appendReplacement(sb, m.group(1) + m.group(2).toUpperCase() );
        }

        m.appendTail(sb);

        return sb.toString();
    }
    
    private String formatMoney(double money) {
        if(Vault.economy == null)
            return Double.toString(money);
        else
            return Vault.economy.format(money);
    }
    
    private Boolean hasPerm(Player player, String perm) {        
        if(Vault.permission == null) {
            return true;            
        }
        Boolean isOP = player.isOp();        
        if(plugin.USE_PERMISSIONS && isOP)
            player.setOp(false);
        if(plugin.USE_PERMISSIONS && Vault.permission.playerHas(player, perm)) {
            player.setOp(isOP);
            return true;
        } else if(!plugin.USE_PERMISSIONS && isOP) {
            return true;
        }
        player.setOp(isOP);
        return false;           
    }
    
    private Boolean hasMoney(String player, float amount) {
        if(Vault.economy == null)
            return false;
        else
            return Vault.economy.has(player, amount);
    }
    
    private Boolean mutateMoney(String player, float amount) {
        if(Vault.economy == null)
            return false;
        EconomyResponse response;
        if(amount > 0.0)
            response = Vault.economy.depositPlayer(player, amount);
        else if(amount < 0.0)
            response = Vault.economy.withdrawPlayer(player, Math.abs(amount));
        else
            return true;
        if(response.type == EconomyResponse.ResponseType.SUCCESS)
            return true;
        else
            return false;
    }
    
    private float parsePrice(String priceline) {
        String sPrice = "";
        float fPrice = 0.0f;
        for(int i = 0; i < priceline.length(); i++)
            if(Character.isDigit(priceline.charAt(i)) || priceline.charAt(i) == '.')
                sPrice += priceline.charAt(i);
        try {
            fPrice = Float.parseFloat(sPrice);
        }
        catch(NumberFormatException nFE) {
            fPrice = 0.0f;
        }
        if(fPrice < 0.0f) {
            fPrice = 0.0f;
        }
        return fPrice;
    }
    
    private Integer convertChestshop(Block sign, Player admin, Boolean alter, Block emptySign) {
        String[] sLines = ((Sign) sign.getState()).getLines();
        Integer iAmount = -1;
        Integer iPrice = -1;
        String sPrice = "";
        String sAmount = sLines[1];
        String sMaterial = sLines[3].toUpperCase().replace(" ", "_");
        if(!admin.isOp())
            return iAmount;        
        if(Material.getMaterial(sMaterial) == null)
            return iAmount;
        try {
            iAmount = Integer.parseInt(sLines[1]);
        } catch(NumberFormatException e) {                        
            return -1;
        }
        if(alter) {
            Integer from;
            Integer to;
            Sign signblock = ((Sign)sign.getState());
            Sign emptyBlock = null;
            if(emptySign != null)
                emptyBlock = ((Sign)emptySign.getState());
            if((sLines[2].contains("B")) && sLines[2].contains("S")) {
                if(emptyBlock == null) {
                    admin.sendMessage("Punch an empty sign first!");
                    return -1;
                }
                if(sLines[2].indexOf(":") == -1)
                    return -1;
                String bits[] = sLines[2].split(":");
                if(bits[0].contains("S"))
                    iPrice = Math.round(parsePrice(bits[0]));
                else if(bits[1].contains("S"))
                    iPrice = Math.round(parsePrice(bits[1]));
                else
                    return -1;
                sPrice = Integer.toString(iPrice);
                
                emptyBlock.setLine(0, "[Sell]");
                emptyBlock.setLine(1, (sAmount + " of"));
                emptyBlock.setLine(2, sLines[3]);
                emptyBlock.setLine(3, sPrice);
                emptyBlock.update();
                
                if(bits[0].contains("B"))
                    iPrice = Math.round(parsePrice(bits[0]));
                else if(bits[1].contains("B"))
                    iPrice = Math.round(parsePrice(bits[1]));
                else
                    return -1;
                sPrice = Integer.toString(iPrice);
                signblock.setLine(0, "[Buy]");
            } else if(sLines[2].contains("B")) {                
                from = sLines[2].indexOf("B");
                if(sLines[2].indexOf(":", from+2) > from+2)
                    to = sLines[2].indexOf(":", from+2);
                else if(sLines[2].indexOf(" ", from+2) > from+2)
                    to = sLines[2].indexOf(" ", from+2);
                else
                    to = sLines[2].length();
                sPrice = sLines[2].substring(from+2, to);
                try {
                    iPrice = Integer.parseInt(sPrice);
                } catch(NumberFormatException e) {
                    return -1;
                }                
                signblock.setLine(0, "[Buy]");
            } else if(sLines[2].contains("S")) {
                from = sLines[2].indexOf("S");
                if(sLines[2].indexOf(":", from+2) > from+2)
                    to = sLines[2].indexOf(":", from+2);
                else if(sLines[2].indexOf(" ", from+2) > from+2)
                    to = sLines[2].indexOf(" ", from+2);
                else
                    to = sLines[2].length();
                sPrice = sLines[2].substring(from+2, to);
                try {
                    iPrice = Integer.parseInt(sPrice);
                } catch(NumberFormatException e) {                    
                    return -1;
                }                
                signblock.setLine(0, "[Sell]");
            } else
                return -1;
            signblock.setLine(1, (sAmount + " of"));
            signblock.setLine(2, sLines[3]);
            signblock.setLine(3, sPrice);
            signblock.update();
        }        
        return iAmount;
    }
    
    private Boolean emptySign(Block sign) {
        String[] sLines = ((Sign) sign.getState()).getLines();
        for(int i = 0; i < 4; i++)
            if(!sLines[i].equals(""))
                return false;
        return true;
    }
    
    public static void setSignStatus(Block sign, ChatColor color) {
        if(sign.getType() == Material.SIGN_POST || sign.getType() == Material.WALL_SIGN) {
            Sign signblock = ((Sign) sign.getState());
            String[] sLines = signblock.getLines();            
            if(sLines[0].length() < 14)
                signblock.setLine(0, (color + ChatColor.stripColor(sLines[0])));
            signblock.update();            
        }
    }
    
    private Boolean registerChest(Block bSign, Block bChest, String sOperation, Player player, String playerName) {
        String[] sLines = ((Sign) bSign.getState()).getLines();
        if(!checkDistance(bSign, bChest, plugin.getMaxSellDistance())) {
            msg(player, SignShop.Errors.get("too_far").replace("!max", Integer.toString(plugin.getMaxSellDistance())));
            setSignStatus(bSign, ChatColor.BLACK);
            return false;
        }        
        if(plugin.getMaxShopsPerPerson() != 0 && SignShop.Storage.countLocations(player.getName()) >= plugin.getMaxShopsPerPerson()) {
            msg(player, SignShop.Errors.get("too_many_shops").replace("!max", Integer.toString(plugin.getMaxShopsPerPerson())));
            setSignStatus(bSign, ChatColor.BLACK);
            return false;
        }
        
        List operation = SignShop.Operations.get(sOperation);

        if(operation.contains(playerIsOp) && !hasPerm(player, ("SignShop.Admin."+sOperation))) {
            msg(player,SignShop.Errors.get("no_permission"));
            setSignStatus(bSign, ChatColor.BLACK);
            return false;
        } else if(!operation.contains(playerIsOp) && !hasPerm(player, ("SignShop.Signs."+sOperation))) {
            msg(player,SignShop.Errors.get("no_permission"));
            setSignStatus(bSign, ChatColor.BLACK);
            return false;
        }

        float fPrice = parsePrice(sLines[3]);

        // Redstone operation
        if(operation.contains(usesLever) && bChest.getType() == Material.LEVER){
            msg(player,getMessage("setup",sOperation,"",fPrice,"",""));

            SignShop.Storage.addSeller(playerName,bSign,bChest,new ItemStack[]{new ItemStack(Material.DIRT,1)});
            setSignStatus(bSign, ChatColor.GREEN);
            mClicks.remove(player.getName());
            return true;
        // Chest operation
        }else if(operation.contains(usesChest) && bChest.getType() == Material.CHEST){
            // Chest items
            Chest cbChest = (Chest) bChest.getState();
            ItemStack[] isChestItems = cbChest.getInventory().getContents();

            //remove extra values
            List<ItemStack> tempItems = new ArrayList<ItemStack>();
            for(ItemStack item : isChestItems) {
                if(item != null && item.getAmount() > 0) {
                    tempItems.add(item);
                }
            }
            isChestItems = tempItems.toArray(new ItemStack[tempItems.size()]);

            // Make sure the chest wasn't empty, if dealing with an operation that uses items
            if(operation.contains(usesChest)){
                if(isChestItems.length == 0){
                    msg(player, SignShop.Errors.get("chest_empty"));
                    return false;
                }
            }

            String sItems = "";
            Boolean first = true;
            for(ItemStack item: isChestItems) {
                if(first) first = false;
                else sItems += ", ";
                sItems += item.getAmount()+" "+formatData(item.getData());
            }

            msg(player,getMessage("setup",sOperation,sItems,fPrice,"",playerName));

            SignShop.Storage.addSeller(playerName,bSign,bChest,isChestItems);
            setSignStatus(bSign, ChatColor.GREEN);
            mClicks.remove(player.getName());
            return true;
        }
        return false;
    }
    
    private Boolean isOutofStock(Inventory iiInventory, ItemStack[] isItemsToTake) {
        ItemStack[] isChestItems = iiInventory.getContents();
        ItemStack[] isBackup = new ItemStack[isChestItems.length];
        for(int i=0;i<isChestItems.length;i++){
            if(isChestItems[i] != null){
                isBackup[i] = new ItemStack(
                    isChestItems[i].getType(),
                    isChestItems[i].getAmount(),
                    isChestItems[i].getDurability()
                );
                isBackup[i].addEnchantments(isChestItems[i].getEnchantments());
                if(isChestItems[i].getData() != null){
                    isBackup[i].setData(isChestItems[i].getData());
                }
            }
        }
        HashMap<Integer, ItemStack> leftOver = iiInventory.removeItem(isItemsToTake);
        Boolean bOutofstock = false;
        if(!leftOver.isEmpty())
            bOutofstock = true;
        iiInventory.setContents(isBackup);        
        return bOutofstock;
    }
    
    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerInteract(PlayerInteractEvent event) {
        // Respect protection plugins
        if(event.getClickedBlock() == null
        || event.isCancelled()){
            return;
        }        
        // Initialize needed variables
        Block bClicked = event.getClickedBlock();
        Player player = event.getPlayer();
        String[] sLines;
        String sOperation;        
        
        // Clicked a sign with redstone
        if(event.getItem() != null && event.getItem().getType() == Material.REDSTONE){
            if(bClicked.getType() == Material.SIGN_POST
            || bClicked.getType() == Material.WALL_SIGN) {
                sLines = ((Sign) bClicked.getState()).getLines();
                // TODO: save the sign operation with signs to avoid sign changing issues
                sOperation = getOperation(sLines[0]);
                
                // Verify this isn't a shop already
                if(SignShop.Storage.getSeller(event.getClickedBlock().getLocation()) != null){
                    return;
                }
                
                // Verify the operation
                if(!SignShop.Operations.containsKey(sOperation)){                    
                    if(!emptySign(bClicked) && (convertChestshop(bClicked, player, false, null)) >= 0) {                        
                        if(sLines[2].contains("B") && sLines[2].contains("S")) {
                            msg(player, "Chestshop sign detected, both Buy and Sell. Please punch an empty sign so both a Buy and Sell sign can be created.");
                            mClicks.put(player.getName(), event.getClickedBlock().getLocation());
                            return;
                        } else if(sLines[2].contains("B"))
                            sOperation =  "Buy";
                        else if(sLines[2].contains("S"))
                            sOperation =  "Sell";
                        else {
                            msg(event.getPlayer(),SignShop.Errors.get("invalid_operation"));
                            return;
                        }
                        msg(player, "Chestshop sign detected, ready for conversion, please adjust the chest's contents to the amount for 1 transaction.");
                        msg(player, "Then link a chest and put back the rest of the items.");
                    } else if(emptySign(bClicked) && mClicks.containsKey(event.getPlayer().getName())) {
                        Block lastClicked = mClicks.get(player.getName()).getBlock();
                        
                        if((lastClicked.getType() == Material.SIGN_POST || lastClicked.getType() == Material.WALL_SIGN) 
                                && convertChestshop(lastClicked, player, false, null) >= 0) {
                            msg(player, "Empty sign detected, ready for conversion, please adjust the chest's contents to the amount for 1 transaction.");
                            msg(player, "Then link a chest and put back the rest of the items. This will allow the chest to be used for both Buy and Sell.");
                            mClicks.put((player.getName() + "_empty"), event.getClickedBlock().getLocation());
                            return;
                        } else {
                            msg(event.getPlayer(),SignShop.Errors.get("invalid_operation"));
                            return;
                        }
                    } else {                        
                        msg(event.getPlayer(),SignShop.Errors.get("invalid_operation"));
                        return;
                    }
                }
                List operation = SignShop.Operations.get(sOperation);
                                
                if(operation.contains(playerIsOp) && !hasPerm(player, ("SignShop.Admin."+sOperation))) {
                    msg(event.getPlayer(),SignShop.Errors.get("no_permission"));
                    return;
                } else if(operation.contains(playerIsOp) && !hasPerm(player, ("SignShop.Signs."+sOperation))) {
                    msg(event.getPlayer(),SignShop.Errors.get("no_permission"));
                    return;
                }

                float fPrice = parsePrice(sLines[3]);
                
                // Does this sign have a chest/lever counterpart?
                if(operation.contains(usesChest) || operation.contains(usesLever)){
                    mClicks.put(event.getPlayer().getName(),event.getClickedBlock().getLocation());
                    msg(event.getPlayer(),SignShop.Errors.get("sign_location_stored"));
                    return;
                // Standalone operation
                } else {
                    SignShop.Storage.addSeller(event.getPlayer().getName(),event.getClickedBlock(),event.getClickedBlock(),new ItemStack[]{new ItemStack(Material.DIRT,1)});
                    msg(event.getPlayer(),getMessage("setup",sOperation,"",fPrice,"",event.getPlayer().getName()));
                    return;
                }
            }
        // Left clicked a chest and has already clicked a sign
        }
        if(event.getAction() == Action.LEFT_CLICK_BLOCK
        && (event.getClickedBlock().getType() == Material.CHEST || event.getClickedBlock().getType() == Material.LEVER)
        && mClicks.containsKey(event.getPlayer().getName())){

            Block bSign = mClicks.get(event.getPlayer().getName()).getBlock();
            sLines = ((Sign) bSign.getState()).getLines();
            sOperation = getOperation(sLines[0]);
            String playerName = event.getPlayer().getName();
            Boolean registerEmptySign = false;
            Block emptySign = null;
            
            // Verify the operation
            if(!SignShop.Operations.containsKey(sOperation)){
                playerName = sLines[0];                
                if(mClicks.containsKey(event.getPlayer().getName() + "_empty")) {
                    emptySign = mClicks.get(event.getPlayer().getName() + "_empty").getBlock();
                    if((sLines[2].contains("B")) && sLines[2].contains("S")) {
                        registerEmptySign = true;
                    }
                }
                if((convertChestshop(bSign, player, true, emptySign)) >= 0) {                    
                    sLines = ((Sign) bSign.getState()).getLines();
                    sOperation = getOperation(sLines[0]);
                    if(!SignShop.Operations.containsKey(sOperation)) {                        
                        msg(event.getPlayer(),SignShop.Errors.get("invalid_operation"));
                        return;
                    }
                    msg(player, "Sign succesfully converted!");                                        
                } else {                    
                    msg(event.getPlayer(),SignShop.Errors.get("invalid_operation"));
                    return;
                }
            }
            registerChest(bSign, event.getClickedBlock(), sOperation, player, playerName);
            if(registerEmptySign) {
                registerChest(emptySign, event.getClickedBlock(), getOperation(((Sign) emptySign.getState()).getLines()[0]), player, playerName);
            }
            return;
            
        // Clicked on a sign, might be a signshop.
        } 
        if(bClicked.getType() == Material.SIGN_POST || bClicked.getType() == Material.WALL_SIGN){
            Seller seller = SignShop.Storage.getSeller(bClicked.getLocation());

            sLines = ((Sign) bClicked.getState()).getLines();
            sOperation = getOperation(sLines[0]);

            // Verify the operation
            if(!SignShop.Operations.containsKey(sOperation)){
                return;
            }
            List operation = SignShop.Operations.get(sOperation);
            
            // Verify seller at this location
            if(seller == null){
                return;
            }
            
            if(hasPerm(player, ("SignShop.DenyUse."+sOperation)) && !hasPerm(player, ("SignShop.Signs."+sOperation)) && !hasPerm(player, ("SignShop.Admin."+sOperation))) {
                msg(event.getPlayer(),SignShop.Errors.get("no_permission_use"));
                return;
            }
            
            float fPrice = parsePrice(sLines[3]);

            // Setup items
            ItemStack[] isItems = seller.getItems();

            String sItems = "";
            Boolean first = true;
            for(ItemStack item: isItems) {
                if(first) first = false;
                else sItems += ", ";
                sItems += item.getAmount()+" "+formatData(item.getData());
            }
            
            //Make sure the money is there            
            if(operation.contains(takePlayerMoney)) {
                if(!hasMoney(player.getName(), fPrice)) {
                    msg(event.getPlayer(),SignShop.Errors.get("no_player_money").replace("!price",this.formatMoney(fPrice)));
                    return;
                }
            }

            if(operation.contains(takeOwnerMoney)){
                if(!hasMoney(seller.owner, fPrice)) {
                    msg(event.getPlayer(),SignShop.Errors.get("no_shop_money").replace("!price",this.formatMoney(fPrice)));
                    return;
                }
            }

            // Make sure the items are there
            // Due to the standalone nature of the statements these need to be defined here
            Chest cbChest = null;
            ItemStack[] isChestItems = null;
            ItemStack[] isChestItemsBackup = null;

            ItemStack[] isPlayerItems = event.getPlayer().getInventory().getContents();
            ItemStack[] isPlayerItemsBackup = new ItemStack[isPlayerItems.length];

            HashMap<Integer,ItemStack> iiItemsLeftover;

            if(operation.contains(usesChest)){
                if(seller.getChest().getType() != Material.CHEST){
                    msg(event.getPlayer(),SignShop.Errors.get("out_of_business"));
                    return;
                }

                cbChest = (Chest) seller.getChest().getState();
                isChestItems = cbChest.getInventory().getContents();                
                isChestItemsBackup = new ItemStack[isChestItems.length];
                for(int i=0;i<isChestItems.length;i++){
                    if(isChestItems[i] != null){
                        isChestItemsBackup[i] = new ItemStack(
                            isChestItems[i].getType(),
                            isChestItems[i].getAmount(),
                            isChestItems[i].getDurability()
                        );
                        isChestItemsBackup[i].addEnchantments(isChestItems[i].getEnchantments());
                        if(isChestItems[i].getData() != null){
                            isChestItemsBackup[i].setData(isChestItems[i].getData());
                        }
                    }
                }

                for(int i=0;i<isPlayerItems.length;i++){
                    if(isPlayerItems[i] != null){
                        isPlayerItemsBackup[i] = new ItemStack(
                            isPlayerItems[i].getType(),
                            isPlayerItems[i].getAmount(),
                            isPlayerItems[i].getDurability()
                        );
                        isPlayerItemsBackup[i].addEnchantments(isPlayerItems[i].getEnchantments());

                        if(isPlayerItems[i].getData() != null){
                            isPlayerItemsBackup[i].setData(isPlayerItems[i].getData());
                        }
                    }
                }

                if(operation.contains(takePlayerItems)){
                    if(isOutofStock(player.getInventory(), isItems)) {
                        msg(event.getPlayer(),SignShop.Errors.get("player_doesnt_have_items").replace("!items", sItems));
                        return;
                    }                    
                }

                if(operation.contains(takeShopItems)){
                    if(isOutofStock(cbChest.getInventory(), isItems)) {
                        setSignStatus(bClicked, ChatColor.RED);
                        msg(event.getPlayer(),SignShop.Errors.get("out_of_stock"));
                        return;
                    } else {
                        setSignStatus(bClicked, ChatColor.GREEN);
                    }
                }

                //Make sure the shop has room
                if(operation.contains(giveShopItems)){
                    iiItemsLeftover = cbChest.getInventory().addItem(isItems);

                    if(!(iiItemsLeftover).isEmpty()){
                        //reset chest inventory
                        cbChest.getInventory().setContents(isChestItemsBackup);

                        msg(event.getPlayer(),SignShop.Errors.get("overstocked"));

                        return;
                    }
                    //every operation step needs to be self cleaning
                    cbChest.getInventory().setContents(isChestItemsBackup);
                }
            }

            //Make sure the item can be repaired
            if(operation.contains(repairPlayerHeldItem)){
                if(event.getItem() == null) {
                    msg(event.getPlayer(),SignShop.Errors.get("no_item_to_repair"));
                    return;
                } else if(event.getItem().getType().getMaxDurability() < 30) {
                    msg(event.getPlayer(),SignShop.Errors.get("invalid_item_to_repair"));
                    return;
                }
            }
            
            // Have they seen the confirm message? (right click skips)
            if(event.getAction() == Action.LEFT_CLICK_BLOCK
            &&(!mConfirms.containsKey(event.getPlayer().getName())
                || mConfirms.get(event.getPlayer().getName()).getBlock() != bClicked)
            ){
                msg(event.getPlayer(),getMessage("confirm",sOperation,sItems,fPrice,event.getPlayer().getName(),seller.owner));

                mConfirms.put(event.getPlayer().getName(),bClicked.getLocation());

                return;
            }
            
            mConfirms.remove(event.getPlayer().getName());
            
            // Item giving/taking
            if(operation.contains(givePlayerItems)){
                PlayerInventory inv = event.getPlayer().getInventory();
                inv.addItem(isItems);
            }
            if(operation.contains(takePlayerItems)){
                event.getPlayer().getInventory().removeItem(isItems);
            }

            if(operation.contains(giveShopItems)){
                cbChest.getInventory().addItem(isItems);                
            }
            if(operation.contains(takeShopItems)){
                cbChest.getInventory().removeItem(isItems);                
                if(isOutofStock(cbChest.getInventory(), isItems))
                    setSignStatus(bClicked, ChatColor.RED);
                else
                    setSignStatus(bClicked, ChatColor.GREEN);
            }

            // Health
            if(operation.contains(healPlayer)){
                event.getPlayer().setHealth(20);
            }

            // Item Repair
            if(operation.contains(repairPlayerHeldItem)){
                event.getPlayer().getItemInHand().setDurability((short) 0);
            }
            

            // Weather Operations
            if(operation.contains(setDayTime)){
                event.getPlayer().getWorld().setTime(0);

                msg(event.getPlayer().getWorld().getPlayers(),SignShop.Errors.get("made_day").replace("!player",event.getPlayer().getDisplayName()));

            }else if(operation.contains(setNightTime)){
                long curtime = player.getWorld().getTime();
                player.getWorld().setTime((curtime + 14000));
                msg(event.getPlayer().getWorld().getPlayers(),SignShop.Errors.get("made_night").replace("!player",event.getPlayer().getDisplayName()));
            }
            
            if(operation.contains(setRaining)){
                event.getPlayer().getWorld().setStorm(true);
                event.getPlayer().getWorld().setThundering(true);

                msg(event.getPlayer().getWorld().getPlayers(),SignShop.Errors.get("made_rain").replace("!player",event.getPlayer().getDisplayName()));
            }else if(operation.contains(setClearSkies)){
                event.getPlayer().getWorld().setStorm(false);
                event.getPlayer().getWorld().setThundering(false);

                msg(event.getPlayer().getWorld().getPlayers(),SignShop.Errors.get("made_clear_skies").replace("!player",event.getPlayer().getDisplayName()));
            }

            // Redstone operations
            if(operation.contains(setRedstoneOn)){
                Block bLever = seller.getChest();

                if(bLever.getType() == Material.LEVER){
                    int iData = (int) bLever.getData();

                    if((iData&0x08) != 0x08){                        
                        iData|=0x08;//send power on
                        bLever.setData((byte) iData);
                        bLever.getState().update(true);
                    } else {
                        msg(event.getPlayer(),SignShop.Errors.get("already_on"));
                        return;
                    }
                }
            }else if(operation.contains(setRedstoneOff)){
                Block bLever = seller.getChest();

                if(bLever.getType() == Material.LEVER) {                    
                    BlockState state = bLever.getState();
                    MaterialData data = state.getData();                                        
                    Lever lever = (Lever)data;                    
                    if(lever.isPowered()) {
                        lever.setPowered(false);
                        state.setData(lever);
                        state.update();
                    } else { 
                        msg(event.getPlayer(),SignShop.Errors.get("already_off"));
                        return;
                    }                    
                }
            }else if(operation.contains(setRedStoneOnTemp)){
                Block bLever = seller.getChest();

                if(bLever.getType() == Material.LEVER){
                    BlockState state = bLever.getState();
                    MaterialData data = state.getData();                                        
                    Lever lever = (Lever)data;                    
                    if(!lever.isPowered()) {
                        lever.setPowered(true);
                        state.setData(lever);
                        state.update();
                    } else { 
                        msg(event.getPlayer(),SignShop.Errors.get("already_on"));
                        return;
                    }     
                    
                    plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin,new lagSetter(bLever),10*20);
                }
            }else if(operation.contains(toggleRedstone)){
                Block bLever = seller.getChest();

                if(bLever.getType() == Material.LEVER){
                    BlockState state = bLever.getState();
                    MaterialData data = state.getData();                                        
                    Lever lever = (Lever)data;                    
                    if(!lever.isPowered())
                        lever.setPowered(true);                        
                    else
                        lever.setPowered(false);                        
                    state.setData(lever);
                    state.update();                    
                }
            }

            if(operation.contains(givePlayerRandomItem)){
                ItemStack isRandom = isItems[(new Random()).nextInt(isItems.length)];

                iiItemsLeftover = cbChest.getInventory().removeItem(isRandom);

                if(!iiItemsLeftover.isEmpty()){
                    //reset chest inventory
                    cbChest.getInventory().setContents(isChestItemsBackup);

                    msg(event.getPlayer(),SignShop.Errors.get("out_of_stock"));

                    return;
                }

                event.getPlayer().getWorld().dropItem(event.getPlayer().getLocation(),isRandom);
                
                sItems = isRandom.getType().name().toLowerCase().replace("_"," ");
            }

            if(event.getAction() == Action.RIGHT_CLICK_BLOCK){
                if(event.getItem() != null){
                    event.setCancelled(true);
                }
                //kludge
                event.getPlayer().updateInventory();
            }
            
            // Mutating money here so if one of the other transactions fail or are not needed
            // money will not be credited or discredited
            Boolean transaction = false;
            // Money giving/taking
            if(operation.contains(givePlayerMoney))
                transaction = mutateMoney(player.getName(), fPrice);
            if(operation.contains(takePlayerMoney))
                transaction = mutateMoney(player.getName(), -fPrice);
            if(transaction == false) {
                // If it fails here we shouldn't attempt to credit or discredit the shopowner
                player.sendMessage("The money transaction failed, please contact the System Administrator");
                return;
            }

            if(operation.contains(giveOwnerMoney))
                transaction = mutateMoney(seller.owner, fPrice);
            if(operation.contains(takeOwnerMoney)) 
                transaction = mutateMoney(seller.owner, -fPrice);
            
            
            SignShop.logTransaction(event.getPlayer().getName(), seller.owner, sOperation, sItems, formatMoney(fPrice));
            msg(event.getPlayer(),getMessage("transaction",sOperation,sItems,fPrice,event.getPlayer().getName(),seller.owner));
            msg(seller.owner,getMessage("transaction_owner",sOperation,sItems,fPrice,event.getPlayer().getName(),seller.owner));
        }
        if(event.getAction() == Action.LEFT_CLICK_BLOCK
        && event.getClickedBlock().getType() == Material.CHEST
        && !mClicks.containsKey(event.getPlayer().getName())){            
            List<Block> signs = SignShop.Storage.getSignsFromChest(bClicked);
            ItemStack[] isItems = null;
            if(signs != null)
                for (Block temp : signs) {                    
                    Seller seller = null;
                    if((seller = SignShop.Storage.getSeller(temp.getLocation())) != null) {
                        Chest cbChest = (Chest) bClicked.getState();
                        isItems = seller.getItems();
                        if(isOutofStock(cbChest.getInventory(), isItems))
                            setSignStatus(temp, ChatColor.RED);
                        else
                            setSignStatus(temp, ChatColor.GREEN);
                    }
                }
        }
    }

    private static class lagSetter implements Runnable{
        private final Block blockToChange;

        lagSetter(Block blockToChange){
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
}