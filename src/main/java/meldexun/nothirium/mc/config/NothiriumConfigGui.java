package meldexun.nothirium.mc.config;

import meldexun.nothirium.mc.Nothirium;
import meldexun.nothirium.mc.config.NothiriumConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.common.config.ConfigElement;
import net.minecraftforge.fml.client.config.GuiConfig;

public class NothiriumConfigGui
extends GuiConfig {
    public NothiriumConfigGui(GuiScreen parent) {
        super(parent, new ConfigElement(NothiriumConfig.config.getCategory("general")).getChildElements(), "nothirium", false, false, "Nothirium");
    }

    public void onGuiClosed() {
        super.onGuiClosed();
        NothiriumConfig.RenderEngine oldRenderEngine = NothiriumConfig.renderEngine;
        String newRenderEngine = NothiriumConfig.config.get("general", "renderEngine", "AUTOMATIC", "Valid values: \nAUTOMATIC \nGL43 \nGL42 \nGL20 \nGL15").getString();
        if (!NothiriumConfigGui.isValidEnum(newRenderEngine)) {
            NothiriumConfig.config.get("general", "renderEngine", "AUTOMATIC", "Valid values: \nAUTOMATIC \nGL43 \nGL42 \nGL20 \nGL15").setValue(oldRenderEngine.name().toUpperCase());
            return;
        }
        NothiriumConfig.config.get("general", "renderEngine", "AUTOMATIC", "Valid values: \nAUTOMATIC \nGL43 \nGL42 \nGL20 \nGL15").setValue(newRenderEngine.toUpperCase());
        NothiriumConfig.config.save();
        NothiriumConfig.loadConfig(Nothirium.configFile);
        if (oldRenderEngine != NothiriumConfig.renderEngine) {
            Minecraft.getMinecraft().renderGlobal.loadRenderers();
        }
    }

    private static boolean isValidEnum(String value) {
        try {
            NothiriumConfig.RenderEngine.valueOf(value.toUpperCase());
            return true;
        }
        catch (Exception e) {
            return false;
        }
    }
}

