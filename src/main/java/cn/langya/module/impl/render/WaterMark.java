package cn.langya.module.impl.render;

import cn.langya.Client;
import cn.langya.event.annotations.EventTarget;
import cn.langya.event.events.EventRender2D;
import cn.langya.module.Category;
import cn.langya.module.Module;
import cn.langya.ui.Element;
import cn.langya.ui.font.FontManager;
import cn.langya.ui.font.impl.UFontRenderer;
import cn.langya.utils.MoveUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;

import java.awt.*;
import java.text.DecimalFormat;

/**
 * @author LangYa
 * @since 2024/12/6 14:11
 */
public class WaterMark extends Module {
    public WaterMark() {
        super(Category.Render);
    }

    private final Element element = Client.getInstance().getElementManager().createElement(getName());

    @EventTarget
    public void onRender2D(EventRender2D event) {
        String displayText = String.format("%s | %sFPS | %sYaw | %sPitch", Client.name, Minecraft.getDebugFPS(), (int) mc.thePlayer.rotationYaw, (int) mc.thePlayer.rotationPitch);
        UFontRenderer fr = FontManager.hanYi(18);
        float width = fr.getStringWidth(displayText);
        float height = fr.FONT_HEIGHT;
        element.setWH(width,height);
        Gui.drawRect((int) element.getX() - 2, (int) element.getY() - 2,(int) element.getX() - 2 + width + 4,(int) element.getY() - 2 + height + 4,new Color(0,0,0,80).getRGB());
        Gui.drawRect((int) element.getX() - 2, (int) element.getY() - 2,(int) element.getX() - 2 + width + 4,(int) element.getY() - 2 + 2,new Color(14, 147, 255).getRGB());
        fr.drawStringWithShadow(displayText, (int) element.getX(), (int) element.getY(), -1);
    }
}
