public interface Scenario
{
    abstract int getMaxPlayers();
    abstract Tile[][] getMap();
    abstract Tile[] getPlayerAssignment(Player player, int position);
}
