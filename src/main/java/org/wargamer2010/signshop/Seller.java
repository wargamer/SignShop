package org.wargamer2010.signshop;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.block.sign.Side;
import org.bukkit.inventory.ItemStack;
import org.wargamer2010.signshop.blocks.SignShopBooks;
import org.wargamer2010.signshop.blocks.SignShopItemMeta;
import org.wargamer2010.signshop.player.PlayerCache;
import org.wargamer2010.signshop.player.PlayerIdentifier;
import org.wargamer2010.signshop.player.SignShopPlayer;
import org.wargamer2010.signshop.util.SSTimeUtil;
import org.wargamer2010.signshop.util.itemUtil;
import org.wargamer2010.signshop.util.signshopUtil;

import java.util.*;


public class Seller {
    private List<Block> containables;
    private List<Block> activatables;
    private ItemStack[] isItems;
    private final Location signLocation;
    private final Map<String, String> miscProps = new HashMap<>();
    private final Map<String, String> volatileProperties = new LinkedHashMap<>();
    private Map<String, Object> serializedData = new HashMap<>();

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
        calculateSerialization();
    }

    public void addMisc(String key, String value) {
        miscProps.put(key, value);
        calculateSerialization();
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
        if(misc.size() > 0)
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
