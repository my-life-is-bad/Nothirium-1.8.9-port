package meldexun.nothirium.mc.integration;

import meldexun.nothirium.mc.renderer.ChunkRenderManager;
import meldexun.nothirium.mc.renderer.chunk.MinecraftChunkRenderer;
import meldexun.nothirium.mc.renderer.chunk.RenderChunkDispatcher;
import meldexun.renderlib.util.RenderUtil;

public class ReplayModIntegration {

    public static boolean isRendering = false;
    public static boolean isFirstFrame = true;

    public static void waitUntilChunksCompile() {
        if (isRendering) {
            RenderChunkDispatcher dispatcher = ChunkRenderManager.getTaskDispatcher();
            while (dispatcher.hasPendingAsyncTasks()) {
                try {
                    Thread.sleep(1);
                    dispatcher.update();
                } catch (Exception e) {
                    break;
                }
            }

            /* This fixes the first frame in the video to have only root chunk
            Cause : the chunks do not get compiled immediately and are not added to renderList.

            I am sure there is a better way to do this but other ways just add code that is
            almost never used.
            */

            if (isFirstFrame) {
                isFirstFrame = false;
                MinecraftChunkRenderer renderer = ChunkRenderManager.getRenderer();
                renderer.setup(ChunkRenderManager.getProvider(), RenderUtil.getCameraX(), RenderUtil.getCameraY(), RenderUtil.getCameraZ(), RenderUtil.getFrustum(), -1);
            }
        }
    }
}