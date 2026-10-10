package de.nstp;

import net.minecraft.world.World;
import rzk.wirelessredstone.rsnetwork.Device;
import rzk.wirelessredstone.rsnetwork.RedstoneNetwork;

/**
 * Einzige Stelle, die Wireless Redstone (rzk.wirelessredstone, 1.12.2) anfasst.
 *
 * Wir verhalten uns wie ein gedrückter "Remote": ItemRemote.setPowered() macht exakt dasselbe,
 * nämlich Device.createRemote(frequency) bei RedstoneNetwork.addDevice()/removeDevice() an- bzw.
 * abzumelden. Ein Remote hat keine Block-Position und wird im Channel nur als Zähler geführt
 * (remotes++ / remotes--).
 */
public class WirelessRedstoneBackend implements FrequencyBackend {

    @Override
    public void setFrequency(World world, int frequency, boolean active) {
        // WR speichert Frequenzen als short, die GUI zeigt sie als unsigned (0..65535)
        if (frequency < 0 || frequency > 0xFFFF) {
            NstpMod.log.warn("Frequenz {} ausserhalb 0..65535, ignoriert", frequency);
            return;
        }

        RedstoneNetwork network = RedstoneNetwork.get(world);
        if (network == null) {
            NstpMod.log.warn("Kein RedstoneNetwork fuer Dimension {}", world.provider.getDimension());
            return;
        }

        Device remote = Device.createRemote((short) frequency);
        if (active) {
            network.addDevice(remote);
        } else {
            network.removeDevice(remote);
        }
    }

    @Override
    public void clearFrequency(World world, int frequency) {
        if (frequency < 0 || frequency > 0xFFFF) return;

        RedstoneNetwork network = RedstoneNetwork.get(world, false);
        if (network != null) {
            network.clearFrequency((short) frequency);
        }
    }
}
