package com.javic.villagerpatrols.entity;

import com.javic.villagerpatrols.Config;
import com.javic.villagerpatrols.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier.Builder;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.biome.Biomes;

public class ShadowEntity extends AbstractKnightEntity {
   private static final int SKINS = 2;
   private static final int BLINDNESS_DURATION = 30;
   private static final float DAY_SPAWN_CHANCE = 0.05F;
   private static final float NETHER_SPAWN_CHANCE = 0.2F;
   private static final float END_SPAWN_CHANCE = 0.05F;

   public ShadowEntity(EntityType<? extends Monster> type, Level level) {
      super(type, level);
      this.xpReward = 8;
   }

   public static Builder createAttributes() {
      return Monster.createMonsterAttributes()
         .add(Attributes.MAX_HEALTH, 40.0)
         .add(Attributes.ATTACK_DAMAGE, 3.0)
         .add(Attributes.MOVEMENT_SPEED, 0.31)
         .add(Attributes.FOLLOW_RANGE, 32.0);
   }

   public static boolean checkShadowSpawnRules(
      EntityType<? extends Monster> type, ServerLevelAccessor level, MobSpawnType spawnType, BlockPos pos, RandomSource random
   ) {
      if (!Config.isNaturalSpawnEnabled((EntityType<?>)ModEntities.SHADOW.get())
         || level.getDifficulty() == Difficulty.PEACEFUL
         || !Mob.checkMobSpawnRules(type, level, spawnType, pos, random)) {
         return false;
      } else if (level.getLevel().dimension() == Level.NETHER) {
         return Monster.checkAnyLightMonsterSpawnRules(type, level, spawnType, pos, random) && random.nextFloat() < 0.2F;
      } else if (level.getLevel().dimension() == Level.END) {
         return Monster.checkAnyLightMonsterSpawnRules(type, level, spawnType, pos, random) && random.nextFloat() < 0.05F;
      } else {
         boolean deepDark = level.getBiome(pos).is(Biomes.DEEP_DARK);
         if (deepDark) {
            return Monster.checkMonsterSpawnRules(type, level, spawnType, pos, random);
         } else {
            return level.getLevel().isDay() && level.canSeeSky(pos)
               ? Monster.checkAnyLightMonsterSpawnRules(type, level, spawnType, pos, random) && random.nextFloat() < 0.05F
               : Monster.checkMonsterSpawnRules(type, level, spawnType, pos, random);
         }
      }
   }

   public boolean checkSpawnRules(LevelAccessor level, MobSpawnType spawnReason) {
      @SuppressWarnings("unchecked") EntityType<? extends Monster> type = (EntityType<? extends Monster>) this.getType();
      return level instanceof ServerLevelAccessor serverLevelAccessor
         && checkShadowSpawnRules(type, serverLevelAccessor, spawnReason, this.blockPosition(), this.getRandom());
   }

   public boolean doHurtTarget(Entity entity) {
      boolean result = super.doHurtTarget(entity);
      if (result && entity instanceof Player player && !this.level().isClientSide) {
         player.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 30, 0), this);
      }

      return result;
   }

   @Override
   public boolean isAlliedTo(Entity entity) {
      return !(entity instanceof ShadowEntity)
            && !(entity instanceof BanditEntity)
            && !(entity instanceof EvilWizardEntity)
            && !(entity instanceof EvilKnightEntity)
            && !(entity instanceof EvilKnightShieldEntity)
            && !(entity instanceof EvilKnightLeaderEntity)
         ? super.isAlliedTo(entity)
         : true;
   }

   @Override
   public ResourceLocation getSkinTexture() {
      return new ResourceLocation("villagerpatrols", "textures/entity/shadow/" + this.getGender() + "/skin_" + this.getSkinIndex() + ".png");
   }

   @Override
   protected String getTextureFolder() {
      return "shadow";
   }

   @Override
   protected boolean usesShield() {
      return false;
   }

   @Override
   protected boolean isFriendlyKnight() {
      return false;
   }

   @Override
   protected int getMaleSkinCount() {
      return 2;
   }

   @Override
   protected int getFemaleSkinCount() {
      return 2;
   }

   @Override
   protected ItemStack createDefaultMainHandItemStack() {
      return new ItemStack(this.getRandom().nextFloat() < 0.7F ? Items.IRON_SWORD : Items.GOLDEN_SWORD);
   }

   @Override
   protected Item getDefaultMainHandItem() {
      return Items.IRON_SWORD;
   }

   @Override
   protected boolean useBiasedGenderRoll() {
      return false;
   }

   @Override
   protected boolean isSunSensitive() {
      return false;
   }
}
