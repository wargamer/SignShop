package org.wargamer2010.signshop.util;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Content;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.block.data.Bisected;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.sign.Side;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.block.Action;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.wargamer2010.signshop.Seller;
import org.wargamer2010.signshop.data.*;
import org.wargamer2010.signshop.configuration.SignShopConfig;
import org.wargamer2010.signshop.data.Storage;
import org.wargamer2010.signshop.operations.SignShopArguments;
import org.wargamer2010.signshop.operations.SignShopArgumentsType;
import org.wargamer2010.signshop.operations.SignShopOperationListItem;
import org.wargamer2010.signshop.player.VirtualInventory;
import org.wargamer2010.signshop.data.serialization.ItemSerializer;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility class for ItemStack operations used throughout SignShop.
 *
 * <p>Provides static utility methods for item manipulation, comparison, serialization,
 * inventory operations, and UI component generation. This is one of the most frequently
 * used utility classes in the codebase.</p>
 *
 * <h2>Key Functionality:</h2>
 * <ul>
 *   <li><b>Item Serialization:</b> {@link #convertItemStacksToString(ItemStack[])} and
 *       {@link #convertStringtoItemStacks(List)} for persistence</li>
 *   <li><b>Inventory Operations:</b> {@link #getItemStacksForContainables(List)},
 *       {@link #getAllItemStacksForContainables(List)} for chest access</li>
 *   <li><b>Item Comparison:</b> {@link #itemstacksEqual(ItemStack, ItemStack, boolean)} for
 *       trade shops and stock checking</li>
 *   <li><b>UI Components:</b> {@link #itemStackToComponent(ItemStack, boolean)} for hover
 *       tooltips in chat messages</li>
 *   <li><b>Chunk Loading:</b> {@link #loadChunkByBlock(Block)} ensures chest chunks are loaded</li>
 * </ul>
 *
 * <h2>Hover Tooltip System:</h2>
 * <p>The {@link #itemStackToComponent} method creates chat components with hover events
 * showing item details. On Paper servers, uses full item tooltips via reflection
 * ({@code serializeItemAsJson}). On Spigot, shows limited info due to API restrictions.</p>
 *
 * <h2>Thread Safety:</h2>
 * <p>All methods are static. Most are NOT thread-safe and must be called from the main thread
 * as they interact with Bukkit API (blocks, inventories).</p>
 *
 * @see ItemSerializer
 * @see SignShopArguments
 * @see signshopUtil
 */
public class itemUtil {
    public static Map<Material, String> formattedMaterials = new HashMap<>();
    private static SignShopConfig signShopConfig;

    static {
        initializeFormattedMaterialMap();
    }

    private itemUtil() {

    }

    public static void setSignShopConfig(SignShopConfig config) {
        signShopConfig = config;
    }

    /**
     * Returns the minimum amount of ItemStacks needed to function with RandomItem
     *
     * @param isItems Stacks to filter
     * @return The minimum ItemStacks needed
     */
    public static ItemStack[] getMinimumAmount(ItemStack[] isItems) {
        HashMap<ItemStack, Integer> materialByMaximumAmount = new LinkedHashMap<>();

        for (ItemStack item : isItems) {
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

    /**
     * Gets all items from all containable blocks (chests, barrels, etc.).
     *
     * @param containables List of container blocks to extract items from
     * @return Array of all non-null items found in the containers
     */
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

    /**
     * Checks if any containable has sufficient stock for the given items.
     *
     * @param containables List of container blocks to check
     * @param items Items to check stock for
     * @param bTakeOrGive true = check if items can be taken, false = check if items can fit
     * @return true if any container has sufficient stock
     */
    public static boolean stockOKForContainables(List<Block> containables, ItemStack[] items, boolean bTakeOrGive) {
        return (getFirstStockOKForContainables(containables, items, bTakeOrGive) != null);
    }

    /**
     * Finds the first containable with sufficient stock for the given items.
     *
     * @param containables List of container blocks to check
     * @param items Items to check stock for
     * @param bTakeOrGive true = check if items can be taken, false = check if items can fit
     * @return First InventoryHolder with sufficient stock, or null if none found
     */
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

    /**
     * Fixes book metadata for items deserialized from legacy format.
     * Only needed for backward compatibility with old shops.
     *
     * @param item The ItemStack to fix (must be WRITTEN_BOOK)
     */
    public static void fixBookLegacy(ItemStack item) {
        if (item == null || !signShopConfig.getEnableWrittenBookFix()) {
            return;
        }

        if (item.getType() != Material.WRITTEN_BOOK || !item.hasItemMeta() || !(item.getItemMeta() instanceof BookMeta)) {
            return;
        }

        ItemStack copy = new ItemStack(Material.WRITTEN_BOOK);

        BookFactory.getBookItem(copy).copyFrom(BookFactory.getBookItem(item));

        ItemMeta copyMeta = copy.getItemMeta();
        ItemMeta realMeta = item.getItemMeta();

        copyMeta.setDisplayName(realMeta.getDisplayName());
        copyMeta.setLore(realMeta.getLore());

        for (Map.Entry<Enchantment, Integer> entry : realMeta.getEnchants().entrySet()) {
            copyMeta.addEnchant(entry.getKey(), entry.getValue(), true);
        }

        item.setItemMeta(copyMeta);
    }

    /**
     * Converts an integer to Roman numerals (used for enchantment level display).
     *
     * @param binary Integer value to convert (1-3999)
     * @return Roman numeral string, or empty string if out of range
     */
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



    private static void initializeFormattedMaterialMap(){
        for (Material mat:Material.values()){
            formattedMaterials.putIfAbsent(mat,formatMaterialName(mat));
        }
    }

    public static void updateFormattedMaterial(Material material,String string){
        formattedMaterials.replace(material,string);
    }

    public static void resetFormattedMaterials() {
        formattedMaterials.clear();
        initializeFormattedMaterialMap();
    }

    public static String formatMaterialName(Material material) {
        if(formattedMaterials.containsKey(material)) {
            return formattedMaterials.get(material);
        }

        String sData;
        sData = material.toString().toLowerCase();
        Pattern p = Pattern.compile("\\(-?[0-9]+\\)");
        Matcher m = p.matcher(sData);
        sData = m.replaceAll("");
        sData = sData.replace("_", " ");

        StringBuilder sb = new StringBuilder(sData.length());
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
        StringBuilder sb = new StringBuilder(constantCaseString.length());

        while(m.find()){
            m.appendReplacement(sb, m.group(1) + m.group(2).toUpperCase() );
        }

        m.appendTail(sb);

        return sb.toString();
    }

   @SuppressWarnings("deprecation")
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

    /**
     * Converts an array of ItemStacks to a human-readable string for chat messages.
     * Consolidates duplicate items and includes enchantments/lore if configured.
     *
     * @param isStacks The item stacks to convert
     * @return Formatted string like "5 Diamond Sword, 3 Iron Pickaxe"
     */
    public static String itemStackToString(ItemStack[] isStacks) {
        if(isStacks == null)
            return "";
        HashMap<ItemStack, Integer> items = new HashMap<>();
        StringBuilder sItems = new StringBuilder();
        boolean first = true;
        int tempAmount;
        for(ItemStack item: isStacks) {
            if(item == null)
                continue;
            ItemStack isBackup = getSingleAmountOfStack(item);

            if(items.containsKey(isBackup)) {
                tempAmount = (items.get(isBackup) + item.getAmount());
                items.put(isBackup, tempAmount);
            } else
                items.put(isBackup, item.getAmount());
        }
        for(Map.Entry<ItemStack, Integer> entry : items.entrySet()) {
            if (first) first = false;
            else sItems.append(signShopConfig.getTextColor()).append(", ");
            String newItemMeta = SignShopItemMeta.getName(entry.getKey(), signShopConfig.getShowItemDetailsInChat());
            String count = (signShopConfig.getTextColor() + entry.getValue().toString() + " ");
            if(newItemMeta.isEmpty())
                sItems.append(count).append(formatMaterialName(entry.getKey()));
            else
                sItems.append(count).append(newItemMeta);

            // Conditionally add extra details in visible text (matches itemStackToComponent behavior)
            if (signShopConfig.getShowItemDetailsInChat()) {
                if(itemUtil.isWriteableBook(entry.getKey())) {
                    IBookItem book = BookFactory.getBookItem(entry.getKey());
                    if(book != null && (book.getAuthor() != null || book.getTitle() != null))
                        sItems.append(" (").append(book.getTitle() == null ? "Unknown" : book.getTitle()).append(" by ").append(book.getAuthor() == null ? "Unknown" : book.getAuthor()).append(")");
                }
                if (entry.getKey().hasItemMeta() && entry.getKey().getItemMeta().hasLore()){
                    sItems.append(signShopConfig.getTextColor()).append("<").append(ChatColor.RESET);
                    boolean firstLore = true;
                    for (String loreLine : entry.getKey().getItemMeta().getLore()){
                        if (firstLore) firstLore = false;
                        else sItems.append(signShopConfig.getTextColor()).append(", ").append(ChatColor.RESET);
                        sItems.append(loreLine);
                    }
                    sItems.append(signShopConfig.getTextColor()).append("> ").append(ChatColor.RESET);
                }
            }
            sItems.append(ChatColor.WHITE);
        }

        return sItems.toString();
    }

    /**
     * Converts an array of ItemStacks to a TextComponent with hover tooltips showing full item details.
     * Each item in the list gets its own hover event displaying the complete item information
     * (enchantments, lore, durability, attributes, etc.) as if hovering over it in an inventory.
     *
     * @param isStacks The item stacks to convert
     * @return BaseComponent with hover events, or simple TextComponent if items is null/empty
     */
    @SuppressWarnings("deprecation") // Bukkit.getUnsafe() required for Paper runtime detection
    public static BaseComponent itemStackToComponent(ItemStack[] isStacks) {
        if (isStacks == null)
            return new TextComponent("");

        // Consolidate duplicate items (same logic as itemStackToString)
        HashMap<ItemStack, Integer> items = new HashMap<>();

        for (ItemStack item : isStacks) {
            if (item == null)
                continue;
            ItemStack isBackup = getSingleAmountOfStack(item);

            if (items.containsKey(isBackup)) {
                int tempAmount = (items.get(isBackup) + item.getAmount());
                items.put(isBackup, tempAmount);
            } else {
                items.put(isBackup, item.getAmount());
            }
        }

        // Build component with hover events for each item
        ComponentBuilder builder = new ComponentBuilder("");
        boolean first = true;

        for (Map.Entry<ItemStack, Integer> entry : items.entrySet()) {
            if (first) {
                first = false;
            } else {
                builder.append(", ").color(net.md_5.bungee.api.ChatColor.getByChar(signShopConfig.getTextColor().getChar()));
            }

            // Build the visible text
            String itemName = SignShopItemMeta.getName(entry.getKey(), signShopConfig.getShowItemDetailsInChat());
            String count = entry.getValue().toString() + " ";
            // Prepend default color code so it applies unless overridden by item name colors
            String colorCode = "§" + signShopConfig.getTextColor().getChar();
            String displayText = colorCode + count + (itemName.isEmpty() ? formatMaterialName(entry.getKey()) : itemName);

            // Conditionally add extra details in visible text (always shown in hover tooltip)
            if (signShopConfig.getShowItemDetailsInChat()) {
                // Add book info if applicable
                if (isWriteableBook(entry.getKey())) {
                    IBookItem book = BookFactory.getBookItem(entry.getKey());
                    if (book != null && (book.getAuthor() != null || book.getTitle() != null)) {
                        displayText += " (" + (book.getTitle() == null ? "Unknown" : book.getTitle()) +
                                      " by " + (book.getAuthor() == null ? "Unknown" : book.getAuthor()) + ")";
                    }
                }

                // Add lore preview if applicable
                if (entry.getKey().hasItemMeta() && entry.getKey().getItemMeta().hasLore()) {
                    StringBuilder lorePreview = new StringBuilder(colorCode + " <");
                    boolean firstLore = true;
                    for (String loreLine : entry.getKey().getItemMeta().getLore()) {
                        if (firstLore) firstLore = false;
                        else lorePreview.append(colorCode).append(", ").append(ChatColor.RESET);
                        lorePreview.append(loreLine);  // Keep original lore colors!
                    }
                    lorePreview.append(colorCode).append("> ").append(ChatColor.RESET);
                    displayText += lorePreview.toString();
                }
            }

            // Add trailing white color to match itemStackToString behavior
            displayText += ChatColor.WHITE;

            // Parse legacy color codes into components
            // This preserves color codes from custom item names while applying default color
            BaseComponent itemComponent = TextComponent.fromLegacy(displayText);

            // Add hover event showing full item tooltip (if enabled in config)
            if (signShopConfig.getShowItemHovers()) {
                try {
                    // Create a copy of the item with the correct amount for tooltip
                    ItemStack hoverItem = entry.getKey().clone();
                    hoverItem.setAmount(entry.getValue());

                    HoverEvent hoverEvent = null;

                    // Try Paper's serializeItemAsJson() first for full tooltip support (1.20.5+)
                    // Uses reflection since this method only exists on Paper, not pure Spigot
                    java.lang.reflect.Method serializeMethod;
                    try {
                        // Call Bukkit.getUnsafe().serializeItemAsJson(item) via reflection
                        serializeMethod = Bukkit.getUnsafe().getClass()
                            .getMethod("serializeItemAsJson", ItemStack.class);
                        JsonObject itemJson = (JsonObject) serializeMethod.invoke(Bukkit.getUnsafe(), hoverItem);

                        if (itemJson != null) {
                            String itemId = itemJson.get("id").getAsString();
                            int itemCount = itemJson.get("count").getAsInt();
                            JsonElement components = itemJson.get("components");

                            if (components != null) {
                                // Paper server with Data Components - full tooltip support!
                                hoverEvent = new HoverEvent(
                                    HoverEvent.Action.SHOW_ITEM,
                                    new PaperItemHoverContent(itemId, itemCount, components)
                                );
                            }
                        }
                    } catch (NoSuchMethodException e) {
                        // Not a Paper server, fall through to Spigot method
                    } catch (Exception e) {
                        // Paper method failed, fall through to Spigot method
                    }

                    // Fallback to Spigot method if Paper method didn't work
                    if (hoverEvent == null) {
                        // Serialize the item's data for the hover tooltip
                        // NOTE: Due to a known BungeeCord limitation (issue #3688), hover tooltips
                        // only show the item type on Spigot 1.20.5+/1.21, not enchantments/lore/etc.
                        // This will be fixed when BungeeCord updates ItemTag.ofNbt() to support
                        // the new Data Components format introduced in Minecraft 1.20.5.
                        String itemData = null;
                        if (hoverItem.hasItemMeta()) {
                            ItemMeta meta = hoverItem.getItemMeta();
                            if (meta != null) {
                                try {
                                    String metaString = meta.getAsString();
                                    if (metaString != null && !metaString.isEmpty()) {
                                        itemData = metaString;
                                    }
                                } catch (Exception e) {
                                    // Serialization failed, use empty data
                                }
                            }
                        }

                        // Create the hover event with the item data
                        // Use empty tag "{}" if no metadata, otherwise use serialized NBT
                        hoverEvent = new HoverEvent(
                            HoverEvent.Action.SHOW_ITEM,
                            new net.md_5.bungee.api.chat.hover.content.Item(
                                hoverItem.getType().getKey().toString(),
                                hoverItem.getAmount(),
                                net.md_5.bungee.api.chat.ItemTag.ofNbt(itemData != null ? itemData : "{}")
                            )
                        );
                    }

                    itemComponent.setHoverEvent(hoverEvent);
                } catch (Exception e) {
                    // If hover fails, just use the text without hover (fallback gracefully)
                    // This ensures the message still displays even if hover creation fails
                }
            }

            builder.append(itemComponent);
        }

        return new TextComponent(builder.create());
    }

    @SuppressWarnings("deprecation")
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

    /**
     * Sets the color of a shop sign's first line to indicate stock status.
     *
     * @param sign The sign block to update
     * @param color The color to apply (typically blue=stocked, red=out of stock)
     */
    public static void setSignStatus(Block sign, ChatColor color) {
        if(clickedSign(sign)) {
            Sign signblock = ((Sign) sign.getState());
            String[] sLines = signblock.getSide(Side.FRONT).getLines();
            if(ChatColor.stripColor(sLines[0]).length() <= 14) {
                signblock.getSide(Side.FRONT).setLine(0, (color + ChatColor.stripColor(sLines[0])));
                if (ChatColor.stripColor(signblock.getSide(Side.BACK).getLine(0)).equals(ChatColor.stripColor(sLines[0]))) {
                    signblock.getSide(Side.BACK).setLine(0, (color + ChatColor.stripColor(sLines[0])));
                }
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
            if (signShopConfig.getAllowUnsafeEnchantments()) {
                try {
                    isEnchantMe.addUnsafeEnchantments(enchantments);
                } catch (IllegalArgumentException exfinal) {
                    return false;
                }
            }
            else
                return false;
        }
        return true;
    }

    /**
     * Converts an ItemStack array to a map of item type to total quantity.
     * Used for consolidating and counting items across multiple stacks.
     *
     * @param isStacks Array of ItemStacks to consolidate
     * @return Map where key is item (amount=1) and value is total count
     */
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

    /**
     * Creates a deep copy of an ItemStack array (clones each item).
     *
     * @param isOriginal Original array to clone
     * @return New array with cloned ItemStacks, or null if input is null
     */
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

    /**
     * Updates sign color based on stock status for a specific shop.
     * Called after every transaction - optimization target if shop operations lag.
     */
    public static void updateStockStatusPerShop(Seller pSeller) {
        if(pSeller != null) {
            Block pSign = pSeller.getSign();
            if(pSign == null || !(pSign.getState() instanceof Sign))
                return;
            String[] sLines = ((Sign) pSign.getState()).getSide(Side.FRONT).getLines();
            if (signShopConfig.getBlocks(signshopUtil.getOperation(sLines[0])).isEmpty())
                return;
            List<String> operation = signShopConfig.getBlocks(signshopUtil.getOperation(sLines[0]));
            List<SignShopOperationListItem> SignShopOperations = signshopUtil.getSignShopOps(operation);
            if(SignShopOperations == null)
                return;
            SignShopArguments ssArgs = new SignShopArguments(economyUtil.parsePrice(sLines[3]), pSeller.getItems(), pSeller.getContainables(), pSeller.getActivatables(),
                                                                null, null, pSign, signshopUtil.getOperation(sLines[0]), null, Action.RIGHT_CLICK_BLOCK, SignShopArgumentsType.Check);
            if(pSeller.getRawMisc() != null)
                ssArgs.miscSettings = pSeller.getRawMisc();
            ssArgs.setSeller(pSeller);  // Set seller reference for cached item access
            Boolean reqOK = true;
            for(SignShopOperationListItem ssOperation : SignShopOperations) {
                ssArgs.setOperationParameters(ssOperation.getParameters());
                reqOK = ssOperation.getOperation().checkRequirements(ssArgs, false);
                if(!reqOK) {
                    itemUtil.setSignStatus(pSign, signShopConfig.getOutOfStockColor());
                    break;
                }
            }
            if(reqOK)
                itemUtil.setSignStatus(pSign, signShopConfig.getInStockColor());
        }
    }

    /**
     * Updates stock status by checking all chests linked to a shop.
     * Called after every transaction to update sign color.
     * If shop operations feel slow, this is a candidate for profiling.
     */
    public static void updateStockStatus(Block bSign, ChatColor ccColor) {
        Seller seTemp = Storage.get().getSeller(bSign.getLocation());
        if(seTemp != null) {
            List<Block> iChests = seTemp.getContainables();
            for(Block bHolder : iChests)
                updateStockStatusPerChest(bHolder, bSign);
        }
        setSignStatus(bSign, ccColor);
    }

    public static Boolean clickedSign(Block bBlock) {
        return (Tag.ALL_SIGNS.isTagged(bBlock.getType()));
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


    /**
     * Deserializes a list of serialized item strings back to ItemStacks.
     *
     * @param itemStringList List of serialized item strings from sellers.yml
     * @return Array of deserialized ItemStacks (may contain nulls if deserialization fails)
     * @see ItemSerializer#deserialize
     */
    public static ItemStack[] convertStringtoItemStacks(List<String> itemStringList) {
        ItemStack[] itemStacks = new ItemStack[itemStringList.size()];

        for (int i = 0; i < itemStringList.size(); i++) {
            itemStacks[i] = ItemSerializer.deserialize(itemStringList.get(i));
        }
        return itemStacks;
    }

    /**
     * Checks if an ItemStack array contains any null items.
     *
     * <p>Null items can occur when:</p>
     * <ul>
     *   <li>Item deserialization fails (incompatible items)</li>
     *   <li>Corrupted shop data</li>
     *   <li>Items that can't be loaded in current Spigot version</li>
     * </ul>
     *
     * @param items The ItemStack array to check (null-safe)
     * @return true if array contains at least one null item, false otherwise
     * @since 5.2.0
     */
    public static boolean hasNullItems(ItemStack[] items) {
        if (items == null) {
            return false;
        }

        for (ItemStack item : items) {
            if (item == null) {
                return true;
            }
        }

        return false;
    }

    /**
     * Serializes an array of ItemStacks to strings for persistence in sellers.yml.
     *
     * @param itemStackArray Items to serialize
     * @return Array of serialized strings (YAML: or LEGACY: prefixed)
     * @see ItemSerializer#serialize
     */
    public static String[] convertItemStacksToString(ItemStack[] itemStackArray) {
        List<String> itemStringList = new ArrayList<>();
        if (itemStackArray == null) {
            return new String[0];
        }

        for (ItemStack item : itemStackArray) {
            if (item != null) {
                String serialized = ItemSerializer.serialize(item);
                if (serialized != null) {
                    itemStringList.add(serialized);
                }
            }
        }
        return itemStringList.toArray(new String[0]);
    }


    public static boolean isWriteableBook(ItemStack item) {
        if (item == null) return false;
        return (item.getType() == Material.getMaterial("WRITTEN_BOOK") || item.getType() == Material.getMaterial("BOOK_AND_QUILL"));
    }


    /**
     * Compares two ItemStacks for equality, optionally ignoring durability.
     * Used for trade shop item matching.
     *
     * @param a First ItemStack to compare
     * @param b Second ItemStack to compare
     * @param ignoredur If true, ignores durability differences
     * @return true if items are equal based on type, enchantments, and metadata
     */
    @SuppressWarnings("deprecation")
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

    /**
     * Ensures chunks around a block are loaded within a given radius.
     * Critical for shop operations when chests may be in unloaded chunks.
     *
     * @param block Center block
     * @param radius Number of chunk-sized steps to load in each direction
     */
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

    /**
     * Ensures the chunk containing a block is loaded.
     *
     * @param block Block whose chunk should be loaded
     * @return true if chunk is now loaded, false if load failed or block is null
     */
    public static boolean loadChunkByBlock(Block block) {
        if(block == null)
            return false;
        Chunk chunk = block.getChunk();
        if (!chunk.isLoaded())
            return chunk.load();
        return true; // Chunk already loaded
    }

    /**
     * Custom HoverEvent Content class for Paper's Data Components format (1.20.5+).
     * This enables full item tooltips (enchantments, lore, etc.) on Paper servers
     * while maintaining graceful fallback on pure Spigot servers.
     * Based on solution from BungeeCord issue #3688 (OstlerDev).
     */
    private static final class PaperItemHoverContent extends Content {
        private final String id;
        private final int count;
        private final JsonElement components;

        private PaperItemHoverContent(String id, int count, JsonElement components) {
            this.id = id;
            this.count = count;
            this.components = components;
        }

        @Override
        public HoverEvent.Action requiredAction() {
            return HoverEvent.Action.SHOW_ITEM;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            PaperItemHoverContent that = (PaperItemHoverContent) o;
            return count == that.count &&
                   java.util.Objects.equals(id, that.id) &&
                   java.util.Objects.equals(components, that.components);
        }

        @Override
        public int hashCode() {
            return java.util.Objects.hash(id, count, components);
        }
    }
}
