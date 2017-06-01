package net.torocraft.powerprobe;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
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

  @Override
  public EnumActionResult onItemUse(EntityPlayer player, World worldIn, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {



    ItemStack stack = player.getHeldItem(hand);

    if (stack.isEmpty() || stack.getItem() != INSTANCE) {
      return EnumActionResult.FAIL;
    }

    if (MouseHandler.INSTANCE.probeInUse) {
      return EnumActionResult.SUCCESS;
    } else {
      MouseHandler.INSTANCE.startUsing();
      System.out.println("on use");
      return EnumActionResult.SUCCESS;
    }
  }


}
