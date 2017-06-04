package net.torocraft.powerprobe;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class PowerMeterGui extends Gui {

  public static final PowerMeterGui INSTANCE = new PowerMeterGui();

  private static final int COLOR = 0xFFFFFF;
  private static final Minecraft mc = Minecraft.getMinecraft();
  private ScaledResolution viewport;
  private int power;
  private boolean enabled = true;

  public static void init() {
    MinecraftForge.EVENT_BUS.register(INSTANCE);
  }

  public int getPower() {
    return power;
  }

  public void setPower(int power) {
    this.power = power;
  }

  @SubscribeEvent
  public void drawHealthBar(RenderGameOverlayEvent.Post event) {
    if (isRunEvent(event)) {
      draw();
    }
  }

  protected boolean isRunEvent(RenderGameOverlayEvent event) {
    return enabled && event.getType() == ElementType.EXPERIENCE;
  }

  private void draw() {
    if (power < 1) {
      return;
    }
    viewport = new ScaledResolution(mc);
    drawString(mc.fontRendererObj, Math.round(power) + "", viewport.getScaledWidth() / 2 + 10, viewport.getScaledHeight() / 2 - 3, COLOR);
  }

}
