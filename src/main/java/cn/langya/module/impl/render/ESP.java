package cn.langya.module.impl.render;

import cn.langya.event.annotations.EventTarget;
import cn.langya.event.events.EventRender3D;
import cn.langya.module.Category;
import cn.langya.module.Module;
import cn.langya.module.impl.world.AntiBots;
import cn.langya.utils.WorldRenderUtil;
import cn.langya.value.impl.BooleanValue;
import cn.langya.value.impl.ModeValue;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.entity.monster.EntityGhast;
import net.minecraft.entity.monster.EntitySlime;
import net.minecraft.entity.passive.EntityAmbientCreature;
import net.minecraft.entity.passive.EntityWaterMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.AxisAlignedBB;
import org.lwjgl.opengl.GL11;

import java.awt.*;

public class ESP extends Module {
    public static ModeValue mode = new ModeValue("Mode", "2DBox", "2DBox", "3DBox", "Cylinder");

    public static BooleanValue invisible = new BooleanValue("Invisible", false);
    public static BooleanValue player = new BooleanValue("Player", true);
    public static BooleanValue mob = new BooleanValue("Mob", false);

    public ESP() {
        super(Category.Render);
    }

    private final Color c = Color.WHITE;

    @EventTarget
    private void onRender3D(final EventRender3D event) {
        if (mode.isMode("3DBox")) {
            for (EntityLivingBase entity : mc.theWorld.playerEntities) {
                if (entity == mc.thePlayer) continue;
                if (isValidForESP(entity)) {
                    continue;
                }

                double posX = entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * (double) event.getPartialTicks() - mc.getRenderManager().renderPosX;
                double posY = entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * (double) event.getPartialTicks() - mc.getRenderManager().renderPosY;
                double posZ = entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * (double) event.getPartialTicks() - mc.getRenderManager().renderPosZ;

                WorldRenderUtil.drawOutlinedEntityESP(posX, posY, posZ, 0.4, entity.getEntityBoundingBox().maxY - entity.getEntityBoundingBox().minY, c.getRed() / 255f, c.getGreen() / 255f, c.getBlue() / 255f, 1);
            }
        }

        if (mode.isMode("2DBox")) {
            for (EntityLivingBase entity : mc.theWorld.playerEntities) {
                if (entity == mc.thePlayer) continue;
                try {
                    if (isValidForESP(entity)) {
                        continue;
                    }

                    double posX = entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * (double) event.getPartialTicks() - mc.getRenderManager().renderPosX;
                    double posY = entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * (double) event.getPartialTicks() - mc.getRenderManager().renderPosY;
                    double posZ = entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * (double) event.getPartialTicks() - mc.getRenderManager().renderPosZ;

                    GL11.glPushMatrix();

                    GL11.glColor4f(c.getRed() / 255f, c.getGreen() / 255f, c.getBlue() / 255f, 1);

                    double width = 0.25;
                    double height = entity.getEntityBoundingBox().maxY - entity.getEntityBoundingBox().minY;

                    WorldRenderUtil.drawBoundingBox(new AxisAlignedBB(posX - width, posY, posZ - width, posX + width, posY + height, posZ + width));
                    WorldRenderUtil.renderOne();
                    WorldRenderUtil.drawBoundingBox(new AxisAlignedBB(posX - width, posY, posZ - width, posX + width, posY + height, posZ + width));
                    WorldRenderUtil.renderTwo();
                    WorldRenderUtil.drawBoundingBox(new AxisAlignedBB(posX - width, posY, posZ - width, posX + width, posY + height, posZ + width));
                    WorldRenderUtil.renderThree();
                    WorldRenderUtil.renderFour(entity);
                    WorldRenderUtil.drawBoundingBox(new AxisAlignedBB(posX - width, posY, posZ - width, posX + width, posY + height, posZ + width));
                    WorldRenderUtil.renderFive();

                    GL11.glColor4f(c.getRed() / 255f, c.getGreen() / 255f, c.getBlue() / 255f, 1);

                    GL11.glPopMatrix();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        if (mode.isMode("Cylinder")) {
            for (EntityLivingBase entity : mc.theWorld.playerEntities) {
                if (isValidForESP(entity)) {
                    continue;
                }

                double posX = entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * (double) event.getPartialTicks() - mc.getRenderManager().renderPosX;
                double posY = entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * (double) event.getPartialTicks() - mc.getRenderManager().renderPosY;
                double posZ = entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * (double) event.getPartialTicks() - mc.getRenderManager().renderPosZ;

                WorldRenderUtil.drawWolframEntityESP(entity, Color.GRAY.getRGB(), posX, posY, posZ);
            }
        }
    }

    public static boolean isValidForESP(EntityLivingBase entity) {
        if(entity == mc.thePlayer) return true;

        if (entity == null) {
            return true;
        }

        if ((entity instanceof EntityWaterMob || entity instanceof EntityCreature || entity instanceof EntityAmbientCreature || entity instanceof EntityArmorStand || entity instanceof EntityGhast) && mob.getValue()) {
            return false;
        }

        if (entity instanceof EntitySlime)
            return true;

        if (entity.isDead || entity.noClip) {
            return true;
        }

        if (entity.isInvisible() && !invisible.getValue()) {
            return true;
        }

        if (entity instanceof EntityPlayer && !player.getValue()) {
            return true;
        }

        return AntiBots.isBot((EntityPlayer) entity);
    }
}