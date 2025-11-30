package meldexun.nothirium.mc.config;

import java.io.File;
import java.util.Arrays;

import meldexun.renderlib.util.GLUtil;

import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.ConfigCategory;
import net.minecraftforge.common.config.Property;

public class NothiriumConfig {

	public static Configuration config;
	public static final ConfigCategory GENERAL = new ConfigCategory("general");

	public enum RenderEngine {
		AUTOMATIC, GL43, GL42, GL20, GL15;
	}

	public static RenderEngine getRenderEngine() {
		return getRenderEngine(renderEngine);
	}

	private static RenderEngine getRenderEngine(RenderEngine preferredRenderEngine) {
		switch (preferredRenderEngine) {
		case AUTOMATIC:
			if (GLUtil.CAPS.OpenGL43)
				return RenderEngine.GL43;
			if (GLUtil.CAPS.OpenGL42)
				return RenderEngine.GL42;
			if (GLUtil.CAPS.OpenGL20)
				return RenderEngine.GL20;
			if (GLUtil.CAPS.OpenGL15)
				return RenderEngine.GL15;
			throw new UnsupportedOperationException();
		case GL43:
			return GLUtil.CAPS.OpenGL43 ? RenderEngine.GL43 : getRenderEngine(RenderEngine.AUTOMATIC);
		case GL42:
			return GLUtil.CAPS.OpenGL42 ? RenderEngine.GL42 : getRenderEngine(RenderEngine.AUTOMATIC);
		case GL20:
			return GLUtil.CAPS.OpenGL20 ? RenderEngine.GL20 : getRenderEngine(RenderEngine.AUTOMATIC);
		case GL15:
			return GLUtil.CAPS.OpenGL15 ? RenderEngine.GL15 : getRenderEngine(RenderEngine.AUTOMATIC);
		default:
			throw new UnsupportedOperationException();
		}
	}

	/*=====GENERAL=====*/
	public static RenderEngine renderEngine;
	public static final String[] options = Arrays.stream(RenderEngine.values()).map(Enum::name).toArray(String[]::new);
	
	private static Property renderEngineProp;


	public static void loadConfig(File file) {
        config = new Configuration(file);
        config.load();

		/*=====GENERAL=====*/

		/*-Properties-*/
		renderEngineProp = config.get(GENERAL.getQualifiedName(), "renderEngine", "AUTOMATIC", "Valid values: \nAUTOMATIC \nGL43 \nGL42 \nGL20 \nGL15");
		renderEngineProp.setValidValues(options);
		

		/*-Variable assigning-*/
        renderEngine = RenderEngine.valueOf(renderEngineProp.getString());
    }

    public static void saveConfig() {
		/*=====GENERAL=====*/
        renderEngineProp.set(renderEngine.name());

        if (config.hasChanged()) {
            config.save();
        }
    }

}
