using System;

namespace ClickDungeon.Dungeon.Runtime
{
    public readonly struct FloorCoordinate : IEquatable<FloorCoordinate>
    {
        public int X { get; }
        public int Y { get; }

        public FloorCoordinate(int x, int y)
        {
            X = x;
            Y = y;
        }

        public bool Equals(FloorCoordinate other) => X == other.X && Y == other.Y;
        public override bool Equals(object obj) => obj is FloorCoordinate other && Equals(other);
        public override int GetHashCode() => (X * 397) ^ Y;
        public static bool operator ==(FloorCoordinate left, FloorCoordinate right) => left.Equals(right);
        public static bool operator !=(FloorCoordinate left, FloorCoordinate right) => !left.Equals(right);
    }
}
