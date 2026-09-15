namespace ClickDungeon.UI.Layout
{
    public enum ViewportProfileId
    {
        Desktop16x9 = 0,
        Desktop16x10 = 1,
        Tablet4x3 = 2,
        Phone19_5x9 = 3,
        Phone20x9 = 4
    }

    public sealed class ViewportProfile
    {
        public ViewportProfileId Id { get; }
        public float WidthUnits { get; }
        public float HeightUnits { get; }
        public bool IsConstrained { get; }
        public bool IsMobile { get; }

        public ViewportProfile(
            ViewportProfileId id,
            float widthUnits,
            float heightUnits,
            bool isConstrained,
            bool isMobile)
        {
            Id = id;
            WidthUnits = widthUnits;
            HeightUnits = heightUnits;
            IsConstrained = isConstrained;
            IsMobile = isMobile;
        }
    }
}
