package com.javic.villagerpatrols.ward;

import com.javic.villagerpatrols.Config;

public enum WardBannerType {
   WARD_BANNER("ward_banner");

   private final String serializedName;

   WardBannerType(String serializedName) {
      this.serializedName = serializedName;
   }

   public String serializedName() {
      return this.serializedName;
   }

   public boolean isEnabled() {
      return (Boolean)Config.ENABLE_WARD_BANNERS.get();
   }

   public int radius() {
      return Config.wardBannerRadius();
   }

   public static WardBannerType byName(String name) {
      for (WardBannerType type : values()) {
         if (type.serializedName.equals(name)) {
            return type;
         }
      }

      return null;
   }
}
