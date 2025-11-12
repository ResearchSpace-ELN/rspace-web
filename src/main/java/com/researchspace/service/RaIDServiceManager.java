package com.researchspace.service;

import com.researchspace.model.User;
import com.researchspace.model.raid.UserRaid;
import java.util.List;

public interface RaIDServiceManager {

  List<UserRaid> getAssociatedRaidByUserAndAlias(User user, String serverAlias);
}
