package org.wargamer2010.signshop.util;

import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.block.data.Bisected;
import org.bukkit.block.data.BlockData;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.block.Action;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.wargamer2010.signshop.Seller;
import org.wargamer2010.signshop.SignShop;
import org.wargamer2010.signshop.blocks.*;
import org.wargamer2010.signshop.configuration.SignShopConfig;
import org.wargamer2010.signshop.configuration.Storage;
import org.wargamer2010.signshop.operations.SignShopArguments;
import org.wargamer2010.signshop.operations.SignShopArgumentsType;
import org.wargamer2010.signshop.operations.SignShopOperationListItem;
import org.wargamer2010.signshop.player.VirtualInventory;

import java.util.*;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** @noinspection deprecation*/ //TODO Remove deprecated calls
public class itemUtil {

    private itemUtil() {

    }

    /**
     * Returns the minimum amount of ItemStacks needed to function with RandomItem
     *
     * @param isItems Stacks to filter
     * @return The minimum ItemStacks needed
     */
    public static ItemStack[] getMinimumAmount(ItemStack[] isItems) {
        HashMap<ItemStack, Integer> materialByMaximumAmount = new LinkedHashMap<>();

        for(ItemStack item: isItems) {
            ItemStack isBackup = getSingleAmountOfStack(item);
            if(!materialByMaximumAmount.containsKey(isBackup) || materialByMaximumAmount.get(isBackup) < item.getAmount())
                materialByMaximumAmount.put(isBackup, item.getAmount());
        }
        ItemStack[] isBackupToTake = new ItemStack[materialByMaximumAmount.size()];
        int i = 0;
        for(Map.Entry<ItemStack, Integer> entry : materialByMaximumAmount.entrySet()) {
            entry.getKey().setAmount(entry.getValue());
            isBackupToTake[i] = entry.getKey();
            i++;
        }
        return isBackupToTake;
    }

    public static ItemStack[] getAllItemStacksForContainables(List<Block> containables) {
        List<ItemStack> tempItems = new LinkedList<>();

        for(Block bHolder : containables) {
            if(bHolder.getState() instanceof InventoryHolder) {
                InventoryHolder Holder = (InventoryHolder)bHolder.getState();
                for(ItemStack item : Holder.getInventory().getContents()) {
                    if(item != null && item.getAmount() > 0) {
                        tempItems.add(item);
                    }
                }
            }
        }

        return tempItems.toArray(new ItemStack[0]);
    }

    public static boolean stockOKForContainables(List<Block> containables, ItemStack[] items, boolean bTakeOrGive) {
        return (getFirstStockOKForContainables(containables, items, bTakeOrGive) != null);
    }

    public static InventoryHolder getFirstStockOKForContainables(List<Block> containables, ItemStack[] items, boolean bTakeOrGive) {
        for(Block bHolder : containables) {
            if(bHolder.getState() instanceof InventoryHolder) {
                InventoryHolder Holder = (InventoryHolder)bHolder.getState();
                VirtualInventory vInventory = new VirtualInventory(Holder.getInventory());
                if(vInventory.isStockOK(items, bTakeOrGive))
                    return Holder;
            }
        }
        return null;
    }

    public static void fixBooks(ItemStack[] stacks) {//TODO this causes a ton of lag do we really even need this?
        if(stacks == null || !SignShopConfig.getEnableWrittenBookFix())
            return;

        long timeMillis = System.currentTimeMillis();
        for(ItemStack stack : stacks) {
            if(stack != null && stack.getType() == Material.WRITTEN_BOOK &&
                    stack.hasItemMeta() && stack.getItemMeta() instanceof BookMeta) {
                ItemStack copy = new ItemStack(Material.WRITTEN_BOOK);

                BookFactory.getBookItem(copy).copyFrom(BookFactory.getBookItem(stack));

                ItemMeta copyMeta = copy.getItemMeta();
                ItemMeta realMeta = stack.getItemMeta();

                copyMeta.setDisplayName(realMeta.getDisplayName());
                copyMeta.setLore(realMeta.getLore());

                for(Map.Entry<Enchantment, Integer> entry : realMeta.getEnchants().entrySet())
                    copyMeta.addEnchant(entry.getKey(), entry.getValue(), true);

                stack.setItemMeta(copyMeta);
            }
        }
        long timeMillis2 = System.currentTimeMillis();
        SignShop.debugTiming("Fixbook loop",timeMillis,timeMillis2);
    }

    public static String binaryToRoman(int binary) {
        final String[] RCODE = {"M", "CM", "D", "CD", "C", "XC", "L",
                                           "XL", "X", "IX", "V", "IV", "I"};
        final int[]    BVAL  = {1000, 900, 500, 400,  100,   90,  50,
                                               40,   10,    9,   5,   4,    1};
        if (binary <= 0 || binary >= 4000) {
            return "";
        }
        StringBuilder roman = new StringBuilder();
        for (int i = 0; i < RCODE.length; i++) {
            while (binary >= BVAL[i]) {
                binary -= BVAL[i];
                roman.append(RCODE[i]);
            }
        }
        return roman.toString();
    }
    public static String formatMaterialName(ItemStack itemStack){

        return formatMaterialName(itemStack.getType());
    }

    public  static String formatMaterialName(Block block){

       return formatMaterialName(block.getType());
    }

    private static String formatMaterialName(Material material) {
        String sData;
        sData = material.toString().toLowerCase();
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

    public static String stripConstantCase(String constantCaseString){
        constantCaseString = constantCaseString.replace("_"," ");
        Pattern p = Pattern.compile("(^|\\W)([a-z])");
        Matcher m = p.matcher(constantCaseString.toLowerCase());
        StringBuffer sb = new StringBuffer(constantCaseString.length());

        while(m.find()){
            m.appendReplacement(sb, m.group(1) + m.group(2).toUpperCase() );
        }

        m.appendTail(sb);

        return sb.toString();
    }

    private static ItemStack getSingleAmountOfStack(ItemStack item) {
        if(item == null)
            return null;
        IItemTags tags = BookFactory.getItemTags();
        ItemStack isBackup = tags.getCraftItemstack(
            item.getType(),
            1,
            item.getDurability()
        );
        safelyAddEnchantments(isBackup, item.getEnchantments());
        return tags.copyTags(item, isBackup);
    }

   @SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
    public static String itemStackToString(ItemStack[] isStacks) {
        if(isStacks == null || isStacks.length == 0)
            return "";
        HashMap<ItemStack, Integer> items = new HashMap<>();
        HashMap<ItemStack, Map<Enchantment, Integer>> enchantments = new HashMap<>();
        StringBuilder sItems = new StringBuilder();
        boolean first = true;
        int tempAmount;
        for(ItemStack item: isStacks) {
            if(item == null)
                continue;
            ItemStack isBackup = getSingleAmountOfStack(item);

            if(item.getEnchantments().size() > 0)
                enchantments.put(isBackup, item.getEnchantments());
            if(items.containsKey(isBackup)) {
                tempAmount = (items.get(isBackup) + item.getAmount());
                items.put(isBackup, tempAmount);
            } else
                items.put(isBackup, item.getAmount());
        }
        for(Map.Entry<ItemStack, Integer> entry : items.entrySet()) {
            if(first) first = false;
            else sItems.append(SignShopConfig.getTextColor()).append(", ");
            String newItemMeta = SignShopItemMeta.getName(entry.getKey());
            String count = (SignShopItemMeta.getTextColor() + entry.getValue().toString() + " ");
            if(newItemMeta.isEmpty())
                sItems.append(count).append(formatMaterialName(entry.getKey()));
            else
                sItems.append(count).append(newItemMeta);
            if(itemUtil.isWriteableBook(entry.getKey())) {
                IBookItem book = BookFactory.getBookItem(entry.getKey());
                if(book != null && (book.getAuthor() != null || book.getTitle() != null))
                    sItems.append(" (").append(book.getTitle() == null ? "Unknown" : book.getTitle()).append(" by ").append(book.getAuthor() == null ? "Unknown" : book.getAuthor()).append(")");
            }
            sItems.append(ChatColor.WHITE);
        }

        return sItems.toString();
    }

    public static String enchantmentsToMessageFormat(Map<Enchantment,Integer> enchantments) {
        StringBuilder enchantmentMessage = new StringBuilder();
        boolean eFirst = true;

        enchantmentMessage.append("(");
        for(Map.Entry<Enchantment,Integer> eEntry : enchantments.entrySet()) {
            if(eFirst) eFirst = false;
            else enchantmentMessage.append(", ");
            enchantmentMessage.append(stripConstantCase(eEntry.getKey().getName())).append(" ").append(binaryToRoman(eEntry.getValue()));
        }
        enchantmentMessage.append(")");
        return enchantmentMessage.toString();
    }

    public static void setSignStatus(Block sign, ChatColor color) {
        if(clickedSign(sign)) {
            Sign signblock = ((Sign) sign.getState());
            String[] sLines = signblock.getLines();
            if(ChatColor.stripColor(sLines[0]).length() <= 14) {
                signblock.setLine(0, (color + ChatColor.stripColor(sLines[0])));
                signblock.update();
            }
        }
    }

    public static Boolean needsEnchantment(ItemStack isEnchantMe, Map<Enchantment, Integer> enchantments) {
        if(enchantments.isEmpty())
            return false;
        Map<Enchantment, Integer> currentEnchantments = isEnchantMe.getEnchantments();
        
        for(Map.Entry<Enchantment, Integer> enchantment : enchantments.entrySet()) {
            if(!currentEnchantments.containsKey(enchantment.getKey()) || !currentEnchantments.get(enchantment.getKey()).equals(enchantment.getValue())) {
                return true;
            }
        }
        
        return false;
    }
    
    public static Boolean safelyAddEnchantments(ItemStack isEnchantMe, Map<Enchantment, Integer> enchantments) {
        if(enchantments.isEmpty())
            return true;

        try {
            isEnchantMe.addEnchantments(enchantments);
        } catch(IllegalArgumentException ex) {
            if(SignShopConfig.getAllowUnsafeEnchantments()) {
                try {
                    isEnchantMe.addUnsafeEnchantments(enchantments);
                } catch(IllegalArgumentException exfinal) {
                    return false;
                }
            } else
                return false;
        }
        return true;
    }

    public static HashMap<ItemStack, Integer> StackToMap(ItemStack[] isStacks) {
        ItemStack[] isBackup = getBackupItemStack(isStacks);
        HashMap<ItemStack, Integer> mReturn = new HashMap<>();
        if(isBackup == null)
            return mReturn;
        int tempAmount;
        for (ItemStack itemStack : isBackup) {
            if (itemStack == null) continue;
            tempAmount = itemStack.getAmount();
            itemStack.setAmount(1);
            if (mReturn.containsKey(itemStack)) {
                tempAmount += mReturn.get(itemStack);
                mReturn.remove(itemStack);
            }
            mReturn.put(itemStack, tempAmount);
        }
        return mReturn;
    }

    public static ItemStack[] getBackupItemStack(ItemStack[] isOriginal) {
        if(isOriginal == null)
            return null;
        ItemStack[] isBackup = new ItemStack[isOriginal.length];
        for(int i = 0; i < isOriginal.length; i++){
            if(isOriginal[i] != null) {
                isBackup[i] = getBackupSingleItemStack(isOriginal[i]);
            }
        }
        return isBackup;
    }

    public static ItemStack getBackupSingleItemStack(ItemStack isOriginal) {
        if(isOriginal == null)
            return isOriginal;
        return isOriginal.clone();
    }

    public static ItemStack[] filterStacks(ItemStack[] all, ItemStack[] filterby) {
        ItemStack[] filtered = new ItemStack[all.length];
        List<ItemStack> tempFiltered = new LinkedList<>();
        HashMap<ItemStack, Integer> mFilter = StackToMap(filterby);
        for(ItemStack stack : all) {
            ItemStack temp = getBackupSingleItemStack(stack);
            temp.setAmount(1);
            if(mFilter.containsKey(temp)) {
                tempFiltered.add(stack);
            }
        }

        return tempFiltered.toArray(filtered);
    }

    public static void updateStockStatusPerChest(Block bHolder, Block bIgnore) {
        List<Block> signs = Storage.get().getSignsFromHolder(bHolder);
        if(signs != null) {
            for (Block temp : signs) {
                if(temp == bIgnore)
                    continue;
                if(!clickedSign(temp))
                    continue;
                Seller seller = Storage.get().getSeller(temp.getLocation());
                updateStockStatusPerShop(seller);
            }
        }
    }

    public static void updateStockStatusPerShop(Seller pSeller) {//TODO called frequently
        if(pSeller != null) {
            Block pSign = pSeller.getSign();
            if(pSign == null || !(pSign.getState() instanceof Sign))
                return;
            String[] sLines = ((Sign) pSign.getState()).getLines();
            if(SignShopConfig.getBlocks(signshopUtil.getOperation(sLines[0])).isEmpty())
                return;
            List<String> operation = SignShopConfig.getBlocks(signshopUtil.getOperation(sLines[0]));
            List<SignShopOperationListItem> SignShopOperations = signshopUtil.getSignShopOps(operation);
            if(SignShopOperations == null)
                return;
            SignShop.debugMessage("itemUtil create args");
            SignShopArguments ssArgs = new SignShopArguments(economyUtil.parsePrice(sLines[3]), pSeller.getItems(), pSeller.getContainables(), pSeller.getActivatables(),
                                                                null, null, pSign, signshopUtil.getOperation(sLines[0]), null, Action.RIGHT_CLICK_BLOCK, SignShopArgumentsType.Check);
            if(pSeller.getRawMisc() != null)
                ssArgs.miscSettings = pSeller.getRawMisc();
            Boolean reqOK = true;
            for(SignShopOperationListItem ssOperation : SignShopOperations) {
                ssArgs.setOperationParameters(ssOperation.getParameters());
                reqOK = ssOperation.getOperation().checkRequirements(ssArgs, false);
                if(!reqOK) {
                    itemUtil.setSignStatus(pSign, ChatColor.DARK_RED);
                    break;
                }
            }
            if(reqOK)
                itemUtil.setSignStatus(pSign, ChatColor.DARK_BLUE);
        }
    }

    public static void updateStockStatus(Block bSign, ChatColor ccColor) {//TODO this is called frequently and makes many ops take a while
        SignShop.debugMessage("Updating Stock Status");
        long timeMillis = System.currentTimeMillis();
        Seller seTemp = Storage.get().getSeller(bSign.getLocation());
        if(seTemp != null) {
            List<Block> iChests = seTemp.getContainables();
            for(Block bHolder : iChests)
                updateStockStatusPerChest(bHolder, bSign);
        }
        setSignStatus(bSign, ccColor);
        long timeMillis2 = System.currentTimeMillis();
        SignShop.debugTiming("Stock update",timeMillis,timeMillis2);
    }

    //TODO This is what is loading chunks
    public static Boolean clickedSign(Block bBlock) {//TODO change to Tag in a later version?
        return (bBlock.getBlockData() instanceof org.bukkit.block.data.type.Sign || bBlock.getBlockData() instanceof org.bukkit.block.data.type.WallSign);
    }

    public static Boolean clickedDoor(Block bBlock) {
       return Tag.DOORS.isTagged(bBlock.getType());
    }

    private static boolean isTopHalf(BlockData blockData) {
        if (blockData instanceof Bisected) {
            Bisected bisected = ((Bisected) blockData);
            return bisected.getHalf() == Bisected.Half.TOP;
        }
        return false;
    }

    public static Block getOtherDoorPart(Block bBlock) {
        if(!clickedDoor(bBlock))
            return null;
        Block up = bBlock.getWorld().getBlockAt(bBlock.getX(), bBlock.getY()+1, bBlock.getZ());
        Block down = bBlock.getWorld().getBlockAt(bBlock.getX(), bBlock.getY()-1, bBlock.getZ());

        Block otherpart = isTopHalf(bBlock.getBlockData()) ? down : up;
        if(clickedDoor(otherpart))
            return otherpart;
        return null;
    }

    public static String Join(String[] arr, int fromIndex) {
        StringBuilder builder = new StringBuilder(400);
        if(fromIndex > arr.length || fromIndex < 0)
            return "";
        for(int i = fromIndex; i < arr.length; i++) {
            builder.append(arr[i]);
        }
        return builder.toString();
    }


    public static ItemStack[] convertStringtoItemStacks(List<String> itemStringList) {
        ItemStack[] itemStacks = new ItemStack[itemStringList.size()];

        for (int i = 0; i < itemStringList.size(); i++) {
            try {
                String base64prop = itemStringList.get(i);
                if (base64prop != null) {
                    ItemStack[] convertedStacks = BukkitSerialization.itemStackArrayFromBase64(base64prop);
                    if(convertedStacks.length > 0 && convertedStacks[0] != null) {
                        itemStacks[i] = convertedStacks[0];
                    }
                }
            } catch (Exception e) {
                if (SignShopConfig.debugging()) {
                    SignShop.log("Error converting strings to item stacks.", Level.WARNING);
                }
            }
        }


        return itemStacks;
    }

    public static String[] convertItemStacksToString(ItemStack[] itemStackArray) {
        List<String> itemStringList = new ArrayList<>();
        if (itemStackArray == null)
            return new String[1];

        ItemStack currentItemStack;
        for (ItemStack itemStack : itemStackArray) {
            if (itemStack != null) {
                currentItemStack = itemStack;
                ItemStack[] stacks = new ItemStack[1];
                stacks[0] = currentItemStack;

                itemStringList.add(BukkitSerialization.itemStackArrayToBase64(stacks));

            }

        }
        String[] items = new String[itemStringList.size()];
        itemStringList.toArray(items);
        return items;
    }


    public static boolean isWriteableBook(ItemStack item) {
        if (item == null) return false;
        return (item.getType() == Material.getMaterial("WRITTEN_BOOK") || item.getType() == Material.getMaterial("BOOK_AND_QUILL"));
    }


    public static boolean itemstackEqual(ItemStack a, ItemStack b, boolean ignoredur) {
        if(a.getType() != b.getType())
            return false;
        if(!ignoredur && a.getDurability() != b.getDurability())
            return false;
        if(a.getEnchantments() != b.getEnchantments())
            return false;
        if(!SignShopItemMeta.getMetaAsMap(a.getItemMeta()).equals(SignShopItemMeta.getMetaAsMap(b.getItemMeta())))
            return false;
        
        return a.getMaxStackSize() == b.getMaxStackSize();
    }

    public static void loadChunkByBlock(Block block, int radius) {
        boolean OK = true;
        int chunksize = 12;
        for(int x = -radius; x <= radius; x++) {
            for(int y = -radius; y <= radius; y++) {
                for(int z = -radius; z <= radius; z++) {
                    OK = (!OK || loadChunkByBlock(
                            block.getWorld().getBlockAt(
                                    block.getX() + (x * chunksize),
                                    block.getY() + (y * chunksize),
                                    block.getZ() + (z * chunksize))));
                }
            }
        }

    }

    public static boolean loadChunkByBlock(Block block) {
        if(block == null)
            return false;
        Chunk chunk = block.getChunk();
        if (!chunk.isLoaded())
            return chunk.load();
        return true; // Chunk already loaded
    }
}
