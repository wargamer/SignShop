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
import java.util.Arrays;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.io.File;
import java.io.InputStream;
import java.io.Closeable;
import java.io.OutputStream;
import java.io.FileOutputStream;
import java.util.logging.Level;
import org.bukkit.Bukkit;
import org.wargamer2010.signshop.util.signshopUtil;
import org.wargamer2010.signshop.hooks.HookManager;
import org.wargamer2010.signshop.SignShop;
import org.wargamer2010.signshop.operations.SignShopOperation;
import org.wargamer2010.signshop.operations.runCommand;

public class SignShopConfig {
    private static Map<String,List<String>> Operations;
    private static Map<String,String> OperationAliases;                         // Alias <-> Original
    private static Map<String,Map<String,HashMap<String,String>>> Messages;
    private static Map<String,Map<String,String>> Errors;
    private static List<Integer> BlacklistedItems;
    public static Map<String,HashMap<String,Float>> PriceMultipliers;
    public static Map<String,List> Commands;
    public static Map<String,Integer> ShopLimits;
    public static Map<Material, String> LinkableMaterials;
    public static List<String> SpecialsOps = new LinkedList<String>();

    private SignShop instance = null;

    //Configurables
    private FileConfiguration config;
    private static int MaxSellDistance = 0;
    private static int MaxShopsPerPerson = 0;
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
    private static boolean EnableSignStacking = false;
    private static boolean fixIncompleteOperations = true;
    private static String Languages = "english";
    private static String baseLanguage = "english";
    private static String preferedLanguage = "";
    private static Material linkMaterial = Material.getMaterial("REDSTONE");
    private static Material updateMaterial = Material.getMaterial("INK_SACK");
    private static Material destroyMaterial = Material.getMaterial("IRON_HOE");


    public SignShopConfig() {
        instance = SignShop.getInstance();
        preferedLanguage = "";
        Languages = "english";
    }

    private List<String> getOrderedListFromArray(String[] array) {
        List<String> list = new LinkedList<String>();
        for(String item : array)
            if(!list.contains(item.toLowerCase().trim()))
                list.add(item.toLowerCase().trim());
        return list;
    }

    public void init() {
        initConfig();
        Languages = Languages.replace(baseLanguage, "config");
        List<String> aLanguages = getOrderedListFromArray(Languages.split(","));
        if(!aLanguages.contains("config"))
            aLanguages.add("config");
        Messages = new LinkedHashMap<String,Map<String,HashMap<String,String>>>();
        Errors = new LinkedHashMap<String,Map<String,String>>();
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
        PriceMultipliers = configUtil.fetchFloatHasmapInHashmap("pricemultipliers", config);
        Commands = configUtil.fetchListInHashmap("commands", config);
        ShopLimits = configUtil.fetchStringIntegerHashMap("limits", config);
        setupBlacklist();
        copyFileFromJar("SSQuickReference.pdf", true);
        setupOperations();
        fixIncompleOperations();
        setupHooks();
        setupSpecialsOps();
        setupLinkables();
    }

    private void setupHooks() {
        HookManager.addHook("LWC");
        HookManager.addHook("Lockette");
        HookManager.addHook("WorldGuard");
        HookManager.addHook("Deadbolt");
        HookManager.addHook("Residence");
        HookManager.addHook("GriefPrevention");
    }

    private void setupSpecialsOps() {
        SpecialsOps.add("convertChestshop");
        SpecialsOps.add("copySign");
        if(Bukkit.getServer().getPluginManager().getPlugin("ShowCaseStandalone") != null)
            SpecialsOps.add("linkShowcase");
        SpecialsOps.add("linkShareSign");
        SpecialsOps.add("linkRestrictedSign");
        SpecialsOps.add("changeOwner");
        SpecialsOps.add("linkAdditionalBlocks");
    }

    private void safeAddLinkeable(String sName, String sGroup) {
        if(Material.getMaterial(sName) != null)
            LinkableMaterials.put(Material.getMaterial(sName), sGroup);
    }

    private void setupLinkables() {
        LinkableMaterials = new HashMap<Material, String>();
        safeAddLinkeable("CHEST", "chest");
        safeAddLinkeable("DISPENSER", "dispenser");
        safeAddLinkeable("FURNACE", "furnace");
        safeAddLinkeable("BURNING_FURNACE", "furnace");
        safeAddLinkeable("BREWING_STAND", "brewingstand");
        safeAddLinkeable("ENCHANTMENT_TABLE", "enchantmenttable");
        safeAddLinkeable("LEVER", "lever");
        safeAddLinkeable("SIGN", "sign");
        safeAddLinkeable("SIGN_POST", "sign");
        safeAddLinkeable("WALL_SIGN", "sign");
        safeAddLinkeable("STEP", "slab");
        safeAddLinkeable("JUKEBOX", "jukebox");
        safeAddLinkeable("WOODEN_DOOR", "door");
    }

    private void initConfig() {
        String filename = "config.yml";
        FileConfiguration ymlThing = configUtil.loadYMLFromPluginFolder(filename);
        if(ymlThing == null)
            return;
        configUtil.loadYMLFromJar(ymlThing, filename);

        MaxSellDistance = ymlThing.getInt("MaxSellDistance", MaxSellDistance);
        TransactionLog = ymlThing.getBoolean("TransactionLog", TransactionLog);
        MaxShopsPerPerson = ymlThing.getInt("MaxShopsPerPerson", MaxShopsPerPerson);
        OPOverride = ymlThing.getBoolean("OPOverride", OPOverride);
        AllowVariableAmounts = ymlThing.getBoolean("AllowVariableAmounts", AllowVariableAmounts);
        AllowEnchantedRepair = ymlThing.getBoolean("AllowEnchantedRepair", AllowEnchantedRepair);
        DisableEssentialsSigns = ymlThing.getBoolean("DisableEssentialsSigns", DisableEssentialsSigns);
        AllowUnsafeEnchantments = ymlThing.getBoolean("AllowUnsafeEnchantments", AllowUnsafeEnchantments);
        AllowMultiWorldShops = ymlThing.getBoolean("AllowMultiWorldShops", AllowMultiWorldShops);
        EnablePermits = ymlThing.getBoolean("EnablePermits", EnablePermits);
        PreventVillagerTrade = ymlThing.getBoolean("PreventVillagerTrade", PreventVillagerTrade);
        ProtectShopsInCreative = ymlThing.getBoolean("ProtectShopsInCreative", ProtectShopsInCreative);
        EnableSignStacking = ymlThing.getBoolean("EnableSignStacking", EnableSignStacking);
        fixIncompleteOperations = ymlThing.getBoolean("fixIncompleteOperations", fixIncompleteOperations);
        Languages = ymlThing.getString("Languages", Languages);
        linkMaterial = getMaterial(ymlThing.getString("LinkMaterial", "REDSTONE"), Material.getMaterial("REDSTONE"));
        updateMaterial = getMaterial(ymlThing.getString("UpdateMaterial", "INK_SACK"), Material.getMaterial("INK_SACK"));
        destroyMaterial = getMaterial(ymlThing.getString("DestroyMaterial", "IRON_HOE"), Material.getMaterial("IRON_HOE"));

        this.config = ymlThing;
    }

    private Material getMaterial(String mat, Material defaultmat) {
        Material temp;
        try {
            Integer ID = Integer.parseInt(mat);
            temp = Material.getMaterial(ID);
        } catch(NumberFormatException ex) {
            String name = mat.toUpperCase();
            temp = Material.getMaterial(name);
        }
        if(temp == null) {
            SignShop.log("Material called: " + mat + " does not exist, please check your config.yml!", Level.WARNING);
            return defaultmat;
        }
        return temp;
    }

    private void setupOperations() {
        Operations = new HashMap<String,List<String>>();

        HashMap<String,String> tempSignOperations = configUtil.fetchStringStringHashMap("signs", config);

        List<String> tempSignOperationString;
        List<String> tempCheckedSignOperation = new LinkedList<String>();
        Boolean failedOp = false;

        for(String sKey : tempSignOperations.keySet()){
            tempSignOperationString = Arrays.asList(tempSignOperations.get(sKey).split("\\,"));
            if(tempSignOperationString.size() > 0) {
                for(int i = 0; i < tempSignOperationString.size(); i++) {
                    List<String> bits = signshopUtil.getParameters(tempSignOperationString.get(i).trim());
                    String op = bits.get(0);
                    try {
                        Class.forName("org.wargamer2010.signshop.operations."+(op.trim()));
                        tempCheckedSignOperation.add(tempSignOperationString.get(i).trim());
                    } catch(ClassNotFoundException notfound) {
                        failedOp = true;
                        break;
                    }
                }
                if(!failedOp && !tempCheckedSignOperation.isEmpty())
                    Operations.put(sKey.toLowerCase(), tempCheckedSignOperation);
                tempCheckedSignOperation = new LinkedList<String>();
                failedOp = false;
            }
        }

        List<String> aLanguages = getOrderedListFromArray(Languages.split(","));
        aLanguages.remove("config");
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

    private String opListToString(List<String> operations) {
        return signshopUtil.implode(operations.toArray(new String[operations.size()]), ",");
    }

    private void saveConfig() {
        File tempFile = new File(SignShop.getInstance().getDataFolder(), "config.yml");
        try {
            config.save(tempFile);
        } catch(IOException ex) {

        }
    }

    private String fetchCaseCorrectedKey(Map<String, String> map, String lowercased) {
        for(Map.Entry<String, String> entry : map.entrySet()) {
            if(entry.getKey().equalsIgnoreCase(lowercased))
                return entry.getKey();
        }
        return "";
    }

    private void fixIncompleOperations() {
        if(!fixIncompleteOperations)
            return;
        HashMap<String,String> tempSignOperations = configUtil.fetchStringStringHashMap("signs", config, true);
        Boolean changedSomething = false;
        for(Map.Entry<String, List<String>> entry :  Operations.entrySet()) {
            if(SignShopConfig.Commands.containsKey(entry.getKey().toLowerCase())) {
                Map<SignShopOperation, List<String>> tempMap = signshopUtil.getSignShopOps(entry.getValue());
                Boolean found = false;
                for(SignShopOperation tempOp : tempMap.keySet())
                    if(tempOp instanceof runCommand)
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
            saveConfig();
    }

    private void closeStream(Closeable in) {
        if(in == null)
            return;
        try {
            in.close();
        } catch(IOException ex) {

        }
    }


    private void copyFileFromJar(String filename, Boolean delete) {
        InputStream in = getClass().getResourceAsStream("/" + filename);
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

        String message = "";
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

    public static boolean registerOperation(String sName, List<String> blocks) {
        if(sName != null && blocks != null && !blocks.isEmpty()) {
            Operations.put(sName, blocks);
            return true;
        }
        return false;
    }

    public static String fillInBlanks(String pMessage, Map<String, String> messageParts) {
        String message = pMessage;
        if(messageParts == null)
            return message;
        for(Map.Entry<String, String> part : messageParts.entrySet()) {
            message = message.replace(part.getKey(), part.getValue());
        }
        message = message.replace("\\", "");
        return message;
    }

    private void setupBlacklist() {
        List<String> tempList = config.getStringList("Blacklisted_items");
        BlacklistedItems = new LinkedList<Integer>();
        for(String item : tempList) {
            try {
                Integer ID = Integer.parseInt(item);
                BlacklistedItems.add(ID);
            } catch(NumberFormatException ex) {
                Material mat = Material.getMaterial(item.toUpperCase());
                if(mat != null)
                    BlacklistedItems.add(mat.getId());
                else
                    SignShop.log("Material called: " + item + " could not be added to the blacklist as it does not exist, please check your config.yml!", Level.WARNING);
            }
        }
    }

    public static Boolean isItemOnBlacklist(int id) {
        return (SignShopConfig.BlacklistedItems.contains(id));
    }

    public static ItemStack isAnyItemOnBlacklist(ItemStack[] stacks) {
        if(stacks == null)
            return null;
        for(ItemStack single : stacks) {
            if(isItemOnBlacklist(single.getTypeId())) {
                return single;
            }
        }
        return null;
    }


    public static int getMaxSellDistance() {
        return MaxSellDistance;
    }

    public static int getMaxShopsPerPerson() {
        return MaxShopsPerPerson;
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

    public static Boolean getEnableSignStacking() {
        return EnableSignStacking;
    }

    public static Boolean getTransactionLog() {
        return TransactionLog;
    }

    public static Boolean getDisableEssentialsSigns() {
        return DisableEssentialsSigns;
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
}
