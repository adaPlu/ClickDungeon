using System;

namespace ClickDungeon.UI.Layout
{
    public sealed class ResponsiveLayoutContract
    {
        public int BoardColumns { get; } = 5;
        public int BoardRows { get; } = 5;
        public bool PreserveHud { get; } = true;
        public bool PreserveActionRow { get; } = true;
        public bool CollapseSecondaryPanels { get; }
        public bool RespectSafeArea { get; }

        public ResponsiveLayoutContract(ViewportProfile profile)
        {
            if (profile == null)
                throw new ArgumentNullException(nameof(profile));

            CollapseSecondaryPanels = profile.IsConstrained;
            RespectSafeArea = profile.IsMobile;
        }
    }
}
