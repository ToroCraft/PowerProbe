package net.torocraft.powerprobe;


import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
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

  private static Minecraft mc;

  public static final PowerOverlayRender INSTANCE = new PowerOverlayRender();

  public static void init() {
    mc = Minecraft.getMinecraft();
    for (int i = 0; i < 8; i++) {
      TEXTURE_OFFSETS[i] = ((double) i) / 8;
    }
    MinecraftForge.EVENT_BUS.register(INSTANCE);
  }

  private double u, v;
  private BlockPos pos;
  private int power;
  private boolean strongPowered;

  @SubscribeEvent
  public void onRenderWorldLastEvent(RenderWorldLastEvent event) {
    EntityPlayerSP player = Minecraft.getMinecraft().player;

    if(notHoldingProbe()){
      return;
    }

    double x = player.lastTickPosX + (player.posX - player.lastTickPosX) * event.getPartialTicks();
    double y = player.lastTickPosY + (player.posY - player.lastTickPosY) * event.getPartialTicks();
    double z = player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * event.getPartialTicks();

    pos = getBlockLookedAt(player);

    if (pos == null) {
      return;
    }

    World world = player.world;

    power = world.getStrongPower(pos);

    if (power == 0) {
      strongPowered = false;
      power = world.isBlockIndirectlyGettingPowered(pos);
    } else {
      strongPowered = true;
    }

    if (!world.getBlockState(pos).isOpaqueCube()) {
      power = 0;
    }

    PowerMeterGui.INSTANCE.setPower(power);

    if (power < 1) {
      return;
    }

    render(world.getTotalWorldTime(), x, y, z);
  }

  private boolean notHoldingProbe() {
    if (mc.player == null) {
      return true;
    }
    for (ItemStack stack : mc.player.getHeldEquipment()) {
      if (!stack.isEmpty() && stack.getItem() == ItemRedstoneProbe.INSTANCE) {
        return false;
      }
    }
    return true;
  }

  private BlockPos getBlockLookedAt(EntityPlayerSP player) {
    RayTraceResult r = RayTracer.rayTrace(player, 15, 1);
    if (r != null && r.typeOfHit.equals(RayTraceResult.Type.BLOCK)) {
      return r.getBlockPos();
    }
    return null;
  }

  public void render(long time, double x, double y, double z) {
    TextureManager tm = Minecraft.getMinecraft().renderEngine;
    GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
    GL11.glPushMatrix();
    GL11.glColor4f(1.0f, 1.0f, 1.0f, 0.3f);
    GL11.glEnable(GL11.GL_BLEND);
    GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
    drawIconVectors(time, x, y, z, tm);
    GL11.glPopMatrix();
    GL11.glPopAttrib();
  }

  private void drawIconVectors(long time, double x, double y, double z, TextureManager tm) {
    VertexBuffer vb;
    tm.bindTexture(ICONS_TEXTURE);
    vb = Tessellator.getInstance().getBuffer();
    vb.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR);
    vb.setTranslation(-x, -y, -z);
    if (power > 0) {

      renderVectors(vb, pos, TEXTURE_OFFSETS[MathHelper.clamp((int)((time >> 1) % 5), 0, 5)], strongPowered ? TEXTURE_OFFSETS[0] : TEXTURE_OFFSETS[1]);
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
