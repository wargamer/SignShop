package org.wargamer2010.signshop.configuration;

import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.wargamer2010.signshop.SignShop;
import org.wargamer2010.signshop.hooks.HookManager;
import org.wargamer2010.signshop.operations.SignShopArguments;
import org.wargamer2010.signshop.operations.SignShopOperation;
import org.wargamer2010.signshop.operations.SignShopOperationListItem;
import org.wargamer2010.signshop.operations.runCommand;
import org.wargamer2010.signshop.specialops.*;
import org.wargamer2010.signshop.util.ItemMessagePart;
import org.wargamer2010.signshop.util.economyUtil;
import org.wargamer2010.signshop.util.itemUtil;
import org.wargamer2010.signshop.util.signshopUtil;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.logging.Level;

/**
 * Central configuration manager for SignShop plugin.
 *
 * <p>Loads and manages all configuration from config.yml, language files, and operation
 * definitions. Provides access to settings, messages, and the compiled operation cache
 * that powers the shop system.</p>
 *
 * <h2>Key Responsibilities:</h2>
 * <ul>
 *   <li><b>Configuration:</b> Loads config.yml settings (distances, limits, toggles)</li>
 *   <li><b>Languages:</b> Loads message files (en_US.yml, etc.) with template support</li>
 *   <li><b>Operations:</b> Registers and caches operation instances for all sign types</li>
 *   <li><b>Permissions:</b> Manages shop limits, price multipliers, blacklists</li>
 * </ul>
 *
 * <h2>Operation Registration:</h2>
 * <p>At startup, parses the {@code signs:} section of config.yml to build operation lists.
 * Each sign type (e.g., Buy, Sell, Trade) maps to a list of operations:</p>
 * <pre>
 * signs:
 *   Buy:
 *     - takePlayerMoney
 *     - giveShopItems
 *     - giveOwnerMoney
 *     - updateSign
 * </pre>
 * <p>Operations are loaded via reflection from {@code org.wargamer2010.signshop.operations}
 * and cached in {@link #compiledOperations} for fast lookup during transactions.</p>
 *
 * <h2>Message Templates:</h2>
 * <p>Supports message templates with placeholders (e.g., {@code %price%}, {@code %itemname%}).
 * Placeholders are populated from {@link SignShopArguments#messageParts} at runtime.</p>
 *
 * <h2>External Operations:</h2>
 * <p>Third-party plugins can register custom operations via
 * {@link #registerExternalOperation(SignShopOperation)} for integration.</p>
 *
 * @see SignShopOperation
 * @see SignShopOperationListItem
 * @see SignShopArguments
 */
public class SignShopConfig {
    public static final String CONFIG_FILENAME = "config.yml";
    private final String defaultOPPackage = "org.wargamer2010.signshop.operations";
    private final Map<String, SignShopOperation> OperationInstances = new LinkedHashMap<>();
    private final List<SignShopSpecialOp> SpecialsOps = new LinkedList<>();
    private final String baseLanguage = "en_US";
    private final Map<String, SignShopOperation> ExternalOperations = new HashMap<>();
    private List<String> BlacklistedBackOfSignText;
    private Map<String, String> OperationAliases;                         // Alias <-> Original
    private Map<String, Map<String, HashMap<String, String>>> Messages;
    private Map<String, Map<String, String>> Errors;
    private List<Material> BlacklistedItems;
    private Map<String, HashMap<String, Double>> PriceMultipliers;
    private Map<String, List<String>> Commands;
    private Map<String, List<String>> DelayedCommands;
    private Map<String, Integer> ShopLimits;
    private List<LinkableMaterial> LinkableMaterials;
    private Map<String, List<String>> Operations = new HashMap<>();
    private Map<String, List<SignShopOperationListItem>> compiledOperations = new HashMap<>();
    private SignShop instance = null;
    //Configurables
    private FileConfiguration config;
    private int MaxSellDistance = 0;
    private int MaxShopsPerPerson = 0;
    private int ShopCooldown = 0;
    private int MessageCooldown = 0;
    private int ChunkLoadRadius = 2;
    private int MaxChestsPerShop = 100;
    private boolean TransactionLog = false;
    private boolean Debugging = false;
    private boolean MetricsEnabled = true;
    private boolean OPOverride = true;
    private boolean AllowUnsafeEnchantments = false;
    private boolean AllowVariableAmounts = false;
    private boolean AllowEnchantedRepair = true;
    private boolean DisableEssentialsSigns = true;
    private boolean AllowMultiWorldShops = true;
    private boolean EnablePermits = false;
    private boolean PreventVillagerTrade = false;
    private boolean ProtectShopsInCreative = true;
    private boolean ProtectShopsFromExplosions = true;
    private boolean fixIncompleteOperations = true;
    private boolean EnablePriceFromWorth = false;
    private boolean EnableDynmapSupport = false;
    private boolean EnableTutorialMessages = true;
    private boolean EnableShopPlotSupport = true;
    private boolean EnableShopOwnerProtection = true;
    private boolean EnableAutomaticLock = false;
    private boolean UseBlacklistAsWhitelist = false;
    private boolean EnableWrittenBookFix = true;
    private boolean CachePrices = true;
    private boolean ShowMaterialInCustomNames = true;
    private boolean ShowItemDetailsInChat = true;
    private boolean ShowItemHovers = true;
    private CommaDecimalSeparatorState AllowCommaDecimalSeparator = CommaDecimalSeparatorState.AUTO;
    private String ColorCode = "&";
    private String ChatPrefix = "&6[SignShop]";
    private ChatColor TextColor = ChatColor.YELLOW;
    private ChatColor TextColorTwo = ChatColor.DARK_PURPLE;
    private ChatColor MoneyColor = ChatColor.GREEN;
    private ChatColor InStockColor = ChatColor.DARK_BLUE;
    private ChatColor OutOfStockColor = ChatColor.DARK_RED;
    private String Languages = "en_US";
    private String preferedLanguage = "";
    private Material linkMaterial = Material.getMaterial("REDSTONE");
    private Material updateMaterial = Material.getMaterial("INK_SAC");
    private Material destroyMaterial = Material.getMaterial("GOLDEN_AXE");
    private Material inspectMaterial = Material.getMaterial("WRITABLE_BOOK");
    private List<String> DebugClasses = new ArrayList<>();


    public SignShopConfig() {
        signshopUtil.setSignShopConfig(this);
        itemUtil.setSignShopConfig(this);
        economyUtil.setSignShopConfig(this);
        init();
    }

    private List<String> getOrderedListFromArray(String[] array) {
        List<String> list = new LinkedList<>();
        for (String item : array)
            if (!list.contains(item.toLowerCase().trim()))
                list.add(item.toLowerCase().trim());
        return list;
    }


    public void init() {
        instance = SignShop.getInstance();
        initConfig();
        updateLanguageFileNames();
        String adjustedLanguages = createAdjustedLanguages();
        List<String> aLanguages = getOrderedListFromArray(adjustedLanguages.split(","));
        if (!aLanguages.contains("config"))
            aLanguages.add("config");
        Messages = new LinkedHashMap<>();
        Errors = new LinkedHashMap<>();
        preferedLanguage = "";
        for (String language : aLanguages) {
            language = toLanguageCase(language);
            String filename = (language + ".yml");
            String languageName = (language.equals("config") ? baseLanguage : language);
            copyFileFromJar(filename, false);
            File languageFile = new File(instance.getDataFolder(), filename);
            if (languageFile.exists()) {
                FileConfiguration ymlThing = configUtil.loadYMLFromPluginFolder(filename);
                if (ymlThing != null && !language.equals("config")) {
                    configUtil.loadYMLFromJar(ymlThing, filename);
                }

                Messages.put(languageName, configUtil.fetchHasmapInHashmap("messages", ymlThing));
                if (Messages.get(languageName) == null)
                    continue;
                Errors.put(languageName, configUtil.fetchStringStringHashMap("errors", ymlThing));
                if (Errors.get(languageName) == null)
                    continue;
                if (preferedLanguage.isEmpty())
                    preferedLanguage = languageName;
            }
            else {
                SignShop.log("The languagefile " + languageFile + " for language: " + languageName + " could not be found in the plugin directory!", Level.WARNING);
            }
        }
        if (preferedLanguage.isEmpty())
            preferedLanguage = baseLanguage;
        PriceMultipliers = configUtil.fetchDoubleHasmapInHashmap("pricemultipliers", config);
        Commands = configUtil.fetchListInHashmap("commands", config);
        DelayedCommands = configUtil.fetchListInHashmap("timedCommands", config);
        ShopLimits = configUtil.fetchStringIntegerHashMap("limits", config);
        updateFormattedMaterials();
        setupBlacklist();
        setupBackOfSignTextBlacklist();
        copyFileFromJar("materials.yml", false);
        copyFileFromJar("SSQuickReference.pdf", true);
        setupOperations();
        fixIncompleOperations();
        setupHooks();
        setupSpecialsOps();
        setupLinkables();
        setupDebugClasses();
    }

    public String getPreferredLanguage() {
        return preferedLanguage;
    }

    private void setupHooks() {

        HookManager.addHook("WorldGuard");
        HookManager.addHook("GriefPrevention");
        HookManager.addHook("Towny");
        HookManager.addHook("BentoBox");
        HookManager.addHook("Residence");
        HookManager.addHook("LWC");
        HookManager.addHook("BlockLocker");
        HookManager.addHook("Lands");

    }

    private void setupSpecialsOps() {
        SpecialsOps.add(new ConvertChestshop());
        SpecialsOps.add(new CopySign());
        SpecialsOps.add(new LinkSpecialSign());
        SpecialsOps.add(new ChangeOwner());
        SpecialsOps.add(new LinkAdditionalBlocks());
        SpecialsOps.add(new ChangeShopItems());
    }

    private void safeAddLinkeable(String sName, String sGroup) {
        if (sName == null || sGroup == null || sName.isEmpty())
            return;
        LinkableMaterials.add(new LinkableMaterial(sName, sGroup));
    }

    private byte getDataFromString(String dur) {
        try {
            return Byte.parseByte(dur);
        } catch (NumberFormatException ex) {
            return -1;
        }
    }

    private void setupLinkables() {
        LinkableMaterials = new ArrayList<>();
        for (Map.Entry<String, String> entry : configUtil.fetchStringStringHashMap("linkableMaterials", config, false).entrySet()) {
            safeAddLinkeable(entry.getKey(), entry.getValue());
        }
    }

    /**
     * Register a material so that it can be linked and used for a shop
     * The Alias is used when checking for DenyLink permission nodes (i.e. DenyLink.door)
     *
     * @param material Material to register
     * @param alias    Alias to use for the given material
     */
    public void addLinkable(String material, String alias) {
        if (material == null || material.isEmpty())
            return;
        safeAddLinkeable(material.toUpperCase(), alias.toLowerCase());
    }

    private void initConfig() {
        FileConfiguration ymlThing = configUtil.loadYMLFromPluginFolder(CONFIG_FILENAME);
        if (ymlThing == null)
            return;
        configUtil.loadYMLFromJar(ymlThing, CONFIG_FILENAME);

       // ConfigVersionDoNotTouch = ymlThing.getInt("ConfigVersionDoNotTouch", ConfigVersionDoNotTouch);
        MaxSellDistance = ymlThing.getInt("MaxSellDistance", MaxSellDistance);
        TransactionLog = ymlThing.getBoolean("TransactionLog", TransactionLog);
        Debugging = ymlThing.getBoolean("Debugging", Debugging);
        MetricsEnabled = ymlThing.getBoolean("MetricsEnabled", MetricsEnabled);
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
        ProtectShopsFromExplosions = ymlThing.getBoolean("ProtectShopsFromExplosions", ProtectShopsFromExplosions);
        fixIncompleteOperations = ymlThing.getBoolean("fixIncompleteOperations", fixIncompleteOperations);
        EnablePriceFromWorth = ymlThing.getBoolean("EnablePriceFromWorth", EnablePriceFromWorth);
        EnableDynmapSupport = ymlThing.getBoolean("EnableDynmapSupport", EnableDynmapSupport);
        EnableTutorialMessages = ymlThing.getBoolean("EnableTutorialMessages", EnableTutorialMessages);
        EnableShopPlotSupport = ymlThing.getBoolean("EnableShopPlotSupport", EnableShopPlotSupport);
        EnableShopOwnerProtection = ymlThing.getBoolean("EnableShopOwnerProtection", EnableShopOwnerProtection);
        EnableAutomaticLock = ymlThing.getBoolean("EnableAutomaticLock", EnableAutomaticLock);
        UseBlacklistAsWhitelist = ymlThing.getBoolean("UseBlacklistAsWhitelist", UseBlacklistAsWhitelist);
        EnableWrittenBookFix = ymlThing.getBoolean("EnableWrittenBookFix", EnableWrittenBookFix);
        CachePrices = ymlThing.getBoolean("CachePrices", CachePrices);
        ShowMaterialInCustomNames = ymlThing.getBoolean("ShowMaterialInCustomNames", ShowMaterialInCustomNames);
        ShowItemDetailsInChat = ymlThing.getBoolean("ShowItemDetailsInChat", ShowItemDetailsInChat);
        ShowItemHovers = ymlThing.getBoolean("ShowItemHovers", ShowItemHovers);
        AllowCommaDecimalSeparator = CommaDecimalSeparatorState.fromName(ymlThing.getString("AllowCommaDecimalSeparator", AllowCommaDecimalSeparator.name));
        ColorCode = ymlThing.getString("ColorCode", ColorCode);
        ChatPrefix = ymlThing.getString("ChatPrefix", ChatPrefix);
        Languages = ymlThing.getString("Languages", Languages);

        linkMaterial = getMaterial(ymlThing.getString("LinkMaterial", "REDSTONE"), Material.getMaterial("REDSTONE"));
        updateMaterial = getMaterial(ymlThing.getString("UpdateMaterial", "INK_SAC"), Material.getMaterial("INK_SAC"));
        destroyMaterial = getMaterial(ymlThing.getString("DestroyMaterial", "GOLDEN_AXE"), Material.getMaterial("GOLDEN_AXE"));
        inspectMaterial = getMaterial(ymlThing.getString("InspectMaterial", "WRITABLE_BOOK"), Material.getMaterial("WRITABLE_BOOK"));

        TextColor = ChatColor.getByChar(ymlThing.getString("ItemColor", "e").replace(ColorCode, ""));
        if (TextColor == null) TextColor = ChatColor.YELLOW;

        TextColorTwo = ChatColor.getByChar(ymlThing.getString("ItemColorTwo", "5").replace(ColorCode, ""));
        if (TextColorTwo == null) TextColorTwo = ChatColor.DARK_PURPLE;

        MoneyColor = ChatColor.getByChar(ymlThing.getString("MoneyColor", "a").replace(ColorCode, ""));
        if (MoneyColor == null) MoneyColor = ChatColor.GREEN;

        InStockColor = ChatColor.getByChar(ymlThing.getString("InStockColor", "1").replace(ColorCode, ""));
        if (InStockColor == null) InStockColor = ChatColor.DARK_BLUE;

        OutOfStockColor = ChatColor.getByChar(ymlThing.getString("OutOfStockColor", "4").replace(ColorCode, ""));
        if (OutOfStockColor == null) OutOfStockColor = ChatColor.DARK_RED;


        // Sanity check
        if (ChunkLoadRadius > 50 || ChunkLoadRadius < 0)
            ChunkLoadRadius = 3;

        config = ymlThing;
    }

    private Material getMaterial(String mat, Material defaultmat) {
        String name = mat.toUpperCase();
        Material temp = Material.getMaterial(name);
        if (temp == null) {
            if (name.equals("INK_SACK") || name.equals("GOLD_AXE")) {
                SignShop.log("Material called: " + mat + " no longer exists, updating config.yml now.", Level.INFO);
                updateConfigMaterials(name);
            }
            else {
                SignShop.log("Material called: " + mat + " does not exist, please check your config.yml!", Level.WARNING);
            }
            return defaultmat;
        }
        return temp;
    }

    public void updateConfigMaterials(String name) {
        FileConfiguration ymlThing = configUtil.loadYMLFromPluginFolder(CONFIG_FILENAME);
        File configFile = new File(SignShop.getInstance().getDataFolder(), CONFIG_FILENAME);
        if (name.equals("INK_SACK") && ymlThing.getString("UpdateMaterial").equalsIgnoreCase("INK_SACK")) {
            ymlThing.set("UpdateMaterial", "ink_sac");
            SignShop.log("UpdateMaterial changed successfully from ink_sack to ink_sac in the config.yml", Level.INFO);
            saveConfig(ymlThing, configFile);
        }
        if (name.equals("GOLD_AXE") && ymlThing.getString("DestroyMaterial").equalsIgnoreCase("GOLD_AXE")) {
            ymlThing.set("DestroyMaterial", "golden_axe");
            SignShop.log("DestroyMaterial changed successfully from gold_axe to golden_axe in the config.yml", Level.INFO);
            saveConfig(ymlThing, configFile);
        }

    }

    public static void saveConfig(FileConfiguration ymlThing, File file) {
        try {
            ymlThing.save(file);
            configUtil.loadYMLFromJar(ymlThing, CONFIG_FILENAME);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private void setupOperations() {
        setupOperations(configUtil.fetchStringStringHashMap("signs", config));
    }

    public void setupOperations(Map<String, String> allSignOperations) {
        setupOperations(allSignOperations, defaultOPPackage);
    }

    private Object getInstance(String fullClassPath) {
        try {
            if (ExternalOperations.containsKey(fullClassPath)) {
                return ExternalOperations.get(fullClassPath);
            }
            Class<?> aclass = Class.forName(fullClassPath);

            return aclass.getDeclaredConstructor().newInstance();
        } catch (ClassNotFoundException | InvocationTargetException | InstantiationException | IllegalAccessException | NoSuchMethodException ignored) {
            // It failed so the operation or classpath must be invalid.
        }

        return null;
    }

    public void setupOperations(Map<String, String> allSignOperations, String packageName) {
        if (Operations == null)
            Operations = new HashMap<>();

        for (String sKey : allSignOperations.keySet()) {
            boolean failedOp = false;
            List<String> tempCheckedSignOperation = new LinkedList<>();

            for (String tempOperationString : allSignOperations.get(sKey).split("(,(?![^{]*}))")) { //Matches commas outside curly braces
                List<String> bits = signshopUtil.getParameters(tempOperationString.trim());
                String op = bits.getFirst();
                Object opinstance = getInstance(packageName + "." + op.trim());
                if (opinstance == null) // Retry with default package
                    opinstance = getInstance(defaultOPPackage + "." + op.trim());
                if (opinstance == null) {
                    failedOp = true;
                    break;
                }

                if (opinstance instanceof SignShopOperation) {
                    OperationInstances.put(op.trim(), (SignShopOperation) opinstance);
                    tempCheckedSignOperation.add(tempOperationString.trim());
                }
                else {
                    failedOp = true;
                }
            }
            if (!failedOp && !tempCheckedSignOperation.isEmpty())
                Operations.put(sKey.toLowerCase(), tempCheckedSignOperation);
        }

        List<String> aLanguages = getOrderedListFromArray(Languages.split(","));
        aLanguages.remove("english");

        OperationAliases = new HashMap<>();

        for (String language : aLanguages) {
            String filename = (toLanguageCase(language) + ".yml");
            File languageFile = new File(instance.getDataFolder(), filename);
            if (languageFile.exists()) {
                FileConfiguration ymlThing = new YamlConfiguration();
                try {
                    ymlThing.load(languageFile);
                } catch (IOException | InvalidConfigurationException ex) {
                    continue;
                }
                HashMap<String, String> tempSignAliases = configUtil.fetchStringStringHashMap("signs", ymlThing);
                for (Map.Entry<String, String> alias : tempSignAliases.entrySet()) {
                    if (Operations.containsKey(alias.getValue().toLowerCase())) {
                        OperationAliases.put(alias.getKey().toLowerCase(), alias.getValue().toLowerCase());
                    }
                    else {
                        SignShop.log("Could not find " + alias.getValue() + " to alias as " + alias.getKey(), Level.WARNING);
                    }

                }
            }
        }

        // Build compiled operations cache for performance
        buildCompiledOperationsCache();
    }

    /**
     * Builds a cache of compiled operation lists for performance optimization.
     * This eliminates the need to convert operation strings to SignShopOperationListItem
     * objects on every transaction.
     */
    private void buildCompiledOperationsCache() {
        compiledOperations.clear();
        for (Map.Entry<String, List<String>> entry : Operations.entrySet()) {
            List<SignShopOperationListItem> compiled = signshopUtil.getSignShopOps(entry.getValue());
            if (compiled != null) {
                compiledOperations.put(entry.getKey(), compiled);
            }
        }
    }

    private String opListToString(List<String> operations) {
        return signshopUtil.implode(operations.toArray(new String[0]), ",");
    }

    private String fetchCaseCorrectedKey(Map<String, String> map, String lowercased) {
        for (Map.Entry<String, String> entry : map.entrySet()) {
            if (entry.getKey().equalsIgnoreCase(lowercased))
                return entry.getKey();
        }
        return "";
    }

    private void fixIncompleOperations() {
        if (!fixIncompleteOperations)
            return;
        HashMap<String, String> tempSignOperations = configUtil.fetchStringStringHashMap("signs", config, true);
        boolean changedSomething = false;
        for (Map.Entry<String, List<String>> entry : Operations.entrySet()) {
            if (Commands.containsKey(entry.getKey().toLowerCase())) {
                List<SignShopOperationListItem> tempList = signshopUtil.getSignShopOps(entry.getValue());
                boolean found = false;
                for (SignShopOperationListItem tempOp : tempList)
                    if (tempOp.getOperation() instanceof runCommand || tempOp.getParameters().contains("runCommand")) {
                        found = true;
                        break;
                    }
                if (found)
                    continue;
                entry.getValue().add("runCommand");
                String opName = fetchCaseCorrectedKey(tempSignOperations, entry.getKey());
                if (opName.isEmpty())
                    opName = entry.getKey();
                config.set(("signs." + opName), opListToString(entry.getValue()));
                if (!changedSomething)
                    changedSomething = true;
                SignShop.log("Added runCommand block to " + opName + " because it has a corresponding entry in the Commands section. Set fixIncompleteOperations to false to disable this function.", Level.INFO);
            }
        }
        if (changedSomething)
            configUtil.loadYMLFromJar(config, CONFIG_FILENAME);
    }

    private void closeStream(Closeable in) {
        if (in == null)
            return;
        try {
            in.close();
        } catch (IOException ignored) {

        }
    }

    private void copyFileFromJar(String filename, Boolean delete) {
        InputStream in = SignShopConfig.class.getResourceAsStream("/" + filename);
        File file = new File(instance.getDataFolder(), filename);
        OutputStream os = null;
        if (file.exists() && delete) {
            if (!file.delete()) {
                closeStream(in);
                return;
            }
        }
        else if (file.exists() && !delete) {
            return;
        }
        try {
            if (in.available() == 0)
                return;
            file.createNewFile();
            os = new FileOutputStream(file.getPath());
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = in.read(buffer)) != -1) {
                os.write(buffer, 0, bytesRead);
            }
        } catch (IOException | NullPointerException ignored) {

        }

        closeStream(in);
        closeStream(os);
    }

    public String getError(String sType, Map<String, Object> messageParts) {
        Map<String, String> localisedError = Errors.get(preferedLanguage);
        Map<String, String> defaultError = Errors.get(baseLanguage);

        String error;
        if (!localisedError.containsKey(sType) || localisedError.get(sType) == null) {
            if (!defaultError.containsKey(sType) || defaultError.get(sType) == null)
                return "";
            else
                error = defaultError.get(sType);
        }
        else
            error = localisedError.get(sType);
        String coloredError = ChatColor.translateAlternateColorCodes(getColorCode(), error);
        return fillInBlanks(coloredError, messageParts);
    }

    public String getMessage(String sType, String pOperation, Map<String, Object> messageParts) {
        Map<String, HashMap<String, String>> localisedMessage = Messages.get(preferedLanguage);
        Map<String, HashMap<String, String>> defaultMessage = Messages.get(baseLanguage);

        String sOperation = pOperation;
        if (OperationAliases.containsKey(sOperation))
            sOperation = OperationAliases.get(sOperation);

        String message;
        if (!localisedMessage.containsKey(sType) || !localisedMessage.get(sType).containsKey(sOperation) || localisedMessage.get(sType).get(sOperation) == null) {
            if (!defaultMessage.containsKey(sType) || !defaultMessage.get(sType).containsKey(sOperation) || defaultMessage.get(sType).get(sOperation) == null) {
                return "";
            }
            else
                message = defaultMessage.get(sType).get(sOperation);
        }
        else
            message = localisedMessage.get(sType).get(sOperation);

        String coloredMessage = ChatColor.translateAlternateColorCodes(getColorCode(), message);
        return fillInBlanks(coloredMessage, messageParts);
    }

    /**
     * Gets a message as a BaseComponent with hover events for rich content like items.
     *
     * @param sType The message type (e.g., "transaction", "confirm")
     * @param pOperation The operation name (e.g., "Buy", "Sell")
     * @param messageParts Map of placeholders to values (String or ItemMessagePart)
     * @return BaseComponent with hover tooltips
     */
    public BaseComponent getMessageAsComponent(String sType, String pOperation, Map<String, Object> messageParts) {
        Map<String, HashMap<String, String>> localisedMessage = Messages.get(preferedLanguage);
        Map<String, HashMap<String, String>> defaultMessage = Messages.get(baseLanguage);

        String sOperation = pOperation;
        if (OperationAliases.containsKey(sOperation))
            sOperation = OperationAliases.get(sOperation);

        String message;
        if (!localisedMessage.containsKey(sType) || !localisedMessage.get(sType).containsKey(sOperation) || localisedMessage.get(sType).get(sOperation) == null) {
            if (!defaultMessage.containsKey(sType) || !defaultMessage.get(sType).containsKey(sOperation) || defaultMessage.get(sType).get(sOperation) == null) {
                return new TextComponent("");
            }
            else
                message = defaultMessage.get(sType).get(sOperation);
        }
        else
            message = localisedMessage.get(sType).get(sOperation);

        String coloredMessage = ChatColor.translateAlternateColorCodes(getColorCode(), message);
        return fillInBlanksAsComponent(coloredMessage, messageParts);
    }

    /**
     * Gets an error message as a BaseComponent with hover events.
     *
     * @param sType The error type
     * @param messageParts Map of placeholders to values (String or ItemMessagePart)
     * @return BaseComponent with hover tooltips
     */
    public BaseComponent getErrorAsComponent(String sType, Map<String, Object> messageParts) {
        Map<String, String> localisedError = Errors.get(preferedLanguage);
        Map<String, String> defaultError = Errors.get(baseLanguage);

        String error;
        if (!localisedError.containsKey(sType) || localisedError.get(sType) == null) {
            if (!defaultError.containsKey(sType) || defaultError.get(sType) == null)
                return new TextComponent("");
            else
                error = defaultError.get(sType);
        }
        else
            error = localisedError.get(sType);

        String coloredError = ChatColor.translateAlternateColorCodes(getColorCode(), error);
        return fillInBlanksAsComponent(coloredError, messageParts);
    }

    public List<String> getBlocks(String pOp) {
        String op = pOp;
        if (OperationAliases.containsKey(op))
            op = OperationAliases.get(op);

        if (Operations.containsKey(op))
            return Operations.get(op);
        else
            return new LinkedList<>();
    }

    /**
     * Gets the compiled operation list for a shop type from cache.
     * This is significantly faster than calling getBlocks() + getSignShopOps()
     * because the operations are pre-compiled at config load time.
     *
     * @param pOp The operation name or alias (e.g., "Buy", "Sell")
     * @return The compiled list of operations, or null if not found
     */
    public List<SignShopOperationListItem> getCompiledOperations(String pOp) {
        String op = pOp;
        if (OperationAliases.containsKey(op))
            op = OperationAliases.get(op);

        return compiledOperations.get(op);
    }

    public Collection<String> getOperations() {
        return Collections.unmodifiableCollection(Operations.keySet());
    }

    public Collection<String> getAliases(String op) {
        Collection<String> aliases = new LinkedList<>();
        if (Languages.contains(baseLanguage))
            aliases.add(op); // If the baseLanguage is explicitly requested, we'll add the english OP as an alias
        for (Map.Entry<String, String> entry : OperationAliases.entrySet()) {
            if (entry.getValue().equals(op))
                aliases.add(entry.getKey());
        }
        return aliases;
    }

    public void registerExternalOperation(SignShopOperation signShopOperation) {
        ExternalOperations.put(signShopOperation.getClass().getName(), signShopOperation);
    }

    public boolean registerOperation(String sName, List<String> blocks) {
        if (sName != null && blocks != null && !blocks.isEmpty()) {
            Operations.put(sName, blocks);
            return true;
        }
        return false;
    }

    public boolean registerMessage(String type, String shop, String message) {
        return registerMessage(baseLanguage, type, shop, message);
    }

    public boolean registerMessage(String language, String type, String shop, String message) {
        if (Messages.containsKey(language) && Messages.get(language).containsKey(type)) {
            HashMap<String, String> temp = Messages.get(language).get(type);
            temp.put(shop, message);
            return true;
        }
        else {
            return false;
        }
    }

    public boolean registerMessages(String type, Map<String, String> messagesByShop) {
        return registerMessages(baseLanguage, type, messagesByShop);
    }

    public boolean registerMessages(String language, String type, Map<String, String> messagesByShop) {
        if (Messages.containsKey(language) && Messages.get(language).containsKey(type)) {
            HashMap<String, String> temp = Messages.get(language).get(type);
            temp.putAll(messagesByShop);
            return true;
        }
        else {
            return false;
        }
    }

    public boolean registerErrorMessage(String type, String message) {
        return registerErrorMessage(baseLanguage, type, message);
    }

    public boolean registerErrorMessage(String language, String type, String message) {
        if (Messages.containsKey(language)) {
            Map<String, String> temp = Errors.get(language);
            temp.put(type, message);
            return true;
        }
        else {
            return false;
        }
    }

    public boolean registerErrorMessages(Map<String, String> messagesByType) {
        return registerErrorMessages(baseLanguage, messagesByType);
    }

    public boolean registerErrorMessages(String language, Map<String, String> messagesByType) {
        if (Messages.containsKey(language)) {
            Errors.get(language).putAll(messagesByType);
            return true;
        }
        else {
            return false;
        }
    }

    /**
     * Fills in placeholder values in a message template. Values can be Strings or ItemMessagePart objects.
     * For ItemMessagePart values, the cached string representation is used.
     *
     * @param pMessage The message template with placeholders (e.g., "You bought !items for !price!")
     * @param messageParts Map of placeholder names to values (String or ItemMessagePart)
     * @return The message with all placeholders replaced
     */
    public String fillInBlanks(String pMessage, Map<String, Object> messageParts) {
        String message = pMessage;

        if (messageParts == null)
            return message;

        // Use TreeMap with StringLengthComparator to process longest placeholders first
        // This prevents "!item" from matching when "!items" exists
        TreeMap<String, String> temp = new TreeMap<>(new StringLengthComparator());

        // Convert all values to strings (extract from ItemMessagePart if needed)
        for (Map.Entry<String, Object> entry : messageParts.entrySet()) {
            if (entry.getValue() instanceof String) {
                temp.put(entry.getKey(), (String) entry.getValue());
            } else if (entry.getValue() instanceof org.wargamer2010.signshop.util.ItemMessagePart) {
                temp.put(entry.getKey(), ((org.wargamer2010.signshop.util.ItemMessagePart) entry.getValue()).getString());
            }
        }

        for (Map.Entry<String, String> part : temp.entrySet()) {
            if (part != null && part.getKey() != null && part.getValue() != null)
                message = message.replace(part.getKey(), part.getValue());
        }
        message = message.replace("\\", "");
        return message;
    }

    /**
     * Fills in placeholder values in a message template and returns a BaseComponent with hover events.
     * This method processes ItemMessagePart values as rich hover tooltips showing full item details.
     *
     * @param pMessage The message template with placeholders (e.g., "You bought !items for !price!")
     * @param messageParts Map of placeholder names to values (String or ItemMessagePart)
     * @return BaseComponent with hover events for items
     */
    public BaseComponent fillInBlanksAsComponent(String pMessage, Map<String, Object> messageParts) {
        if (pMessage == null || pMessage.isEmpty())
            return new TextComponent("");

        String workingMessage = ChatColor.translateAlternateColorCodes(getColorCode(), pMessage);
        // Remove escape characters from template before processing
        workingMessage = workingMessage.replace("\\", "");

        if (messageParts == null || messageParts.isEmpty()) {
            // No placeholders to replace, convert legacy colors and return
            return TextComponent.fromLegacy(workingMessage);
        }

        // Build component by scanning message left-to-right, replacing placeholders as found
        // Use TreeMap with StringLengthComparator to process longest placeholders first
        // This prevents "!item" from matching when "!items" exists
        TreeMap<String, Object> sortedParts = new TreeMap<>(new StringLengthComparator());
        sortedParts.putAll(messageParts);

        ComponentBuilder builder = new ComponentBuilder("");
        boolean hasContent = false;
        int currentPos = 0;

        // Scan message left-to-right
        while (currentPos < workingMessage.length()) {
            // Find the next placeholder (check all placeholders, pick the earliest one)
            int earliestIndex = -1;
            String earliestPlaceholder = null;
            Object earliestValue = null;

            for (Map.Entry<String, Object> entry : sortedParts.entrySet()) {
                int index = workingMessage.indexOf(entry.getKey(), currentPos);
                if (index != -1 && (earliestIndex == -1 || index < earliestIndex)) {
                    earliestIndex = index;
                    earliestPlaceholder = entry.getKey();
                    earliestValue = entry.getValue();
                }
            }

            if (earliestIndex == -1) {
                // No more placeholders, add remaining text
                builder.append(TextComponent.fromLegacy(workingMessage.substring(currentPos)), ComponentBuilder.FormatRetention.NONE);
                hasContent = true;
                break;
            }

            // Add text before placeholder
            if (earliestIndex > currentPos) {
                builder.append(TextComponent.fromLegacy(workingMessage.substring(currentPos, earliestIndex)), ComponentBuilder.FormatRetention.NONE);
                hasContent = true;
            }

            // Add placeholder value
            if (earliestValue instanceof ItemMessagePart) {
                BaseComponent itemComponent = itemUtil.itemStackToComponent(((ItemMessagePart) earliestValue).getItems());
                builder.append(itemComponent, ComponentBuilder.FormatRetention.NONE);
                hasContent = true;
            } else if (earliestValue instanceof String) {
                builder.append(TextComponent.fromLegacy((String) earliestValue), ComponentBuilder.FormatRetention.NONE);
                hasContent = true;
            }

            // Move past this placeholder
            currentPos = earliestIndex + earliestPlaceholder.length();
        }

        // Build final component
        BaseComponent[] components = hasContent ? builder.create() : new BaseComponent[0];
        return components.length > 0 ? new TextComponent(components) : new TextComponent("");
    }

    private void setupBlacklist() {
        List<String> tempList = config.getStringList("Blacklisted_items");
        BlacklistedItems = new LinkedList<>();
        for (String item : tempList) {
            Material mat = Material.getMaterial(item.toUpperCase());
            if (mat != null)
                BlacklistedItems.add(mat);
            else
                SignShop.log("Material called: " + item + " could not be added to the blacklist as it does not exist, please check your config.yml!", Level.WARNING);
        }
    }

    public Boolean isItemOnBlacklist(Material mat) {
        return (
                (BlacklistedItems.contains(mat) && !getUseBlacklistAsWhitelist())
                        ||
                        (!BlacklistedItems.contains(mat) && getUseBlacklistAsWhitelist())
        );
    }

    private void setupBackOfSignTextBlacklist() {
        BlacklistedBackOfSignText = new ArrayList<>();
        config.getStringList("BlacklistedBackOfSignText").forEach(s -> BlacklistedBackOfSignText.add(ChatColor.stripColor(s.toLowerCase())));
    }
    private void setupDebugClasses() {
        DebugClasses = new ArrayList<>();
        config.getStringList("DebugClasses").forEach(s -> DebugClasses.add(ChatColor.stripColor(s.toUpperCase())));

    }

    public boolean weAreDebuggingClass(String className) {
       return DebugClasses.contains("ALL") || DebugClasses.contains(className.toUpperCase());
    }

    public Boolean stringIsOnBackOfSignTextBlacklist(String string){
        return BlacklistedBackOfSignText.contains(string);
    }

    public ItemStack isAnyItemOnBlacklist(ItemStack[] stacks) {
        if (stacks == null)
            return null;
        for (ItemStack single : stacks) {
            if (single == null)
                continue;
            if (isItemOnBlacklist(single.getType())) {
                return single;
            }
        }
        return null;
    }

    public int getMaxChestsPerShop() {
        return MaxChestsPerShop;
    }

    public Boolean ExceedsMaxChestsPerShop(int currentAmountOfChests) {
        return MaxChestsPerShop != 0 && currentAmountOfChests > MaxChestsPerShop;
    }

    public Map<String, SignShopOperation> getOperationInstances() {
        return Collections.unmodifiableMap(OperationInstances);
    }

    public Map<String, HashMap<String, Double>> getPriceMultipliers() {
        return Collections.unmodifiableMap(PriceMultipliers);
    }

    public Map<String, List<String>> getCommands() {
        return Collections.unmodifiableMap(Commands);
    }

    public Map<String, List<String>> getDelayedCommands() {
        return Collections.unmodifiableMap(DelayedCommands);
    }

    public Map<String, Integer> getShopLimits() {
        return Collections.unmodifiableMap(ShopLimits);
    }

    public List<LinkableMaterial> getLinkableMaterials() {
        return Collections.unmodifiableList(LinkableMaterials);
    }

    public List<SignShopSpecialOp> getSpecialOps() {
        return Collections.unmodifiableList(SpecialsOps);
    }

    public int getMaxSellDistance() {
        return MaxSellDistance;
    }

    public int getMaxShopsPerPerson() {
        return MaxShopsPerPerson;
    }

    public int getShopCooldown() {
        return ShopCooldown;
    }

    public int getChunkLoadRadius() {
        return ChunkLoadRadius;
    }

    public int getMessageCooldown() {
        return MessageCooldown;
    }

    public boolean getOPOverride() {
        return OPOverride;
    }

    public boolean getAllowVariableAmounts() {
        return AllowVariableAmounts;
    }

    public boolean getAllowEnchantedRepair() {
        return AllowEnchantedRepair;
    }

    public boolean getAllowUnsafeEnchantments() {
        return AllowUnsafeEnchantments;
    }

    public boolean getAllowMultiWorldShops() {
        return AllowMultiWorldShops;
    }

    public boolean getEnablePermits() {
        return EnablePermits;
    }

    public boolean getPreventVillagerTrade() {
        return PreventVillagerTrade;
    }

    public boolean getProtectShopsInCreative() {
        return ProtectShopsInCreative;
    }

    public boolean getProtectShopsFromExplosions() {
        return ProtectShopsFromExplosions;
    }

    public boolean getTransactionLog() {
        return TransactionLog;
    }

    public boolean getDisableEssentialsSigns() {
        return DisableEssentialsSigns;
    }

    public boolean getEnablePriceFromWorth() {
        return EnablePriceFromWorth;
    }

    public boolean getEnableDynmapSupport() {
        return EnableDynmapSupport;
    }

    public boolean getShowMaterialInCustomNames() {
        return ShowMaterialInCustomNames;
    }

    public boolean getShowItemDetailsInChat() {
        return ShowItemDetailsInChat;
    }

    public boolean getShowItemHovers() {
        return ShowItemHovers;
    }

    public boolean getEnableTutorialMessages() {
        return EnableTutorialMessages;
    }

    public boolean getEnableShopPlotSupport() {
        return EnableShopPlotSupport;
    }

    public boolean getEnableShopOwnerProtection() {
        return EnableShopOwnerProtection;
    }


    public boolean getEnableAutomaticLock() {
        return EnableAutomaticLock;
    }

    public boolean getUseBlacklistAsWhitelist() {
        return UseBlacklistAsWhitelist;
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean getEnableWrittenBookFix() {
        return EnableWrittenBookFix;
    }

    public boolean debugging() {
        return Debugging;
    }

    public boolean metricsEnabled() {
        return MetricsEnabled;
    }

    public boolean isOPMaterial(Material check) {
        return (check == updateMaterial || check == linkMaterial);
    }

    public boolean isLinkMaterial(Material material) {
        return material == linkMaterial;
    }

    public boolean isInspectionMaterial(ItemStack item) {
        return (item != null && item.getType() == inspectMaterial);
    }

    public CommaDecimalSeparatorState allowCommaDecimalSeparator() {
        return AllowCommaDecimalSeparator;
    }

    public void setAllowCommaDecimalSeparator(CommaDecimalSeparatorState state, boolean doSave) {
        AllowCommaDecimalSeparator = state;

        if (doSave) {
            FileConfiguration ymlThing = configUtil.loadYMLFromPluginFolder(CONFIG_FILENAME);
            File configFile = new File(SignShop.getInstance().getDataFolder(), CONFIG_FILENAME);
            ymlThing.set("AllowCommaDecimalSeparator", state.name);
            saveConfig(ymlThing, configFile);
        }
    }

    public void setAllowCommaDecimalSeparator(CommaDecimalSeparatorState state) {
        setAllowCommaDecimalSeparator(state, true);
    }

    public boolean cachePrices() {
        return CachePrices;
    }

    public void setCachePrices(boolean value) {
        CachePrices = value;
    }

    public Material getLinkMaterial() {
        return linkMaterial;
    }

    public Material getUpdateMaterial() {
        return updateMaterial;
    }

    public Material getDestroyMaterial() {
        return destroyMaterial;
    }

    public String getChatPrefix() {
        return ChatColor.translateAlternateColorCodes(getColorCode(), ChatPrefix);
    }

    public char getColorCode() {
        char[] code = ColorCode.toCharArray();
        return code[0];
    }

    public ChatColor getTextColor() {
        return TextColor;
    }

    public ChatColor getTextColorTwo() {
        return TextColorTwo;
    }

    public ChatColor getMoneyColor() {
        return MoneyColor;
    }

    public ChatColor getInStockColor() {
        return InStockColor;
    }

    public ChatColor getOutOfStockColor() {
        return OutOfStockColor;
    }

    private String toLanguageCase(String language) {
        String[] languageParts = language.split("_");
        if (languageParts.length == 2) {
            return (languageParts[0].toLowerCase() + "_" + languageParts[1].toUpperCase());
        }
        return language;
    }

    private String createAdjustedLanguages() {
        String adjustedLanguages = Languages;
        for (LanguageSpelling languageSpelling : LanguageSpelling.values()) {
            adjustedLanguages = adjustedLanguages.replace(languageSpelling.oldName, languageSpelling.localeName);
        }
        adjustedLanguages = adjustedLanguages.replace("en_US", "config");
        return adjustedLanguages;

    }

    private void updateLanguageFileNames() {
        for (LanguageSpelling languageSpelling : LanguageSpelling.values()) {
            File oldLangFile = new File(instance.getDataFolder(), languageSpelling.oldName + ".yml");
            if (oldLangFile.exists()) {
                SignShop.log("Renaming deprecated language filename '" + languageSpelling.oldName + ".yml' to '" + languageSpelling.localeName + ".yml'.", Level.INFO);
                oldLangFile.renameTo(new File(instance.getDataFolder(), languageSpelling.localeName + ".yml"));

            }
        }
    }

    private void updateFormattedMaterials() {
        File file = new File(SignShop.getInstance().getDataFolder(), "materials.yml");
        FileConfiguration configuration = YamlConfiguration.loadConfiguration(file);
        ConfigurationSection section = configuration.getConfigurationSection("materials");
        if (section != null) {
            for (String matString : section.getKeys(false)) {
                Material matKey = Material.matchMaterial(matString);
                String customName = section.getString(matString);
                itemUtil.updateFormattedMaterial(matKey, ChatColor.translateAlternateColorCodes(getColorCode(), customName));
            }
        }
    }

    public String getDebugClasses() {
        return DebugClasses.toString();
    }


    public enum CommaDecimalSeparatorState {
        AUTO("auto", false),
        FALSE("false", false),
        TRUE("true", true);

        private final String name;
        private final boolean permitted;

        CommaDecimalSeparatorState(String name, boolean permitted) {
            this.name = name;
            this.permitted = permitted;
        }

        public static CommaDecimalSeparatorState fromName(String name) {
            for (CommaDecimalSeparatorState state : CommaDecimalSeparatorState.values()) {
                if (state.name.equalsIgnoreCase(name)) return state;
            }

            return CommaDecimalSeparatorState.AUTO;
        }

        public String getName() {
            return name;
        }

        public boolean isPermitted() {
            return permitted;
        }
    }


    private enum LanguageSpelling {
        ENGLISH("english", "config"),
        CHINESE("chinese", "zh_TW"),
        DUTCH("dutch", "nl_NL"),
        FRENCH("french", "fr_FR"),
        GERMAN("german", "de_DE"),
        PORTUGUESE("portuguese", "pt_PT"),
        RUSSIAN("russian", "ru_RU"),
        SPANISH("spanish", "es_ES");

        final String oldName;
        final String localeName;

        LanguageSpelling(String oldName, String localeName) {
            this.oldName = oldName;
            this.localeName = localeName;
        }
    }

    /**
     * Orders strings by their length from long to short
     */
    private static class StringLengthComparator implements Comparator<String> {
        @SuppressWarnings("ComparatorMethodParameterNotUsed")
        @Override
        public int compare(String s1, String s2) {
            int s2Length = s2.length();
            int s1Length = s1.length();
            if (s2Length == s1Length)
                return -1;
            return s2Length - s1Length;
        }
    }
}
