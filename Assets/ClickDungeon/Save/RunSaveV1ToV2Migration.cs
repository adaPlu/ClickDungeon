using System;
using System.Collections.Generic;

namespace ClickDungeon.Save
{
    public sealed class RunSaveV1ToV2Migration : ISaveMigration<RunSave>
    {
        public int FromVersion => 1;
        public int ToVersion => 2;

        public RunSave Migrate(RunSave source)
        {
            if (source == null) throw new ArgumentNullException(nameof(source));
            source.EncounterRooms = source.EncounterRooms ?? new List<EncounterRoomSave>();
            source.SchemaVersion = ToVersion;
            return source;
        }
    }
}
