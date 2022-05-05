package com.give928.springdata.jpa.repository.projection;

public interface UsernameAndTeamNestedClosedProjection {
    String getUsername();
    TeamInfo getTeam();

    interface TeamInfo {
        String getName();
    }
}
