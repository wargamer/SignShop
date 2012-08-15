package org.wargamer2010.signshop;

import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import java.util.HashMap;
import java.io.IOException;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.FileOutputStream;
import java.util.List;
import java.util.Map;
import java.util.LinkedList;
import java.util.Arrays;
import java.util.logging.*;
import java.util.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import org.wargamer2010.signshop.listeners.*;
import org.wargamer2010.signshop.hooks.HookManager;
import org.wargamer2010.signshop.util.itemUtil;
import com.bergerkiller.bukkit.common.SafeField;
import org.wargamer2010.signshop.util.clicks;
import org.wargamer2010.signshop.util.signshopUtil;
import org.wargamer2010.signshop.metrics.setupMetrics;

public class SignShop extends JavaPlugin{
    private final SignShopPlayerListener playerListener = new SignShopPlayerListener();
    private final SignShopBlockListener blockListener = new SignShopBlockListener();
    private static SignShop instance;

    private static final Logger logger = Logger.getLogger("Minecraft");
    private static final Logger transactionlogger = Logger.getLogger("SignShop_Transactions");

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
    

    //Statics
    public static Storage Storage;
    public static Map<String,List<String>> Operations;
    public static Map<String,HashMap<String,String>> Messages;
    public static Map<String,String> Errors;
    public static Map<String,HashMap<String,Float>> PriceMultipliers;
    public static Map<String,List> Commands;
    public static Map<String,Integer> ShopLimits;
    public static Map<Material, String> LinkableMaterials;
    public static List<String> SpecialsOps = new LinkedList();
    
    //Permissions
    public static boolean USE_PERMISSIONS = false;    
    
    // Vault
    private Vault vault = null;
    private setupMetrics metricsSetup = null;
    
    //Logging
    public void log(String message, Level level,int tag) {
        if(!message.equals(""))
            logger.log(level,("[SignShop] ["+tag+"] " + message));
    }
    public static void log(String message, Level level) {
        if(!message.equals(""))
            logger.log(level,("[SignShop] " + message));
    }
    public static void logTransaction(String customer, String owner, String Operation, String items, String Price) {
        if(SignShop.TransactionLog && !items.equals("")) {
            String message = ("Customer: " + customer + ", Owner: " + owner + ", Operation: " + Operation + ", Items: " + items + ", Price: " + Price);
            transactionlogger.log(Level.FINER, message);
        }
    }
            
    private void checkOldDir() {
        File olddir = new File("plugins", "SignShops");
        if(olddir.exists()) {
            if(!this.getDataFolder().exists()) {                
                boolean renamed = olddir.renameTo(this.getDataFolder());
                if(renamed)
                    log("Old configuration directory (SignShops) found and succesfully migrated.", Level.INFO);
                else
                    log("Old configuration directory (SignShops) found, but could not rename to SignShop. Please move configs manually!", Level.INFO);
            } else
                log("Old configuration directory (SignShops) found, but new (SignShop) exists. Please move configs manually!", Level.INFO);
        }
    }
    
    private void setItemMaxSize(Material material, int maxstacksize) {        
        SafeField.set(net.minecraft.server.Item.byId[material.getId()], "maxStackSize", maxstacksize);
    }
    
    private void fixStackSize() {
        if(config.getBoolean("EnableSignStacking", false)) {
            setItemMaxSize(Material.SIGN, 64);
            setItemMaxSize(Material.SIGN_POST, 64);
            setItemMaxSize(Material.WALL_SIGN, 64);
        }
    }
    
    @Override
    public void onEnable() {
        // Migrate configs from old directory
        this.checkOldDir();        
        if(!this.getDataFolder().exists()) {
            this.getDataFolder().mkdir();
        }
        initConfig();      
        fixStackSize();
        itemUtil.initDiscs();        
        clicks.init();
        instance = this;
        metricsSetup = new setupMetrics();
        if(metricsSetup.setup(this))
            log("Succesfully started Metrics, see http://mcstats.org for more information.", Level.INFO);
        else
            log("Could not start Metrics, see http://mcstats.org for more information.", Level.INFO);
        
        SignShop.Messages = configUtil.fetchHasmapInHashmap("messages", config);
        SignShop.Errors = configUtil.fetchStringStringHashMap("errors", config);
        SignShop.PriceMultipliers = configUtil.fetchFloatHasmapInHashmap("pricemultipliers", config);
        SignShop.Commands = configUtil.fetchListInHashmap("commands", config);
        SignShop.ShopLimits = configUtil.fetchStringIntegerHashMap("limits", config);
                
        //Create a storage locker for shops        
        SignShop.Storage = new Storage(new File(this.getDataFolder(),"sellers.yml"),this);
        SignShop.Storage.Save();
        
        try {
            FileHandler fh = new FileHandler("plugins/SignShop/Transaction.log", true);
            TransferFormatter formatter = new TransferFormatter();
            fh.setFormatter(formatter);
            fh.setLevel(Level.FINEST);
            transactionlogger.addHandler(fh);
            transactionlogger.setLevel(Level.FINEST);
            logger.setUseParentHandlers(false);
        } catch(IOException ex) {
            log("Failed to create transaction log", Level.INFO);
        }

        setupOperations();
        setupVault();
        setupHooks();
        setupLinkables();
        setupSpecialsOps();
        copyPDF();
        
        PluginDescriptionFile pdfFile = this.getDescription();
        PluginManager pm = getServer().getPluginManager();
        if(Vault.vaultFound) {
            // Register events
            pm.registerEvents(playerListener, this);
            pm.registerEvents(blockListener, this);
            if(DisableEssentialsSigns) {
                SignShopServerListener SListener = new SignShopServerListener(getServer());
                pm.registerEvents(SListener, this);
            }
            log("v" + pdfFile.getVersion() + " Enabled", Level.INFO);
        } else {
            SignShopLoginListener login = new SignShopLoginListener(this);
            pm.registerEvents(login, this);
            log("v" + pdfFile.getVersion() + " Disabled", Level.INFO);
        }
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String args[]) {
        String commandName = cmd.getName().toLowerCase();        
        if(!commandName.equalsIgnoreCase("signshop"))
            return true;
        if(args.length != 1)
            return false;
        if((sender instanceof Player) && !((Player)sender).isOp()) {
            ((Player)sender).sendMessage(ChatColor.RED + "You are not allowed to use that command. OP only.");
            return true;
        }
        if(args[0].equals("reload")) {
            Bukkit.getServer().getPluginManager().disablePlugin(this);
            Bukkit.getServer().getPluginManager().enablePlugin(this);
        } else
            return false;
        SignShop.log("Reloaded", Level.INFO);
        if((sender instanceof Player))
            ((Player)sender).sendMessage(ChatColor.GREEN + "SignShop has been reloaded");
        return true;
    }
    
    public void initConfig() {
        this.reloadConfig();
        File configFile = new File("plugins/SignShop", "config.yml");
        if(!configFile.exists()) {
            this.saveDefaultConfig();
            this.saveConfig();
        }
        config = this.getConfig();        
        config.options().copyDefaults(true);
        this.saveConfig();
        this.reloadConfig();
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
    }
    
    public static SignShop getInstance() {
        return instance;
    }
    
    public String getLogPrefix() {
        PluginDescriptionFile pdfFile = this.getDescription();
        String prefix = "[SignShop] [" +pdfFile.getVersion() +"]";
        return prefix;
    }
    
    private void closeHandlers() {
        Handler[] handlers = transactionlogger.getHandlers();
        for(int i = 0; i < handlers.length; i++)
            handlers[i].close();
    }
    
    @Override
    public void onDisable(){
        SignShop.Storage.Save();
        closeHandlers();
        
        log("Disabled", Level.INFO);
    }

    private void setupVault() {
        vault = new Vault(getServer());
        vault.setupChat();
        Boolean vault_Perms = vault.setupPermissions();
        if(!vault_Perms || Vault.permission.getName().equals("SuperPerms")) {
            log("Vault's permissions not found, defaulting to OP.", Level.INFO);
            USE_PERMISSIONS = false;
        } else
            USE_PERMISSIONS = true;
        Boolean vault_Economy = vault.setupEconomy();
        if(!vault_Economy)
            log("Could not hook into Vault's Economy!", Level.WARNING);
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
        // SpecialsOps.add("linkShareSign");
        SpecialsOps.add("changeOwner");
    }
    
    private void setupOperations() {
        SignShop.Operations = new HashMap<String,List<String>>();
        
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
                    SignShop.Operations.put(sKey, tempCheckedSignOperation);                
                tempCheckedSignOperation = new LinkedList();
                failedOp = false;
            }
        }
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
    
    private void copyPDF() {
        InputStream in = getClass().getResourceAsStream("/SSQuickReference.pdf");
        File file = new File(this.getDataFolder(), "SSQuickReference.pdf");        
        if(file.exists())
            if(!file.delete())
                return;
        try {
            file.createNewFile();

            OutputStream os = new FileOutputStream(file.getPath());
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = in.read(buffer)) != -1) {
              os.write(buffer, 0, bytesRead);
            }
            in.close();
            os.close();
        } catch(java.io.FileNotFoundException notfoundex) {
            return;
        } catch(java.io.IOException ioex) {
            return;
        } catch(NullPointerException nullex) {
            return;
        }
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
    
    public class TransferFormatter extends Formatter {    
        private final DateFormat df = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss.SSS");
        
        @Override
        public String format(LogRecord record) {
            StringBuilder builder = new StringBuilder(1000);
            builder.append(df.format(new Date(record.getMillis()))).append(" - ");            
            builder.append("[").append(record.getLevel()).append("] - ");
            builder.append(formatMessage(record));
            builder.append("\n");
            return builder.toString();
        }

        @Override
        public String getHead(Handler h) {
            return super.getHead(h);
        }

        @Override
        public String getTail(Handler h) {
            return super.getTail(h);
        }
    }
}