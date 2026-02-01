package org.wargamer2010.signshop;

import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.Event;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.FileUtil;
import org.jetbrains.annotations.NotNull;
import org.wargamer2010.signshop.data.SignShopBooks;
import org.wargamer2010.signshop.data.SignShopItemMeta;
import org.wargamer2010.signshop.commands.*;
import org.wargamer2010.signshop.configuration.ColorUtil;
import org.wargamer2010.signshop.configuration.MetaFormats;
import org.wargamer2010.signshop.configuration.SignShopConfig;
import org.wargamer2010.signshop.data.Storage;
import org.wargamer2010.signshop.configuration.configUtil;
import org.wargamer2010.signshop.listeners.*;
import org.wargamer2010.signshop.listeners.sslisteners.*;
import org.wargamer2010.signshop.money.MoneyModifierManager;
import org.wargamer2010.signshop.player.PlayerMetadata;
import org.wargamer2010.signshop.scheduling.SchedulerAdapter;
import org.wargamer2010.signshop.timing.TimeManager;
import org.wargamer2010.signshop.util.DataConverter;
import org.wargamer2010.signshop.util.SSTimeUtil;
import org.wargamer2010.signshop.util.commandUtil;
import org.wargamer2010.signshop.worth.CMIWorthHandler;
import org.wargamer2010.signshop.worth.EssentialsWorthHandler;
import org.wargamer2010.signshop.worth.WorthHandler;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.*;

/**
 * Main plugin class for SignShop - a Spigot/Bukkit plugin that enables creation of physical shops
 * using signs and chests. Players can buy/sell items, execute commands, control redstone, and more
 * by interacting with signs linked to chests.
 *
 * <p><b>Plugin Lifecycle:</b></p>
 * <ul>
 *   <li>{@link #onLoad()} - Registers WorldGuard flags before WorldGuard initialization</li>
 *   <li>{@link #onEnable()} - Main initialization: config, storage, vault, events, listeners</li>
 *   <li>{@link #onDisable()} - Cleanup: save data, close handlers, stop timers</li>
 * </ul>
 *
 * <p><b>Singleton Pattern:</b></p>
 * Access the plugin instance via {@link #getInstance()} from anywhere in the codebase.
 *
 * <p><b>Core Systems:</b></p>
 * <ul>
 *   <li><b>Configuration:</b> {@link SignShopConfig} - Loads config.yml, operation definitions, sign types</li>
 *   <li><b>Storage:</b> {@link Storage} - Manages all shops in memory and persistence to sellers.yml</li>
 *   <li><b>Vault Integration:</b> {@link Vault} - Economy and permissions via Vault API (hard dependency)</li>
 *   <li><b>Commands:</b> {@link CommandDispatcher} - Routes /signshop subcommands to handlers</li>
 *   <li><b>Timing:</b> {@link TimeManager} - Manages delayed/timed commands and expiration</li>
 * </ul>
 *
 * <p><b>Debugging:</b></p>
 * Enable verbose logging with {@code Debugging: true} in config.yml. Use {@link #debugMessage(String)}
 * for general debug output or {@link #debugClassMessage(String, String)} for class-specific debugging
 * controlled by {@code DebugClasses} config option.
 *
 * <p><b>Transaction Logging:</b></p>
 * When {@code TransactionLog: true}, all shop transactions are logged to Transaction.log with
 * customer, owner, operation, items, and price details via {@link #logTransaction}.
 *
 * @see SignShopConfig
 * @see Storage
 * @see Vault
 * @see CommandDispatcher
 * @see TimeManager
 */
public class SignShop extends JavaPlugin {
    private static final int CONFIG_VERSION_DO_NOT_TOUCH = 4;
    public static final int DATA_VERSION = 4;
    private static final Logger logger = Logger.getLogger("Minecraft");
    private static final Logger transactionlogger = Logger.getLogger("SignShop_Transactions");
    public static WorthHandler worthHandler;
    private static SignShop instance;
    private SignShopConfig signShopConfig;
    //Statics
    private static Storage store;
    private static TimeManager manager = null;
    private static SchedulerAdapter scheduler = null;
    //Permissions
    private static boolean USE_PERMISSIONS = false;
    // Commands
    private static final CommandDispatcher commandDispatcher = new CommandDispatcher();
    private final SignShopPlayerListener playerListener = new SignShopPlayerListener();
    private final SignShopBlockListener blockListener = new SignShopBlockListener();
    private final SignShopLoginListener loginListener = new SignShopLoginListener();
    private final int B_STATS_ID = 6574;

    // Vault
    private Vault vault = null;

    public void reload() {
        signShopConfig = new SignShopConfig();
        ColorUtil.init();
        MetaFormats.init();
    }


    public SignShopConfig getSignShopConfig() {
        return signShopConfig;
    }

    public void debugTiming(String message, long start, long end) {
        if (getSignShopConfig().debugging()) {
            debugMessage(message + " took: " + (end - start) + "ms");
        }
    }

    public void debugMessage(String message) {
        if (getSignShopConfig().debugging()) {
            log(message, Level.INFO);
        }
    }

    public void debugClassMessage(String message, String className) {
        if (getSignShopConfig().debugging() && getSignShopConfig().weAreDebuggingClass(className.toUpperCase())){
            log(className+": "+message, Level.INFO);
        }
    }

    public static void log(String message, Level level) {
        if (message != null && !message.trim().isEmpty())
            logger.log(level, ("[SignShop] " + message));
    }

    public void logTransaction(String customer, String owner, String Operation, String items, String Price) {
        if (getSignShopConfig().getTransactionLog()) {
            String fixedItems = (items.isEmpty() ? "none" : items);
            String message = ("Customer: " + customer + ", Owner: " + owner + ", Operation: " + Operation + ", Items: " + fixedItems + ", Price: " + Price);
            transactionlogger.log(Level.INFO, message);
        }
    }

    public static String getLogPrefix() {
        PluginDescriptionFile pdfFile = SignShop.getInstance().getDescription();
        return ChatColor.GOLD + "[SignShop] [" + pdfFile.getVersion() + "]" + ChatColor.RED;
    }

    public static SignShop getInstance() {
        return instance;
    }

    public static void scheduleEvent(Event event) {
        SignShop.getInstance().getServer().getPluginManager().callEvent(event);
    }

    public static boolean usePermissions() {
        return USE_PERMISSIONS;
    }

    public static TimeManager getTimeManager() {
        return manager;
    }

    public static SchedulerAdapter getScheduler() {
        return scheduler;
    }

    public static CommandDispatcher getCommandDispatcher() {
        return commandDispatcher;
    }

    //Logging
    public void log(String message, Level level, int tag) {
        if (message != null && !message.trim().isEmpty())
            logger.log(level, ("[SignShop] [" + tag + "] " + message));
    }

    private void disableSignShop() {
        PluginManager pm = Bukkit.getServer().getPluginManager();
        pm.disablePlugin(this);
    }
    @Override
    public void onLoad(){
        // This has to be registered before WorldGuard is enabled.
        if (this.getServer().getPluginManager().getPlugin("WorldGuard") != null) {
            WorldGuardChecker.registerWorldGuardFlag(this.getServer());
        }
    }

    @Override
    public void onEnable() {
        if (!this.getDataFolder().exists()) {
            if (!this.getDataFolder().mkdir())
                log("Could not create plugins/SignShop folder.", Level.SEVERE);
        }
        instance = this;
        scheduler = new SchedulerAdapter(this);

        setupCommands();

        // Backup config if it is an old version
        backupOldConfig();
        signShopConfig = new SignShopConfig();

        SignShopBooks.init();
        PlayerMetadata.init();
        SignShopItemMeta.init();
        ColorUtil.init();
        MetaFormats.init();
        MoneyModifierManager.init();

        // Convert legacy player names to UUID
        PlayerMetadata.convertToUuid(this);

        // Convert old data to new format if needed
        DataConverter.init();

        //Create a storage locker for shops
        store = Storage.init(new File(this.getDataFolder(), "sellers.yml"));
        manager = new TimeManager(new File(this.getDataFolder(), "timing.yml"));

        if (getSignShopConfig().allowCommaDecimalSeparator() == SignShopConfig.CommaDecimalSeparatorState.AUTO) {
            // Plugin must decide if the comma separators are enabled
            SignShopConfig.CommaDecimalSeparatorState state;
            if (store.shopCount() == 0) {
                log("Comma decimal seperators have been enabled because this server has 0 shops.", Level.INFO);
                state = SignShopConfig.CommaDecimalSeparatorState.TRUE;
            }
            else {
                log("Comma decimal separators have been disabled because this server has " + store.shopCount() + " shop(s) that may or may not be compatible with the new parser.", Level.INFO);
                state = SignShopConfig.CommaDecimalSeparatorState.FALSE;
            }
            log("This can be changed by modifying 'AllowCommaDecimalSeparator' in the config.", Level.INFO);
            getSignShopConfig().setAllowCommaDecimalSeparator(state);
        }

        if (getSignShopConfig().getTransactionLog()) {
            try {
                FileHandler fh = new FileHandler("plugins/SignShop/Transaction.log", true);
                TransferFormatter formatter = new TransferFormatter();
                fh.setFormatter(formatter);
                fh.setLevel(Level.FINEST);
                transactionlogger.addHandler(fh);
                transactionlogger.setLevel(Level.INFO);
                transactionlogger.setParent(logger);
                transactionlogger.setUseParentHandlers(false);
            } catch (IOException ex) {
                log("Failed to create transaction log", Level.INFO);
            }
        }

        setupVault();

        PluginDescriptionFile pdfFile = this.getDescription();
        PluginManager pm = getServer().getPluginManager();
        pm.registerEvents(loginListener, this);

        if (Vault.isVaultFound()) {
            // Register events
            pm.registerEvents(playerListener, this);
            pm.registerEvents(blockListener, this);

            if (getSignShopConfig().getDisableEssentialsSigns()) {
                SignShopServerListener SListener = new SignShopServerListener(getServer());
                pm.registerEvents(SListener, this);
            }
            registerSSListeners();
        }
        else {
            log("Vault was not found.", Level.WARNING);
            disableSignShop();
        }
        //Setup worth
        if (getSignShopConfig().getEnablePriceFromWorth()) {
            if (Bukkit.getServer().getPluginManager().getPlugin("CMI") != null && Bukkit.getServer().getPluginManager().getPlugin("CMI").isEnabled()) {
                worthHandler = new CMIWorthHandler();
                log("Using worth information from CMI.", Level.INFO);
            }
            else if (Bukkit.getServer().getPluginManager().getPlugin("Essentials") != null && Bukkit.getServer().getPluginManager().getPlugin("Essentials").isEnabled()) {
                worthHandler = new EssentialsWorthHandler();
                log("Using worth information from Essentials.", Level.INFO);
            }
            else {
                log("No compatible worth plugin found, [Worth] disabled.", Level.WARNING);
            }
        }
        //Enable metrics
        if (getSignShopConfig().metricsEnabled()) {
            Metrics metrics = new Metrics(this, B_STATS_ID);
            log("Thank you for enabling metrics!", Level.INFO);
        }
        if (getSignShopConfig().debugging()) {
            SignShop.log("Debugging enabled.", Level.INFO);
            SignShop.log("Debugging Classe(s): "+signShopConfig.getDebugClasses(),Level.INFO);
        }

        debugClassMessage("This is an example class message.",this.getName());

        //Warn if spawn-protection is enabled
        if(Bukkit.getSpawnRadius() > 0 && !Bukkit.getOperators().isEmpty()){
            int opsCount = Bukkit.getOperators().size();
            String opsPhrase = (opsCount > 1) ? ("are " + opsCount + " ops!") : ("is " + opsCount + " op!");
            log("Spawn-Protection is set to " + Bukkit.getSpawnRadius() + " in server.properties and there " + opsPhrase, Level.WARNING);
            log("Non-opped players may not be able to use SignShop signs in spawn!", Level.WARNING);
            log("Please set 'spawn-protection' to 0 and use a protection plugin to protect spawn instead.", Level.WARNING);
        }

        log("v" + pdfFile.getVersion() + " Enabled", Level.INFO);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String commandLabel, @NotNull String[] args) {
        String commandName = cmd.getName().toLowerCase();
        if (!commandName.equalsIgnoreCase("signshop"))
            return true;
        return commandUtil.handleCommand(sender, cmd, commandLabel, args, commandDispatcher);
    }

    private void closeHandlers() {
        if (transactionlogger == null || transactionlogger.getHandlers() == null)
            return;
        Handler[] handlers = transactionlogger.getHandlers();
        for (Handler handler : handlers) handler.close();
    }

    @Override
    public void onDisable() {
        closeHandlers();
        if (store != null)
            store.Save();
        Storage.dispose();
        if (manager != null)
            manager.stop();
        log("Disabled", Level.INFO);
    }

    private void setupVault() {
        vault = new Vault();
        vault.setupChat();
        Boolean vault_Perms = vault.setupPermissions();
        if (!vault_Perms || Vault.getPermission().getName().equals("SuperPerms")) {
            log("Vault's permissions not found, defaulting to OP.", Level.INFO);
            USE_PERMISSIONS = false;
        }
        else
            USE_PERMISSIONS = true;
        Boolean vault_Economy = vault.setupEconomy();
        if (!vault_Economy) {
            getServer().getPluginManager().registerEvents(new ServerLoadedListener(),this);
            log("Could not hook into Vault's Economy! Signshop will retry after server is loaded.", Level.WARNING);
        }
    }

    private void setupCommands() {
        commandDispatcher.registerHandler("stats", StatsHandler.getInstance());
        commandDispatcher.registerHandler("version", StatsHandler.getInstance());
        commandDispatcher.registerHandler("about", StatsHandler.getInstance());
        commandDispatcher.registerHandler("reload", ReloadHandler.getInstance());
        commandDispatcher.registerHandler("tutorial", TutorialHandler.getInstance());
        commandDispatcher.registerHandler("help", HelpHandler.getInstance());
        commandDispatcher.registerHandler("sign", HelpHandler.getInstance());
        commandDispatcher.registerHandler("list", HelpHandler.getInstance());
        commandDispatcher.registerHandler("unlink", UnlinkHandler.getInstance());
        commandDispatcher.registerHandler("", HelpHandler.getInstance());
        commandDispatcher.registerHandler("ignore", IgnoreHandler.getInstance());
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
        pm.registerEvents(new StockChecker(), this);
        pm.registerEvents(new TimedCommandListener(), this);
        pm.registerEvents(new MoneyModifierListener(), this);
        pm.registerEvents(new SignSidesValidator(),this);

        // Dynmap integration (modern API listener pattern for Dynmap 3.0+)
        if (getSignShopConfig().getEnableDynmapSupport() && this.getServer().getPluginManager().isPluginEnabled("Dynmap")) {
            DynmapManager dmm = new DynmapManager();
            pm.registerEvents(dmm, this);
        }

        if (getSignShopConfig().getEnableShopPlotSupport()) {
            if (this.getServer().getPluginManager().isPluginEnabled("WorldGuard")) {
                pm.registerEvents(new WorldGuardChecker(), this);
            }
            pm.registerEvents(new TownyChecker(), this);
        }

        // Money Transactions Types
        pm.registerEvents(new DefaultMoneyTransaction(), this);
        pm.registerEvents(new BankTransaction(), this);
        pm.registerEvents(new SharedMoneyTransaction(), this);
    }

    private void backupOldConfig() {
        FileConfiguration ymlThing = configUtil.loadYMLFromPluginFolder(SignShopConfig.CONFIG_FILENAME);
        File configFile = new File(SignShop.getInstance().getDataFolder(), SignShopConfig.CONFIG_FILENAME);

        if ((ymlThing != null && configFile.exists())
                && (!ymlThing.isSet("ConfigVersionDoNotTouch") || ymlThing.getInt("ConfigVersionDoNotTouch") != CONFIG_VERSION_DO_NOT_TOUCH)) {

            SignShop.log("Old config detected, backing it up before modifiying it.", Level.INFO);
            File configBackup = new File(SignShop.getInstance().getDataFolder(), "configBackup" + SSTimeUtil.getDateTimeStamp() + ".yml");
            FileUtil.copy(configFile, configBackup);
            ymlThing.set("ConfigVersionDoNotTouch", CONFIG_VERSION_DO_NOT_TOUCH);
            SignShopConfig.saveConfig(ymlThing, configFile);
        }
    }

    public Vault getVault() {
        return vault;
    }

    private static class TransferFormatter extends Formatter {
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