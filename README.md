<p align="center">
 <img src="signshopheader.png" alt="SignShop LOGO">
</p>



SignShop allows you to set up physical shops by punching a chest with your items you want to sell, then punching a sign (while holding redstone dust). It's easy to set up, and even easier to customize!

## Project Links

 - [Discord - Community server](https://discord.gg/4wnyysf)
 - [Plugin Page - Bukkit Dev](https://dev.bukkit.org/projects/signshop)
 - [Spigot Page](https://www.spigotmc.org/resources/signshop.10997/)
 - [Config file - Configuration manual](https://github.com/wargamer/SignShop/blob/master/src/main/resources/config.yml)


## Basic Features


- Don't need to memorize any annoying commands
- Can create global shops (with infinite items and infinite money using [iBuy] and [iSell])
- Can create player owned shops
- Can buy and sell raw XP and XP levels
- Signs have colors: [Blue] means the shop is stocked and working, [Red] means the shop is out or overstocked, [Black] means the sign is not active
- Can sell multiple items per sign (e.g. Alchemy Starter Kit, containing 3 glass bottles 1 brewing stand, netherwarts, ghast tears, etc.)
- Chests do not need to be directly under signs, they can be anywhere (Distance can be customized in the config)
- Can use multiple signs per chest (just remove any extra items, link the chest to the new sign, and stock the chest with multiple item types!)
- Ability to set up shops to control redstone levers (to charge admittance to a door, power some sort of contraption, blow up a bunch of TNT, whatever you want!)
- Can limit number of shops per player or permission group
- Can edit active signs by clicking a sign with the desired text using Black - Dye and then clicking the active SignShop
- Can disable trading with villagers
- Profit sharing using [Share] signs linked to SignShops
- Restrict shop use to only groups listed on [Restricted] signs
- Localization support (if you would like your native language to be - supported, help us translate our config! More information on this page.)
- Supports custom potions, books, fireworks, and lores

## Advanced Features

- Can set up signs to run commands in console
- Can run commands as if the buyer typed the command themselves (use "runCommand{asUser}" block in the config. NOTE: "*" permission nodes must be supported by your permission plugin for this feature)
- Can run commands after a certain amount of time, allowing you to sell things like temporary permissions
- Can sell partial amounts to signs (disabled by default)
- Customizable messages
- Customizable signs
- Can define multipliers for groups to allow certain groups to get discounts or make more money for selling items


## List of signs 

### Player signs


| Sign          | Description |
| ------------- | ----------- |
| [Buy]         | Buy an item from the shop chest for the price specified on the 4th line |
| [Sell]        | Sell an item to the shop chest for the price specified on the 4th line |
| [Trade]       | Trades one set of items for another, 2 chests required |
| [Share]       | Link to another SignShop to split profits, lines 2 and 3 are for the other players, line 4 is for % amounts (e.g. "25/50" for 75% to others) |
| [Bank]        | Link to another SignShop to make the shop take/give money to a bank account, The bank account is specified on line 2 of the sign (Note: you must own the bank account for this to work) |
| [Donate]      | Gives an item to the shop chest |
| [DonateHand]  | Donates the item in your hand to the shop chest |
| [Dispose]     | Takes the item in your hand and safely decomposes the material |
| [Slot]        | Gives a random item from the selected chest items (not the entire inventory) to the player |
| [DeviceOn]    | Turns a lever on |
| [DeviceOff]   | Turns a lever off |
| [Toggle]      | Toggles a lever |
| [Device]      | Temporarily turns on a lever |
| [DeviceItem]  | Temporarily turns on a lever using items as payment |
| [Jukebox]     | Allows players to create jukeboxes by placing music discs in a chest |
| [Restricted]  | Makes it so only certain permission groups can use the linked SignShop (listed on lines 2, 3, and 4) |



### Admin signs

Important: "Admin" signs require OP or SignShop.Admin.* to create

| Sign          | Description |
| ------------- | ----------- |
| [gBuy]        | Buy an item from the shop chest, but the owner receives no money |
| [gSell]       | Sell an item to the shop chest, and the player receives money, but not from the owner |
| [iBuy]        | Buy an item from the "shop", money goes to no one, infinite items |
| [iSell]       | Sell an item to the "shop", infinite money, item disappears |
| [iTrade]      | Trades one set of items for another, infinite stock |
| [Class]       | Takes the user's inventory and replaces it with items from a chest, infinite stock |
| [Kit]         | Gives the buyer a set of items once (infinite stock), must be reset using ResetKit sign before they can use it again |
| [ResetKit]    | Allows a player to use a Kit sign again |
| [iBuyXP]      | Buy the number of XP levels on the third line of the sign |
| [iSellXP]     | Sell the number of XP levels on the third line of the sign |
| [iXPBuy]      | Buy an item using raw XP points on the third line of the sign |
| [iXPSell]     | Sell an item using raw XP points on the third line of the sign |
| [iSlot]       | Gives a random item from the selected chest items with infinite stock |
| [Day]         | Turns the time to day |
| [Night]       | Turns the time to night |
| [Rain]        | Turns on rain + thunder |
| [ClearSkies]  | Turns off rain + thunder |
| [Repair]      | Repairs the current item |
| [Heal]        | Fully heals the player |
| [Enchant]     | Sells the enchantments from the item in the chest |
| [Disenchant]  | Removes enchantments from an item |
| [TpToOwner]   | An example of a custom sign for running commands |
| [Command]     | Allows players to run commands on the 2nd and 3rd lines of the sign |
| [UserCommand] | Allows players to run commands on the 2nd and 3rd lines of the sign as if they typed it themselves |
| [Promote]     | Promotes players to the permission group listed on the 2nd line of the sign |



Info: If the sign you are looking for isn't here, you can create custom signs in the config. Just add it to the signs: section and give it the appropriate blocks. If you need help, make sure to check out the SignShop Quick Reference found in your plugins/SignShop folder.



## Permissions

| Permission                    | Description                                                                                                                            |
|-------------------------------|----------------------------------------------------------------------------------------------------------------------------------------|
| Signshop.DenyUse.*             | Denies usage of signs. Example: You may wish to deny a group's ability to sell to an infinite shop (i.e., an [iSell] sign), in which case you would give them: Signshop.DenyUse.iSell                |
| Signshop.DenyLink.*            | Denies the linking of shops to certain in-game blocks (Chest, Sign, Lever, Dispenser, Furnace, Brewingstand, Enchantmenttable, Slab). Example: You may wish to deny a group's ability to link their shops to a furnace to prevent automatic smelting, in which case you would give them: Signshop.DenyLink.Furnace          |
| Signshop.Signs.*               | Allows players to create signs. Example: You might want to disallow a group from creating signs to modify redstone levers, you can remove that ability by negating them with the following permission nodes (assuming your permission plugin allows negating permissions): -Signshop.Signs.Device -Signshop.Signs.DeviceOn -Signshop.Signs.DeviceOff -Signshop.Signs.Toggle        |
| Signshop.CopyPaste             | Allows players to click on signs with black dye to copy information onto an already active SignShop. Example: If you want to update the price of an item, create a new sign and put the new price on the bottom line, leave the other 3 lines blank, and click with black dye. You can modify the description, the price, and the type of sign this way. Blank lines are ignored. You cannot, however, change a Device sign to a Buy sign, as the operations are incompatible with one another. You can also allow moderators and admins to edit other player's signs with Signshop.CopyPaste.Others  |
| Signshop.Permit                | If the "AllowPermits" setting in the global options is set to true, players must have this node in order for their shops to work. Example: You can use SignShop and a permission plugin to sell permits allowing users to be merchants. Without a permit, the shop will be disabled and they will need to buy another in order for their shops to continue functioning. You can also do Signshop.Permit.Stone to only allow shops containing the material “Stone”. (Make sure to replace any spaces with an underscore, i.e. “BAKED POTATO” becomes “BAKED_POTATO”)               |
| Signshop.ChangeOwner           | Allows a player to click on another player with redstone to change the owner of a SignShop. Example: If you would like to set up a player account as a bank, or transfer a store to another player, you would punch them with redstone, then punch the sign you would like to modify. If you do not own the sign that is being modified, you will also need the permission Signshop.ChangeOwner.Others to do so.    |
| Signshop.Destroy.Others        | Allows a player to destroy shops that they do not own. Example: Give this permission to moderators or helpers that you want to be able to clean up abandoned shops but you don't want them to be able to do other SignShop admin duties.     |
| Signshop.IgnoreMax             | Bypasses any defined maximum shop settings in the config. Example: You can make it so normal players can only create something like 10 signs, while donators can create infinite, by giving them Signshop.IgnoreMax           |
| Signshop.IgnoreRepair          | Bypasses AllowEnchantedRepair setting in the config. Example: You can make it so "Blacksmiths" can repair enchanted items with Signshop.IgnoreRepair          |
| Signshop.Inspect.Own           | Allows you to inspect your own shops by hitting the sign with the inspection material. (Book and Quill by default)                           |
| Signshop.Inspect.Others        | Allows you to inspect other player's shops by hitting the sign with the inspection material. (Book and Quill by default)                     |
| Signshop.Dye.Own               | Allows you to use dyes and glow on your shop signs.                                                                                        |
| Signshop.Dye.Others            | Allows you to use dyes and glow on shops that you do not own.                                                                            |
| Signshop.BypassShopPlots.*     | Bypasses EnableShopPlotSupport setting in the config. Example: You can make it so VIPs are allowed to create shops in regions with the “allow-shop” flag set to “deny” in Worldguard using Signshop.BypassShopPlots.Worldguard          |
| Signshop.Admin.*               | Allows players to create administrative signs (such as shops with infinite items for global shops). Example: You can use this permission to grant the ability to make signs with the playerIsOp tag (defined in the config). Signshop.Admin.Heal allows a player to create a healing station.        |
| Signshop.SuperAdmin            | Makes players seen by SignShop as OPs. Example: You can use this permission to grant the ability to bypass the blacklist, break other users' shops, use /signshop reload, etc.       |


## FAQ

#### How do I get money?

Firstly, you must have Vault on your server. Secondly, you must be using one of the many economy plugins Vault supports. GoldisMoney and Gringotts are both good for item based economies. If you would rather have a currency, Craftconomy is a popular choice. Then you must use a command from your economy plugin to give yourself money. Check their documentation, but it's usually something like /money grant, /eco give, things like that.

#### Why isn't money coming out of my account?

Firstly, make sure you have an economy plugin and Vault installed. If you have done that correctly, then chances are, you are buying from a shop that you made yourself. The money is going to the owner of that sign, and that happens to be you. To avoid that, use an infinite sign like [iBuy] and the money will not go into the owner's account, or have someone else make a sign that you can use.

#### How do I make shops with infinite items / no chest?

Usually when people ask this, they want an [iBuy] or [iSell] sign, but there are many other shops with infinite items. You will need the chest to setup the shop originally, but you can destroy it afterwards.

#### Why is my shop only giving me X amount of items? I want it to give me Y amount!

When you link the sign and the chest, the amount that is in the chest will be the amount of items per transaction. So if you want to only sell 8 of an item with each transaction, you put 8 in the chest, then link it to the sign. Afterwards, you can fill up the chest, and it will still continue to sell 8 at a time.

#### Why can't my players use shops? They can only get the confirm purchase message.

This is usually caused by a plugin interfering with SignShop or the built-in vanilla spawn protection. Try changing spawn-protection=16 to spawn-protection=0 in server.properties. If you have Essentials installed try disabling all of the Essentials signs including -color by commenting them out. Otherwise, you can use your permission plugin's verbose logging to see what permissions are being checked when they try to use a sign. i.e. /LuckPerms verbose on or /pex toggle debug.

#### What does THIS feature/option do, and how do I use it?

Most of these sorts of questions are answered in our [Tutorial Video](http://www.youtube.com/watch?feature=player_embedded&v=MXCpwJaxozg) and Quick [Reference PDF](https://dev.bukkit.org/linkout?remoteUrl=https%253a%252f%252fwww.dropbox.com%252fs%252fuiqcsb8ux1o2vxu%252fSSQuickReference.pdf). You can also find this in your plugins/signshop folder for your server. If it is not answered there, feel free to drop a comment on our main page.

#### My [Slot] sign keeps randomly running out of items, what gives?

Think of each slot in the chest like it has an even chance of being the slot selected to give to the player. If you wanted a 10% chance for diamond, and 90% chance of coal, you would put a diamond in a slot, and coal in 9 separate slots. With this setup, if you were to get the diamond on your first try, it would be removed from the chest, and it would report the chest is out of stock because there are no diamonds, so it's impossible for a player to win a diamond. Once an item is no longer in the chest, it cannot be randomly selected to be given out. To avoid this, after linking the chest with 1 diamond and 9 coal, you can fill the whole chest up with diamonds and coal, just like a normal shop. Also, if you wanted the winner to have a 10% chance of getting 2 diamonds, and 90% chance for coal, you could do the same as before, but put 2 diamonds in one slot of the chest, so you will still have 10 filled slots total when linking.

#### How come [Device] type signs aren't activating my redstone?

[Device] type signs will only work on levers that are on the ground, not on walls. Unfortunately, this is a bug in CraftBukkit and there is nothing we can do about it.

#### How do I destroy a shop?

If you are in creative mode you must use a gold axe (if ProtectShopsInCreative is set to true in the config). Otherwise, you can use anything except redstone dust or an ink sack. Note that these are all the default objects set in the config, you can change these items.

#### Can you make it so people HAVE to write what they are selling on the sign?

I know many people think that writing one thing on a sign, and selling another is a good way to scam people. HOWEVER, if you are a buyer, all you have to do is left click a sign, and it will tell you exactly what it is selling. We do it this way for 2 reasons. The first is because you cannot sell multiple items with the limited amount of space on a sign. In SignShop you can sell an Alchemy starter kit, 1 Brewing Stand, 3 Glass bottles, Netherwarts, etc. all on one sign. That would be impossible to list. The second reason is because then your users have to memorize item numbers, or item names. Think about how many things you can call a wood slab (wood slab, wooden slab, wood step, wooden step, etc.). It's just too annoying to memorize.

#### Can you update the plugin to the latest version of Minecraft?

There's not usually something that needs to be changed in SignShop just because Minecraft has changed versions. Our latest release should work. Occasionally, however, something DOES change that requires our attention. If this is the case, first grab the latest development build and make sure that doesn't solve your issue. If it does not, please create a ticket and fill out the requested information. Development builds of this project can be acquired at the provided continuous integration server. These builds have not been approved by the BukkitDev staff. Use them at your own risk. You can get them [here](https://dev.bukkit.org/linkout?remoteUrl=http%253a%252f%252fsignshop.beastmc.com%252f).

#### How do I get help or make a ticket?

First run the "/signshop help" command to get the version information. Then check which Bukkit version you have by running "/version". Then make sure you get all the lines from any relevant error messages from your server.log. And lastly, put all the info in a new [ticket](http://dev.bukkit.org/server-mods/signshop/tickets/).

