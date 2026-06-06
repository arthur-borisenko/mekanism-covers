package dev.lucaargolo.mekanismcovers.mixin;

import net.minecraft.item.Item;
import net.minecraft.util.math.shapes.EntitySelectionContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(EntitySelectionContext.class)
public interface EntityCollisionContextAccessor {

    @Accessor("heldItem")
    Item getHeldItem();

}
