package cn.langya.utils;

import cn.langya.Wrapper;
import net.minecraft.network.INetHandler;
import net.minecraft.network.Packet;
import net.minecraft.network.ThreadQuickExitException;
import net.minecraft.network.play.INetHandlerPlayServer;

import java.util.ArrayList;
import java.util.List;

/**
 * @author LangYa
 * @since 2024/12/6 14:58
 */
public class PacketUtil implements Wrapper {
    public static List<Packet<INetHandlerPlayServer>> skipSendEvent = new ArrayList<>();

    public static void sendPacket(Packet<?> packet) {
        mc.getNetHandler().addToSendQueue(packet);
    }

    public static <H extends INetHandler> Packet<H> castPacket(Packet<?> packet) throws ClassCastException {
        return (Packet<H>) packet;
    }

    public static void sendPacketNoEvent(Packet<?> packet) {
        if (packet == null)
            return;
        try {
            Packet<INetHandlerPlayServer> casted = castPacket(packet);
            skipSendEvent.add(casted);
            mc.thePlayer.sendQueue.addToSendQueue(casted);
        } catch (ThreadQuickExitException | ClassCastException ignored) {
        }
    }
}
