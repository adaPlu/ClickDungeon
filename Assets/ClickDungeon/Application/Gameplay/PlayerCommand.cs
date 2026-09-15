using System;
using ClickDungeon.Core.Content;
using ClickDungeon.Dungeon.Runtime;

namespace ClickDungeon.Application.Gameplay
{
    public enum PlayerCommandKind
    {
        Move,
        Ability,
        Interact,
        UseItem,
        Wait
    }

    public sealed class PlayerCommand
    {
        public PlayerCommandKind Kind { get; }
        public FloorCoordinate? TargetCoordinate { get; }
        public ContentId? ContentId { get; }

        private PlayerCommand(PlayerCommandKind kind, FloorCoordinate? targetCoordinate, ContentId? contentId)
        {
            Kind = kind;
            TargetCoordinate = targetCoordinate;
            ContentId = contentId;
            Validate();
        }

        public static PlayerCommand Move(FloorCoordinate target) => new PlayerCommand(PlayerCommandKind.Move, target, null);
        public static PlayerCommand Ability(ContentId abilityId, FloorCoordinate? target = null) => new PlayerCommand(PlayerCommandKind.Ability, target, abilityId);
        public static PlayerCommand Interact(FloorCoordinate? target = null) => new PlayerCommand(PlayerCommandKind.Interact, target, null);
        public static PlayerCommand UseItem(ContentId itemId) => new PlayerCommand(PlayerCommandKind.UseItem, null, itemId);
        public static PlayerCommand Wait() => new PlayerCommand(PlayerCommandKind.Wait, null, null);

        public void Validate()
        {
            if (Kind == PlayerCommandKind.Move && !TargetCoordinate.HasValue)
                throw new InvalidOperationException("Move commands require a target coordinate.");
            if ((Kind == PlayerCommandKind.Ability || Kind == PlayerCommandKind.UseItem) && !ContentId.HasValue)
                throw new InvalidOperationException("Ability and item commands require a content ID.");
            if (Kind == PlayerCommandKind.Wait && (TargetCoordinate.HasValue || ContentId.HasValue))
                throw new InvalidOperationException("Wait commands cannot carry targets or content IDs.");
        }
    }
}
