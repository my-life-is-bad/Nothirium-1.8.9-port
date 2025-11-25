package meldexun.nothirium.mc.renderer.chunk;

import java.util.Arrays;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.stream.IntStream;

import org.lwjgl.opengl.GL11;
import java.nio.ByteBuffer;

import meldexun.memoryutil.NIOBufferUtil;
import meldexun.nothirium.api.renderer.chunk.ChunkRenderPass;
import meldexun.nothirium.api.renderer.chunk.IChunkRenderer;
import meldexun.nothirium.api.renderer.chunk.IRenderChunkDispatcher;
import meldexun.nothirium.api.renderer.chunk.RenderChunkTaskResult;
import meldexun.nothirium.mc.util.EnumWorldBlockLayerUtil;
import meldexun.nothirium.mc.util.EnumFacingUtil;
import meldexun.nothirium.renderer.chunk.AbstractRenderChunkTask;
import meldexun.nothirium.util.Direction;
import meldexun.nothirium.util.VertexSortUtil;
import meldexun.nothirium.util.VisibilityGraph;
import meldexun.nothirium.util.VisibilitySet;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.RegionRenderCacheBuilder;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.util.EnumWorldBlockLayer;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.BlockPos;
import net.minecraft.util.BlockPos.MutableBlockPos;
import net.minecraft.util.Vec3;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.client.ForgeHooksClient;

public class RenderChunkTaskCompile extends AbstractRenderChunkTask<RenderChunk> {

	private static final BlockingQueue<RegionRenderCacheBuilder> BUFFER_QUEUE = new LinkedBlockingQueue<>();
	
	
	private static final int BYTES_PER_BUFFER = 8 * 1024 * 1024; // 8MB per buffer
	private static final int MIN_BUFFERS = 1;
	private static final int BUFFER_COUNT;
	static {
		long maxMemBytes = Runtime.getRuntime().maxMemory();

		long maxBufferBytes = (long)(maxMemBytes * 0.3);

		int buffersByMemory = (int)(maxBufferBytes / BYTES_PER_BUFFER);


		int cores = Runtime.getRuntime().availableProcessors();
		int buffersByCores = (cores - 2) * 2;						
	/* this is the original formula used in Nothirium before commit
	   "Fix mesh generation for CPUs with less than 3 threads" */

		BUFFER_COUNT = Math.max(MIN_BUFFERS, Math.min(buffersByMemory, buffersByCores));	// calculate the num of buffers by buffersByCores and buffersByMemory
		for (int i = 0; i < BUFFER_COUNT; ++i) {
			BUFFER_QUEUE.add(new RegionRenderCacheBuilder());
		}

		System.out.println("BufferQueue size : " + BUFFER_COUNT);
	}



	private final IBlockAccess chunkCache;

	public RenderChunkTaskCompile(IChunkRenderer<?> chunkRenderer, IRenderChunkDispatcher taskDispatcher, RenderChunk renderChunk, IBlockAccess chunkCache) {
		super(chunkRenderer, taskDispatcher, renderChunk);
		this.chunkCache = chunkCache;
	}

	private static void freeBuffer(RegionRenderCacheBuilder buffer) {
		for (EnumWorldBlockLayer layer : EnumWorldBlockLayerUtil.ALL) {
			WorldRenderer bufferBuilder = buffer.getWorldRendererByLayer(layer);
			if (bufferBuilder.isDrawing) {
				bufferBuilder.finishDrawing();
			}
			bufferBuilder.reset();
			bufferBuilder.setTranslation(0, 0, 0);
		}
		BUFFER_QUEUE.add(buffer);
	}

	@Override
	public RenderChunkTaskResult run() {
		if (chunkCache instanceof SectionRenderCache) {
			((SectionRenderCache) chunkCache).initCaches();
		}
		try {
			return compileSection();
		} finally {
			if (chunkCache instanceof SectionRenderCache) {
				((SectionRenderCache) chunkCache).freeCaches();
			}
		}
	}

	private RenderChunkTaskResult compileSection() {

		// RegionRenderCacheBuilder bufferBuilderPack = BUFFER_QUEUE.poll();		1.12.2 has great memory management so we can just let the chunks compile when buffere queue is full
		// if (bufferBuilderPack == null) {											but in 1.8.9 memory management is crap and we just get java.lang.OutOfMemoryError: Direct buffer memory
		// 	bufferBuilderPack = new RegionRenderCacheBuilder();					
		// }


		RegionRenderCacheBuilder bufferBuilderPack;	 // so we use this :
		try {
			bufferBuilderPack = BUFFER_QUEUE.take(); // blocks until a buffer is free
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			return RenderChunkTaskResult.CANCELLED;
		}


		boolean freeBufferBuilderPack = true;
		try {
			freeBufferBuilderPack = compileSection(bufferBuilderPack) != RenderChunkTaskResult.SUCCESSFUL;
		} finally {
			if (freeBufferBuilderPack) {
				freeBuffer(bufferBuilderPack);
			}
		}

		return this.canceled() ? RenderChunkTaskResult.CANCELLED : RenderChunkTaskResult.SUCCESSFUL;
	}

	private RenderChunkTaskResult compileSection(RegionRenderCacheBuilder bufferBuilderPack) {
		if (this.canceled()) {
			return RenderChunkTaskResult.CANCELLED;
		}

		Minecraft mc = Minecraft.getMinecraft();
		MutableBlockPos pos = new MutableBlockPos();
		VisibilityGraph visibilityGraph = new VisibilityGraph();

		for (int x = 0; x < 16; x++) {
			for (int y = 0; y < 16; y++) {
				for (int z = 0; z < 16; z++) {
					pos.set(this.renderChunk.getX() + x, this.renderChunk.getY() + y, this.renderChunk.getZ() + z);
					IBlockState blockState = this.chunkCache.getBlockState(pos);
					renderBlockState(blockState, pos, visibilityGraph, bufferBuilderPack);
				}
			}

			if (this.canceled()) {
				return RenderChunkTaskResult.CANCELLED;
			}
		}

		VisibilitySet visibilitySet = visibilityGraph.compute();

		if (bufferBuilderPack.getWorldRendererByLayer(EnumWorldBlockLayer.TRANSLUCENT).isDrawing) {
			Entity entity = mc.getRenderViewEntity();
			if (entity != null) {
				WorldRenderer bufferBuilder = bufferBuilderPack.getWorldRendererByLayer(EnumWorldBlockLayer.TRANSLUCENT);
				Vec3 camera = entity.getPositionEyes(1.0F);
				VertexSortUtil.sortVertexData(NIOBufferUtil.asMemoryAccess(bufferBuilder.getByteBuffer()), bufferBuilder.getVertexCount(), bufferBuilder.getVertexFormat().getNextOffset(), 4,
						(float) (renderChunk.getX() - camera.xCoord), (float) (renderChunk.getY() - camera.yCoord), (float) (renderChunk.getZ() - camera.zCoord));
			}
		}

		if (this.canceled()) {
			return RenderChunkTaskResult.CANCELLED;
		}

		WorldRenderer[] finishedBufferBuilders = Arrays.stream(EnumWorldBlockLayerUtil.ALL).map(bufferBuilderPack::getWorldRendererByLayer).map(bufferBuilder -> {
			if (!bufferBuilder.isDrawing) {
				return null;
			}
			bufferBuilder.finishDrawing();
			if (bufferBuilder.getVertexCount() == 0) {
				return null;
			}
			return bufferBuilder;
		}).toArray(WorldRenderer[]::new);

		if (this.canceled()) {
			return RenderChunkTaskResult.CANCELLED;
		}

		this.taskDispatcher.runOnRenderThread(() -> {
			try {
				if (!this.canceled()) {
					this.renderChunk.setVisibility(visibilitySet);
					for (ChunkRenderPass pass : ChunkRenderPass.ALL) {
						WorldRenderer bufferBuilder = finishedBufferBuilders[pass.ordinal()];
						if (bufferBuilder == null) {
							this.renderChunk.setVBOPart(pass, null);
						} else {
							this.renderChunk.setVBOPart(pass, this.chunkRenderer.buffer(pass, bufferBuilder.getByteBuffer()));
							if (pass == ChunkRenderPass.TRANSLUCENT) {
								this.renderChunk.setTranslucentVertexData(NIOBufferUtil.copyAsUnsafeBuffer(bufferBuilder.getByteBuffer()));
							}
						}
					}
				}
			} finally {
				freeBuffer(bufferBuilderPack);
			}
		});

		return RenderChunkTaskResult.SUCCESSFUL;
	}

	public void renderBlockState(IBlockState blockState, BlockPos pos, VisibilityGraph visibilityGraph, RegionRenderCacheBuilder bufferBuilderPack) {
		// if (blockState.getRenderType() == EnumBlockRenderType.INVISIBLE) {
		// 	return;
		// }
		Block block = blockState.getBlock();
        if (block.getRenderType() == 0) {
            return;
        }

		for (Direction dir : Direction.ALL) {
			if (block.isSideSolid(this.chunkCache, pos, EnumFacingUtil.getFacing(dir))) {//blockState.doesSideBlockRendering(this.chunkCache, pos, EnumFacingUtil.getFacing(dir))
				visibilityGraph.setOpaque(pos.getX(), pos.getY(), pos.getZ(), dir);
			}
		}

		for (EnumWorldBlockLayer layer : EnumWorldBlockLayerUtil.ALL) {
			if (block.getBlockLayer() != layer) { //!blockState.getBlock().canRenderInLayer(blockState, layer)
				continue;
			}
			ForgeHooksClient.setRenderLayer(layer);
			WorldRenderer bufferBuilder = bufferBuilderPack.getWorldRendererByLayer(layer);
			if (!bufferBuilder.isDrawing) {
				bufferBuilder.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);
				bufferBuilder.setTranslation(-this.renderChunk.getX(), -this.renderChunk.getY(), -this.renderChunk.getZ());
			}
			Minecraft.getMinecraft().getBlockRendererDispatcher().renderBlock(blockState, pos, this.chunkCache, bufferBuilder);
			ForgeHooksClient.setRenderLayer(null);
		}
	}

}
