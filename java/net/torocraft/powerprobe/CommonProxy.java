package net.torocraft.powerprobe;


import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

public class CommonProxy {

  public void preInit(FMLPreInitializationEvent e) {
    MessageUsingProbe.init(0);
    BlockPowerProbe.init();
    ItemRedstoneProbe.init();
  }

  public void init(FMLInitializationEvent e) {

  }

  public void postInit(FMLPostInitializationEvent e) {

  }
}
