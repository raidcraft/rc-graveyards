package de.raidcraft.rcgraveyards;


import de.raidcraft.api.locations.Locations;
import de.raidcraft.rcgraveyards.api.AbstractGraveyard;
import de.raidcraft.rcgraveyards.api.Graveyard;
import de.raidcraft.util.CaseInsensitiveHashSet;
import de.raidcraft.util.DateUtil;
import de.raidcraft.util.EnumUtils;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;

import java.util.*;

/**
 * @author Philip Urban
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ConfiguredGraveyard extends AbstractGraveyard {

    public ConfiguredGraveyard(String identifier, ConfigurationSection config) {
        super(identifier);
        setName(config.getString("name", identifier));
        Locations.fromConfig(config).ifPresent(location -> {
            setDiscoveryRadius(config.getInt("discovery-radius", location.getRadius()));
            setLocation(location.getLocation());
        });
        setRespawnRadius(config.getInt("respawn-radius", 30));
        getRegions().addAll(config.getStringList("regions"));
        config.getStringList("types").stream()
                .map(type -> EnumUtils.getEnumFromString(Type.class, type))
                .filter(Objects::nonNull)
                .forEach(type -> getTypes().add(type));
    }
}
