package meldexun.nothirium.mc.mixin.vertex;

import java.nio.ByteBuffer;

import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import meldexun.memoryutil.NIOBufferUtil;
import meldexun.nothirium.mc.vertex.ExtendedWorldRenderer;
import meldexun.nothirium.mc.vertex.ExtendedVertexFormatElement;
import meldexun.nothirium.util.VertexSortUtil;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.client.renderer.vertex.VertexFormatElement;

@Mixin(WorldRenderer.class)
public abstract class MixinWorldRenderer implements ExtendedWorldRenderer {

	@Shadow
	private ByteBuffer byteBuffer;
	@Shadow
	private int vertexCount;
	@Shadow
	private VertexFormatElement vertexFormatElement;
	@Shadow
	private int vertexFormatIndex;
	@Shadow
	private boolean noColor;
	@Shadow
	private double xOffset;
	@Shadow
	private double yOffset;
	@Shadow
	private double zOffset;
	@Shadow
	private VertexFormat vertexFormat;

	@Unique
	private long address;

	// @ModifyVariable(method = "<init>", at = @At("RETURN"), index = 1, ordinal = 0, name = "bufferSizeIn")
	// private int init(int bufferSizeIn) {
	// 	address = NIOBufferUtil.getAddress(byteBuffer);
	// 	return bufferSizeIn;
	// }

	@Inject(method={"<init>"}, at={@At(value="RETURN")})
    private void onInit(int bufferSizeIn, CallbackInfo ci) {
        address = NIOBufferUtil.getAddress(byteBuffer);
    }

	@ModifyVariable(method = "growBuffer", at = @At(value = "FIELD", target = "Lnet/minecraft/client/renderer/WorldRenderer;byteBuffer:Ljava/nio/ByteBuffer;", opcode = Opcodes.PUTFIELD, shift = Shift.AFTER), index = 1)
	private int growBuffer(int increaseAmount) {
		address = NIOBufferUtil.getAddress(byteBuffer);
		return increaseAmount;
	}

	@Overwrite
	public void sortVertexData(float cameraX, float cameraY, float cameraZ) {
		VertexSortUtil.sortVertexData(NIOBufferUtil.asMemoryAccess(byteBuffer), vertexCount, vertexFormat.getNextOffset(), 4,
				(float) (xOffset - cameraX), (float) (yOffset - cameraY), (float) (zOffset - cameraZ));
	}

	@Overwrite
	public WorldRenderer pos(double x, double y, double z) {
		((ExtendedVertexFormatElement) vertexFormatElement).getVertexConsumer().pos(this, x, y, z);
		this.nextVertexFormatIndex();
		return (WorldRenderer) (Object) this;
	}

	@Overwrite
	public WorldRenderer color(int red, int green, int blue, int alpha) {
		if (this.noColor) {
			return (WorldRenderer) (Object) this;
		}

		((ExtendedVertexFormatElement) vertexFormatElement).getVertexConsumer().color(this, red, green, blue, alpha);
		this.nextVertexFormatIndex();
		return (WorldRenderer) (Object) this;
	}

	@Overwrite
	public WorldRenderer tex(double u, double v) {
		((ExtendedVertexFormatElement) vertexFormatElement).getVertexConsumer().tex(this, u, v);
		this.nextVertexFormatIndex();
		return (WorldRenderer) (Object) this;
	}

	@Overwrite
	public WorldRenderer lightmap(int skyLight, int blockLight) {
		((ExtendedVertexFormatElement) vertexFormatElement).getVertexConsumer().lightmap(this, skyLight, blockLight);
		this.nextVertexFormatIndex();
		return (WorldRenderer) (Object) this;
	}

	@Overwrite
	public WorldRenderer normal(float x, float y, float z) {
		((ExtendedVertexFormatElement) vertexFormatElement).getVertexConsumer().normal(this, x, y, z);
		this.nextVertexFormatIndex();
		return (WorldRenderer) (Object) this;
	}

	@Overwrite
	public void nextVertexFormatIndex() {
		if (++vertexFormatIndex == vertexFormat.getElementCount()) {
			vertexFormatIndex = 0;
		}
		if ((vertexFormatElement = ((ExtendedVertexFormatElement) vertexFormatElement).getNext())
				.getUsage() == VertexFormatElement.EnumUsage.PADDING) {
			nextVertexFormatIndex();
		}
	}

	@Override
	public long getAddress() {
		return address;
	}

	@Override
	public int getOffset() {
		return vertexCount * vertexFormat.getNextOffset() + ((ExtendedVertexFormatElement) vertexFormatElement).getOffset();
	}

	@Override
	public double xOffset() {
		return xOffset;
	}

	@Override
	public double yOffset() {
		return yOffset;
	}

	@Override
	public double zOffset() {
		return zOffset;
	}

}
