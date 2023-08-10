package net.trueHorse.yourItemsToNewWorlds;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.trueHorse.yourItemsToNewWorlds.feature.YourItemsToNewWorldsFeatures;
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

        // Register the commonSetup method for modloading
        modEventBus.addListener(this::commonSetup);

        // Register ourselves for server and other game events we are interested in
        MinecraftForge.EVENT_BUS.register(this);
    }

    private void commonSetup(final FMLCommonSetupEvent event)
    {
        YourItemsToNewWorldsFeatures.registerFeatures();
    }
}