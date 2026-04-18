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
-keep class com.adaplu.clickdungeon.model.CharacterProfile { *; }
-keep class com.adaplu.clickdungeon.model.Tile { *; }
-keep class com.adaplu.clickdungeon.model.TileType { *; }
-keep class com.adaplu.clickdungeon.model.Monster { *; }
-keep class com.adaplu.clickdungeon.model.AnimatedMonster { *; }
-keep class com.adaplu.clickdungeon.model.AnimatedPlayer { *; }
-keep class com.adaplu.clickdungeon.model.AnimationState { *; }
-keep class com.adaplu.clickdungeon.model.Achievement { *; }
-keep class com.adaplu.clickdungeon.model.InventoryItem { *; }
-keep class com.adaplu.clickdungeon.model.ItemDefinition { *; }
-keep class com.adaplu.clickdungeon.model.ShopItem { *; }
-keep class com.adaplu.clickdungeon.model.PricedItem { *; }
-keep class com.adaplu.clickdungeon.model.PlayerClass { *; }
-keep class com.adaplu.clickdungeon.model.PlayerClass$AbilityDefinition { *; }
-keep class com.adaplu.clickdungeon.model.TerrainType { *; }
-keep class com.adaplu.clickdungeon.model.MonsterFamily { *; }
-keep class com.adaplu.clickdungeon.model.MonsterAffinity { *; }

# SaveManager inner classes serialized/deserialized via Gson
-keep class com.adaplu.clickdungeon.util.SaveManager$SaveBlob { *; }
-keep class com.adaplu.clickdungeon.util.SaveManager$SaveSnapshot { *; }
-keep class com.adaplu.clickdungeon.util.SaveManager$RunMetadata { *; }

# Generic Gson rules: keep field names on any class annotated with
# @SerializedName, and preserve the Gson runtime itself.
-keepattributes *Annotation*
-dontwarn sun.misc.**
-keep class com.google.gson.** { *; }
-keep interface com.google.gson.** { *; }

# Keep enum values (Gson serializes enums by name by default)
-keepclassmembers enum com.adaplu.clickdungeon.** {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# -----------------------------------------------------------------------
# Strip debug/verbose log calls from release builds.
# -----------------------------------------------------------------------
-assumenosideeffects class android.util.Log {
    public static int d(...);
    public static int v(...);
}

# -----------------------------------------------------------------------
# Firebase (not yet added; suppress residual references from Tink/etc)
# -----------------------------------------------------------------------
-dontwarn com.google.firebase.**

# -----------------------------------------------------------------------
# Robolectric / test-only code -- never in the release APK.
# -----------------------------------------------------------------------
-dontwarn org.robolectric.**
