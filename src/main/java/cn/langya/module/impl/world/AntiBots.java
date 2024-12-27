/*
 * MoonLight Hacked Client
 *
 * A free and open-source hacked client for Minecraft.
 * Developed using Minecraft's resources.
 *
 * Repository: https://github.com/randomguy3725/MoonLight
 *
 * Author(s): [RandomGuy & opZywl] LangYa466
 */
package cn.langya.module.impl.world;

import cn.langya.Client;
import cn.langya.event.annotations.EventTarget;
import cn.langya.event.events.EventUpdate;
import cn.langya.event.events.EventWorldLoad;
import cn.langya.module.Category;
import cn.langya.module.Module;
import cn.langya.value.impl.ModeValue;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;

import java.util.ArrayList;

public class AntiBots extends Module {
	private static final ModeValue modeValue = new ModeValue("Mode","Hypixel","Hypixel","Matrix Armor","Miniblox");
	public final ArrayList<EntityPlayer> bots = new ArrayList<>();
	private static final String VALID_USERNAME_REGEX = "^[a-zA-Z0-9_]{1,16}+$";

	public AntiBots() {
		super(Category.World);
	}

	@EventTarget
	public void onUpdate(EventUpdate event) {
		for (EntityPlayer player : mc.theWorld.playerEntities) {
			if (player != mc.thePlayer) if (isBot(player)) bots.add(player);
		}
	}

	@EventTarget
	public void onWorld(EventWorldLoad event) {
		bots.clear();
	}

	@Override
	public void onDisable() {
		bots.clear();
	}

	public static boolean isBot(EntityPlayer player) {
		if (!Client.getInstance().getModuleManager().getModule("AntiBots").isEnabled()) return false;

		if (modeValue.isMode("Hypixel")) {
			for (NetworkPlayerInfo info : mc.getNetHandler().getPlayerInfoMap()) {
				return info.getGameProfile().getId().compareTo(player.getUniqueID()) != 0 || nameStartsWith(player) || !player.getName().matches(VALID_USERNAME_REGEX);
			}
		}

		if (modeValue.isMode("Miniblox")) {
			for (NetworkPlayerInfo info : mc.getNetHandler().getPlayerInfoMap()) {
				return nameEqualsTo(player);
			}
		}

		if (modeValue.isMode("Matrix Armor")) {
			ItemStack helmet = player.getInventory()[3];
			ItemStack chestPlate = player.getInventory()[2];

			if (helmet == null || chestPlate == null)
				return true;
			if (helmet.getItem() == null || chestPlate.getItem() == null)
				return true;

			int helmetColor = ((ItemArmor) helmet.getItem()).getColor(helmet);
			int chestPlateColor = ((ItemArmor) chestPlate.getItem()).getColor(chestPlate);
			return !(helmetColor > 0 && chestPlateColor == helmetColor);
		}
		return false;
	}

	private static boolean nameStartsWith(EntityPlayer player) {
		return EnumChatFormatting.getTextWithoutFormattingCodes(player.getDisplayName().getUnformattedText()).startsWith("[NPC] ");
	}

	private static boolean nameEqualsTo(EntityPlayer player) {
		return EnumChatFormatting.getTextWithoutFormattingCodes(player.getDisplayName().getUnformattedText()).equals("BOT");
	}
}