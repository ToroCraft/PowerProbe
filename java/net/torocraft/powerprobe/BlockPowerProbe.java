package net.torocraft.powerprobe;

import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

// TODO break when target block breaks

// TODO show target on inner face

// TODO wires shouldn't attach

public class BlockPowerProbe extends Block {

  public static final PropertyDirection FACING = PropertyDirection.create("facing");

  public static BlockPowerProbe INSTANCE;
  public static final String NAME = PowerProbe.MODID + "_block_probe";

  public BlockPowerProbe() {
    super(Material.AIR);
    setDefaultState(blockState.getBaseState().withProperty(FACING, EnumFacing.DOWN));
    setUnlocalizedName(NAME);
  }

  public static void init() {
    INSTANCE = new BlockPowerProbe();
    ResourceLocation resourceName = new ResourceLocation(PowerProbe.MODID, NAME);
    INSTANCE.setRegistryName(resourceName);
    GameRegistry.register(INSTANCE);
  }

  public EnumBlockRenderType getRenderType(IBlockState state) {
    return EnumBlockRenderType.INVISIBLE;
  }

  public boolean requiresUpdates() {
    return true;
  }

  @Override
  public void randomTick(World worldIn, BlockPos pos, IBlockState state, Random random) {
    System.out.println("random tick");
    remove(worldIn, pos);
  }

  @Override
  public void updateTick(World worldIn, BlockPos pos, IBlockState state, Random rand) {
    System.out.println("update tick");
    remove(worldIn, pos);
  }

  protected static final AxisAlignedBB DOWN_AABB = new AxisAlignedBB(0.4D, 0.0D, 0.4D,    0.6D, 0.4D, 0.6D);
//  protected static final AxisAlignedBB TORCH_NORTH_AABB = new AxisAlignedBB(0.3499999940395355D, 0.20000000298023224D, 0.699999988079071D, 0.6499999761581421D, 0.800000011920929D, 1.0D);
//  protected static final AxisAlignedBB TORCH_SOUTH_AABB = new AxisAlignedBB(0.3499999940395355D, 0.20000000298023224D, 0.0D, 0.6499999761581421D, 0.800000011920929D, 0.30000001192092896D);
//  protected static final AxisAlignedBB TORCH_WEST_AABB = new AxisAlignedBB(0.699999988079071D, 0.20000000298023224D, 0.3499999940395355D, 1.0D, 0.800000011920929D, 0.6499999761581421D);
//  protected static final AxisAlignedBB TORCH_EAST_AABB = new AxisAlignedBB(0.0D, 0.20000000298023224D, 0.3499999940395355D, 0.30000001192092896D, 0.800000011920929D, 0.6499999761581421D);


  public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
    switch (state.getValue(FACING)) {
      case EAST:
        return DOWN_AABB;
      case WEST:
        return DOWN_AABB;
      case SOUTH:
        return DOWN_AABB;
      case NORTH:
        return DOWN_AABB;
      default:
        return DOWN_AABB;
    }
  }

  protected void updateState(World worldIn, BlockPos pos, IBlockState state) {

  }

  @Override
  public void onEntityCollidedWithBlock(World worldIn, BlockPos pos, IBlockState state, Entity entityIn) {
    //remove(worldIn, pos);
  }

  private void remove(World worldIn, BlockPos pos) {
    if (worldIn.getBlockState(pos).getBlock() == INSTANCE) {
      worldIn.setBlockState(pos, Blocks.AIR.getDefaultState());
    }
  }

  public boolean canProvidePower(IBlockState state) {
    return true;
  }

  public Item getItemDropped(IBlockState state, Random rand, int fortune) {
    return Items.AIR;
  }

  public int getStrongPower(IBlockState blockState, IBlockAccess blockAccess, BlockPos pos, EnumFacing side) {
    return blockState.getWeakPower(blockAccess, pos, side);
  }

  public int getWeakPower(IBlockState blockState, IBlockAccess blockAccess, BlockPos pos, EnumFacing side) {
    if (side == blockState.getValue(FACING)) {
      return 15;
    }
    return 0;
  }

  public void breakBlock(World worldIn, BlockPos pos, IBlockState state) {
    super.breakBlock(worldIn, pos, state);
    if (worldIn.isRemote) {
      return;
    }
    notifyWireNeighbors(worldIn, pos.offset(state.getValue(FACING)));
  }


  public void onBlockAdded(World worldIn, BlockPos pos, IBlockState state) {
    if (worldIn.isRemote) {
     return;
    }
    notifyWireNeighbors(worldIn, pos.offset(state.getValue(FACING)));
  }

  private void notifyWireNeighbors(World worldIn, BlockPos pos) {
    for (EnumFacing enumfacing : EnumFacing.values()) {
      worldIn.notifyNeighborsOfStateChange(pos.offset(enumfacing), this, false);
    }

    for (EnumFacing side : EnumFacing.values()) {
      this.notifyWireNeighborsOfStateChange(worldIn, pos.offset(side));
    }

    for (EnumFacing side : EnumFacing.values()) {
      BlockPos blockpos = pos.offset(side);

      if (worldIn.getBlockState(blockpos).isNormalCube()) {
        this.notifyWireNeighborsOfStateChange(worldIn, blockpos.up());
      } else {
        this.notifyWireNeighborsOfStateChange(worldIn, blockpos.down());
      }
    }
  }

  private void notifyWireNeighborsOfStateChange(World worldIn, BlockPos pos) {
    if (worldIn.getBlockState(pos).getBlock() == this) {
      worldIn.notifyNeighborsOfStateChange(pos, this, false);

      for (EnumFacing enumfacing : EnumFacing.values()) {
        worldIn.notifyNeighborsOfStateChange(pos.offset(enumfacing), this, false);
      }
    }
  }

  @Nullable
  public AxisAlignedBB getCollisionBoundingBox(IBlockState blockState, IBlockAccess worldIn, BlockPos pos) {
    return NULL_AABB;
  }

  public boolean isOpaqueCube(IBlockState state) {
    return false;
  }

  public boolean isFullCube(IBlockState state) {
    return false;
  }

  public boolean canPlaceBlockAt(World worldIn, BlockPos pos) {
    return worldIn.isAirBlock(pos);
  }

  protected BlockStateContainer createBlockState() {
    return new BlockStateContainer(this, new IProperty[]{FACING});
  }

  /**
   * Convert the given metadata into a BlockState for this Block
   */
  @Override
  public IBlockState getStateFromMeta(int meta) {
    IBlockState iblockstate = this.getDefaultState();

    switch (meta) {
      case 1:
        iblockstate = iblockstate.withProperty(FACING, EnumFacing.EAST);
        break;
      case 2:
        iblockstate = iblockstate.withProperty(FACING, EnumFacing.WEST);
        break;
      case 3:
        iblockstate = iblockstate.withProperty(FACING, EnumFacing.SOUTH);
        break;
      case 4:
        iblockstate = iblockstate.withProperty(FACING, EnumFacing.NORTH);
        break;
      case 5:
        iblockstate = iblockstate.withProperty(FACING, EnumFacing.DOWN);
        break;
      case 6:
        iblockstate = iblockstate.withProperty(FACING, EnumFacing.UP);
        break;
      default:
        iblockstate = iblockstate.withProperty(FACING, EnumFacing.DOWN);
        break;
    }
    return iblockstate;
  }

  @Override
  @SideOnly(Side.CLIENT)
  public BlockRenderLayer getBlockLayer() {
    return BlockRenderLayer.CUTOUT;
  }

  /**
   * Convert the BlockState into the correct metadata value
   */
  @Override
  public int getMetaFromState(IBlockState state) {
    int i = 0;

    switch (state.getValue(FACING)) {
      case EAST:
        i = i | 1;
        break;
      case WEST:
        i = i | 2;
        break;
      case SOUTH:
        i = i | 3;
        break;
      case NORTH:
        i = i | 4;
        break;
      case DOWN:
        i = i | 5;
        break;
      case UP:
        i = i | 6;
        break;
      default:
        i = i | 5;
        break;
    }
    return i;
  }

}
