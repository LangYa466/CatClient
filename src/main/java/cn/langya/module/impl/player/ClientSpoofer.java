package cn.langya.module.impl.player;

import cn.langya.event.annotations.EventTarget;
import cn.langya.event.events.EventPacket;
import cn.langya.module.Category;
import cn.langya.module.Module;
import cn.langya.utils.PacketUtil;
import cn.langya.value.impl.ModeValue;
import io.netty.buffer.Unpooled;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.client.C17PacketCustomPayload;

/**
 * @author LangYa
 * @since 2024/12/29 10:30
 */
public class ClientSpoofer extends Module {
    public ClientSpoofer() {
        super(Category.Player);
    }

    private final ModeValue modeValue = new ModeValue("Client","Lunar","Vanilla",
            "OptiFine",
            "Fabric",
            "Lunar",
            "LabyMod",
            "CheatBreaker",
            "PvPLounge",
            "Geyser");

    @EventTarget
    public void onPacket(EventPacket event) {
        Packet<?> packet = event.getPacket();
        if (packet instanceof C17PacketCustomPayload) {
            C17PacketCustomPayload c17PacketCustomPayload = (C17PacketCustomPayload) packet;
            if (!c17PacketCustomPayload.getChannelName().equals("MC|Brand")) return;
            event.setCancelled();
            switch (modeValue.getValue()) {
                case "Vanilla": {
                    PacketUtil.sendPacketNoEvent(
                            new C17PacketCustomPayload(
                                    "MC|Brand",
                                    new PacketBuffer(Unpooled.buffer()).writeString("vanilla")
                            )
                    );
                    break;
                }
                case "OptiFine": {
                    PacketUtil.sendPacketNoEvent(
                            new C17PacketCustomPayload(
                                    "MC|Brand",
                                    new PacketBuffer(Unpooled.buffer()).writeString("optifine")
                            )
                    );
                    break;
                }
                case "Fabric": {
                    PacketUtil.sendPacketNoEvent(
                            new C17PacketCustomPayload(
                                    "MC|Brand",
                                    new PacketBuffer(Unpooled.buffer()).writeString("fabric")
                            )
                    );
                    break;
                }
                case "LabyMod": {
                    PacketUtil.sendPacketNoEvent(
                            new C17PacketCustomPayload(
                                    "MC|Brand",
                                    new PacketBuffer(Unpooled.buffer()).writeString("LMC")
                            )
                    );
                    break;
                }
                case "CheatBreaker": {
                    PacketUtil.sendPacketNoEvent(
                            new C17PacketCustomPayload(
                                    "MC|Brand",
                                    new PacketBuffer(Unpooled.buffer()).writeString("CB")
                            )
                    );
                    break;
                }
                case "PvPLounge": {
                    PacketUtil.sendPacketNoEvent(
                            new C17PacketCustomPayload(
                                    "MC|Brand",
                                    new PacketBuffer(Unpooled.buffer()).writeString("PLC18")
                            )
                    );
                    break;
                }
                case "Geyser": {
                    PacketUtil.sendPacketNoEvent(
                            new C17PacketCustomPayload(
                                    "MC|Brand",
                                    new PacketBuffer(Unpooled.buffer()).writeString("eyser")
                            )
                    );
                    break;
                }
                case "Lunar": {
                    PacketUtil.sendPacketNoEvent(
                            new C17PacketCustomPayload(
                                    "REGISTER",
                                    new PacketBuffer(Unpooled.buffer()).writeString("Lunar-Client")
                            )
                    );
                }
            }
        }
    }
}
