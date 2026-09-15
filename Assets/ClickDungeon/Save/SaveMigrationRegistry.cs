using System;
using System.Collections.Generic;

namespace ClickDungeon.Save
{
    public interface ISaveMigration<T>
    {
        int FromVersion { get; }
        int ToVersion { get; }
        T Migrate(T source);
    }

    public sealed class SaveMigrationRegistry<T>
    {
        private readonly Dictionary<int, ISaveMigration<T>> migrations = new Dictionary<int, ISaveMigration<T>>();

        public void Register(ISaveMigration<T> migration)
        {
            if (migration == null) throw new ArgumentNullException(nameof(migration));
            if (migration.ToVersion != checked(migration.FromVersion + 1))
                throw new InvalidOperationException("Each save migration must advance exactly one schema version.");
            if (migrations.ContainsKey(migration.FromVersion))
                throw new InvalidOperationException("A migration is already registered for this source version.");
            migrations.Add(migration.FromVersion, migration);
        }

        public T Migrate(T source, int sourceVersion, int targetVersion)
        {
            if (sourceVersion > targetVersion)
                throw new InvalidOperationException("future schema cannot be migrated backward");
            if (sourceVersion < 0 || targetVersion < 1)
                throw new ArgumentOutOfRangeException(nameof(sourceVersion));

            var current = source;
            var version = sourceVersion;
            while (version < targetVersion)
            {
                if (!migrations.TryGetValue(version, out var migration))
                    throw new InvalidOperationException("migration gap prevents loading this save");
                current = migration.Migrate(current);
                version = migration.ToVersion;
            }
            return current;
        }
    }
}
