using System.Collections.Generic;

namespace ClickDungeon.Save
{
    public sealed class CoordinateSave
    {
        public int X { get; set; }
        public int Y { get; set; }
    }

    public sealed class BoardCellSave
    {
        public int X { get; set; }
        public int Y { get; set; }
        public string BaseTerrainId { get; set; } = string.Empty;
        public string StructureId { get; set; } = string.Empty;
        public string ContentId { get; set; } = string.Empty;
        public string ActorId { get; set; } = string.Empty;
        public string StateOverlayId { get; set; } = string.Empty;
        public bool Visible { get; set; }
    }

    public sealed class InventoryEntrySave
    {
        public string InstanceId { get; set; } = string.Empty;
        public string DefinitionId { get; set; } = string.Empty;
        public int Quantity { get; set; }
    }

    public sealed class StatusEffectSave
    {
        public string StatusId { get; set; } = string.Empty;
        public int Stacks { get; set; }
        public int RemainingTurns { get; set; }
    }

    public sealed class EnemyStateSave
    {
        public string EntityId { get; set; } = string.Empty;
        public string DefinitionId { get; set; } = string.Empty;
        public CoordinateSave Position { get; set; } = new CoordinateSave();
        public int CurrentHealth { get; set; }
        public List<StatusEffectSave> Statuses { get; set; } = new List<StatusEffectSave>();
    }

    public sealed class EncounterChestSave
    {
        public int Index { get; set; }
        public string TransactionId { get; set; } = string.Empty;
        public bool Unlocked { get; set; }
        public bool Claimed { get; set; }
    }

    public sealed class EncounterRoomSave
    {
        public string RoomId { get; set; } = string.Empty;
        public int ParentFloorIndex { get; set; }
        public CoordinateSave Doorway { get; set; } = new CoordinateSave();
        public string DoorKind { get; set; } = string.Empty;
        public bool DoorOpened { get; set; }
        public ulong RoomSeed { get; set; }
        public bool Cleared { get; set; }
        public string RewardMode { get; set; } = string.Empty;
        public string RewardTier { get; set; } = string.Empty;
        public List<EnemyStateSave> Monsters { get; set; } = new List<EnemyStateSave>();
        public List<EncounterChestSave> Chests { get; set; } = new List<EncounterChestSave>();
    }

    public sealed class TileRuntimeStateSave
    {
        public int X { get; set; }
        public int Y { get; set; }
        public string StateKey { get; set; } = string.Empty;
        public int IntValue { get; set; }
        public bool BoolValue { get; set; }
        public string LinkId { get; set; } = string.Empty;
    }

    public sealed class ObjectiveStateSave
    {
        public string ObjectiveId { get; set; } = string.Empty;
        public int Progress { get; set; }
        public bool Complete { get; set; }
    }

    public sealed class RunProgressionSave
    {
        public int CurrentFloorIndex { get; set; }
        public int Depth { get; set; }
        public List<string> CompletedFloorTransactionIds { get; set; } = new List<string>();
    }

    public sealed class RunSave
    {
        public int SchemaVersion { get; set; } = SaveSchema.RunVersion;
        public int GenerationVersion { get; set; }
        public int RunSeed { get; set; }
        public string HeroId { get; set; } = string.Empty;
        public int FloorIndex { get; set; }
        public List<BoardCellSave> Board { get; set; } = new List<BoardCellSave>();
        public CoordinateSave PlayerPosition { get; set; } = new CoordinateSave();
        public int PlayerHealth { get; set; }
        public int PlayerResource { get; set; }
        public List<InventoryEntrySave> Inventory { get; set; } = new List<InventoryEntrySave>();
        public List<StatusEffectSave> Statuses { get; set; } = new List<StatusEffectSave>();
        public List<EnemyStateSave> Enemies { get; set; } = new List<EnemyStateSave>();
        public List<TileRuntimeStateSave> TileStates { get; set; } = new List<TileRuntimeStateSave>();
        public List<string> CommittedRewardTransactionIds { get; set; } = new List<string>();
        public List<ObjectiveStateSave> Objectives { get; set; } = new List<ObjectiveStateSave>();
        public List<EncounterRoomSave> EncounterRooms { get; set; } = new List<EncounterRoomSave>();
        public RunProgressionSave RunProgression { get; set; } = new RunProgressionSave();
    }
}
