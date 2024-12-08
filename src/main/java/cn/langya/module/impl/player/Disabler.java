package cn.langya.module.impl.player;

import cn.langya.event.annotations.EventTarget;
import cn.langya.event.events.EventMotion;
import cn.langya.event.events.EventPacket;
import cn.langya.event.events.EventWorldLoad;
import cn.langya.module.Category;
import cn.langya.module.Module;
import cn.langya.utils.ChatUtil;
import cn.langya.utils.HypixelUtil;
import cn.langya.utils.MoveUtil;
import cn.langya.value.impl.ModeValue;
import net.minecraft.network.play.server.S08PacketPlayerPosLook;

/**
 * @author LangYa
 * @since 2024/12/6 14:38
 */
public class Disabler extends Module {
    public Disabler() {
        super(Category.Player);
    }

    private static final ModeValue modeValue = new ModeValue("Mode","Hypixel","Hypixel");

    private static boolean isFinished = false;
    private int flagged;
    private int offGroundTicks = 0;
    private boolean canRun;

    public static boolean isDisabled() {
        return isFinished && modeValue.isMode("Hypixel");
    }

    @Override
    public String getSuffix() {
        return modeValue.getValue();
    }

    @EventTarget
    public void onPreMotion(EventMotion event) {
        if (!event.isPre()) return;
        if (isFinished || mc.thePlayer.ticksExisted < 20) return;
        canRun = HypixelUtil.isLobby();
        if (!canRun) return;
        if (mc.thePlayer.onGround) {
            if (!MoveUtil.jumpDown())
                mc.thePlayer.jump();
        } else if (offGroundTicks >= 9) {
            if (offGroundTicks % 2 == 0) {
                event.setZ(event.getZ() + MoveUtil.randomizeDouble(0.09, 0.12));  // 0.095
            }

            mc.thePlayer.motionX = mc.thePlayer.motionY = mc.thePlayer.motionZ = 0.0;
        }
    }

    @EventTarget
    public void onReceivePacket(EventPacket event) {
        if (!canRun) return;
        if (event.getPacket() instanceof S08PacketPlayerPosLook && !isFinished) {
            flagged++;
            if (this.flagged == 20) {
                isFinished = true;
                flagged = 0;
                ChatUtil.log("WatchDog Motion is disable.");
                isFinished = true;
                canRun = false;
            }
        }
    }

    @EventTarget
    public void onWorldChange(EventWorldLoad event) {
        isFinished = false;
        this.flagged = 0;
        canRun = false;
    }

    @Override
    public void onDisable() {
        isFinished = false;
        offGroundTicks = 0;
        canRun = false;
    }
}
