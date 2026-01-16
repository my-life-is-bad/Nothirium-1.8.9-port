package meldexun.nothirium.mc;

import net.minecraft.client.Minecraft;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import meldexun.nothirium.mc.config.NothiriumConfig;
import meldexun.nothirium.mc.config.NothiriumConfig.RenderEngine;
import net.minecraftforge.common.MinecraftForge;
import my_life_is_bad.configurationsbackport.common.config.Config;
import my_life_is_bad.configurationsbackport.common.config.ConfigManager;
import net.minecraftforge.fml.client.event.ConfigChangedEvent.OnConfigChangedEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLConstructionEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Mod(modid = Nothirium.MODID, acceptableRemoteVersions = "*", dependencies = "required-after:renderlib@[1.4.2,);"+"required-after:configurationsbackport@[1.1,)")
public class Nothirium {

    public static final String MODID = "nothirium";
    public static final Logger LOGGER = LogManager.getLogger(MODID);

    @EventHandler
    public void onFMLConstructionEvent(FMLConstructionEvent event) {
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onConfigChangedEvent(OnConfigChangedEvent event) {
        if (event.modID.equals(MODID)) {
            RenderEngine oldRenderEngine = NothiriumConfig.renderEngine;

            ConfigManager.sync(MODID, Config.Type.INSTANCE);

            if (event.isWorldRunning && oldRenderEngine != NothiriumConfig.renderEngine) {
                Minecraft.getMinecraft().renderGlobal.loadRenderers();
            }
        }
    }

}