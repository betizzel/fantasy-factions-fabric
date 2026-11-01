package com.javic.villagerpatrols.item;

import com.javic.villagerpatrols.ModEntities;
import com.javic.villagerpatrols.entity.WizardEntitySupport;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.dispenser.DispenseItemBehavior;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
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

public class WizardSpawnEggItem extends SpawnEggItem {
   public WizardSpawnEggItem(int backgroundColor, int highlightColor, Properties properties) {
      super(ModEntities.MALE_WIZARD.get(), backgroundColor, highlightColor, properties);
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
         boolean female = level.getRandom().nextBoolean();
         EntityType type = WizardEntitySupport.resolveFriendlyType(level, female);
         if (level.getBlockEntity(blockPos) instanceof SpawnerBlockEntity spawner) {
            spawner.setEntityId(type, level.getRandom());
            level.sendBlockUpdated(blockPos, blockState, blockState, 3);
            level.gameEvent(context.getPlayer(), GameEvent.BLOCK_CHANGE, blockPos);
            itemStack.shrink(1);
            return InteractionResult.CONSUME;
         } else {
            BlockPos spawnPos = blockState.getCollisionShape(level, blockPos).isEmpty() ? blockPos : blockPos.relative(direction);
            Entity entity = type.spawn(
               serverLevel, itemStack, context.getPlayer(), spawnPos, MobSpawnType.SPAWN_EGG, true, !blockPos.equals(spawnPos) && direction == Direction.UP
            );
            if (entity != null) {
               WizardEntitySupport.prepareSpawnedEntity(entity, female, true);
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
            boolean female = level.getRandom().nextBoolean();
            EntityType<? extends Mob> type = WizardEntitySupport.resolveFriendlyType(level, female);
            Entity entity = type.spawn(serverLevel, itemStack, player, blockPos, MobSpawnType.SPAWN_EGG, false, false);
            if (entity == null) {
               return InteractionResultHolder.pass(itemStack);
            }

            WizardEntitySupport.prepareSpawnedEntity(entity, female, true);
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
         boolean female = source.getLevel().getRandom().nextBoolean();
         EntityType<? extends Mob> type = WizardEntitySupport.resolveFriendlyType(source.getLevel(), female);

         try {
            Entity entity = type.spawn(source.getLevel(), stack, null, source.getPos().relative(face), MobSpawnType.DISPENSER, face != Direction.UP, false);
            if (entity != null) {
               WizardEntitySupport.prepareSpawnedEntity(entity, female, true);
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
