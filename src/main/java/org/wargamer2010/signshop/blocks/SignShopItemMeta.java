
package org.wargamer2010.signshop.blocks;

import com.google.common.collect.ImmutableList;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.FireworkEffect.Builder;
import org.bukkit.block.ShulkerBox;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.*;
import org.bukkit.potion.*;
import org.wargamer2010.signshop.configuration.ColorUtil;
import org.wargamer2010.signshop.configuration.SignShopConfig;
import org.wargamer2010.signshop.util.SSTimeUtil;
import org.wargamer2010.signshop.util.itemUtil;
import org.wargamer2010.signshop.util.signshopUtil;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;


public class SignShopItemMeta {
    private static final String listSeperator = "~";
    private static final String valueSeperator = "-";
    private static final String innerListSeperator = "^";
    private static final String filename = "books.db";

    private static  ChatColor txtColor = ChatColor.YELLOW;
    private static  ChatColor txtColorTwo = ChatColor.DARK_PURPLE;
    
    private SignShopItemMeta() {

    }

    public static void init() {
        SSDatabase db = new SSDatabase(filename);
        txtColor = SignShopConfig.getTextColor();
        txtColorTwo = SignShopConfig.getTextColorTwo();
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
        List<String> temp = new LinkedList<>();

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

    private static boolean hasNoMeta(ItemStack stack) {
        // This seems silly but some parts of the code below function when an item has no metadata but itemmeta is not null
        return (stack.getItemMeta() == null);
    }

    private static String getDisplayName(ItemStack stack) {
        return getDisplayName(stack, txtColor);
    }

    //May need to implement my own WhatIsIt friendly name yml
    private static String getDisplayName(ItemStack stack, ChatColor color) {
        String txtcolor = txtColor.toString();
        String customcolor = (stack.getEnchantments().isEmpty() ? color.toString() : txtColorTwo.toString());

        String normal = itemUtil.formatMaterialName(stack);
        String displayname = "";

        if(stack.getItemMeta() != null) {
            String custom = (stack.getItemMeta().hasDisplayName()
                    ? (txtcolor + "\"" + customcolor + stack.getItemMeta().getDisplayName() + txtcolor + "\"") : "");
            if(custom.length() > 0)
                displayname = (custom + " (" + normal + ")" + txtcolor);
        }

        if(displayname.isEmpty())
            displayname = (txtcolor + customcolor + normal + txtcolor);

        //noinspection deprecation TODO remove deprecation
        if(stack.getType().getMaxDurability() >= 30 && stack.getDurability() != 0)
            displayname = (" Damaged " + displayname);
        if(stack.getEnchantments().size() > 0)
            displayname += (txtcolor + " " + itemUtil.enchantmentsToMessageFormat(stack.getEnchantments()));

        return displayname;
    }

    public static String getName(ItemStack stack) {
        if (hasNoMeta(stack))
            return getDisplayName(stack);

        ItemMeta meta = stack.getItemMeta();

        List<MetaType> metatypes = getTypesOfMeta(meta);
        for(MetaType type : metatypes) {
            if(type == MetaType.EnchantmentStorage) {
                EnchantmentStorageMeta enchantmeta = (EnchantmentStorageMeta) meta;
                if(enchantmeta.hasStoredEnchants())
                    return (getDisplayName(stack, txtColorTwo) + " " + itemUtil.enchantmentsToMessageFormat(enchantmeta.getStoredEnchants()));
            } else if(type == MetaType.LeatherArmor) {
                LeatherArmorMeta leathermeta = (LeatherArmorMeta) meta;
                return (ColorUtil.getColorAsString(leathermeta.getColor()) + " Colored " + getDisplayName(stack));
            } else if(type == MetaType.Skull) {
                String postfix = "'s Head";
                SkullMeta skullmeta = (SkullMeta) meta;
                if(skullmeta.getOwningPlayer() !=null && skullmeta.getOwningPlayer().getName() != null) {
                    // Name coloring support had to be dropped since there is no more link between
                    // the skull owner and the actual player
                    return (skullmeta.getOwningPlayer().getName() + postfix);
                } else {
                    // We can no longer get a pretty name by ID (SKULL_ITEM isn't pretty, is it?)
                    // So we'll have to rely on the web lookup, if the server owner has it enabled
                    return getDisplayName(stack);
                }
            } else if(type == MetaType.Potion) {
                PotionMeta potionMeta = (PotionMeta) meta;
                StringBuilder nameBuilder = new StringBuilder();
                nameBuilder.append(txtColorTwo);
                nameBuilder.append(itemUtil.stripConstantCase(stack.getType().toString()));
                nameBuilder.append(txtColor);
                nameBuilder.append(" (");
                if (potionMeta.hasDisplayName()) {
                    nameBuilder.append("\"");
                    nameBuilder.append(potionMeta.getDisplayName());
                    nameBuilder.append(txtColor);
                    nameBuilder.append("\" ");
                }

                if (potionMeta.getBasePotionData().getType() == PotionType.UNCRAFTABLE || potionMeta.getBasePotionData().getType() == PotionType.WATER) {

                    boolean first = true;
                    for (PotionEffect potionEffect : potionMeta.getCustomEffects()) {
                        if (first) first = false;
                        else nameBuilder.append(", ");
                        StringBuilder effectString = new StringBuilder();
                        effectString.append(itemUtil.stripConstantCase(potionEffect.getType().getName()));
                        if (potionEffect.getAmplifier() > 0) {
                            effectString.append(" ");
                            effectString.append(potionEffect.getAmplifier());
                        }
                        effectString.append(" for ");
                        effectString.append(SSTimeUtil.parseTime(potionEffect.getDuration() / 20));
                        nameBuilder.append(effectString);

                    }
                }
                else {
                    String prefix = "";
                    if (potionMeta.getBasePotionData().isUpgraded()) prefix = "Strong ";
                    if (potionMeta.getBasePotionData().isExtended()) prefix = "Lasting ";
                    nameBuilder.append(prefix);
                    nameBuilder.append(itemUtil.stripConstantCase(potionMeta.getBasePotionData().getType().toString()));
                }

                nameBuilder.append(")");

                return nameBuilder.toString();
            } else if(type == MetaType.Fireworks) {
                FireworkMeta fireworkmeta = (FireworkMeta) meta;

                StringBuilder namebuilder = new StringBuilder(256);
                namebuilder.append(getDisplayName(stack, txtColorTwo));

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

            } else if(type == MetaType.BlockState){
                BlockStateMeta blockStateMeta = (BlockStateMeta) meta;
                if (blockStateMeta.getBlockState() instanceof ShulkerBox){
                    ShulkerBox shulker = (ShulkerBox) blockStateMeta.getBlockState();
                    StringBuilder nameBuilder = new StringBuilder();
                    nameBuilder.append(getDisplayName(stack, txtColorTwo));

                    nameBuilder.append(txtColor);
                    nameBuilder.append(" [");

                    boolean isEmpty = true;
                    ItemStack[] itemStacks = shulker.getInventory().getContents();
                    for (ItemStack item : itemStacks) {
                        if (item != null){
                            isEmpty = false;
                            break;
                        }
                    }
                    if (isEmpty){//TODO change to shulker.getInventory().isEmpty() when available
                        nameBuilder.append("Empty");
                    } else {

                        boolean first = true;
                        for (ItemStack item : itemStacks) {
                            if (item == null) continue;
                            if (first) first = false;
                            else nameBuilder.append(", ");
                            nameBuilder.append(item.getAmount()).append(" ");
                            nameBuilder.append(getName(item));
                        }
                    }
                    nameBuilder.append(txtColor);
                    nameBuilder.append("]");

                    return nameBuilder.toString();
                }
            }
        }

        stack.getItemMeta().hasDisplayName();
        return getDisplayName(stack);
    }
    //This method is only used for converting legacy data. Deprecation can be ignored until it is no longer valid.
    /** @noinspection deprecation*/
    public static void setMetaForID(ItemStack stack, Integer ID) {
        Map<String, String> metamap = new LinkedHashMap<>();
        ItemMeta meta = stack.getItemMeta();
        SSDatabase db = new SSDatabase(filename);

        try {
            Map<Integer, Object> pars = new LinkedHashMap<>();
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
                else //noinspection StatementWithEmptyBody
                    if (type == MetaType.Map) {
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
        } catch (ClassCastException | NumberFormatException ignored) {

        }


        stack.setItemMeta(meta);
    }

    public static void storeMeta(ItemStack stack) {
        if (hasNoMeta(stack))
            return;

        SSDatabase db = new SSDatabase(filename);
        Map<String, String> metamap = getMetaAsMap(stack.getItemMeta());

        try {
            Integer existingID = getMetaID(stack, metamap);
            if(existingID > -1)
                return;

            Integer itemmetaid;
            Map<Integer, Object> pars = new LinkedHashMap<>();
            pars.put(1, metamap.hashCode());

            itemmetaid = (Integer)db.runStatement("INSERT INTO ItemMeta(ItemMetaHash) VALUES (?);", pars, false);

            if(itemmetaid == null || itemmetaid == -1)
                return;

            for(Map.Entry<String, String> metaproperty : metamap.entrySet()) {
                pars.clear();
                pars.put(1, itemmetaid);
                pars.put(2, metaproperty.getKey());
                pars.put(3, metaproperty.getValue());
                db.runStatement("INSERT INTO MetaProperty(ItemMetaID, PropertyName, ProperyValue) VALUES (?, ?, ?);", pars, false);
            }

        } finally {
            db.close();
        }
    }

    public static Integer getMetaID(ItemStack stack) {
        if (hasNoMeta(stack))
            return -1;

        return getMetaID(stack, null);
    }

    private static Integer getMetaID(ItemStack stack, Map<String, String> pMetamap) {
        Map<String, String> metamap = (pMetamap != null ? pMetamap : getMetaAsMap(stack.getItemMeta()));
        SSDatabase db = new SSDatabase(filename);
        try {
            Map<Integer, Object> pars = new LinkedHashMap<>();
            pars.put(1, metamap.hashCode());
            ResultSet set = (ResultSet)db.runStatement("SELECT ItemMetaID FROM ItemMeta WHERE ItemMetaHash = ?;", pars, true);
            if(set != null && set.next())
                return set.getInt("ItemMetaID");
        } catch (SQLException ignored) {

        } finally {
            db.close();
        }

        return -1;
    }

    public static Map<String, String> getMetaAsMap(ItemMeta meta) {
        Map<String, String> metamap = new LinkedHashMap<>();
        if(meta == null)
            return metamap;
        List<MetaType> types = getTypesOfMeta(meta);

        if(meta.getDisplayName() != null)
            metamap.put("displayname", meta.getDisplayName());
        if(meta.getEnchants() != null && !meta.getEnchants().isEmpty())
            metamap.put("enchants", signshopUtil.convertEnchantmentsToString(meta.getEnchants()));
        if(meta.getLore() != null && !meta.getLore().isEmpty()) {
            String[] lorearr = new String[meta.getLore().size()];
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
                    metamap.put("owner", skullmeta.getOwningPlayer().getName());
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
        List<MetaType> types = new LinkedList<>();

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
        if (meta instanceof org.bukkit.inventory.meta.BlockStateMeta)
            types.add(MetaType.BlockState);
        return types;
    }

    private static String getPropValue(String name, Map<String, String> metamap) {
        return metamap.getOrDefault(name, "");
    }

    private static String convertPotionMetaToString(PotionMeta meta) {
        if(!meta.hasCustomEffects())
            return "";
        StringBuilder returnbuilder = new StringBuilder(meta.getCustomEffects().size() * 50);
        for(PotionEffect effect : meta.getCustomEffects()) {
            returnbuilder.append(effect.getType().getName());
            returnbuilder.append(valueSeperator);
            returnbuilder.append(effect.getDuration());
            returnbuilder.append(valueSeperator);
            returnbuilder.append(effect.getAmplifier());
            returnbuilder.append(valueSeperator);
            returnbuilder.append(effect.isAmbient());
            returnbuilder.append(listSeperator);
        }
        return returnbuilder.toString();
    }

    @SuppressWarnings("deprecation") // Allowed for transition purposes
    private static List<PotionEffect> convertStringToPotionMeta(String meta) {
        List<PotionEffect> effects = new LinkedList<>();
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
                } catch (NumberFormatException ignored) {
                }


            }
        }

        return effects;
    }

    private static String getColorsAsAString(List<Color> colors) {
        List<String> temp = new LinkedList<>();
        for(Color color : colors) {
            temp.add(Integer.toString(color.asRGB()));
        }
        String[] colorarr = new String[temp.size()];
        return signshopUtil.implode(temp.toArray(colorarr), innerListSeperator);
    }

    private static ImmutableList<Color> getColorsFromString(String colors) {
        List<Color> temp = new LinkedList<>();
        //noinspection SuspiciousRegexArgument
        String[] split = colors.split(innerListSeperator);
        for(String part : split) {
            try {
                temp.add(Color.fromRGB(Integer.parseInt(part)));
            } catch (NumberFormatException ignored) {
            }
        }

        return ImmutableList.copyOf(temp);
    }

    private static String convertFireworkMetaToString(FireworkMeta meta) {
        if(!meta.hasEffects())
            return "";
        StringBuilder returnbuilder = new StringBuilder(meta.getEffects().size() * 50);

        for(FireworkEffect effect : meta.getEffects()) {
            returnbuilder.append(effect.getType());
            returnbuilder.append(valueSeperator);
            returnbuilder.append(getColorsAsAString(effect.getColors()));
            returnbuilder.append(valueSeperator);
            returnbuilder.append(getColorsAsAString(effect.getFadeColors()));
            returnbuilder.append(valueSeperator);
            returnbuilder.append(effect.hasFlicker());
            returnbuilder.append(valueSeperator);
            returnbuilder.append(effect.hasTrail());
            returnbuilder.append(listSeperator);
        }
        return returnbuilder.toString();
    }

    private static List<FireworkEffect> convertStringToFireworkEffects(String meta) {
        List<FireworkEffect> effects = new LinkedList<>();
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
                } catch (NumberFormatException ignored) {
                }
            }
        }
        return effects;
    }

    private enum MetaType {
        EnchantmentStorage,
        LeatherArmor,
        Map,
        Potion,
        Repairable,
        Fireworks,
        Skull,
        Stock,
        Bundle,
        BlockState,
    }
}
