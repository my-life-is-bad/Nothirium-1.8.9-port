package meldexun.nothirium.mc.config;

import meldexun.nothirium.mc.Nothirium;
import meldexun.nothirium.mc.config.NothiriumConfig;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.common.config.ConfigElement;
import net.minecraftforge.fml.client.config.GuiConfig;
import net.minecraft.client.Minecraft;

public class NothiriumConfigGui extends GuiConfig {
    public NothiriumConfigGui(GuiScreen parent) {
        super(
                parent, 
                new ConfigElement(NothiriumConfig.config.getCategory(NothiriumConfig.GENERAL.getName())).getChildElements(),
                Nothirium.MODID, 
                false,
                false,
                "Nothirium"
        );
    }

    @Override
    protected void actionPerformed(GuiButton button){
        super.actionPerformed(button);
        
        if (button.id == 2000) {
            NothiriumConfig.config.save();
            NothiriumConfig.loadConfig(Nothirium.configFile);
            Minecraft.getMinecraft().renderGlobal.loadRenderers();
        }
    }
}

