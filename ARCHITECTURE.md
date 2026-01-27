# SignShop Architecture

This document describes the internal architecture of the SignShop plugin, a Spigot/Bukkit plugin for chest-based trading.

## Table of Contents

1. [Package Structure](#package-structure)
2. [Core Classes](#core-classes)
3. [Plugin Lifecycle](#plugin-lifecycle)
4. [Data Storage](#data-storage)
5. [Operation System](#operation-system)
6. [Event System](#event-system)
7. [Extension Points](#extension-points)
8. [Design Patterns](#design-patterns)

---

## Package Structure

### Root Package (`org.wargamer2010.signshop`)
- **`SignShop.java`**: Main plugin class, manages lifecycle and subsystems
- **`Seller.java`**: Core shop model (owner, location, items, blocks, metadata)
- **`Vault.java`**: Economy and permissions integration

### `commands/`
Command handling: `CommandDispatcher`, `ICommandHandler`, handlers for `/signshop` subcommands

### `configuration/`
- **`SignShopConfig.java`**: Configuration manager, operation registration, message templates
- **`FileSaveWorker.java`**: Async file saving
- **`LinkableMaterial.java`**: Materials that can be linked to shops

### `data/`
- **`Storage.java`**: Shop persistence singleton, async saves to `sellers.yml`
- **`serialization/ItemSerializer.java`**: YAML+Base64 serialization with legacy fallback

### `events/`
Internal event system: `SSCreatedEvent`, `SSPreTransactionEvent`, `SSPostTransactionEvent`, `SSMoneyTransactionEvent`, `SSLinkEvent`, `SSDestroyedEvent`

### `hooks/`
Protection plugin integrations: WorldGuard, Towny, GriefPrevention, Residence, Lands, LWC, BlockLocker, BentoBox

### `incompatibility/`
Framework for detecting problematic items:
- **`IncompatibilityChecker.java`**: Main API
- **`detectors/PlayerHeadIncompatibilityDetector.java`**: Detects heads causing Spigot 1.21.10+ NPE

### `listeners/`
Bukkit event listeners:
- **`SignShopPlayerListener.java`**: Primary handler for shop creation and transactions
- **`SignShopBlockListener.java`**: Block events (break, explosion)

### `operations/`
100+ operation implementations:
- **`SignShopOperation.java`**: Interface with `setupOperation()`, `checkRequirements()`, `runOperation()`
- **`SignShopArguments.java`**: Context object passed through operation pipeline

### `player/`
- **`SignShopPlayer.java`**: Player abstraction
- **`PlayerIdentifier.java`**: UUID/name abstraction
- **`PlayerCache.java`**: Player instance management

### `util/`
Utilities: `itemUtil`, `signshopUtil`, `economyUtil`, `DataConverter`, `clicks`

---

## Core Classes

### SignShop (Main Plugin)
```
SignShop
├── SignShopConfig (configuration, operations)
├── Storage (shop persistence)
├── Vault (economy/permissions)
├── TimeManager (delayed commands)
└── Event Listeners
```

### Seller (Shop Model)
```
Seller
├── owner: SignShopPlayer
├── signLocation: Location
├── containables: List<Block> (chests)
├── activatables: List<Block> (levers)
├── isItems: ItemStack[]
├── miscProps: Map<String, String>
└── miscItemsCache: Map<String, ItemStack[]> (transient)
```

### SignShopArguments (Context)
```
SignShopArguments
├── fPrice: Double
├── isItems: ItemStack[]
├── containables/activatables: List<Block>
├── ssPlayer/ssOwner: SignShopPlayer
├── bSign: Block
├── sOperation: String
├── messageParts: Map<String, Object>
├── miscSettings: Map<String, String>
└── seller: Seller (for cached access)
```

---

## Plugin Lifecycle

### Startup (onEnable)
1. Create data folder
2. Load configuration (`SignShopConfig`)
3. Register operations via reflection
4. Build compiled operations cache
5. Run data migration (`DataConverter.init()`)
6. Load shops (`Storage.init()`)
7. Initialize Vault economy/permissions
8. Register event listeners
9. Setup hooks for protection plugins
10. Enable metrics

### Shutdown (onDisable)
1. Save storage
2. Dispose resources
3. Stop time manager

### Reload (`/signshop reload`)
- Reloads config and messages
- Shops remain in memory

---

## Data Storage

### sellers.yml Format
```yaml
sellers:
  "worldX/Y/Z":
    shopworld: "world"
    owner: "uuid:12345678-..."
    sign: "X/Y/Z"
    items:
      - "YAML:base64..."
      - "LEGACY:base64..."
    containables:
      - "X/Y/Z"
    misc:
      - "chest1:YAML:..."
DataVersion: 4
```

### Item Serialization
| Format | Prefix | When Used |
|--------|--------|-----------|
| Modern YAML | `YAML:` | Default (v5.1.0+) |
| Legacy | `LEGACY:` or `rO0AB` | Incompatible items, fallback |

### Async Saving
`Storage.Save()` → `FileSaveWorker` → Background thread write

---

## Operation System

### Interface
```java
public interface SignShopOperation {
    Boolean setupOperation(SignShopArguments ssArgs);
    Boolean checkRequirements(SignShopArguments ssArgs, Boolean activeCheck);
    Boolean runOperation(SignShopArguments ssArgs);
}
```

### Configuration
```yaml
signs:
  Buy:
    - takePlayerMoney
    - giveShopItems
    - giveOwnerMoney
    - updateSign
```

### Execution Flow

**Shop Creation:**
```
Left-click sign with redstone
  → For each operation: setupOperation()
  → Fire SSCreatedEvent (cancellable)
  → Storage.addSeller()
```

**Transaction:**
```
Right-click shop sign
  → For each operation: checkRequirements()
  → Fire SSPreTransactionEvent (cancellable)
  → For each operation: runOperation()
  → Fire SSPostTransactionEvent
  → Save storage
```

### Compiled Operations Cache
- Pre-compiled at config load
- `getCompiledOperations(signType)` returns cached list
- 32% faster transactions (eliminates reflection overhead)

---

## Event System

### Event Hierarchy
```
SSEvent (base)
├── SSCreatedEvent
├── SSDestroyedEvent
├── SSLinkEvent
├── SSTouchShopEvent
├── SSPreTransactionEvent
├── SSPostTransactionEvent
└── SSMoneyTransactionEvent
```

### Key Events

| Event | When | Cancellable |
|-------|------|-------------|
| SSCreatedEvent | Shop creation | Yes |
| SSPreTransactionEvent | Before transaction | Yes |
| SSPostTransactionEvent | After transaction | Yes |
| SSMoneyTransactionEvent | Money operations | Yes |

### Internal Listeners (`sslisteners/`)
- `PermissionChecker`: Validates permissions
- `StockChecker`: Updates sign colors
- `ShopCooldown`: Enforces cooldowns
- `BankTransaction`: Vault integration
- `SimpleMessenger`: Transaction messages

---

## Extension Points

### 1. Custom Operations
```java
public class MyOperation implements SignShopOperation {
    public Boolean setupOperation(SignShopArguments ssArgs) { return true; }
    public Boolean checkRequirements(SignShopArguments ssArgs, Boolean activeCheck) { return true; }
    public Boolean runOperation(SignShopArguments ssArgs) { return true; }
}

// Register
SignShop.getInstance().getSignShopConfig()
    .registerExternalOperation(new MyOperation());
```

### 2. Custom Hooks
```java
public class MyHook implements Hook {
    public String getName() { return "MyPlugin"; }
    public Boolean canBuild(Player player, Block block) { return true; }
    public Boolean protectBlock(Player player, Block block) { return true; }
}
```

### 3. Event Listeners
```java
@EventHandler
public void onTransaction(SSPostTransactionEvent event) {
    SignShopArguments args = event.getArguments();
    // Custom logic
}
```

### 4. Incompatibility Detectors
```java
public class MyDetector implements IncompatibilityDetector {
    public IncompatibilityType check(ItemStack item) { return null; }
    public boolean isRelevantForCurrentVersion() { return true; }
}
// Add to DETECTORS list in IncompatibilityChecker
```

---

## Design Patterns

| Pattern | Usage |
|---------|-------|
| Singleton | Storage, SignShop instance |
| Strategy | SignShopOperation implementations |
| Observer | Event system, Bukkit listeners |
| Factory | SSEventFactory, BookFactory |
| Template Method | SignShopOperation lifecycle |
| Flyweight | Compiled operations cache |
| Facade | SignShopConfig, economyUtil |
| Chain of Responsibility | Operation pipeline |
| Adapter | PlayerIdentifier, Hooks |
| Lazy Init | Deferred sellers, item cache |

---

## Performance Optimizations

1. **Operation List Caching**: Pre-compile at startup (32% faster)
2. **Item Deserialization Caching**: `Seller.miscItemsCache`
3. **Async File Saving**: `FileSaveWorker`
4. **takeVariablePlayerItems Caching**: Cache in messageParts
5. **Data Version Normalization**: Fix cross-version item comparison

---

## Thread Safety

| Thread | Operations |
|--------|------------|
| Main | All Minecraft API, operations, events |
| Async | File I/O (FileSaveWorker) |

`FileSaveWorker` uses `LinkedBlockingQueue` for thread-safe save queue.

---

## Error Handling

- **Shop Validation Errors**: Logged, shop removed, backup created
- **Operation Errors**: `checkRequirements()` → cancel, `runOperation()` → abort
- **Serialization Errors**: Fallback to legacy format, log error

---

This architecture enables SignShop to handle 100+ shop types with a plugin-style operation system, while maintaining backward compatibility with 10+ years of shop data.
