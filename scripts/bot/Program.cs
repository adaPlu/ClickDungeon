using System;
using System.Collections.Generic;
using System.Text;
using ClickDungeon.Application.Gameplay;
using ClickDungeon.Combat;
using ClickDungeon.Core.Content;
using ClickDungeon.Dungeon.Generation;
using ClickDungeon.Dungeon.Interaction;
using ClickDungeon.Dungeon.Runtime;
using ClickDungeon.Progression;

internal static class Program
{
    private const ulong BaseSeed = 0xC1C1D00D5EED1234UL;
    private const ulong SeedStep = 0x9E3779B97F4A7C15UL;

    private static int Main(string[] args)
    {
        try
        {
            var runs = ParseRuns(args);
            var totalTurns = 0;

            for (var i = 0; i < runs; i++)
            {
                var seed = unchecked(BaseSeed + ((ulong)i * SeedStep));
                var first = PlayOne(seed);
                var replay = PlayOne(seed);

                Require(first.Digest == replay.Digest, $"seed {seed:X16} replay diverged");
                totalTurns += first.Turns;
            }

            Console.WriteLine(
                $"BOT_SOAK_PASS runs={runs} turns={totalTurns} " +
                "determinism=PASS reward_idempotency=PASS chest_idempotency=PASS");
            return 0;
        }
        catch (Exception ex)
        {
            Console.Error.WriteLine($"BOT_SOAK_FAIL {ex.GetType().Name}: {ex.Message}");
            return 1;
        }
    }

    private static int ParseRuns(string[] args)
    {
        var runs = 50;
        for (var i = 0; i < args.Length; i++)
        {
            if (!string.Equals(args[i], "--runs", StringComparison.Ordinal)) continue;
            if (i + 1 >= args.Length || !int.TryParse(args[i + 1], out runs) || runs <= 0 || runs > 1000)
                throw new ArgumentException("--runs must be an integer from 1 through 1000.");
            i++;
        }
        return runs;
    }

    private static RunResult PlayOne(ulong seed)
    {
        var floor = new DungeonGenerator().Generate(seed, 1, 1);
        Require(floor.Start.X == 0, "generated start must be on the left edge");
        Require(floor.Exit.X == FloorState.Width - 1, "generated exit must be on the right edge");
        Require(floor.IsInBounds(floor.Start) && floor.IsInBounds(floor.Exit), "generated endpoints must be in bounds");

        DecorateExercisePath(floor);

        var player = new CombatantState("hero.clickington", 120, new CombatStats(12, 3, 8));
        var enemy = new CombatantState("enemy.bot_goblin", 28, new CombatStats(7, 2, 4));
        var goldId = ContentId.Parse("currency.gold");
        var combatItemId = ContentId.Parse("item.bot_loot");
        var combatInstanceId = $"bot-loot-{seed:X16}";

        var inventory = new InventoryState();
        var currency = new CurrencyState();
        var ledger = new RewardLedger();
        var grants = new RewardGrantService(ledger, inventory, currency);
        var rewardSource = new RepeatingCombatRewardSource(
            enemy,
            new RewardGrant(
                $"reward:bot:{seed:X16}",
                combatInstanceId,
                combatItemId,
                1,
                goldId,
                7));

        var session = new GameplaySession(
            floor,
            player,
            floor.Start,
            new BotTraversalPolicy(),
            new SpecialTileResolver(),
            new BotPlayerCombatPhase(player, enemy, seed ^ 0xA5A5A5A5A5A5A5A5UL),
            new BotEnemyTurnPhase(player, enemy, seed ^ 0x5A5A5A5A5A5A5A5AUL),
            rewardSource,
            grants,
            runSeed: (int)(seed & 0x7FFFFFFF),
            floorIndex: 1);

        var digest = new StringBuilder();
        digest.Append("seed=").Append(seed.ToString("X16"));
        digest.Append(";start=").Append(floor.Start.X).Append(',').Append(floor.Start.Y);
        digest.Append(";exit=").Append(floor.Exit.X).Append(',').Append(floor.Exit.Y);

        var coverage = new Coverage();
        var turns = 0;

        ExpectRejectedMove(session, new FloorCoordinate(-1, floor.Start.Y), "out-of-bounds move");
        ExpectRejectedMove(session, new FloorCoordinate(2, floor.Start.Y), "non-adjacent move");

        var slashId = ContentId.Parse("ability.bot_slash");
        while (!enemy.IsDefeated && turns < 10)
        {
            Record(session.ResolveTurn(PlayerCommand.Ability(slashId)), digest, coverage);
            turns++;
            Require(!player.IsDefeated, "bot player died during deterministic combat setup");
        }
        Require(enemy.IsDefeated, "bot failed to defeat the deterministic enemy within 10 turns");
        Require(coverage.PlayerCombat, "player combat phase was not exercised");
        Require(coverage.EnemyCombat, "enemy combat phase was not exercised");
        Require(coverage.RewardGranted, "combat reward was not granted");

        Require(inventory.GetRequired(combatInstanceId).Quantity == 1, "combat reward item was not granted exactly once");
        Require(currency.GetBalance(goldId) == 7, "combat reward currency was not granted exactly once");

        var duplicateRewardTurn = session.ResolveTurn(PlayerCommand.Wait());
        Record(duplicateRewardTurn, digest, coverage);
        turns++;
        Require(CountPhase(duplicateRewardTurn, GameplayTurnPhase.Reward) == 0, "duplicate reward emitted another reward event");
        Require(inventory.GetRequired(combatInstanceId).Quantity == 1, "duplicate reward increased item quantity");
        Require(currency.GetBalance(goldId) == 7, "duplicate reward increased currency balance");

        var pathY = floor.Start.Y;
        for (var x = 1; x < FloorState.Width; x++)
        {
            var result = session.ResolveTurn(PlayerCommand.Move(new FloorCoordinate(x, pathY)));
            Record(result, digest, coverage);
            turns++;
            Require(floor.IsInBounds(session.PlayerPosition), "bot left the 5x5 board");
            Require(result.PlayerHealth >= 0 && result.PlayerHealth <= player.MaxHealth, "player health escaped valid bounds");
            Require(!result.PlayerDefeated, "bot player was unexpectedly defeated while traversing the exercise path");
        }

        Require(session.PlayerPosition == floor.Exit, "teleport/path did not finish at the generated exit");
        Require(coverage.TileDamage, "lava damage path was not exercised");
        Require(coverage.TileHealing, "healing fountain path was not exercised");
        Require(coverage.PressurePlate, "pressure-plate path was not exercised");
        Require(coverage.Teleport, "teleport path was not exercised");

        var chest = new ChestInteractionState("chest.bot", requiredInteractions: 3, requiredKeyTag: "key.bot");
        Require(!chest.RegisterInteraction("key.wrong"), "locked chest accepted the wrong key tag");
        Require(chest.RegisterInteraction("key.bot"), "chest interaction 1 failed");
        Require(chest.RegisterInteraction("key.bot"), "chest interaction 2 failed");
        Require(chest.RegisterInteraction("key.bot"), "chest interaction 3 failed");
        Require(chest.IsReadyToCommit, "chest did not become commit-ready");

        var chestItemId = ContentId.Parse("item.bot_chest_loot");
        var chestInstanceId = $"bot-chest-{seed:X16}";
        Require(
            session.TryCommitChestReward(
                chest,
                chestInstanceId,
                chestItemId,
                1,
                goldId,
                11,
                out var chestReward),
            "ready chest reward did not commit");
        Require(chest.RewardCommitted, "chest did not mark reward committed");
        Require(session.ChestOrdinal == 1, "chest ordinal did not advance exactly once");

        Require(
            !session.TryCommitChestReward(
                chest,
                chestInstanceId,
                chestItemId,
                1,
                goldId,
                11,
                out _),
            "same chest reward committed more than once");
        Require(session.ChestOrdinal == 1, "duplicate chest commit advanced ordinal");
        Require(inventory.GetRequired(chestInstanceId).Quantity == 1, "duplicate chest commit changed chest item quantity");
        Require(currency.GetBalance(goldId) == 18, "currency total does not match one combat reward plus one chest reward");

        digest.Append(";chest=").Append(chestReward.TransactionId);
        digest.Append(";final=").Append(session.PlayerPosition.X).Append(',').Append(session.PlayerPosition.Y);
        digest.Append(";hp=").Append(player.CurrentHealth);
        digest.Append(";gold=").Append(currency.GetBalance(goldId));
        digest.Append(";turns=").Append(turns);

        return new RunResult(digest.ToString(), turns);
    }

    private static void DecorateExercisePath(FloorState floor)
    {
        var y = floor.Start.Y;
        floor.CellAt(new FloorCoordinate(1, y)).BaseTerrain = ContentId.Parse("tile.lava");
        floor.CellAt(new FloorCoordinate(2, y)).Content = ContentId.Parse("tile.fountain_heal");
        floor.AddLink(new FloorLink(
            ContentId.Parse("link.bot_pressure"),
            FloorLinkKind.PressurePlate,
            new FloorCoordinate(3, y),
            new FloorCoordinate(3, y)));
        floor.AddLink(new FloorLink(
            ContentId.Parse("link.bot_teleport"),
            FloorLinkKind.Teleport,
            new FloorCoordinate(4, y),
            floor.Exit));
    }

    private static void ExpectRejectedMove(GameplaySession session, FloorCoordinate target, string label)
    {
        var before = session.PlayerPosition;
        try
        {
            session.ResolveTurn(PlayerCommand.Move(target));
            throw new InvalidOperationException($"{label} was accepted");
        }
        catch (InvalidOperationException ex)
        {
            if (string.Equals(ex.Message, $"{label} was accepted", StringComparison.Ordinal)) throw;
        }
        Require(session.PlayerPosition == before, $"{label} mutated player position");
    }

    private static int CountPhase(GameplayTurnResult result, GameplayTurnPhase phase)
    {
        var count = 0;
        for (var i = 0; i < result.Events.Count; i++)
            if (result.Events[i].Phase == phase) count++;
        return count;
    }

    private static void Record(GameplayTurnResult result, StringBuilder digest, Coverage coverage)
    {
        digest.Append("|p=").Append(result.PlayerPosition.X).Append(',').Append(result.PlayerPosition.Y);
        digest.Append(";h=").Append(result.PlayerHealth);
        for (var i = 0; i < result.Events.Count; i++)
        {
            var item = result.Events[i];
            digest.Append(';').Append(item.Phase).Append(':').Append(item.Kind).Append(':').Append(item.Amount);

            if (item.Phase == GameplayTurnPhase.PlayerCombat && item.Kind == CombatEventKind.DamageApplied.ToString())
                coverage.PlayerCombat = true;
            if (item.Phase == GameplayTurnPhase.Enemy && item.Kind == CombatEventKind.DamageApplied.ToString())
                coverage.EnemyCombat = true;
            if (item.Phase == GameplayTurnPhase.Tile && item.Kind == SpecialTileEventKind.DamageRequested.ToString())
                coverage.TileDamage = true;
            if (item.Phase == GameplayTurnPhase.Tile && item.Kind == SpecialTileEventKind.HealingRequested.ToString())
                coverage.TileHealing = true;
            if (item.Phase == GameplayTurnPhase.Tile && item.Kind == SpecialTileEventKind.PressurePlateActivated.ToString())
                coverage.PressurePlate = true;
            if (item.Phase == GameplayTurnPhase.Tile && item.Kind == SpecialTileEventKind.TeleportRequested.ToString())
                coverage.Teleport = true;
            if (item.Phase == GameplayTurnPhase.Reward && item.Kind == "RewardGranted")
                coverage.RewardGranted = true;
        }
    }

    private static void Require(bool condition, string message)
    {
        if (!condition) throw new InvalidOperationException(message);
    }

    private sealed class BotTraversalPolicy : ITraversalPolicy
    {
        public bool CanEnter(FloorState floor, FloorCoordinate coordinate)
        {
            var structure = floor.CellAt(coordinate).Structure?.Value;
            return structure != "tile.wall" &&
                   structure != "tile.wall_corner" &&
                   structure != "tile.door_locked" &&
                   structure != "tile.stair_up_locked" &&
                   structure != "tile.stair_down_locked";
        }
    }

    private sealed class SeededCombatRandom : ICombatRandom
    {
        private readonly DeterministicRng rng;

        public SeededCombatRandom(ulong seed)
        {
            rng = new DeterministicRng(seed);
        }

        public int NextPercent() => rng.NextInt(0, 100);
    }

    private sealed class BotPlayerCombatPhase : IPlayerCombatPhase
    {
        private readonly CombatantState player;
        private readonly CombatantState enemy;
        private readonly CombatResolver resolver = new CombatResolver();
        private readonly AbilityDefinition ability = new AbilityDefinition(
            ContentId.Parse("ability.bot_slash"),
            flatDamage: 5,
            attackScalingPermille: 500,
            criticalChancePercent: 25);
        private readonly SeededCombatRandom random;

        public BotPlayerCombatPhase(CombatantState player, CombatantState enemy, ulong seed)
        {
            this.player = player;
            this.enemy = enemy;
            random = new SeededCombatRandom(seed);
        }

        public IReadOnlyList<CombatEvent> Resolve(PlayerCommand command)
        {
            if (command.Kind != PlayerCommandKind.Ability || enemy.IsDefeated)
                return Array.Empty<CombatEvent>();
            return resolver.ResolveAbility(player, enemy, ability, random);
        }
    }

    private sealed class BotEnemyTurnPhase : IEnemyTurnPhase
    {
        private readonly CombatantState player;
        private readonly CombatantState enemy;
        private readonly CombatResolver resolver = new CombatResolver();
        private readonly AbilityDefinition ability = new AbilityDefinition(
            ContentId.Parse("ability.enemy_swipe"),
            flatDamage: 5,
            attackScalingPermille: 400,
            criticalChancePercent: 10);
        private readonly SeededCombatRandom random;

        public BotEnemyTurnPhase(CombatantState player, CombatantState enemy, ulong seed)
        {
            this.player = player;
            this.enemy = enemy;
            random = new SeededCombatRandom(seed);
        }

        public IReadOnlyList<CombatEvent> Resolve()
        {
            if (enemy.IsDefeated || player.IsDefeated)
                return Array.Empty<CombatEvent>();
            return resolver.ResolveAbility(enemy, player, ability, random);
        }
    }

    private sealed class RepeatingCombatRewardSource : IRewardSource
    {
        private readonly CombatantState enemy;
        private readonly RewardGrant reward;

        public RepeatingCombatRewardSource(CombatantState enemy, RewardGrant reward)
        {
            this.enemy = enemy;
            this.reward = reward;
        }

        public IReadOnlyList<RewardGrant> Resolve()
        {
            if (!enemy.IsDefeated) return Array.Empty<RewardGrant>();
            return new[] { reward };
        }
    }

    private sealed class Coverage
    {
        public bool PlayerCombat { get; set; }
        public bool EnemyCombat { get; set; }
        public bool TileDamage { get; set; }
        public bool TileHealing { get; set; }
        public bool PressurePlate { get; set; }
        public bool Teleport { get; set; }
        public bool RewardGranted { get; set; }
    }

    private sealed class RunResult
    {
        public string Digest { get; }
        public int Turns { get; }

        public RunResult(string digest, int turns)
        {
            Digest = digest;
            Turns = turns;
        }
    }
}
