# SignShop Code Flow

Detailed workflow documentation for SignShop's core operations.

## Table of Contents
1. [Shop Creation Flow](#1-shop-creation-flow)
2. [Transaction Flow](#2-transaction-flow)
3. [Serialization Flow](#3-serialization-flow)
4. [Configuration Loading](#4-configuration-loading)
5. [Event System](#5-event-system)

---

## 1. Shop Creation Flow

### Overview
Players create shops by:
1. Left-clicking blocks with redstone dust to select them
2. Writing a sign with the shop type (e.g., `[Buy]`)
3. Left-clicking the sign with redstone to link

### Step-by-Step

#### Step 1: Block Selection
**File:** `SignShopPlayerListener.onPlayerInteract()` (line ~344)

```
PlayerInteractEvent
  → Left-click with redstone dust
  → signshopUtil.registerClickedMaterial(event)
  → clicks.mClicksPerLocation.put(location, player)
```

#### Step 2: Sign Writing
**File:** `SignShopPlayerListener.onSignChangeEvent()` (line ~254)

```
SignChangeEvent
  → signshopUtil.getOperation(sLines[0])
  → Validate operation exists
  → Show tutorial (if first time)
```

#### Step 3: Sign Linking
**File:** `SignShopPlayerListener.onPlayerInteract()` (line ~366-418)

```java
// 1. Get operation from sign
sOperation = signshopUtil.getOperation(sLines[0]);
List<SignShopOperationListItem> ops = getCompiledOperations(sOperation);

// 2. Gather linked blocks from click map
signshopUtil.getSignshopBlocksFromList(ssPlayer, containables, activatables, bClicked);

// 3. Create context
SignShopArguments ssArgs = new SignShopArguments(
    price, null, containables, activatables,
    ssPlayer, ssPlayer, bClicked, sOperation,
    blockFace, action, SignShopArgumentsType.Setup
);

// 4. Run setup operations
for (SignShopOperationListItem op : ops) {
    ssArgs.setOperationParameters(op.getParameters());
    if (!op.getOperation().setupOperation(ssArgs))
        return; // Abort on failure
}

// 5. Fire creation event
SSCreatedEvent event = SSEventFactory.generateCreatedEvent(ssArgs);
SignShop.scheduleEvent(event);
if (event.isCancelled()) return;

// 6. Persist
Storage.get().addSeller(owner, world, sign, containables, activatables, items, misc);

// 7. Cleanup
clicks.removePlayerFromClickmap(player);
```

---

## 2. Transaction Flow

### Overview
Right-click shop sign → Check requirements → Execute operations → Log transaction

### Phases

#### Phase 1: Load Shop
**File:** `SignShopPlayerListener.onPlayerInteract()` (line ~511)

```java
Seller seller = Storage.get().getSeller(clickedBlock.getLocation());
if (seller == null) return;

// Load chunks for multi-chunk shops
for (Block b : seller.getContainables())
    itemUtil.loadChunkByBlock(b);
```

#### Phase 2: Create Context
```java
SignShopArguments ssArgs = new SignShopArguments(
    price, seller.getItems(),
    seller.getContainables(), seller.getActivatables(),
    ssPlayer, ssOwner, bClicked, sOperation,
    blockFace, Action.RIGHT_CLICK, SignShopArgumentsType.Check
);
ssArgs.setSeller(seller);  // For cached item access
```

#### Phase 3: Check Requirements
```java
for (SignShopOperationListItem op : ops) {
    ssArgs.setOperationParameters(op.getParameters());
    bRequirementsOK = op.getOperation().checkRequirements(ssArgs, true);

    if (!ssArgs.isLeftClicking() && !bRequirementsOK)
        break;  // Right-click: abort on first failure
}
```

**Example checks for [Buy]:**
- `takePlayerMoney.checkRequirements()` → Check player has money
- `giveShopItems.checkRequirements()` → Check shop has stock

#### Phase 4: Pre-Transaction Event
```java
SSPreTransactionEvent event = SSEventFactory.generatePreTransactionEvent(
    ssArgs, seller, action, bRequirementsOK
);
SignShop.scheduleEvent(event);

if (event.isCancelled()) return;

// Price modifiers can change price here
ssArgs.getPrice().set(event.getPrice());
```

#### Phase 5: Left-Click Confirmation
```java
if (ssArgs.isLeftClicking()) {
    BaseComponent msg = config.getMessageAsComponent("confirm", operation, messageParts);
    ssPlayer.sendMessage(msg);
    return;  // Don't execute, just show confirmation
}
```

#### Phase 6: Execute Operations
```java
ssArgs.setArgumentType(SignShopArgumentsType.Run);
ssArgs.reset();  // Clear special values

for (SignShopOperationListItem op : ops) {
    ssArgs.setOperationParameters(op.getParameters());
    if (!op.getOperation().runOperation(ssArgs))
        return;  // Abort on failure
}
```

**Example for [Buy]:**
1. `takePlayerMoney.runOperation()` → Fire SSMoneyTransactionEvent, deduct money
2. `giveShopItems.runOperation()` → Remove from chest, add to player inventory
3. `updateSign.runOperation()` → Update sign color

#### Phase 7: Post-Transaction Event
```java
SSPostTransactionEvent event = SSEventFactory.generatePostTransactionEvent(ssArgs, seller, action);
SignShop.scheduleEvent(event);
```

#### Phase 8: Logging
```java
SignShop.getInstance().logTransaction(
    player.getName(), owner.getName(), operation, items, price
);
```

---

## 3. Serialization Flow

### Serialization (ItemStack → String)

**File:** `ItemSerializer.serialize()`

```java
// 1. Check for incompatibilities
IncompatibilityType issue = IncompatibilityChecker.checkItem(item);
if (issue != null) {
    // Use LEGACY format for problematic items
    return LEGACY_PREFIX + BukkitSerialization.itemStackArrayToBase64(item);
}

// 2. Modern YAML serialization
Map<String, Object> serialized = item.serialize();
YamlConfiguration yaml = new YamlConfiguration();
yaml.set("item", serialized);
String yamlString = yaml.saveToString();

// 3. Encode to Base64
return MODERN_PREFIX + Base64.getEncoder().encodeToString(yamlString.getBytes());
```

### Deserialization (String → ItemStack)

```java
// 1. Detect format
if (serialized.startsWith("YAML:")) {
    return deserializeModern(serialized.substring(5));
} else if (serialized.startsWith("LEGACY:") || serialized.startsWith("rO0AB")) {
    return deserializeLegacy(serialized);
}

// 2. Modern deserialization
byte[] yamlBytes = Base64.getDecoder().decode(base64);
YamlConfiguration yaml = new YamlConfiguration();
yaml.loadFromString(new String(yamlBytes));
ItemStack item = ItemStack.deserialize(yaml.getConfigurationSection("item").getValues(true));

// 3. CRITICAL: Normalize data version
return ItemStack.deserialize(item.serialize());
```

### Data Version Normalization

**Problem:** After MC updates, item data versions differ:
- Stored item: `v: 4556` (MC 1.21.10)
- Player's item: `v: 4671` (MC 1.21.11)
- `ItemStack.equals()` fails → Trade shops break

**Solution:**
```java
// Round-trip through serialize/deserialize normalizes version
ItemStack normalized = ItemStack.deserialize(item.serialize());
```

### Migration Flow

**File:** `DataConverter.init()`

```java
if (sellers.getInt("DataVersion") < SignShop.DATA_VERSION) {
    // Create backup
    FileUtil.copy(sellersFile, backupFile);

    // Convert all shops
    for (shop : sellers) {
        for (item : shop.items) {
            if (!item.startsWith("YAML:")) {
                ItemStack is = ItemSerializer.deserialize(item);
                if (IncompatibilityChecker.checkItem(is) == null) {
                    // Migrate to YAML
                    updateShop(shop, ItemSerializer.serialize(is));
                } else {
                    // Keep LEGACY
                    updateShop(shop, "LEGACY:" + item);
                }
            }
        }
    }
} else {
    // Re-check LEGACY shops for auto re-migration
    recheckLegacyShops();
}
```

---

## 4. Configuration Loading

### Startup Sequence

**File:** `SignShop.onEnable()`

```
1. Create data folder
     ↓
2. SignShopConfig()
   ├── Load config.yml
   ├── Load language files
   ├── Register operations via reflection
   └── Build compiledOperations cache
     ↓
3. DataConverter.init()
   ├── Check DataVersion
   ├── Backup if migrating
   └── Migrate items
     ↓
4. Storage.init()
   ├── Load sellers.yml
   ├── Validate each shop
   └── Defer unloaded worlds
     ↓
5. Vault.setupEconomy()
     ↓
6. Register listeners
     ↓
7. Register hooks
     ↓
8. Enable metrics
```

### Operation Registration

**File:** `SignShopConfig.registerOperations()`

```java
ConfigurationSection signs = config.getConfigurationSection("signs");

for (String signType : signs.getKeys(false)) {
    List<String> opNames = signs.getStringList(signType);
    List<SignShopOperationListItem> opList = new LinkedList<>();

    for (String opName : opNames) {
        // Reflection to instantiate
        Class<?> cls = Class.forName("org.wargamer2010.signshop.operations." + opName);
        SignShopOperation op = (SignShopOperation) cls.getDeclaredConstructor().newInstance();

        // Cache instance
        OperationInstances.put(opName, op);
        opList.add(new SignShopOperationListItem(op, parseParameters(opName)));
    }

    // Store compiled list
    compiledOperations.put(signType, opList);
}
```

### Shop Loading

**File:** `Storage.Load()`

```java
for (shopSettings : tempSellers) {
    try {
        // Validate world
        World world = Bukkit.getWorld(shopWorld);
        if (world == null) {
            deferredSellers.put(shopKey, settings);
            continue;
        }

        // Validate sign
        Block signBlock = location.getBlock();
        if (!itemUtil.clickedSign(signBlock)) {
            throw new StorageException(SIGN_NOT_VALID);
        }

        // Deserialize items
        ItemStack[] items = itemUtil.convertStringtoItemStacks(itemStrings);

        // Create seller
        addSeller(owner, world, sign, containables, activatables, items, misc, false);
    } catch (StorageException e) {
        invalidSellers.add(shopKey);
    }
}

// Handle deferred shops when world loads
@EventHandler
public void onWorldLoad(WorldLoadEvent event) {
    loadDeferredShops(event.getWorld().getName());
}
```

---

## 5. Event System

### Event Lifecycle

```
Shop Creation:
  setupOperation() × n
       ↓
  SSCreatedEvent ─→ [Cancellable]
       ↓
  Storage.addSeller()

Transaction:
  checkRequirements() × n
       ↓
  SSPreTransactionEvent ─→ [Cancellable, Price modifiable]
       ↓
  runOperation() × n
       ↓
  SSPostTransactionEvent
       ↓
  Logging
```

### Event Types

| Event | Trigger | Cancellable | Key Data |
|-------|---------|-------------|----------|
| SSCreatedEvent | Shop creation | Yes | ssArgs, miscSettings |
| SSPreTransactionEvent | Before ops run | Yes | price, items, seller |
| SSPostTransactionEvent | After ops run | Yes | same as above |
| SSMoneyTransactionEvent | Money ops | Yes | amount, eventType |
| SSLinkEvent | Block linked | Yes | player, block, shop |
| SSDestroyedEvent | Shop destruction | Yes | destroyReason |

### SSMoneyTransactionEvent

**Request Types:**
- `CheckBalance`: Verify player has funds
- `ExecuteTransaction`: Actually move money

```java
// In takePlayerMoney.checkRequirements()
SSMoneyTransactionEvent event = SSEventFactory.generateMoneyEvent(
    ssArgs, SSMoneyEventType.TakeFromPlayer, SSMoneyRequestType.CheckBalance
);
SignShop.scheduleEvent(event);
return event.isHandled() && !event.isCancelled();

// In takePlayerMoney.runOperation()
SSMoneyTransactionEvent event = SSEventFactory.generateMoneyEvent(
    ssArgs, SSMoneyEventType.TakeFromPlayer, SSMoneyRequestType.ExecuteTransaction
);
SignShop.scheduleEvent(event);
return event.isHandled() && !event.isCancelled();
```

### Internal Listeners

**Package:** `listeners.sslisteners`

| Listener | Purpose |
|----------|---------|
| PermissionChecker | Validate SignShop.Signs.* permissions |
| StockChecker | Update sign colors based on stock |
| ShopCooldown | Enforce cooldown between transactions |
| BankTransaction | Handle Vault economy operations |
| SimpleMessenger | Send transaction messages |
| MoneyModifierListener | Apply group price discounts |

### Creating Custom Listeners

```java
public class MyListener implements Listener {
    @EventHandler(priority = EventPriority.NORMAL)
    public void onPreTransaction(SSPreTransactionEvent event) {
        // Apply 10% discount
        double newPrice = event.getPrice() * 0.9;
        event.setPrice(newPrice);
        event.getPlayer().get().sendMessage("10% discount applied!");
    }
}

// Register
Bukkit.getPluginManager().registerEvents(new MyListener(), plugin);
```

---

## Performance Optimizations

### Hot Paths

| Path | Optimization | Impact |
|------|--------------|--------|
| `getCompiledOperations()` | Pre-compiled at startup | 32% faster |
| Trade shop items | `Seller.miscItemsCache` | 1-5ms saved |
| `takeVariablePlayerItems` | Cache in messageParts | 40-50% faster |

### Bottleneck Prevention

```java
// BAD: Reflection on every transaction
SignShopOperation op = (SignShopOperation) Class.forName(name).newInstance();

// GOOD: Cached instance
SignShopOperation op = compiledOperations.get(signType).get(i).getOperation();
```

---

## Diagrams

### Transaction Flow
```
Player right-clicks sign
         ↓
┌─────────────────────┐
│ Load shop & chunks  │
└─────────┬───────────┘
          ↓
┌─────────────────────┐
│ checkRequirements() │──→ Fail? Return
└─────────┬───────────┘
          ↓
┌─────────────────────┐
│ SSPreTransactionEvt │──→ Cancelled? Return
└─────────┬───────────┘
          ↓
   Left-click? ──→ Show confirmation, return
          ↓
┌─────────────────────┐
│   runOperation()    │──→ Fail? Return
└─────────┬───────────┘
          ↓
┌─────────────────────┐
│ SSPostTransactionEvt│
└─────────┬───────────┘
          ↓
┌─────────────────────┐
│       Logging       │
└─────────────────────┘
```

### Serialization Pipeline
```
ItemStack
    ↓
┌───────────────────┐
│Incompatibility?   │
└───┬───────────┬───┘
    ↓Yes        ↓No
 LEGACY:     YAML:
  Base64      Base64
    ↓           ↓
    └─────┬─────┘
          ↓
    sellers.yml
```

---

## Key Files Reference

| File | Purpose |
|------|---------|
| `SignShopPlayerListener.java` | Shop creation & transaction handling |
| `Storage.java` | Shop persistence |
| `SignShopConfig.java` | Configuration & operation registration |
| `ItemSerializer.java` | Item serialization |
| `DataConverter.java` | Data migration |
| `SignShopArguments.java` | Context object |
| `SSEventFactory.java` | Event creation |
| `IncompatibilityChecker.java` | Item compatibility detection |
