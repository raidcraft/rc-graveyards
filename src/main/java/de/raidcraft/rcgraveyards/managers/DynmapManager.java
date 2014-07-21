package de.raidcraft.rcgraveyards.managers;

import de.raidcraft.rcgraveyards.Graveyard;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.dynmap.DynmapAPI;
import org.dynmap.markers.Marker;
import org.dynmap.markers.MarkerAPI;
import org.dynmap.markers.MarkerSet;

/**
 * Author: Philip
 * Date: 12.12.12 - 22:16
 * Description:
 */
public class DynmapManager {

    private MarkerAPI markerAPI = null;
    private MarkerSet graveyardSet = null;

    public DynmapManager() {

        Plugin dynmap = Bukkit.getServer().getPluginManager().getPlugin("dynmap");
        if(dynmap == null) {
            return;
        }
        DynmapAPI api = (DynmapAPI) dynmap;
        markerAPI = api.getMarkerAPI();
        // TODO: i18n
        graveyardSet = markerAPI.getMarkerSet("friedhoefe");
    }

    public void addGraveyardMarker(Graveyard graveyard) {

        if (markerAPI == null ||graveyardSet == null) {
            return;
        }

        removeMarker(graveyard);

        graveyardSet.createMarker(graveyard.getName().toLowerCase().replace(" ", "_")
                , graveyard.getFriendlyName()
                , graveyard.getLocation().getWorld().getName()
                , graveyard.getLocation().getBlockX()
                , graveyard.getLocation().getBlockY()
                , graveyard.getLocation().getBlockZ()
                , markerAPI.getMarkerIcon("skull")
                , true);
    }

    public void removeMarker(Graveyard graveyard) {

        if (graveyardSet == null) {
            return;
        }
        for (Marker marker : graveyardSet.getMarkers()) {
            if (marker.getLabel().equalsIgnoreCase(graveyard.getName()) || marker.getLabel().equalsIgnoreCase(graveyard.getFriendlyName())) {
                marker.deleteMarker();
            }
        }
    }
}
