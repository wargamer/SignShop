package org.wargamer2010.signshop.configuration;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.Material;
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
import java.io.OutputStream;
import java.io.FileOutputStream;
import org.bukkit.Bukkit;
import org.wargamer2010.signshop.util.signshopUtil;
import org.wargamer2010.signshop.hooks.HookManager;
import org.wargamer2010.signshop.SignShop;

public class SignShopConfig {
    public static Map<String,List<String>> Operations;
    public static Map<String,Map<String,HashMap<String,String>>> Messages;
    public static Map<String,Map<String,String>> Errors;
    public static Map<String,HashMap<String,Float>> PriceMultipliers;
    public static Map<String,List> Commands;
    public static Map<String,Integer> ShopLimits;
    public static Map<Material, String> LinkableMaterials;
    public static List<String> SpecialsOps = new LinkedList();    
    
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
    private static String Languages = "english";
    private static String baseLanguage = "english";
    private static String preferedLanguage = "";
    
    public SignShopConfig() {
        instance = SignShop.getInstance();
        config = instance.getConfig();
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
        System.out.println("[*] Languages setting is at: " + Languages);
        Messages = new LinkedHashMap<String,Map<String,HashMap<String,String>>>();
        Errors = new LinkedHashMap<String,Map<String,String>>();
        for(String language : aLanguages) {
            String filename = (language + ".yml");
            String languageName = (language.equals("config") ? baseLanguage : language);
            copyFileFromJar(filename, false);
            File languageFile = new File(instance.getDataFolder(), filename);
            if(languageFile.exists()) {
                System.out.println("[*] Found language called: " + languageName + " with file: " + languageFile + " and original list key: " + language);
                FileConfiguration ymlThing = new YamlConfiguration();                
                FileConfiguration thingInJar = new YamlConfiguration();                
                try {
                    ymlThing.load(languageFile);                    
                } catch(FileNotFoundException ex) {
                    continue;
                } catch(IOException ex) {
                    continue;
                } catch(InvalidConfigurationException ex) {
                    continue;
                }                      
                try {
                    InputStream in = getClass().getResourceAsStream("/" + filename);
                    System.out.println("Loading: " + ("/" + filename));
                    if(in != null) {
                        thingInJar.load(in);                        
                        ymlThing.setDefaults(thingInJar);  
                        ymlThing.options().copyDefaults(true);
                        ymlThing.options().copyHeader(true);                        
                        ymlThing.save(languageFile);
                    } else {
                        System.out.println("[!] Input == null!");
                    }
                } catch(FileNotFoundException ex) { }
                catch(IOException ex) { }
                catch(InvalidConfigurationException ex) { }
                    
                Messages.put(languageName, configUtil.fetchHasmapInHashmap("messages", ymlThing));
                if(Messages.get(languageName) == null)
                    continue;
                Errors.put(languageName, configUtil.fetchStringStringHashMap("errors", ymlThing));
                if(Errors.get(languageName) == null)
                    continue;
                if(preferedLanguage.equals(""))
                    preferedLanguage = languageName;
                System.out.println("[*] Loaded language called: " + languageName + " with file: " + languageFile + " and original list key: " + language);
            }
        }
        if(preferedLanguage.equals(""))
            preferedLanguage = baseLanguage;
        PriceMultipliers = configUtil.fetchFloatHasmapInHashmap("pricemultipliers", config);
        Commands = configUtil.fetchListInHashmap("commands", config);
        ShopLimits = configUtil.fetchStringIntegerHashMap("limits", config);
        copyFileFromJar("SSQuickReference.pdf", true);
        setupOperations();
        setupHooks();
        setupSpecialsOps();
        setupLinkables();
        System.out.println("[*] init routine finished, left with preferedLanguage of: " + preferedLanguage + " and " + Messages.get(preferedLanguage).size() + " messages and " + Errors.get(preferedLanguage).size() + " errors for that language");
    }
    
    private void setupHooks() {
        HookManager.addHook("LWC");
        HookManager.addHook("Lockette");
        HookManager.addHook("WorldGuard");
        HookManager.addHook("Deadbolt");
        HookManager.addHook("Residence");
    }
    
    private void setupSpecialsOps() {
        SpecialsOps.add("convertChestshop");
        SpecialsOps.add("copySign");
        if(Bukkit.getServer().getPluginManager().getPlugin("ShowCaseStandalone") != null)
            SpecialsOps.add("linkShowcase");
        SpecialsOps.add("linkShareSign");
        SpecialsOps.add("linkRestrictedSign");
        SpecialsOps.add("changeOwner");
    }
    
    private void setupLinkables() {
        LinkableMaterials = new HashMap<Material, String>();
        LinkableMaterials.put(Material.CHEST, "chest");
        LinkableMaterials.put(Material.DISPENSER, "dispenser");        
        LinkableMaterials.put(Material.FURNACE, "furnace");
        LinkableMaterials.put(Material.BURNING_FURNACE, "furnace");
        LinkableMaterials.put(Material.BREWING_STAND, "brewingstand");
        LinkableMaterials.put(Material.ENCHANTMENT_TABLE, "enchantmenttable");
        LinkableMaterials.put(Material.LEVER, "lever");
        LinkableMaterials.put(Material.SIGN, "sign");
        LinkableMaterials.put(Material.SIGN_POST, "sign");
        LinkableMaterials.put(Material.WALL_SIGN, "sign");        
        LinkableMaterials.put(Material.STEP, "slab");           
    }
    
    private void initConfig() {        
        instance.reloadConfig();
        File configFile = new File("plugins/SignShop", "config.yml");
        if(!configFile.exists()) {
            instance.saveDefaultConfig();
            instance.saveConfig();
        }        
        config.options().copyDefaults(true);
        instance.saveConfig();
        instance.reloadConfig();
        MaxSellDistance = config.getInt("MaxSellDistance", MaxSellDistance);
        TransactionLog = config.getBoolean("TransactionLog", TransactionLog);
        MaxShopsPerPerson = config.getInt("MaxShopsPerPerson", MaxShopsPerPerson);
        OPOverride = config.getBoolean("OPOverride", OPOverride);
        AllowVariableAmounts = config.getBoolean("AllowVariableAmounts", AllowVariableAmounts);        
        AllowEnchantedRepair = config.getBoolean("AllowEnchantedRepair", AllowEnchantedRepair);
        DisableEssentialsSigns = config.getBoolean("DisableEssentialsSigns", DisableEssentialsSigns);
        AllowUnsafeEnchantments = config.getBoolean("AllowUnsafeEnchantments", AllowUnsafeEnchantments);
        AllowMultiWorldShops = config.getBoolean("AllowMultiWorldShops", AllowMultiWorldShops);
        EnablePermits = config.getBoolean("EnablePermits", EnablePermits);
        PreventVillagerTrade = config.getBoolean("PreventVillagerTrade", PreventVillagerTrade);
        ProtectShopsInCreative = config.getBoolean("ProtectShopsInCreative", ProtectShopsInCreative);
        EnableSignStacking = config.getBoolean("EnableSignStacking", EnableSignStacking);
        Languages = config.getString("Languages", Languages);
    }
    
    private void setupOperations() {
        Operations = new HashMap<String,List<String>>();
        
        HashMap<String,String> tempSignOperations = configUtil.fetchStringStringHashMap("signs", config);

        List<String> tempSignOperationString = new LinkedList();
        List<String> tempCheckedSignOperation = new LinkedList();
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
                tempCheckedSignOperation = new LinkedList();
                failedOp = false;
            }
        }
    }
    
    private void copyFileFromJar(String filename, Boolean delete) {
        InputStream in = getClass().getResourceAsStream("/" + filename);
        File file = new File(instance.getDataFolder(), filename);
        OutputStream os = null;
        if(file.exists() && delete) {
            if(!file.delete()) {
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
        try {
            if(in != null)
                in.close();
            if(os != null)
                os.close();
        } catch(java.io.IOException ioex) {            
            return;
        } 
        
    }
    
    public static String getError(String sType, Map<String, String> messageParts) {        
        Map<String,String> localisedError = Errors.get(SignShopConfig.preferedLanguage);        
        Map<String,String> defaultError = Errors.get(SignShopConfig.baseLanguage);
        
        String error = "";
        if(!localisedError.containsKey(sType) || localisedError.get(sType) == null) {
            if(!defaultError.containsKey(sType) || defaultError.get(sType) == null)
                return "";
            else
                error = defaultError.get(sType);
        } else
            error = localisedError.get(sType);
        return fillInBlanks(error, messageParts);
    }
    
    public static String getMessage(String sType, String sOperation, Map<String, String> messageParts) {
        Map<String,HashMap<String,String>> localisedMessage = Messages.get(SignShopConfig.preferedLanguage);        
        Map<String,HashMap<String,String>> defaultMessage = Messages.get(SignShopConfig.baseLanguage);
        
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
    
    public static String fillInBlanks(String message, Map<String, String> messageParts) {
        if(messageParts == null)
            return message;
        for(Map.Entry<String, String> part : messageParts.entrySet()) {            
            message = message.replace(part.getKey(), part.getValue());
        }        
        message = message.replace("\\", "");
        return message;
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
}
