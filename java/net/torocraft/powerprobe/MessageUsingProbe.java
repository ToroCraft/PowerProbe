package net.torocraft.powerprobe;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

public class MessageUsingProbe implements IMessage {

  public enum Action {ADD, REMOVE}

  public Action action;
  public BlockPos target;
  public EnumFacing side;

  public static void init(int packetId) {
    PowerProbe.NETWORK.registerMessage(MessageUsingProbe.Handler.class, MessageUsingProbe.class, packetId, Side.SERVER);
  }

  public MessageUsingProbe() {

  }

  public MessageUsingProbe(Action action, BlockPos target, EnumFacing side) {
    this.action = action;
    this.target = target;
    this.side = side;
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

  private static final String NBT_KEY = "redstone_probe_location";

  public static class Handler implements IMessageHandler<MessageUsingProbe, IMessage> {

    @Override
    public IMessage onMessage(final MessageUsingProbe message, MessageContext ctx) {
      final EntityPlayerMP player = ctx.getServerHandler().playerEntity;
      player.getServerWorld().addScheduledTask(new Runnable() {
        @Override
        public void run() {
          NBTTagCompound data = player.getEntityData();
          removePreviousProbe(data, message, player);

          if (Action.ADD.equals(message.action)) {
            System.out.println("add");
            // add
            addProbe(player.world, message);
            data.setLong(NBT_KEY, message.target.toLong());

          } else {
            System.out.println("remove");
            // remove
            data.removeTag(NBT_KEY);
            removeProbe(player.world, message);
          }

        }
      });
      return null;
    }

    private void removePreviousProbe(NBTTagCompound data, MessageUsingProbe message, EntityPlayerMP player) {
      if (data.hasKey(NBT_KEY)) {
        BlockPos pos = BlockPos.fromLong(data.getLong(NBT_KEY));
        if (!pos.equals(message.target)) {
          System.out.println("removing old probe");
          removeProbe(player.world, pos);
        }
      }
    }

    private void removeProbe(World world, MessageUsingProbe message){
      removeProbe(world,  message.target.offset(message.side));
    }

    private void removeProbe(World world, BlockPos pos) {
      if (world.getBlockState(pos).getBlock() == BlockPowerProbe.INSTANCE) {
       world.setBlockState(pos, Blocks.AIR.getDefaultState());
      }
    }

    private BlockPos addProbe(World world, MessageUsingProbe message) {

      BlockPos probePos = message.target.offset(message.side);

      System.out.println("addProbe target[" + message.target + "] side[" + message.side + "] probe[" + probePos + "]");

      if (world.getBlockState(probePos).getBlock() == Blocks.AIR) {
        world.setBlockState(probePos, BlockPowerProbe.INSTANCE.getDefaultState().withProperty(BlockPowerProbe.FACING, message.side));
      } else {
        System.out.println("probe pos " + probePos + " was not air");
      }

      return probePos;
    }
  }
}