package cn.langya.module.impl.combat;

import cn.langya.event.annotations.EventTarget;
import cn.langya.event.events.EventPacket;
import cn.langya.module.Category;
import cn.langya.module.Module;
import cn.langya.utils.MoveUtil;
import cn.langya.value.impl.BooleanValue;
import cn.langya.value.impl.ModeValue;
import cn.langya.value.impl.NumberValue;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S12PacketEntityVelocity;
import net.minecraft.network.play.server.S27PacketExplosion;

public class Velocity extends Module {

    private final ModeValue mode = new ModeValue("Mode", "Watchdog", "Packet", "Stack", "Watchdog");
    private final NumberValue horizontal = new NumberValue("Horizontal", 0, 100, 0, 1) {
        @Override
        public boolean isHide() {
            return mode.isMode("Packet");
        }
    };
    private final NumberValue vertical = new NumberValue("Vertical", 0, 100, 0, 1) {
        @Override
        public boolean isHide() {
            return mode.isMode("Packet");
        }
    };
    private final BooleanValue onlyWhileMoving = new BooleanValue("Only while moving", false);

    private boolean cancel;

    public Velocity() {
        super(Category.Combat);
    }

    @Override
    public String getSuffix() {
        return mode.getValue();
    }

    @EventTarget
    public void onPacketReceiveEvent(EventPacket e) {
        if (!e.isRev()) return;
        if ((onlyWhileMoving.getValue() && !MoveUtil.isMoving())) return;
        Packet<?> packet = e.getPacket();
        switch (mode.getValue()) {
            case "Packet":
                if (packet instanceof S12PacketEntityVelocity) {
                    S12PacketEntityVelocity s12 = (S12PacketEntityVelocity) e.getPacket();
                    if (mc.thePlayer != null && s12.getEntityID() == mc.thePlayer.getEntityId()) {
                        s12.motionX *= horizontal.getValue() / 100;
                        s12.motionZ *= horizontal.getValue() / 100;
                        s12.motionY *= vertical.getValue() / 100;
                    }
                } else if (packet instanceof S27PacketExplosion) {
                    S27PacketExplosion s27 = (S27PacketExplosion) e.getPacket();
                    s27.motionX *= horizontal.getValue() / 100F;
                    s27.motionZ *= horizontal.getValue() / 100F;
                    s27.motionY *= vertical.getValue() / 100F;
                }
                break;
            case "Watchdog":
                if (packet instanceof S12PacketEntityVelocity) {
                    S12PacketEntityVelocity s12 = (S12PacketEntityVelocity) e.getPacket();
                    if (mc.thePlayer != null && s12.getEntityID() == mc.thePlayer.getEntityId()) {
                        s12.motionX *= 0;
                        s12.motionZ *= 0;
                    }
                } else if (packet instanceof S27PacketExplosion) {
                    S27PacketExplosion s27 = (S27PacketExplosion) e.getPacket();
                    s27.motionX *= 0;
                    s27.motionZ *= 0;
                    s27.motionY *= 1;
                }
                break;
            case "Stack":
                if (packet instanceof S12PacketEntityVelocity) {
                    cancel = !cancel;
                    if (cancel) {
                        e.setCancelled();
                    }
                }
                if (packet instanceof S27PacketExplosion) {
                    e.setCancelled();
                }
                break;
        }
    }
}