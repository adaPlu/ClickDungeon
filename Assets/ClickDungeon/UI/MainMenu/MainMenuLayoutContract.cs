using System;
using System.Collections.Generic;
using ClickDungeon.Core.Brand;

namespace ClickDungeon.UI
{
    public enum ResponsiveLayoutMode
    {
        DesktopWide,
        TabletAdaptive,
        MobileCompact
    }

    public readonly struct SafeAreaInsets
    {
        public int Left { get; }
        public int Top { get; }
        public int Right { get; }
        public int Bottom { get; }

        public SafeAreaInsets(int left, int top, int right, int bottom)
        {
            Left = Math.Max(0, left);
            Top = Math.Max(0, top);
            Right = Math.Max(0, right);
            Bottom = Math.Max(0, bottom);
        }
    }
}

namespace ClickDungeon.UI.MainMenu
{
    public enum MainMenuControlId
    {
        HeroProfile,
        GoldStore,
        GemStore,
        ContinueRun,
        Play,
        HeroSelect,
        Inventory,
        Talents,
        Shop,
        Settings,
        Quit,
        Challenges,
        Inbox,
        Menu,
        DailyRewardClaim
    }

    public sealed class MainMenuLayoutContract
    {
        private static readonly IReadOnlyList<MainMenuControlId> ReferenceControls = new[]
        {
            MainMenuControlId.HeroProfile,
            MainMenuControlId.GoldStore,
            MainMenuControlId.GemStore,
            MainMenuControlId.ContinueRun,
            MainMenuControlId.Play,
            MainMenuControlId.HeroSelect,
            MainMenuControlId.Inventory,
            MainMenuControlId.Talents,
            MainMenuControlId.Shop,
            MainMenuControlId.Settings,
            MainMenuControlId.Quit,
            MainMenuControlId.Challenges,
            MainMenuControlId.Inbox,
            MainMenuControlId.Menu,
            MainMenuControlId.DailyRewardClaim
        };

        public const int MinTouchTargetPixels = 44;
        public string Title => ProductBrand.PlayerFacingName;
        public IReadOnlyList<MainMenuControlId> Controls => ReferenceControls;
        public ClickDungeon.UI.ResponsiveLayoutMode LayoutMode { get; }
        public ClickDungeon.UI.SafeAreaInsets SafeArea { get; }

        public MainMenuLayoutContract(ClickDungeon.UI.ResponsiveLayoutMode layoutMode, ClickDungeon.UI.SafeAreaInsets safeArea)
        {
            LayoutMode = layoutMode;
            SafeArea = safeArea;
        }

        public bool IsVisible(MainMenuControlId control, bool isDesktop)
        {
            return control != MainMenuControlId.Quit || isDesktop;
        }
    }
}
