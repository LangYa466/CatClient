package cn.langya.command.impl;

import cn.langya.Client;
import cn.langya.command.Command;
import cn.langya.utils.ChatUtil;
import cn.langya.utils.RenderUtil;

/**
 * @author LangYa
 * @since 2024/12/13 02:09
 */
public class RectCommand extends Command {
    public RectCommand() {
        super("rect");
    }

    @Override
    public void run(String[] args) {
        if (args.length != 6) {
            ChatUtil.log(".rect name colorName width height colorAlpha");
            return;
        }
        String name = args[1];
        String colorName = args[2];
        int[] wh = new int[] {Integer.parseInt(args[3]), Integer.parseInt(args[4])};
        int alpha = Integer.parseInt(args[5]);
        Client.getInstance().getCustomUIManager().addCustomUIWithRect(name, RenderUtil.reAlpha(Client.getInstance().getCustomUIManager().parseColor(colorName), alpha), wh);
        super.run(args);
    }
}
