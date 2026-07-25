package meldexun.nothirium.mc.asm.compatibility;

import meldexun.asmutil2.ASMUtil;
import meldexun.asmutil2.IClassTransformerRegistry;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

public class ReplayModTransformer {
    public static void registerTransformers(IClassTransformerRegistry registry) {
        // skip this.forceChunkLoadingHook = new ForceChunkLoadingHook(this.mc.renderGlobal); and sets isRendering flag true
        registry.add("com.replaymod.render.rendering.VideoRenderer", "setup", "()V", 2, (methodNode) -> {
            AbstractInsnNode startNode = ASMUtil.first(methodNode).typeInsn("com/replaymod/render/hooks/ForceChunkLoadingHook").find();
            startNode = ASMUtil.prev(methodNode, startNode).opcode(Opcodes.ALOAD).find();

            AbstractInsnNode endNode = ASMUtil.first(methodNode).fieldInsn("com/replaymod/render/rendering/VideoRenderer", "forceChunkLoadingHook", "Lcom/replaymod/render/hooks/ForceChunkLoadingHook;").opcode(Opcodes.PUTFIELD).find();

            LabelNode skipLabel = new LabelNode();

            FieldInsnNode FieldNode = new FieldInsnNode(Opcodes.PUTSTATIC, "meldexun/nothirium/mc/integration/ReplayModIntegration", "isRendering", "Z");
            FieldInsnNode FieldNode2 = new FieldInsnNode(Opcodes.PUTSTATIC, "meldexun/nothirium/mc/integration/ReplayModIntegration", "isFirstFrame", "Z");

            methodNode.instructions.insert(endNode, skipLabel);
            methodNode.instructions.insert(ASMUtil.listOf(new InsnNode(Opcodes.ICONST_1), FieldNode, new InsnNode(Opcodes.ICONST_1), FieldNode2));
            methodNode.instructions.insertBefore(startNode, new JumpInsnNode(Opcodes.GOTO, skipLabel));
        });

        // skip this.forceChunkLoadingHook.uninstall(); and sets isRendering flag false
        registry.add("com.replaymod.render.rendering.VideoRenderer", "finish", "()V", 2, (methodNode) -> {
            AbstractInsnNode startNode = ASMUtil.first(methodNode).methodInsn("com/replaymod/render/hooks/ForceChunkLoadingHook", "uninstall", "()V").find();
            startNode = ASMUtil.prev(methodNode, startNode).opcode(Opcodes.ALOAD).find();

            AbstractInsnNode endNode = ASMUtil.first(methodNode).methodInsn("com/replaymod/render/hooks/ForceChunkLoadingHook", "uninstall", "()V").opcode(Opcodes.INVOKEVIRTUAL).find();

            LabelNode skipLabel = new LabelNode();

            FieldInsnNode FieldNode = new FieldInsnNode(Opcodes.PUTSTATIC, "meldexun/nothirium/mc/integration/ReplayModIntegration", "isRendering", "Z");

            methodNode.instructions.insert(endNode, skipLabel);
            methodNode.instructions.insert(ASMUtil.listOf(new InsnNode(Opcodes.ICONST_0), FieldNode));
            methodNode.instructions.insertBefore(startNode, new JumpInsnNode(Opcodes.GOTO, skipLabel));
        });

        // run RenderUtil.update(renderPartialTicks) at updateForNextFrame
        registry.add("com.replaymod.render.rendering.VideoRenderer", "updateForNextFrame", "()F", 2, methodNode -> {
            AbstractInsnNode returnNode = ASMUtil.first(methodNode).opcode(Opcodes.FRETURN).find();

            methodNode.instructions.insertBefore(returnNode, ASMUtil.listOf(
                    new InsnNode(Opcodes.DUP),
                    new InsnNode(Opcodes.F2D),
                    new MethodInsnNode(Opcodes.INVOKESTATIC, "meldexun/renderlib/util/RenderUtil", "update", "(D)V", false)
            ));
        });

        // run ReplayModIntegration.waitUntilChunksCompile() at the end of ChunkRenderManager.setup()
        registry.add("meldexun.nothirium.mc.renderer.ChunkRenderManager", "setup", "()V", 2, methodNode -> {
            AbstractInsnNode returnNode = ASMUtil.first(methodNode).opcode(Opcodes.RETURN).find();

            methodNode.instructions.insertBefore(returnNode, new MethodInsnNode(Opcodes.INVOKESTATIC, "meldexun/nothirium/mc/integration/ReplayModIntegration", "waitUntilChunksCompile", "()V", false));
        });
    }
}
