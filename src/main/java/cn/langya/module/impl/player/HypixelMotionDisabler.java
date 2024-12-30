package cn.langya.module.impl.player;

import cn.langya.Client;
import cn.langya.event.annotations.EventTarget;
import cn.langya.event.events.EventMotion;
import cn.langya.event.events.EventPacket;
import cn.langya.event.events.EventRender2D;
import cn.langya.event.events.EventWorldLoad;
import cn.langya.module.Category;
import cn.langya.module.Module;
import cn.langya.ui.font.FontManager;
import cn.langya.ui.notification.NotificationType;
import cn.langya.utils.ChatUtil;
import cn.langya.utils.HypixelUtil;
import cn.langya.utils.MoveUtil;
import cn.langya.utils.RotationUtil;
import cn.langya.value.impl.BooleanValue;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.network.play.server.S08PacketPlayerPosLook;

public class HypixelMotionDisabler extends Module {
    private static boolean isFinished = false;
    private final BooleanValue sprint = new BooleanValue("Sprint",true);
    private final BooleanValue lobbyCheck = new BooleanValue("Lobby check", true);
    private final BooleanValue cFontValue = new BooleanValue("ClientFont",true);
    private final BooleanValue debug = new BooleanValue("Dev Debug",true);
    private final FontRenderer fr = cFontValue.getValue() ? FontManager.hanYi() : mc.fontRendererObj;
    private int flagged;

    public HypixelMotionDisabler() {
        super(Category.Player);
    }

    public static boolean isDisabled() {
        if (!Client.getInstance().getModuleManager().isEnabled("HypixelMotionDisabler")) return false;
        return isFinished;
    }

    public static double randomizeDouble(double min, double max) {
        return Math.random() * (max - min) + min;
    }

    @EventTarget
    public void onPreMotion(EventMotion event) {
        if(debug.getValue()) ChatUtil.info("Disabler return 1");
        if (mc.isSingleplayer()) return;
        if(debug.getValue()) ChatUtil.info("Disabler return 2");
        if (!event.isPre()) return;
        if (sprint.getValue() && MoveUtil.getSpeedEffect() == 0 && !MoveUtil.canSprint(true)) mc.thePlayer.setSprinting(false);
        if(debug.getValue()) ChatUtil.info("Disabler return 3");
        if (isFinished) return;
        if(debug.getValue()) ChatUtil.info("Disabler return 4");
        if (mc.thePlayer.ticksExisted < 20) return;
        if(debug.getValue()) ChatUtil.info("Disabler return 5");
        if (lobbyCheck.getValue() && HypixelUtil.isLobby()) return;
        if(debug.getValue()) ChatUtil.info("Disabler return 6");
        if (mc.thePlayer.onGround) {
            if (!mc.gameSettings.keyBindJump.isKeyDown())
                mc.thePlayer.jump();
        } else if (MoveUtil.offGroundTicks >= 9) {
            if (MoveUtil.offGroundTicks % 2 == 0) {
                RotationUtil.setRotations(new float[]{(float) (mc.thePlayer.rotationYaw - (10) + (Math.random() - 0.5) * 3), mc.thePlayer.rotationPitch});
                event.setX(event.getX() + 0.095+Math.random() / 100);
            }
            MoveUtil.stop();
        }
    }

    @EventTarget
    public void onReceivePacket(EventPacket event) {
        if (mc.isSingleplayer()) return;
        if(debug.getValue()) ChatUtil.info("Disabler return 7");
        if (!event.isRev()) return;
        if(debug.getValue()) ChatUtil.info("Disabler return 8");
        if (event.getPacket() instanceof S08PacketPlayerPosLook && !isFinished) {
            if(debug.getValue()) ChatUtil.info("Disabler return 9");
            flagged++;
            if(debug.getValue()) ChatUtil.info("Disabler flagged " + flagged);

            if (this.flagged >= 24) {
                isFinished = true;
                flagged = 0;
                Client.getInstance().getNotificationManager().post("WatchDog Motion is disable.", NotificationType.INFO);
                isFinished = true;
            }
        }
    }

    @EventTarget
    public void onWorldChange(EventWorldLoad event) {
        isFinished = false;
        this.flagged = 0;
    }

    @EventTarget
    public void onRender2D(EventRender2D event) {
        if (mc.isSingleplayer()) return;
        if (lobbyCheck.getValue() && HypixelUtil.isLobby()) return;
        if (!isFinished) {
            String displayText = "Disabler " + (flagged * 5) + "%";
            fr.drawStringWithShadow(displayText, (event.getScaledresolution().getScaledWidth() / 2F) - (fr.getStringWidth(displayText) / 2f), event.getScaledresolution().getScaledHeight() / 2F, -1);
        }
    }

    @Override
    public void onEnable() {
        onWorldChange(null);
    }

    @Override
    public void onDisable() {
        onWorldChange(null);
    }
}