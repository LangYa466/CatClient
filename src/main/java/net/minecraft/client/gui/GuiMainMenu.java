package net.minecraft.client.gui;

import java.io.IOException;

import cn.langya.Client;
import cn.langya.ui.ClientButton;
import cn.langya.ui.alt.GuiAltManager;
import cn.langya.ui.font.FontManager;
import net.minecraft.client.resources.I18n;

public class GuiMainMenu extends GuiScreen {

    public void initGui() {
        int buttonY = this.height / 4 + 48;
        this.buttonList.add(new ClientButton(1, this.width / 2 - 100, buttonY, I18n.format("menu.singleplayer")));
        this.buttonList.add(new ClientButton(2, this.width / 2 - 100, buttonY + 24, I18n.format("menu.multiplayer")));
        this.buttonList.add(new ClientButton(3, this.width / 2 - 100, buttonY + 48, I18n.format("Alt Manager")));
        this.buttonList.add(new ClientButton(0, this.width / 2 - 100, buttonY + 76, 98, 20, I18n.format("menu.options")));
        this.buttonList.add(new ClientButton(4, this.width / 2 + 2, buttonY + 76, 98, 20, I18n.format("menu.quit")));
        this.buttonList.add(new GuiButtonLanguage(5, this.width / 2 - 124, buttonY + 76));
    }

    protected void actionPerformed(GuiButton button) throws IOException {
        if (button.id == 0) {
            this.mc.displayGuiScreen(new GuiOptions(this, this.mc.gameSettings));
        }
        if (button.id == 1) {
            this.mc.displayGuiScreen(new GuiSelectWorld(this));
        }
        if (button.id == 2) {
            this.mc.displayGuiScreen(new GuiMultiplayer(this));
        }
        if (button.id == 3) {
            this.mc.displayGuiScreen(new GuiAltManager(this));
        }
        if (button.id == 4) {
            this.mc.shutdown();
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();
        FontManager.hanYi(70).drawCenteredStringWithShadow(Client.name,width / 2F,height / 4F,-1);
        super.drawScreen(mouseX, mouseY, partialTicks);
    }
}
