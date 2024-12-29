package cn.langya.ui.clickgui;

import cn.langya.Client;
import cn.langya.module.Category;
import cn.langya.module.Module;
import cn.langya.module.impl.render.ClickGUI;
import cn.langya.ui.clickgui.comp.CheckBox;
import cn.langya.ui.clickgui.comp.Combo;
import cn.langya.ui.clickgui.comp.Comp;
import cn.langya.ui.clickgui.comp.Slider;
import cn.langya.ui.font.FontManager;
import cn.langya.ui.font.impl.UFontRenderer;
import cn.langya.value.Value;
import cn.langya.value.impl.BooleanValue;
import cn.langya.value.impl.ModeValue;
import cn.langya.value.impl.NumberValue;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;

import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;

public class ClickGuiScreen extends GuiScreen {
    public double posX, posY, width, height, dragX, dragY;
    public boolean dragging;
    public Category selectedCategory;
    public final FontRenderer fr = ClickGUI.fr;

    public int modeIndex;

    public ArrayList<Comp> comps = new ArrayList<>();

    public static final ClickGuiScreen INSTANCE = new ClickGuiScreen();

    public ClickGuiScreen() {
        dragging = false;
        posX = getScaledRes().getScaledWidth() / 2F - 150;
        posY = getScaledRes().getScaledHeight() / 2F - 100;
        selectedCategory = Category.Combat;
    }

    @Override
    public void onGuiClosed() {
        Client.getInstance().getConfigManager().saveConfig("Module.json");
        super.onGuiClosed();
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        comps.removeIf(comp -> comp.setting != null && comp.setting.isHide());

        super.drawScreen(mouseX, mouseY, partialTicks);
        if (dragging) {
            posX = mouseX - dragX;
            posY = mouseY - dragY;
        }
        width = posX + 350;
        height = posY + 450;
        Gui.drawRect(posX, posY - 10, width, posY + 5, new Color(0, 140, 255).getRGB());
        Gui.drawRect(posX, posY, width, height, new Color(45,45,45).getRGB());

        int offset = 0;
        for (Category category : Category.values()) {
            Gui.drawRect(posX,posY + 1 + offset,posX + 60,posY + 15 + offset,category.equals(selectedCategory) ? new Color(0, 140, 255).getRGB() : new Color(28,28,28).getRGB());
            fr.drawString(category.name(),(int)posX + 2, (int)(posY + 5) + offset, -1);
            offset += 15;
        }
        offset = 0;
        for (Module m : Client.getInstance().getModuleManager().getModulesWithCategory(selectedCategory)) {
            Gui.drawRect(posX + 65,posY + 1 + offset,posX + 160,posY + 15 + offset,m.isEnabled() ? new Color(0, 140, 255).getRGB() : new Color(28,28,28).getRGB());
            fr.drawString(m.getName(),(int)posX + 67, (int)(posY + 5) + offset, -1);
            offset += 15;
        }

        for (Comp comp : comps) {
            comp.drawScreen(mouseX, mouseY);
        }

        fr.drawStringWithShadow(Client.name, (float) posX, (float) posY - 9,-1);
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        super.keyTyped(typedChar, keyCode);
        for (Comp comp : comps) {
            comp.keyTyped(typedChar, keyCode);
        }
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        if (isInside(mouseX, mouseY, posX, posY - 10, width, posY) && mouseButton == 0) {
            dragging = true;
            dragX = mouseX - posX;
            dragY = mouseY - posY;
        }
        int offset = 0;
        for (Category category : Category.values()) {
            if (isInside(mouseX, mouseY,posX,posY + 1 + offset,posX + 60,posY + 15 + offset) && mouseButton == 0) {
                selectedCategory = category;
            }
            offset += 15;
        }
        offset = 0;
        for (Module m : Client.getInstance().getModuleManager().getModulesWithCategory(selectedCategory)) {
            if (isInside(mouseX, mouseY,posX + 65,posY + 1 + offset,posX + 125,posY + 15 + offset)) {
                if (mouseButton == 0) {
                    m.toggle();
                }

                // 不然不显示isHide的 所以每次都获取一下
                comps.clear();
                getModuleValues(m);
            }
            offset += 15;
        }
        for (Comp comp : comps) {
            comp.mouseClicked(mouseX, mouseY, mouseButton);
        }
    }

    private void getModuleValues(Module m) {
        int sOffset = 3;
        if (m.getValues().isEmpty()) return;
        for (Value<?> setting : m.getValues()) {
            if (setting.isHide()) return;
            if (setting instanceof ModeValue) {
                comps.add(new Combo(275, sOffset, this, m, (ModeValue) setting));
                sOffset += 15;
            }
            if (setting instanceof BooleanValue) {
                comps.add(new CheckBox(275, sOffset, this, m,(BooleanValue) setting));
                sOffset += 15;
            }
            if (setting instanceof NumberValue) {
                comps.add(new Slider(275, sOffset, this, m,(NumberValue) setting));
                sOffset += 25;
            }
        }
    }

    @Override
    protected void mouseReleased(int mouseX, int mouseY, int state) {
        super.mouseReleased(mouseX, mouseY, state);
        dragging = false;
        for (Comp comp : comps) {
            comp.mouseReleased(mouseX, mouseY, state);
        }
    }

    @Override
    public void initGui() {
        super.initGui();
        dragging = false;
    }

    public boolean isInside(int mouseX, int mouseY, double x, double y, double x2, double y2) {
        return (mouseX > x && mouseX < x2) && (mouseY > y && mouseY < y2);
    }

    public ScaledResolution getScaledRes() {
        return new ScaledResolution(Minecraft.getMinecraft());
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }
}
