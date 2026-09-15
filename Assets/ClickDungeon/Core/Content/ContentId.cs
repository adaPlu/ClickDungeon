using System;
using System.Text.RegularExpressions;

namespace ClickDungeon.Core.Content
{
    public readonly struct ContentId : IEquatable<ContentId>
    {
        private static readonly Regex ValidPattern = new Regex(
            @"^[a-z0-9]+(?:\.[a-z0-9_]+)+$",
            RegexOptions.Compiled | RegexOptions.CultureInvariant);

        public string Value { get; }

        public ContentId(string value)
        {
            if (string.IsNullOrWhiteSpace(value) || !ValidPattern.IsMatch(value))
            {
                throw new ArgumentException(
                    "Content IDs must use lowercase dotted segments, for example tile.floor_stone.",
                    nameof(value));
            }

            Value = value;
        }

        public static ContentId Parse(string value) => new ContentId(value);

        public bool Equals(ContentId other) => string.Equals(Value, other.Value, StringComparison.Ordinal);
        public override bool Equals(object obj) => obj is ContentId other && Equals(other);
        public override int GetHashCode() => StringComparer.Ordinal.GetHashCode(Value ?? string.Empty);
        public override string ToString() => Value ?? string.Empty;

        public static bool operator ==(ContentId left, ContentId right) => left.Equals(right);
        public static bool operator !=(ContentId left, ContentId right) => !left.Equals(right);
    }
}
