using System;
using ClickDungeon.Core.Content;

namespace ClickDungeon.Progression
{
    public sealed class RewardGrant
    {
        public string TransactionId { get; }
        public string ItemInstanceId { get; }
        public ContentId ItemDefinitionId { get; }
        public int ItemQuantity { get; }
        public ContentId? CurrencyId { get; }
        public long CurrencyAmount { get; }

        public RewardGrant(
            string transactionId,
            string itemInstanceId,
            ContentId itemDefinitionId,
            int itemQuantity,
            ContentId? currencyId = null,
            long currencyAmount = 0)
        {
            if (string.IsNullOrWhiteSpace(transactionId)) throw new ArgumentException("Transaction ID is required.", nameof(transactionId));
            if (string.IsNullOrWhiteSpace(itemInstanceId)) throw new ArgumentException("Item instance ID is required.", nameof(itemInstanceId));
            if (itemQuantity <= 0) throw new ArgumentOutOfRangeException(nameof(itemQuantity));
            if (currencyAmount < 0) throw new ArgumentOutOfRangeException(nameof(currencyAmount));
            TransactionId = transactionId;
            ItemInstanceId = itemInstanceId;
            ItemDefinitionId = itemDefinitionId;
            ItemQuantity = itemQuantity;
            CurrencyId = currencyId;
            CurrencyAmount = currencyAmount;
        }
    }

    public sealed class RewardGrantService
    {
        private readonly RewardLedger ledger;
        private readonly InventoryState inventory;
        private readonly CurrencyState currency;

        public RewardGrantService(RewardLedger ledger, InventoryState inventory, CurrencyState currency)
        {
            this.ledger = ledger ?? throw new ArgumentNullException(nameof(ledger));
            this.inventory = inventory ?? throw new ArgumentNullException(nameof(inventory));
            this.currency = currency ?? throw new ArgumentNullException(nameof(currency));
        }

        public bool Grant(RewardGrant reward)
        {
            if (reward == null) throw new ArgumentNullException(nameof(reward));
            if (!ledger.TryBegin(reward.TransactionId)) return false;

            try
            {
                inventory.Add(reward.ItemInstanceId, reward.ItemDefinitionId, reward.ItemQuantity);
                if (reward.CurrencyId.HasValue && reward.CurrencyAmount > 0)
                    currency.Credit(reward.CurrencyId.Value, reward.CurrencyAmount);
                ledger.Commit(reward.TransactionId);
                return true;
            }
            catch
            {
                ledger.Rollback(reward.TransactionId);
                throw;
            }
        }
    }
}
