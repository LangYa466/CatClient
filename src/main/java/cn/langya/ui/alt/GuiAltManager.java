package cn.langya.ui.alt;

import cn.langya.ui.font.FontManager;
import com.mojang.authlib.exceptions.AuthenticationException;
import cn.langya.ui.alt.altimpl.MicrosoftAlt;
import cn.langya.ui.alt.microsoft.GuiMicrosoftLogin;
import cn.langya.ui.alt.microsoft.MicrosoftLogin;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.Session;

import java.io.IOException;

public class GuiAltManager extends GuiScreen {
    private final GuiScreen parentScreen;

    private volatile String status;
    private volatile MicrosoftLogin microsoftLogin;
    private volatile Thread runningThread;

    private static Alt selectAlt;

    public GuiAltManager(GuiScreen parentScreen) {
        this.parentScreen = parentScreen;
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        String info;
        info = "Your account: ";
        status = EnumChatFormatting.GREEN + "Waiting...";
        try {
            if (microsoftLogin != null) {
                status = microsoftLogin.getStatus();
            }
        } catch (NullPointerException ignored) {
        }

        drawBackground(0);
        FontManager.hanYi().drawCenteredStringWithShadow(EnumChatFormatting.YELLOW + info + mc.getSession().getUsername(), width / 2.0f, height / 2.0f - 10, -1);
        FontManager.hanYi().drawCenteredStringWithShadow(status, width / 2.0f, height / 2.0f, -1);

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        if (button.id == 0) {
            if (runningThread != null) {
                runningThread.interrupt();
            }

            mc.displayGuiScreen(parentScreen);
        } else if (button.id == 2) {
            if (selectAlt != null) {
                final Thread thread = new Thread(() -> {
                    status = EnumChatFormatting.GREEN + "Logging in...";

                    switch (selectAlt.getAccountType()) {
                        case OFFLINE:
                            mc.session = new Session(selectAlt.getUserName(), "", "", "mojang");
                            status = EnumChatFormatting.GREEN + "Logged in! " + mc.session.getUsername();
                            break;
                        case MICROSOFT: {
                            try {
                                microsoftLogin = new MicrosoftLogin(((MicrosoftAlt) selectAlt).getRefreshToken());

                                while (mc.running) {
                                    if (microsoftLogin.logged) {
                                        mc.session = new Session(microsoftLogin.getUserName(), microsoftLogin.getUuid(), microsoftLogin.getAccessToken(), "mojang");
                                        status = EnumChatFormatting.GREEN + "Logged in! " + mc.session.getUsername();
                                        break;
                                    }
                                }
                            } catch (Throwable e) {
                                e.printStackTrace();
                                status = EnumChatFormatting.RED + "Login failed! " + e.getClass().getName() + ": " + e.getMessage();
                            }

                            microsoftLogin = null;

                            break;
                        }
                    }
                }, "AltManager Login Thread");

                thread.setDaemon(true);
                thread.start();

                setRunningThread(thread);
            }
        } else if (button.id == 3) {
            if (selectAlt != null) {
                AltManager.Instance.getAltList().remove(selectAlt);
                selectAlt = null;
            }
        } else if (button.id == 4) {
            mc.displayGuiScreen(new GuiAltLogin(this) {
                @Override
                public void onLogin(String account, String password) {
                    final Thread thread = new Thread() {
                        @Override
                        public void run() {
                            final AltManager.LoginStatus loginStatus;
                            try {
                                status = EnumChatFormatting.GREEN + "Logging in...";
                                loginStatus = AltManager.loginAlt(account, password);

                                switch (loginStatus) {
                                    case FAILED:
                                        status = EnumChatFormatting.RED + "Login failed!";
                                        break;
                                    case SUCCESS:
                                        status = EnumChatFormatting.GREEN + "Logged in! " + mc.session.getUsername();
                                        break;
                                }
                            } catch (AuthenticationException e) {
                                e.printStackTrace();
                                status = EnumChatFormatting.RED + "Login failed! " + e.getClass().getName() + ": " + e.getMessage();
                            }

                            interrupt();
                        }
                    };

                    thread.setDaemon(true);
                    thread.start();

                    setRunningThread(thread);
                }
            });
        } else if (button.id == 5) {
            mc.displayGuiScreen(new GuiMicrosoftLogin(this));
        }
        super.actionPerformed(button);
    }

    @Override
    public void initGui() {
        buttonList.add(new GuiButton(4, this.width / 2 - 120, this.height - 48, 70, 20, "Offline"));
        buttonList.add(new GuiButton(5, this.width / 2 - 40, this.height - 48, 70, 20, "Microsoft"));
        this.buttonList.add(new GuiButton(0, this.width / 2 + 40, this.height - 48, 70, 20, "Exit"));
        GuiButton buttonLogin;
        buttonList.add(buttonLogin = new GuiButton(2, -1145141919, -1145141919, 70, 20, "Login"));
        GuiButton buttonRemove;
        buttonList.add(buttonRemove = new GuiButton(3, -1145141919, -1145141919, 70, 20, "Remove"));

        super.initGui();
    }

    public void setRunningThread(Thread runningThread) {
        if (this.runningThread != null) {
            this.runningThread.interrupt();
        }

        this.runningThread = runningThread;
    }
}
