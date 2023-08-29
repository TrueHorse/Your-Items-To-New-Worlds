package net.trueHorse.yourItemsToNewWorlds;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterClientCommandsEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.trueHorse.yourItemsToNewWorlds.commands.YourItemsToNewWorldsCommands;
import net.trueHorse.yourItemsToNewWorlds.network.YourItemsToNewWorldsPacketHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mod(YourItemsToNewWorlds.MODID)
public class YourItemsToNewWorlds
{
    // Define mod id in a common place for everything to reference
    public static final String MODID = "your_items_to_new_worlds";
    // Directly reference a slf4j logger
    public static final Logger LOGGER = LoggerFactory.getLogger("your_items_to_new_worlds");
    // Create a Deferred Register to hold Blocks which will all be registered under the "examplemod" namespace

    public YourItemsToNewWorlds()
    {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        // Register ourselves for server and other game events we are interested in
        MinecraftForge.EVENT_BUS.register(this);
        YourItemsToNewWorldsPacketHandler.registerPackets();
    }

    @Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.FORGE,value = Dist.CLIENT)
    public static class ModEvents{

        @SubscribeEvent
        public static void commonSetup(RegisterClientCommandsEvent event){
            YourItemsToNewWorldsCommands.registerClientCommands(event.getDispatcher(),event.getBuildContext());
        }

    }

    /*
    @Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class ModEvents{

        @SubscribeEvent
        public void commonSetup(FMLCommonSetupEvent event){
            LOGGER.error("registerFea");
            YourItemsToNewWorldsFeatures.registerFeatures();
        }

    }
     */
}