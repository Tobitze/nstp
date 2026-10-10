package de.nstp;

import net.minecraft.world.World;

/** Abstraktion: alles, was mit Wireless Redstone zu tun hat, steckt hinter diesem Interface. */
public interface FrequencyBackend {

    /** Schaltet eine Frequenz per "Remote" ein oder aus. */
    void setFrequency(World world, int frequency, boolean active);

    /** Setzt eine Frequenz komplett zurück (alle Sender und Remotes werden abgemeldet). */
    void clearFrequency(World world, int frequency);
}
