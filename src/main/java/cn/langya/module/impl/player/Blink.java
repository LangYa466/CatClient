package cn.langya.module.impl.player;

import cn.langya.event.annotations.EventTarget;
import cn.langya.event.events.EventPacket;
import cn.langya.event.events.EventRender2D;
import cn.langya.event.events.EventWorldLoad;
import cn.langya.module.Category;
import cn.langya.module.Module;
import cn.langya.ui.font.FontManager;
import cn.langya.ui.font.impl.UFontRenderer;
import cn.langya.utils.PacketUtil;
import cn.langya.utils.RenderUtil;
import cn.langya.value.impl.BooleanValue;
import cn.langya.value.impl.NumberValue;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.network.Packet;
import net.minecraft.network.handshake.client.C00Handshake;
import net.minecraft.network.login.client.C00PacketLoginStart;
import net.minecraft.network.login.client.C01PacketEncryptionResponse;
import net.minecraft.network.play.client.C01PacketChatMessage;
import net.minecraft.network.status.client.C00PacketServerQuery;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.Vec3;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class Blink extends Module {
    public final BooleanValue overlay = new BooleanValue("Overlay", false);
    public final List<Packet<?>> blinkedPackets = new ArrayList<>();
    private final BooleanValue pulse = new BooleanValue("Pulse", false);
    private final NumberValue pulseDelay = new NumberValue("Pulse delay", 1000, 0, 10000, 100);
    private final BooleanValue initialPosition = new BooleanValue("Show initial position", false);
    private long startTime = -1;
    private Vec3 pos;
    private final UFontRenderer fr = FontManager.hanYi(20);

    public Blink() {
        super(Category.Player);
    }

    @Override
    public void onEnable() {
        start();
    }

    private void start() {
        blinkedPackets.clear();
        pos = new Vec3(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ);
        startTime = System.currentTimeMillis();
    }

    public void onDisable() {
        reset();
    }

    private void reset() {
        synchronized (blinkedPackets) {
            for (Packet<?> packet : blinkedPackets) {
                PacketUtil.sendPacketNoEvent(packet);
            }
        }
        blinkedPackets.clear();
        pos = null;
    }

    @Override
    public String getSuffix() {
        return String.valueOf(blinkedPackets.size());
    }

    @EventTarget
    public void onRender2D(EventRender2D event) {
        if (!overlay.getValue() || mc.thePlayer == null) return;
        String displayText = "Blinking: " + blinkedPackets.size();
        fr.drawStringWithShadow(displayText, event.getScaledresolution().getScaledWidth() / 2.0F - fr.getStringWidth(displayText) / 2.0F, event.getScaledresolution().getScaledHeight() / 2.0F + 10, -1);

    }

    @EventTarget
    public void onSendPacket(EventPacket e) {
        if (!e.isSend()) return;
        if (mc.thePlayer == null) {
            this.setEnable(false);
            return;
        }
        Packet<?> packet = e.getPacket();
        if (packet.getClass().getSimpleName().startsWith("S")) {
            return;
        }
        if (packet instanceof C00Handshake
                || packet instanceof C00PacketLoginStart
                || packet instanceof C00PacketServerQuery
                || packet instanceof C01PacketEncryptionResponse
                || packet instanceof C01PacketChatMessage) {
            return;
        }
        blinkedPackets.add(packet);
        e.setCancelled();

        if (pulse.getValue()) {
            if (System.currentTimeMillis() - startTime >= pulseDelay.getValue()) {
                reset();
                start();
            }
        }
    }

    public static final int color = new Color(255, 255, 255, 200).getRGB();
    public static void drawBox(Vec3 pos) {
        GlStateManager.pushMatrix();
        double x = pos.xCoord - mc.getRenderManager().viewerPosX;
        double y = pos.yCoord - mc.getRenderManager().viewerPosY;
        double z = pos.zCoord - mc.getRenderManager().viewerPosZ;
        AxisAlignedBB bbox = mc.thePlayer.getEntityBoundingBox().expand(0.1D, 0.1, 0.1);
        AxisAlignedBB axis = new AxisAlignedBB(bbox.minX - mc.thePlayer.posX + x, bbox.minY - mc.thePlayer.posY + y, bbox.minZ - mc.thePlayer.posZ + z, bbox.maxX - mc.thePlayer.posX + x, bbox.maxY - mc.thePlayer.posY + y, bbox.maxZ - mc.thePlayer.posZ + z);
        float a = (float) (color >> 24 & 255) / 255.0F;
        float r = (float) (color >> 16 & 255) / 255.0F;
        float g = (float) (color >> 8 & 255) / 255.0F;
        float b = (float) (color & 255) / 255.0F;
        GL11.glBlendFunc(770, 771);
        GL11.glEnable(3042);
        GL11.glDisable(3553);
        GL11.glDisable(2929);
        GL11.glDepthMask(false);
        GL11.glLineWidth(2.0F);
        GL11.glColor4f(r, g, b, a);
        RenderUtil.drawBoundingBox(axis, r, g, b);
        GL11.glEnable(3553);
        GL11.glEnable(2929);
        GL11.glDepthMask(true);
        GL11.glDisable(3042);
        GlStateManager.popMatrix();
    }

    @EventTarget
    public void onRenderWorld(EventWorldLoad e) {
        if (mc.thePlayer == null || pos == null || !initialPosition.getValue()) return;
        Blink.drawBox(pos);
    }
}