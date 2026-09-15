namespace ClickDungeon.Save
{
    public sealed class SettingsSave
    {
        public int SchemaVersion { get; set; } = SaveSchema.SettingsVersion;
        public int MasterVolume { get; set; } = 100;
        public int MusicVolume { get; set; } = 80;
        public int SfxVolume { get; set; } = 100;
        public bool Fullscreen { get; set; } = true;
        public int TextScale { get; set; } = 100;
        public bool ReduceMotion { get; set; }
        public int ScreenShakeScale { get; set; } = 100;
    }
}
