
package org.wargamer2010.signshop.blocks;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import org.bukkit.Color;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.*;
import org.bukkit.material.Skull;
import org.wargamer2010.signshop.SignShop;

import org.wargamer2010.signshop.util.signshopUtil;
public class SignShopItemMeta {
    private static final String listSeperator = "~";
    private static String filename = "books.db";

    private SignShopItemMeta() {

    }

    public static void init() {
        SSDatabase db = new SSDatabase(filename);

        try {
            if(!db.tableExists("ItemMeta"))
                db.runStatement("CREATE TABLE ItemMeta ( ItemMetaID INTEGER, ItemMetaHash INT, PRIMARY KEY(ItemMetaID) )", null, false);
            if(!db.tableExists("MetaProperty"))
                db.runStatement("CREATE TABLE MetaProperty ( PropertyID INTEGER, ItemMetaID INTEGER, PropertyName TEXT NOT NULL, ProperyValue TEXT NOT NULL, PRIMARY KEY(PropertyID) )", null, false);
        } finally {
            db.close();
        }
    }


    public static void setMetaForID(ItemStack stack, Integer ID) {
        SSDatabase db = new SSDatabase(filename);

        Map<Integer, Object> pars = new LinkedHashMap<Integer, Object>();
        pars.put(1, ID);

        Map<String, String> metamap = new LinkedHashMap<String, String>();
        ItemMeta meta = stack.getItemMeta();

        ResultSet setprops = (ResultSet)db.runStatement("SELECT PropertyName, ProperyValue FROM MetaProperty WHERE ItemMetaID = ?;", pars, true);
        try {
            while(setprops.next())
                metamap.put(setprops.getString("PropertyName"), setprops.getString("ProperyValue"));
        } catch(SQLException ex) {
            return;
        }

        if(metamap.isEmpty())
            return;

        if(!getPropValue("displayname", metamap).isEmpty())
            meta.setDisplayName(getPropValue("displayname", metamap));
        if(!getPropValue("lore", metamap).isEmpty()) {
            List<String> temp = Arrays.asList(getPropValue("lore", metamap).split(listSeperator));
            meta.setLore(temp);
        }
        if(!getPropValue("enchants", metamap).isEmpty()) {
            for(Map.Entry<Enchantment, Integer> enchant : signshopUtil.convertStringToEnchantments(getPropValue("enchants", metamap)).entrySet()) {
                meta.addEnchant(enchant.getKey(), enchant.getValue(), true);
            }
        }

        List<MetaType> metatypes = getTypesOfMeta(meta);

        try {
            for(MetaType type : metatypes) {
                if(type == MetaType.EnchantmentStorage) {
                    EnchantmentStorageMeta enchantmentmeta = (EnchantmentStorageMeta) meta;
                    if(getPropValue("storedenchants", metamap).isEmpty()) {
                        for(Map.Entry<Enchantment, Integer> enchant : signshopUtil.convertStringToEnchantments(getPropValue("storedenchants", metamap)).entrySet()) {
                            enchantmentmeta.addStoredEnchant(enchant.getKey(), enchant.getValue(), true);
                        }
                    }
                }
                else if(type == MetaType.LeatherArmor) {
                    LeatherArmorMeta leathermeta = (LeatherArmorMeta) meta;
                    if(!getPropValue("color", metamap).isEmpty())
                        leathermeta.setColor(Color.fromRGB(Integer.getInteger(getPropValue("color", metamap))));
                }
                else if(type == MetaType.Map) {
                    MapMeta mapmeta = (MapMeta) meta;
                    if(!getPropValue("scaling", metamap).isEmpty())
                        mapmeta.setScaling(Boolean.valueOf(getPropValue("scaling", metamap)));
                }
                else if(type == MetaType.Repairable) {
                    Repairable repairmeta = (Repairable) meta;
                    if(!getPropValue("repaircost", metamap).isEmpty())
                        repairmeta.setRepairCost(Integer.getInteger(getPropValue("repaircost", metamap)));
                }
                else if(type == MetaType.Skull) {
                    SkullMeta skullmeta = (SkullMeta) meta;
                    if(!getPropValue("owner", metamap).isEmpty())
                        skullmeta.setOwner(getPropValue("owner", metamap));
                }
            }
        } catch(ClassCastException ex) {

        } catch(NumberFormatException ex) {

        }


        stack.setItemMeta(meta);
    }

    public static Integer storeMeta(ItemStack stack) {
        SSDatabase db = new SSDatabase(filename);
        Map<String, String> metamap = getMetaAsMap(stack.getItemMeta());

        try {
            Integer existingID = getMetaID(stack, metamap);
            if(existingID > -1)
                return existingID;

            Integer itemmetaid;
            Map<Integer, Object> pars = new LinkedHashMap<Integer, Object>();
            pars.put(1, metamap.hashCode());

            itemmetaid = (Integer)db.runStatement("INSERT INTO ItemMeta(ItemMetaHash) VALUES (?);", pars, false);

            if(itemmetaid == -1)
                return -1;

            for(Map.Entry<String, String> metaproperty : metamap.entrySet()) {
                pars.clear();
                pars.put(1, itemmetaid);
                pars.put(2, metaproperty.getKey());
                pars.put(3, metaproperty.getValue());
                db.runStatement("INSERT INTO MetaProperty(ItemMetaID, PropertyName, ProperyValue) VALUES (?, ?, ?);", pars, false);
            }

            return itemmetaid;
        } finally {
            db.close();
        }
    }

    public static Integer getMetaID(ItemStack stack) {
        return getMetaID(stack, null);
    }

    private static Integer getMetaID(ItemStack stack, Map<String, String> pMetamap) {
        Map<String, String> metamap = (pMetamap != null ? pMetamap : getMetaAsMap(stack.getItemMeta()));
        SSDatabase db = new SSDatabase(filename);
        try {
            Map<Integer, Object> pars = new LinkedHashMap<Integer, Object>();
            pars.put(1, metamap.hashCode());
            ResultSet set = (ResultSet)db.runStatement("SELECT ItemMetaID FROM ItemMeta WHERE ItemMetaHash = ?;", pars, true);
            if(set.next())
                return set.getInt("ItemMetaID");
        } catch (SQLException ex) {

        } finally {
            db.close();
        }

        return -1;
    }

    private static Map<String, String> getMetaAsMap(ItemMeta meta) {
        Map<String, String> metamap = new LinkedHashMap<String, String>();
        List<MetaType> types = getTypesOfMeta(meta);

        if(meta.getDisplayName() != null)
            metamap.put("displayname", meta.getDisplayName());
        if(meta.getEnchants() != null && !meta.getEnchants().isEmpty())
            metamap.put("enchants", signshopUtil.convertEnchantmentsToString(meta.getEnchants()));
        if(meta.getLore() != null && meta.getLore().isEmpty()) {
            String lorearr[] = new String[meta.getLore().size()];
            metamap.put("lore", signshopUtil.implode(meta.getLore().toArray(lorearr), listSeperator));
        }

        for(MetaType type : types) {
            if(type == MetaType.EnchantmentStorage) {
                EnchantmentStorageMeta enchantmentmeta = (EnchantmentStorageMeta) meta;
                if(enchantmentmeta.hasStoredEnchants())
                    metamap.put("storedenchants", signshopUtil.convertEnchantmentsToString(enchantmentmeta.getStoredEnchants()));
            }
            else if(type == MetaType.LeatherArmor) {
                LeatherArmorMeta leathermeta = (LeatherArmorMeta) meta;
                metamap.put("color", Integer.toString(leathermeta.getColor().asRGB()));
            }
            else if(type == MetaType.Map) {
                MapMeta mapmeta = (MapMeta) meta;
                metamap.put("scaling", Boolean.toString(mapmeta.isScaling()));
            }
            else if(type == MetaType.Skull) {
                SkullMeta skullmeta = (SkullMeta) meta;
                if(skullmeta.hasOwner()) {
                    metamap.put("owner", skullmeta.getOwner());
                }
            }
            else if(type == MetaType.Repairable) {
                Repairable repairmeta = (Repairable) meta;
                if(repairmeta.hasRepairCost()) {
                    metamap.put("repaircost", Integer.toString(repairmeta.getRepairCost()));
                }
            }
        }

        return metamap;
    }

    private static List<MetaType> getTypesOfMeta(ItemMeta meta) {
        List<MetaType> types = new LinkedList<MetaType>();

        if(meta instanceof org.bukkit.inventory.meta.EnchantmentStorageMeta)
            types.add(MetaType.EnchantmentStorage);
        if(meta instanceof org.bukkit.inventory.meta.LeatherArmorMeta)
            types.add(MetaType.LeatherArmor);
        if(meta instanceof org.bukkit.inventory.meta.MapMeta)
            types.add(MetaType.Map);
        if(meta instanceof org.bukkit.inventory.meta.SkullMeta)
            types.add(MetaType.Skull);
        if(meta instanceof org.bukkit.inventory.meta.Repairable)
            types.add(MetaType.Repairable);

        return types;
    }

    private static String getPropValue(String name, Map<String, String> metamap) {
        if(metamap.containsKey(name)) {
            return metamap.get(name);
        } else {
            return "";
        }
    }

    private static enum MetaType {
        EnchantmentStorage,
        LeatherArmor,
        Map,
        Repairable,
        Skull,
        Stock,
    }
}
