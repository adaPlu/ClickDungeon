using ClickDungeon.Application.Gameplay;
using ClickDungeon.Core.Content;
using ClickDungeon.Dungeon.Runtime;

namespace ClickDungeon.Platform.Input
{
    public sealed class WindowsInputAdapter : IPlayerInputAdapter
    {
        public bool TryTranslate(
            PlatformInputEvent inputEvent,
            FloorCoordinate currentPosition,
            out PlayerCommand command)
        {
            command = null;
            if (inputEvent == null)
                return false;

            switch (inputEvent.Action)
            {
                case PlatformInputAction.MoveUp:
                    command = PlayerCommand.Move(new FloorCoordinate(currentPosition.X, currentPosition.Y - 1));
                    return true;
                case PlatformInputAction.MoveDown:
                    command = PlayerCommand.Move(new FloorCoordinate(currentPosition.X, currentPosition.Y + 1));
                    return true;
                case PlatformInputAction.MoveLeft:
                    command = PlayerCommand.Move(new FloorCoordinate(currentPosition.X - 1, currentPosition.Y));
                    return true;
                case PlatformInputAction.MoveRight:
                    command = PlayerCommand.Move(new FloorCoordinate(currentPosition.X + 1, currentPosition.Y));
                    return true;
                case PlatformInputAction.SelectCell:
                    if (!inputEvent.HasCoordinate)
                        return false;
                    command = PlayerCommand.Move(new FloorCoordinate(inputEvent.X, inputEvent.Y));
                    return true;
                case PlatformInputAction.ActivateAction:
                    if (string.IsNullOrWhiteSpace(inputEvent.ContentIdValue))
                        return false;
                    command = PlayerCommand.Ability(
                        ContentId.Parse(inputEvent.ContentIdValue),
                        inputEvent.HasCoordinate
                            ? new FloorCoordinate(inputEvent.X, inputEvent.Y)
                            : (FloorCoordinate?)null);
                    return true;
                case PlatformInputAction.Interact:
                    command = PlayerCommand.Interact(
                        inputEvent.HasCoordinate
                            ? new FloorCoordinate(inputEvent.X, inputEvent.Y)
                            : (FloorCoordinate?)null);
                    return true;
                case PlatformInputAction.OpenInventory:
                case PlatformInputAction.PauseOrCancel:
                    return false;
                default:
                    return false;
            }
        }
    }
}
