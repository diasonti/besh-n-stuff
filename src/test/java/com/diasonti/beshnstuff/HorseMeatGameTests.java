package com.diasonti.beshnstuff;

import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.horse.Horse;
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
}
