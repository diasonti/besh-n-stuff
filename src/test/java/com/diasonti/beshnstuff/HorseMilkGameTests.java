package com.diasonti.beshnstuff;

import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.horse.Horse;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder
@PrefixGameTestTemplate(false)
public final class HorseMilkGameTests {
    private HorseMilkGameTests() {
    }

    @GameTest(templateNamespace = "minecraft", template = "bastion/mobs/empty")
    public static void adultHorseFillsBucketWithMilk(GameTestHelper helper) {
        Horse horse = helper.spawn(EntityType.HORSE, new BlockPos(0, 1, 0));
        var player = helper.makeMockPlayer(GameType.SURVIVAL);
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.BUCKET));

        PlayerInteractEvent.EntityInteract event = new PlayerInteractEvent.EntityInteract(player, InteractionHand.MAIN_HAND, horse);
        HorseMilkHandler.milkHorse(event);

        helper.assertTrue(event.isCanceled(), "Milking an adult horse should consume the interaction");
        helper.assertTrue(player.getMainHandItem().is(Items.MILK_BUCKET), "Milking an adult horse should fill the bucket with milk");
        helper.succeed();
    }

    @GameTest(templateNamespace = "minecraft", template = "bastion/mobs/empty")
    public static void babyHorseDoesNotFillBucketWithMilk(GameTestHelper helper) {
        Horse horse = helper.spawn(EntityType.HORSE, new BlockPos(0, 1, 0));
        horse.setBaby(true);
        var player = helper.makeMockPlayer(GameType.SURVIVAL);
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.BUCKET));

        PlayerInteractEvent.EntityInteract event = new PlayerInteractEvent.EntityInteract(player, InteractionHand.MAIN_HAND, horse);
        HorseMilkHandler.milkHorse(event);

        helper.assertTrue(!event.isCanceled(), "Milking a baby horse should not consume the interaction");
        helper.assertTrue(player.getMainHandItem().is(Items.BUCKET), "Milking a baby horse should leave the bucket empty");
        helper.succeed();
    }
}
