package de.nstp;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;

/** Merkt sich, welcher Spieler gerade für welches Stellwerk "auf Sendung" ist. */
public class SessionManager {

    // Zugriff nur vom Server-Thread
    private final Map<UUID, String> active = new HashMap<>();

    public void start(UUID player, String stellwerk) {
        active.put(player, stellwerk);
    }

    public void stop(UUID player) {
        active.remove(player);
    }

    /** @return Stellwerksname oder null, wenn der Spieler nicht aktiv ist */
    public String get(UUID player) {
        return active.get(player);
    }

    @SubscribeEvent
    public void onLogout(PlayerEvent.PlayerLoggedOutEvent e) {
        active.remove(e.player.getUniqueID());
    }
}
