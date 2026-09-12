package com.diasonti.beshnstuff;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.horse.Horse;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.Items;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

@EventBusSubscriber(modid = BeshNStuffMod.MODID)
public final class HorseMilkHandler {
    private HorseMilkHandler() {
    }

    @SubscribeEvent
    public static void milkHorse(PlayerInteractEvent.EntityInteract event) {
        final Entity target = event.getTarget();
        if (target instanceof Horse horseTarget
                && event.getItemStack().is(Items.BUCKET)) {
            if (horseTarget.isBaby()) {
                return;
            }

            event.getEntity().playSound(SoundEvents.COW_MILK, 1.0f, 1.0f);
            event.getEntity().setItemInHand(event.getHand(), ItemUtils.createFilledResult(
                    event.getItemStack(), event.getEntity(), Items.MILK_BUCKET.getDefaultInstance()));
            event.setCancellationResult(InteractionResult.sidedSuccess(event.getLevel().isClientSide()));
            event.setCanceled(true);
        }
    }
}
