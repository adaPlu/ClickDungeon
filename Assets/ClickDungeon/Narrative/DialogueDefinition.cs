using System;
using ClickDungeon.Core.Content;

namespace ClickDungeon.Narrative
{
    public enum DialogueExpression
    {
        Neutral,
        Happy,
        Confident,
        Worried,
        Shocked,
        Angry,
        Victorious,
        Defeated
    }

    public sealed class DialogueDefinition
    {
        public ContentId Id { get; }
        public ContentId SpeakerId { get; }
        public DialogueExpression Expression { get; }
        public string Text { get; }

        public DialogueDefinition(ContentId id, ContentId speakerId, DialogueExpression expression, string text)
        {
            if (string.IsNullOrWhiteSpace(text)) throw new ArgumentException("Dialogue text is required.", nameof(text));
            Id = id;
            SpeakerId = speakerId;
            Expression = expression;
            Text = text;
        }
    }
}
