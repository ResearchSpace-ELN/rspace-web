package com.researchspace.service.impl;

import com.researchspace.dao.RaIDDao;
import com.researchspace.model.Group;
import com.researchspace.model.User;
import com.researchspace.model.raid.UserRaid;
import com.researchspace.service.GroupManager;
import com.researchspace.service.RaIDServiceManager;
import com.researchspace.webapp.integrations.raid.RaIDReferenceDTO;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class RaIDServiceManagerImpl implements RaIDServiceManager {

  @Autowired private RaIDDao raidDao;
  @Autowired private GroupManager groupManager;

  @Override
  public Set<RaIDReferenceDTO> getAssociatedRaidByUserAndAlias(User user, String serverAlias) {
    List<UserRaid> userRaidList = raidDao.getAssociatedRaidByUserAndAlias(user, serverAlias);
    return userRaidList.stream()
        .map(r -> new RaIDReferenceDTO(r.getId(), r.getRaidServerAlias(), r.getRaidIdentifier()))
        .collect(Collectors.toSet());
  }

  @Override
  public boolean deleteAssociation(User user, Group projectGroup, UserRaid raidToDelete) {
    boolean result = true;
    try {
      projectGroup.setRaid(null);
      groupManager.saveGroup(projectGroup, false, user);
      raidDao.remove(raidToDelete.getId());
    } catch (Exception ex) {
      result = false;
    }
    return result;
  }
}
