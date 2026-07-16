package com.noctis.nanookmod.item;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraftforge.common.ForgeMod;

import java.util.UUID;

public class NanookSpearItem extends SwordItem {

    private static final UUID REACH_MODIFIER_ID = UUID.fromString("b3a9c1d2-4e5f-4a1b-9c3d-7e8f9a0b1c2d");

    public NanookSpearItem(Properties properties) {
        super(NanookTier.NANOOK, 6, -2.8F, properties);
    }

    @Override
    public Multimap<Attribute, AttributeModifier> getDefaultAttributeModifiers(EquipmentSlot slot) {
        Multimap<Attribute, AttributeModifier> modifiers = HashMultimap.create(super.getDefaultAttributeModifiers(slot));
        if (slot == EquipmentSlot.MAINHAND) {
            modifiers.put(ForgeMod.ENTITY_REACH.get(),
                    new AttributeModifier(REACH_MODIFIER_ID, "Alcance de la Lanza de Nanook", 2.5, AttributeModifier.Operation.ADDITION));
        }
        return modifiers;
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 40, 2));
        return super.hurtEnemy(stack, target, attacker);
    }
}
