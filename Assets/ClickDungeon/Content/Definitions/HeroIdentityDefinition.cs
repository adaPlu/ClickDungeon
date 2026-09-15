using System;
using System.Collections.Generic;
using ClickDungeon.Core.Content;

namespace ClickDungeon.Content.Definitions
{
    public enum PresentationArchetype
    {
        Standard,
        Mascot
    }

    public enum HeroArtState
    {
        MasterPose,
        PortraitHud,
        RosterIcon,
        GameplayChibi,
        Idle,
        Attack,
        Hit,
        Victory,
        Defeat
    }

    public sealed class HeroIdentityDefinition
    {
        public ContentId Id { get; }
        public string DisplayName { get; }
        public ContentId MechanicsClassId { get; }
        public string PresentationClassLabel { get; }
        public PresentationArchetype PresentationArchetype { get; }
        public ContentId ArtSetId { get; }
        public ContentId FlavorTextId { get; }
        public ContentId DialogueSetId { get; }
        public ContentId StoryCampaignId { get; }
        public IReadOnlyList<HeroArtState> RequiredArtStates { get; }

        public HeroIdentityDefinition(
            ContentId id,
            string displayName,
            ContentId mechanicsClassId,
            string presentationClassLabel,
            PresentationArchetype presentationArchetype,
            ContentId artSetId,
            ContentId flavorTextId,
            ContentId dialogueSetId,
            ContentId storyCampaignId,
            IReadOnlyList<HeroArtState> requiredArtStates)
        {
            if (string.IsNullOrWhiteSpace(displayName)) throw new ArgumentException("Display name is required.", nameof(displayName));
            if (string.IsNullOrWhiteSpace(presentationClassLabel)) throw new ArgumentException("Presentation class label is required.", nameof(presentationClassLabel));
            if (requiredArtStates == null || requiredArtStates.Count == 0) throw new ArgumentException("Required art states are required.", nameof(requiredArtStates));

            Id = id;
            DisplayName = displayName;
            MechanicsClassId = mechanicsClassId;
            PresentationClassLabel = presentationClassLabel;
            PresentationArchetype = presentationArchetype;
            ArtSetId = artSetId;
            FlavorTextId = flavorTextId;
            DialogueSetId = dialogueSetId;
            StoryCampaignId = storyCampaignId;

            var copy = new HeroArtState[requiredArtStates.Count];
            for (var i = 0; i < requiredArtStates.Count; i++) copy[i] = requiredArtStates[i];
            RequiredArtStates = copy;
        }
    }
}
