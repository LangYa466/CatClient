package cn.langya.module.impl.world;

import cn.langya.Client;
import cn.langya.event.annotations.EventTarget;
import cn.langya.event.events.EventTick;
import cn.langya.event.events.EventWorldLoad;
import cn.langya.module.Category;
import cn.langya.module.Module;
import cn.langya.utils.ChatUtil;
import cn.langya.utils.HypixelUtil;
import cn.langya.utils.Location;
import cn.langya.value.impl.ModeValue;
import net.minecraft.client.gui.GuiPlayerTabOverlay;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumChatFormatting;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class AntiBots extends Module {
	public static ModeValue mode = new ModeValue("Mode", "Hypixel", "Basic", "Advanced", "Hypixel", "BrokenID", "Invisible", "Enclose");
	private final HashMap<String, Integer> TabTicks = new HashMap<>();
	private final HashMap<Integer, Integer> InvisTicks = new HashMap<>();
	private final List<Integer> Grounded = new ArrayList<>();

	private final List<Integer> blacklisted = new ArrayList<>();

	public AntiBots() {
		super(Category.World);
	}

	public static boolean isInTablist(EntityLivingBase entity) {
		for (NetworkPlayerInfo item : mc.getNetHandler().getPlayerInfoMap()) {

            if (item != null && item.getGameProfile() != null
					&& item.getGameProfile().getName().contains(entity.getName())) {
				return true;
			}
		}

		return false;
	}

	private boolean hasBadlionBots(final EntityPlayer parent) {
		if (parent.isInvisible())
			return false;

		for (EntityPlayer player : mc.theWorld.playerEntities) {
			if (player != parent && player.isInvisible() && parent.getDistanceToEntity(player) < 3.0)
				return true;
		}

		return false;
	}
	
	@EventTarget
	public void onTick(EventTick event) {
		if (mc.theWorld == null) return;
		for (EntityPlayer player : mc.theWorld.playerEntities) {
			final String name = EnumChatFormatting.getTextWithoutFormattingCodes(player.getName());

			if (!TabTicks.containsKey(name)) {
				TabTicks.put(name, 0);
			}

			if (isInTablist(player)) {
				int before = TabTicks.get(name);
				TabTicks.remove(name);
				TabTicks.put(name, before + 1);
			}
		}
	}

	@EventTarget
	public void onWorldLoad(EventWorldLoad event) {
		this.TabTicks.clear();
		this.Grounded.clear();
		this.InvisTicks.clear();

		this.blacklisted.clear();
	}

	public boolean isNPC(EntityLivingBase entity) {
		if (entity == null) {
			return true;
		}

		if (mode.isMode("Hypixel") && entity.ticksExisted <= 10 * 20)
			return false;

		if (entity.isPlayerSleeping()) {
			return true;
		}

		if (mode.getValue().equals("BrokenID") && entity.getEntityId() > 1000000) {
			return true;
		}

		if ((mode.getValue().equals("Invisible") || mode.getValue().equals("Basic")) && !isInTablist(entity)) {
			return true;
		}

		if ((mode.getValue().equals("Basic") || mode.getValue().equals("Advanced"))
				&& entity.ticksExisted <= 80) {
			return true;
		}

		if (mode.getValue().equals("Advanced") && !this.Grounded.contains(entity.getEntityId())) {
			return true;
		}

		if (mode.getValue().equals("Enclose") && hasBadlionBots((EntityPlayer) entity)) {
			return true;
		}

        return mode.getValue().equals("Invisible") && this.blacklisted.contains(entity.getEntityId());
    }

	public static List<EntityPlayer> getTabPlayerList() {
		NetHandlerPlayClient nhpc = mc.thePlayer.sendQueue;
		List<EntityPlayer> list = new ArrayList<>();
		List<NetworkPlayerInfo> players = GuiPlayerTabOverlay.field_175252_a.sortedCopy(nhpc.getPlayerInfoMap());
		for (final NetworkPlayerInfo o : players) {
            if (o == null) continue;
			list.add(mc.theWorld.getPlayerEntityByName(o.getGameProfile().getName()));
		}
		return list;
	}

	public static boolean isHypixelNPC(Entity entity) {
		if (!Client.getInstance().getModuleManager().getModule("AntiBots").isEnabled() || !mode.isMode("Hypixel")) return false;

		if (!(entity instanceof EntityPlayer)) return false;

		String formattedName = entity.getDisplayName().getFormattedText();
		String customName = entity.getCustomNameTag();

		if (!formattedName.startsWith("\247") && formattedName.endsWith("\247r")) {
			return true;
		}

        return formattedName.contains("[NPC]");
    }

	@EventTarget
	public void onTickon(EventTick event) {
		if (mc.theWorld == null) return;
		if (mode.getValue().equals("Invisible")) {
			for (EntityPlayer player : mc.theWorld.playerEntities) {
				if (this.InvisTicks.containsKey(player.getEntityId()) && this.InvisTicks.get(player.getEntityId()) > 40
						&& HypixelUtil.hasFakeInvisible(player)) {
					this.blacklisted.add(player.getEntityId());

					ChatUtil.log("removed Invisible bot (name:" + player.getDisplayName().getFormattedText() + ")");
				}
			}
		}

		if (mode.getValue().equals("Hypixel")) {
			for (EntityPlayer entity : mc.theWorld.playerEntities) {
				if (entity != mc.thePlayer && entity != null) {
					if (!getTabPlayerList().contains(entity)
							&& !entity.getDisplayName().getFormattedText().toLowerCase().contains("[npc")
							&& entity.getDisplayName().getFormattedText().startsWith("\u00a7")
							&& entity.isEntityAlive()) {
						if (!isHypixelNPC(entity) && entity.isInvisible()) {
							ChatUtil.log("removed Hypixel bot (name:" + entity.getDisplayName().getFormattedText() + ")");
							mc.theWorld.removeEntity(entity);
						}
					}
				}
			}
		}

		// grounded
		this.Grounded.addAll(mc.theWorld.playerEntities.stream()
				.filter(entityPlayer -> entityPlayer.onGround && !this.Grounded.contains(entityPlayer.getEntityId()))
				.map(EntityPlayer::getEntityId).collect(Collectors.toList()));

		// custom ticks
		for (EntityPlayer player : mc.theWorld.playerEntities) {
			if (!this.InvisTicks.containsKey(player.getEntityId()))
				this.InvisTicks.put(player.getEntityId(), 0);

			if (player.isInvisible() && HypixelUtil.hasFakeInvisible(player)) {
				this.InvisTicks.put(player.getEntityId(), this.InvisTicks.get(player.getEntityId()) + 1);
			} else {
				this.InvisTicks.put(player.getEntityId(), 0);
			}
		}
	}

	private boolean isWatchdoger(EntityPlayer entity) {
		Location myLoc = new Location(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ);
		Location targetLoc = new Location(entity.posX, entity.posY, entity.posZ);

        return entity.ticksExisted <= 20 && (entity.posY - mc.thePlayer.posY) > 8.0
                && myLoc.distanceToXZ(targetLoc) < 3.5;
    }
}