package de.nstp;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

/** Schaltet eine Frequenz ein und nach X Ticks wieder aus. Überlappende Pulse verlängern nur. */
public class PulseScheduler {

    private static final class Key {
        final int dim, freq;
        Key(int dim, int freq) { this.dim = dim; this.freq = freq; }
        @Override public boolean equals(Object o) {
            if (!(o instanceof Key)) return false;
            Key k = (Key) o;
            return k.dim == dim && k.freq == freq;
        }
        @Override public int hashCode() { return 31 * dim + freq; }
    }

    private static final class Pulse {
        final WorldServer world;
        int remaining;
        Pulse(WorldServer world, int remaining) { this.world = world; this.remaining = remaining; }
    }

    private final FrequencyBackend backend;
    private final Map<Key, Pulse> active = new HashMap<>();

    public PulseScheduler(FrequencyBackend backend) {
        this.backend = backend;
    }

    public void pulse(WorldServer world, int freq, int ticks) {
        Key k = new Key(world.provider.getDimension(), freq);
        Pulse p = active.get(k);
        if (p == null) {
            backend.setFrequency(world, freq, true);
            active.put(k, new Pulse(world, ticks));
        } else {
            p.remaining = Math.max(p.remaining, ticks);
        }
    }

    @SubscribeEvent
    public void onTick(TickEvent.ServerTickEvent e) {
        if (e.phase != TickEvent.Phase.END || active.isEmpty()) return;

        Iterator<Map.Entry<Key, Pulse>> it = active.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<Key, Pulse> en = it.next();
            Pulse p = en.getValue();
            if (--p.remaining <= 0) {
                backend.setFrequency(p.world, en.getKey().freq, false);
                it.remove();
            }
        }
    }

    public void releaseAll() {
        for (Map.Entry<Key, Pulse> en : active.entrySet()) {
            backend.setFrequency(en.getValue().world, en.getKey().freq, false);
        }
        active.clear();
    }
}
