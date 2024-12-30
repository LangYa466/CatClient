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
import cn.langya.event.events.*;
import cn.langya.module.Category;
import cn.langya.module.Module;
import cn.langya.ui.font.FontManager;
import cn.langya.utils.MoveUtil;
import cn.langya.value.impl.BooleanValue;
import cn.langya.value.impl.ModeValue;
import cn.langya.value.impl.NumberValue;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.item.ItemFireball;
import net.minecraft.item.ItemStack;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraft.network.play.server.S12PacketEntityVelocity;
import org.apache.commons.lang3.Range;

import java.util.Objects;

import static cn.langya.utils.PacketUtil.sendPacket;
import static cn.langya.utils.PacketUtil.sendPacketNoEvent;

public class LongJump extends Module {

    public LongJump() {
        super(Category.Move);
    }

    public final ModeValue mode = new ModeValue("Mode","Watchdog Fireball", "Old Matrix", "Miniblox", "Watchdog Fireball");
    public final ModeValue wdFBMode = new ModeValue("Fireball Mode", "Rise", "Chef", "Chef High", "Rise");
    private final NumberValue oMatrixTimer = new NumberValue("Matrix Timer", 0.3f, 0.1f, 1, 0.01f) {
        @Override
        public boolean isHide() {
            return mode.isMode("Old Matrix");
        }
    };
    private final BooleanValue boost = new BooleanValue("Boost", true) {
        @Override
        public boolean isHide() {
            return  mode.isMode("Watchdog Fireball");
        }
    };
    private int lastSlot = -1;
    //fb
    private int ticks = -1;
    private boolean setSpeed;
    private int ticksSinceVelocity;
    public static boolean stopModules;
    private boolean sentPlace;
    private int initTicks;
    private boolean thrown;
    private boolean velo;

    //matrix
    private boolean mPacket;
    private int matrixTimer = 0;

    //miniblox
    private boolean jumped;
    private int currentTimer = 0;
    private int pauseTimes = 0;
    private int activeTicks = 0;

    //others
    private double distance;

    @Override
    public void onEnable() {
        lastSlot = mc.thePlayer.inventory.currentItem;
        ticks = 0;
        distance = 0;
        if (mode.isMode("Watchdog Fireball")) {
            int fbSlot = getFBSlot();
            if (fbSlot == -1) {
                toggle();
            }

            stopModules = true;
            initTicks = 0;
        }
    }

    @Override
    public void onDisable() {
        if (Objects.equals(mode.getValue(), "Watchdog Fireball")) {
            if (lastSlot != -1) {
                mc.thePlayer.inventory.currentItem = lastSlot;
            }


            ticks = lastSlot = -1;
            setSpeed = stopModules = sentPlace = false;
            initTicks = 0;
            ticksSinceVelocity = 0;
            velo = false;
        }

        if (Objects.equals(mode.getValue(), "Old Matrix")) {
            mPacket = false;
            matrixTimer = 0;
            mc.timer.timerSpeed = 1f;
        }

        if (Objects.equals(mode.getValue(), "Miniblox")) {
            jumped = false;
            currentTimer = 0;
            pauseTimes = 0;
            activeTicks = 0;
            MoveUtil.stop();
        }
    }

    @Override
    public String getSuffix() {
        return mode.getValue();
    }

    @EventTarget
    public void onUpdate(EventUpdate event) {
        if (velo)
            ticksSinceVelocity++;
        switch (mode.getValue()) {
            case "Old Matrix":
                if (!mPacket) {
                    if (mc.thePlayer.onGround)
                        mc.thePlayer.jump();
                    sendPacketNoEvent(new C03PacketPlayer(false));
                    mPacket = true;
                }
                if (mPacket) {
                    mc.timer.timerSpeed = oMatrixTimer.getValue();
                    mc.thePlayer.motionX = 1.97 * -Math.sin(MoveUtil.getDirection());
                    mc.thePlayer.motionZ = 1.97 * Math.cos(MoveUtil.getDirection());
                    mc.thePlayer.motionY = 0.42;
                    matrixTimer++;

                    if (matrixTimer >= 3) {
                        toggle();
                    }
                }
                break;

            case "Miniblox":
                KeyBinding.setKeyBindState(mc.gameSettings.keyBindForward.getKeyCode(), false);
                KeyBinding.setKeyBindState(mc.gameSettings.keyBindBack.getKeyCode(), false);
                KeyBinding.setKeyBindState(mc.gameSettings.keyBindLeft.getKeyCode(), false);
                KeyBinding.setKeyBindState(mc.gameSettings.keyBindRight.getKeyCode(), false);
                activeTicks++;

                if (activeTicks <= 10) {
                    MoveUtil.stop();
                } else {
                    if (!jumped) {
                        if (mc.thePlayer.onGround) {
                            MoveUtil.stop();
                            mc.thePlayer.jump();
                        }

                        jumped = true;
                    } else {
                        int maxTimer = 0;

                        switch (pauseTimes) {
                            case 0:
                                mc.thePlayer.motionX = 1.9 * -Math.sin(MoveUtil.getDirection());
                                mc.thePlayer.motionZ = 1.9 * Math.cos(MoveUtil.getDirection());
                                maxTimer = 10;
                                break;
                            case 1:
                                mc.thePlayer.motionX = 1.285 * -Math.sin(MoveUtil.getDirection());
                                mc.thePlayer.motionZ = 1.285 * Math.cos(MoveUtil.getDirection());
                                maxTimer = 15;
                                break;
                            case 2:
                                mc.thePlayer.motionX = 1.1625 * -Math.sin(MoveUtil.getDirection());
                                mc.thePlayer.motionZ = 1.1625 * Math.cos(MoveUtil.getDirection());
                                maxTimer = 5;
                                break;
                        }

                        mc.thePlayer.motionY = 0.29;
                        currentTimer++;

                        if (Range.between(4, maxTimer).contains(currentTimer)) {
                            MoveUtil.stop();
                        } else if (currentTimer > maxTimer) {
                            pauseTimes++;
                            currentTimer = 0;
                            jumped = false;
                        }
                    }

                    if (pauseTimes >= 3) {
                        MoveUtil.stop();
                        toggle();
                    }
                    break;
                }
        }
    }

    @EventTarget
    public void onMotion(EventMotion event) {

        if (event.isPost()) {
            distance += Math.hypot(mc.thePlayer.posX - mc.thePlayer.lastTickPosX, mc.thePlayer.posZ - mc.thePlayer.lastTickPosZ);
        }
        switch (mode.getValue()) {
            case "Watchdog Fireball":
                if (event.isPre()) {

                    if (velo && mc.thePlayer.onGround) {
                        toggle();
                    }

                    switch (wdFBMode.getValue()) {
                        case "Rise":
                            if (mc.thePlayer.hurtTime == 10) {
                                mc.thePlayer.motionY = 1.1f;
                            }

                            if (ticksSinceVelocity <= 80 && ticksSinceVelocity >= 1) {
                                mc.thePlayer.motionY += 0.028f;
                            }

                            if (ticksSinceVelocity == 28) {
                                if (boost.getValue()) {
                                    MoveUtil.strafe(0.42);
                                }
                                mc.thePlayer.motionY = 0.16f;
                            }
                            if (ticksSinceVelocity >= 35 && ticksSinceVelocity <= 50) {
                                MoveUtil.strafe();
                                mc.thePlayer.posY = mc.thePlayer.posY + .029f;

                            }

                            if (ticksSinceVelocity >= 3 && ticksSinceVelocity <= 50) {
                                MoveUtil.strafe();
                            }
                            break;
                        case "Chef":
                            if (velo) {
                                if (ticksSinceVelocity >= 1 && ticksSinceVelocity <= 33) {
                                    mc.thePlayer.motionY = 0.7 - ticksSinceVelocity * 0.015;
                                }
                            }
                            break;
                        case "Chef high":
                            if (velo) {
                                if (ticksSinceVelocity >= 1 && ticksSinceVelocity <= 28) {
                                    mc.thePlayer.motionY = ticksSinceVelocity * 0.016;
                                }
                            }
                            break;
                    }

                    if (initTicks == 0) {

                        event.setYaw(mc.thePlayer.rotationYaw - 180);
                        event.setPitch(89);
                        int fireballSlot = getFBSlot();
                        if (fireballSlot != -1 && fireballSlot != mc.thePlayer.inventory.currentItem) {
                            mc.thePlayer.inventory.currentItem = fireballSlot;
                        }
                    }
                    if (initTicks == 1) {

                        if (!sentPlace) {
                            sendPacket(new C08PacketPlayerBlockPlacement(mc.thePlayer.getHeldItem()));
                            sentPlace = true;

                        }
                    } else if (initTicks == 2) {

                        if (lastSlot != -1) {
                            mc.thePlayer.inventory.currentItem = lastSlot;
                            lastSlot = -1;
                        }
                    }
                    if (setSpeed) {

                        stopModules = true;
                        MoveUtil.strafe(1.768f);
                        ticks++;
                    }
                    if (initTicks < 3) {
                        initTicks++;
                    }

                    if (setSpeed) {
                        if (ticks > 1) {
                            stopModules = setSpeed = false;
                            ticks = 0;
                            return;
                        }
                        stopModules = true;
                        ticks++;
                        MoveUtil.strafe(1.768f);
                    }
                }

                break;
        }
    }

    @EventTarget
    public void onStrafe(EventStrafe event) {
        switch (mode.getValue()) {

            case "Watchdog Fireball":
                if (ticksSinceVelocity <= 70 && ticksSinceVelocity >= 1) {
                    mc.thePlayer.motionX *= 1.0003;
                    mc.thePlayer.motionZ *= 1.0003;
                }

                if (ticksSinceVelocity == 1) {
                    mc.thePlayer.motionX *= 1.15;
                    mc.thePlayer.motionZ *= 1.15;
                }


                if (mc.thePlayer.hurtTime == 8) {
                    mc.thePlayer.motionX *= 1.02;
                    mc.thePlayer.motionZ *= 1.02;
                }

                if (mc.thePlayer.hurtTime == 7) {
                    mc.thePlayer.motionX *= 1.0004;
                    mc.thePlayer.motionZ *= 1.0004;
                }

                if (mc.thePlayer.hurtTime == 6) {
                    mc.thePlayer.motionX *= 1.0004;
                    mc.thePlayer.motionZ *= 1.0004;
                }

                if (mc.thePlayer.hurtTime == 5) {
                    mc.thePlayer.motionX *= 1.0004;
                    mc.thePlayer.motionZ *= 1.0004;
                }

                if (mc.thePlayer.hurtTime <= 4 && !(mc.thePlayer.hurtTime == 0)) {
                    mc.thePlayer.motionX *= 1.0004;
                    mc.thePlayer.motionZ *= 1.0004;
                }
                break;
        }
    }

    @EventTarget
    public void onPacket(EventPacket event) {
        Packet<?> packet = event.getPacket();

        if (mode.isMode("Watchdog Fireball")) {
            if (packet instanceof C08PacketPlayerBlockPlacement) {
                C08PacketPlayerBlockPlacement c08PacketPlayerBlockPlacement = (C08PacketPlayerBlockPlacement) packet;
                if (c08PacketPlayerBlockPlacement.getStack() != null && c08PacketPlayerBlockPlacement.getStack().getItem() instanceof ItemFireball) {
                    thrown = true;
                    if (mc.thePlayer.onGround) {
                        mc.thePlayer.jump();
                    }
                }
            }

            if (packet instanceof S12PacketEntityVelocity) {
                S12PacketEntityVelocity s12PacketEntityVelocity = (S12PacketEntityVelocity) packet;
                if (s12PacketEntityVelocity.getEntityID() == mc.thePlayer.getEntityId()) {
                    if (thrown) {
                        ticksSinceVelocity = 0;
                        ticks = 0;
                        setSpeed = true;
                        thrown = false;
                        stopModules = true;
                        velo = true;
                    }
                }
            }
        }
    }

    @EventTarget
    public void onRender2D(EventRender2D event) {
        FontManager.hanYi(15).drawCenteredString((Math.round(distance * 100.0) / 100.0) + "blocks", (float) event.getScaledresolution().getScaledWidth() / 2, (float) event.getScaledresolution().getScaledHeight() / 2 - 30, -1);
    }

    @EventTarget
    public void onWorld(EventWorldLoad event) {
        setEnabled(false);
    }

    private int getFBSlot() {
        for (int i = 36; i <= 44; i++) {
            ItemStack stack = mc.thePlayer.inventoryContainer.getSlot(i).getStack();
            if (stack != null && stack.getItem() instanceof ItemFireball) {
                return i - 36;
            }
        }
        return -1;
    }
}