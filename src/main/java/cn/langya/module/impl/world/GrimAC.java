package cn.langya.module.impl.world;

import java.text.DecimalFormat;

import cn.langya.event.annotations.EventTarget;
import cn.langya.event.events.EventPacket;
import cn.langya.event.events.EventWorldLoad;
import cn.langya.module.Category;
import cn.langya.module.Module;
import cn.langya.utils.ChatUtil;
import cn.langya.value.impl.BooleanValue;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.play.server.S14PacketEntity;
import net.minecraft.network.play.server.S19PacketEntityStatus;
import net.minecraft.util.EnumChatFormatting;

public class GrimAC extends Module {
    public BooleanValue reachValue = new BooleanValue("Reach", true);
    public BooleanValue noslowAValue = new BooleanValue("NoSlowA", true);
    public static final DecimalFormat DF_1 = new DecimalFormat("0.000000");
    int vl;

    public GrimAC() {
        super(Category.World);
    }

    @Override
    public void onEnable() {
        this.vl = 0;
    }

    @EventTarget
    public void onWorld(EventWorldLoad event) {
        this.vl = 0;
    }

    @EventTarget
    public void onPacket(EventPacket event) {
        if (!event.isRev()) return;
        if (mc.thePlayer.ticksExisted % 6 == 0) {
            S19PacketEntityStatus s19;
            if (event.getPacket() instanceof S19PacketEntityStatus && this.reachValue.getValue() && (s19 = (S19PacketEntityStatus)event.getPacket()).getOpCode() == 2) {
                new Thread(() -> this.checkCombatHurt(s19.getEntity(mc.theWorld))).start();
            }
            if (event.getPacket() instanceof S14PacketEntity && this.noslowAValue.getValue()) {
                S14PacketEntity packet = (S14PacketEntity)event.getPacket();
                Entity entity = packet.getEntity(mc.theWorld);
                if (!(entity instanceof EntityPlayer)) {
                    return;
                }
                new Thread(() -> this.checkPlayer((EntityPlayer)entity)).start();
            }
        }
    }

    private void checkCombatHurt(Entity entity) {
        if (!(entity instanceof EntityLivingBase)) {
            return;
        }
        Entity attacker = null;
        int attackerCount = 0;
        for (Entity worldEntity : mc.theWorld.getLoadedEntityList()) {
            if (!(worldEntity instanceof EntityPlayer) || worldEntity.getDistanceToEntity(entity) > 7.0f || worldEntity.equals(entity)) continue;
            ++attackerCount;
            attacker = worldEntity;
        }
        if (attacker == null || attacker.equals(entity) || Teams.isSameTeam(attacker)) {
            return;
        }
        double reach = attacker.getDistanceToEntity(entity);
        String prefix = EnumChatFormatting.GRAY + "[" + EnumChatFormatting.AQUA + "GrimAC" + EnumChatFormatting.GRAY + "] " + EnumChatFormatting.RESET + EnumChatFormatting.GRAY + attacker.getName() + EnumChatFormatting.WHITE + " failed ";
        if (reach > 3.0) {
            ChatUtil.log(prefix + EnumChatFormatting.AQUA + "Reach" + EnumChatFormatting.WHITE + " (vl:" + attackerCount + ".0)" + EnumChatFormatting.GRAY + ": " + DF_1.format(reach) + " blocks");
        }
    }

    private void checkPlayer(EntityPlayer player) {
        if (player.equals(mc.thePlayer) || Teams.isSameTeam(player)) {
            return;
        }
        String prefix = EnumChatFormatting.GRAY + "[" + EnumChatFormatting.AQUA + "GrimAC" + EnumChatFormatting.GRAY + "] " + EnumChatFormatting.RESET + EnumChatFormatting.GRAY + player.getName() + EnumChatFormatting.WHITE + " failed ";
        if (player.isUsingItem() && (player.posX - player.lastTickPosX > 0.2 || player.posZ - player.lastTickPosZ > 0.2)) {
            ChatUtil.log(prefix + EnumChatFormatting.AQUA + "NoSlowA (Prediction)" + EnumChatFormatting.WHITE + " (vl:" + this.vl + ".0)");
            ++this.vl;
        }
        if (!mc.theWorld.loadedEntityList.contains(player) || !player.isEntityAlive()) {
            this.vl = 0;
        }
    }
}