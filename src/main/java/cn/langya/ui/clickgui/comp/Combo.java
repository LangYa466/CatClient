package cn.langya.ui.clickgui.comp;

import cn.langya.module.Module;
import cn.langya.ui.clickgui.ClickGuiScreen;
import cn.langya.value.impl.ModeValue;
import net.minecraft.client.gui.Gui;

import java.awt.*;

public class Combo extends Comp {
    private final ModeValue setting;

    public Combo(double x, double y, ClickGuiScreen parent, Module module, ModeValue setting) {
        this.x = x;
        this.y = y;
        this.parent = parent;
        this.module = module;
        this.setting = setting;
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        if (isInside(mouseX, mouseY, parent.posX + x - 70, parent.posY + y, parent.posX + x, parent.posY + y + 10) && mouseButton == 0) {
            int max = setting.modes.length;
            if (parent.modeIndex + 1 >= max) {
                parent.modeIndex = 0;
            } else {
                parent.modeIndex++;
            }
            setting.setValue(setting.getNextValue());
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY) {
        super.drawScreen(mouseX, mouseY);
        Gui.drawRect(parent.posX + x - 70, parent.posY + y, parent.posX  + x + (ClickGuiScreen.INSTANCE.fr.getStringWidth(setting.getName() + ": " + setting.getValue()) / 2F), parent.posY + y + 10, new Color(30,30,30).getRGB());
        ClickGuiScreen.INSTANCE.fr.drawString(setting.getName() + ": " + setting.getValue(), (int)(parent.posX + x - 69), (int)(parent.posY + y + 1), new Color(200,200,200).getRGB());
    }
}
