package net.torocraft.powerprobe;


import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.PlayerTickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;

// improve animated texture

// add config options

@SideOnly(Side.CLIENT)
public class PowerOverlayRender {

  private static final double T = 0.125f;
  private static final double[] TEXTURE_OFFSETS = new double[8];
  private static final ResourceLocation ICONS_TEXTURE = new ResourceLocation(PowerProbe.MODID, "textures/icons.png");

  private static final double O_ZER = -0.001;
  private static final double O_ONE = 1.001;
  private static final int RADIUS = 5;
  private static final PowerOverlayRender INSTANCE = new PowerOverlayRender();
  private static Minecraft mc;
  private PowerOverlayData cursorOverlay = new PowerOverlayData();
  private PowerOverlayData[] areaOverlays;

  public static void init() {
    mc = Minecraft.getMinecraft();
    for (int i = 0; i < 8; i++) {
      TEXTURE_OFFSETS[i] = ((double) i) / 8;
    }
    MinecraftForge.EVENT_BUS.register(INSTANCE);
  }

  @SubscribeEvent
  public void onRenderWorldLastEvent(RenderWorldLastEvent event) {
    EntityPlayerSP player = Minecraft.getMinecraft().player;
    render(player, event.getPartialTicks());
  }

  @SubscribeEvent
  public void onWorldUpdate(PlayerTickEvent event) {

    EntityPlayer player = event.player;
    World world = player.world;

    if (!world.isRemote || world.getTotalWorldTime() % 2 != 0) {
      return;
    }

    EntityPlayerSP playerSp = (EntityPlayerSP) player;

    resetOverlays();
    if (wearingHelmet()) {
      updateAreaScan(playerSp, world);
    } else {
      cursorOverlay = null;
    }

    PowerMeterGui.INSTANCE.setPower(0);
    if (holdingProbe()) {
      updateOverlayUnderCursor(world, playerSp);
    } else {
      if (cursorOverlay != null) {
        cursorOverlay.pos = null;
      }
    }
  }

  private void updateAreaScan(EntityPlayerSP player, World world) {
    initOverlays();
    scanArea(player, world);
  }

  private void updateOverlayUnderCursor(World world, EntityPlayerSP player) {
    if (cursorOverlay == null) {
      cursorOverlay = new PowerOverlayData();
    }
    cursorOverlay.pos = getBlockLookedAt(player);

    if (cursorOverlay.pos == null) {
      PowerMeterGui.INSTANCE.setPower(0);
      return;
    }

    updateOverlayForPos(world, cursorOverlay.pos, cursorOverlay);
    PowerMeterGui.INSTANCE.setPower(cursorOverlay.power);
  }

  private void resetOverlays() {
    if (areaOverlays == null) {
      return;
    }
    for (PowerOverlayData overlay : areaOverlays) {
      overlay.pos = null;
    }
  }

  private void initOverlays() {
    if (areaOverlays == null) {
      areaOverlays = new PowerOverlayData[(int) Math.pow((RADIUS * 2) + 1, 3)];
      for (int i = 0; i < INSTANCE.areaOverlays.length; i++) {
        INSTANCE.areaOverlays[i] = new PowerOverlayData();
      }
    }
  }

  private void scanArea(EntityPlayerSP player, World world) {
    int px = player.getPosition().getX();
    int py = player.getPosition().getY() + 1;
    int pz = player.getPosition().getZ();
    int i = 0;
    for (int x = px - RADIUS - 1; x < px + RADIUS; x++) {
      for (int y = py - RADIUS - 1; y < py + RADIUS; y++) {
        for (int z = pz - RADIUS - 1; z < pz + RADIUS; z++) {
          updateOverlayForPos(world, new BlockPos(x, y, z), areaOverlays[i++]);
        }
      }
    }
  }

  private void updateOverlayForPos(World world, BlockPos pos, PowerOverlayData overlay) {
    if (world.isAirBlock(pos)) {
      return;
    }
    overlay.pos = pos;
    overlay.power = world.getStrongPower(overlay.pos);
    if (overlay.power == 0) {
      overlay.strongPowered = false;
      overlay.power = world.isBlockIndirectlyGettingPowered(overlay.pos);
    } else {
      overlay.strongPowered = true;
    }
    if (!world.getBlockState(overlay.pos).isOpaqueCube()) {
      overlay.power = 0;
    }
  }

  private boolean holdingProbe() {
    if (mc.player == null) {
      return false;
    }
    for (ItemStack stack : mc.player.getHeldEquipment()) {
      if (!stack.isEmpty() && stack.getItem() == ItemRedstoneProbe.INSTANCE) {
        return true;
      }
    }
    return false;
  }

  private boolean wearingHelmet() {
    if (mc.player == null) {
      return false;
    }
    ItemStack helmet = mc.player.getItemStackFromSlot(EntityEquipmentSlot.HEAD);
    return !helmet.isEmpty() && helmet.getItem() == ItemRedstoneArmor.INSTANCE_HELMET;
  }

  private BlockPos getBlockLookedAt(EntityPlayerSP player) {
    RayTraceResult r = RayTracer.rayTrace(player, 15, 1);
    if (r != null && r.typeOfHit.equals(RayTraceResult.Type.BLOCK)) {
      return r.getBlockPos();
    }
    return null;
  }

  private void render(EntityPlayerSP player, float partialTicks) {
    double x = player.lastTickPosX + (player.posX - player.lastTickPosX) * partialTicks;
    double y = player.lastTickPosY + (player.posY - player.lastTickPosY) * partialTicks;
    double z = player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * partialTicks;

    TextureManager tm = Minecraft.getMinecraft().renderEngine;
    GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
    GL11.glPushMatrix();
    GL11.glColor4f(1.0f, 1.0f, 1.0f, 0.3f);
    GL11.glEnable(GL11.GL_BLEND);
    GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
    if (areaOverlays != null) {
      for (PowerOverlayData overlay : areaOverlays) {
        drawIconVectors(x, y, z, tm, overlay);
      }
    }
    drawIconVectors(x, y, z, tm, cursorOverlay);
    GL11.glPopMatrix();
    GL11.glPopAttrib();
  }

  private void drawIconVectors(double x, double y, double z, TextureManager tm, PowerOverlayData overlay) {
    if (overlay == null || overlay.pos == null || overlay.power < 1) {
      return;
    }
    VertexBuffer vb;
    tm.bindTexture(ICONS_TEXTURE);
    vb = Tessellator.getInstance().getBuffer();
    vb.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR);
    vb.setTranslation(-x, -y, -z);

    if (overlay.power > 0) {
      renderVectors(vb, overlay.pos, TEXTURE_OFFSETS[0],
          overlay.strongPowered ? TEXTURE_OFFSETS[0] : TEXTURE_OFFSETS[1]);
    }
    vb.setTranslation(0, 0, 0);
    Tessellator.getInstance().draw();
  }

  private void renderVectors(VertexBuffer vb, BlockPos pos, double u, double v) {
    double x = pos.getX();
    double y = pos.getY();
    double z = pos.getZ();

    //top
    vector(vb, x + O_ZER, y + O_ONE, z + O_ZER, u, v, T, T);
    vector(vb, x + O_ZER, y + O_ONE, z + O_ONE, u, v, T, 0);
    vector(vb, x + O_ONE, y + O_ONE, z + O_ONE, u, v, 0, 0);
    vector(vb, x + O_ONE, y + O_ONE, z + O_ZER, u, v, 0, T);

    //bottom
    vector(vb, x + O_ZER, y + O_ZER, z + O_ZER, u, v, T, T);
    vector(vb, x + O_ONE, y + O_ZER, z + O_ZER, u, v, T, 0);
    vector(vb, x + O_ONE, y + O_ZER, z + O_ONE, u, v, 0, 0);
    vector(vb, x + O_ZER, y + O_ZER, z + O_ONE, u, v, 0, T);

    //Z ZERO
    vector(vb, x + O_ZER, y + O_ZER, z + O_ZER, u, v, T, T);
    vector(vb, x + O_ZER, y + O_ONE, z + O_ZER, u, v, T, 0);
    vector(vb, x + O_ONE, y + O_ONE, z + O_ZER, u, v, 0, 0);
    vector(vb, x + O_ONE, y + O_ZER, z + O_ZER, u, v, 0, T);

    //Z ONE
    vector(vb, x + O_ZER, y + O_ZER, z + O_ONE, u, v, T, T);
    vector(vb, x + O_ONE, y + O_ZER, z + O_ONE, u, v, T, 0);
    vector(vb, x + O_ONE, y + O_ONE, z + O_ONE, u, v, 0, 0);
    vector(vb, x + O_ZER, y + O_ONE, z + O_ONE, u, v, 0, T);

    //X ZERO
    vector(vb, x + O_ZER, y + O_ZER, z + O_ZER, u, v, T, T);
    vector(vb, x + O_ZER, y + O_ZER, z + O_ONE, u, v, T, 0);
    vector(vb, x + O_ZER, y + O_ONE, z + O_ONE, u, v, 0, 0);
    vector(vb, x + O_ZER, y + O_ONE, z + O_ZER, u, v, 0, T);

    //X ONE
    vector(vb, x + O_ONE, y + O_ZER, z + O_ZER, u, v, T, T);
    vector(vb, x + O_ONE, y + O_ONE, z + O_ZER, u, v, T, 0);
    vector(vb, x + O_ONE, y + O_ONE, z + O_ONE, u, v, 0, 0);
    vector(vb, x + O_ONE, y + O_ZER, z + O_ONE, u, v, 0, T);
  }

  private void vector(VertexBuffer vb, double x, double y, double z, double u, double v, double oU, double oV) {
    vb.pos(x, y, z);
    vb.tex(u + oU, v + oV);
    vb.color(255, 255, 255, 255);
    vb.endVertex();
  }

}
