using System.Collections.Generic;
using ClickDungeon.Content.Definitions;
using ClickDungeon.Core.Content;

namespace ClickDungeon.Content.Canonical
{
    public static class CanonicalHeroes
    {
        private static readonly IReadOnlyList<HeroArtState> StandardArtStates = new[]
        {
            HeroArtState.MasterPose,
            HeroArtState.PortraitHud,
            HeroArtState.RosterIcon,
            HeroArtState.GameplayChibi,
            HeroArtState.Idle,
            HeroArtState.Attack,
            HeroArtState.Hit,
            HeroArtState.Victory,
            HeroArtState.Defeat
        };

        private static readonly IReadOnlyList<HeroIdentityDefinition> Definitions = Build();
        public static IReadOnlyList<HeroIdentityDefinition> All => Definitions;

        private static IReadOnlyList<HeroIdentityDefinition> Build()
        {
            var items = new List<HeroIdentityDefinition>(9);

            Add("hero.ironheart", "Ironheart", "class.knight", "Knight", PresentationArchetype.Standard,
                "art.hero.ironheart", "flavor.hero.ironheart", "dialogue.hero.ironheart", "campaign.ironheart", items);
            Add("hero.shadowcut", "Shadowcut", "class.thief", "Rogue", PresentationArchetype.Standard,
                "art.hero.shadowcut", "flavor.hero.shadowcut", "dialogue.hero.shadowcut", "campaign.shadowcut", items);
            Add("hero.emberwisp", "Emberwisp", "class.wizard", "Wizard", PresentationArchetype.Standard,
                "art.hero.emberwisp", "flavor.hero.emberwisp", "dialogue.hero.emberwisp", "campaign.emberwisp", items);
            Add("hero.windsong", "Windsong", "class.ranger", "Ranger", PresentationArchetype.Standard,
                "art.hero.windsong", "flavor.hero.windsong", "dialogue.hero.windsong", "campaign.windsong", items);
            Add("hero.lightbringer", "Lightbringer", "class.cleric", "Cleric", PresentationArchetype.Standard,
                "art.hero.lightbringer", "flavor.hero.lightbringer", "dialogue.hero.lightbringer", "campaign.lightbringer", items);
            Add("hero.rageclaw", "Rageclaw", "class.berserker", "Berserker", PresentationArchetype.Standard,
                "art.hero.rageclaw", "flavor.hero.rageclaw", "dialogue.hero.rageclaw", "campaign.rageclaw", items);
            Add("hero.gearspark", "Gearspark", "class.engineer", "Engineer", PresentationArchetype.Standard,
                "art.hero.gearspark", "flavor.hero.gearspark", "dialogue.hero.gearspark", "campaign.gearspark", items);
            Add("hero.dawnward", "Dawnward", "class.paladin", "Paladin", PresentationArchetype.Standard,
                "art.hero.dawnward", "flavor.hero.dawnward", "dialogue.hero.dawnward", "campaign.dawnward", items);

            // The Sir Clickington sheet calls him a Mascot. That is presentation identity only:
            // approved game rules keep his mechanics on the same Knight class used by Ironheart.
            Add("hero.sir_clickington", "Sir Clickington", "class.knight", "Mascot", PresentationArchetype.Mascot,
                "art.hero.sir_clickington", "flavor.hero.sir_clickington", "dialogue.hero.sir_clickington", "campaign.sir_clickington", items);

            return items;
        }

        private static void Add(
            string id,
            string displayName,
            string mechanicsClassId,
            string presentationClassLabel,
            PresentationArchetype archetype,
            string artSetId,
            string flavorTextId,
            string dialogueSetId,
            string storyCampaignId,
            ICollection<HeroIdentityDefinition> items)
        {
            items.Add(new HeroIdentityDefinition(
                ContentId.Parse(id),
                displayName,
                ContentId.Parse(mechanicsClassId),
                presentationClassLabel,
                archetype,
                ContentId.Parse(artSetId),
                ContentId.Parse(flavorTextId),
                ContentId.Parse(dialogueSetId),
                ContentId.Parse(storyCampaignId),
                StandardArtStates));
        }
    }
}
