using System;

namespace ClickDungeon.Save
{
    public interface IRunSaveSnapshotProvider
    {
        RunSave CaptureRunSave();
    }

    public interface IAutosaveCheckpointSink
    {
        void Request(AutosaveReason reason);
    }

    public sealed class SaveCheckpointService : IAutosaveCheckpointSink
    {
        private readonly SaveRepository repository;
        private readonly IRunSaveSnapshotProvider snapshotProvider;

        public AutosaveReason? LastReason { get; private set; }

        public SaveCheckpointService(SaveRepository repository, IRunSaveSnapshotProvider snapshotProvider)
        {
            this.repository = repository ?? throw new ArgumentNullException(nameof(repository));
            this.snapshotProvider = snapshotProvider ?? throw new ArgumentNullException(nameof(snapshotProvider));
        }

        public void Request(AutosaveReason reason)
        {
            var snapshot = snapshotProvider.CaptureRunSave();
            SaveValidator.ValidateRun(snapshot);
            repository.SaveRun(snapshot);
            LastReason = reason;
        }
    }
}
