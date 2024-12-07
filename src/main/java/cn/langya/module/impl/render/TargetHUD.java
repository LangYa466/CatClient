package cn.langya.module.impl.render;

import cn.langya.Client;
import cn.langya.event.annotations.EventTarget;
import cn.langya.event.events.EventRender2D;
import cn.langya.module.Category;
import cn.langya.module.Module;
import cn.langya.module.impl.combat.KillAura;
import cn.langya.ui.Element;
import cn.langya.ui.font.FontManager;
import cn.langya.ui.font.impl.UFontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.entity.EntityLivingBase;

import java.awt.*;
import java.text.DecimalFormat;

/**
 * @author LangYa
 * @since 2024/12/7 14:37
 */
public class TargetHUD extends Module {
    public TargetHUD() {
        super(Category.Render);
    }

    private final Element element = Client.getInstance().getElementManager().createElement("TargetHUD");
    private final DecimalFormat decimalFormat = new DecimalFormat("0.0");

    @EventTarget
    public void onRender2D(EventRender2D event) {
        EntityLivingBase target = KillAura.target;
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
        Gui.drawRect(x,y,x + width,y + height + healthRectHeight,new Color(0,0,0,80).getRGB());
        Gui.drawRect(x,y,x + width,y + 2,new Color(0, 140, 255).getRGB());
        y = y + 2;
        fr.drawStringWithShadow(targetName,x,y,-1);
        fr.drawStringWithShadow(healthText,x,y + fr.getHeight() + 1,-1);
        float endWidth = target.getHealth() / target.getMaxHealth() * width;
        Gui.drawRect(x,y + 10 + fr.getHeight(),x + endWidth,y + 10 + healthRectHeight + fr.getHeight(),new Color(255,0,0).getRGB());
        element.setWH(width,height);
    }
}
