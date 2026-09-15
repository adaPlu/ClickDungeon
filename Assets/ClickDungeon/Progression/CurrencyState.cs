using System;
using System.Collections.Generic;
using ClickDungeon.Core.Content;

namespace ClickDungeon.Progression
{
    public sealed class CurrencyState
    {
        private readonly Dictionary<ContentId, long> balances = new Dictionary<ContentId, long>();

        public long GetBalance(ContentId currencyId) => balances.TryGetValue(currencyId, out var value) ? value : 0L;

        public void Credit(ContentId currencyId, long amount)
        {
            if (amount < 0) throw new ArgumentOutOfRangeException(nameof(amount));
            var next = checked(GetBalance(currencyId) + amount);
            balances[currencyId] = next;
        }

        public bool Debit(ContentId currencyId, long amount)
        {
            if (amount < 0) throw new ArgumentOutOfRangeException(nameof(amount));
            var current = GetBalance(currencyId);
            if (amount > current) return false;
            balances[currencyId] = current - amount;
            return true;
        }
    }
}
