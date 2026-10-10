package de.nstp;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.SerializedName;

/**
 * config/nstp.json
 *
 * {
 *   "pulseTicks": 20,
 *   "stellwerke": {
 *     "Hbf": {
 *       "dimension": 0,
 *       "-":  { "101": 1001 },
 *       "--": { "101": 2001 },
 *       "+":  { "101": 3001 },
 *       "++": { "101": 4001 }
 *     }
 *   }
 * }
 *
 * Zahlen (XXX/YYY) sind Strings, damit führende Nullen ("007") erhalten bleiben.
 */
public class NstpConfig {

    /** Dauer des Signals in Ticks (20 = 1 Sekunde). */
    public int pulseTicks = 20;

    public Map<String, Stellwerk> stellwerke = new LinkedHashMap<>();

    public static class Stellwerk {
        /** Dimension, in der Wireless Redstone geschaltet wird (WR arbeitet pro Dimension). */
        public int dimension = 0;

        @SerializedName("-")
        public Map<String, Integer> minus = new LinkedHashMap<>();
        @SerializedName("--")
        public Map<String, Integer> doubleMinus = new LinkedHashMap<>();
        @SerializedName("+")
        public Map<String, Integer> plus = new LinkedHashMap<>();
        @SerializedName("++")
        public Map<String, Integer> doublePlus = new LinkedHashMap<>();

        public Map<String, Integer> table(String symbol) {
            Map<String, Integer> t;
            switch (symbol) {
                case "-":  t = minus; break;
                case "--": t = doubleMinus; break;
                case "+":  t = plus; break;
                case "++": t = doublePlus; break;
                default:   t = null;
            }
            return t != null ? t : Collections.<String, Integer>emptyMap();
        }
    }

    /** Stellwerksname ohne Beachtung der Groß-/Kleinschreibung. */
    public Stellwerk find(String name) {
        for (Map.Entry<String, Stellwerk> e : stellwerke.entrySet()) {
            if (e.getKey().equalsIgnoreCase(name)) return e.getValue();
        }
        return null;
    }

    public String canonicalName(String name) {
        for (String key : stellwerke.keySet()) {
            if (key.equalsIgnoreCase(name)) return key;
        }
        return null;
    }

    private static NstpConfig defaults() {
        NstpConfig c = new NstpConfig();
        Stellwerk s = new Stellwerk();
        s.minus.put("101", 1001);
        s.doubleMinus.put("101", 2001);
        s.plus.put("101", 3001);
        s.doublePlus.put("101", 4001);
        c.stellwerke.put("Beispiel", s);
        return c;
    }

    public static NstpConfig load(File file) {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        if (!file.exists()) {
            NstpConfig def = defaults();
            try (Writer w = new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8)) {
                gson.toJson(def, w);
            } catch (IOException ex) {
                NstpMod.log.error("Konnte Standard-Config nicht schreiben", ex);
            }
            return def;
        }

        try (Reader r = new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8)) {
            NstpConfig c = gson.fromJson(r, NstpConfig.class);
            if (c == null) return defaults();
            if (c.stellwerke == null) c.stellwerke = new LinkedHashMap<>();
            return c;
        } catch (Exception ex) {
            NstpMod.log.error("nstp.json fehlerhaft, verwende Standardwerte", ex);
            return defaults();
        }
    }
}
