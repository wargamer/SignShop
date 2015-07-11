package org.wargamer2010.signshop.configuration;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.io.File;
import java.io.InputStream;
import java.io.Closeable;
import java.io.OutputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.TreeMap;
import java.util.logging.Level;
import org.bukkit.Bukkit;
import org.wargamer2010.signshop.util.signshopUtil;
import org.wargamer2010.signshop.hooks.HookManager;
import org.wargamer2010.signshop.SignShop;
import org.wargamer2010.signshop.operations.SignShopOperation;
import org.wargamer2010.signshop.operations.SignShopOperationListItem;
import org.wargamer2010.signshop.operations.runCommand;
import org.wargamer2010.signshop.specialops.*;

public class SignShopConfig {
    private static final String defaultOPPackage = "org.wargamer2010.signshop.operations";
    private static final String configFilename = "config.yml";
    private static final Map<String,SignShopOperation> OperationInstances = new LinkedHashMap<String, SignShopOperation>();
    private static Map<String,List<String>> Operations = new HashMap<String,List<String>>();
    private static Map<String,String> OperationAliases;                         // Alias <-> Original
    private static Map<String,Map<String,HashMap<String,String>>> Messages;
    private static Map<String,Map<String,String>> Errors;
    private static List<Material> BlacklistedItems;
    private static Map<String,HashMap<String,Double>> PriceMultipliers;
    private static Map<String,List<String>> Commands;
    private static Map<String,List<String>> DelayedCommands;
    private static Map<String,Integer> ShopLimits;
    private static List<LinkableMaterial> LinkableMaterials;
    private static final List<SignShopSpecialOp> SpecialsOps = new LinkedList<SignShopSpecialOp>();

    private static SignShop instance = null;

    //Configurables
    private static FileConfiguration config;
    private static int MaxSellDistance = 0;
    private static int MaxShopsPerPerson = 0;
    private static int ShopCooldown = 0;
    private static int MessageCooldown = 0;
    private static int ChunkLoadRadius = 2;
    private static int MaxChestsPerShop = 100;
    private static Boolean TransactionLog = false;
    private static boolean OPOverride = true;
    private static boolean AllowUnsafeEnchantments = false;
    private static boolean AllowVariableAmounts = false;
    private static boolean AllowEnchantedRepair = true;
    private static boolean DisableEssentialsSigns = true;
    private static boolean AllowMultiWorldShops = true;
    private static boolean EnablePermits = false;
    private static boolean PreventVillagerTrade = false;
    private static boolean ProtectShopsInCreative = true;
    private static boolean fixIncompleteOperations = true;
    private static boolean EnablePriceFromWorth = false;
    private static boolean EnableDynmapSupport = false;
    private static boolean EnableTutorialMessages = true;
    private static boolean EnableShopPlotSupport = true;
    private static boolean EnableShopOwnerProtection = true;
    private static boolean EnableNamesFromTheWeb = false;
    private static boolean EnableAutomaticLock = false;
    private static boolean UseBlacklistAsWhitelist = false;
    private static boolean EnableWrittenBookFix = true;
    private static String Languages = "english";
    private static final String baseLanguage = "english";
    private static String preferedLanguage = "";
    private static Material linkMaterial = Material.getMaterial("REDSTONE");
    private static Material updateMaterial = Material.getMaterial("INK_SACK");
    private static Material destroyMaterial = Material.getMaterial("IRON_HOE");

    private SignShopConfig() {

    }

    private static List<String> getOrderedListFromArray(String[] array) {
        List<String> list = new LinkedList<String>();
        for(String item : array)
            if(!list.contains(item.toLowerCase().trim()))
                list.add(item.toLowerCase().trim());
        return list;
    }

    public static void init() {
        instance = SignShop.getInstance();
        initConfig();
        String LanguagesAdjusted = Languages.replace(baseLanguage, "config");
        List<String> aLanguages = getOrderedListFromArray(LanguagesAdjusted.split(","));
        if(!aLanguages.contains("config"))
            aLanguages.add("config");
        Messages = new LinkedHashMap<String,Map<String,HashMap<String,String>>>();
        Errors = new LinkedHashMap<String,Map<String,String>>();
        preferedLanguage = "";
        for(String language : aLanguages) {
            String filename = (language + ".yml");
            String languageName = (language.equals("config") ? baseLanguage : language);
            copyFileFromJar(filename, false);
            File languageFile = new File(instance.getDataFolder(), filename);
            if(languageFile.exists()) {
                FileConfiguration ymlThing = configUtil.loadYMLFromPluginFolder(filename);
                if(ymlThing != null && !language.equals("config")) {
                    configUtil.loadYMLFromJar(ymlThing, filename);
                }

                Messages.put(languageName, configUtil.fetchHasmapInHashmap("messages", ymlThing));
                if(Messages.get(languageName) == null)
                    continue;
                Errors.put(languageName, configUtil.fetchStringStringHashMap("errors", ymlThing));
                if(Errors.get(languageName) == null)
                    continue;
                if(preferedLanguage.isEmpty())
                    preferedLanguage = languageName;
            } else {
                SignShop.log("The languagefile " + languageFile + " for language: " + languageName + " could not be found in the plugin directory!", Level.WARNING);
            }
        }
        if(preferedLanguage.isEmpty())
            preferedLanguage = baseLanguage;
        PriceMultipliers = configUtil.fetchDoubleHasmapInHashmap("pricemultipliers", config);
        Commands = configUtil.fetchListInHashmap("commands", config);
        DelayedCommands = configUtil.fetchListInHashmap("timedCommands", config);
        ShopLimits = configUtil.fetchStringIntegerHashMap("limits", config);
        setupBlacklist();
        copyFileFromJar("SSQuickReference.pdf", true);
        setupOperations();
        fixIncompleOperations();
        setupHooks();
        setupSpecialsOps();
        setupLinkables();
    }

    public static String getPreferredLanguage() {
        return preferedLanguage;
    }

    private static void setupHooks() {
        HookManager.addHook("LWC");
        HookManager.addHook("Lockette");
        HookManager.addHook("WorldGuard");
        HookManager.addHook("Deadbolt");
        HookManager.addHook("Residence");
        HookManager.addHook("GriefPrevention");
        HookManager.addHook("PreciousStones");
        HookManager.addHook("PlotMe");
        HookManager.addHook("Towny");
        HookManager.addHook("Lorelocks");
    }

    private static void setupSpecialsOps() {
        SpecialsOps.add(new ConvertChestshop());
        SpecialsOps.add(new CopySign());
        if(Bukkit.getServer().getPluginManager().getPlugin("ShowCaseStandalone") != null)
            SpecialsOps.add(new LinkShowcase());
        SpecialsOps.add(new LinkSpecialSign());
        SpecialsOps.add(new ChangeOwner());
        SpecialsOps.add(new LinkAdditionalBlocks());
        SpecialsOps.add(new ChangeShopItems());
    }

    private static void safeAddLinkeable(String sName, String sGroup, byte sData) {
        if(sName == null || sGroup == null || sName.isEmpty())
            return;
        LinkableMaterials.add(new LinkableMaterial(sName, sGroup, sData));
    }

    private static byte getDataFromString(String dur) {
        try {
            return Byte.parseByte(dur);
        } catch(NumberFormatException ex) {
            return -1;
        }
    }

    private static void setupLinkables() {
        LinkableMaterials = new ArrayList<LinkableMaterial>();
        for(Map.Entry<String, String> entry : configUtil.fetchStringStringHashMap("linkableMaterials", config, false).entrySet()) {
            byte data = -1;
            String material;
            if(entry.getKey().contains("~")) {
                String[] bits = entry.getKey().split("~");
                material = bits[0];
                data = getDataFromString(bits[1]);
            } else {
                material = entry.getKey();
            }
            safeAddLinkeable(material, entry.getValue(), data);
        }
    }

    /**
     * Register a material so that it can be linked and used for a shop
     * The Alias is used when checking for DenyLink permission nodes (i.e. DenyLink.door)
     *
     * @param material Material to register
     * @param alias Alias to use for the given material
     */
    public static void addLinkable(String material, String alias) {
        if(material == null || material.isEmpty())
            return;
        safeAddLinkeable(material.toUpperCase(), alias.toLowerCase(), (byte)-1);
    }

    /**
     * Register a material so that it can be linked and used for a shop
     * The Alias is used when checking for DenyLink permission nodes (i.e. DenyLink.door)
     * Passing a value for data other than -1 will only allow the given material with that specific
     * data value to be linked. It is allowed to register the same material with multiple data values.
     *
     * @param material Material to register
     * @param alias Alias to use for the given material
     * @param data Durability value to compare
     */
    public static void addLinkable(String material, String alias, byte data) {
        if(material == null || material.isEmpty())
            return;
        safeAddLinkeable(material.toUpperCase(), alias.toLowerCase(), data);
    }

    private static void initConfig() {
        FileConfiguration ymlThing = configUtil.loadYMLFromPluginFolder(configFilename);
        if(ymlThing == null)
            return;
        configUtil.loadYMLFromJar(ymlThing, configFilename);

        MaxSellDistance = ymlThing.getInt("MaxSellDistance", MaxSellDistance);
        TransactionLog = ymlThing.getBoolean("TransactionLog", TransactionLog);
        MaxShopsPerPerson = ymlThing.getInt("MaxShopsPerPerson", MaxShopsPerPerson);
        ChunkLoadRadius = ymlThing.getInt("ChunkLoadRadius", ChunkLoadRadius);
        ShopCooldown = ymlThing.getInt("ShopCooldownMilliseconds", ShopCooldown);
        MessageCooldown = ymlThing.getInt("MessageCooldownSeconds", MessageCooldown);
        MaxChestsPerShop = ymlThing.getInt("MaxChestsPerShop", MaxChestsPerShop);
        OPOverride = ymlThing.getBoolean("OPOverride", OPOverride);
        AllowVariableAmounts = ymlThing.getBoolean("AllowVariableAmounts", AllowVariableAmounts);
        AllowEnchantedRepair = ymlThing.getBoolean("AllowEnchantedRepair", AllowEnchantedRepair);
        DisableEssentialsSigns = ymlThing.getBoolean("DisableEssentialsSigns", DisableEssentialsSigns);
        AllowUnsafeEnchantments = ymlThing.getBoolean("AllowUnsafeEnchantments", AllowUnsafeEnchantments);
        AllowMultiWorldShops = ymlThing.getBoolean("AllowMultiWorldShops", AllowMultiWorldShops);
        EnablePermits = ymlThing.getBoolean("EnablePermits", EnablePermits);
        PreventVillagerTrade = ymlThing.getBoolean("PreventVillagerTrade", PreventVillagerTrade);
        ProtectShopsInCreative = ymlThing.getBoolean("ProtectShopsInCreative", ProtectShopsInCreative);
        fixIncompleteOperations = ymlThing.getBoolean("fixIncompleteOperations", fixIncompleteOperations);
        EnablePriceFromWorth = ymlThing.getBoolean("EnablePriceFromWorth", EnablePriceFromWorth);
        EnableDynmapSupport = ymlThing.getBoolean("EnableDynmapSupport", EnableDynmapSupport);
        EnableTutorialMessages = ymlThing.getBoolean("EnableTutorialMessages", EnableTutorialMessages);
        EnableShopPlotSupport = ymlThing.getBoolean("EnableShopPlotSupport", EnableShopPlotSupport);
        EnableShopOwnerProtection = ymlThing.getBoolean("EnableShopOwnerProtection", EnableShopOwnerProtection);
        EnableNamesFromTheWeb = ymlThing.getBoolean("EnableNamesFromTheWeb", EnableNamesFromTheWeb);
        EnableAutomaticLock = ymlThing.getBoolean("EnableAutomaticLock", EnableAutomaticLock);
        UseBlacklistAsWhitelist = ymlThing.getBoolean("UseBlacklistAsWhitelist", UseBlacklistAsWhitelist);
        EnableWrittenBookFix = ymlThing.getBoolean("EnableWrittenBookFix", EnableWrittenBookFix);
        Languages = ymlThing.getString("Languages", Languages);
        linkMaterial = getMaterial(ymlThing.getString("LinkMaterial", "REDSTONE"), Material.getMaterial("REDSTONE"));
        updateMaterial = getMaterial(ymlThing.getString("UpdateMaterial", "INK_SACK"), Material.getMaterial("INK_SACK"));
        destroyMaterial = getMaterial(ymlThing.getString("DestroyMaterial", "GOLD_AXE"), Material.getMaterial("GOLD_AXE"));

        // Sanity check
        if(ChunkLoadRadius > 50 || ChunkLoadRadius < 0)
            ChunkLoadRadius = 3;

        config = ymlThing;
    }

    private static Material getMaterial(String mat, Material defaultmat) {
        String name = mat.toUpperCase();
        Material temp = Material.getMaterial(name);
        if(temp == null) {
            SignShop.log("Material called: " + mat + " does not exist, please check your config.yml!", Level.WARNING);
            return defaultmat;
        }
        return temp;
    }

    private static void setupOperations() {
        setupOperations(configUtil.fetchStringStringHashMap("signs", config));
    }

    public static void setupOperations(Map<String, String> allSignOperations) {
        setupOperations(allSignOperations, defaultOPPackage);
    }

    private static Object getInstance(String fullClassPath) {
        try {
            Class<?> aclass = Class.forName(fullClassPath);
            return aclass.newInstance();
        } catch(ClassNotFoundException notfound) {
            return null;
        } catch (InstantiationException ex) {
            return null;
        } catch (IllegalAccessException ex) {
            return null;
        }
    }

    public static void setupOperations(Map<String, String> allSignOperations, String packageName) {
        if(Operations == null)
            Operations = new HashMap<String,List<String>>();

        for(String sKey : allSignOperations.keySet()){
            boolean failedOp = false;
            List<String> tempCheckedSignOperation = new LinkedList<String>();

            for(String tempOperationString : allSignOperations.get(sKey).split("\\,")) {
                List<String> bits = signshopUtil.getParameters(tempOperationString.trim());
                String op = bits.get(0);
                Object opinstance = getInstance(packageName + "." + op.trim());
                if(opinstance == null) // Retry with default package
                    opinstance = getInstance(defaultOPPackage + "." + op.trim());
                if(opinstance == null) {
                    failedOp = true;
                    break;
                }

                if(opinstance instanceof SignShopOperation) {
                    SignShopConfig.OperationInstances.put(op.trim(), (SignShopOperation)opinstance);
                    tempCheckedSignOperation.add(tempOperationString.trim());
                } else {
                    failedOp = true;
                }
            }
            if(!failedOp && !tempCheckedSignOperation.isEmpty())
                Operations.put(sKey.toLowerCase(), tempCheckedSignOperation);
        }

        List<String> aLanguages = getOrderedListFromArray(Languages.split(","));
        aLanguages.remove("english");

        SignShopConfig.OperationAliases = new HashMap<String,String>();

        for(String language : aLanguages) {
            String filename = (language + ".yml");
            File languageFile = new File(instance.getDataFolder(), filename);
            if(languageFile.exists()) {
                FileConfiguration ymlThing = new YamlConfiguration();
                try {
                    ymlThing.load(languageFile);
                } catch(FileNotFoundException ex) {
                    continue;
                } catch(IOException ex) {
                    continue;
                } catch(InvalidConfigurationException ex) {
                    continue;
                }
                HashMap<String,String> tempSignAliases = configUtil.fetchStringStringHashMap("signs", ymlThing);
                for(Map.Entry<String, String> alias : tempSignAliases.entrySet()) {
                    if(Operations.containsKey(alias.getValue().toLowerCase())) {
                        SignShopConfig.OperationAliases.put(alias.getKey().toLowerCase(), alias.getValue().toLowerCase());
                    } else {
                        SignShop.log("Could not find " + alias.getValue() + " to alias as " + alias.getKey(), Level.WARNING);
                    }

                }
            }
        }
    }

    private static String opListToString(List<String> operations) {
        return signshopUtil.implode(operations.toArray(new String[operations.size()]), ",");
    }

    private static String fetchCaseCorrectedKey(Map<String, String> map, String lowercased) {
        for(Map.Entry<String, String> entry : map.entrySet()) {
            if(entry.getKey().equalsIgnoreCase(lowercased))
                return entry.getKey();
        }
        return "";
    }

    private static void fixIncompleOperations() {
        if(!fixIncompleteOperations)
            return;
        HashMap<String,String> tempSignOperations = configUtil.fetchStringStringHashMap("signs", config, true);
        Boolean changedSomething = false;
        for(Map.Entry<String, List<String>> entry :  Operations.entrySet()) {
            if(SignShopConfig.Commands.containsKey(entry.getKey().toLowerCase())) {
                List<SignShopOperationListItem> tempList = signshopUtil.getSignShopOps(entry.getValue());
                Boolean found = false;
                for(SignShopOperationListItem tempOp : tempList)
                    if(tempOp.getOperation() instanceof runCommand || tempOp.getParameters().contains("runCommand"))
                        found = true;
                if(found)
                    continue;
                entry.getValue().add("runCommand");
                String opName = fetchCaseCorrectedKey(tempSignOperations, entry.getKey());
                if(opName.isEmpty())
                    opName = entry.getKey();
                config.set(("signs." + opName), opListToString(entry.getValue()));
                if(!changedSomething)
                    changedSomething = true;
                SignShop.log("Added runCommand block to " + opName + " because it has a corresponding entry in the Commands section. Set fixIncompleteOperations to false to disable this function.", Level.INFO);
            }
        }
        if(changedSomething)
            configUtil.loadYMLFromJar(config, configFilename);
    }

    private static void closeStream(Closeable in) {
        if(in == null)
            return;
        try {
            in.close();
        } catch(IOException ex) {

        }
    }


    private static void copyFileFromJar(String filename, Boolean delete) {
        InputStream in = SignShopConfig.class.getResourceAsStream("/" + filename);
        File file = new File(instance.getDataFolder(), filename);
        OutputStream os = null;
        if(file.exists() && delete) {
            if(!file.delete()) {
                closeStream(in);
                return;
            }
        } else if(file.exists() && !delete) {
            return;
        }
        try {
            if(in.available() == 0)
                return;
            file.createNewFile();
            os = new FileOutputStream(file.getPath());
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = in.read(buffer)) != -1) {
              os.write(buffer, 0, bytesRead);
            }
        } catch(java.io.FileNotFoundException notfoundex) {

        } catch(java.io.IOException ioex) {

        } catch(NullPointerException nullex) {

        }

        closeStream(in);
        closeStream(os);
    }

    public static String getError(String sType, Map<String, String> messageParts) {
        Map<String,String> localisedError = Errors.get(SignShopConfig.preferedLanguage);
        Map<String,String> defaultError = Errors.get(SignShopConfig.baseLanguage);

        String error;
        if(!localisedError.containsKey(sType) || localisedError.get(sType) == null) {
            if(!defaultError.containsKey(sType) || defaultError.get(sType) == null)
                return "";
            else
                error = defaultError.get(sType);
        } else
            error = localisedError.get(sType);
        return fillInBlanks(error, messageParts);
    }

    public static String getMessage(String sType, String pOperation, Map<String, String> messageParts) {
        Map<String,HashMap<String,String>> localisedMessage = Messages.get(SignShopConfig.preferedLanguage);
        Map<String,HashMap<String,String>> defaultMessage = Messages.get(SignShopConfig.baseLanguage);

        String sOperation = pOperation;
        if(OperationAliases.containsKey(sOperation))
            sOperation = OperationAliases.get(sOperation);

        String message;
        if(!localisedMessage.containsKey(sType) || !localisedMessage.get(sType).containsKey(sOperation) || localisedMessage.get(sType).get(sOperation) == null) {
            if(!defaultMessage.containsKey(sType) || !defaultMessage.get(sType).containsKey(sOperation) || defaultMessage.get(sType).get(sOperation) == null) {
                return "";
            } else
                message = defaultMessage.get(sType).get(sOperation);
        } else
            message = localisedMessage.get(sType).get(sOperation);

        return fillInBlanks(message, messageParts);
    }

    public static List<String> getBlocks(String pOp) {
        String op = pOp;
        if(OperationAliases.containsKey(op))
            op = OperationAliases.get(op);

        if(Operations.containsKey(op))
            return Operations.get(op);
        else
            return new LinkedList<String>();
    }

    public static Collection<String> getOperations() {
        return Collections.unmodifiableCollection(Operations.keySet());
    }

    public static Collection<String> getAliases(String op) {
        Collection<String> aliases = new LinkedList<String>();
        if(Languages.contains(baseLanguage))
            aliases.add(op); // If the baseLanguage is explicitly requested, we'll add the english OP as an alias
        for(Map.Entry<String, String> entry : OperationAliases.entrySet()) {
            if(entry.getValue().equals(op))
                aliases.add(entry.getKey());
        }
        return aliases;
    }

    public static boolean registerOperation(String sName, List<String> blocks) {
        if(sName != null && blocks != null && !blocks.isEmpty()) {
            Operations.put(sName, blocks);
            return true;
        }
        return false;
    }

    public static boolean registerMessage(String type, String shop, String message) {
        return registerMessage(SignShopConfig.baseLanguage, type, shop, message);
    }

    public static boolean registerMessage(String language, String type, String shop, String message) {
        if(Messages.containsKey(language) && Messages.get(language).containsKey(type)) {
            HashMap<String, String> temp = Messages.get(language).get(type);
            temp.put(shop, message);
            return true;
        } else {
            return false;
        }
    }

    public static boolean registerMessages(String type, Map<String, String> messagesByShop) {
        return registerMessages(SignShopConfig.baseLanguage, type, messagesByShop);
    }

    public static boolean registerMessages(String language, String type, Map<String, String> messagesByShop) {
        if(Messages.containsKey(language) && Messages.get(language).containsKey(type)) {
            HashMap<String, String> temp = Messages.get(language).get(type);
            temp.putAll(messagesByShop);
            return true;
        } else {
            return false;
        }
    }

    public static boolean registerErrorMessage(String type, String message) {
        return registerErrorMessage(SignShopConfig.baseLanguage, type, message);
    }

    public static boolean registerErrorMessage(String language, String type, String message) {
        if(Messages.containsKey(language)) {
            Map<String, String> temp = Errors.get(language);
            temp.put(type, message);
            return true;
        } else {
            return false;
        }
    }

    public static boolean registerErrorMessages(Map<String, String> messagesByType) {
        return registerErrorMessages(SignShopConfig.baseLanguage, messagesByType);
    }

    public static boolean registerErrorMessages(String language, Map<String, String> messagesByType) {
        if(Messages.containsKey(language)) {
            Errors.get(language).putAll(messagesByType);
            return true;
        } else {
            return false;
        }
    }

    public static String fillInBlanks(String pMessage, Map<String, String> messageParts) {
        String message = pMessage;

        if(messageParts == null)
            return message;

        TreeMap<String, String> temp = new TreeMap<String, String>(new StringLengthComparator());
        temp.putAll(messageParts);

        for(Map.Entry<String, String> part : temp.entrySet()) {
            if(part != null && part.getKey() != null && part.getValue() != null)
                message = message.replace(part.getKey(), part.getValue());
        }
        message = message.replace("\\", "");
        return message;
    }

    private static void setupBlacklist() {
        List<String> tempList = config.getStringList("Blacklisted_items");
        BlacklistedItems = new LinkedList<Material>();
        for(String item : tempList) {
            Material mat = Material.getMaterial(item.toUpperCase());
            if(mat != null)
                BlacklistedItems.add(mat);
            else
                SignShop.log("Material called: " + item + " could not be added to the blacklist as it does not exist, please check your config.yml!", Level.WARNING);
        }
    }

    public static Boolean isItemOnBlacklist(Material mat) {
        return (
                (SignShopConfig.BlacklistedItems.contains(mat) && !SignShopConfig.getUseBlacklistAsWhitelist())
                    ||
                (!SignShopConfig.BlacklistedItems.contains(mat) && SignShopConfig.getUseBlacklistAsWhitelist())
        );
    }

    public static ItemStack isAnyItemOnBlacklist(ItemStack[] stacks) {
        if(stacks == null)
            return null;
        for(ItemStack single : stacks) {
            if(single == null)
                continue;
            if(isItemOnBlacklist(single.getType())) {
                return single;
            }
        }
        return null;
    }

    public static int getMaxChestsPerShop() {
        return MaxChestsPerShop;
    }

    public static Boolean ExceedsMaxChestsPerShop(int currentAmountOfChests) {
        return MaxChestsPerShop != 0 && currentAmountOfChests > MaxChestsPerShop;
    }

    public static Map<String, SignShopOperation> getOperationInstances() {
        return Collections.unmodifiableMap(OperationInstances);
    }

    public static Map<String, HashMap<String, Double>> getPriceMultipliers() {
        return Collections.unmodifiableMap(PriceMultipliers);
    }

    public static Map<String, List<String>> getCommands() {
        return Collections.unmodifiableMap(Commands);
    }

    public static Map<String, List<String>> getDelayedCommands() {
        return Collections.unmodifiableMap(DelayedCommands);
    }

    public static Map<String, Integer> getShopLimits() {
        return Collections.unmodifiableMap(ShopLimits);
    }

    public static List<LinkableMaterial> getLinkableMaterials() {
        return Collections.unmodifiableList(LinkableMaterials);
    }

    public static List<SignShopSpecialOp> getSpecialOps() {
        return Collections.unmodifiableList(SpecialsOps);
    }

    public static int getMaxSellDistance() {
        return MaxSellDistance;
    }

    public static int getMaxShopsPerPerson() {
        return MaxShopsPerPerson;
    }

    public static int getShopCooldown() {
        return ShopCooldown;
    }

    public static int getChunkLoadRadius() {
        return ChunkLoadRadius;
    }

    public static int getMessageCooldown() {
        return MessageCooldown;
    }

    public static Boolean getOPOverride() {
        return OPOverride;
    }

    public static Boolean getAllowVariableAmounts() {
        return AllowVariableAmounts;
    }

    public static Boolean getAllowEnchantedRepair() {
        return AllowEnchantedRepair;
    }

    public static Boolean getAllowUnsafeEnchantments() {
        return AllowUnsafeEnchantments;
    }

    public static Boolean getAllowMultiWorldShops() {
        return AllowMultiWorldShops;
    }

    public static Boolean getEnablePermits() {
        return EnablePermits;
    }

    public static Boolean getPreventVillagerTrade() {
        return PreventVillagerTrade;
    }

    public static Boolean getProtectShopsInCreative() {
        return ProtectShopsInCreative;
    }

    public static Boolean getTransactionLog() {
        return TransactionLog;
    }

    public static Boolean getDisableEssentialsSigns() {
        return DisableEssentialsSigns;
    }

    public static Boolean getEnablePriceFromWorth() {
        return EnablePriceFromWorth;
    }

    public static Boolean getEnableDynmapSupport() {
        return EnableDynmapSupport;
    }

    public static Boolean getEnableTutorialMessages() {
        return EnableTutorialMessages;
    }

    public static Boolean getEnableShopPlotSupport() {
        return EnableShopPlotSupport;
    }

    public static Boolean getEnableShopOwnerProtection() {
        return EnableShopOwnerProtection;
    }

    public static boolean getEnableNamesFromTheWeb() {
        return EnableNamesFromTheWeb;
    }

    public static boolean getEnableAutomaticLock() {
        return EnableAutomaticLock;
    }

    public static boolean getUseBlacklistAsWhitelist() {
        return UseBlacklistAsWhitelist;
    }

    public static boolean getEnableWrittenBookFix() {
        return EnableWrittenBookFix;
    }

    public static Material getLinkMaterial() {
        return linkMaterial;
    }

    public static Material getUpdateMaterial() {
        return updateMaterial;
    }

    public static Material getDestroyMaterial() {
        return destroyMaterial;
    }

    public static boolean isOPMaterial(Material check) {
        return (check == updateMaterial || check == linkMaterial);
    }

    /**
     * Orders strings by their length from long to short
     */
    private static class StringLengthComparator implements Comparator<String> {
        @Override
        public int compare(String s1, String s2) {
            int s2Length = s2.length();
            int s1Length = s1.length();
            if(s2Length == s1Length)
                return -1;
            return s2Length - s1Length;
        }
    }
}
