package cn.langya.module.impl.world;

import cn.langya.event.annotations.EventTarget;
import cn.langya.event.events.EventMotion;
import cn.langya.event.events.EventPacket;
import cn.langya.event.events.EventSafeWalk;
import cn.langya.event.events.EventTick;
import cn.langya.module.Category;
import cn.langya.module.Module;
import cn.langya.utils.*;
import cn.langya.value.impl.BooleanValue;
import cn.langya.value.impl.ModeValue;
import cn.langya.value.impl.NumberValue;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.init.Blocks;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.network.play.client.C09PacketHeldItemChange;
import net.minecraft.network.play.client.C0APacketAnimation;
import net.minecraft.network.play.client.C0BPacketEntityAction;
import net.minecraft.potion.Potion;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MouseFilter;

public class Scaffold extends Module {

    private final ModeValue countMode = new ModeValue("Block Counter", "Tenacity", "None", "Tenacity", "Basic", "Polar");
    private final BooleanValue rotations = new BooleanValue("Rotations", true);
    private final ModeValue rotationMode = new ModeValue("Rotation Mode", "Watchdog", "Watchdog", "NCP", "Back", "45", "Enum", "Down", "0");
    private final ModeValue placeType = new ModeValue("Place Type", "Post", "Pre", "Post", "Dynamic");
    public static ModeValue keepYMode = new ModeValue("Keep Y Mode", "Always", "Always", "Speed toggled");
    public static ModeValue sprintMode = new ModeValue("Sprint Mode", "Vanilla", "Vanilla", "Watchdog", "Cancel");
    public static ModeValue towerMode = new ModeValue("Tower Mode", "Watchdog", "Vanilla", "NCP", "Watchdog", "Verus");
    public static ModeValue swingMode = new ModeValue("Swing Mode", "Client", "Client", "Silent");
    public static NumberValue delay = new NumberValue("Delay", 0, 2, 0, 0.05F);
    //public static NumberValue extend = new NumberValue("Extend", 0, 6, 0, 0.05);
    private final NumberValue timer = new NumberValue("Timer", 1, 5, 0.1F, 0.1F);
    public static final BooleanValue auto3rdPerson = new BooleanValue("Auto 3rd Person", false);
    public static final BooleanValue speedSlowdown = new BooleanValue("Speed Slowdown", true);
    public static final NumberValue speedSlowdownAmount = new NumberValue("Slowdown Amount", 0.1F, 0.2F, 0.01F, 0.01F);
    public static final BooleanValue itemSpoof = new BooleanValue("Item Spoof", false);
    public static final BooleanValue downwards = new BooleanValue("Downwards", false);
    public static final BooleanValue safewalk = new BooleanValue("Safewalk", false);
    public static final BooleanValue sprint = new BooleanValue("Sprint", false);
    private final BooleanValue sneak = new BooleanValue("Sneak", false);
    public static final BooleanValue tower = new BooleanValue("Tower", false);
    private final NumberValue towerTimer = new NumberValue("Tower Timer Boost", 1.2F, 5F, 0.1F, 0.1F);
    private final BooleanValue swing = new BooleanValue("Swing", true);
    private final BooleanValue autoJump = new BooleanValue("Auto Jump", false);
    private final BooleanValue hideJump = new BooleanValue("Hide Jump", false);
    private final BooleanValue baseSpeed = new BooleanValue("Base Speed", false);
    public static BooleanValue keepY = new BooleanValue("Keep Y", false);
    private ScaffoldUtil.BlockCache blockCache, lastBlockCache;
    private float y;
    private float speed;
    private final MouseFilter pitchMouseFilter = new MouseFilter();
    private final TimerUtil delayTimer = new TimerUtil();
    private final TimerUtil timerUtil = new TimerUtil();
    public static double keepYCoord;
    private boolean shouldSendPacket;
    private boolean shouldTower;
    private boolean firstJump;
    private boolean pre;
    private int jumpTimer;
    private int slot;
    private int prevSlot;
    private float[] cachedRots = new float[2];
    
    public Scaffold() {
        super(Category.World);
    }

    @EventTarget
    public void onMotionEvent(EventMotion e) {
        // Timer Stuff
        if (!mc.gameSettings.keyBindJump.isKeyDown()) {
            mc.timer.timerSpeed = timer.getValue();
        } else {
            mc.timer.timerSpeed = tower.getValue() ? towerTimer.getValue() : 1;
        }

        if (e.isPre()) {
            // Auto Jump
            if (baseSpeed.getValue()) {
                MoveUtil.setSpeed(MoveUtil.getBaseMoveSpeed() * 0.7);
            }
            if (autoJump.getValue() && mc.thePlayer.onGround && MoveUtil.isMoving() && !mc.gameSettings.keyBindJump.isKeyDown()) {
                mc.thePlayer.jump();
            }

            if (sprint.getValue() && sprintMode.isMode("Watchdog") && mc.thePlayer.onGround && MoveUtil.isMoving() && !mc.gameSettings.keyBindJump.isKeyDown() && !isDownwards() && mc.thePlayer.isSprinting()) {
                final double[] offset = RotationUtil.yawPos(mc.thePlayer.getDirection(), MoveUtil.getSpeed() / 2);
                mc.thePlayer.sendQueue.addToSendQueue(new C03PacketPlayer.C04PacketPlayerPosition(mc.thePlayer.posX - offset[0], mc.thePlayer.posY, mc.thePlayer.posZ - offset[1], true));
            }

            if (sprint.getValue() && sprintMode.isMode("None")) {
                mc.thePlayer.setSprinting(false);
            }

            // Rotations
            if (rotations.getValue()) {
                float[] rotations = new float[]{0, 0};
                switch (rotationMode.getValue()) {
                    case "Watchdog":
                        rotations = new float[]{MoveUtil.getMoveYaw(e.getYaw()) - 180, y};
                        e.setRotations(rotations);
                        break;
                    case "NCP":
                        float prevYaw = cachedRots[0];
                        if ((blockCache = ScaffoldUtil.getBlockInfo()) == null) {
                            blockCache = lastBlockCache;
                        }
                        if (blockCache != null && (mc.thePlayer.ticksExisted % 3 == 0
                                || mc.theWorld.getBlockState(new BlockPos(e.getX(), ScaffoldUtil.getYLevel(), e.getZ())).getBlock() == Blocks.air)) {
                            cachedRots = RotationUtil.getRotations(blockCache.getPosition(), blockCache.getFacing());
                        }
                        if ((mc.thePlayer.onGround || (MoveUtil.isMoving() && tower.getValue() && mc.gameSettings.keyBindJump.isKeyDown())) && Math.abs(cachedRots[0] - prevYaw) >= 90) {
                            cachedRots[0] = MoveUtil.getMoveYaw(e.getYaw()) - 180;
                        }
                        rotations = cachedRots;
                        e.setRotations(rotations);
                        break;
                    case "Back":
                        rotations = new float[]{MoveUtil.getMoveYaw(e.getYaw()) - 180, 77};
                        e.setRotations(rotations);
                        break;
                    case "Down":
                        e.setPitch(90);
                        break;
                    case "45":
                        float val;
                        if (MoveUtil.isMoving()) {
                            float f = MoveUtil.getMoveYaw(e.getYaw()) - 180;
                            float[] numbers = new float[]{-135, -90, -45, 0, 45, 90, 135, 180};
                            float lastDiff = 999;
                            val = f;
                            for (float v : numbers) {
                                float diff = Math.abs(v - f);
                                if (diff < lastDiff) {
                                    lastDiff = diff;
                                    val = v;
                                }
                            }
                        } else {
                            val = rotations[0];
                        }
                        rotations = new float[]{
                                (val + MathHelper.wrapAngleTo180_float(mc.thePlayer.prevRotationYawHead)) / 2.0F,
                                (77 + MathHelper.wrapAngleTo180_float(mc.thePlayer.prevRotationPitchHead)) / 2.0F};
                        e.setRotations(rotations);
                        break;
                    case "Enum":
                        if (lastBlockCache != null) {
                            float yaw = RotationUtil.getEnumRotations(lastBlockCache.getFacing());
                            e.setRotations(new float[] {yaw, 77});
                        } else {
                            e.setRotations(new float[] {mc.thePlayer.rotationYaw + 180, 77});
                        }
                        break;
                    case "0":
                        e.setRotations(new float[] {0,0});
                        break;
                }
            }

            // Speed 2 Slowdown
            if (speedSlowdown.getValue() && mc.thePlayer.isPotionActive(Potion.moveSpeed) && !mc.gameSettings.keyBindJump.isKeyDown() && mc.thePlayer.onGround) {
                MoveUtil.setSpeed(speedSlowdownAmount.getValue());
            }

            if (sneak.getValue()) KeyBinding.setKeyBindState(mc.gameSettings.keyBindSneak.getKeyCode(), true);

            // Save ground Y level for keep Y
            if (mc.thePlayer.onGround) {
                keepYCoord = Math.floor(mc.thePlayer.posY - 1.0);
            }

            if (tower.getValue() && mc.gameSettings.keyBindJump.isKeyDown()) {
                double centerX = Math.floor(e.getX()) + 0.5, centerZ = Math.floor(e.getZ()) + 0.5;
                switch (towerMode.getValue()) {
                    case "Vanilla":
                        mc.thePlayer.motionY = 0.42f;
                        break;
                    case "Verus":
                        if (mc.thePlayer.ticksExisted % 2 == 0)
                            mc.thePlayer.motionY = 0.42f;
                        break;
                    case "Watchdog":
                        if (!MoveUtil.isMoving() && mc.theWorld.getBlockState(new BlockPos(mc.thePlayer).down()).getBlock() != Blocks.air && lastBlockCache != null) {
                            if (mc.thePlayer.ticksExisted % 6 == 0) {
                                e.setX(centerX + 0.1);
                                e.setZ(centerZ + 0.1);
                            } else {
                                e.setX(centerX - 0.1);
                                e.setZ(centerZ - 0.1);
                            }
                            MoveUtil.setSpeed(0);
                        }

                        mc.thePlayer.motionY = 0.3;
                        e.setOnGround(true);
                        break;
                    case "NCP":
                        if (!MoveUtil.isMoving() || MoveUtil.getSpeed() < 0.16) {
                            if (mc.thePlayer.onGround) {
                                mc.thePlayer.motionY = 0.42;
                            } else if (mc.thePlayer.motionY < 0.23) {
                                mc.thePlayer.setPosition(mc.thePlayer.posX, (int) mc.thePlayer.posY, mc.thePlayer.posZ);
                                mc.thePlayer.motionY = 0.42;
                            }
                        }
                        break;
                }
            }

            // Setting Block Cache
            blockCache = ScaffoldUtil.getBlockInfo();
            if (blockCache != null) {
                lastBlockCache = ScaffoldUtil.getBlockInfo();
            } else {
                return;
            }

            if (mc.thePlayer.ticksExisted % 4 == 0) {
                pre = true;
            }

            // Placing Blocks (Pre)
            if (placeType.isMode("Pre") || (placeType.isMode("Dynamic") && pre)) {
                if (place()) {
                    pre = false;
                }
            }
        } else {
            // Setting Item Slot
            if (!itemSpoof.getValue()) {
                mc.thePlayer.inventory.currentItem = slot;
            }

            // Placing Blocks (Post)
            if (placeType.isMode("Post") || (placeType.isMode("Dynamic") && !pre)) {
                place();
            }

            pre = false;
        }
    }

    private boolean place() {
        int slot = ScaffoldUtil.getBlockSlot();
        if (blockCache == null || lastBlockCache == null || slot == -1) return false;

        if (this.slot != slot) {
            this.slot = slot;
            PacketUtil.sendPacketNoEvent(new C09PacketHeldItemChange(this.slot));
        }

        boolean placed = false;
        if (delayTimer.hasReached(delay.getValue().intValue() * 1000)) {
            firstJump = false;
            if (mc.playerController.onPlayerRightClick(mc.thePlayer, mc.theWorld,
                    mc.thePlayer.inventory.getStackInSlot(this.slot),
                    lastBlockCache.getPosition(), lastBlockCache.getFacing(),
                    ScaffoldUtil.getHypixelVec3(lastBlockCache))) {
                placed = true;
                y = MathUtil.getRandomInRange(79.5f, 83.5f);
                if (swing.getValue()) {
                    if (swingMode.isMode("Client")) {
                        mc.thePlayer.swingItem();
                    } else {
                        PacketUtil.sendPacket(new C0APacketAnimation());
                    }
                }
            }
            delayTimer.reset();
            blockCache = null;
        }
        return placed;
    }

    @EventTarget
    public void onTickEvent(EventTick event) {
        if (mc.thePlayer == null) return;
        if (hideJump.getValue() && !mc.gameSettings.keyBindJump.isKeyDown() && MoveUtil.isMoving() && !mc.thePlayer.onGround && autoJump.getValue()) {
            mc.thePlayer.posY -= mc.thePlayer.posY - mc.thePlayer.lastTickPosY;
            mc.thePlayer.lastTickPosY -= mc.thePlayer.posY - mc.thePlayer.lastTickPosY;
            mc.thePlayer.cameraYaw = mc.thePlayer.cameraPitch = 0.1F;
        }
        if (downwards.getValue()) {
            KeyBinding.setKeyBindState(mc.gameSettings.keyBindSneak.getKeyCode(), false);
            mc.thePlayer.movementInput.sneak = false;
        }
    }

    @Override
    public void onDisable() {
        if (mc.thePlayer != null) {
            if (!itemSpoof.getValue()) mc.thePlayer.inventory.currentItem = prevSlot;
            if (slot != mc.thePlayer.inventory.currentItem && itemSpoof.getValue())
                PacketUtil.sendPacketNoEvent(new C09PacketHeldItemChange(mc.thePlayer.inventory.currentItem));

            if (auto3rdPerson.getValue()) {
                mc.gameSettings.thirdPersonView = 0;
            }
            if (mc.thePlayer.isSneaking() && sneak.getValue())
                KeyBinding.setKeyBindState(mc.gameSettings.keyBindSneak.getKeyCode(), GameSettings.isKeyDown(mc.gameSettings.keyBindSneak));
        }
        mc.timer.timerSpeed = 1;
        super.onDisable();
    }

    @Override
    public void onEnable() {
        lastBlockCache = null;
        if (mc.thePlayer != null) {
            prevSlot = mc.thePlayer.inventory.currentItem;
            slot = mc.thePlayer.inventory.currentItem;
            if (mc.thePlayer.isSprinting() && sprint.getValue() && sprintMode.isMode("Cancel")) {
                PacketUtil.sendPacketNoEvent(new C0BPacketEntityAction(mc.thePlayer, C0BPacketEntityAction.Action.STOP_SPRINTING));
            }
            if (auto3rdPerson.getValue()) {
                mc.gameSettings.thirdPersonView = 1;
            }
        }
        firstJump = true;
        speed = 1.1f;
        timerUtil.reset();
        jumpTimer = 0;
        y = 80;
        super.onEnable();
    }
   
    @EventTarget
    public void onPacketSendEvent(EventPacket e) {
        if (!e.isSend()) return;
        if (e.getPacket() instanceof C0BPacketEntityAction
                && ((C0BPacketEntityAction) e.getPacket()).getAction() == C0BPacketEntityAction.Action.START_SPRINTING
                && sprint.getValue() && sprintMode.isMode("Cancel")) {
            e.setCancelled();
        }
        if (e.getPacket() instanceof C09PacketHeldItemChange && itemSpoof.getValue()) {
            e.setCancelled();
        }
    }

    @EventTarget
    public void onSafeWalkEvent(EventSafeWalk event) {
        if ((safewalk.getValue() && !isDownwards()) || ScaffoldUtil.getBlockCount() == 0) {
            event.setSafe(true);
        }
    }

    public static boolean isDownwards() {
        return downwards.getValue() && GameSettings.isKeyDown(mc.gameSettings.keyBindSneak);
    }

}
