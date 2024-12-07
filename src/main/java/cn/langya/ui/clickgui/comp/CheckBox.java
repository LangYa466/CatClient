package cn.langya.ui.clickgui.comp;

import cn.langya.module.Module;
import cn.langya.ui.clickgui.ClickGuiScreen;
import cn.langya.value.impl.BooleanValue;
import net.minecraft.client.gui.Gui;

import java.awt.*;

public class CheckBox extends Comp {
    private final BooleanValue setting;

    public CheckBox(double x, double y, ClickGuiScreen parent, Module module, BooleanValue setting) {
        this.x = x;
        this.y = y;
        this.parent = parent;
        this.module = module;
        this.setting = setting;
    }

    @Override
    public void drawScreen(int mouseX, int mouseY) {
        super.drawScreen(mouseX, mouseY);
        Gui.drawRect(parent.posX + x - 70, parent.posY + y, parent.posX + x + 10 - 70, parent.posY + y + 10,setting.getValue() ? new Color(0, 140, 255).getRGB() : new Color(30,30,30).getRGB());
        ClickGuiScreen.INSTANCE.fr.drawString(setting.getName(), (int)(parent.posX + x - 55), (int)(parent.posY + y + 1), new Color(200,200,200).getRGB());
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        if (isInside(mouseX, mouseY, parent.posX + x - 70, parent.posY + y, parent.posX + x + 10 - 70, parent.posY + y + 10) && mouseButton == 0) {
            setting.setValue(!setting.getValue());
        }
    }

}
