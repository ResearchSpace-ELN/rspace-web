package com.researchspace.service;

import com.researchspace.api.v1.model.ApiQuantityInfo;
import com.researchspace.api.v1.model.stoichiometry.StoichiometryInventoryLinkDTO;
import com.researchspace.api.v1.model.stoichiometry.StoichiometryInventoryLinkRequest;
import com.researchspace.api.v1.model.stoichiometry.StoichiometryLinkStockReductionResult;
import com.researchspace.model.User;
import java.util.List;

public interface StoichiometryInventoryLinkManager {

  StoichiometryInventoryLinkDTO createLink(StoichiometryInventoryLinkRequest req, User user);

  StoichiometryInventoryLinkDTO getById(long linkId, User user);

  StoichiometryInventoryLinkDTO updateQuantity(long linkId, ApiQuantityInfo newQuantity, User user);

  void deleteLink(long linkId, User user);

  StoichiometryLinkStockReductionResult reduceStock(List<Long> linkIds, User user);
}
