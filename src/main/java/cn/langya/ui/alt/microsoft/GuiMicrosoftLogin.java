package cn.langya.ui.alt.microsoft;

import cn.langya.ui.font.FontManager;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Session;
import org.apache.commons.io.IOUtils;

import java.io.IOException;

public class GuiMicrosoftLogin extends GuiScreen {
    private volatile MicrosoftLogin microsoftLogin;
    private volatile boolean closed = false;

    private final GuiScreen parentScreen;

    public GuiMicrosoftLogin(GuiScreen parentScreen) {
        this.parentScreen = parentScreen;

        final Thread thread = new Thread("MicrosoftLogin Thread") {
            @Override
            public void run() {
                try {
                    microsoftLogin = new MicrosoftLogin();

                    while (!closed) {
                        if (microsoftLogin.logged) {
                            IOUtils.closeQuietly(microsoftLogin);

                            closed = true;

                            microsoftLogin.setStatus("登录成功! " + microsoftLogin.getUserName());

                            mc.session = new Session(microsoftLogin.getUserName(), microsoftLogin.getUuid(), microsoftLogin.getAccessToken(), "mojang");

                            break;
                        }
                    }
                } catch (Throwable e) {
                    closed = true;

                    e.printStackTrace();

                    IOUtils.closeQuietly(microsoftLogin);

                    microsoftLogin.setStatus("Login failed! " + e.getClass().getName() + ":" + e.getMessage());
                }
            }
        };

        thread.setDaemon(true);
        thread.start();
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        super.actionPerformed(button);

        if (button.id == 0) {
            if (microsoftLogin != null && !closed) {
                microsoftLogin.close();
                closed = true;
                IOUtils.closeQuietly(microsoftLogin);

            }

            mc.displayGuiScreen(parentScreen);
        }
    }

    @Override
    public void initGui() {
        super.initGui();

        buttonList.add(new GuiButton(0,width / 2 - 100,height / 2 + 50,"返回"));
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();
        drawBackground(0);
        super.drawScreen(mouseX, mouseY, partialTicks);

        if (microsoftLogin == null) {
            FontManager.hanYi().drawCenteredStringWithShadow(EnumChatFormatting.YELLOW + "登录中...",width / 2.0f,height / 2.0f - 5f,-1);
        } else {
            FontManager.hanYi().drawCenteredStringWithShadow(microsoftLogin.getStatus(),width / 2.0f,height / 2.0f - 5f,-1);
        }
    }

    @Override
    public void onGuiClosed() {

        microsoftLogin.close();
        closed = true;
        IOUtils.closeQuietly(microsoftLogin);

        super.onGuiClosed();
    }
}