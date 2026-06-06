package dev.lucaargolo.mekanismcovers;


import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

import java.util.List;

public class EmptyCoverItem extends Item {

    public EmptyCoverItem(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public void appendHoverText(ItemStack pStack, World pLevel, List<ITextComponent> pTooltipComponents, ITooltipFlag pIsAdvanced) {
        super.appendHoverText(pStack, pLevel, pTooltipComponents, pIsAdvanced);
        pTooltipComponents.add(new TranslationTextComponent("text.mekanismcovers.empty").withStyle(TextFormatting.GRAY, TextFormatting.ITALIC));
    }

    @Override
    public  String getDescriptionId() {
        return MekanismCovers.COVER.get().getDescriptionId();
    }
}
