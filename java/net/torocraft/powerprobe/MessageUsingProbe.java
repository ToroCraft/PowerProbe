package net.torocraft.powerprobe;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.init.SoundEvents;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

public class MessageUsingProbe implements IMessage {

  private static final String NBT_KEY = "redstone_probe_location";
  public Action action;
  public BlockPos target;
  public EnumFacing side;
  public MessageUsingProbe() {

  }

  public MessageUsingProbe(Action action, BlockPos target, EnumFacing side) {
    this.action = action;
    this.target = target;
    this.side = side;
  }

  public static void init(int packetId) {
    PowerProbe.NETWORK.registerMessage(MessageUsingProbe.Handler.class, MessageUsingProbe.class, packetId, Side.SERVER);
  }

  private static boolean isReplaceableBlockAt(World world, BlockPos probePos) {
    return world.getBlockState(probePos).getBlock() == Blocks.AIR || world.getBlockState(probePos).getBlock() == BlockPowerProbe.INSTANCE;
  }

  @Override
  public void fromBytes(ByteBuf buf) {
    action = Action.values()[buf.readInt()];
    target = BlockPos.fromLong(buf.readLong());
    side = EnumFacing.values()[buf.readInt()];
  }

  @Override
  public void toBytes(ByteBuf buf) {
    buf.writeInt(action.ordinal());
    buf.writeLong(target.toLong());
    buf.writeInt(side.ordinal());
  }

  public enum Action {ADD, REMOVE}

  public static class Handler implements IMessageHandler<MessageUsingProbe, IMessage> {

    @Override
    public IMessage onMessage(final MessageUsingProbe message, MessageContext ctx) {
      final EntityPlayerMP player = ctx.getServerHandler().playerEntity;
      player.getServerWorld().addScheduledTask(new Runnable() {
        @Override
        public void run() {
          handle(player, message);
        }
      });
      return null;
    }

    private void handle(EntityPlayerMP player, MessageUsingProbe message) {
      NBTTagCompound data = player.getEntityData();
      removePreviousProbe(data, message, player);

      if (Action.ADD.equals(message.action)) {
        addProbe(player, message);
        data.setLong(NBT_KEY, message.target.toLong());

      } else {
        data.removeTag(NBT_KEY);
        removeProbe(player.world, message);
      }
    }

    private void removePreviousProbe(NBTTagCompound data, MessageUsingProbe message, EntityPlayerMP player) {
      if (data.hasKey(NBT_KEY)) {
        BlockPos pos = BlockPos.fromLong(data.getLong(NBT_KEY));
        if (!pos.equals(message.target)) {
          removeProbe(player.world, pos);
        }
      }
    }

    private void removeProbe(World world, MessageUsingProbe message) {
      removeProbe(world, message.target.offset(message.side));
    }

    private void removeProbe(World world, BlockPos pos) {
      if (world.getBlockState(pos).getBlock() == BlockPowerProbe.INSTANCE) {
        world.setBlockState(pos, Blocks.AIR.getDefaultState());
      }
    }

    private BlockPos addProbe(EntityPlayer player, MessageUsingProbe message) {
      BlockPos probePos = message.target.offset(message.side);
      if (isReplaceableBlockAt(player.world, probePos)) {
        player.world.setBlockState(probePos, BlockPowerProbe.INSTANCE.getDefaultState().withProperty(BlockPowerProbe.FACING, message.side));
        playSound(player);
      }
      return probePos;
    }

    private void playSound(EntityPlayer player) {
      player.world.playSound((EntityPlayer) null, player.posX, player.posY, player.posZ, SoundEvents.ITEM_HOE_TILL, SoundCategory.NEUTRAL, 0.5f,
          1f);
    }
  }
}
