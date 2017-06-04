package net.torocraft.powerprobe;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.RayTraceResult.Type;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent.MouseInputEvent;
import net.torocraft.powerprobe.MessageUsingProbe.Action;
import org.lwjgl.input.Mouse;

// TODO player sound when using, and maybe when turned off

public class MouseReleaseHandler {

  public static final MouseReleaseHandler INSTANCE = new MouseReleaseHandler();
  public boolean probeInUse;
  private BlockPos targetBlockPos;
  private EnumFacing targetBlockSide;

  public static void init() {
    MinecraftForge.EVENT_BUS.register(INSTANCE);
  }

  @SubscribeEvent(priority = EventPriority.NORMAL)
  public void handle(MouseInputEvent event) {
    if (shouldStopUsing()) {
      stopUsing();
    }
  }

  public void startUsing() {
    if (notHoldingProbe()) {
      return;
    }
    updateTargetedBlock();
    if (targetBlockPos != null && targetBlockSide != null) {
      PowerProbe.NETWORK.sendToServer(new MessageUsingProbe(Action.ADD, targetBlockPos, targetBlockSide));
      probeInUse = true;
    }
  }

  private void updateTargetedBlock() {
    targetBlockPos = null;
    targetBlockSide = null;

    EntityPlayerSP player = Minecraft.getMinecraft().player;
    double distance = Minecraft.getMinecraft().playerController.getBlockReachDistance();
    RayTraceResult r = RayTracer.rayTrace(player, distance, 1, false);

    if (r != null && Type.BLOCK.equals(r.typeOfHit)) {
      targetBlockPos = r.getBlockPos();
      targetBlockSide = r.sideHit;
    }
  }

  private void stopUsing() {
    if (notHoldingProbe()) {
      return;
    }
    PowerProbe.NETWORK.sendToServer(new MessageUsingProbe(Action.REMOVE, targetBlockPos, targetBlockSide));
    probeInUse = false;
  }

  private boolean notHoldingProbe() {
    if (Minecraft.getMinecraft().player == null) {
      return true;
    }
    for (ItemStack stack : Minecraft.getMinecraft().player.getHeldEquipment()) {
      if (!stack.isEmpty() && stack.getItem() == ItemRedstoneProbe.INSTANCE) {
        return false;
      }
    }
    return true;
  }

  private boolean shouldStopUsing() {
    return !Mouse.isButtonDown(1) && probeInUse;
  }

}
