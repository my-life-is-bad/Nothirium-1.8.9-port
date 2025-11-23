package meldexun.nothirium.mc.config;

import java.util.Set;
import meldexun.nothirium.mc.config.NothiriumConfigGui;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.fml.client.IModGuiFactory;

public class NothiriumConfigGuiFactory
implements IModGuiFactory {
    public void initialize(Minecraft minecraft) {
    }

    public Class<? extends GuiScreen> mainConfigGuiClass() {
        return NothiriumConfigGui.class;
    }

    public Set<IModGuiFactory.RuntimeOptionCategoryElement> runtimeGuiCategories() {
        return null;
    }

    public IModGuiFactory.RuntimeOptionGuiHandler getHandlerFor(IModGuiFactory.RuntimeOptionCategoryElement runtimeOptionCategoryElement) {
        return null;
    }
}

