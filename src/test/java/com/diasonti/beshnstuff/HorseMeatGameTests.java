package com.diasonti.beshnstuff;

import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.horse.Horse;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder
@PrefixGameTestTemplate(false)
public final class HorseMeatGameTests {
    private HorseMeatGameTests() {
    }

    @GameTest(templateNamespace = "minecraft", template = "bastion/mobs/empty")
    public static void horseDropsHorseMeat(GameTestHelper helper) {
        Horse horse = helper.spawn(EntityType.HORSE, new BlockPos(0, 1, 0));
        horse.hurt(helper.getLevel().damageSources().generic(), Float.MAX_VALUE);
        helper.succeedWhen(() -> helper.assertItemEntityPresent(BeshNStuffMod.HORSE_MEAT.get(), new BlockPos(0, 1, 0), 4.0));
    }

    @GameTest(templateNamespace = "minecraft", template = "bastion/mobs/empty", timeoutTicks = 240)
    public static void furnaceCooksHorseMeat(GameTestHelper helper) {
        BlockPos furnacePos = new BlockPos(0, 1, 0);
        helper.setBlock(furnacePos, Blocks.FURNACE);
        AbstractFurnaceBlockEntity furnace = helper.getBlockEntity(furnacePos);
        furnace.setItem(0, new ItemStack(BeshNStuffMod.HORSE_MEAT.get()));
        furnace.setItem(1, new ItemStack(Items.COAL));

        helper.succeedOnTickWhen(200, () -> helper.assertContainerContains(furnacePos, BeshNStuffMod.COOKED_HORSE_MEAT.get()));
    }

    @GameTest(templateNamespace = "minecraft", template = "bastion/mobs/empty", timeoutTicks = 140)
    public static void smokerCooksHorseMeat(GameTestHelper helper) {
        BlockPos smokerPos = new BlockPos(0, 1, 0);
        helper.setBlock(smokerPos, Blocks.SMOKER);
        AbstractFurnaceBlockEntity smoker = helper.getBlockEntity(smokerPos);
        smoker.setItem(0, new ItemStack(BeshNStuffMod.HORSE_MEAT.get()));
        smoker.setItem(1, new ItemStack(Items.COAL));

        helper.succeedOnTickWhen(100, () -> helper.assertContainerContains(smokerPos, BeshNStuffMod.COOKED_HORSE_MEAT.get()));
    }
}
