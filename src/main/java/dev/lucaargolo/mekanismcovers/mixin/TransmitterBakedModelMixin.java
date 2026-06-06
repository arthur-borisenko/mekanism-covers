package dev.lucaargolo.mekanismcovers.mixin;

import dev.lucaargolo.mekanismcovers.MekanismCovers;
import dev.lucaargolo.mekanismcovers.MekanismCoversClient;
import mekanism.client.render.obj.TransmitterBakedModel;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderTypeLookup;
import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockDisplayReader;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.client.model.data.EmptyModelData;
import net.minecraftforge.client.model.data.IModelData;
import net.minecraftforge.client.model.data.ModelDataMap;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Mixin(value = TransmitterBakedModel.class, remap = false)
public abstract class TransmitterBakedModelMixin implements IBakedModel {

    @Inject(at = @At("RETURN"), method = "getQuads(Lnet/minecraft/block/BlockState;Lnet/minecraft/util/Direction;Ljava/util/Random;Lnet/minecraftforge/client/model/data/IModelData;)Ljava/util/List;", cancellable = true)
    public void injectCoverModel(@Nullable BlockState state, @Nullable Direction side, Random rand, IModelData extraData, CallbackInfoReturnable<List<BakedQuad>> cir) {
        if (extraData == null || !extraData.hasProperty(MekanismCovers.COVER_STATE)) {
            return;
        }

        List<BakedQuad> originalQuads = cir.getReturnValue();
        BlockState coverState = extraData.getData(MekanismCovers.COVER_STATE);

        if (coverState != null) {
            Minecraft minecraft = Minecraft.getInstance();
            boolean transparent = MekanismCoversClient.isCoverTransparentFast();
            RenderType currentLayer = MinecraftForgeClient.getRenderLayer();
            
            List<BakedQuad> combinedQuads = new ArrayList<>(originalQuads);
            IModelData data = extraData.getData(MekanismCovers.COVER_DATA);
            if (data == null) {
                data = EmptyModelData.INSTANCE;
            }

            if (transparent) {
                // --- РЕЖИМ ПРОЗРАЧНОСТИ (Инструмент в руках) ---
                if (currentLayer == RenderType.cutout()) {
                    if (MekanismCoversClient.ADVANCED_COVER_RENDERING) {
                        // 1. Advanced режим: в чанке крышку НЕ рендерим.
                        // Отрисовкой займется ShaderCoverRendering (RenderWorldLastEvent)
                        cir.setReturnValue(originalQuads);
                    } else {
                        // 2. Ordinary режим: рендерим альтернативную модель-заглушку в чанке
                        IBakedModel altModel = minecraft.getModelManager().getModel(MekanismCovers.COVER_MODEL);
                        combinedQuads.addAll(altModel.getQuads(Blocks.AIR.defaultBlockState(), side, rand, data));
                        cir.setReturnValue(combinedQuads);
                    }
                } else {
                    cir.setReturnValue(originalQuads);
                }
            } else {
                // --- СТАНДАРТНЫЙ РЕЖИМ (Полная видимость) ---
                RenderType coverLayer = RenderTypeLookup.getChunkRenderType(coverState);
                IBakedModel bakedModel = minecraft.getBlockRenderer().getBlockModel(coverState);

                if (currentLayer == null || currentLayer == coverLayer || currentLayer == RenderType.cutout()) {
                    combinedQuads.addAll(bakedModel.getQuads(coverState, side, rand, data));
                }
                cir.setReturnValue(combinedQuads);
            }
        }
    }

    @Inject(at = @At("RETURN"), method = "getParticleTexture", cancellable = true)
    public void injectParticleTexture(IModelData extraData, CallbackInfoReturnable<TextureAtlasSprite> cir) {
        if (extraData != null && extraData.hasProperty(MekanismCovers.COVER_STATE)) {
            BlockState coverState = extraData.getData(MekanismCovers.COVER_STATE);
            if (coverState != null) {
                IBakedModel bakedModel = Minecraft.getInstance().getBlockRenderer().getBlockModel(coverState);
                cir.setReturnValue(bakedModel.getParticleTexture(extraData));
            }
        }
    }

    @Inject(at = @At("HEAD"), method = "getModelData", cancellable = true)
    public void injectGetModelData(@Nonnull IBlockDisplayReader world, @Nonnull BlockPos pos, @Nonnull BlockState state, @Nonnull IModelData tileData, CallbackInfoReturnable<IModelData> cir) {
        if (tileData.hasProperty(MekanismCovers.COVER_STATE)) {
            BlockState coverState = tileData.getData(MekanismCovers.COVER_STATE);
            IModelData resultData = tileData;

            if (!(resultData instanceof ModelDataMap)) {
                ModelDataMap.Builder builder = new ModelDataMap.Builder();
                builder.withInitial(MekanismCovers.COVER_STATE, coverState);
                ModelDataMap newMap = builder.build();
                newMap.setData(MekanismCovers.COVER_DATA, MekanismCoversClient.getModelData(coverState, world, pos));
                resultData = newMap;
            } else {
                ((ModelDataMap) resultData).setData(MekanismCovers.COVER_DATA, MekanismCoversClient.getModelData(coverState, world, pos));
            }

            if (tileData.hasProperty(mekanism.common.tile.transmitter.TileEntityTransmitter.TRANSMITTER_PROPERTY) && resultData instanceof ModelDataMap) {
                ((ModelDataMap) resultData).setData(
                        mekanism.common.tile.transmitter.TileEntityTransmitter.TRANSMITTER_PROPERTY,
                        tileData.getData(mekanism.common.tile.transmitter.TileEntityTransmitter.TRANSMITTER_PROPERTY)
                );
            }

            cir.setReturnValue(resultData);
        }
    }
}