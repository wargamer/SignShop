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
import org.bukkit.event.Event;
import org.wargamer2010.signshop.blocks.BookFactory;
import org.wargamer2010.signshop.blocks.IItemTags;
import org.wargamer2010.signshop.blocks.SignShopBooks;
import org.wargamer2010.signshop.blocks.SignShopItemMeta;
import org.wargamer2010.signshop.commands.CommandDispatcher;
import org.wargamer2010.signshop.configuration.SignShopConfig;
import org.wargamer2010.signshop.listeners.sslisteners.*;
import org.wargamer2010.signshop.metrics.setupMetrics;
import org.wargamer2010.signshop.player.PlayerMetadata;
import org.wargamer2010.signshop.player.SignShopPlayer;
import org.wargamer2010.signshop.timing.TimeManager;
import org.wargamer2010.signshop.util.SSBukkitVersion;
import org.wargamer2010.signshop.util.googleTranslateUtil;
import org.wargamer2010.signshop.util.versionUtil;

public class SignShop extends JavaPlugin{
    private final SignShopPlayerListener playerListener = new SignShopPlayerListener();
    private final SignShopBlockListener blockListener = new SignShopBlockListener();
    private final SignShopLoginListener loginListener = new SignShopLoginListener();
    private static SignShop instance;

    private static final Logger logger = Logger.getLogger("Minecraft");
    private static final Logger transactionlogger = Logger.getLogger("SignShop_Transactions");

    //Statics
    private static Storage store;
    private static TimeManager manager = null;

    //Permissions
    private static boolean USE_PERMISSIONS = false;

    // Vault
    private Vault vault = null;
    private setupMetrics metricsSetup = null;

    //Logging
    public void log(String message, Level level,int tag) {
        if(message != null && !message.trim().isEmpty())
            logger.log(level,("[SignShop] ["+tag+"] " + message));
    }
    public static void log(String message, Level level) {
        if(message != null && !message.trim().isEmpty())
            logger.log(level,("[SignShop] " + message));
    }
    public static void logTransaction(String customer, String owner, String Operation, String items, String Price) {
        if(SignShopConfig.getTransactionLog()) {
            String fixedItems = (items.isEmpty() ? "none" : items);
            String message = ("Customer: " + customer + ", Owner: " + owner + ", Operation: " + Operation + ", Items: " + fixedItems + ", Price: " + Price);
            transactionlogger.log(Level.INFO, message);
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

    private void fixStackSize() {
        IItemTags tags = BookFactory.getItemTags();
        if(SignShopConfig.getEnableSignStacking()) {
            if(Material.getMaterial("SIGN") != null)
                tags.setItemMaxSize(Material.getMaterial("SIGN"), 64);
            if(Material.getMaterial("SIGN_POST") != null)
                tags.setItemMaxSize(Material.getMaterial("SIGN_POST"), 64);
            if(Material.getMaterial("WALL_SIGN") != null)
                tags.setItemMaxSize(Material.getMaterial("WALL_SIGN"), 64);
        }
    }

    private void DisableSignShop() {
        PluginManager pm = Bukkit.getServer().getPluginManager();
        pm.disablePlugin(this);
    }

    @Override
    public void onEnable() {
        if(versionUtil.getBukkitVersionType() == SSBukkitVersion.Unknown) {
            DisableSignShop();
            return;
        }

        // Migrate configs from old directory
        this.checkOldDir();
        if(!this.getDataFolder().exists()) {
            this.getDataFolder().mkdir();
        }
        itemUtil.initDiscs();

        instance = this;
        metricsSetup = new setupMetrics(this);
        if(!metricsSetup.isOptOut()) {
            if(metricsSetup.setup())
                log("Succesfully started Metrics, see http://mcstats.org for more information.", Level.INFO);
            else
                log("Could not start Metrics, see http://mcstats.org for more information.", Level.INFO);
        }

        SignShopConfig.init();
        SignShopBooks.init();
        PlayerMetadata.init();
        SignShopItemMeta.init();
        CommandDispatcher.init();
        fixStackSize();
        googleTranslateUtil.init();

        //Create a storage locker for shops
        store = Storage.init(new File(this.getDataFolder(),"sellers.yml"));
        manager = new TimeManager(new File(this.getDataFolder(), "timing.yml"));

        if(SignShopConfig.getTransactionLog()) {
            try {
                FileHandler fh = new FileHandler("plugins/SignShop/Transaction.log", true);
                TransferFormatter formatter = new TransferFormatter();
                fh.setFormatter(formatter);
                fh.setLevel(Level.FINEST);
                transactionlogger.addHandler(fh);
                transactionlogger.setLevel(Level.INFO);
                transactionlogger.setParent(logger);
                transactionlogger.setUseParentHandlers(false);
            } catch(IOException ex) {
                log("Failed to create transaction log", Level.INFO);
            }
        }

        setupVault();

        PluginDescriptionFile pdfFile = this.getDescription();
        PluginManager pm = getServer().getPluginManager();
        pm.registerEvents(loginListener, this);

        if(Vault.isVaultFound()) {
            // Register events
            pm.registerEvents(playerListener, this);
            pm.registerEvents(blockListener, this);
            pm.registerEvents(new SignShopWorthListener(), this);
            if(SignShopConfig.getDisableEssentialsSigns()) {
                SignShopServerListener SListener = new SignShopServerListener(getServer());
                pm.registerEvents(SListener, this);
            }
            registerSSListeners();
            log("v" + pdfFile.getVersion() + " Enabled", Level.INFO);
        } else {
            DisableSignShop();
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String args[]) {
        String commandName = cmd.getName().toLowerCase();
        if(!commandName.equalsIgnoreCase("signshop"))
            return true;
        SignShopPlayer player = null;
        if(sender instanceof Player)
            player = new SignShopPlayer((Player) sender);
        String[] remainingArgs;
        String subCommandName;
        if(args.length == 0) {
            subCommandName = "";
            remainingArgs = new String[0];
        } else {
            subCommandName = args[0].toLowerCase();
            remainingArgs = new String[args.length-1];
            if(args.length > 1) {
                for(int i = 1; i < args.length; i++)
                    remainingArgs[i-1] = args[i].toLowerCase();
            }
        }
        return CommandDispatcher.handle(subCommandName, remainingArgs, player);
    }


    public static SignShop getInstance() {
        return instance;
    }

    public static void scheduleEvent(Event event) {
        SignShop.getInstance().getServer().getPluginManager().callEvent(event);
    }

    public static String getLogPrefix() {
        PluginDescriptionFile pdfFile = SignShop.getInstance().getDescription();
        String prefix = ChatColor.GOLD + "[SignShop] [" +pdfFile.getVersion() +"]" + ChatColor.RED;
        return prefix;
    }

    private void closeHandlers() {
        if(transactionlogger == null || transactionlogger.getHandlers() == null)
            return;
        Handler[] handlers = transactionlogger.getHandlers();
        for(int i = 0; i < handlers.length; i++)
            handlers[i].close();
    }

    @Override
    public void onDisable() {
        closeHandlers();
        if(store != null)
            store.SafeSave();
        log("Disabled", Level.INFO);
    }

    private void setupVault() {
        vault = new Vault();
        vault.setupChat();
        Boolean vault_Perms = vault.setupPermissions();
        if(!vault_Perms || Vault.getPermission().getName().equals("SuperPerms")) {
            log("Vault's permissions not found, defaulting to OP.", Level.INFO);
            USE_PERMISSIONS = false;
        } else
            USE_PERMISSIONS = true;
        Boolean vault_Economy = vault.setupEconomy();
        if(!vault_Economy)
            log("Could not hook into Vault's Economy!", Level.WARNING);
    }

    private void registerSSListeners() {
        PluginManager pm = getServer().getPluginManager();
        pm.registerEvents(new SimpleBlacklister(), this);
        pm.registerEvents(new SimpleMessenger(), this);
        pm.registerEvents(new SimpleRestricter(), this);
        pm.registerEvents(new SimpleShopLimiter(), this);
        pm.registerEvents(new SimpleShopProtector(), this);
        pm.registerEvents(new SimpleBlockProtector(), this);
        pm.registerEvents(new PermissionChecker(), this);
        pm.registerEvents(new PermitChecker(), this);
        pm.registerEvents(new ShopUpdater(), this);
        pm.registerEvents(new GetPriceFromWorth(), this);
        pm.registerEvents(new ShopCooldown(), this);
        pm.registerEvents(new NotificationsHooker(), this);
        pm.registerEvents(new StockChecker(), this);
        if(SignShopConfig.getEnableDynmapSupport())
            pm.registerEvents(new DynmapManager(), this);
        if(SignShopConfig.getEnableShopPlotSupport()) {
            pm.registerEvents(new WorldGuardChecker(), this);
            pm.registerEvents(new TownyChecker(), this);
        }

        // Money Transactions Types
        pm.registerEvents(new DefaultMoneyTransaction(), this);
        pm.registerEvents(new BankTransaction(), this);
        pm.registerEvents(new SharedMoneyTransaction(), this);
    }

    public static boolean usePermissions() {
        return USE_PERMISSIONS;
    }

    public static TimeManager getTimeManager() {
        return manager;
    }

    private class TransferFormatter extends Formatter {
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