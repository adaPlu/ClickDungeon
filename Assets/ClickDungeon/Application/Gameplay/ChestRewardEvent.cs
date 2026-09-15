using ClickDungeon.Core.Content;

namespace ClickDungeon.Application.Gameplay
{
    public readonly struct ChestRewardEvent
    {
        public string ChestId { get; }
        public string TransactionId { get; }
        public ContentId ItemDefinitionId { get; }
        public int Quantity { get; }
        public ContentId? CurrencyId { get; }
        public long CurrencyAmount { get; }

        public ChestRewardEvent(
            string chestId,
            string transactionId,
            ContentId itemDefinitionId,
            int quantity,
            ContentId? currencyId,
            long currencyAmount)
        {
            ChestId = chestId;
            TransactionId = transactionId;
            ItemDefinitionId = itemDefinitionId;
            Quantity = quantity;
            CurrencyId = currencyId;
            CurrencyAmount = currencyAmount;
        }
    }
}
