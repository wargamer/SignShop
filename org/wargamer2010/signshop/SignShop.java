package org.wargamer2010.signshop;

import org.wargamer2010.signshop.configuration.Storage;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import java.io.IOException;
import java.io.File;
import java.util.logging.*;
import java.util.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import org.wargamer2010.signshop.listeners.*;
import org.wargamer2010.signshop.util.itemUtil;
import com.bergerkiller.bukkit.common.SafeField;
import org.wargamer2010.signshop.blocks.SignShopBooks;
import org.wargamer2010.signshop.configuration.SignShopConfig;
import org.wargamer2010.signshop.util.clicks;
import org.wargamer2010.signshop.metrics.setupMetrics;

public class SignShop extends JavaPlugin{
    private final SignShopPlayerListener playerListener = new SignShopPlayerListener();
    private final SignShopBlockListener blockListener = new SignShopBlockListener();
    private final SignShopLoginListener loginListener = new SignShopLoginListener();
    private static SignShop instance;

    private static final Logger logger = Logger.getLogger("Minecraft");
    private static final Logger transactionlogger = Logger.getLogger("SignShop_Transactions");

    //Statics
    public static Storage Storage;    
    private static SignShopConfig SignShopConfig;    
    public static SignShopBooks BookStore = new SignShopBooks();
    
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
        if(SignShopConfig.getTransactionLog() && !items.equals("")) {
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
        if(SignShopConfig.getEnableSignStacking()) {
            if(Material.getMaterial("SIGN") != null)
                setItemMaxSize(Material.getMaterial("SIGN"), 64);
            if(Material.getMaterial("SIGN_POST") != null)
                setItemMaxSize(Material.getMaterial("SIGN_POST"), 64);
            if(Material.getMaterial("WALL_SIGN") != null)
                setItemMaxSize(Material.getMaterial("WALL_SIGN"), 64);
        }
    }
    
    @Override
    public void onEnable() {
        // Migrate configs from old directory
        this.checkOldDir();        
        if(!this.getDataFolder().exists()) {
            this.getDataFolder().mkdir();
        }        
        fixStackSize();
        itemUtil.initDiscs();        
        clicks.init();        
        instance = this;
        metricsSetup = new setupMetrics();
        if(metricsSetup.setup(this))
            log("Succesfully started Metrics, see http://mcstats.org for more information.", Level.INFO);
        else
            log("Could not start Metrics, see http://mcstats.org for more information.", Level.INFO);
                
        SignShopConfig = new SignShopConfig();
        SignShopConfig.init();
        BookStore.init();
        
        //Create a storage locker for shops        
        SignShop.Storage = new Storage(new File(this.getDataFolder(),"sellers.yml"));
        
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

        setupVault();                
        
        PluginDescriptionFile pdfFile = this.getDescription();
        PluginManager pm = getServer().getPluginManager();
        pm.registerEvents(loginListener, this);
        
        if(Vault.vaultFound) {
            // Register events
            pm.registerEvents(playerListener, this);
            pm.registerEvents(blockListener, this);            
            if(SignShopConfig.getDisableEssentialsSigns()) {
                SignShopServerListener SListener = new SignShopServerListener(getServer());
                pm.registerEvents(SListener, this);
            }
            log("v" + pdfFile.getVersion() + " Enabled", Level.INFO);
        } else {            
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
        if(args[0].equalsIgnoreCase("reload")) {
            Bukkit.getServer().getPluginManager().disablePlugin(this);
            Bukkit.getServer().getPluginManager().enablePlugin(this);
            SignShop.log("Reloaded", Level.INFO);
            if((sender instanceof Player))
                ((Player)sender).sendMessage(ChatColor.GREEN + "SignShop has been reloaded");
        } else if(args[0].equalsIgnoreCase("stats") || args[0].equalsIgnoreCase("version") || args[0].equalsIgnoreCase("help")) {
            PluginDescriptionFile pdfFile = this.getDescription();
            String message = "Amount of Shops: " + Storage.shopCount() + "\n"
                    + "SignShop version: " + pdfFile.getVersion() + "\n"
                    + "Vault version: " + vault.getVersion() + "\n"
                    + "SignShop Authors: " + pdfFile.getAuthors().toString().replace("[", "").replace("]", "") + "\n"
                    + "SignShop Home: http://tiny.cc/signshop" + "\n";
            if((sender instanceof Player))
                ((Player)sender).sendMessage(ChatColor.GREEN + message);
            else
                SignShop.log(message, Level.INFO);
        } else
            return false;
        return true;
    }
    
    
    public static SignShop getInstance() {
        return instance;
    }
    
    public static String getLogPrefix() {
        PluginDescriptionFile pdfFile = SignShop.getInstance().getDescription();
        String prefix = ChatColor.GOLD + "[SignShop] [" +pdfFile.getVersion() +"]" + ChatColor.RED;
        return prefix;
    }
    
    private void closeHandlers() {
        Handler[] handlers = transactionlogger.getHandlers();
        for(int i = 0; i < handlers.length; i++)
            handlers[i].close();
    }
    
    @Override
    public void onDisable() {
        closeHandlers();
        Storage.SafeSave();
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
    
    public static Logger getMainLogger() {
        return logger;
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