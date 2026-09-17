using System;
using ClickDungeon.Dungeon.Runtime;

namespace ClickDungeon.Application.Gameplay
{
    public sealed class EncounterRoomRuntimeState
    {
        public EncounterRoomLayout Layout { get; }
        public bool IsDoorOpen { get; private set; }

        public EncounterRoomRuntimeState(EncounterRoomLayout layout)
        {
            Layout = layout ?? throw new ArgumentNullException(nameof(layout));
        }

        public void MarkDoorOpen()
        {
            IsDoorOpen = true;
        }
    }
}
