package dev.lucaargolo.mekanismcovers;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import mekanism.common.block.transmitter.BlockTransmitter;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.Atlases;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.LightType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.client.model.data.EmptyModelData;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Map;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class ShaderCoverRendering {

    @SubscribeEvent
    public static void renderTransparentCovers(RenderWorldLastEvent event) {
        // Если Advanced выключен (режим Ordinary) или прозрачность не требуется, выходим
        if (!MekanismCoversClient.ADVANCED_COVER_RENDERING || !MekanismCoversClient.isCoverTransparentFast()) {
            return;
        }

        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;

        IRenderTypeBuffer.Impl bufferSource = mc.renderBuffers().bufferSource();
        IVertexBuilder baseConsumer = bufferSource.getBuffer(Atlases.translucentCullBlockSheet());

        IVertexBuilder transparentConsumer = new IVertexBuilder() {
            @Override
            public IVertexBuilder vertex(double x, double y, double z) { return baseConsumer.vertex(x, y, z); }
            @Override
            public IVertexBuilder color(int r, int g, int b, int a) { return baseConsumer.color(r, g, b, 85); }
            @Override
            public IVertexBuilder uv(float u, float v) { return baseConsumer.uv(u, v); }
            @Override
            public IVertexBuilder overlayCoords(int u, int v) { return baseConsumer.overlayCoords(u, v); }
            @Override
            public IVertexBuilder uv2(int u, int v) { return baseConsumer.uv2(u, v); }
            @Override
            public IVertexBuilder normal(float x, float y, float z) { return baseConsumer.normal(x, y, z); }
            @Override
            public void endVertex() { baseConsumer.endVertex(); }
        };

        MatrixStack matrixStack = event.getMatrixStack();
        ActiveRenderInfo camera = mc.gameRenderer.getMainCamera();
        Vector3d cameraPos = camera.getPosition();

        for (Map.Entry<BlockPos, BlockState> entry : MekanismCovers.POSSIBLE_BLOCKS.entrySet()) {
            BlockPos pos = entry.getKey();
            BlockState coverState = entry.getValue();

            if (coverState == null || !mc.level.hasChunkAt(pos)) continue;

            if (mc.level.getBlockState(pos).getBlock() instanceof BlockTransmitter) {
                matrixStack.pushPose();
                matrixStack.translate(pos.getX() - cameraPos.x, pos.getY() - cameraPos.y, pos.getZ() - cameraPos.z);

                int blockLight = mc.level.getBrightness(LightType.BLOCK, pos);
                int skyLight = mc.level.getBrightness(LightType.SKY, pos);
                int combinedLight = (skyLight << 20) | (blockLight << 4);

                net.minecraft.client.renderer.model.IBakedModel model = mc.getBlockRenderer().getBlockModel(coverState);

                mc.getBlockRenderer().getModelRenderer().renderModel(
                        matrixStack.last(),
                        transparentConsumer,
                        coverState,
                        model,
                        1.0f, 1.0f, 1.0f,
                        combinedLight,
                        OverlayTexture.NO_OVERLAY,
                        model.getModelData(mc.level, pos, coverState, EmptyModelData.INSTANCE)
                );

                matrixStack.popPose();
            }
        }
        
        bufferSource.endBatch(Atlases.translucentCullBlockSheet());
    }
}