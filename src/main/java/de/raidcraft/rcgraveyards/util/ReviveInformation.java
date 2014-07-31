package de.raidcraft.rcgraveyards.util;

import lombok.Getter;

import java.util.UUID;

/**
 * @author Philip Urban
 */
@Getter
public class ReviveInformation {

    private int reviveDelay;
    private boolean looted;
    private UUID robberId;
    private ReviveReason reason;

    public ReviveInformation(int reviveDelay, boolean looted, UUID robberId, ReviveReason reason) {

        this.reviveDelay = reviveDelay;
        this.looted = looted;
        this.robberId = robberId;
        this.reason = reason;
    }

    public void increaseReviveDelay() {

        this.reviveDelay++;
    }

    public void decreaseReviveDelay() {

        this.reviveDelay--;
    }
}
