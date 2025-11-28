package com.researchspace.service;

import com.researchspace.model.User;
import com.researchspace.model.dtos.RaidGroupAssociation;
import com.researchspace.model.raid.UserRaid;
import com.researchspace.webapp.integrations.raid.RaIDReferenceDTO;
import java.util.Set;

public interface RaIDServiceManager {

  UserRaid getUserRaid(Long userRaidId);

  Set<RaIDReferenceDTO> getAssociatedRaidsByUserAndAlias(User user, String serverAlias);

  void bindRaidToGroupAndSave(User user, RaidGroupAssociation raidToGroupAssociation);

  void unbindRaidFromGroupAndSave(User user, Long projectGroupId);
}
