using ClickDungeon.Core.Brand;
using ClickDungeon.Dungeon.Runtime;
using ClickDungeon.UI;
using ClickDungeon.UI.Gameplay;
using ClickDungeon.UI.MainMenu;
using NUnit.Framework;

namespace ClickDungeon.Tests.EditMode
{
    public sealed class ReleaseContractTests
    {
        [Test]
        public void CanonicalReleaseContractsRemainStable()
        {
            Assert.AreEqual("ClickDungeon", ProductBrand.PlayerFacingName);
            Assert.AreEqual(5, FloorState.Width);
            Assert.AreEqual(5, FloorState.Height);
            Assert.AreEqual(5, GameplayHudContract.BoardColumns);
            Assert.AreEqual(5, GameplayHudContract.BoardRows);

            var safeArea = new SafeAreaInsets(0, 0, 0, 0);
            var desktop = new MainMenuLayoutContract(ResponsiveLayoutMode.DesktopWide, safeArea);
            var mobile = new MainMenuLayoutContract(ResponsiveLayoutMode.MobileCompact, safeArea);
            Assert.IsTrue(desktop.IsVisible(MainMenuControlId.Quit, true));
            Assert.IsFalse(mobile.IsVisible(MainMenuControlId.Quit, false));
        }
    }
}
