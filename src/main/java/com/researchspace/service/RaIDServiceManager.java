package com.researchspace.service;

import com.researchspace.model.User;
import com.researchspace.model.dtos.RaidGroupAssociation;
import com.researchspace.webapp.integrations.raid.RaIDReferenceDTO;
import java.util.Set;

public interface RaIDServiceManager {

  Set<RaIDReferenceDTO> getRaidAssociatedByUserAndAlias(User user, String serverAlias);

  void bindRaidToGroupAndSave(User user, RaidGroupAssociation raidToGroupAssociation);

  void unbindRaidFromGroupAndSave(User user, Long projectGroupId);
}
