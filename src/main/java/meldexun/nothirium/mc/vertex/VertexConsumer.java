package meldexun.nothirium.mc.vertex;

public interface VertexConsumer {

	default void pos(ExtendedWorldRenderer buffer, double x, double y, double z) {
		// ignore
	}

	default void color(ExtendedWorldRenderer buffer, int red, int green, int blue, int alpha) {
		// ignore
	}

	default void tex(ExtendedWorldRenderer buffer, double u, double v) {
		// ignore
	}

	default void lightmap(ExtendedWorldRenderer buffer, int skyLight, int blockLight) {
		// ignore
	}

	default void normal(ExtendedWorldRenderer buffer, float x, float y, float z) {
		// ignore
	}

}
