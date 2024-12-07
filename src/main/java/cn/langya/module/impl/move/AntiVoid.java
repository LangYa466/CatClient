package cn.langya.module.impl.move;

import cn.langya.Client;
import cn.langya.event.annotations.EventTarget;
import cn.langya.event.events.EventUpdate;
import cn.langya.module.Category;
import cn.langya.module.Module;
import cn.langya.module.impl.player.Blink;
import cn.langya.module.impl.world.Scaffold;
import cn.langya.utils.MoveUtil;
import cn.langya.utils.PacketUtil;
import cn.langya.value.impl.BooleanValue;
import cn.langya.value.impl.NumberValue;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.util.Vec3;

public class AntiVoid extends Module {
    public final Blink blink = new Blink();
    private final NumberValue distance = new NumberValue("Distance", 5, 0, 10, 1);
    private final BooleanValue toggleScaffold = new BooleanValue("Toggle scaffold", false);
    private Vec3 position, motion;
    private boolean wasVoid, setBack;
    private int overVoidTicks;
    private boolean disabledForLongJump = false;

    public AntiVoid() {
        super(Category.Player);
    }

    @Override
    public String getSuffix() {
        return "Watchdog";
    }

    @EventTarget
    public void onUpdate(EventUpdate event) {
        if (mc.thePlayer.capabilities.allowFlying) return;
        if (mc.thePlayer.ticksExisted <= 50) return;

        if (disabledForLongJump && mc.thePlayer.onGround)
            disabledForLongJump = false;

      //  if (longJump.isEnabled()) disabledForLongJump = true;
        Scaffold scaffold = (Scaffold) Client.getInstance().getModuleManager().getModule("Scaffold");
        if (scaffold.isEnable() || disabledForLongJump) {
            blink.setEnable(false);
            return;
        }

        boolean overVoid = !mc.thePlayer.onGround && MoveUtil.overVoid();

        if (overVoid) {
            overVoidTicks++;
        } else if (mc.thePlayer.onGround) {
            overVoidTicks = 0;
        }

        if (overVoid && position != null && motion != null && overVoidTicks < 30 + distance.getValue() * 20) {
            if (!setBack) {
                wasVoid = true;

                blink.setEnable(true);

                if (mc.thePlayer.fallDistance > distance.getValue() || setBack) {
                    PacketUtil.sendPacketNoEvent(new C03PacketPlayer.C04PacketPlayerPosition(position.xCoord, position.yCoord - 0.1 - Math.random(), position.zCoord, false));
                    if (this.toggleScaffold.getValue()) {
                        scaffold.setEnable(true);
                    }

                    blink.blinkedPackets.clear();

                    mc.thePlayer.fallDistance = 0;

                    setBack = true;
                }
            } else {
                blink.setEnable(false);
            }
        } else {
            setBack = false;

            if (wasVoid) {
                blink.setEnable(false);
                wasVoid = false;
            }

            motion = new Vec3(mc.thePlayer.motionX, mc.thePlayer.motionY, mc.thePlayer.motionZ);
            position = new Vec3(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ);
        }
    }
}