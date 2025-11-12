package com.researchspace.service.impl;

import com.researchspace.dao.RaIDDao;
import com.researchspace.model.User;
import com.researchspace.model.raid.UserRaid;
import com.researchspace.service.RaIDServiceManager;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class RaIDServiceManagerImpl implements RaIDServiceManager {

  @Autowired private RaIDDao raidDao;

  @Override
  public List<UserRaid> getAssociatedRaidByUserAndAlias(User user, String serverAlias) {
    return raidDao.getAssociatedRaidByUserAndAlias(user, serverAlias);
  }
}
