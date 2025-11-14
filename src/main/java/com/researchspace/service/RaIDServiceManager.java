package com.researchspace.service;

import com.researchspace.model.Group;
import com.researchspace.model.User;
import com.researchspace.model.raid.UserRaid;
import com.researchspace.webapp.integrations.raid.RaIDReferenceDTO;
import java.util.Set;

public interface RaIDServiceManager {

  Set<RaIDReferenceDTO> getAssociatedRaidByUserAndAlias(User user, String serverAlias);

  boolean deleteAssociation(User user, Group projectGroup, UserRaid raidToDelete);
}
