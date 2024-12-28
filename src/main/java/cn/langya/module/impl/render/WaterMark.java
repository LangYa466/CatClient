package cn.langya.module.impl.render;

import cn.langya.Client;
import cn.langya.event.annotations.EventTarget;
import cn.langya.event.events.EventRender2D;
import cn.langya.module.Category;
import cn.langya.module.Module;
import cn.langya.ui.Element;
import cn.langya.ui.font.FontManager;
import cn.langya.ui.font.impl.UFontRenderer;
import cn.langya.utils.RenderUtil;
import cn.langya.value.impl.BooleanValue;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.util.MathHelper;

import java.awt.*;

/**
 * @author LangYa
 * @since 2024/12/6 14:11
 */
public class WaterMark extends Module {
    public WaterMark() {
        super(Category.Render);
    }

    private final BooleanValue cFontValue = new BooleanValue("ClientFont",true);
    private final Element element = Client.getInstance().getElementManager().createElement(getName());

    @EventTarget
    public void onRender2D(EventRender2D event) {
        float x = element.getX();
        float y = element.getY();
        UFontRenderer fr = FontManager.hanYi(18);
        String inputString = String.format("%s | %sFPS | %sYaw | %sPitch", Client.name, Minecraft.getDebugFPS(), Math.round(MathHelper.wrapAngleTo180_float(mc.thePlayer.rotationYaw)), (int) mc.thePlayer.rotationPitch);
        float textWidth = fr.getStringWidth(inputString);
        String firstChar;
        String restOfString;

        firstChar = String.valueOf(inputString.charAt(0));
        restOfString = inputString.substring(1);

        String showName = firstChar + "§r§f" + restOfString;
        int color = RenderUtil.skyRainbow(0, 0.5f, 1f).getRGB();

        RenderUtil.drawRect(x - 2,y - 2,textWidth + 4,fr.getHeight() + 6,new Color(0,0,0,80).getRGB());
        if (cFontValue.getValue()) {
            fr.drawStringWithShadow(showName, x, y, color);
        } else {
            mc.fontRendererObj.drawStringWithShadow(showName, x, y, color);
        }
    }
}
