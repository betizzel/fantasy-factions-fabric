package com.javic.villagerpatrols.item;

import com.javic.villagerpatrols.ModEntities;
import com.javic.villagerpatrols.entity.VampireEntitySupport;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.dispenser.DispenseItemBehavior;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ClipContext.Fluid;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.gameevent.GameEvent.Context;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult.Type;
import net.minecraft.world.item.SpawnEggItem;

public class VampireSpawnEggItem extends SpawnEggItem {
   public VampireSpawnEggItem(int backgroundColor, int highlightColor, Properties properties) {
      super(ModEntities.VAMPIRE.get(), backgroundColor, highlightColor, properties);
      DispenserBlock.registerBehavior(this, createDispenseBehavior());
   }

   public InteractionResult useOn(UseOnContext context) {
      Level level = context.getLevel();
      if (!(level instanceof ServerLevel serverLevel)) {
         return InteractionResult.SUCCESS;
      } else {
         ItemStack itemStack = context.getItemInHand();
         BlockPos blockPos = context.getClickedPos();
         Direction direction = context.getClickedFace();
         BlockState blockState = level.getBlockState(blockPos);
         VampireEntitySupport.SpawnChoice choice = VampireEntitySupport.chooseSpawn(level);
         if (level.getBlockEntity(blockPos) instanceof SpawnerBlockEntity spawner) {
            spawner.setEntityId(choice.type(), level.getRandom());
            level.sendBlockUpdated(blockPos, blockState, blockState, 3);
            level.gameEvent(context.getPlayer(), GameEvent.BLOCK_CHANGE, blockPos);
            itemStack.shrink(1);
            return InteractionResult.CONSUME;
         } else {
            BlockPos spawnPos = blockState.getCollisionShape(level, blockPos).isEmpty() ? blockPos : blockPos.relative(direction);
            Entity entity = choice.type()
               .spawn(
                  serverLevel, itemStack, context.getPlayer(), spawnPos, MobSpawnType.SPAWN_EGG, true, !blockPos.equals(spawnPos) && direction == Direction.UP
               );
            if (entity != null) {
               VampireEntitySupport.prepareSpawnedEntity(entity, choice, true);
               itemStack.shrink(1);
               level.gameEvent(context.getPlayer(), GameEvent.ENTITY_PLACE, blockPos);
            }

            return InteractionResult.CONSUME;
         }
      }
   }

   public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
      ItemStack itemStack = player.getItemInHand(hand);
      BlockHitResult blockHitResult = getPlayerPOVHitResult(level, player, Fluid.SOURCE_ONLY);
      if (blockHitResult.getType() != Type.BLOCK) {
         return InteractionResultHolder.pass(itemStack);
      }

      if (level instanceof ServerLevel serverLevel) {
         BlockPos blockPos = blockHitResult.getBlockPos();
         if (!(level.getBlockState(blockPos).getBlock() instanceof LiquidBlock)) {
            return InteractionResultHolder.pass(itemStack);
         }

         if (level.mayInteract(player, blockPos) && player.mayUseItemAt(blockPos, blockHitResult.getDirection(), itemStack)) {
            VampireEntitySupport.SpawnChoice choice = VampireEntitySupport.chooseSpawn(level);
            Entity entity = choice.type().spawn(serverLevel, itemStack, player, blockPos, MobSpawnType.SPAWN_EGG, false, false);
            if (entity == null) {
               return InteractionResultHolder.pass(itemStack);
            }

            VampireEntitySupport.prepareSpawnedEntity(entity, choice, true);
            itemStack.shrink(1);
            player.awardStat(Stats.ITEM_USED.get(this));
            level.gameEvent(player, GameEvent.ENTITY_PLACE, entity.position());
            return InteractionResultHolder.consume(itemStack);
         } else {
            return InteractionResultHolder.fail(itemStack);
         }
      } else {
         return InteractionResultHolder.success(itemStack);
      }
   }

   protected DispenseItemBehavior createDispenseBehavior() {
      return (source, stack) -> {
         Direction face = (Direction)source.getBlockState().getValue(DispenserBlock.FACING);
         VampireEntitySupport.SpawnChoice choice = VampireEntitySupport.chooseSpawn(source.getLevel());

         try {
            Entity entity = choice.type()
               .spawn(source.getLevel(), stack, null, source.getPos().relative(face), MobSpawnType.DISPENSER, face != Direction.UP, false);
            if (entity != null) {
               VampireEntitySupport.prepareSpawnedEntity(entity, choice, true);
            }
         } catch (Exception exception) {
            return ItemStack.EMPTY;
         }

         stack.shrink(1);
         source.getLevel().gameEvent(GameEvent.ENTITY_PLACE, source.getPos(), Context.of(source.getBlockState()));
         return stack;
      };
   }
}
