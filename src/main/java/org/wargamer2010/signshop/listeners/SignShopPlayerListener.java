package org.wargamer2010.signshop.listeners;

import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.block.sign.Side;
import org.bukkit.block.sign.SignSide;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.wargamer2010.signshop.Seller;
import org.wargamer2010.signshop.SignShop;
import org.wargamer2010.signshop.data.Storage;
import org.wargamer2010.signshop.events.*;
import org.wargamer2010.signshop.operations.SignShopArguments;
import org.wargamer2010.signshop.operations.SignShopArgumentsType;
import org.wargamer2010.signshop.operations.SignShopOperationListItem;
import org.wargamer2010.signshop.player.PlayerCache;
import org.wargamer2010.signshop.player.SignShopPlayer;
import org.wargamer2010.signshop.specialops.SignShopSpecialOp;
import org.wargamer2010.signshop.util.clicks;
import org.wargamer2010.signshop.util.economyUtil;
import org.wargamer2010.signshop.util.itemUtil;
import org.wargamer2010.signshop.util.signshopUtil;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Primary event listener for all player interactions with SignShop signs.
 *
 * <p>This class is the heart of SignShop's user interaction system. It handles:</p>
 * <ul>
 *   <li><b>Shop Creation</b>: Left-clicking a sign with the link material (default: redstone dust)
 *       while having blocks selected triggers shop setup</li>
 *   <li><b>Shop Usage</b>: Right-clicking executes transactions, left-clicking shows confirmation/info</li>
 *   <li><b>Sign Customization</b>: Dyeing signs, applying glow ink, waxing with honeycomb</li>
 *   <li><b>Shop Inspection</b>: Using the inspection material (default: stick) to view shop details</li>
 *   <li><b>Special Operations</b>: CopySign, ChangeOwner, LinkAdditionalBlocks, etc.</li>
 *   <li><b>Player Selection</b>: Hitting players with the link material to select them for operations</li>
 * </ul>
 *
 * <h2>Interaction Flow</h2>
 * <pre>
 * PlayerInteractEvent
 *     │
 *     ├─→ Shop Creation (left-click sign + link material + no existing shop)
 *     │       │
 *     │       ├─→ Parse operation from sign text
 *     │       ├─→ Get linked blocks from clicks map
 *     │       ├─→ Run setupOperation() on each operation
 *     │       ├─→ Fire SSCreatedEvent
 *     │       └─→ Store in Storage.addSeller()
 *     │
 *     ├─→ Shop Transaction (click existing shop without OP material)
 *     │       │
 *     │       ├─→ Load chunks for shop's blocks
 *     │       ├─→ Run checkRequirements() on each operation
 *     │       ├─→ Fire SSPreTransactionEvent
 *     │       ├─→ Left-click: Show confirmation message and return
 *     │       ├─→ Right-click: Run runOperation() on each operation
 *     │       ├─→ Fire SSPostTransactionEvent
 *     │       └─→ Log transaction
 *     │
 *     ├─→ Sign Customization (right-click shop + dye/ink/honeycomb/brush)
 *     │       └─→ Apply visual changes if player has permission
 *     │
 *     ├─→ Shop Inspection (right-click shop + inspection material)
 *     │       └─→ Display shop info if player has permission
 *     │
 *     └─→ Special Operations (click with link material)
 *             └─→ Delegate to SignShopSpecialOp implementations
 * </pre>
 *
 * <h2>Click Map System</h2>
 * <p>The {@link clicks} utility class maintains a map of blocks that players have selected
 * by left-clicking with the link material. When creating a shop, these selected blocks
 * become the shop's containables (chests) and activatables (levers, etc.).</p>
 *
 * @see SignShopArguments The context object passed through the operation pipeline
 * @see SignShopOperationListItem Individual operations that make up a shop type
 * @see Storage Where shops are persisted
 * @see clicks Block selection tracking
 */
public class SignShopPlayerListener implements Listener {
    /** Prefix for player metadata tracking which shop types they've seen help for */
    private static final String helpPrefix = "help_";
    /** Metadata key indicating player has dismissed all help messages */
    private static final String anyHelp = "help_anyhelp";

    /**
     * Attempts to run special operations (CopySign, ChangeOwner, etc.) based on player's clicked blocks.
     *
     * <p>Special operations are administrative actions that modify existing shops rather than
     * creating new ones or performing transactions. They are triggered when a player has
     * selected blocks and clicks on a shop sign with the link material.</p>
     *
     * <p>Examples of special operations:</p>
     * <ul>
     *   <li>{@code CopySign} - Copies a shop's configuration to a new sign</li>
     *   <li>{@code ChangeOwner} - Transfers shop ownership to another player</li>
     *   <li>{@code LinkAdditionalBlocks} - Adds more chests/blocks to an existing shop</li>
     *   <li>{@code ChangeShopItems} - Updates the items in a shop using ink sac</li>
     * </ul>
     *
     * @param event The player interact event that triggered this check
     * @return {@code true} if any special operation was executed, {@code false} otherwise
     * @see SignShopSpecialOp The interface all special operations implement
     */
    private Boolean runSpecialOperations(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        // Get all blocks this player has selected by left-clicking with link material
        Set<Location> lClicked = signshopUtil.getKeysByValue(clicks.mClicksPerLocation, player);
        Boolean ranSomething = false;

        List<SignShopSpecialOp> specialops = signshopUtil.getSignShopSpecialOps();
        List<Block> clickedBlocks = new LinkedList<>();
        for (Location lTemp : lClicked)
            clickedBlocks.add(player.getWorld().getBlockAt(lTemp));
        if (!specialops.isEmpty()) {
            // Try each special operation until one succeeds
            for (SignShopSpecialOp special : specialops) {
                ranSomething = (special.runOperation(clickedBlocks, event, ranSomething) || ranSomething);
                if (ranSomething) {
                    break;
                }
            }
            // Clear the click map after a successful special operation
            if (ranSomething)
                clicks.removePlayerFromClickmap(player);
        }

        return ranSomething;
    }

    /**
     * Prevents ghost shops when a sign is replaced by placing a new block.
     *
     * <p>This fixes a bug where placing a block at a shop sign's location would leave
     * orphaned shop data in storage. The fix removes any existing shop at the location
     * when a new block is placed there.</p>
     *
     * <p>Credit: Cat7373 - <a href="https://github.com/wargamer/SignShop/issues/15">Issue #15</a></p>
     *
     * @param event The block place event
     */
    @EventHandler(priority = EventPriority.LOWEST)
    public void SSBugFix(BlockPlaceEvent event) {
        if (event.isCancelled())
            return;
        Block block = event.getBlock();

        // If a sign is being placed where a shop exists, remove the old shop data
        if (itemUtil.clickedSign(block)) {
            Location location = block.getLocation();

            if (Storage.get().getSeller(location) != null) {
                Storage.get().removeSeller(location);
            }
        }
    }

    /**
     * Optionally blocks villager trading when configured.
     *
     * <p>Some server administrators want to disable vanilla villager trading to force
     * players to use SignShop for all trading. This is controlled by the
     * {@code PreventVillagerTrade} config option.</p>
     *
     * @param event The entity interaction event
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerVillagerTrade(PlayerInteractEntityEvent event) {
        if (event.getPlayer() == null || event.getRightClicked() == null)
            return;
        Entity ent = event.getRightClicked();
        SignShopPlayer ssPlayer = PlayerCache.getPlayer(event.getPlayer());
        if (SignShop.getInstance().getSignShopConfig().getPreventVillagerTrade() && ent.getType() == EntityType.VILLAGER) {
            if (!event.isCancelled()) {
                ssPlayer.sendMessage(SignShop.getInstance().getSignShopConfig().getError("villager_trading_disabled", null));
                event.setCancelled(true);
            }
        }
    }

    /**
     * Handles player selection for operations that target other players.
     *
     * <p>Some shop types (like ChangeOwner) need to reference another player. This handler
     * allows players to "select" other players by hitting them with the link material
     * (default: redstone dust). The selected player is stored in the clicks map and can
     * be used by subsequent operations.</p>
     *
     * <p>Hitting the same player again deselects them (toggle behavior).</p>
     *
     * @param event The entity damage event
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (event.getDamager().getType() != EntityType.PLAYER)
            return;

        Player player = (Player) event.getDamager();

        // Only process if player is holding an OP material (link material)
        if (player.getInventory().getItemInMainHand() == null || !SignShop.getInstance().getSignShopConfig().isOPMaterial(player.getInventory().getItemInMainHand().getType()))
            return;
        if (event.getEntity().getType() == EntityType.PLAYER) {
            SignShopPlayer ssPlayer = PlayerCache.getPlayer(player);
            SignShopPlayer clickedPlayer = PlayerCache.getPlayer((Player) event.getEntity());

            // Toggle selection - deselect if already selected, otherwise select
            if (clicks.mClicksPerPlayerId.containsKey(clickedPlayer.GetIdentifier())) {
                ssPlayer.sendMessage("You have deselected a player with name: " + clickedPlayer.getName());
                clicks.mClicksPerPlayerId.remove(clickedPlayer.GetIdentifier());
            }
            else {
                ssPlayer.sendMessage("You hit a player with name: " + clickedPlayer.getName());
                clicks.mClicksPerPlayerId.put(clickedPlayer.GetIdentifier(), player);
            }
            // Cancel damage - this is just for selection, not combat
            event.setCancelled(true);
        }
    }

    /**
     * Shows tutorial messages when players write valid shop sign types.
     *
     * <p>When a player writes text on a sign that matches a valid SignShop operation
     * (like "[Buy]" or "[Sell]"), this handler displays a tutorial message explaining
     * how to set up that shop type. Tutorial messages are only shown once per shop type
     * per player (tracked in player metadata).</p>
     *
     * <p>The tutorial system can be disabled via the {@code EnableTutorialMessages} config option.</p>
     *
     * @param event The sign change event
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerSignChange(SignChangeEvent event) {
        if (event.getPlayer() == null || !itemUtil.clickedSign(event.getBlock()))
            return;
        String[] oldLines = ((Sign) event.getBlock().getState()).getSide(Side.FRONT).getLines();
        // Prevent the message from being shown when the top line remains the same
        // (e.g., when editing other lines of an existing sign)
        if (oldLines[0].equals(event.getLine(0)))
            return;

        String[] sLines = event.getLines();
        String sOperation = signshopUtil.getOperation(sLines[0]);
        // Only show tutorial for valid shop operations
        if (SignShop.getInstance().getSignShopConfig().getBlocks(sOperation).isEmpty())
            return;

        List<String> operation = SignShop.getInstance().getSignShopConfig().getBlocks(sOperation);
        if (signshopUtil.getSignShopOps(operation) == null)
            return;

        SignShopPlayer ssPlayer = PlayerCache.getPlayer(event.getPlayer());
        if (SignShop.getInstance().getSignShopConfig().getEnableTutorialMessages()) {
            // Only show tutorial if player hasn't seen it for this operation type
            // and hasn't dismissed all tutorials (anyHelp flag)
            if (!ssPlayer.hasMeta(helpPrefix + sOperation.toLowerCase()) && !ssPlayer.hasMeta(anyHelp)) {
                ssPlayer.setMeta(helpPrefix + sOperation.toLowerCase(), "1");
                String[] args = new String[]{
                        sOperation
                };
                SignShop.getCommandDispatcher().handle("sign", args, ssPlayer);
            }
        }
    }

    /**
     * Resets player state when they leave the server.
     *
     * <p>Clears the "ignore messages" flag which may have been set during certain
     * operations to prevent message spam.</p>
     *
     * @param event The player quit event
     */
    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent event) {
        SignShopPlayer signShopPlayer = PlayerCache.getPlayer(event.getPlayer());
        signShopPlayer.setIgnoreMessages(false);
    }

    /**
     * Main handler for all player block interactions - the core of SignShop's functionality.
     *
     * <p>This method handles multiple distinct interaction types based on context:</p>
     *
     * <h3>1. Shop Creation (Left-click + Link Material + No Existing Shop)</h3>
     * <ol>
     *   <li>Parse operation type from sign's first line (e.g., "[Buy]")</li>
     *   <li>Gather linked blocks from the player's click map</li>
     *   <li>Run {@code setupOperation()} on each operation in the chain</li>
     *   <li>Fire {@link SSCreatedEvent} for listeners to validate/modify</li>
     *   <li>Store the new shop via {@link Storage#addSeller}</li>
     * </ol>
     *
     * <h3>2. Shop Transaction (Click Existing Shop Without OP Material)</h3>
     * <ol>
     *   <li>Load chunks containing the shop's linked blocks</li>
     *   <li>Run {@code checkRequirements()} on each operation</li>
     *   <li>Fire {@link SSPreTransactionEvent}</li>
     *   <li><b>Left-click</b>: Show confirmation message and return</li>
     *   <li><b>Right-click</b>: Run {@code runOperation()} on each operation</li>
     *   <li>Fire {@link SSPostTransactionEvent}</li>
     *   <li>Log the transaction</li>
     * </ol>
     *
     * <h3>3. Shop Inspection (Right-click + Inspection Material)</h3>
     * <p>Display shop information if player has permission.</p>
     *
     * <h3>4. Sign Customization (Right-click + Dye/Ink/Honeycomb/Brush)</h3>
     * <p>Apply visual changes to the sign (color, glow, wax).</p>
     *
     * <h3>5. Special Operations (Click + Link Material + Existing Shop)</h3>
     * <p>Delegate to special operation handlers (CopySign, ChangeOwner, etc.).</p>
     *
     * <h3>6. Block Selection (Left-click + Link Material + Non-Sign)</h3>
     * <p>Register the block in the player's click map for future shop creation.</p>
     *
     * @param event The player interact event
     * @see SignShopArguments Context object passed through the operation pipeline
     * @see SSPreTransactionEvent Fired before transaction execution
     * @see SSPostTransactionEvent Fired after successful transaction
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerInteract(PlayerInteractEvent event) {
        // Respect protection plugins - if another plugin denied the interaction, don't process
        if (event.getClickedBlock() == null
                || event.useInteractedBlock() == Event.Result.DENY
                || event.getPlayer() == null) {
            return;
        }
        // Initialize needed variables
        Block bClicked = event.getClickedBlock();
        Player player = event.getPlayer();
        String[] sLines;
        String sOperation;
        World world = player.getWorld();
        Seller seller = Storage.get().getSeller(event.getClickedBlock().getLocation());

        //Cancel all right clicks on shops because of 1.20 sign edit feature. Uncancel below if needed.
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK && seller != null) {
            event.setCancelled(true);
        }


        if (event.getAction() == Action.LEFT_CLICK_BLOCK && event.getItem() != null && seller == null && SignShop.getInstance().getSignShopConfig().isOPMaterial(event.getItem().getType())) {
            if (itemUtil.clickedSign(bClicked) && event.getItem().getType() == SignShop.getInstance().getSignShopConfig().getLinkMaterial()) {
                sLines = ((Sign) bClicked.getState()).getSide(Side.FRONT).getLines();
                sOperation = signshopUtil.getOperation(sLines[0]);
                SignShopPlayer ssPlayer = PlayerCache.getPlayer(player);

                // Get compiled operations from cache (faster than getBlocks + getSignShopOps)
                List<SignShopOperationListItem> SignShopOperations = SignShop.getInstance().getSignShopConfig().getCompiledOperations(sOperation);
                if (SignShopOperations == null) {
                    if (!runSpecialOperations(event) && !signshopUtil.registerClickedMaterial(event))
                        ssPlayer.sendMessage(SignShop.getInstance().getSignShopConfig().getError("invalid_operation", null));
                    return;
                }

                event.setCancelled(true);
                signshopUtil.fixCreativeModeSignRendering(event.getClickedBlock(), event.getPlayer());
                List<Block> containables = new LinkedList<>();
                List<Block> activatables = new LinkedList<>();
                boolean wentOK = signshopUtil.getSignshopBlocksFromList(ssPlayer, containables, activatables, bClicked);
                if (!wentOK) {
                    return;
                }
                SignShopArguments ssArgs = new SignShopArguments(economyUtil.parsePrice(sLines[3]), null, containables, activatables,
                        ssPlayer, ssPlayer, bClicked, sOperation, event.getBlockFace(), event.getAction(), SignShopArgumentsType.Setup);
                Boolean bSetupOK = false;

                for (SignShopOperationListItem ssOperation : SignShopOperations) {
                    ssArgs.setOperationParameters(ssOperation.getParameters());
                    bSetupOK = ssOperation.getOperation().setupOperation(ssArgs);
                    if (!bSetupOK)
                        return;
                }
                if (!bSetupOK)
                    return;

                if (signshopUtil.cantGetPriceFromMoneyEvent(ssArgs))
                    return;

                SSCreatedEvent createdevent = SSEventFactory.generateCreatedEvent(ssArgs);
                SignShop.scheduleEvent(createdevent);
                if (createdevent.isCancelled()) {
                    itemUtil.setSignStatus(bClicked, ChatColor.BLACK);
                    signshopUtil.fixCreativeModeSignRendering(event.getClickedBlock(), event.getPlayer());
                    return;
                }

                Storage.get().addSeller(ssPlayer.GetIdentifier(), world.getName(), ssArgs.getSign().get(), ssArgs.getContainables().getRoot(), ssArgs.getActivatables().getRoot()
                        , ssArgs.getItems().get(), createdevent.getMiscSettings());
                if (!ssArgs.bDoNotClearClickmap)
                    clicks.removePlayerFromClickmap(player);

                signshopUtil.fixCreativeModeSignRendering(event.getClickedBlock(), event.getPlayer());
                return;
            }
            signshopUtil.registerClickedMaterial(event);
            signshopUtil.fixCreativeModeSignRendering(event.getClickedBlock(), event.getPlayer());
        }
        else if (event.getAction() == Action.RIGHT_CLICK_BLOCK && seller != null && SignShop.getInstance().getSignShopConfig().isInspectionMaterial(event.getItem())) {
            SignShopPlayer signShopPlayer = PlayerCache.getPlayer(event.getPlayer());
            if (playerCanInspect(seller, signShopPlayer)) {
                signShopPlayer.sendMessage(seller.getInfo());
            }
            else {
                signShopPlayer.sendMessage(SignShop.getInstance().getSignShopConfig().getError("no_permission_to_inspect_shop", null));
            }

        }
        // Handle dyes and glow effect
        else if (event.getAction() == Action.RIGHT_CLICK_BLOCK && seller != null && itemIsInkOrDyeRelated(event.getItem())) {
            SignShopPlayer signShopPlayer = PlayerCache.getPlayer(event.getPlayer());
            if (playerCanDyeShop(seller, signShopPlayer)) {
                BlockState blockState = bClicked.getState();
                ItemStack item = event.getItem();
                boolean alreadyApplied = false;
                if (blockState instanceof Sign) {
                    Sign sign = (Sign) blockState;
                    boolean itemIsDye = item.getType().name().contains("DYE");
                    boolean itemIsHoneyComb = item.getType().equals(Material.HONEYCOMB);
                    boolean itemIsInkSac = item.getType().equals(Material.INK_SAC);
                    boolean itemIsGlowInkSac = item.getType().equals(Material.GLOW_INK_SAC);
                    boolean itemIsBrush = item.getType().equals(Material.BRUSH);
                    boolean takeItem = false;
                    SignSide signFront = sign.getSide(Side.FRONT);
                    SignSide signBack = sign.getSide(Side.BACK);
                    Sound sound = null;

                    if (itemIsGlowInkSac) {
                        if (!sign.isWaxed() && (!signFront.isGlowingText() || !signBack.isGlowingText())) {
                            signFront.setGlowingText(true);
                            signBack.setGlowingText(true);
                            sound = Sound.ITEM_GLOW_INK_SAC_USE;
                            takeItem = true;
                        }
                        else {
                            player.playSound(player, Sound.BLOCK_SIGN_WAXED_INTERACT_FAIL, SoundCategory.MASTER, 1, 1);
                        }
                    }
                    if (itemIsInkSac) {
                        if (!sign.isWaxed() && (signFront.isGlowingText() || signBack.isGlowingText())) {
                            signFront.setGlowingText(false);
                            signBack.setGlowingText(false);
                            sound = Sound.ITEM_INK_SAC_USE;
                            takeItem = true;
                        }
                        else {
                            player.playSound(player, Sound.BLOCK_SIGN_WAXED_INTERACT_FAIL, SoundCategory.MASTER, 1, 1);
                        }
                    }
                    if (itemIsDye) {
                        DyeColor dyeColor = DyeColor.valueOf(item.getType().name().toUpperCase().replace("_DYE", ""));
                        if (!sign.isWaxed() && (signFront.getColor() != dyeColor || signBack.getColor() != dyeColor)) {
                            signFront.setColor(dyeColor);
                            signBack.setColor(dyeColor);
                            sound = Sound.ITEM_DYE_USE;
                            takeItem = true;
                        }
                        else {
                            player.playSound(player, Sound.BLOCK_SIGN_WAXED_INTERACT_FAIL, SoundCategory.MASTER, 1, 1);
                        }
                    }
                    if (itemIsHoneyComb) {
                        if (!sign.isWaxed()) {
                            event.setCancelled(false);
                        }
                    }
                    if (itemIsBrush) {
                        sign.setWaxed(false);
                        player.playSound(player, Sound.ITEM_BRUSH_BRUSHING_GENERIC, SoundCategory.MASTER, 1, 1);
                    }

                    if (takeItem) {
                        if (player.getGameMode().equals(GameMode.SURVIVAL))
                            event.getItem().setAmount(event.getItem().getAmount() - 1);
                        player.playSound(player, sound, SoundCategory.MASTER, 1, 1);
                    }
                    sign.update(true, false);
                }
            }
            else {
                event.setCancelled(true);
                signShopPlayer.sendMessage(SignShop.getInstance().getSignShopConfig().getError("no_permission_to_dye_shop", null));
            }
            return;

        }
        else if (itemUtil.clickedSign(bClicked) && seller != null && (event.getItem() == null || !SignShop.getInstance().getSignShopConfig().isOPMaterial(event.getItem().getType()))) {
            SignShopPlayer ssOwner = seller.getOwner();
            sLines = ((Sign) bClicked.getState()).getSide(Side.FRONT).getLines();
            sOperation = signshopUtil.getOperation(sLines[0]);

            // Get compiled operations from cache (faster than getBlocks + getSignShopOps)
            List<SignShopOperationListItem> SignShopOperations = SignShop.getInstance().getSignShopConfig().getCompiledOperations(sOperation);
            SignShopPlayer ssPlayer = PlayerCache.getPlayer(player);
            if (SignShopOperations == null) {
                ssPlayer.sendMessage(SignShop.getInstance().getSignShopConfig().getError("invalid_operation", null));
                return;
            }

            for (Block bContainable : seller.getContainables())
                itemUtil.loadChunkByBlock(bContainable);
            for (Block bActivatable : seller.getActivatables())
                itemUtil.loadChunkByBlock(bActivatable);
            if (event.getAction() == Action.RIGHT_CLICK_BLOCK && event.getItem() != null) {
                event.setCancelled(true);
            }
            SignShopArguments ssArgs = new SignShopArguments(economyUtil.parsePrice(sLines[3]), seller.getItems(), seller.getContainables(), seller.getActivatables(),
                    ssPlayer, ssOwner, bClicked, sOperation, event.getBlockFace(), event.getAction(), SignShopArgumentsType.Check);

            if (seller.getRawMisc() != null)
                ssArgs.miscSettings = seller.getRawMisc();
            ssArgs.setSeller(seller);  // Set seller reference for cached item access
            boolean bRequirementsOK = true;
            boolean bReqOKSolid = true;
            boolean bRunOK = false;

            // If left-clicking, all blocks should get a chance to run checkRequirements
            for (SignShopOperationListItem ssOperation : SignShopOperations) {
                ssArgs.setOperationParameters(ssOperation.getParameters());
                bRequirementsOK = ssOperation.getOperation().checkRequirements(ssArgs, true);
                if (!ssArgs.isLeftClicking() && !bRequirementsOK)
                    break;
                else if (!bRequirementsOK)
                    bReqOKSolid = false;
            }

            if (!bReqOKSolid)
                bRequirementsOK = false;
            SSPreTransactionEvent pretransactevent = SSEventFactory.generatePreTransactionEvent(ssArgs, seller, event.getAction(), bRequirementsOK);
            SignShop.scheduleEvent(pretransactevent);
            // Skip the requirements check if we're left-clicking
            // The confirmation message should always be shown when left-clicking
            if (!ssArgs.isLeftClicking() && (!bRequirementsOK || pretransactevent.isCancelled()))
                return;
            ssArgs.setArgumentType(SignShopArgumentsType.Run);
            ssArgs.getPrice().set(pretransactevent.getPrice());
            if (ssArgs.isLeftClicking()) {
                // Use component-based message for rich hover tooltips on items
                BaseComponent confirmMessage = SignShop.getInstance().getSignShopConfig()
                        .getMessageAsComponent("confirm", ssArgs.getOperation().get(), ssArgs.getMessageParts());
                ssPlayer.sendMessage(confirmMessage);
                ssArgs.reset();
                return;
            }
            ssArgs.reset();
            for (SignShopOperationListItem ssOperation : SignShopOperations) {
                ssArgs.setOperationParameters(ssOperation.getParameters());
                bRunOK = ssOperation.getOperation().runOperation(ssArgs);

                if (!bRunOK)
                    return;
            }
            if (!bRunOK)
                return;

            SSPostTransactionEvent posttransactevent = SSEventFactory.generatePostTransactionEvent(ssArgs, seller, event.getAction());
            SignShop.scheduleEvent(posttransactevent);
            if (posttransactevent.isCancelled())
                return;
            if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                //noinspection UnstableApiUsage
                player.updateInventory();
            }
            List<String> chests = new LinkedList<>();
            for (Map.Entry<String, Object> entry : ssArgs.getMessageParts().entrySet())
                if (entry.getKey().contains("chest"))
                    chests.add(entry.getValue().toString());
            String[] sChests = new String[chests.size()];
            chests.toArray(sChests);
            String items = (!ssArgs.hasMessagePart("!items") ? signshopUtil.implode(sChests, " and ") : ssArgs.getMessagePart("!items"));
            SignShop.getInstance().logTransaction(player.getName(), seller.getOwner().getName(), sOperation, items, economyUtil.formatMoney(ssArgs.getPrice().get()));

            event.setCancelled(true);
            return;
        }
        if (event.getItem() != null && seller != null && SignShop.getInstance().getSignShopConfig().isOPMaterial(event.getItem().getType())) {
            if (!runSpecialOperations(event)) {
                signshopUtil.registerClickedMaterial(event);
            }
        }
        List<Seller> touchedShops = Storage.get().getShopsByBlock(bClicked);
        if (!touchedShops.isEmpty()) {
            SignShopPlayer ssPlayer = PlayerCache.getPlayer(player);
            for (Seller shop : touchedShops) {
                SSTouchShopEvent touchevent = new SSTouchShopEvent(ssPlayer, shop, event.getAction(), bClicked);
                SignShop.scheduleEvent(touchevent);
                if (touchevent.isCancelled()) {
                    event.setCancelled(true);
                    signshopUtil.fixCreativeModeSignRendering(event.getClickedBlock(), event.getPlayer());
                    break;
                }
            }
        }
    }

    /**
     * Checks if a player has permission to inspect (view details of) a shop.
     *
     * <p>Permission is granted if any of these conditions are true:</p>
     * <ul>
     *   <li>Player owns the shop AND has {@code Signshop.Inspect.Own} permission</li>
     *   <li>Player is a server operator</li>
     *   <li>Player has {@code Signshop.Inspect.Others} permission</li>
     * </ul>
     *
     * @param seller The shop to inspect
     * @param signShopPlayer The player attempting to inspect
     * @return {@code true} if the player can inspect the shop
     */
    private boolean playerCanInspect(Seller seller, SignShopPlayer signShopPlayer) {
        return ((signShopPlayer.isOwner(seller) && signShopPlayer.hasPerm("Signshop.Inspect.Own", false))
                || signShopPlayer.isOp() || signShopPlayer.hasPerm("Signshop.Inspect.Others", true));
    }

    /**
     * Checks if a player has permission to dye/customize a shop sign.
     *
     * <p>Permission is granted if any of these conditions are true:</p>
     * <ul>
     *   <li>Player owns the shop AND has {@code Signshop.Dye.Own} permission</li>
     *   <li>Player is a server operator</li>
     *   <li>Player has {@code Signshop.Dye.Others} permission</li>
     * </ul>
     *
     * @param seller The shop to customize
     * @param signShopPlayer The player attempting to dye
     * @return {@code true} if the player can dye the shop sign
     */
    private boolean playerCanDyeShop(Seller seller, SignShopPlayer signShopPlayer) {
        return ((signShopPlayer.isOwner(seller) && signShopPlayer.hasPerm("Signshop.Dye.Own", false))
                || signShopPlayer.isOp() || signShopPlayer.hasPerm("Signshop.Dye.Others", true));

    }

    /**
     * Checks if an item is used for sign customization (dyes, ink sacs, honeycomb, brush).
     *
     * <p>These items have special handling when right-clicking shop signs:</p>
     * <ul>
     *   <li><b>Dyes</b>: Change sign text color</li>
     *   <li><b>Glow Ink Sac</b>: Make sign text glow</li>
     *   <li><b>Ink Sac</b>: Remove glow effect from sign text</li>
     *   <li><b>Honeycomb</b>: Wax the sign (prevent further edits)</li>
     *   <li><b>Brush</b>: Remove wax from sign</li>
     * </ul>
     *
     * <p>Note: Link materials and inspection materials are excluded even if they
     * happen to be dye-related items.</p>
     *
     * @param item The item to check
     * @return {@code true} if the item is used for sign customization
     */
    private boolean itemIsInkOrDyeRelated(ItemStack item) {
        if (item == null || SignShop.getInstance().getSignShopConfig().isLinkMaterial(item.getType()) || SignShop.getInstance().getSignShopConfig().isInspectionMaterial(item))
            return false;
        String materialName = item.getType().name();

        switch (materialName) {
            case "BRUSH":
            case "HONEYCOMB":
            case "BLACK_DYE":
            case "BLUE_DYE":
            case "BROWN_DYE":
            case "CYAN_DYE":
            case "GRAY_DYE":
            case "GREEN_DYE":
            case "LIGHT_BLUE_DYE":
            case "LIGHT_GRAY_DYE":
            case "LIME_DYE":
            case "MAGENTA_DYE":
            case "ORANGE_DYE":
            case "PINK_DYE":
            case "PURPLE_DYE":
            case "RED_DYE":
            case "WHITE_DYE":
            case "YELLOW_DYE":
            case "GLOW_INK_SAC":
            case "INK_SAC":
                return true;
            default:
                return false;

        }
    }

}