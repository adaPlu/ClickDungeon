using ClickDungeon.Application.Gameplay;
using ClickDungeon.Dungeon.Runtime;

namespace ClickDungeon.Platform.Input
{
    public interface IPlayerInputAdapter
    {
        bool TryTranslate(
            PlatformInputEvent inputEvent,
            FloorCoordinate currentPosition,
            out PlayerCommand command);
    }
}
