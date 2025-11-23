package meldexun.nothirium.mc.util;

import meldexun.nothirium.api.renderer.chunk.ChunkRenderPass;
import net.minecraft.util.EnumWorldBlockLayer;

public class EnumWorldBlockLayerUtil {

	public static final EnumWorldBlockLayer[] ALL = EnumWorldBlockLayer.values();

	public static EnumWorldBlockLayer getEnumWorldBlockLayer(ChunkRenderPass pass) {
		return ALL[pass.ordinal()];
	}

	public static ChunkRenderPass getChunkRenderPass(EnumWorldBlockLayer layer) {
		return ChunkRenderPass.ALL[layer.ordinal()];
	}

}
