package net.torocraft.powerprobe;


import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

public class ClientProxy extends CommonProxy {

  @Override
  public void preInit(FMLPreInitializationEvent e) {
    super.preInit(e);
    OverlayRenderer.init();
  }

  @Override
  public void init(FMLInitializationEvent e) {
    super.init(e);
    ItemRedstoneProbe.registerRenders();
    PowerMeterGui.init();
    MouseHandler.init();
  }

  @Override
  public void postInit(FMLPostInitializationEvent e) {
    super.postInit(e);
  }
}
