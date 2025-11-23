package meldexun.nothirium.mc.util;

import net.minecraft.util.BlockPos;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;

public class LightUtil {

	public static int pack(int skyLight, int blockLight) {
		return ((skyLight & 15) << 20) | ((blockLight & 15) << 4);
	}

	public static int block(int combinedLight) {
		return (combinedLight >> 4) & 15;
	}

	public static int sky(int combinedLight) {
		return (combinedLight >> 20) & 15;
	}

	public static int getSkyLight(ExtendedBlockStorage section, BlockPos pos) {
		return section.getExtSkylightValue(pos.getX() & 15, pos.getY() & 15, pos.getZ() & 15);
	}

	public static int getBlockLight(ExtendedBlockStorage section, BlockPos pos) {
		return section.getExtBlocklightValue(pos.getX() & 15, pos.getY() & 15, pos.getZ() & 15);
	}

}
