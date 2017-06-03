package net.torocraft.powerprobe;

import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

// TODO wires shouldn't attach

public class BlockPowerProbe extends Block {

  public static final PropertyDirection FACING = PropertyDirection.create("facing");
  public static final String NAME = PowerProbe.MODID + "_block_probe";
  public static BlockPowerProbe INSTANCE;

  public BlockPowerProbe() {
    super(Material.CIRCUITS);
    setDefaultState(blockState.getBaseState().withProperty(FACING, EnumFacing.DOWN));
    setUnlocalizedName(NAME);
  }

  public static void init() {
    INSTANCE = new BlockPowerProbe();
    ResourceLocation resourceName = new ResourceLocation(PowerProbe.MODID, NAME);
    INSTANCE.setRegistryName(resourceName);
    GameRegistry.register(INSTANCE);
  }

  @Override
  public boolean requiresUpdates() {
    return true;
  }

  protected static final double BOX_UNIT = 0.0625D;
  protected static final AxisAlignedBB UP_AABB = createBox(7, 0, 7, 9, 1, 9);
  protected static final AxisAlignedBB DOWN_AABB = createBox(7, 15, 7, 9, 16, 9);
  protected static final AxisAlignedBB EAST_AABB = createBox(0, 7, 7, 1, 9, 9);
  protected static final AxisAlignedBB WEST_AABB = createBox(15, 7, 7, 16, 9, 9);
  protected static final AxisAlignedBB SOUTH_AABB = createBox(7, 7, 0, 9, 9, 1);
  protected static final AxisAlignedBB NORTH_AABB = createBox(7, 7, 15, 9, 9, 16);

  public static AxisAlignedBB createBox(int x1, int y1, int z1, int x2, int y2, int z2) {
    return new AxisAlignedBB(
        x1 * BOX_UNIT,
        y1 * BOX_UNIT,
        z1 * BOX_UNIT,

        x2 * BOX_UNIT,
        y2 * BOX_UNIT,
        z2 * BOX_UNIT
    );
  }

  @Override
  public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
    switch (state.getValue(FACING)) {
      case EAST:
        return EAST_AABB;
      case WEST:
        return WEST_AABB;
      case SOUTH:
        return SOUTH_AABB;
      case NORTH:
        return NORTH_AABB;
      case UP:
        return UP_AABB;
      default:
        return DOWN_AABB;
    }
  }

  private void remove(World worldIn, BlockPos pos) {
    if (worldIn.getBlockState(pos).getBlock() == INSTANCE) {
      worldIn.setBlockState(pos, Blocks.AIR.getDefaultState());
    }
  }

  @Override
  public boolean canProvidePower(IBlockState state) {
    return true;
  }

  public Item getItemDropped(IBlockState state, Random rand, int fortune) {
    return Items.AIR;
  }

  @Override
  public int getStrongPower(IBlockState blockState, IBlockAccess blockAccess, BlockPos pos, EnumFacing side) {
    return blockState.getWeakPower(blockAccess, pos, side);
  }

  @Override
  public int getWeakPower(IBlockState blockState, IBlockAccess blockAccess, BlockPos pos, EnumFacing side) {
    if (side == blockState.getValue(FACING)) {
      return 15;
    }
    return 0;
  }

  @Override
  public void breakBlock(World worldIn, BlockPos pos, IBlockState state) {
    super.breakBlock(worldIn, pos, state);
    if (worldIn.isRemote) {
      return;
    }
    notifyWireNeighbors(state, worldIn, pos.offset(state.getValue(FACING)));
  }

  @Override
  public void onBlockAdded(World worldIn, BlockPos pos, IBlockState state) {
    if (worldIn.isRemote) {
      return;
    }
    notifyWireNeighbors(state, worldIn, pos.offset(state.getValue(FACING)));
  }

  private void notifyWireNeighbors(IBlockState state, World worldIn, BlockPos posIn) {
    BlockPos pos = posIn.offset(state.getValue(FACING).getOpposite());
    for (EnumFacing side : EnumFacing.values()) {
      worldIn.notifyNeighborsOfStateChange(pos.offset(side), this, false);
    }
  }

  private void notifyWireNeighborsOLD(World worldIn, BlockPos pos) {
    for (EnumFacing side : EnumFacing.values()) {
      worldIn.notifyNeighborsOfStateChange(pos.offset(side), this, false);
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

      for (EnumFacing side : EnumFacing.values()) {
        worldIn.notifyNeighborsOfStateChange(pos.offset(side), this, false);
      }
    }
  }

  @Override
  @Nullable
  public AxisAlignedBB getCollisionBoundingBox(IBlockState blockState, IBlockAccess worldIn, BlockPos pos) {
    return NULL_AABB;
  }

  @Override
  public boolean isOpaqueCube(IBlockState state) {
    return false;
  }

  @Override
  public boolean isFullCube(IBlockState state) {
    return false;
  }

  @Override
  public boolean canPlaceBlockAt(World worldIn, BlockPos pos) {
    return worldIn.isAirBlock(pos);
  }

  @Override
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
