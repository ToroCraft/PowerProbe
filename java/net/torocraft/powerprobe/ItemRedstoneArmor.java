package net.torocraft.powerprobe;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.util.EnumHelper;
import net.minecraftforge.fml.common.registry.GameRegistry;

public class ItemRedstoneArmor extends ItemArmor {

  public static final String NAME = PowerProbe.MODID + "_redstone_armor";

  public static ItemRedstoneArmor INSTANCE_HELMET;
  public static ArmorMaterial REDSTONE_ARMOR_MATERIAL = EnumHelper
      .addArmorMaterial("REDSTONE_POWER_PROBE", PowerProbe.MODID + ":" + PowerProbe.MODID + "_redstone_armor", 7, new int[]{2, 5, 3, 1}, 15, null, 0);


  public ItemRedstoneArmor(String unlocalizedName, int renderIndexIn, EntityEquipmentSlot equipmentSlotIn) {
    super(REDSTONE_ARMOR_MATERIAL, renderIndexIn, equipmentSlotIn);
    this.setUnlocalizedName(unlocalizedName);
    setMaxDamage(20);
  }

  public static void init() {
    initHelmet();
    GameRegistry.addRecipe(new ItemStack(ItemRedstoneArmor.INSTANCE_HELMET), "ggg", "grg", "   ", 'g', Items.GOLD_INGOT, 'r', Blocks.REDSTONE_BLOCK);
    GameRegistry.addRecipe(new ItemStack(ItemRedstoneArmor.INSTANCE_HELMET), "   ", "ggg", "grg", 'g', Items.GOLD_INGOT, 'r', Blocks.REDSTONE_BLOCK);
  }

  public static void registerRenders() {
    registerRendersHelmet();
  }

  private static void initHelmet() {
    INSTANCE_HELMET = new ItemRedstoneArmor(NAME + "_helmet", 0, EntityEquipmentSlot.HEAD);
    GameRegistry.register(INSTANCE_HELMET, new ResourceLocation(PowerProbe.MODID, NAME + "_helmet"));
  }

  private static void registerRendersHelmet() {
    Minecraft.getMinecraft().getRenderItem().getItemModelMesher()
        .register(INSTANCE_HELMET, 0, new ModelResourceLocation(PowerProbe.MODID + ":" + NAME + "_helmet", "inventory"));
  }

}
