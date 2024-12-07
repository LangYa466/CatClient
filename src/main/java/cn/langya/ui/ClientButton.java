package cn.langya.ui;

import cn.langya.ui.font.FontManager;
import lombok.var;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;

import java.awt.*;

public class ClientButton extends GuiButton {
    public ClientButton(int buttonId, int x, int y, String buttonText) {
        super(buttonId, x, y, 200, 20, buttonText);
    }

    public ClientButton(int buttonId, int x, int y, int widthIn, int heightIn, String buttonText) {
        super(buttonId, x, y, widthIn, heightIn, buttonText);
    }

    private final Color c = new Color(0,0,0,80);
    private final Color c1 = new Color(0,0,0,160);

    public void drawButton(Minecraft mc, int mouseX, int mouseY) {
        this.mouseDragged(mc, mouseX, mouseY);
        if (this.visible) {
            this.hovered = mouseX >= this.xPosition && mouseY >= this.yPosition && mouseX < this.xPosition + this.width && mouseY < this.yPosition + this.height;
            Color color;
            if (hovered) color = c1; else color = c;
            Gui.drawRect(xPosition,yPosition,xPosition + width,yPosition + height, color.getRGB());
            var fr = FontManager.hanYi();
            fr.drawCenteredString(this.displayString, this.xPosition + (width / 2F), this.yPosition + (this.height / 2F), -1);
        }
    }
}
