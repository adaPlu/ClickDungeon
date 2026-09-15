using System;
using System.Collections.Generic;
using System.Globalization;

namespace ClickDungeon.Progression
{
    public sealed class RunProgressionState
    {
        private readonly HashSet<string> completedFloorTransactionIds = new HashSet<string>(StringComparer.Ordinal);

        public int CurrentFloorIndex { get; private set; }
        public int Depth { get; private set; }
        public IReadOnlyCollection<string> CompletedFloorTransactionIds => completedFloorTransactionIds;

        public RunProgressionState(int currentFloorIndex = 0, int depth = 0)
        {
            if (currentFloorIndex < 0) throw new ArgumentOutOfRangeException(nameof(currentFloorIndex));
            if (depth < 0) throw new ArgumentOutOfRangeException(nameof(depth));
            CurrentFloorIndex = currentFloorIndex;
            Depth = depth;
        }

        public static string BuildFloorTransactionId(int runSeed, int floorIndex)
        {
            if (floorIndex < 0) throw new ArgumentOutOfRangeException(nameof(floorIndex));
            return string.Format(CultureInfo.InvariantCulture, "progress:floor:{0}:{1}", runSeed, floorIndex);
        }

        public bool TryCompleteFloor(string transactionId)
        {
            if (string.IsNullOrWhiteSpace(transactionId)) throw new ArgumentException("Floor transaction ID is required.", nameof(transactionId));
            if (!completedFloorTransactionIds.Add(transactionId)) return false;

            CurrentFloorIndex = checked(CurrentFloorIndex + 1);
            Depth = checked(Depth + 1);
            return true;
        }

        public void RestoreCompletedTransactions(IEnumerable<string> transactionIds)
        {
            if (transactionIds == null) throw new ArgumentNullException(nameof(transactionIds));
            foreach (var transactionId in transactionIds)
            {
                if (string.IsNullOrWhiteSpace(transactionId)) throw new ArgumentException("Restored floor transaction IDs must be non-empty.", nameof(transactionIds));
                completedFloorTransactionIds.Add(transactionId);
            }
        }
    }
}
