package org.wargamer2010.signshop;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.block.sign.Side;
import org.bukkit.inventory.ItemStack;
import org.wargamer2010.signshop.data.SignShopBooks;
import org.wargamer2010.signshop.data.SignShopItemMeta;
import org.wargamer2010.signshop.operations.SignShopArguments;
import org.wargamer2010.signshop.player.PlayerCache;
import org.wargamer2010.signshop.player.PlayerIdentifier;
import org.wargamer2010.signshop.player.SignShopPlayer;
import org.wargamer2010.signshop.util.SSTimeUtil;
import org.wargamer2010.signshop.util.itemUtil;
import org.wargamer2010.signshop.util.signshopUtil;

import java.util.*;

/**
 * Represents a single SignShop instance in the game world.
 * <p>
 * A Seller (shop) is the core data model for SignShop, containing all information about a physical shop
 * including its owner, location, linked blocks, items, and configuration settings. Each shop is uniquely
 * identified by its sign's Location.
 * </p>
 *
 * <h2>Key Properties:</h2>
 * <ul>
 *   <li><b>Owner:</b> The player who created and owns this shop ({@link SignShopPlayer})</li>
 *   <li><b>Sign Location:</b> The physical location of the shop's sign (primary identifier)</li>
 *   <li><b>Containables:</b> Linked storage blocks (chests, barrels, etc.) that hold shop inventory</li>
 *   <li><b>Activatables:</b> Linked interactive blocks (levers, buttons, etc.) triggered by shop operations</li>
 *   <li><b>Items:</b> Template items defining what this shop trades (prices, stock, etc.)</li>
 *   <li><b>Misc Properties:</b> Shop-specific configuration (price multipliers, messages, Trade shop templates, etc.)</li>
 * </ul>
 *
 * <h2>Serialization Format:</h2>
 * <ul>
 *   <li><b>YAML Format:</b> Modern format (DataVersion 4+) using Bukkit's YAML serialization with Base64 NBT.</li>
 *   <li><b>LEGACY Format:</b> Old format using Java object serialization. Used for backward compatibility
 *       and for shops with incompatible items (e.g., player heads with empty names in Spigot 1.21.10+).</li>
 * </ul>
 *
 * <h2>Transient Cache:</h2>
 * <p>
 * The {@code miscItemsCache} field provides performance optimization for Trade shops and other operations
 * that frequently access deserialized items from misc properties. Items are cached in memory after first
 * deserialization, avoiding repeated YAML parsing overhead (1-5ms per deserialization).
 * </p>
 *
 * @see org.wargamer2010.signshop.data.Storage
 * @see org.wargamer2010.signshop.util.DataConverter
 * @see org.wargamer2010.signshop.incompatibility.IncompatibilityChecker
 */
public class Seller {
    private List<Block> containables;
    private List<Block> activatables;
    private ItemStack[] isItems;
    private final Location signLocation;
    private final Map<String, String> miscProps = new HashMap<>();
    private final Map<String, String> volatileProperties = new LinkedHashMap<>();
    private Map<String, Object> serializedData = new HashMap<>();

    // Cache for deserialized misc items (chest1, chest2, etc.) to avoid repeated deserialization
    // Transient = not serialized to disk, rebuilt from miscProps as needed
    private transient final Map<String, ItemStack[]> miscItemsCache = new HashMap<>();

    private SignShopPlayer owner;
    private final String world;

    public Seller(PlayerIdentifier playerId, String sWorld, List<Block> pContainables, List<Block> pActivatables, ItemStack[] isChestItems, Location location,
            Map<String, String> pMiscProps, Boolean save) {
        owner = PlayerCache.getPlayer(playerId);
        world = sWorld;

        isItems = itemUtil.getBackupItemStack(isChestItems);
        containables = pContainables;
        activatables = pActivatables;
        signLocation = location;
        if(pMiscProps != null)
            miscProps.putAll(pMiscProps);
        if(save)
            storeMeta(isItems);

        calculateSerialization();
    }

    public ItemStack[] getItems() {
        return getItems(true);
    }

    public ItemStack[] getItems(boolean backup) {
        if(backup)
            return itemUtil.getBackupItemStack(isItems);
        else
            return isItems;
    }

    public void setItems(ItemStack[] items) {
        isItems = items;
        calculateSerialization();
    }

    public List<Block> getContainables() {
        return containables;
    }

    public void setContainables(List<Block> blocklist) {
        containables = blocklist;
        calculateSerialization();
    }

    public List<Block> getActivatables() {
        return activatables;
    }

    public void setActivatables(List<Block> blocklist) {
        activatables = blocklist;
        calculateSerialization();
    }

    public SignShopPlayer getOwner() {
        return owner;
    }

    public void setOwner(SignShopPlayer newowner) {
        owner = newowner;
        calculateSerialization();
    }

    public boolean isOwner(SignShopPlayer player) {
        return player.compareTo(owner);
    }

    public String getWorld() {
        return world;
    }

    public boolean hasMisc(String key) {
        return miscProps.containsKey(key);
    }

    public void removeMisc(String key) {
        miscProps.remove(key);
        miscItemsCache.remove(key);  // Invalidate cache for this key
        calculateSerialization();
    }

    public void addMisc(String key, String value) {
        miscProps.put(key, value);
        miscItemsCache.remove(key);  // Invalidate cache for this key
        calculateSerialization();
    }

    public void setMiscSettings(Map<String, String> newMiscSettings) {
        if (newMiscSettings != null) {
            miscProps.putAll(newMiscSettings);
            miscItemsCache.clear();  // Invalidate entire cache when bulk updating
            calculateSerialization();
        }
    }

    /**
     * Gets deserialized items from misc settings, using cache to avoid repeated deserialization.
     * @param key The misc key (e.g., "chest1", "chest2")
     * @return Deserialized ItemStack array, or null if key doesn't exist or deserialization fails
     */
    public ItemStack[] getCachedMiscItems(String key) {
        // Check cache first
        if (miscItemsCache.containsKey(key)) {
            return miscItemsCache.get(key);
        }

        // Not in cache - deserialize from miscProps
        String serialized = miscProps.get(key);
        if (serialized == null) {
            return null;
        }

        // Deserialize (may contain multiple items separated by separator)
        String[] serializedItems;
        if (!serialized.contains(SignShopArguments.separator)) {
            serializedItems = new String[]{serialized};
        } else {
            serializedItems = serialized.split(SignShopArguments.separator);
        }

        ItemStack[] items = itemUtil.convertStringtoItemStacks(Arrays.asList(serializedItems));

        // Cache the result (even if null, to avoid repeated failed deserializations)
        miscItemsCache.put(key, items);

        return items;
    }

    public String getMisc(String key) {
        if(miscProps.containsKey(key))
            return miscProps.get(key);
        return null;
    }

    public Map<String, String> getRawMisc() {
        return miscProps;
    }

    public static void storeMeta(ItemStack[] stacks) {
        if(stacks == null)
            return;
        for(ItemStack stack : stacks) {
            if(itemUtil.isWriteableBook(stack)) {
                SignShopBooks.addBook(stack);
            }
            SignShopItemMeta.storeMeta(stack);
        }
    }

    public String getVolatile(String key) {
        if(volatileProperties.containsKey(key))
            return volatileProperties.get(key);
        return null;
    }

    public void setVolatile(String key, String value) {
        volatileProperties.put(key, value);
    }

    public Block getSign() {
        return signLocation.getBlock();
    }

    public Location getSignLocation() {
        return signLocation;
    }

    public String getOperation() {
        Block block = getSign();
        if(block == null)
            return "";
        if(itemUtil.clickedSign(block)) {
            Sign sign = (Sign) block.getState();
            return signshopUtil.getOperation(sign.getSide(Side.FRONT).getLine(0));
        }
        return "";
    }

    public void reloadBlocks() {
        List<Block> tempContainables = new LinkedList<>();
        List<Block> tempActivatables = new LinkedList<>();
        for(Block a : containables)
            tempContainables.add(a.getWorld().getBlockAt(a.getX(), a.getY(), a.getZ()));
        for(Block b : activatables)
            tempActivatables.add(b.getWorld().getBlockAt(b.getX(), b.getY(), b.getZ()));
        containables = tempContainables;
        activatables = tempActivatables;
        calculateSerialization();
    }

    public Map<String, Object> getSerializedData() {
        return serializedData;
    }

    private void calculateSerialization() {
        Map<String, Object> temp = new HashMap<>();

        temp.put("shopworld", getWorld());
        temp.put("owner", getOwner().GetIdentifier().toString());
        temp.put("items", itemUtil.convertItemStacksToString(getItems(false)));

        String[] sContainables = new String[containables.size()];
        for(int i = 0; i < containables.size(); i++)
            sContainables[i] = signshopUtil.convertLocationToString(containables.get(i).getLocation());
        temp.put("containables", sContainables);

        String[] sActivatables = new String[activatables.size()];
        for(int i = 0; i < activatables.size(); i++)
            sActivatables[i] = signshopUtil.convertLocationToString(activatables.get(i).getLocation());
        temp.put("activatables", sActivatables);

        temp.put("sign", signshopUtil.convertLocationToString(getSignLocation()));

        Map<String, String> misc = miscProps;
        if(!misc.isEmpty())
            temp.put("misc", MapToList(misc));

        serializedData = temp;
    }

    private List<String> MapToList(Map<String, String> map) {
        List<String> returnList = new LinkedList<>();
        for(Map.Entry<String, String> entry : map.entrySet())
            returnList.add(entry.getKey() + ":" + entry.getValue());
        return returnList;
    }


    public String getInfo(){
        String newLine = "\n";
        StringBuilder sb = new StringBuilder();
        sb.append("--ShopInfo--").append(newLine)
                .append("  Owner: ").append(owner.getName()).append(" LastSeen: ").append(SSTimeUtil.getDateTimeFromLong(owner.getOfflinePlayer().getLastPlayed())).append(newLine)
                .append("  Sign Location: ").append(signshopUtil.convertLocationToString(getSignLocation())).append(newLine)
                .append("  Container Locations: ");
        for (Block block: containables){
            sb.append("  ").append(signshopUtil.convertLocationToString(block.getLocation())).append(" ");
        }
        sb.append(newLine)
                .append("  Activatable Locations: ");
        for (Block block: activatables){
            sb.append("  ").append(signshopUtil.convertLocationToString(block.getLocation())).append(" ");
        }
        sb.append(newLine)
                .append("  Misc: ");
        for (String key : miscProps.keySet())
            sb.append("  ").append(key).append(": ").append(miscProps.get(key)).append(" ");

        return sb.toString();
    }
}
