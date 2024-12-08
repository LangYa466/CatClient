package cn.langya.module.impl.world;

import cn.langya.Client;
import cn.langya.module.Category;
import cn.langya.module.Module;
import cn.langya.utils.PlayerUtil;
import cn.langya.value.impl.BooleanValue;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;

public class Teams extends Module {
    public Teams() {
        super(Category.World);
    }

    private static final BooleanValue armor = new BooleanValue("ArmorColor", true);
    private static final BooleanValue color = new BooleanValue("Color", true);
    private static final BooleanValue scoreboard = new BooleanValue("ScoreboardTeam", true);
    
    public static boolean isSameTeam(final Entity entity) {
        if (entity instanceof EntityPlayer) {
            EntityPlayer entityPlayer = (EntityPlayer) entity;
            if (entityPlayer == mc.thePlayer) return false;
            return Client.getInstance().getModuleManager().getModule("Teams").isEnabled() && ((armor.getValue() && PlayerUtil.armorTeam(entityPlayer))
                    || (color.getValue() && PlayerUtil.colorTeam(entityPlayer))
                    || (scoreboard.getValue() && PlayerUtil.scoreTeam(entityPlayer)));
        }
        return false;
    }

}