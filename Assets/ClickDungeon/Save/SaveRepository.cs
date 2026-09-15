using System;

namespace ClickDungeon.Save
{
    public interface ISaveStore
    {
        bool Exists(string slot);
        string Read(string slot);
        void Write(string slot, string payload);
    }

    public interface ISaveCodec
    {
        string EncodeProfile(ProfileSave save);
        ProfileSave DecodeProfile(string payload);
        string EncodeRun(RunSave save);
        RunSave DecodeRun(string payload);
        string EncodeSettings(SettingsSave save);
        SettingsSave DecodeSettings(string payload);
    }

    public sealed class SaveRepository
    {
        public const string ProfileSlot = "profile";
        public const string RunSlot = "run";
        public const string SettingsSlot = "settings";

        private readonly ISaveStore store;
        private readonly ISaveCodec codec;
        private readonly SaveMigrationRegistry<ProfileSave> profileMigrations;
        private readonly SaveMigrationRegistry<RunSave> runMigrations;
        private readonly SaveMigrationRegistry<SettingsSave> settingsMigrations;

        public SaveRepository(ISaveStore store, ISaveCodec codec)
            : this(
                store,
                codec,
                new SaveMigrationRegistry<ProfileSave>(),
                new SaveMigrationRegistry<RunSave>(),
                new SaveMigrationRegistry<SettingsSave>())
        {
        }

        public SaveRepository(
            ISaveStore store,
            ISaveCodec codec,
            SaveMigrationRegistry<ProfileSave> profileMigrations,
            SaveMigrationRegistry<RunSave> runMigrations,
            SaveMigrationRegistry<SettingsSave> settingsMigrations)
        {
            this.store = store ?? throw new ArgumentNullException(nameof(store));
            this.codec = codec ?? throw new ArgumentNullException(nameof(codec));
            this.profileMigrations = profileMigrations ?? throw new ArgumentNullException(nameof(profileMigrations));
            this.runMigrations = runMigrations ?? throw new ArgumentNullException(nameof(runMigrations));
            this.settingsMigrations = settingsMigrations ?? throw new ArgumentNullException(nameof(settingsMigrations));
        }

        public void SaveProfile(ProfileSave save)
        {
            SaveValidator.ValidateProfile(save);
            store.Write(ProfileSlot, codec.EncodeProfile(save));
        }

        public bool TryLoadProfile(out ProfileSave save)
        {
            save = null;
            if (!store.Exists(ProfileSlot)) return false;
            var decoded = codec.DecodeProfile(store.Read(ProfileSlot));
            var migrated = profileMigrations.Migrate(decoded, decoded.SchemaVersion, SaveSchema.ProfileVersion);
            SaveValidator.ValidateProfile(migrated);
            save = migrated;
            return true;
        }

        public void SaveRun(RunSave save)
        {
            SaveValidator.ValidateRun(save);
            store.Write(RunSlot, codec.EncodeRun(save));
        }

        public bool TryLoadRun(out RunSave save)
        {
            save = null;
            if (!store.Exists(RunSlot)) return false;
            var decoded = codec.DecodeRun(store.Read(RunSlot));
            var migrated = runMigrations.Migrate(decoded, decoded.SchemaVersion, SaveSchema.RunVersion);
            SaveValidator.ValidateRun(migrated);
            save = migrated;
            return true;
        }

        public void SaveSettings(SettingsSave save)
        {
            SaveValidator.ValidateSettings(save);
            store.Write(SettingsSlot, codec.EncodeSettings(save));
        }

        public bool TryLoadSettings(out SettingsSave save)
        {
            save = null;
            if (!store.Exists(SettingsSlot)) return false;
            var decoded = codec.DecodeSettings(store.Read(SettingsSlot));
            var migrated = settingsMigrations.Migrate(decoded, decoded.SchemaVersion, SaveSchema.SettingsVersion);
            SaveValidator.ValidateSettings(migrated);
            save = migrated;
            return true;
        }
    }
}
