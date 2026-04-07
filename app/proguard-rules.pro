# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Preserve line numbers in crash stack traces.
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# -----------------------------------------------------------------------
# Gson serialization: keep all model classes and their fields so that
# Gson can read/write saved-game data, inventory, shop catalogs, and
# achievements without stripping or renaming field names.
# -----------------------------------------------------------------------

# Core model layer
-keep class com.example.clickdungeon.model.CharacterProfile { *; }
-keep class com.example.clickdungeon.model.Tile { *; }
-keep class com.example.clickdungeon.model.TileType { *; }
-keep class com.example.clickdungeon.model.Monster { *; }
-keep class com.example.clickdungeon.model.AnimatedMonster { *; }
-keep class com.example.clickdungeon.model.AnimatedPlayer { *; }
-keep class com.example.clickdungeon.model.AnimationState { *; }
-keep class com.example.clickdungeon.model.Achievement { *; }
-keep class com.example.clickdungeon.model.InventoryItem { *; }
-keep class com.example.clickdungeon.model.ItemDefinition { *; }
-keep class com.example.clickdungeon.model.ShopItem { *; }
-keep class com.example.clickdungeon.model.PricedItem { *; }
-keep class com.example.clickdungeon.model.PlayerClass { *; }
-keep class com.example.clickdungeon.model.PlayerClass$AbilityDefinition { *; }
-keep class com.example.clickdungeon.model.TerrainType { *; }
-keep class com.example.clickdungeon.model.MonsterFamily { *; }
-keep class com.example.clickdungeon.model.MonsterAffinity { *; }

# SaveManager internal blob (private static inner class used with Gson)
-keep class com.example.clickdungeon.util.SaveManager$SaveBlob { *; }

# Generic Gson rules: keep field names on any class annotated with
# @SerializedName, and preserve the Gson runtime itself.
-keepattributes *Annotation*
-dontwarn sun.misc.**
-keep class com.google.gson.** { *; }
-keep interface com.google.gson.** { *; }

# Keep enum values (Gson serializes enums by name by default)
-keepclassmembers enum com.example.clickdungeon.** {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# -----------------------------------------------------------------------
# Firebase / Play Billing (referenced but not yet initialized in release;
# suppress warnings until libraries are fully wired in v1.1)
# -----------------------------------------------------------------------
-dontwarn com.google.firebase.**
-dontwarn com.android.billingclient.**

# -----------------------------------------------------------------------
# Robolectric / test-only code is never in the release APK; suppress any
# residual references that sneak through the compile classpath.
# -----------------------------------------------------------------------
-dontwarn org.robolectric.**
