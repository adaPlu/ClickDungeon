using System.Collections.Generic;
using ClickDungeon.Dungeon.Runtime;

namespace ClickDungeon.Dungeon.Interaction
{
    public enum SpecialTileEventKind
    {
        DamageRequested,
        HealingRequested,
        TeleportRequested,
        PressurePlateActivated
    }

    public readonly struct SpecialTileEvent
    {
        public SpecialTileEventKind Kind { get; }
        public int Amount { get; }
        public FloorCoordinate Source { get; }
        public FloorCoordinate Target { get; }

        public SpecialTileEvent(SpecialTileEventKind kind, int amount, FloorCoordinate source, FloorCoordinate target)
        {
            Kind = kind;
            Amount = amount;
            Source = source;
            Target = target;
        }
    }

    public sealed class SpecialTileResolver
    {
        public IReadOnlyList<SpecialTileEvent> ResolveEntry(FloorState floor, FloorCoordinate coordinate)
        {
            var events = new List<SpecialTileEvent>();
            var cell = floor.CellAt(coordinate);
            switch (cell.BaseTerrain.Value)
            {
                case "tile.lava":
                    events.Add(new SpecialTileEvent(SpecialTileEventKind.DamageRequested, 10, coordinate, coordinate));
                    break;
            }

            if (cell.Content?.Value == "tile.fountain_heal")
                events.Add(new SpecialTileEvent(SpecialTileEventKind.HealingRequested, 20, coordinate, coordinate));

            foreach (var link in floor.Links)
            {
                if (link.Source != coordinate) continue;
                if (link.Kind == FloorLinkKind.Teleport)
                    events.Add(new SpecialTileEvent(SpecialTileEventKind.TeleportRequested, 0, link.Source, link.Target));
                else if (link.Kind == FloorLinkKind.PressurePlate && link.ConsumeOnce())
                    events.Add(new SpecialTileEvent(SpecialTileEventKind.PressurePlateActivated, 0, link.Source, link.Target));
            }

            // Teleport resolution ends here. Application moves the actor and decides whether a later command
            // resolves the destination; this resolver never recursively resolves the target in the same command.
            return events;
        }
    }
}
