using System.Collections.Generic;
using ClickDungeon.Core.Content;

namespace ClickDungeon.Narrative
{
    public static class CanonicalSirClickingtonCampaign
    {
        public static CampaignDefinition Build()
        {
            var dialogues = new List<DialogueDefinition>
            {
                Dialogue("dialogue.sir_clickington.intro", "hero.sir_clickington", DialogueExpression.Confident,
                    "Small Knight. Big Enthusiasm. The dungeon has no idea what is about to click it."),
                Dialogue("dialogue.sir_clickington.chest", "hero.sir_clickington", DialogueExpression.Happy,
                    "Treasure! My favorite kind of strategic planning."),
                Dialogue("dialogue.lord_blobert.intro", "boss.lord_blobert", DialogueExpression.Angry,
                    "You dare challenge Lord Blobert, the most magnificent, most putrid blob in all dungeons?"),
                Dialogue("dialogue.sir_clickington.blobert_reply", "hero.sir_clickington", DialogueExpression.Shocked,
                    "Magnificent? Absolutely. Putrid? Also absolutely."),
                Dialogue("dialogue.sir_clickington.blobert_victory", "hero.sir_clickington", DialogueExpression.Victorious,
                    "Adventure looks better together. Even when together includes a very loud blob."),
                Dialogue("dialogue.sir_clickington.complete", "hero.sir_clickington", DialogueExpression.Happy,
                    "One dungeon down. Several extremely questionable ideas to go.")
            };

            var events = new List<CampaignEventDefinition>
            {
                Event("event.sir_clickington.intro", "trigger.campaign.start", "dialogue.sir_clickington.intro", "event.sir_clickington.chest"),
                Event("event.sir_clickington.chest", "trigger.chest.discovered", "dialogue.sir_clickington.chest", "event.sir_clickington.blobert_intro"),
                Event("event.sir_clickington.blobert_intro", "trigger.boss.blobert_encounter", "dialogue.lord_blobert.intro", "event.sir_clickington.blobert_reply"),
                Event("event.sir_clickington.blobert_reply", "trigger.dialogue.advance", "dialogue.sir_clickington.blobert_reply", "event.sir_clickington.blobert_victory"),
                Event("event.sir_clickington.blobert_victory", "trigger.boss.blobert_defeated", "dialogue.sir_clickington.blobert_victory", "event.sir_clickington.complete"),
                Event("event.sir_clickington.complete", "trigger.floor.exit", "dialogue.sir_clickington.complete", null)
            };

            return new CampaignDefinition(
                ContentId.Parse("campaign.sir_clickington"),
                ContentId.Parse("hero.sir_clickington"),
                ContentId.Parse("boss.lord_blobert"),
                events,
                dialogues);
        }

        public static CampaignState CreateInitialState()
        {
            return new CampaignState(
                ContentId.Parse("campaign.sir_clickington"),
                ContentId.Parse("event.sir_clickington.intro"));
        }

        private static DialogueDefinition Dialogue(string id, string speakerId, DialogueExpression expression, string text)
        {
            return new DialogueDefinition(ContentId.Parse(id), ContentId.Parse(speakerId), expression, text);
        }

        private static CampaignEventDefinition Event(string id, string triggerId, string dialogueId, string nextEventId)
        {
            return new CampaignEventDefinition(
                ContentId.Parse(id),
                ContentId.Parse(triggerId),
                ContentId.Parse(dialogueId),
                nextEventId == null ? (ContentId?)null : ContentId.Parse(nextEventId));
        }
    }
}
