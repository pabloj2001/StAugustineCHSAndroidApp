package ca.staugustinechs.staugustineapp.Interfaces;

import java.util.List;

import ca.staugustinechs.staugustineapp.Objects.ClubAnnouncement;

public interface ClubAnnounGetter {

    void updateAnnouns(List<ClubAnnouncement> clubAnnouns);

    void setOffline();

}
