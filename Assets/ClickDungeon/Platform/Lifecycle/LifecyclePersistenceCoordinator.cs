using System;
using ClickDungeon.Application.Persistence;

namespace ClickDungeon.Platform.Lifecycle
{
    public sealed class LifecyclePersistenceCoordinator
    {
        private readonly GameSessionPersistenceOrchestrator persistence;

        public LifecyclePersistenceCoordinator(GameSessionPersistenceOrchestrator persistence)
        {
            this.persistence = persistence ?? throw new ArgumentNullException(nameof(persistence));
        }

        public bool Handle(PlatformLifecycleRequest request)
        {
            if (request == null)
                throw new ArgumentNullException(nameof(request));

            if (request.Kind == PlatformLifecycleRequestKind.AutosaveCheckpoint)
            {
                persistence.OnLifecyclePauseOrBackground();
                return false;
            }

            return request.Kind == PlatformLifecycleRequestKind.PauseOrCancel;
        }
    }
}
