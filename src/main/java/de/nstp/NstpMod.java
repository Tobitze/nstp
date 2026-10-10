package de.nstp;

import java.io.File;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.logging.log4j.Logger;

import net.minecraft.server.MinecraftServer;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartedEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.event.FMLServerStoppingEvent;

/**
 * Reiner Server-Mod: Clients brauchen sie nicht (acceptableRemoteVersions = "*").
 * Mod-ID von Wireless Redstone: "wirelessredstone" (WirelessRedstone.MOD_ID).
 */
@Mod(modid = NstpMod.MODID, name = "NSTP", version = "1.0.0",
        acceptableRemoteVersions = "*", dependencies = "required-after:wirelessredstone")
public class NstpMod {

    public static final String MODID = "nstp";

    public static Logger log;
    public static NstpConfig config;
    public static final SessionManager sessions = new SessionManager();
    public static FrequencyBackend backend;
    public static PulseScheduler scheduler;

    @EventHandler
    public void preInit(FMLPreInitializationEvent e) {
        log = e.getModLog();
        config = NstpConfig.load(new File(e.getModConfigurationDirectory(), "nstp.json"));

        backend = new WirelessRedstoneBackend();
        scheduler = new PulseScheduler(backend);

        MinecraftForge.EVENT_BUS.register(new ChatHandler());
        MinecraftForge.EVENT_BUS.register(sessions);
        MinecraftForge.EVENT_BUS.register(scheduler);
    }

    @EventHandler
    public void serverStarting(FMLServerStartingEvent e) {
        e.registerServerCommand(new NstpCommand());
    }

    @EventHandler
    public void serverStarted(FMLServerStartedEvent e) {
        log.info("NSTP: setze alle Stellwerks-Frequenzen nach dem Start zurueck");
        resetAll(FMLCommonHandler.instance().getMinecraftServerInstance());
    }

    @EventHandler
    public void serverStopping(FMLServerStoppingEvent e) {
        // Keine hängenden Frequenzen beim Herunterfahren
        scheduler.releaseAll();
    }

    /** @return Anzahl zurückgesetzter Frequenzen, oder -1 wenn die Dimension nicht geladen ist */
    public static int resetStellwerk(MinecraftServer server, NstpConfig.Stellwerk sw) {
        WorldServer world = server.getWorld(sw.dimension);
        if (world == null) return -1;

        Set<Integer> freqs = new TreeSet<>();
        for (String symbol : new String[] { "-", "--", "+", "++" }) {
            freqs.addAll(sw.table(symbol).values());
        }
        for (int f : freqs) backend.clearFrequency(world, f);
        return freqs.size();
    }

    public static void resetAll(MinecraftServer server) {
        for (Map.Entry<String, NstpConfig.Stellwerk> e : config.stellwerke.entrySet()) {
            int n = resetStellwerk(server, e.getValue());
            if (n < 0) {
                log.warn("Stellwerk '{}': Dimension {} nicht geladen, nicht zurueckgesetzt",
                        e.getKey(), e.getValue().dimension);
            } else {
                log.info("Stellwerk '{}': {} Frequenzen zurueckgesetzt (Dimension {})",
                        e.getKey(), n, e.getValue().dimension);
            }
        }
    }
}
