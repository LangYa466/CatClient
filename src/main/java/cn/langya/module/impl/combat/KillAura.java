package cn.langya.module.impl.combat;

import cn.langya.Client;
import cn.langya.event.annotations.EventTarget;
import cn.langya.event.events.EventMotion;
import cn.langya.event.events.EventUpdate;
import cn.langya.module.Category;
import cn.langya.module.Module;
import cn.langya.module.impl.world.AntiBots;
import cn.langya.module.impl.world.Teams;
import cn.langya.utils.RotationUtil;
import cn.langya.utils.TimerUtil;
import cn.langya.value.impl.BooleanValue;
import cn.langya.value.impl.ModeValue;
import cn.langya.value.impl.NumberValue;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemSword;
import net.minecraft.network.play.client.C07PacketPlayerDigging;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import org.lwjgl.input.Keyboard;

import java.util.ArrayList;
import java.util.List;

/**
 * @author LangYa
 * @since 2024/12/6 13:10
 */
@Getter
@Setter
public class KillAura extends Module {

    public KillAura() {
        super(Category.Combat);
        this.attackTimer = new TimerUtil();
        this.switchTimer = new TimerUtil();
        setKeyCode(Keyboard.KEY_R);
    }

    private final NumberValue cpsValue = new NumberValue("CPS", 6, 20, 1, 1);
    private final NumberValue rangeValue = new NumberValue("Range", 3, 6, 1, 0.1F);
    private final ModeValue targetModeValue = new ModeValue("Target Mode", "Single", "Single", "Switch");
    private final NumberValue switchDelayValue = new NumberValue("Switch Delay", 500, 1000, 0, 50);
    private final ModeValue priorityModeValue = new ModeValue("Priority Mode", "Health", "Range", "Health");
    private final BooleanValue onlyAttackPlayer = new BooleanValue("Only Attack Player",true);
    private final BooleanValue autoBlockValue = new BooleanValue("Auto Block",true);

    private final List<EntityLivingBase> targets = new ArrayList<>();
    public static EntityLivingBase target;
    private final TimerUtil attackTimer;
    private int switchIndex = 0;
    private final TimerUtil switchTimer;
    private boolean isBlocking;

    @Override
    public String getSuffix() {
        return String.valueOf(targets.size());
    }

    @EventTarget
    public void onUpdate(EventUpdate event) {
        float reach = rangeValue.getValue();

        // getTargets
        for (Entity entity : mc.theWorld.loadedEntityList) {
            // 防止重复添加
            if (!(entity instanceof EntityLivingBase) || targets.contains(target)) continue;
            EntityLivingBase entityLivingBase = (EntityLivingBase) entity;
            if (entityLivingBase == mc.thePlayer || entityLivingBase.getHealth() <= 0 ||
                    entityLivingBase.isDead || entityLivingBase.getDistanceToEntity(mc.thePlayer) > reach) continue;
            targets.add(entityLivingBase);
        }

        if (targets.isEmpty()) return;
        targets.removeIf(target -> target.getHealth() <= 0  || target.isDead|| target.getDistanceToEntity(mc.thePlayer) > reach || AntiBots.isHypixelNPC(target) || Teams.isSameTeam(target) || (onlyAttackPlayer.getValue() && !(target instanceof EntityPlayer)));

        if (target != null && (target.getHealth() <= 0 || target.isDead || target.getDistanceToEntity(mc.thePlayer) > reach)) target = null;

        // sortTargets
        if (!targets.isEmpty()) {
            EntityPlayerSP thePlayer = mc.thePlayer;
            switch (priorityModeValue.getValue()) {
                case "Range":
                    targets.sort((o1, o2) -> (int) (o1.getDistanceToEntity(thePlayer) - o2.getDistanceToEntity(thePlayer)));
                    break;
                case "Health":
                    targets.sort((o1, o2) -> (int) (o1.getHealth() - o2.getHealth()));
                    break;
            }
        }

        // getTarget
        if (!targets.isEmpty() && target == null) {
            switch (targetModeValue.getValue()) {
                case "Single":
                    target = targets.get(0);
                    break;
                case "Switch":
                    if (switchTimer.hasReached(switchDelayValue.getValue().intValue())) {
                        if (switchIndex >= targets.size()) {
                            switchIndex = 0;
                            return;
                        }
                        target = targets.get(switchIndex);
                        switchTimer.reset();
                        switchIndex += 1;
                    }
                    break;
            }
        }
    }

    @EventTarget
    public void onMotion(EventMotion event) {
        if (target == null) RotationUtil.setRotations();
        if (event.isPre() && !targets.isEmpty() && target != null) {
            float[] rotations = RotationUtil.getRotationsNeeded(target);
            RotationUtil.setRotations(rotations);
            if (attackTimer.hasReached(1000 / cpsValue.getValue().intValue())) {
                if (autoBlockValue.getValue()) unBlock();
                mc.thePlayer.swingItem();
                mc.playerController.attackEntity(mc.thePlayer, target);
                attackTimer.reset();
            } else {
                if (autoBlockValue.getValue()) mc.gameSettings.keyBindRight.pressed = true;
            }
        }
    }

    @Override
    public void onDisable() {
        RotationUtil.setRotations();
        targets.clear();
        target = null;
        super.onDisable();
    }

    private void unBlock() {
        if (!(mc.thePlayer.getHeldItem().getItem() instanceof ItemSword)) return;
        mc.thePlayer.sendQueue.addToSendQueue(new C07PacketPlayerDigging(C07PacketPlayerDigging.Action.RELEASE_USE_ITEM, BlockPos.ORIGIN, EnumFacing.DOWN));
        isBlocking = false;
    }
}
