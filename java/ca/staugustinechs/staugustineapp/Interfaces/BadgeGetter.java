package ca.staugustinechs.staugustineapp.Interfaces;

import java.util.List;

import ca.staugustinechs.staugustineapp.Objects.Badge;

public interface BadgeGetter {

    void updateBadges(List<Badge> badges);

    void setOffline();

}
