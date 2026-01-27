# Folia Compatibility

This fork adds **Folia support** to SignShop, allowing it to run on PaperMC's new multi-threaded server software.

## What is Folia?

Folia is Paper's experimental fork that implements region-based multithreading. It changes how the server schedules tasks, requiring plugins to adapt their code to work with the new threading model.

## What Changed?

### New Scheduler Adapter
Added `SchedulerAdapter` and `FoliaDetector` classes that automatically detect if the server is running Folia and use the appropriate scheduler API:
- **On Bukkit/Spigot/Paper:** Uses standard `BukkitScheduler`
- **On Folia:** Uses Folia's region-based scheduler (`RegionScheduler`, `GlobalRegionScheduler`, `AsyncScheduler`)

### Thread-Safe Shop Loading
Modified shop loading to handle Folia's threading model:
- Shop validation deferred on startup to prevent threading issues
- Shops validated on first use (on the correct region thread)

### API Compatibility
Updated all scheduler calls to work with Folia's `Consumer<ScheduledTask>` API instead of `Runnable`

## Building

```bash
mvn clean package
```

**Note:** Some optional plugin integrations (CMI, Essentials worth handlers, Residence, LWC, BlockLocker) are excluded from compilation in this build. If you need these, you'll need to install the dependency JARs to your local Maven repository.

## Testing

Successfully tested on:
- ✅ Folia 1.21.11
- ✅ Paper (backward compatible)

## Compatibility

- **Folia:** Full support
- **Paper/Spigot/Bukkit:** Full backward compatibility
- **Minecraft:** 1.13.2 - 1.21+

## Known Issues

- Console logs show raw color codes (§6, §f, etc.) - this is normal behavior, colors work properly in-game
- Shop validation warnings may appear on Folia startup - this is expected and shops will validate on first use

## Credits

Original plugin by [wargamer2010](https://github.com/wargamer/SignShop) and continued by [Aelshi-nui](https://github.com/Aelshi-nui/SignShop)

Folia compatibility added for multi-threaded server support.
