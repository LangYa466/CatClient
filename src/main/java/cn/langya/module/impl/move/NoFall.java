/*
 * MoonLight Hacked Client
 *
 * A free and open-source hacked client for Minecraft.
 * Developed using Minecraft's resources.
 *
 * Repository: https://github.com/randomguy3725/MoonLight
 *
 * Author(s): [Randumbguy & opZywl & lucas]
 */
package cn.langya.module.impl.move;

import cn.langya.event.annotations.EventTarget;
import cn.langya.event.events.EventMotion;
import cn.langya.event.events.EventRender2D;
import cn.langya.module.Category;
import cn.langya.module.Module;
import cn.langya.utils.BlinkComponent;
import cn.langya.utils.MoveUtil;
import cn.langya.utils.PlayerUtil;
import cn.langya.value.impl.ModeValue;
import cn.langya.value.impl.NumberValue;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.network.play.client.C03PacketPlayer;

public class NoFall extends Module {
    public NoFall() {
        super(Category.Move);
    }

    public final ModeValue mode = new ModeValue("Mode", "NoGround", "Extra", "Blink", "Watchdog", "NoGround");
    public final NumberValue minDistance = new NumberValue("Min Distance", 3, 8, 0, 1) {
        @Override
        public boolean isHide() {
            return !mode.isMode("NoGround");
        }
    };

    private boolean blinked = false;
    private boolean prevOnGround = false;
    private double fallDistance = 0;
    private boolean timed = false;

    @Override
    public String getSuffix() {
        return mode.getValue();
    }

    @Override
    public void onEnable() {
        if (nullCheck()) this.fallDistance = mc.thePlayer.fallDistance;
    }

    @Override
    public void onDisable() {
        if (blinked) {
            BlinkComponent.dispatch();
            blinked = false;
        }
        mc.timer.timerSpeed = 1f;
    }

    @EventTarget
    public void onMotion(EventMotion event) {
        if (!nullCheck()) return;

        if (event.isPost()) return;

        if (mc.thePlayer.onGround)
            fallDistance = 0;
        else {
            fallDistance += (float) Math.max(mc.thePlayer.lastTickPosY - event.getY(), 0);

            fallDistance -= MoveUtil.predictedMotionY(mc.thePlayer.motionY, 1);
        }

        if (mc.thePlayer.capabilities.allowFlying) return;
        if (isVoid()) {
            if (blinked) {
                BlinkComponent.dispatch();
                blinked = false;
            }
            return;
        }

        switch (mode.getValue()) {
            case "NoGround":
                event.setOnGround(false);
                break;

            case "Extra":
                float extra$fallDistance = mc.thePlayer.fallDistance;
                mc.timer.timerSpeed = 1f;
                if (extra$fallDistance - mc.thePlayer.motionY > minDistance.getValue()) {
                    mc.timer.timerSpeed = 0.5f;
                    mc.getNetHandler().addToSendQueue(new C03PacketPlayer(true));
                    mc.thePlayer.fallDistance = 0;
                }
                break;

            case "Watchdog":
                if (fallDistance >= minDistance.getValue()) {
                    mc.timer.timerSpeed = (float) 0.5;
                    timed = true;
                    mc.getNetHandler().addToSendQueue(new C03PacketPlayer(true));
                    fallDistance = 0;
                } else if (timed) {
                    mc.timer.timerSpeed = 1;
                    timed = false;
                }
                break;
            case "Blink":
                if (mc.thePlayer.onGround) {
                    if (blinked) {
                        BlinkComponent.dispatch();
                        blinked = false;
                    }

                    this.prevOnGround = true;
                } else if (this.prevOnGround) {
                    if (shouldBlink()) {
                        if (!BlinkComponent.blinking)
                            BlinkComponent.blinking = true;
                        blinked = true;
                    }

                    prevOnGround = false;
                } else if (PlayerUtil.isBlockUnder() && BlinkComponent.blinking && (this.fallDistance - mc.thePlayer.motionY) >= minDistance.getValue()) {
                    mc.thePlayer.sendQueue.addToSendQueue(new C03PacketPlayer(true));
                    this.fallDistance = 0.0F;
                }
                break;
        }
    }

    @EventTarget
    public void onRender2D(EventRender2D event) {
        ScaledResolution sr = new ScaledResolution(mc);
        if (mode.isMode("Blink")) {
            if (blinked)
                mc.fontRendererObj.drawStringWithShadow("Blinking: " + BlinkComponent.packets.size(), (float) sr.getScaledWidth() / 2.0F - (float) mc.fontRendererObj.getStringWidth("Blinking: " + BlinkComponent.packets.size()) / 2.0F, (float) sr.getScaledHeight() / 2.0F + 13.0F, -1);
        }
    }

    private boolean isVoid() {
        return PlayerUtil.overVoid(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ);
    }

    private boolean shouldBlink() {
        return !mc.thePlayer.onGround && !PlayerUtil.isBlockUnder((int) Math.floor(minDistance.getValue())) && PlayerUtil.isBlockUnder();
    }
}