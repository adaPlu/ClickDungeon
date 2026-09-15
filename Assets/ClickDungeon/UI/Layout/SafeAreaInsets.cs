namespace ClickDungeon.UI.Layout
{
    public readonly struct SafeAreaInsets
    {
        public float Top { get; }
        public float Right { get; }
        public float Bottom { get; }
        public float Left { get; }

        public SafeAreaInsets(float top, float right, float bottom, float left)
        {
            Top = top;
            Right = right;
            Bottom = bottom;
            Left = left;
        }
    }
}
