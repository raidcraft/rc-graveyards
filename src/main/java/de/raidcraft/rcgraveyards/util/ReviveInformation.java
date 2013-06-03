package de.raidcraft.rcgraveyards.util;

/**
 * @author Philip Urban
 */
public class ReviveInformation {

    private int reviveDelay;
    private boolean looted;
    private String robber;
    private ReviveReason reason;

    public ReviveInformation(int reviveDelay, boolean looted, String robber, ReviveReason reason) {

        this.reviveDelay = reviveDelay;
        this.looted = looted;
        this.robber = robber;
        this.reason = reason;
    }

    public int getReviveDelay() {

        return reviveDelay;
    }

    public void increaseReviveDelay() {

        this.reviveDelay++;
    }

    public void decreaseReviveDelay() {

        this.reviveDelay--;
    }

    public boolean isLooted() {

        return looted;
    }

    public String getRobber() {

        return robber;
    }

    public ReviveReason getReason() {

        return reason;
    }
}
