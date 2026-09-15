using System;
using System.Collections.Generic;
using ClickDungeon.Core.Content;

namespace ClickDungeon.Narrative
{
    public sealed class CampaignState
    {
        private readonly HashSet<ContentId> completedEventIds = new HashSet<ContentId>();

        public ContentId CampaignId { get; }
        public ContentId? CurrentEventId { get; private set; }
        public IReadOnlyCollection<ContentId> CompletedEventIds => completedEventIds;
        public bool IsComplete => !CurrentEventId.HasValue;

        public CampaignState(ContentId campaignId, ContentId firstEventId)
        {
            CampaignId = campaignId;
            CurrentEventId = firstEventId;
        }

        public void Advance(ContentId completedEventId, ContentId? nextEventId)
        {
            if (!CurrentEventId.HasValue || CurrentEventId.Value != completedEventId)
                throw new InvalidOperationException("Only the current campaign event can advance.");
            if (!completedEventIds.Add(completedEventId))
                throw new InvalidOperationException("Campaign event was already completed.");
            CurrentEventId = nextEventId;
        }
    }
}
