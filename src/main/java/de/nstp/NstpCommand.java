package de.nstp;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;

/** /nstp <stellwerksname> <on|off>   und   /nstp reset <stellwerksname|all> */
public class NstpCommand extends CommandBase {

    @Override
    public String getName() {
        return "nstp";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "/nstp <stellwerksname> <on|off>  |  /nstp reset <stellwerksname|all>";
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 0; // jeder Spieler; "reset" prüft zusätzlich selbst auf Op-Rechte
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        if (args.length == 2 && args[0].equalsIgnoreCase("reset")) {
            executeReset(server, sender, args[1]);
            return;
        }

        EntityPlayerMP player = getCommandSenderAsPlayer(sender);

        if (args.length != 2) throw new WrongUsageException(getUsage(sender));

        String mode = args[1].toLowerCase();
        if (mode.equals("on")) {
            String name = NstpMod.config.canonicalName(args[0]);
            if (name == null) {
                throw new CommandException("Unbekanntes Stellwerk: " + args[0]);
            }
            NstpMod.sessions.start(player.getUniqueID(), name);
            player.sendMessage(new TextComponentString("§a[NSTP] Aktiv für Stellwerk '" + name
                    + "'. Nachrichten im Format 123456+ werden nicht mehr an den Chat gesendet."));
        } else if (mode.equals("off")) {
            NstpMod.sessions.stop(player.getUniqueID());
            player.sendMessage(new TextComponentString("§e[NSTP] Deaktiviert."));
        } else {
            throw new WrongUsageException(getUsage(sender));
        }
    }

    private void executeReset(MinecraftServer server, ICommandSender sender, String target) throws CommandException {
        if (!sender.canUseCommand(2, getName())) {
            throw new CommandException("Dafür sind Op-Rechte nötig.");
        }

        if (target.equalsIgnoreCase("all")) {
            NstpMod.resetAll(server);
            sender.sendMessage(new TextComponentString("§e[NSTP] Alle Stellwerke zurückgesetzt (Details im Serverlog)."));
            return;
        }

        String name = NstpMod.config.canonicalName(target);
        if (name == null) throw new CommandException("Unbekanntes Stellwerk: " + target);

        NstpConfig.Stellwerk sw = NstpMod.config.find(name);
        int n = NstpMod.resetStellwerk(server, sw);
        if (n < 0) throw new CommandException("Dimension " + sw.dimension + " ist nicht geladen.");

        NstpMod.log.info("Stellwerk '{}': {} Frequenzen zurueckgesetzt (durch {})", name, n, sender.getName());
        sender.sendMessage(new TextComponentString("§e[NSTP] " + name + ": " + n + " Frequenzen zurückgesetzt."));
    }

    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args,
                                          @Nullable BlockPos targetPos) {
        if (args.length == 1) {
            List<String> opts = new ArrayList<>(NstpMod.config.stellwerke.keySet());
            opts.add("reset");
            return getListOfStringsMatchingLastWord(args, opts);
        }
        if (args.length == 2) {
            if (args[0].equalsIgnoreCase("reset")) {
                List<String> opts = new ArrayList<>(NstpMod.config.stellwerke.keySet());
                opts.add("all");
                return getListOfStringsMatchingLastWord(args, opts);
            }
            return getListOfStringsMatchingLastWord(args, "on", "off");
        }
        return Collections.emptyList();
    }
}
