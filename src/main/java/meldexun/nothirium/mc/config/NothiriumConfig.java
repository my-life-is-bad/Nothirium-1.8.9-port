package meldexun.nothirium.mc.config;

import java.io.File;
import meldexun.renderlib.util.GLUtil;
import net.minecraftforge.common.config.Configuration;

public class NothiriumConfig {

	public static Configuration config;

	public enum RenderEngine {

		AUTOMATIC, GL43, GL42, GL20, GL15;

	}

	public static RenderEngine renderEngine;

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


	public static void loadConfig(File file) {
        config = new Configuration(file);
        config.load();
        renderEngine = NothiriumConfig.parseEnum(config.get("general", "renderEngine", "AUTOMATIC", "Valid values: \nAUTOMATIC \nGL43 \nGL42 \nGL20 \nGL15").getString(), RenderEngine.AUTOMATIC);
    }

    public static void saveConfig(File file) {
        config = new Configuration(file);
        config.get("general", "renderEngine", "AUTOMATIC", "Valid values: \nAUTOMATIC \nGL43 \nGL42 \nGL20 \nGL15").setValue(renderEngine.name());
        if (config.hasChanged()) {
            config.save();
        }
    }

    private static <T extends Enum<T>> T parseEnum(String value, T defaultValue) {
        try {
            return Enum.valueOf(defaultValue.getDeclaringClass(), value.toUpperCase());
        }
        catch (IllegalArgumentException e) {
            return defaultValue;
        }
    }

}
