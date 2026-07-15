package com.noctis.nanookmod.item;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;

/**
 * Lanza de Nanook: recompensa del crafteo oculto que se desbloquea con
 * el logro "La Bendicion Nanook". Es una SwordItem para heredar el
 * barrido y las mecanicas de combate estandar, con danio muy por encima
 * de netherite y un efecto de "frio" al golpear como toque tematico.
 */
public class NanookSpearItem extends SwordItem {

    public NanookSpearItem(Properties properties) {
        // tier, danio base adicional, modificador de velocidad de ataque
        super(NanookTier.NANOOK, 4, -2.8F, properties);
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 40, 1));
        return super.hurtEnemy(stack, target, attacker);
    }
}
