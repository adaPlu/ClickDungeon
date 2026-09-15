using System.Collections.Generic;

namespace ClickDungeon.UI.Gameplay
{
    public enum GameplayHudElement
    {
        HeroPortrait,
        HeroLevel,
        Health,
        ClassResource,
        Gold,
        Gems,
        FloorIdentity,
        EnemyHealthBar,
        SelectedCellHighlight,
        Inventory,
        Talents,
        Shop
    }

    public enum GameplayActionButton
    {
        Move,
        Slash,
        Shield,
        Dash,
        Potion
    }

    public sealed class GameplayHudContract
    {
        public const int BoardColumns = 5;
        public const int BoardRows = 5;
        public const int MinTouchTargetPixels = 44;

        public static readonly IReadOnlyList<GameplayHudElement> HudElements = new[]
        {
            GameplayHudElement.HeroPortrait,
            GameplayHudElement.HeroLevel,
            GameplayHudElement.Health,
            GameplayHudElement.ClassResource,
            GameplayHudElement.Gold,
            GameplayHudElement.Gems,
            GameplayHudElement.FloorIdentity,
            GameplayHudElement.EnemyHealthBar,
            GameplayHudElement.SelectedCellHighlight,
            GameplayHudElement.Inventory,
            GameplayHudElement.Talents,
            GameplayHudElement.Shop
        };

        public static readonly IReadOnlyList<GameplayActionButton> ActionRow = new[]
        {
            GameplayActionButton.Move,
            GameplayActionButton.Slash,
            GameplayActionButton.Shield,
            GameplayActionButton.Dash,
            GameplayActionButton.Potion
        };

        public ClickDungeon.UI.ResponsiveLayoutMode LayoutMode { get; }
        public ClickDungeon.UI.SafeAreaInsets SafeArea { get; }

        public GameplayHudContract(ClickDungeon.UI.ResponsiveLayoutMode layoutMode, ClickDungeon.UI.SafeAreaInsets safeArea)
        {
            LayoutMode = layoutMode;
            SafeArea = safeArea;
        }
    }
}
