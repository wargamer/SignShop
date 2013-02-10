package org.wargamer2010.signshop.util;

import org.bukkit.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.Location;
import org.bukkit.block.Sign;
import org.bukkit.World;
import org.bukkit.inventory.InventoryHolder;
import java.util.List;
import java.util.LinkedList;
import java.util.Map;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Arrays;
import java.util.LinkedHashSet;
import org.wargamer2010.signshop.Seller;
import org.wargamer2010.signshop.SignShop;
import org.wargamer2010.signshop.Vault;
import org.wargamer2010.signshop.configuration.SignShopConfig;
import org.wargamer2010.signshop.events.SSEventFactory;
import org.wargamer2010.signshop.events.SSLinkEvent;
import org.wargamer2010.signshop.operations.SignShopArguments;
import org.wargamer2010.signshop.player.SignShopPlayer;
import org.wargamer2010.signshop.operations.SignShopOperation;
import org.wargamer2010.signshop.specialops.*;

public class signshopUtil {

    private signshopUtil() {
    }

    public static String getOperation(String sSignOperation){
        if(sSignOperation.length() < 4){
            return "";
        }
        String stripped = ChatColor.stripColor(sSignOperation);
        return stripped.substring(1, stripped.length()-1).toLowerCase();
    }

    public static void generateInteractEvent(Block bLever, Player player, BlockFace bfBlockface) {
        PlayerInteractEvent event = new PlayerInteractEvent(player, Action.RIGHT_CLICK_BLOCK, player.getItemInHand(), bLever, bfBlockface);
        Bukkit.getServer().getPluginManager().callEvent(event);
    }

    public static List<String> getParameters(String sOperation) {
        List<String> parts = new LinkedList<String>();
        if(sOperation.contains("{") && sOperation.contains("}")) {
            String[] bits = sOperation.split("\\{");
            if(bits.length == 2) {
                parts.add(bits[0]);
                String parameters = bits[1].replace("}", "");
                String[] parbits = parameters.split(",");
                if(parbits.length > 1)
                    parts.addAll(Arrays.asList(parbits));
                else
                    parts.add(parameters);
            }
        }
        if(parts.isEmpty())
            parts.add(sOperation);
        return parts;
    }

    public static SignShopOperation getSignShopBlock(String blockName) {
        try {
            Class<Object> fc = (Class<Object>)Class.forName("org.wargamer2010.signshop.operations."+blockName);
            return ((SignShopOperation)fc.newInstance());
        } catch(ClassNotFoundException notfoundex) {
            return null;
        } catch(InstantiationException instex) {
            return null;
        } catch(IllegalAccessException illex) {
            return null;
        }
    }

    public static Map<SignShopOperation, List<String>> getSignShopOps(List<String> operation) {
        Map<SignShopOperation, List<String>> SignShopOperations = new LinkedHashMap<SignShopOperation, List<String>>();
        for(String sSignShopOp : operation) {
            List<String> bits = getParameters(sSignShopOp);
            String op = bits.get(0);
            bits.remove(0);
            SignShopOperation ssOP = getSignShopBlock(op);
            if(ssOP == null)
                return null;
            else
                SignShopOperations.put(ssOP, bits);
        }
        return SignShopOperations;
    }

    public static List<SignShopSpecialOp> getSignShopSpecialOps() {
        List<SignShopSpecialOp> SignShopOperations = new LinkedList<SignShopSpecialOp>();
        for(String sSignShopOp : SignShopConfig.SpecialsOps) {
            try {
                Class<Object> fc = (Class<Object>)Class.forName("org.wargamer2010.signshop.specialops."+sSignShopOp);
                SignShopOperations.add((SignShopSpecialOp)fc.newInstance());
            } catch(ClassNotFoundException notfoundex) {
                return null;
            } catch(InstantiationException instex) {
                return null;
            } catch(IllegalAccessException illex) {
                return null;
            }
        }
        return SignShopOperations;
    }

    public static Map<Enchantment, Integer> convertStringToEnchantments(String sEnchantments) {
        Map<Enchantment, Integer> mEnchantments = new HashMap<Enchantment, Integer>();
        String saEnchantments[] = sEnchantments.split(";");
        if(saEnchantments.length == 0)
            return mEnchantments;
        for(int i = 0; i < saEnchantments.length; i++) {
            String sEnchantment[] = saEnchantments[i].split("\\|");
            int iEnchantment; int iEnchantmentLevel;
            if(sEnchantment.length < 2)
                continue;
            else {
                try {
                    iEnchantment = Integer.parseInt(sEnchantment[0]);
                    iEnchantmentLevel = Integer.parseInt(sEnchantment[1]);
                } catch(NumberFormatException ex) {
                    continue;
                }
                Enchantment eTemp = Enchantment.getById(iEnchantment);
                if(eTemp != null)
                    mEnchantments.put(eTemp, iEnchantmentLevel);
            }
        }
        return mEnchantments;
    }

    public static String convertEnchantmentsToString(Map<Enchantment, Integer> aEnchantments) {
        String sEnchantments = "";
        Boolean first = true;
        for(Map.Entry<Enchantment, Integer> entry : aEnchantments.entrySet()) {
            if(first) first = false;
            else sEnchantments += ";";
            sEnchantments += (entry.getKey().getId() + "|" + entry.getValue());
        }
        return sEnchantments;
    }

    public static String convertLocationToString(Location loc) {
        return (loc.getBlockX() + "/" + loc.getBlockY() + "/" + loc.getBlockZ() + "/" + loc.getWorld().getName());
    }

    public static Location convertStringToLocation(String sLoc, World pWorld) {
        String[] sCoords = sLoc.split("/");
        if(sCoords.length < 3)
            return null;
        try {
            World world = pWorld;
            if(sCoords.length > 3 && Bukkit.getWorld(sCoords[3]) != null)
                world = Bukkit.getWorld(sCoords[3]);
            Location loc = new Location(world, Double.parseDouble(sCoords[0]), Double.parseDouble(sCoords[1]), Double.parseDouble(sCoords[2]));
            return loc;
        } catch(NumberFormatException ex) {
            return null;
        }
    }

    public static Float getNumberFromThirdLine(Block bSign) {
        Sign sign = (Sign)bSign.getState();
        String XPline = sign.getLines()[2];
        return economyUtil.parsePrice(XPline);
    }



    public static List<Integer> getSharePercentages(String line) {
        List<String> bits = new LinkedList<String>();
        List<Integer> percentages = new LinkedList<Integer>();
        if(line == null)
            return percentages;
        if(line.contains("/"))
            bits = Arrays.asList(line.split("/"));
        else
            bits.add(line);
        for(int i = 0; i < bits.size() && i < 2; i++) {
            String bit = bits.get(i);
            try {
                percentages.add(Integer.parseInt(bit));
            } catch(NumberFormatException ex) {
                continue;
            }
        }
        return percentages;
    }

    public static String implode(String[] ary, String delim) {
        String out = "";
        if(ary == null)
            return out;
        for(int i=0; i<ary.length; i++) {
            if(i!=0) { out += delim; }
            out += ary[i];
        }
        return out;
    }

    public static String validateShareSign(List<Block> clickedBlocks, SignShopPlayer ssPlayer) {
        List<String> blocklocations = new LinkedList<String>();
        List<Integer> percentages = new LinkedList<Integer>();
        for(Block sharesign : clickedBlocks) {
            if(itemUtil.clickedSign(sharesign)) {
                Sign sign = (Sign)sharesign.getState();
                List<Integer> tempperc = signshopUtil.getSharePercentages(sign.getLine(3));
                percentages.addAll(tempperc);
                blocklocations.add(signshopUtil.convertLocationToString(sharesign.getLocation()));
                if(tempperc.size() == 2 && (lineIsEmpty(sign.getLine(1)) || lineIsEmpty(sign.getLine(2))))
                    ssPlayer.sendMessage("The second percentage will be ignored as only one username is given.");
                else if(tempperc.size() == 1 && !lineIsEmpty(sign.getLine(2)))
                    ssPlayer.sendMessage("The second username will be ignored as only one percentage is given.");
            }
        }
        int sum = 0;
        for(Integer percentage : percentages)
            sum += percentage;
        if(sum > 100) {
            ssPlayer.sendMessage("Sum of the percentages can never be greater than 100, please adjust the number(s) on the fourth line.");
            return "";
        }
        String[] implodedLocations = new String[blocklocations.size()];
        blocklocations.toArray(implodedLocations);

        return signshopUtil.implode(implodedLocations, SignShopArguments.seperator);
    }

    public static String validateRestrictSign(List<Block> clickedBlocks, SignShopPlayer player) {
        List<String> blocklocations = new LinkedList<String>();
        List<String> permGroups = Arrays.asList(Vault.permission.getGroups());
        for(Block restrictedsign : clickedBlocks) {
            if(itemUtil.clickedSign(restrictedsign)) {
                Sign sign = (Sign)restrictedsign.getState();
                Boolean bValidGroup = false;
                for(int i = 1; i < 4; i++) {
                    if(!lineIsEmpty(sign.getLine(i)))
                        bValidGroup = true;
                    if(!lineIsEmpty(sign.getLine(i)) && !permGroups.contains(sign.getLine(i)))
                        player.sendMessage("The group " + sign.getLine(i) + " does not currently exist!");
                }
                if(bValidGroup)
                    blocklocations.add(signshopUtil.convertLocationToString(restrictedsign.getLocation()));
            }
        }

        String[] implodedLocations = new String[blocklocations.size()];
        blocklocations.toArray(implodedLocations);

        return signshopUtil.implode(implodedLocations, SignShopArguments.seperator);
    }

    public static Boolean restrictedFromUsing(Seller seller, SignShopPlayer player) {
        List<Block> blocks = signshopUtil.getSignsFromMisc(seller, "restrictedsigns");
        if(blocks.isEmpty())
            return false;
        List<String> permGroups = Arrays.asList(Vault.permission.getGroups());
        List<String> playerGroups = new LinkedList<String>();
        for(Block restrictedsign : blocks) {
            if(itemUtil.clickedSign(restrictedsign)) {
                Sign sign = (Sign)restrictedsign.getState();
                for(int i = 1; i < 4; i++) {
                    if(!lineIsEmpty(sign.getLine(i)) && !permGroups.contains(sign.getLine(i))) {
                        player.sendMessage("The group " + sign.getLine(i) + " does not currently exist!");
                    } else if(!lineIsEmpty(sign.getLine(i)) && permGroups.contains(sign.getLine(i))) {
                        playerGroups.add(sign.getLine(i));
                    }
                }
            }
        }
        for(String group : playerGroups) {
            if(Vault.permission.playerInGroup(player.getPlayer(), group)) {
                return false;
            }
        }
        if(playerGroups.size() > 0 && seller.getOwner().equals(player.getPlayer().getName())) {
            player.sendMessage(SignShopConfig.getError("restricted_but_owner", null));
            return false;
        } else
            return (playerGroups.size() > 0 ? !player.isOp() : false);
    }

    public static Boolean lineIsEmpty(String line) {
        return (line == null || line.length() == 0);
    }

    private static Map<String, Integer> getShares(Sign sign, SignShopPlayer ssPlayer) {
        List<Integer> tempperc = signshopUtil.getSharePercentages(sign.getLine(3));
        HashMap<String, Integer> shares = new HashMap<String, Integer>();

        if(tempperc.size() == 2 && (lineIsEmpty(sign.getLine(1)) || lineIsEmpty(sign.getLine(2)))) {
            shares.put((sign.getLine(1) == null ? sign.getLine(2) : sign.getLine(1)), tempperc.get(0));
            ssPlayer.sendMessage("The second percentage will be ignored as only one username is given.");
        } else if(tempperc.size() == 1 && !lineIsEmpty(sign.getLine(2))) {
            shares.put(sign.getLine(1), tempperc.get(0));
            ssPlayer.sendMessage("The second username will be ignored as only one percentage is given.");
        } else if(tempperc.size() == 2) {
            shares.put(sign.getLine(1), tempperc.get(0));
            shares.put(sign.getLine(2), tempperc.get(1));
        } else if(tempperc.size() == 1) {
            shares.put(sign.getLine(1), tempperc.get(0));
        }
        return shares;
    }

    public static List<Block> getSignsFromMisc(Seller seller, String miscprop) {
        List<Block> signs = new LinkedList<Block>();
        if(seller.getMisc().containsKey(miscprop)) {
            String imploded = seller.getMisc().get(miscprop);
            String[] exploded;
            if(imploded.contains(SignShopArguments.seperator))
                exploded = imploded.split(SignShopArguments.seperator);
            else {
                exploded = new String[1];
                exploded[0] = imploded;
            }
            List<String> tempList = Arrays.asList(exploded);
            signs = getBlocksFromLocStringList(tempList, Bukkit.getServer().getWorld(seller.getWorld()));
        }
        return signs;
    }


    public static Boolean distributeMoney(Seller seller, Float fPrice, SignShopPlayer ssPlayer) {
        List<Block> shareSigns = getSignsFromMisc(seller, "sharesigns");
        SignShopPlayer ssOwner = new SignShopPlayer(seller.getOwner());
        if(shareSigns.isEmpty()) {
            return ssOwner.mutateMoney(fPrice);
        } else {
            Boolean bTotalTransaction;
            Map<String, Integer> shares = new HashMap<String, Integer>();
            for(Block sharesign : shareSigns) {
                if(itemUtil.clickedSign(sharesign)) {
                    shares.putAll(getShares((Sign)sharesign.getState(), ssPlayer));
                }
            }
            Integer totalPercentage = 0;
            for(Map.Entry<String, Integer> share : shares.entrySet()) {

                Float amount = (fPrice / 100 * share.getValue());
                SignShopPlayer sharee = new SignShopPlayer(share.getKey());
                if(sharee.getPlayer() == null && Bukkit.getServer().getOfflinePlayer(share.getKey()) == null)
                    ssOwner.sendMessage("Not giving " + share.getKey() + " " + economyUtil.formatMoney(amount) + " because player doesn't exist!");
                else {
                    ssOwner.sendMessage("Giving " + share.getKey() + " a share of " + economyUtil.formatMoney(amount));
                    sharee.sendMessage("You were given a share of " + economyUtil.formatMoney(amount));
                    totalPercentage += share.getValue();
                    bTotalTransaction = sharee.mutateMoney(amount);
                    if(!bTotalTransaction) {
                        ssOwner.sendMessage("Money transaction failed for player: " + share.getKey());
                        return false;
                    }
                }
            }
            if(totalPercentage != 100) {
                Float amount = fPrice;
                if(totalPercentage > 0)
                    amount = (fPrice / 100 * (100 - totalPercentage));
                return ssOwner.mutateMoney(amount);
            } else
                return true;
        }
    }

    public static List<Block> getBlocksFromLocStringList(List<String> sLocs, World world) {
        List<Block> blocklist = new LinkedList<Block>();
        for(String loc : sLocs) {
            Location temp = signshopUtil.convertStringToLocation(loc, world);
            if(temp != null)
                blocklist.add(temp.getBlock());
        }
        return blocklist;
    }

    public static Boolean clickedSignShopMat(Block bBlock, SignShopPlayer ssPlayer) {
        String materialName = SignShopConfig.LinkableMaterials.get(bBlock.getType());
        if(SignShopConfig.LinkableMaterials.containsKey(bBlock.getType())) {
            if(!ssPlayer.isOp() && ssPlayer.hasPerm("SignShop.DenyLink." + materialName, true) && !ssPlayer.hasPerm("SignShop.AllowLink." + materialName, true)) {
                ssPlayer.sendMessage(SignShopConfig.getError("link_notallowed", null));
                return false;
            }
            return true;
        } else
            return false;
    }

    public static Boolean registerClickedMaterial(PlayerInteractEvent event) {
        SignShopPlayer ssPlayer = new SignShopPlayer(event.getPlayer());
        Boolean signshopMat = registerClickedMaterial(event.getClickedBlock(), ssPlayer);
        if(signshopMat)
            event.setCancelled(true);
        return signshopMat;
    }

    public static Boolean registerClickedMaterial(Block bClicked, SignShopPlayer ssPlayer) {
        if(clickedSignShopMat(bClicked, ssPlayer)) {
            if(clicks.mClicksPerLocation.containsKey(bClicked.getLocation())) {
                clicks.mClicksPerLocation.remove(bClicked.getLocation());
                ssPlayer.sendMessage("Removed stored location");
            } else {
                SSLinkEvent event = SSEventFactory.generateLinkEvent(bClicked, ssPlayer, null);
                SignShop.scheduleEvent(event);
                if(event.isCancelled()) {
                    ssPlayer.sendMessage(SignShopConfig.getError("link_notallowed", null));
                    return false;
                } else {
                    clicks.mClicksPerLocation.put(bClicked.getLocation(), ssPlayer.getPlayer());
                    ssPlayer.sendMessage("Stored location of " + itemUtil.formatData(bClicked.getState().getData()));
                }
            }
            return true;
        }
        return false;
    }

    public static Float ApplyPriceMod(SignShopArguments ssArgs) {
        if(ssArgs.tryToApplyPriceMod()) {
            Float fPrice = ApplyPriceMod(ssArgs.get_ssPlayer(), ssArgs.get_fPrice(), ssArgs.get_sOperation());
            ssArgs.set_fPrice(fPrice);
            ssArgs.setMessagePart("!price", economyUtil.formatMoney(fPrice));
        }
        return ssArgs.get_fPrice();
    }

    public static Float ApplyPriceMod(SignShopPlayer player, Float fPrice, String sOperation) {
        Float fPricemod = player.getPlayerPricemod(sOperation, true);
        return (fPrice * fPricemod);
    }

    public static boolean getSignshopBlocksFromList(SignShopPlayer ssPlayer, List<Block> containables, List<Block> activatables, Block bClicked) {
        Boolean multiWorld = false;
        LinkedHashSet<Location> lClicked = getKeysByValue(clicks.mClicksPerLocation, ssPlayer.getPlayer());
        for (Location loc : lClicked) {
            Block bBlockat = loc.getBlock();
            if(bBlockat.getLocation().equals(bClicked.getLocation()))
                continue;
            if (bBlockat.getState() instanceof InventoryHolder) {
                containables.add(bBlockat);
            } else if (signshopUtil.clickedSignShopMat(bBlockat, ssPlayer)) {
                activatables.add(bBlockat);
                if(itemUtil.clickedDoor(bBlockat)) {
                    Block otherpart = itemUtil.getOtherDoorPart(bBlockat);
                    if(otherpart != null)
                        activatables.add(otherpart);
                }
            }
            if (!multiWorld && !bBlockat.getWorld().getName().equals(bClicked.getWorld().getName())) {
                if (SignShopConfig.getAllowMultiWorldShops()) {
                    multiWorld = true;
                } else {
                    ssPlayer.sendMessage(SignShopConfig.getError("multiworld_not_allowed", null));
                    return false;
                }
            }
        }
        return true;
    }

    public static <T, E> LinkedHashSet<T> getKeysByValue(Map<T, E> map, E value) {
        LinkedHashSet<T> keys = new LinkedHashSet<T>();
        for (Map.Entry<T, E> entry : map.entrySet()) {
            if (value.equals(entry.getValue())) {
                keys.add(entry.getKey());
            }
        }
        return keys;
    }

    public static Boolean checkDistance(Block a, Block b, int maxdistance) {
        if (maxdistance <= 0) {
            return true;
        }
        int xdiff = Math.abs(a.getX() - b.getX());
        int ydiff = Math.abs(a.getY() - b.getY());
        int zdiff = Math.abs(a.getZ() - b.getZ());
        if (xdiff > maxdistance || ydiff > maxdistance || zdiff > maxdistance) {
            return false;
        } else {
            return true;
        }
    }

    public static String capFirstLetter(final String string) {
        if(string == null || string.isEmpty())
            return string;
        String workwith = string.replace("_", " ");
        String[] spacesplit;
        if(workwith.contains(" "))
            spacesplit = workwith.split(" ");
        else {
            spacesplit = new String[1];
            spacesplit[0] = workwith;
        }
        for(int i = 0; i < spacesplit.length; i++) {
            char[] arr = spacesplit[i].toCharArray();
            arr[0] = Character.toUpperCase(arr[0]);
            spacesplit[i] = new String(arr);
        }
        return implode(spacesplit, " ");
    }
}
