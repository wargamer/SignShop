
package org.wargamer2010.signshop.blocks;

import com.flobi.WhatIsIt.WhatIsIt;
import com.google.common.collect.ImmutableList;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.FireworkEffect.Builder;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.*;
import org.bukkit.potion.Potion;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;
import org.wargamer2010.signshop.configuration.ColorUtil;
import org.wargamer2010.signshop.util.SSBukkitVersion;
import org.wargamer2010.signshop.util.WebUtil;
import org.wargamer2010.signshop.util.itemUtil;
import static org.wargamer2010.signshop.util.itemUtil.enchantmentsToMessageFormat;
import org.wargamer2010.signshop.util.signshopUtil;
import org.wargamer2010.signshop.util.versionUtil;

public class SignShopItemMeta {
    private static final String listSeperator = "~";
    private static final String valueSeperator = "-";
    private static final String innerListSeperator = "^";
    private static final ChatColor txtColor = ChatColor.YELLOW;
    private static final String filename = "books.db";
    private static Boolean legacy = false;

    private SignShopItemMeta() {

    }

    public static void init() {
        legacy = (versionUtil.getBukkitVersionType() == SSBukkitVersion.Pre145);
        if(legacy)
            return;

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

    public static String convertColorsToDisplay(List<Color> colors) {
        if(colors == null || colors.isEmpty())
            return "";
        List<String> temp = new LinkedList<String>();

        for(Color color : colors) {
            temp.add(ColorUtil.getColorAsString(color));
        }

        String[] arr = new String[temp.size()];
        return signshopUtil.implode(temp.toArray(arr), ", ");
    }

    public static ChatColor getTextColor() {
        return txtColor;
    }

    private static String convertFireworkTypeToDisplay(FireworkEffect.Type type) {
        String temp = signshopUtil.capFirstLetter(type.toString().toLowerCase()).replace("_", " ");
        if(temp.contains(" ")) {
            String[] temparr = temp.split(" ");
            String bak = temparr[0]; temparr[0] = temparr[1];
            temparr[1] = bak;
            temp = signshopUtil.implode(temparr, " ");
        }
        return signshopUtil.capFirstLetter(temp);
    }

    private static boolean hasMeta(ItemStack stack) {
        // This seems silly but some parts of the code below function when an item has no meta data but itemmeta is not null
        return (stack.getItemMeta() != null);
    }

    private static String getDisplayName(ItemStack stack) {
        return getDisplayName(stack, txtColor);
    }

    private static String getDisplayName(ItemStack stack, ChatColor color) {
        String nameFromWeb = WebUtil.getNameFromWeb(stack);
        String txtcolor = txtColor.toString();
        String customcolor = (stack.getEnchantments().isEmpty() ? color.toString() : ChatColor.DARK_PURPLE.toString());
        String nameFromWhatIsIt = "Unknown";
        if(Bukkit.getServer().getPluginManager().isPluginEnabled("WhatIsIt"))
        	nameFromWhatIsIt = WhatIsIt.itemName(stack);
        String itemName = nameFromWhatIsIt.isEmpty() || (nameFromWhatIsIt.compareTo("Unknown")) == 0 ? nameFromWeb : nameFromWhatIsIt;
        String normal = itemName.isEmpty() ? itemUtil.formatData(stack.getData(), stack.getDurability()) : itemName;
        String displayname = "";

        if(!isLegacy()) {
            if(stack.getItemMeta() != null) {
                String custom = (stack.getItemMeta().hasDisplayName()
                            ? (txtcolor + "\"" + customcolor + stack.getItemMeta().getDisplayName() + txtcolor + "\"") : "");
                if(custom.length() > 0)
                    displayname = (custom + " (" + normal + ")" + txtcolor);
            }
        }
        if(displayname.isEmpty())
            displayname = (txtcolor + customcolor + normal + txtcolor);

        if(stack.getType().getMaxDurability() >= 30 && stack.getDurability() != 0)
            displayname = (" Damaged " + displayname);
        if(stack.getEnchantments().size() > 0)
            displayname += (txtcolor + " " + enchantmentsToMessageFormat(stack.getEnchantments()));

        return displayname;
    }

    public static String getName(ItemStack stack) {
        if(isLegacy() || !hasMeta(stack))
            return getDisplayName(stack);

        ItemMeta meta = stack.getItemMeta();

        List<MetaType> metatypes = getTypesOfMeta(meta);
        for(MetaType type : metatypes) {
            if(type == MetaType.EnchantmentStorage) {
                EnchantmentStorageMeta enchantmeta = (EnchantmentStorageMeta) meta;
                if(enchantmeta.hasStoredEnchants())
                    return (getDisplayName(stack, ChatColor.DARK_PURPLE) + " " + itemUtil.enchantmentsToMessageFormat(enchantmeta.getStoredEnchants()));
            } else if(type == MetaType.LeatherArmor) {
                LeatherArmorMeta leathermeta = (LeatherArmorMeta) meta;
                return (ColorUtil.getColorAsString(leathermeta.getColor()) + " Colored " + getDisplayName(stack));
            } else if(type == MetaType.Skull) {
                String postfix = "'s Head";
                SkullMeta skullmeta = (SkullMeta) meta;
                if(skullmeta.getOwner() != null) {
                    // Name coloring support had to be dropped since there is no more link between
                    // the skull owner and the actual player
                    return (skullmeta.getOwner() + postfix);
                } else {
                    // We can no longer get a pretty name by ID (SKULL_ITEM isn't pretty, is it?)
                    // So we'll have to rely on the web lookup, if the server owner has it enabled
                    return getDisplayName(stack);
                }
            } else if(type == MetaType.Potion) {
                PotionMeta potionmeta = (PotionMeta) meta;

                boolean first = true;
                StringBuilder namebuilder = new StringBuilder(512);
                namebuilder.append(getDisplayName(stack, ChatColor.DARK_PURPLE));

                Collection<PotionEffect> effects = null;
                Potion pot = null;
                if(!potionmeta.hasCustomEffects()) {
                    try {
                        pot = Potion.fromItemStack(stack);
                        effects = pot.getEffects();
                    } catch(IllegalArgumentException ex) {
                        int EXTENDED_BIT = 0x40;
                        short damage = stack.getDurability();
                        if ((damage & EXTENDED_BIT) > 0) {
                            // Instant potions cannot be extended!
                            // So let's invert the extended bit and retry.
                            Integer tempint = (damage ^ EXTENDED_BIT);
                            stack.setDurability(tempint.shortValue());
                            try {
                                pot = Potion.fromItemStack(stack);
                                effects = pot.getEffects();
                            } catch (IllegalArgumentException ex2) {
                                // I give up
                            }
                            stack.setDurability(damage);
                        }
                        if(effects == null) {
                            pot = new Potion(PotionType.WATER);
                            effects = pot.getEffects();
                        }
                    }
                } else
                    effects = potionmeta.getCustomEffects();

                namebuilder.append(" (");

                for(PotionEffect effect : effects) {
                    if(first) first = false;
                    else namebuilder.append(", ");

                    namebuilder.append(signshopUtil.capFirstLetter(effect.getType().getName().toLowerCase()));
                    if(pot != null && pot.getLevel() > 0) {
                        namebuilder.append(" ");
                        namebuilder.append(itemUtil.binaryToRoman(pot.getLevel()));
                    } else {
                        namebuilder.append(" with");
                        namebuilder.append(" amplifier: ");
                        namebuilder.append(effect.getAmplifier());
                    }
                    if(effect.getDuration() > 1) {
                        namebuilder.append(" and duration: ");
                        namebuilder.append(effect.getDuration());
                    }
                }

                namebuilder.append(")");

                return namebuilder.toString();
            } else if(type == MetaType.Fireworks) {
                FireworkMeta fireworkmeta = (FireworkMeta) meta;

                StringBuilder namebuilder = new StringBuilder(256);
                namebuilder.append(getDisplayName(stack, ChatColor.DARK_PURPLE));

                if(fireworkmeta.hasEffects()) {
                    namebuilder.append(" (");
                    namebuilder.append("Duration : ");
                    namebuilder.append(fireworkmeta.getPower());
                    for(FireworkEffect effect : fireworkmeta.getEffects()) {
                        namebuilder.append(", ");

                        namebuilder.append(convertFireworkTypeToDisplay(effect.getType()));
                        namebuilder.append(" with");
                        namebuilder.append((effect.getColors().size() > 0 ? " colors: " : ""));
                        namebuilder.append(convertColorsToDisplay(effect.getColors()));
                        namebuilder.append((effect.getFadeColors().size() > 0 ? " and fadecolors: " : ""));
                        namebuilder.append(convertColorsToDisplay(effect.getFadeColors()));

                        namebuilder.append(effect.hasFlicker() ? " +twinkle" : "");
                        namebuilder.append(effect.hasTrail()? " +trail" : "");
                    }
                    namebuilder.append(")");
                }

                return namebuilder.toString();
            }
        }

        if(stack.getItemMeta().hasDisplayName())
            return getDisplayName(stack);
        return getDisplayName(stack);
    }

    public static void setMetaForID(ItemStack stack, Integer ID) {
        if(isLegacy())
            return;

        Map<String, String> metamap = new LinkedHashMap<String, String>();
        ItemMeta meta = stack.getItemMeta();
        SSDatabase db = new SSDatabase(filename);

        try {
            Map<Integer, Object> pars = new LinkedHashMap<Integer, Object>();
            pars.put(1, ID);

            ResultSet setprops = (ResultSet)db.runStatement("SELECT PropertyName, ProperyValue FROM MetaProperty WHERE ItemMetaID = ?;", pars, true);
            if(setprops == null)
                return;
            try {
                while(setprops.next())
                    metamap.put(setprops.getString("PropertyName"), setprops.getString("ProperyValue"));
            } catch(SQLException ex) {
                return;
            }

            if(metamap.isEmpty())
                return;
        } finally {
            db.close();
        }


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
                    if(!getPropValue("storedenchants", metamap).isEmpty()) {
                        for(Map.Entry<Enchantment, Integer> enchant : signshopUtil.convertStringToEnchantments(getPropValue("storedenchants", metamap)).entrySet()) {
                            enchantmentmeta.addStoredEnchant(enchant.getKey(), enchant.getValue(), true);
                        }
                    }
                }
                else if(type == MetaType.LeatherArmor) {
                    LeatherArmorMeta leathermeta = (LeatherArmorMeta) meta;
                    if(!getPropValue("color", metamap).isEmpty())
                        leathermeta.setColor(Color.fromRGB(Integer.parseInt(getPropValue("color", metamap))));
                }
                else if(type == MetaType.Map) {
                    // We could set scaling here but for some reason Spigot doesn't when stacks are built up
                    // Which results in items not matching anymore if we do, so we won't
                }
                else if(type == MetaType.Repairable) {
                    Repairable repairmeta = (Repairable) meta;
                    if(!getPropValue("repaircost", metamap).isEmpty())
                        repairmeta.setRepairCost(Integer.parseInt(getPropValue("repaircost", metamap)));
                }
                else if(type == MetaType.Skull) {
                    SkullMeta skullmeta = (SkullMeta) meta;
                    if(!getPropValue("owner", metamap).isEmpty())
                        skullmeta.setOwner(getPropValue("owner", metamap));
                } else if(type == MetaType.Potion) {
                    PotionMeta potionmeta = (PotionMeta) meta;
                    List<PotionEffect> effects = convertStringToPotionMeta(getPropValue("potioneffects", metamap));
                    for(PotionEffect effect : effects) {
                        potionmeta.addCustomEffect(effect, true);
                    }
                } else if(type == MetaType.Fireworks) {
                    FireworkMeta fireworkmeta = (FireworkMeta) meta;
                    fireworkmeta.addEffects(convertStringToFireworkEffects(getPropValue("fireworkeffects", metamap)));
                    fireworkmeta.setPower(Integer.parseInt(getPropValue("fireworkpower", metamap)));
                }
            }
        } catch(ClassCastException ex) {

        } catch(NumberFormatException ex) {

        }


        stack.setItemMeta(meta);
    }

    public static Integer storeMeta(ItemStack stack) {
        if(isLegacy() || !hasMeta(stack))
            return -1;

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

            if(itemmetaid == null || itemmetaid == -1)
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
        if(isLegacy() || !hasMeta(stack))
            return -1;

        return getMetaID(stack, null);
    }

    private static Integer getMetaID(ItemStack stack, Map<String, String> pMetamap) {
        Map<String, String> metamap = (pMetamap != null ? pMetamap : getMetaAsMap(stack.getItemMeta()));
        SSDatabase db = new SSDatabase(filename);
        try {
            Map<Integer, Object> pars = new LinkedHashMap<Integer, Object>();
            pars.put(1, metamap.hashCode());
            ResultSet set = (ResultSet)db.runStatement("SELECT ItemMetaID FROM ItemMeta WHERE ItemMetaHash = ?;", pars, true);
            if(set != null && set.next())
                return set.getInt("ItemMetaID");
        } catch (SQLException ex) {

        } finally {
            db.close();
        }

        return -1;
    }

    public static Map<String, String> getMetaAsMap(ItemMeta meta) {
        Map<String, String> metamap = new LinkedHashMap<String, String>();
        if(isLegacy() || meta == null)
            return metamap;
        List<MetaType> types = getTypesOfMeta(meta);

        if(meta.getDisplayName() != null)
            metamap.put("displayname", meta.getDisplayName());
        if(meta.getEnchants() != null && !meta.getEnchants().isEmpty())
            metamap.put("enchants", signshopUtil.convertEnchantmentsToString(meta.getEnchants()));
        if(meta.getLore() != null && !meta.getLore().isEmpty()) {
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
                if(meta instanceof Repairable) {
                    Repairable repairmeta = (Repairable) meta;
                    if(repairmeta.hasRepairCost()) {
                        metamap.put("repaircost", Integer.toString(repairmeta.getRepairCost()));
                    }
                }
            } else if(type == MetaType.Potion) {
                PotionMeta potionmeta = (PotionMeta) meta;
                if(potionmeta.hasCustomEffects()) {
                    metamap.put("potioneffects", convertPotionMetaToString(potionmeta));
                }
            } else if(type == MetaType.Fireworks) {
                FireworkMeta fireworkmeta = (FireworkMeta) meta;
                if(fireworkmeta.hasEffects()) {
                    metamap.put("fireworkeffects", convertFireworkMetaToString(fireworkmeta));
                    metamap.put("fireworkpower", Integer.toString(fireworkmeta.getPower()));
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
        if(meta instanceof org.bukkit.inventory.meta.PotionMeta)
            types.add(MetaType.Potion);
        if(meta instanceof org.bukkit.inventory.meta.FireworkMeta)
            types.add(MetaType.Fireworks);
        return types;
    }

    private static String getPropValue(String name, Map<String, String> metamap) {
        if(metamap.containsKey(name)) {
            return metamap.get(name);
        } else {
            return "";
        }
    }

    private static String convertPotionMetaToString(PotionMeta meta) {
        if(!meta.hasCustomEffects())
            return "";
        StringBuilder returnbuilder = new StringBuilder(meta.getCustomEffects().size() * 50);
        for(PotionEffect effect : meta.getCustomEffects()) {
            returnbuilder.append(effect.getType().getName());
            returnbuilder.append(valueSeperator);
            returnbuilder.append(Integer.toString(effect.getDuration()));
            returnbuilder.append(valueSeperator);
            returnbuilder.append(Integer.toString(effect.getAmplifier()));
            returnbuilder.append(valueSeperator);
            returnbuilder.append(Boolean.toString(effect.isAmbient()));
            returnbuilder.append(listSeperator);
        }
        return returnbuilder.toString();
    }

    @SuppressWarnings("deprecation") // Allowed for transition purposes
    private static List<PotionEffect> convertStringToPotionMeta(String meta) {
        List<PotionEffect> effects = new LinkedList<PotionEffect>();
        List<String> splitted = Arrays.asList(meta.split(listSeperator));
        if(splitted.isEmpty())
            return effects;
        for(String split : splitted) {
            String[] bits = split.split(valueSeperator);
            if(bits.length == 4) {
                try {
                    int dur = Integer.parseInt(bits[1]);
                    int amp = Integer.parseInt(bits[2]);
                    boolean amb = Boolean.parseBoolean(bits[3]);
                    PotionEffect effect = null;
                    try {
                        int id = Integer.parseInt(bits[0]);
                        effect = new PotionEffect(PotionEffectType.getById(id), dur, amp, amb);
                    } catch(NumberFormatException ex) {
                        PotionEffectType type = PotionEffectType.getByName(bits[0]);
                        if(type != null)
                            effect = new PotionEffect(PotionEffectType.getByName(bits[0]), dur, amp, amb);
                    }
                    if(effect != null)
                        effects.add(effect);
                } catch(NumberFormatException ex) {
                    continue;
                }


            }
        }

        return effects;
    }

    private static String getColorsAsAString(List<Color> colors) {
        List<String> temp = new LinkedList<String>();
        for(Color color : colors) {
            temp.add(Integer.toString(color.asRGB()));
        }
        String[] colorarr = new String[temp.size()];
        return signshopUtil.implode(temp.toArray(colorarr), innerListSeperator);
    }

    private static ImmutableList<Color> getColorsFromString(String colors) {
        List<Color> temp = new LinkedList<Color>();
        List<String> split = Arrays.asList(colors.split(innerListSeperator));
        for(String part : split) {
            try {
                temp.add(Color.fromRGB(Integer.parseInt(part)));
            } catch(NumberFormatException ex) {
                continue;
            }
        }

        return ImmutableList.copyOf(temp);
    }

    private static String convertFireworkMetaToString(FireworkMeta meta) {
        if(!meta.hasEffects())
            return "";
        StringBuilder returnbuilder = new StringBuilder(meta.getEffects().size() * 50);

        for(FireworkEffect effect : meta.getEffects()) {
            returnbuilder.append(effect.getType().toString());
            returnbuilder.append(valueSeperator);
            returnbuilder.append(getColorsAsAString(effect.getColors()));
            returnbuilder.append(valueSeperator);
            returnbuilder.append(getColorsAsAString(effect.getFadeColors()));
            returnbuilder.append(valueSeperator);
            returnbuilder.append(Boolean.toString(effect.hasFlicker()));
            returnbuilder.append(valueSeperator);
            returnbuilder.append(Boolean.toString(effect.hasTrail()));
            returnbuilder.append(listSeperator);
        }
        return returnbuilder.toString();
    }

    private static List<FireworkEffect> convertStringToFireworkEffects(String meta) {
        List<FireworkEffect> effects = new LinkedList<FireworkEffect>();
        List<String> splitted = Arrays.asList(meta.split(listSeperator));
        if(splitted.isEmpty())
            return effects;
        for(String split : splitted) {
            String[] bits = split.split(valueSeperator);
            if(bits.length == 5) {
                try {
                    Builder builder = FireworkEffect.builder().with(FireworkEffect.Type.valueOf(bits[0]));
                    ImmutableList<Color> colors = getColorsFromString(bits[1]);
                    if(colors != null)
                        builder = builder.withColor(colors);
                    ImmutableList<Color> fadecolors = getColorsFromString(bits[2]);
                    if(fadecolors != null)
                        builder = builder.withFade(fadecolors);
                    builder = (Boolean.parseBoolean(bits[3]) ? builder.withFlicker() : builder);
                    builder = (Boolean.parseBoolean(bits[4]) ? builder.withTrail() : builder);

                    effects.add(builder.build());
                } catch(NumberFormatException ex) {
                    continue;
                }
            }
        }
        return effects;
    }


    public static Boolean isLegacy() {
        return legacy;
    }

    private static enum MetaType {
        EnchantmentStorage,
        LeatherArmor,
        Map,
        Potion,
        Repairable,
        Fireworks,
        Skull,
        Stock,
    }
}
