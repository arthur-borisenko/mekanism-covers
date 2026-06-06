package dev.lucaargolo.mekanismcovers.mixin;

import dev.lucaargolo.mekanismcovers.MekanismCovers;
import dev.lucaargolo.mekanismcovers.mixed.TileEntityTransmitterMixed;
import mekanism.common.tile.base.CapabilityTileEntity;
import mekanism.common.tile.transmitter.TileEntityTransmitter;
import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.client.model.data.IModelData;
import net.minecraftforge.client.model.data.ModelDataMap;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = TileEntityTransmitter.class)
public abstract class TileEntityTransmitterMixin extends CapabilityTileEntity implements TileEntityTransmitterMixed {

    @Unique
    private BlockState mekanism_covers$coverState = null;
    @Unique
    private boolean mekanism_covers$updateClientLight = false;

    public TileEntityTransmitterMixin(TileEntityType<?> type) {
        super(type);
    }

    @Inject(at = @At("RETURN"), method = "getModelData", cancellable = true, remap=false)
    public void injectCoverModel(CallbackInfoReturnable<IModelData> cir) {
        IModelData data = cir.getReturnValue();
        if (this.mekanism_covers$coverState != null) {
            if (!(data instanceof ModelDataMap)) {
                data = new ModelDataMap.Builder().build();
            }
            ((ModelDataMap) data).setData(MekanismCovers.COVER_STATE, this.mekanism_covers$coverState);
            cir.setReturnValue(data);
        }
    }

    @Inject(at = @At("RETURN"), method = "save", cancellable = true, remap = true)
    public void injectSaveCover(CompoundNBT nbtTags, CallbackInfoReturnable<CompoundNBT> cir) {
        if (this.mekanism_covers$coverState != null) {
            CompoundNBT returnedNbt = cir.getReturnValue();
            returnedNbt.put("CoverState", NBTUtil.writeBlockState(this.mekanism_covers$coverState));
            cir.setReturnValue(returnedNbt);
        }
    }

    @Inject(at = @At("TAIL"), method = "load", remap = true)
    public void injectLoad(BlockState state, CompoundNBT nbtTags, CallbackInfo ci) {
        try {
            if (nbtTags.contains("CoverState")) {
                this.mekanism_covers$coverState = NBTUtil.readBlockState(nbtTags.getCompound("CoverState"));
                if (this.getLevel() != null && this.getLevel().isClientSide() && this.worldPosition != null) {
                    MekanismCovers.POSSIBLE_BLOCKS.put(this.worldPosition, this.mekanism_covers$coverState);
                }
            }
            if (this.getLevel() != null && this.worldPosition != null) {
                this.getLevel().getLightEngine().checkBlock(this.worldPosition);
            }
        } catch (Exception exception) {
            this.mekanism_covers$coverState = null;
        }
    }

    @Inject(at = @At("RETURN"), method = "getReducedUpdateTag", cancellable = true, remap = false)
    public void injectSaveCoverUpdateTag(CallbackInfoReturnable<CompoundNBT> cir) {
        CompoundNBT nbtTags = cir.getReturnValue();
        if (this.mekanism_covers$coverState != null) {
            nbtTags.put("CoverState", NBTUtil.writeBlockState(this.mekanism_covers$coverState));
            cir.setReturnValue(nbtTags);
        }
    }

    @Inject(at = @At("TAIL"), method = "handleUpdateTag", remap = false)
    public void injectUpdateTag(BlockState state, CompoundNBT nbtTags, CallbackInfo ci) {
        try {
            if (nbtTags.contains("CoverState")) {
                this.mekanism_covers$coverState = NBTUtil.readBlockState(nbtTags.getCompound("CoverState"));
                if (this.worldPosition != null) {
                    MekanismCovers.POSSIBLE_BLOCKS.put(this.worldPosition, this.mekanism_covers$coverState);
                }
                this.mekanism_covers$updateClientLight = true;
            }
        } catch (Exception exception) {
            this.mekanism_covers$coverState = null;
            if (this.worldPosition != null) {
                MekanismCovers.POSSIBLE_BLOCKS.remove(this.worldPosition);
            }
        }
    }

    @Inject(at = @At("HEAD"), method = "setRemoved", remap = true)
    public void injectUnload(CallbackInfo ci) {
        if (this.worldPosition != null) {
            MekanismCovers.POSSIBLE_BLOCKS.remove(this.worldPosition);
        }
    }

    @Override
    public void mekanism_covers$onUpdateClient() {
        if (this.mekanism_covers$updateClientLight) {
            if (this.getLevel() != null && this.worldPosition != null) {
                this.getLevel().getLightEngine().checkBlock(this.worldPosition);
                this.mekanism_covers$updateClientLight = false;
            }
        }
    }

    @Override
    public BlockState mekanism_covers$getCoverState() {
        return this.mekanism_covers$coverState;
    }

    @Override
    public void mekanism_covers$setCoverState(BlockState coverState) {
        this.mekanism_covers$coverState = coverState;
        if (this.getLevel() != null && this.getLevel().isClientSide() && this.worldPosition != null) {
            if (coverState != null) {
                MekanismCovers.POSSIBLE_BLOCKS.put(this.worldPosition, coverState);
            } else {
                MekanismCovers.POSSIBLE_BLOCKS.remove(this.worldPosition);
            }
        }
    }
}