using System;
using System.Collections.Generic;
using ClickDungeon.Core.Content;

namespace ClickDungeon.Narrative
{
    public readonly struct NarrativeTrigger
    {
        public ContentId TriggerId { get; }
        public ContentId? SubjectId { get; }

        public NarrativeTrigger(ContentId triggerId, ContentId? subjectId = null)
        {
            TriggerId = triggerId;
            SubjectId = subjectId;
        }
    }

    public readonly struct NarrativeResult
    {
        public ContentId CampaignEventId { get; }
        public DialogueDefinition Dialogue { get; }

        public NarrativeResult(ContentId campaignEventId, DialogueDefinition dialogue)
        {
            CampaignEventId = campaignEventId;
            Dialogue = dialogue;
        }
    }

    public sealed class NarrativeResolver
    {
        public IReadOnlyList<NarrativeResult> Resolve(
            CampaignDefinition definition,
            CampaignState state,
            NarrativeTrigger trigger)
        {
            if (definition == null) throw new ArgumentNullException(nameof(definition));
            if (state == null) throw new ArgumentNullException(nameof(state));
            if (definition.Id != state.CampaignId) throw new InvalidOperationException("Campaign state does not match definition.");
            if (!state.CurrentEventId.HasValue) return Array.Empty<NarrativeResult>();

            var current = definition.GetEventRequired(state.CurrentEventId.Value);
            if (current.TriggerId != trigger.TriggerId) return Array.Empty<NarrativeResult>();

            var dialogue = definition.GetDialogueRequired(current.DialogueId);
            state.Advance(current.Id, current.NextEventId);
            return new[] { new NarrativeResult(current.Id, dialogue) };
        }
    }
}
