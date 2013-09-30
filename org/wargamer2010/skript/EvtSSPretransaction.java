package org.wargamer2010.skript;

import org.bukkit.event.Event;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.SerializableGetter;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptEvent;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.registrations.EventValues;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.wargamer2010.signshop.events.SSPreTransactionEvent;

@SuppressWarnings("serial")
public class EvtSSPretransaction extends SkriptEvent {

        public static void register() {
            Skript.registerEvent("SSPreTransaction", EvtSSPretransaction.class, SSPreTransactionEvent.class, "signshop pretransaction")
                            .description("Called when a player initiates a SignShop transaction.")
                            .examples("")
                            .since("1.3.7");

            EventValues.registerEventValue(SSPreTransactionEvent.class, Player.class, new SerializableGetter<Player, SSPreTransactionEvent>() {
                @Override
                public Player get(final SSPreTransactionEvent e) {
                        return e.getPlayer().getPlayer();
                }
            }, 0);

            EventValues.registerEventValue(SSPreTransactionEvent.class, Block.class, new SerializableGetter<Block, SSPreTransactionEvent>() {
                @Override
                public Block get(final SSPreTransactionEvent e) {
                        return e.getSign();
                }
            }, 0);
        }

	@Override
	public boolean init(final Literal<?>[] args, final int matchedPattern, final ParseResult parser) {
            return true;
	}

	@Override
	public boolean check(final Event e) {
            return !((SSPreTransactionEvent) e).isCancelled();
	}

	@Override
	public String toString(final Event e, final boolean debug) {
            return "SSPreTransaction";
	}

}
