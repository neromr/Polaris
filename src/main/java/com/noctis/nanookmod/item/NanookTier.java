package com.noctis.nanookmod.item;

import com.noctis.nanookmod.NanookMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.ForgeTier;

public final class NanookTier {

    private NanookTier() {}

    private static final TagKey<Block> INCORRECT_FOR_NANOOK_TOOL =
            TagKey.create(Registries.BLOCK, new ResourceLocation(NanookMod.MOD_ID, "incorrect_for_nanook_tool"));

    public static final Tier NANOOK = new ForgeTier(
            4,
            2600,
            9.0F,
            6.0F,
            20,
            INCORRECT_FOR_NANOOK_TOOL,
            () -> Ingredient.of(Items.NETHERITE_INGOT)
    );
}
