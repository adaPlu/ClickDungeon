using System;
using System.Collections.Generic;
using ClickDungeon.Core.Content;

namespace ClickDungeon.Narrative
{
    public sealed class CampaignEventDefinition
    {
        public ContentId Id { get; }
        public ContentId TriggerId { get; }
        public ContentId DialogueId { get; }
        public ContentId? NextEventId { get; }

        public CampaignEventDefinition(ContentId id, ContentId triggerId, ContentId dialogueId, ContentId? nextEventId)
        {
            Id = id;
            TriggerId = triggerId;
            DialogueId = dialogueId;
            NextEventId = nextEventId;
        }
    }

    public sealed class CampaignDefinition
    {
        public ContentId Id { get; }
        public ContentId HeroId { get; }
        public ContentId PrimaryBossId { get; }
        public IReadOnlyList<CampaignEventDefinition> Events { get; }
        public IReadOnlyList<DialogueDefinition> Dialogues { get; }

        public CampaignDefinition(
            ContentId id,
            ContentId heroId,
            ContentId primaryBossId,
            IReadOnlyList<CampaignEventDefinition> events,
            IReadOnlyList<DialogueDefinition> dialogues)
        {
            if (events == null || events.Count == 0) throw new ArgumentException("Campaign events are required.", nameof(events));
            if (dialogues == null || dialogues.Count == 0) throw new ArgumentException("Campaign dialogue is required.", nameof(dialogues));
            Id = id;
            HeroId = heroId;
            PrimaryBossId = primaryBossId;
            Events = Copy(events);
            Dialogues = Copy(dialogues);
        }

        public CampaignEventDefinition GetEventRequired(ContentId id)
        {
            for (var i = 0; i < Events.Count; i++)
                if (Events[i].Id == id) return Events[i];
            throw new KeyNotFoundException($"Unknown campaign event: {id}");
        }

        public DialogueDefinition GetDialogueRequired(ContentId id)
        {
            for (var i = 0; i < Dialogues.Count; i++)
                if (Dialogues[i].Id == id) return Dialogues[i];
            throw new KeyNotFoundException($"Unknown dialogue: {id}");
        }

        private static IReadOnlyList<T> Copy<T>(IReadOnlyList<T> source)
        {
            var copy = new T[source.Count];
            for (var i = 0; i < source.Count; i++) copy[i] = source[i];
            return copy;
        }
    }
}
