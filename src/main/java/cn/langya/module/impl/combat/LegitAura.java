package cn.langya.module.impl.combat;

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
import net.minecraft.util.MovingObjectPosition;
import org.lwjgl.input.Keyboard;

import java.util.ArrayList;
import java.util.List;

/**
 * @author LangYa
 * @since 2024/12/6 13:10
 */
@Getter
@Setter
public class LegitAura extends Module {

    public LegitAura() {
        super(Category.Combat);
        this.attackTimer = new TimerUtil();
        this.switchTimer = new TimerUtil();
        setKeyCode(Keyboard.KEY_R);
    }

    private final NumberValue cpsValue = new NumberValue("CPS", 6, 20, 1, 1);
    private final NumberValue scanRangeValue = new NumberValue("Scan Range", 3, 6, 1, 0.1F);
    private final NumberValue attackRangeValue = new NumberValue("Attack Range", 3, 6, 1, 0.1F);
    private final ModeValue targetModeValue = new ModeValue("Target Mode", "Single", "Single", "Switch");
    private final NumberValue switchDelayValue = new NumberValue("Switch Delay", 500, 1000, 0, 50);
    private final ModeValue priorityModeValue = new ModeValue("Priority Mode", "Health", "Range", "Health", "None");
    private final BooleanValue onlyAttackPlayer = new BooleanValue("Only Attack Player",true);
    private final BooleanValue autoBlockValue = new BooleanValue("Auto Block",true);
    private final ModeValue autoBlockModeValue = new ModeValue("Auto Block Mode","Legit","Legit","Off");
    private final BooleanValue rayCastValue = new BooleanValue("Ray Cast",true);

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
        float reach = scanRangeValue.getValue();

        // 更新目标列表
        mc.theWorld.loadedEntityList.stream()
                .filter(entity -> entity instanceof EntityLivingBase)
                .map(entity -> (EntityLivingBase) entity)
                .filter(entity -> entity != mc.thePlayer && entity.getHealth() > 0 && !entity.isDead && entity.getDistanceToEntity(mc.thePlayer) <= reach)
                .filter(entity -> !targets.contains(entity))
                .forEach(targets::add);

        targets.removeIf(target ->
                target.getHealth() <= 0 ||
                        target.isDead ||
                        target.getDistanceToEntity(mc.thePlayer) > reach ||
                        AntiBots.isBot(target) ||
                        Teams.isSameTeam(target) ||
                        (onlyAttackPlayer.getValue() && !(target instanceof EntityPlayer))
        );

        if (targets.isEmpty()) {
            unBlock();
            target = null;
            return;
        }

        // 选择目标优先级
        EntityPlayerSP thePlayer = mc.thePlayer;
        targets.sort((o1, o2) -> {
            switch (priorityModeValue.getValue()) {
                case "Range":
                    return Float.compare(o1.getDistanceToEntity(thePlayer), o2.getDistanceToEntity(thePlayer));
                case "Health":
                    return Float.compare(o1.getHealth(), o2.getHealth());
                default:
                    return 0;
            }
        });

        // 确定目标
        if (target == null && !targets.isEmpty()) {
            if ("Single".equals(targetModeValue.getValue())) {
                target = targets.get(0);
            } else if ("Switch".equals(targetModeValue.getValue()) && switchTimer.hasReached(switchDelayValue.getValue().intValue())) {
                target = targets.get(switchIndex % targets.size());
                switchTimer.reset();
                switchIndex++;
            }
        }
    }

    @EventTarget
    public void onMotion(EventMotion event) {
        if (event.isPre() && target != null) {
            float[] rotations = RotationUtil.getRotationsNeeded(target);

            if (rayCastValue.getValue()) {
                if (mc.objectMouseOver.entityHit != target || mc.objectMouseOver.typeOfHit != MovingObjectPosition.MovingObjectType.ENTITY) {
                    RotationUtil.setRotations(rotations);
                    return;
                }
            } else {
                RotationUtil.setRotations(rotations);
            }

            if (attackTimer.hasReached(1000 / cpsValue.getValue().intValue())) {
                if (target.getDistanceToEntity(mc.thePlayer) <= attackRangeValue.getValue()) {
                    if (autoBlockValue.getValue()) unBlock();
                    if (isBlocking) return;
                    mc.thePlayer.swingItem();
                    mc.playerController.attackEntity(mc.thePlayer, target);
                    attackTimer.reset();
                }
            } else if (autoBlockValue.getValue()) {
                doBlock();
            }
        } else if (target == null) {
            RotationUtil.setRotations();
        }
    }

    @Override
    public void onDisable() {
        targets.clear();
        target = null;
        RotationUtil.setRotations();
        super.onDisable();
    }

    private void doBlock() {
        if (isBlocking || mc.thePlayer.getHeldItem() == null || !(mc.thePlayer.getHeldItem().getItem() instanceof ItemSword)) return;
        isBlocking = true;
        switch (autoBlockModeValue.getValue()) {
            case "Legit":
                mc.gameSettings.keyBindUseItem.pressed = true;
                break;
            case "Off": break;
        }
    }

    private void unBlock() {
        if (!isBlocking || !(mc.thePlayer.getHeldItem().getItem() instanceof ItemSword)) return;
        switch (autoBlockModeValue.getValue()) {
            case "Legit":
                mc.gameSettings.keyBindUseItem.pressed = false;
                break;
            case "Off": break;
        }
        isBlocking = false;
    }
}
