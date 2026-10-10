package de.nstp;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.WorldServer;
import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/** Fängt Chatnachrichten im Schema XXXYYY<+|-|++|--> ab und löst die Frequenzen aus. */
public class ChatHandler {

    private static final Pattern SCHEMA = Pattern.compile("^(\\d{3})(\\d{3})(\\+\\+|--|\\+|-)$");

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onChat(ServerChatEvent e) {
        EntityPlayerMP player = e.getPlayer();

        String stellwerk = NstpMod.sessions.get(player.getUniqueID());
        if (stellwerk == null) return;

        Matcher m = SCHEMA.matcher(e.getMessage().trim());
        if (!m.matches()) return;

        // Passt ins Schema -> nie an andere Spieler weiterleiten
        e.setCanceled(true);

        NstpConfig.Stellwerk sw = NstpMod.config.find(stellwerk);
        if (sw == null) {
            reply(player, "§c[NSTP] Stellwerk '" + stellwerk + "' nicht mehr in der Config.");
            return;
        }

        WorldServer world = player.getServer().getWorld(sw.dimension);
        if (world == null) {
            reply(player, "§c[NSTP] Dimension " + sw.dimension + " nicht geladen.");
            return;
        }

        String symbol = m.group(3);
        Map<String, Integer> table = sw.table(symbol);

        // Erst beide Zahlen prüfen: fehlt eine, wird die ganze Eingabe verworfen
        List<String> missing = new ArrayList<>();
        for (int i = 1; i <= 2; i++) {
            if (!table.containsKey(m.group(i))) missing.add(m.group(i));
        }
        if (!missing.isEmpty()) {
            reply(player, "§c[NSTP] Eingabe verworfen: " + String.join(", ", missing)
                    + " fehlt in der Config (" + stellwerk + " " + symbol + "). Es wurde nichts geschaltet.");
            return;
        }

        int f1 = table.get(m.group(1));
        int f2 = table.get(m.group(2));
        NstpMod.scheduler.pulse(world, f1, NstpMod.config.pulseTicks);
        NstpMod.scheduler.pulse(world, f2, NstpMod.config.pulseTicks);

        String relais = symbol.length() == 1 ? "Zugfahrstraßen-Tastenrelais" : "Rangierfahrstraßen-Tastenrelais";
        reply(player, "§7[NSTP] " + stellwerk + ": " + relais + " " + m.group(1) + " und " + m.group(2) + " geschaltet. Klick Klack!");
    }

    private static void reply(EntityPlayerMP p, String msg) {
        p.sendMessage(new TextComponentString(msg));
    }
}
