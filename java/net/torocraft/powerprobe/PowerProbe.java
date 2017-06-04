package net.torocraft.powerprobe;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;

@Mod(modid = PowerProbe.MODID, name = PowerProbe.MODNAME, version = PowerProbe.VERSION)
public class PowerProbe {

  public static final String MODID = "powerprobe";
  public static final String MODNAME = "PowerProbe";
  public static final String VERSION = "1.11.2-2.0";

  @Mod.Instance(MODID)
  public static PowerProbe INSTANCE;

  public static SimpleNetworkWrapper NETWORK = NetworkRegistry.INSTANCE
      .newSimpleChannel(PowerProbe.MODID);

  @SidedProxy(clientSide = "net.torocraft.powerprobe.ClientProxy", serverSide = "net.torocraft.powerprobe.ServerProxy")
  public static CommonProxy proxy;

  @EventHandler
  public void preInit(FMLPreInitializationEvent e) {
    proxy.preInit(e);
  }

  @EventHandler
  public void init(FMLInitializationEvent e) {
    proxy.init(e);
  }

  @EventHandler
  public void postInit(FMLPostInitializationEvent e) {
    proxy.postInit(e);
  }

}
