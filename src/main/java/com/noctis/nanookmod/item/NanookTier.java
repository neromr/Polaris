package com.noctis.nanookmod.item;

import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Block;

/**
 * Material propio de la Lanza de Nanook: mas potente que netherite
 * (danio base de netherite = 4, aqui 6) para justificar que sea el
 * arma "muy poderosa" que desbloquea el logro oculto.
 */
public class NanookTier implements Tier {

    public static final Tier NANOOK = new NanookTier();

    @Override
    public int getUses() {
        return 2600;
    }

    @Override
    public float getSpeed() {
        return 9.0F;
    }

    @Override
    public float getAttackDamageBonus() {
        return 6.0F;
    }

    @Override
    public TagKey<Block> getIncorrectBlocksForDrops() {
        // Netherite no tiene su propio tag de nivel de minado: en vanilla,
        // las herramientas de netherite usan el mismo limite que diamante
        // (no hay ningun bloque que exija netherite especificamente).
        return BlockTags.INCORRECT_FOR_DIAMOND_TOOL;
    }

    @Override
    @SuppressWarnings("deprecation")
    public int getLevel() {
        // Deprecado desde hace varias versiones, pero en 1.20.1 la interfaz
        // Tier todavia lo declara como abstracto: hay que implementarlo si
        // o si para que la clase compile. 4 = un nivel por encima de
        // diamante (3), igual que usa Tiers.NETHERITE internamente.
        return 4;
    }

    @Override
    public int getEnchantmentValue() {
        return 20;
    }

    @Override
    public Ingredient getRepairIngredient() {
        return Ingredient.of(Items.NETHERITE_INGOT);
    }
}
