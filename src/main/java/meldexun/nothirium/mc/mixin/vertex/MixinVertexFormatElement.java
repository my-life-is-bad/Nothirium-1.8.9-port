package meldexun.nothirium.mc.mixin.vertex;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


import meldexun.nothirium.mc.vertex.ColorUploader;
import meldexun.nothirium.mc.vertex.ExtendedVertexFormatElement;
import meldexun.nothirium.mc.vertex.NormalUploader;
import meldexun.nothirium.mc.vertex.PositionUploader;
import meldexun.nothirium.mc.vertex.TextureCoordinateUploader;
import meldexun.nothirium.mc.vertex.VertexConsumer;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.client.renderer.vertex.VertexFormatElement;
import net.minecraft.client.renderer.vertex.VertexFormatElement.EnumType;
import net.minecraft.client.renderer.vertex.VertexFormatElement.EnumUsage;

@Mixin(VertexFormatElement.class)
public class MixinVertexFormatElement implements ExtendedVertexFormatElement {

	@Shadow
	@Final
	private VertexFormatElement.EnumType type;
	@Shadow
	@Final
	private VertexFormatElement.EnumUsage usage;
	@Shadow
	@Final
	private int index;

	@Unique
	private VertexFormat vertexFormat;
	@Unique
	private int offset;
	@Unique
	private VertexFormatElement next;
	@Unique
	private VertexConsumer vertexConsumer;

	// @ModifyVariable(method = "<init>", at = @At("RETURN"), index = 1, ordinal = 0, name = "indexIn")
	// public int init(int indexIn) {
	// 	switch (usage) {
	// 	case POSITION:
	// 		vertexConsumer = PositionUploader.fromType(type);
	// 		break;
	// 	case COLOR:
	// 		vertexConsumer = ColorUploader.fromType(type);
	// 		break;
	// 	case UV:
	// 		vertexConsumer = TextureCoordinateUploader.fromType(type);
	// 		break;
	// 	case NORMAL:
	// 		vertexConsumer = NormalUploader.fromType(type);
	// 		break;
	// 	default:
	// 		break;
	// 	}
	// 	return indexIn;
	// }

	// @Override
    // public VertexConsumer getVertexConsumer() {
    //     if (this.vertexConsumer == null) {
    //         switch (this.field_177380_c) {
    //             case POSITION: {
    //                 this.vertexConsumer = PositionUploader.fromType(this.field_177379_b);
    //                 break;
    //             }
    //             case COLOR: {
    //                 this.vertexConsumer = ColorUploader.fromType(this.field_177379_b);
    //                 break;
    //             }
    //             case UV: {
    //                 this.vertexConsumer = TextureCoordinateUploader.fromType(this.field_177379_b);
    //                 break;
    //             }
    //             case NORMAL: {
    //                 this.vertexConsumer = NormalUploader.fromType(this.field_177379_b);
    //                 break;
    //             }
    //         }
    //     }
    //     return this.vertexConsumer;
    // }

	@Inject(method = "<init>", at = @At("RETURN"))
	private void onInit(int indexIn, EnumType type, EnumUsage usage, int count, CallbackInfo ci) {

		switch (this.usage) {
			case POSITION:
				this.vertexConsumer = PositionUploader.fromType(type);
				break;
			case COLOR:
				this.vertexConsumer = ColorUploader.fromType(type);
				break;
			case UV:
				this.vertexConsumer = TextureCoordinateUploader.fromType(type);
				break;
			case NORMAL:
				this.vertexConsumer = NormalUploader.fromType(type);
				break;
			default:
				break;
		}
	}
	@Override
	public void setVertexFormat(VertexFormat vertexFormat) {
		this.vertexFormat = vertexFormat;
	}

	@Override
	public VertexFormat getVertexFormat() {
		return vertexFormat;
	}

	@Override
	public void setOffset(int offset) {
		this.offset = offset;
	}

	@Override
	public int getOffset() {
		return offset;
	}

	@Override
	public void setNext(VertexFormatElement next) {
		this.next = next;
	}

	@Override
	public VertexFormatElement getNext() {
		return next;
	}

	@Override
	public VertexConsumer getVertexConsumer() {
		return vertexConsumer;
	}

}
