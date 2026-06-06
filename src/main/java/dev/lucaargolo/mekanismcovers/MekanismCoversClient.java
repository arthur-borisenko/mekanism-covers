package dev.lucaargolo.mekanismcovers;

import dev.lucaargolo.mekanismcovers.mixed.TileEntityTransmitterMixed;
import mekanism.api.providers.IBlockProvider;
import mekanism.common.block.transmitter.BlockTransmitter;
import mekanism.common.item.ItemConfigurator;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.registries.MekanismItems;
import mekanism.common.tile.transmitter.TileEntityTransmitter;
import mekanism.common.util.WorldUtils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ItemOverrideList;
import net.minecraft.client.renderer.model.ModelResourceLocation;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.IBlockDisplayReader;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ColorHandlerEvent;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.client.model.data.IModelData;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import javax.annotation.Nullable;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;

import static dev.lucaargolo.mekanismcovers.MekanismCovers.COVER_MODEL;
import static dev.lucaargolo.mekanismcovers.MekanismCovers.MODID;

@Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class MekanismCoversClient {

    public static final boolean ADVANCED_COVER_RENDERING = !ModConfig.getInstance().isDisableAdvancedCoverRendering();
    public static final boolean SHADER_COVER_RENDERING = ModConfig.getInstance().isEnableShaderCompatibleRendering();

    private static boolean lastTransparency = false;

    @SubscribeEvent
    public static void registerCoverModel(ModelRegistryEvent event) {
        ModelLoader.addSpecialModel(COVER_MODEL);
    }

    @SubscribeEvent
    public static void onModelBake(ModelBakeEvent event) {
        ModelResourceLocation coverItemModelLoc = new ModelResourceLocation(MODID + ":cover", "inventory");
        IBakedModel existingModel = event.getModelRegistry().get(coverItemModelLoc);
        if (existingModel != null) {
            event.getModelRegistry().put(coverItemModelLoc, new CoverItemBakedModel(existingModel));
        }
    }

    @SubscribeEvent
    public static void blockColorsRegister(ColorHandlerEvent.Block event) {
        Block[] transmitters = MekanismBlocks.BLOCKS.getAllBlocks().stream()
                .map(IBlockProvider::getBlock)
                .filter(block -> block instanceof BlockTransmitter)
                .collect(Collectors.toList())
                .toArray(new Block[0]);

        event.getBlockColors().register((pState, pLevel, pPos, pTintIndex) -> {
            if (pPos != null && pLevel != null) {
                TileEntityTransmitter tile = WorldUtils.getTileEntity(TileEntityTransmitter.class, pLevel, pPos);
                if (tile instanceof TileEntityTransmitterMixed) {
                    TileEntityTransmitterMixed transmitter = (TileEntityTransmitterMixed) tile;
                    BlockState coverState = transmitter.mekanism_covers$getCoverState();
                    if (coverState != null) {
                        return event.getBlockColors().getColor(coverState, pLevel, pPos, pTintIndex);
                    }
                }
            }
            return 0xFFFFFF;
        }, transmitters);
    }

    public static void updateCoverTransparency() {
        boolean transparency = isCoverTransparent();
        if (transparency != lastTransparency) {
            Minecraft client = Minecraft.getInstance();
            if (client.player == null || client.level == null) {
                return;
            }

            // Получаем координаты секций чанков только для тех блоков, где установлены крышки,
            // и отправляем их на моментальную перерисовку без фризов
            for (BlockPos pos : MekanismCovers.POSSIBLE_BLOCKS.keySet()) {
                int secX = pos.getX() >> 4;
                int secY = pos.getY() >> 4;
                int secZ = pos.getZ() >> 4;

                client.levelRenderer.setSectionDirty(secX, secY, secZ);
            }
        }
        lastTransparency = transparency;
    }

    public static boolean isCoverTransparentFast() {
        return lastTransparency;
    }

    private static boolean isCoverTransparent() {
        ClientPlayerEntity player = Minecraft.getInstance().player;
        if (player != null) {
            ItemStack mainStack = player.getMainHandItem();
            ItemStack offStack = player.getOffhandItem();
            ItemStack[] stacks = new ItemStack[]{mainStack, offStack};
            boolean transparent = false;
            for (ItemStack stack : stacks) {
                if (stack.getItem() == MekanismItems.CONFIGURATOR.get()) {
                    ItemConfigurator.ConfiguratorMode mode = MekanismItems.CONFIGURATOR.get().getMode(stack);
                    if (mode != ItemConfigurator.ConfiguratorMode.WRENCH) {
                        transparent = true;
                        break;
                    }
                } else {
                    Item item = stack.getItem();
                    if (item instanceof BlockItem) {
                        Block block = ((BlockItem) item).getBlock();
                        if (block instanceof BlockTransmitter) {
                            transparent = true;
                            break;
                        }
                    }
                }
            }
            return transparent;
        } else {
            return false;
        }
    }

    public static IModelData getModelData(BlockState state, IBlockDisplayReader level, BlockPos worldPosition) {
        return Minecraft.getInstance().getBlockRenderer().getBlockModel(state).getModelData(level, worldPosition, state, net.minecraftforge.client.model.data.EmptyModelData.INSTANCE);
    }

    @SuppressWarnings("rawtypes")
    public static boolean hasShaderPack() {
        try {
            Class<?> irisClass = Thread.currentThread().getContextClassLoader().loadClass("net.irisshaders.iris.Iris");
            Method packMethod = irisClass.getDeclaredMethod("getCurrentPack");
            Optional optional = (Optional) packMethod.invoke(null);
            return optional.isPresent();
        } catch (Exception ignored) {
            return false;
        }
    }

    private static class CoverItemBakedModel implements IBakedModel {
        private final IBakedModel originalModel;
        private final ItemOverrideList overrides;

        public CoverItemBakedModel(IBakedModel originalModel) {
            this.originalModel = originalModel;
            this.overrides = new ItemOverrideList() {
                @Nullable
                @Override
                public IBakedModel resolve(IBakedModel model, ItemStack stack, @Nullable ClientWorld world, @Nullable LivingEntity entity) {
                    Block coverBlock = CoverItem.getBlock(stack);
                    if (coverBlock != null && coverBlock != Blocks.AIR) {
                        return Minecraft.getInstance().getBlockRenderer().getBlockModel(coverBlock.defaultBlockState());
                    }
                    return originalModel;
                }
            };
        }

        @Override
        public List<net.minecraft.client.renderer.model.BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, Random rand) {
            return originalModel.getQuads(state, side, rand);
        }

        @Override public boolean useAmbientOcclusion() { return originalModel.useAmbientOcclusion(); }
        @Override public boolean isGui3d() { return originalModel.isGui3d(); }
        @Override public boolean usesBlockLight() { return originalModel.usesBlockLight(); }
        @Override public boolean isCustomRenderer() { return originalModel.isCustomRenderer(); }
        @Override public net.minecraft.client.renderer.texture.TextureAtlasSprite getParticleIcon() { return originalModel.getParticleIcon(); }
        @Override public net.minecraft.client.renderer.model.ItemCameraTransforms getTransforms() { return originalModel.getTransforms(); }
        @Override public ItemOverrideList getOverrides() { return overrides; }
    }
}