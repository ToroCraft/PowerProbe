package net.torocraft.powerprobe;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagLong;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemRedstoneProbe extends Item {

  public static ItemRedstoneProbe INSTANCE;
  public static String NAME = PowerProbe.MODID + "_probe";
  private static final String NBT_KEY_POS = "probe_pos";

  public static void init() {
    INSTANCE = new ItemRedstoneProbe();
    GameRegistry.register(INSTANCE, new ResourceLocation(PowerProbe.MODID, NAME));
  }

  @SideOnly(Side.CLIENT)
  public static void registerRenders() {
    Minecraft.getMinecraft().getRenderItem().getItemModelMesher().register(INSTANCE, 0, new ModelResourceLocation("minecraft:stick", "inventory"));
  }

  public ItemRedstoneProbe() {
    setUnlocalizedName(NAME);
    setCreativeTab(CreativeTabs.REDSTONE);
  }

  public EnumActionResult onItemUseeeee(EntityPlayer player, World worldIn, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {


    System.out.println("on use");

    ItemStack stack = player.getHeldItem(hand);

    if (stack.isEmpty() || stack.getItem() != INSTANCE) {
      return EnumActionResult.FAIL;
    }



    if(!worldIn.isRemote){
      stack.setTagInfo(NBT_KEY_POS, new NBTTagLong(pos.toLong()));
      System.out.println("place block");
      player.world.getStrongPower(pos);
      worldIn.setBlockState(pos.up(), BlockPowerProbe.INSTANCE.getDefaultState());

      worldIn.updateBlockTick(pos, BlockPowerProbe.INSTANCE, 10, -1);

      //worldIn.scheduleBlockUpdate(pos, BlockPowerProbe.INSTANCE, 1, -1);

    }



    return EnumActionResult.PASS;
  }

@SubscribeEvent
public void onStopUsing(LivingEntityUseItemEvent.Stop event) {
  System.out.println("stop using");
  if (event.getEntity().getEntityWorld().isRemote) {
    return;
  }

  removeProbeBlock(event.getItem(), event.getEntity().getEntityWorld());
}

  private void removeProbeBlock(ItemStack stack, World world) {
    if (stack.isEmpty() || stack.getItem() != INSTANCE || !stack.hasTagCompound()) {
      return;
    }

    long l = stack.getTagCompound().getLong(NBT_KEY_POS);

    if (l == 0) {
      System.out.println("no pos saved in stack");
      return;
    }

    BlockPos pos = BlockPos.fromLong(l);

    if (world.getBlockState(pos).getBlock() != BlockPowerProbe.INSTANCE) {
      System.out.println("not probe block");
      return;
    }

    world.setBlockState(pos, Blocks.AIR.getDefaultState());

    stack.removeSubCompound(NBT_KEY_POS);

  }

}
