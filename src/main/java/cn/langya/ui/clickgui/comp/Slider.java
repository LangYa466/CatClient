package cn.langya.ui.clickgui.comp;

import cn.langya.module.Module;
import cn.langya.ui.clickgui.ClickGuiScreen;
import cn.langya.value.impl.NumberValue;
import net.minecraft.client.gui.Gui;

import java.awt.*;
import java.math.BigDecimal;
import java.math.RoundingMode;

public class Slider extends Comp {
    private boolean dragging = false;
    private double renderWidth2;

    private final NumberValue setting;

    public Slider(double x, double y, ClickGuiScreen parent, Module module, NumberValue setting) {
        this.x = x;
        this.y = y;
        this.parent = parent;
        this.module = module;
        this.setting = setting;
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        if (isInside(mouseX, mouseY, parent.posX + x - 70, parent.posY + y + 10,parent.posX + x - 70 + renderWidth2, parent.posY + y + 20) && mouseButton == 0) {
            dragging = true;
        }
    }

    @Override
    public void mouseReleased(int mouseX, int mouseY, int state) {
        super.mouseReleased(mouseX, mouseY, state);
        dragging = false;
    }

    @Override
    public void drawScreen(int mouseX, int mouseY) {
        super.drawScreen(mouseX, mouseY);

        double min = setting.minValue;
        double max = setting.maxValue;
        double l = 90;

        double renderWidth = (l) * (setting.getValue() - min) / (max - min);
        renderWidth2 = (l) * (setting.maxValue - min) / (max - min);

        double diff = Math.min(l, Math.max(0, mouseX - (parent.posX + x - 70)));
        if (dragging) {
            if (diff == 0) {
                setting.setValue(setting.minValue);
            }
            else {
                float newValue = roundToPlace(((diff / l) * (max - min) + min));
                setting.setValue(newValue);
            }
        }
        Gui.drawRect(parent.posX + x - 70, parent.posY + y + 10,parent.posX + x - 70 + renderWidth2, parent.posY + y + 20, new Color(0, 140, 255, 255).darker().getRGB());
        Gui.drawRect(parent.posX + x - 70, parent.posY + y + 10, parent.posX + x - 70 + renderWidth, parent.posY + y + 20, new Color(14, 147, 255).getRGB());
        ClickGuiScreen.INSTANCE.fr.drawString(setting.getName() + ": " + setting.getValue(),(int)(parent.posX + x - 70),(int)(parent.posY + y), -1);
    }

    private float roundToPlace(double value) {
        BigDecimal bd = new BigDecimal(value);
        bd = bd.setScale(1, RoundingMode.HALF_UP);
        return bd.floatValue();
    }
}
