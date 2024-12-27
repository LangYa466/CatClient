package cn.langya.module.impl.render;

import cn.langya.Client;
import cn.langya.event.annotations.EventTarget;
import cn.langya.event.events.EventRender2D;
import cn.langya.module.Category;
import cn.langya.module.Module;
import cn.langya.module.impl.combat.LegitAura;
import cn.langya.ui.Element;
import cn.langya.ui.font.FontManager;
import cn.langya.ui.font.impl.UFontRenderer;
import cn.langya.utils.Colors;
import cn.langya.utils.MathUtil;
import cn.langya.utils.RenderUtil;
import cn.langya.value.impl.ModeValue;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.text.DecimalFormat;
import java.util.ArrayList;

/**
 * @author LangYa
 * @since 2024/12/7 14:37
 */
public class TargetHUD extends Module {
    public TargetHUD() {
        super(Category.Render);
    }

    private final ModeValue uiMode = new ModeValue("UI Mode","Exhibition","Exhibition","Simple");
    private final Element element = Client.getInstance().getElementManager().createElement("TargetHUD");
    private final DecimalFormat decimalFormat = new DecimalFormat("0.0");

    @EventTarget
    public void onRender2D(EventRender2D event) {
        EntityLivingBase target = LegitAura.target;
        if (mc.currentScreen instanceof GuiChat && target == null) target = mc.thePlayer;
        if (target == null) return;
        UFontRenderer fr = FontManager.hanYi();
        String targetName = String.format("Name: %s",target.getName());
        float x = element.getX();
        float y = element.getY();
        String healthText = String.format("Health: %s/%s",decimalFormat.format(target.getHealth()),decimalFormat.format(target.getMaxHealth()));
        float width = fr.getStringWidth(targetName);
        float newWidth =  fr.getStringWidth(healthText) + 2;
        if (width < newWidth) width = newWidth;
        int healthRectHeight = 2;
        float height = 10 + fr.getHeight() + healthRectHeight;
        switch (uiMode.getValue()) {
            case "Simple": {
                Gui.drawRect(x, y, x + width, y + height + healthRectHeight, new Color(0, 0, 0, 80).getRGB());
                Gui.drawRect(x, y, x + width, y + 2, new Color(0, 140, 255).getRGB());
                y = y + 2;
                fr.drawStringWithShadow(targetName, x, y, -1);
                fr.drawStringWithShadow(healthText, x, y + fr.getHeight() + 1, -1);
                float endWidth = target.getHealth() / target.getMaxHealth() * width;
                Gui.drawRect(x, y + 10 + fr.getHeight(), x + endWidth, y + 10 + healthRectHeight + fr.getHeight(), new Color(255, 0, 0).getRGB());
                break;
            }
            case "Exhibition": {
                GlStateManager.pushMatrix();
                width = (int) (FontManager.hanYi(18).getStringWidth(target.getName()) > 70.0f ? (double) (125.0f + FontManager.hanYi(18).getStringWidth(target.getName()) - 70.0f) : 125.0);
                height = 45;
                GlStateManager.translate(x, y + 6, 0.0f);
                RenderUtil.skeetRect(0, -2.0, FontManager.hanYi(18).getStringWidth(target.getName()) > 70.0f ? (double) (124.0f + FontManager.hanYi(18).getStringWidth(target.getName()) - 70.0f) : 124.0, 38.0, 1.0);
                RenderUtil.skeetRectSmall(0.0f, -2.0f, 124.0f, 38.0f, 1.0);
                FontManager.hanYi(18).drawStringWithShadow(target.getName(), 41f, 0.3f, -1);
                final float health = target.getHealth();
                final float healthWithAbsorption = target.getHealth() + target.getAbsorptionAmount();
                final float progress = health / target.getMaxHealth();
                final Color healthColor = health >= 0.0f ? Colors.getBlendColor(target.getHealth(), target.getMaxHealth()).brighter() : Color.RED;
                double cockWidth = 0.0;
                cockWidth = MathUtil.round(cockWidth, (int) 5.0);
                if (cockWidth < 50.0) {
                    cockWidth = 50.0;
                }
                final double healthBarPos = cockWidth * (double) progress;
                Gui.drawRect(42.5, 10.3, 53.0 + healthBarPos + 0.5, 13.5, healthColor.getRGB());
                if (target.getAbsorptionAmount() > 0.0f) {
                    Gui.drawRect(97.5 - (double) target.getAbsorptionAmount(), 10.3, 103.5, 13.5, new Color(137, 112, 9).getRGB());
                }
                RenderUtil.drawBorderedRect(42.0, 9.8f, 54.0 + cockWidth, 14.0, 0.5f, 0, Color.BLACK.getRGB());
                for (int dist = 1; dist < 10; ++dist) {
                    final double cock = cockWidth / 8.5 * (double) dist;
                    Gui.drawRect(43.5 + cock, 9.8, 43.5 + cock + 0.5, 14.0, Color.BLACK.getRGB());
                }
                GlStateManager.scale(0.5, 0.5, 0.5);
                final int distance = (int) mc.thePlayer.getDistanceToEntity(target);
                final String nice = "HP: " + (int) healthWithAbsorption + " | Dist: " + distance;
                FontManager.hanYi(22).drawString(nice, 85.3f, 32.3f, -1, true);
                GlStateManager.scale(1.9, 1.9, 1.9);
                GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
                GlStateManager.enableAlpha();
                GlStateManager.enableBlend();
                GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
                drawEquippedShit(28, 21, target);
                GlStateManager.disableAlpha();
                GlStateManager.disableBlend();
                GlStateManager.scale(0.31, 0.31, 0.31);
                GlStateManager.translate(73.0f, 102.0f, 40.0f);
                GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
                drawModel(target.rotationYaw, target.rotationPitch, target);
                GlStateManager.popMatrix();
                break;
            }
        }
        element.setWH(width,height);
    }

    public void drawModel(final float yaw, final float pitch, final EntityLivingBase entityLivingBase) {
        GlStateManager.resetColor();
        GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
        GlStateManager.enableColorMaterial();
        GlStateManager.pushMatrix();
        GlStateManager.translate(0.0f, 0.0f, 50.0f);
        GlStateManager.scale(-50.0f, 50.0f, 50.0f);
        GlStateManager.rotate(180.0f, 0.0f, 0.0f, 1.0f);
        final float renderYawOffset = entityLivingBase.renderYawOffset;
        final float rotationYaw = entityLivingBase.rotationYaw;
        final float rotationPitch = entityLivingBase.rotationPitch;
        final float prevRotationYawHead = entityLivingBase.prevRotationYawHead;
        final float rotationYawHead = entityLivingBase.rotationYawHead;
        GlStateManager.rotate(135.0f, 0.0f, 1.0f, 0.0f);
        RenderHelper.enableStandardItemLighting();
        GlStateManager.rotate(-135.0f, 0.0f, 1.0f, 0.0f);
        GlStateManager.rotate((float) (-Math.atan(pitch / 40.0f) * 20.0), 1.0f, 0.0f, 0.0f);
        entityLivingBase.renderYawOffset = yaw - 0.4f;
        entityLivingBase.rotationYaw = yaw - 0.2f;
        entityLivingBase.rotationPitch = pitch;
        entityLivingBase.rotationYawHead = entityLivingBase.rotationYaw;
        entityLivingBase.prevRotationYawHead = entityLivingBase.rotationYaw;
        GlStateManager.translate(0.0f, 0.0f, 0.0f);
        final RenderManager renderManager = mc.getRenderManager();
        renderManager.setPlayerViewY(180.0f);
        renderManager.setRenderShadow(false);
        renderManager.renderEntityWithPosYaw(entityLivingBase, 0.0, 0.0, 0.0, 0.0f, 1.0f);
        renderManager.setRenderShadow(true);
        entityLivingBase.renderYawOffset = renderYawOffset;
        entityLivingBase.rotationYaw = rotationYaw;
        entityLivingBase.rotationPitch = rotationPitch;
        entityLivingBase.prevRotationYawHead = prevRotationYawHead;
        entityLivingBase.rotationYawHead = rotationYawHead;
        GlStateManager.popMatrix();
        RenderHelper.disableStandardItemLighting();
        GlStateManager.disableRescaleNormal();
        GlStateManager.setActiveTexture(OpenGlHelper.lightmapTexUnit);
        GlStateManager.disableTexture2D();
        GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit);
        GlStateManager.resetColor();
    }

    public static void drawEquippedShit(final int x, final int y, final EntityLivingBase target) {
        if (!(target instanceof EntityPlayer)) return;
        GL11.glPushMatrix();
        final ArrayList<ItemStack> stuff = new ArrayList<>();
        int cock = -2;
        for (int geraltOfNigeria = 3; geraltOfNigeria >= 0; --geraltOfNigeria) {
            final ItemStack armor = target.getCurrentArmor(geraltOfNigeria);
            if (armor != null) {
                stuff.add(armor);
            }
        }
        if (target.getHeldItem() != null) {
            stuff.add(target.getHeldItem());
        }

        for (final ItemStack yes : stuff) {
            if (mc.theWorld != null) {
                RenderHelper.enableGUIStandardItemLighting();
                cock += 16;
            }
            GlStateManager.pushMatrix();
            GlStateManager.disableAlpha();
            GlStateManager.clear(256);
            GlStateManager.enableBlend();
            Minecraft.getMinecraft().getRenderItem().renderItemIntoGUI(yes, cock + x, y);
            Minecraft.getMinecraft().getRenderItem().renderItemOverlays(Minecraft.getMinecraft().fontRendererObj, yes, cock + x, y);
            GlStateManager.disableBlend();
            GlStateManager.scale(0.5, 0.5, 0.5);
            GlStateManager.disableDepth();
            GlStateManager.disableLighting();
            GlStateManager.enableDepth();
            GlStateManager.scale(2.0f, 2.0f, 2.0f);
            GlStateManager.enableAlpha();
            GlStateManager.popMatrix();
            yes.getEnchantmentTagList();
        }
        GL11.glPopMatrix();
    }
}
