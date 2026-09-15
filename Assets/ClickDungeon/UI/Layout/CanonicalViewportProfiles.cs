namespace ClickDungeon.UI.Layout
{
    public static class CanonicalViewportProfiles
    {
        public static readonly ViewportProfile Desktop16x9 =
            new ViewportProfile(ViewportProfileId.Desktop16x9, 16f, 9f, false, false);

        public static readonly ViewportProfile Desktop16x10 =
            new ViewportProfile(ViewportProfileId.Desktop16x10, 16f, 10f, false, false);

        public static readonly ViewportProfile Tablet4x3 =
            new ViewportProfile(ViewportProfileId.Tablet4x3, 4f, 3f, true, true);

        public static readonly ViewportProfile Phone19_5x9 =
            new ViewportProfile(ViewportProfileId.Phone19_5x9, 19.5f, 9f, true, true);

        public static readonly ViewportProfile Phone20x9 =
            new ViewportProfile(ViewportProfileId.Phone20x9, 20f, 9f, true, true);
    }
}
