package net.torocraft.powerprobe;

import javax.annotation.Nullable;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class RayTracer {

  static boolean stopOnLiquid = false;
  static boolean ignoreBlockWithoutBoundingBox = false;
  static boolean returnLastUncollidableBlock = true;

  @Nullable
  @SideOnly(Side.CLIENT)
  public static RayTraceResult rayTrace(EntityPlayerSP player, double blockReachDistance, float partialTicks) {
    return rayTrace(player, blockReachDistance, partialTicks, true);
  }

  @Nullable
  @SideOnly(Side.CLIENT)
  public static RayTraceResult rayTrace(EntityPlayerSP player, double blockReachDistance, float partialTicks, boolean ignoreNonOpaqueCube) {
    Vec3d vec3d = player.getPositionEyes(partialTicks);
    Vec3d vec3d1 = player.getLook(partialTicks);
    Vec3d vec3d2 = vec3d.addVector(vec3d1.xCoord * blockReachDistance, vec3d1.yCoord * blockReachDistance, vec3d1.zCoord * blockReachDistance);
    return RayTracer.rayTraceBlocks(player.world, vec3d, vec3d2, ignoreNonOpaqueCube);
  }

  @Nullable
  public static RayTraceResult rayTraceBlocks(World world, Vec3d start, Vec3d lookVector, boolean ignoreNonOpaqueCube) {

    if (Double.isNaN(start.xCoord) || Double.isNaN(start.yCoord) || Double.isNaN(start.zCoord)) {
      return null;

    }

    if (Double.isNaN(lookVector.xCoord) || Double.isNaN(lookVector.yCoord) || Double.isNaN(lookVector.zCoord)) {
      return null;
    }

    int xLook = MathHelper.floor(lookVector.xCoord);
    int yLook = MathHelper.floor(lookVector.yCoord);
    int zLook = MathHelper.floor(lookVector.zCoord);
    int xStart = MathHelper.floor(start.xCoord);
    int yStart = MathHelper.floor(start.yCoord);
    int zStart = MathHelper.floor(start.zCoord);

    BlockPos pos1 = new BlockPos(xStart, yStart, zStart);
    IBlockState state = world.getBlockState(pos1);
    Block block = state.getBlock();

    boolean condition = isHit(state, block, ignoreNonOpaqueCube);

    if (condition) {
      RayTraceResult raytraceresult = state.collisionRayTrace(world, pos1, start, lookVector);

      if (raytraceresult != null) {
        return raytraceresult;
      }
    }

    RayTraceResult raytraceresult2 = null;
    int k1 = 200;

    while (k1-- >= 0) {
      if (Double.isNaN(start.xCoord) || Double.isNaN(start.yCoord) || Double.isNaN(start.zCoord)) {
        return null;
      }

      if (xStart == xLook && yStart == yLook && zStart == zLook) {
        return returnLastUncollidableBlock ? raytraceresult2 : null;
      }

      boolean xNotSame = true;
      boolean flag = true;
      boolean flag1 = true;
      double d0 = 999.0D;
      double d1 = 999.0D;
      double d2 = 999.0D;

      if (xLook > xStart) {
        d0 = (double) xStart + 1.0D;
      } else if (xLook < xStart) {
        d0 = (double) xStart + 0.0D;
      } else {
        xNotSame = false;
      }

      if (yLook > yStart) {
        d1 = (double) yStart + 1.0D;
      } else if (yLook < yStart) {
        d1 = (double) yStart + 0.0D;
      } else {
        flag = false;
      }

      if (zLook > zStart) {
        d2 = (double) zStart + 1.0D;
      } else if (zLook < zStart) {
        d2 = (double) zStart + 0.0D;
      } else {
        flag1 = false;
      }

      double d3 = 999.0D;
      double d4 = 999.0D;
      double d5 = 999.0D;
      double d6 = lookVector.xCoord - start.xCoord;
      double d7 = lookVector.yCoord - start.yCoord;
      double d8 = lookVector.zCoord - start.zCoord;

      if (xNotSame) {
        d3 = (d0 - start.xCoord) / d6;
      }

      if (flag) {
        d4 = (d1 - start.yCoord) / d7;
      }

      if (flag1) {
        d5 = (d2 - start.zCoord) / d8;
      }

      if (d3 == -0.0D) {
        d3 = -1.0E-4D;
      }

      if (d4 == -0.0D) {
        d4 = -1.0E-4D;
      }

      if (d5 == -0.0D) {
        d5 = -1.0E-4D;
      }

      EnumFacing side;

      if (d3 < d4 && d3 < d5) {
        side = xLook > xStart ? EnumFacing.WEST : EnumFacing.EAST;
        start = new Vec3d(d0, start.yCoord + d7 * d3, start.zCoord + d8 * d3);
      } else if (d4 < d5) {
        side = yLook > yStart ? EnumFacing.DOWN : EnumFacing.UP;
        start = new Vec3d(start.xCoord + d6 * d4, d1, start.zCoord + d8 * d4);
      } else {
        side = zLook > zStart ? EnumFacing.NORTH : EnumFacing.SOUTH;
        start = new Vec3d(start.xCoord + d6 * d5, start.yCoord + d7 * d5, d2);
      }

      xStart = MathHelper.floor(start.xCoord) - (side == EnumFacing.EAST ? 1 : 0);
      yStart = MathHelper.floor(start.yCoord) - (side == EnumFacing.UP ? 1 : 0);
      zStart = MathHelper.floor(start.zCoord) - (side == EnumFacing.SOUTH ? 1 : 0);

      pos1 = new BlockPos(xStart, yStart, zStart);
      IBlockState iblockstate1 = world.getBlockState(pos1);
      Block block1 = iblockstate1.getBlock();

      if (isHit(iblockstate1, block1, ignoreNonOpaqueCube)) {
        RayTraceResult raytraceresult1 = iblockstate1.collisionRayTrace(world, pos1, start, lookVector);

        if (raytraceresult1 != null) {
          return raytraceresult1;
        }
      } else {
        raytraceresult2 = new RayTraceResult(RayTraceResult.Type.MISS, start, side, pos1);
      }
    }

    return returnLastUncollidableBlock ? raytraceresult2 : null;
  }

  private static boolean isHit(IBlockState state, Block block1, boolean ignoreNonOpaqueCube) {
    if (ignoreNonOpaqueCube && !state.isOpaqueCube()) {
      return false;
    }
    return block1.canCollideCheck(state, stopOnLiquid);
  }


}
