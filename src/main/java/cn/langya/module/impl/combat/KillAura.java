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
    private final BooleanValue noScaffoldRunValue = new BooleanValue("No Scaffold Run",true);
    private final BooleanValue noGuiRunValue = new BooleanValue("No Gui Run",false);

    private final List<EntityLivingBase> targets = new ArrayList<>();
    public static EntityLivingBase target;
    private final TimerUtil attackTimer;
    private int switchIndex = 0;
    private final TimerUtil switchTimer;

    @Override
    public String getSuffix() {
        return String.valueOf(targets.size());
    }

    @EventTarget
    public void onUpdate(EventUpdate event) {
        if (cantRun()) return;
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
        targets.removeIf(target -> target.getHealth() <= 0 || target.getDistanceToEntity(mc.thePlayer) > reach || AntiBots.isHypixelNPC(target) || Teams.isSameTeam(target));

        if (target != null && (target.getHealth() <= 0 || target.getDistanceToEntity(mc.thePlayer) > reach)) target = null;

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
        if (cantRun()) return;
        if (target == null) RotationUtil.setRotations();
        if (event.isPre() && !targets.isEmpty() && target != null) {
            float[] rotations = RotationUtil.getRotationsNeeded(target);
            RotationUtil.setRotations(rotations);
            if (attackTimer.hasReached(1000 / cpsValue.getValue().intValue())) {
                mc.thePlayer.swingItem();
                mc.playerController.attackEntity(mc.thePlayer, target);
                attackTimer.reset();
            }
        }
    }

    private boolean cantRun() {
        return (noGuiRunValue.getValue() && mc.currentScreen != null ) || (noScaffoldRunValue.getValue() && Client.getInstance().getModuleManager().getModule("Scaffold").isEnabled());
    }

    @Override
    public void onDisable() {
        RotationUtil.setRotations();
        super.onDisable();
    }
}
