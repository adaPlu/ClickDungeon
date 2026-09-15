using System;
using System.Collections.Generic;

namespace ClickDungeon.Progression
{
    public sealed class RewardLedger
    {
        private readonly HashSet<string> pending = new HashSet<string>(StringComparer.Ordinal);
        private readonly HashSet<string> committed = new HashSet<string>(StringComparer.Ordinal);

        public bool TryBegin(string transactionId)
        {
            Validate(transactionId);
            if (pending.Contains(transactionId) || committed.Contains(transactionId)) return false;
            pending.Add(transactionId);
            return true;
        }

        public void Commit(string transactionId)
        {
            Validate(transactionId);
            if (!pending.Remove(transactionId)) throw new InvalidOperationException("Reward transaction was not pending.");
            committed.Add(transactionId);
        }

        public void Rollback(string transactionId)
        {
            Validate(transactionId);
            pending.Remove(transactionId);
        }

        public bool IsCommitted(string transactionId)
        {
            Validate(transactionId);
            return committed.Contains(transactionId);
        }

        private static void Validate(string transactionId)
        {
            if (string.IsNullOrWhiteSpace(transactionId)) throw new ArgumentException("Transaction ID is required.", nameof(transactionId));
        }
    }
}
