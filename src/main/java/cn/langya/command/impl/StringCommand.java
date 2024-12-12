package cn.langya.command.impl;

import cn.langya.Client;
import cn.langya.command.Command;
import cn.langya.utils.ChatUtil;
import cn.langya.utils.RenderUtil;

/**
 * @author LangYa
 * @since 2024/12/13 02:09
 */
public class StringCommand extends Command {
    public StringCommand() {
        super("str");
    }

    @Override
    public void run(String[] args) {
        if (args.length != 6) {
            ChatUtil.log(".str name colorName displayText colorAlpha");
            return;
        }
        String name = args[1];
        String colorName = args[2];
        String displayText = args[3];
        int alpha = Integer.parseInt(args[4]);
        Client.getInstance().getCustomUIManager().addCustomUIWithString(name, RenderUtil.reAlpha(Client.getInstance().getCustomUIManager().parseColor(colorName), alpha), displayText);
        super.run(args);
    }
}
