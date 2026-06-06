package dev.lucaargolo.mekanismcovers;

import dev.lucaargolo.mekanismcovers.mixed.TileEntityTransmitterMixed;
import mekanism.client.model.BaseModelCache;
import net.minecraft.block.BlockState;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.SpecialRecipeSerializer;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraftforge.client.model.data.IModelData;
import net.minecraftforge.client.model.data.ModelProperty;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.network.PacketDistributor;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;


import java.util.HashMap;
import java.util.Map;

@Mod(MekanismCovers.MODID)
public class MekanismCovers {

    public static final String MODID = "mekanismcovers";

    public static final ModelProperty<BlockState> COVER_STATE = new ModelProperty<>();
    public static final ModelProperty<IModelData> COVER_DATA = new ModelProperty<>();

    public static final Map<BlockPos, BlockState> POSSIBLE_BLOCKS = new HashMap<>();

    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MODID);
    public static final RegistryObject<Item> EMPTY_COVER = ITEMS.register("empty_cover", () -> new EmptyCoverItem(new Item.Properties().tab(ItemGroup.TAB_BUILDING_BLOCKS)));
    public static final RegistryObject<Item> COVER = ITEMS.register("cover", () -> new CoverItem(new Item.Properties()));
    public static final DeferredRegister<IRecipeSerializer<?>> RECIPE_SERIALIZERS = DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, MODID);
    public static final RegistryObject<IRecipeSerializer<CoverRecipe>> COVER_SERIALIZER = RECIPE_SERIALIZERS.register("crafting_special_cover", () -> new SpecialRecipeSerializer<>(CoverRecipe::new));
    public static final ResourceLocation COVER_MODEL = new ResourceLocation(MODID, "block/cover");

    public MekanismCovers() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        ITEMS.register(modEventBus);
        RECIPE_SERIALIZERS.register(modEventBus);
    }


    public static void removeCover(World world, TileEntity tile, BlockState state, BlockPos pos, TileEntityTransmitterMixed transmitter, boolean update) {
        BlockState coverState = transmitter.mekanism_covers$getCoverState();
        ItemStack blockItemStack = coverState.getBlock().asItem().getDefaultInstance();
        ItemStack currentStack = new ItemStack(MekanismCovers.COVER.get());
        currentStack.getOrCreateTag().put("CoverBlockItem", blockItemStack.save(new CompoundNBT()));
        InventoryHelper.dropItemStack(world, pos.getX()+0.5, pos.getY()+0.5, pos.getZ()+0.5, currentStack);
        if(update) {
            transmitter.mekanism_covers$setCoverState(null);
            tile.setChanged();
            world.sendBlockUpdated(pos, state, state, 3);
            world.getLightEngine().checkBlock(pos);
        }
    }

}
