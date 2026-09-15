using System;
using ClickDungeon.Platform;

namespace ClickDungeon.UI.MainMenu
{
    public sealed class PlatformMenuPolicy
    {
        public bool Play { get; } = true;
        public bool Continue { get; } = true;
        public bool HeroSelect { get; } = true;
        public bool Inventory { get; } = true;
        public bool Talents { get; } = true;
        public bool Shop { get; } = true;
        public bool Settings { get; } = true;
        public bool ShowQuit { get; }
        public bool UseSystemBackInsteadOfQuit { get; }

        public PlatformMenuPolicy(PlatformCapabilities capabilities)
        {
            if (capabilities == null)
                throw new ArgumentNullException(nameof(capabilities));

            ShowQuit = capabilities.SupportsDesktopQuit;
            UseSystemBackInsteadOfQuit = !capabilities.SupportsDesktopQuit;
        }
    }
}
