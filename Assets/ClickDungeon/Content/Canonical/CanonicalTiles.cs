using System;
using System.Collections.Generic;
using ClickDungeon.Content.Definitions;
using ClickDungeon.Core.Content;

namespace ClickDungeon.Content.Canonical
{
    public static class CanonicalTiles
    {
        private static readonly IReadOnlyList<TileDefinition> Definitions = Build();
        public static IReadOnlyList<TileDefinition> All => Definitions;

        private static IReadOnlyList<TileDefinition> Build()
        {
            var items = new List<TileDefinition>(24);
            Add("tile.floor_stone", "Stone Floor", TileLayer.BaseTerrain, "Art/Runtime/Tiles/tile_floor_stone.png", items);
            Add("tile.trap_pit", "Pit Trap", TileLayer.Content, "Art/Runtime/Tiles/tile_trap_pit.png", items);
            Add("tile.trap_bomb", "Bomb Trap", TileLayer.Content, "Art/Runtime/Tiles/tile_trap_bomb.png", items);
            Add("tile.trap_spike", "Spike Trap", TileLayer.Content, "Art/Runtime/Tiles/tile_trap_spike.png", items);
            Add("tile.stair_up", "Stair Up", TileLayer.Structure, "Art/Runtime/Tiles/tile_stair_up.png", items);
            Add("tile.stair_up_locked", "Locked Stair Up", TileLayer.Structure, "Art/Runtime/Tiles/tile_stair_up_locked.png", items);
            Add("tile.stair_down", "Stair Down", TileLayer.Structure, "Art/Runtime/Tiles/tile_stair_down.png", items);
            Add("tile.stair_down_locked", "Locked Stair Down", TileLayer.Structure, "Art/Runtime/Tiles/tile_stair_down_locked.png", items);
            Add("tile.wall", "Wall", TileLayer.Structure, "Art/Runtime/Tiles/tile_wall.png", items);
            Add("tile.wall_corner", "Wall Corner", TileLayer.Structure, "Art/Runtime/Tiles/tile_wall_corner.png", items);
            Add("tile.key", "Key", TileLayer.Content, "Art/Runtime/Tiles/tile_key.png", items);
            Add("tile.chest_closed", "Closed Chest", TileLayer.Content, "Art/Runtime/Tiles/tile_chest_closed.png", items);
            Add("tile.chest_open", "Open Chest", TileLayer.Content, "Art/Runtime/Tiles/tile_chest_open.png", items);
            Add("tile.door_locked", "Locked Door", TileLayer.Structure, "Art/Runtime/Tiles/tile_door_locked.png", items);
            Add("tile.door_open", "Open Door", TileLayer.Structure, "Art/Runtime/Tiles/tile_door_open.png", items);
            Add("tile.torch", "Torch", TileLayer.Content, "Art/Runtime/Tiles/tile_torch.png", items);
            Add("tile.floor_cracked", "Cracked Floor", TileLayer.BaseTerrain, "Art/Runtime/Tiles/tile_floor_cracked.png", items);
            Add("tile.floor_moss", "Mossy Floor", TileLayer.BaseTerrain, "Art/Runtime/Tiles/tile_floor_moss.png", items);
            Add("tile.water", "Water", TileLayer.BaseTerrain, "Art/Runtime/Tiles/tile_water.png", items);
            Add("tile.lava", "Lava", TileLayer.BaseTerrain, "Art/Runtime/Tiles/tile_lava.png", items);
            Add("tile.shadow", "Shadow / Void", TileLayer.BaseTerrain, "Art/Runtime/Tiles/tile_shadow.png", items);
            Add("tile.pressure_plate", "Pressure Plate", TileLayer.Content, "Art/Runtime/Tiles/tile_pressure_plate.png", items);
            Add("tile.teleport", "Teleporter", TileLayer.Content, "Art/Runtime/Tiles/tile_teleport.png", items);
            Add("tile.fountain_heal", "Healing Fountain", TileLayer.Content, "Art/Runtime/Tiles/tile_fountain_heal.png", items);
            return items;
        }

        private static void Add(string id, string displayName, TileLayer layer, string spritePath, ICollection<TileDefinition> items)
        {
            items.Add(new TileDefinition(ContentId.Parse(id), displayName, layer, spritePath));
        }
    }
}
