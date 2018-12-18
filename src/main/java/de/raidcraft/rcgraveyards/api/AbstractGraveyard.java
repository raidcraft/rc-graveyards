package de.raidcraft.rcgraveyards.api;

import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import de.raidcraft.rcgraveyards.GraveyardPlayer;
import de.raidcraft.util.CaseInsensitiveHashSet;
import de.raidcraft.util.LocationUtil;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.bukkit.Location;

import java.util.EnumSet;
import java.util.Set;

@Data
@EqualsAndHashCode(of = "identifier")
public abstract class AbstractGraveyard implements Graveyard {

    private final String identifier;
    private final Set<String> regions = new CaseInsensitiveHashSet();
    private String name;
    private Location location;
    private EnumSet<Type> types;
    private int discoveryRadius;
    private int respawnRadius;

    protected AbstractGraveyard(String identifier) {
        this.identifier = identifier;
    }

    @Override
    public boolean canRespawn(GraveyardPlayer player) {

        boolean insideRegion = LocationUtil.getWorldGuardRegions(player.getPlayer().getLocation())
                .map(ApplicableRegionSet::getRegions)
                .map(protectedRegions -> protectedRegions.stream()
                        .map(ProtectedRegion::getId)
                        .anyMatch(id -> getRegions().contains(id))
                ).orElse(getRegions().isEmpty());

        return (getTypes().contains(Type.MAIN) || player.hasDiscovered(this)) && insideRegion;
    }
}
