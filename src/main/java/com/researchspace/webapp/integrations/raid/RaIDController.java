package com.researchspace.webapp.integrations.raid;

import com.researchspace.model.Group;
import com.researchspace.model.User;
import com.researchspace.model.dtos.RaidGroupAssociation;
import com.researchspace.model.field.ErrorList;
import com.researchspace.model.raid.UserRaid;
import com.researchspace.raid.model.RaID;
import com.researchspace.service.RaIDServiceManager;
import com.researchspace.service.raid.RaIDServerConfigurationDTO;
import com.researchspace.service.raid.RaIDServiceClientAdapter;
import com.researchspace.webapp.controller.AjaxReturnObject;
import com.researchspace.webapp.integrations.helper.BaseOAuth2Controller;
import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.client.HttpClientErrorException;

@Controller
@RequestMapping("/apps/raid")
public class RaIDController extends BaseOAuth2Controller {

  @Autowired private RaIDServiceClientAdapter raidServiceClientAdapter;

  @Autowired private RaIDServiceManager raidServiceManager;

  public RaIDController() {}

  /***
   * This method takes care of getting the list of the created raid by the user and remove from it
   * the ones that have been already associated in RSpace
   *
   * @param principal
   * @return the list of RaID created by the user without the ones that have been already associated
   */
  @GetMapping()
  @ResponseBody
  @ResponseStatus(HttpStatus.CREATED)
  public AjaxReturnObject<Map<String, Set<RaID>>> getRaidListByUser(Principal principal) {
    try {
      Map<String, RaIDServerConfigurationDTO> serverByAlias =
          raidServiceClientAdapter.getServerMapByAlias();
      Map<String, Set<RaID>> raidsByServerAlias = new HashMap<>();
      Set<RaID> externalRaIDList = null;
      // for each server get the list of RaID
      for (String currentServerAlias : serverByAlias.keySet()) {
        try {
          externalRaIDList =
              raidServiceClientAdapter.getRaIDList(principal.getName(), currentServerAlias);
          if (externalRaIDList != null && !externalRaIDList.isEmpty()) {
            // remove from the external list the RaID that have already been associated
            // by this user
            List<UserRaid> userRaidAlreadyAssociated =
                raidServiceManager.getAssociatedRaidByUserAndAlias(
                    userManager.getUserByUsername(principal.getName()), currentServerAlias);
            for (UserRaid currentAssociatedRaid : userRaidAlreadyAssociated) {
              externalRaIDList.remove(new RaID(currentAssociatedRaid.getRaidIdentifier()));
            }
            raidsByServerAlias.put(currentServerAlias, externalRaIDList);
          }
        } catch (Exception ex) {
          log.warn(
              "Not able to get RaID list for the user="
                  + principal.getName()
                  + " and server alias="
                  + currentServerAlias);
        }
      }
      return new AjaxReturnObject<>(raidsByServerAlias, null);
    } catch (HttpClientErrorException e) {
      log.warn("error connecting to RaID", e);
      return new AjaxReturnObject<>(null, ErrorList.of("Error connecting to RaID."));
    }
  }

  // TODO[nik]:  remove the GET since it is here just for testing without UI
  @GetMapping("/associate/{projectGroupId}/{raidServerAlias}/{raidIdentifier}")
  @ResponseStatus(HttpStatus.CREATED)
  @ResponseBody
  public AjaxReturnObject<Group> associateRaidToGroup_GET(
      @PathVariable Long projectGroupId,
      @PathVariable String raidServerAlias,
      @PathVariable String raidIdentifier) {
    RaidGroupAssociation input =
        new RaidGroupAssociation(
            projectGroupId, new RaIDReferenceDTO(raidServerAlias, raidIdentifier));
    return associateRaidToGroup(
        input, new BeanPropertyBindingResult(input, "raidGroupAssociation"));
  }

  @PostMapping()
  @ResponseStatus(HttpStatus.CREATED)
  @ResponseBody
  public AjaxReturnObject<Group> associateRaidToGroup(
      @RequestBody RaidGroupAssociation raidGroupAssociation, BindingResult errors) {
    validateInput(raidGroupAssociation);
    Group group = null;
    try {
      User user = userManager.getAuthenticatedUserInSession();
      group = groupManager.getGroup(raidGroupAssociation.getProjectGroupId());
      //      Validate.isTrue(group.getGroupType().equals(GroupType.PROJECT_GROUP),
      //          "Only Project Group can be associated to RaID");
      if (group.getRaid() != null) {
        group.setRaid(
            new UserRaid(
                user,
                group,
                raidGroupAssociation.getRaid().getRaidServerAlias(),
                raidGroupAssociation.getRaid().getRaidIdentifier()));
      } else {
        errors.rejectValue(
            "projectGroupId",
            "raid.alreadyAssociated",
            "The group with projectGroupId"
                + raidGroupAssociation.getProjectGroupId()
                + " was had already a RaID association. Please remove it first before to associate"
                + " another RaID");
      }
      groupManager.saveGroup(group, false, user);
    } catch (Exception e) {
      errors.reject(
          "associateRaidToGroup", "Impossible to associate RaID to group: " + e.getMessage());
    }
    if (errors.hasErrors()) {
      ErrorList el = inputValidator.populateErrorList(errors, new ErrorList());
      return new AjaxReturnObject<>(null, el);
    }

    return new AjaxReturnObject<>(group, null);
  }

  // TODO[nik]: RSDEV-849 - Create all the other end points (but Auth2.0) for managing RaID

  private static void validateInput(RaidGroupAssociation raidGroupAssociation) {
    Validate.isTrue(raidGroupAssociation.getProjectGroupId() != null, "projectGroupId is missing");
    Validate.isTrue(
        StringUtils.isNotBlank(raidGroupAssociation.getRaid().getRaidIdentifier()),
        "raidIdentifier is missing");
    Validate.isTrue(
        StringUtils.isNotBlank(raidGroupAssociation.getRaid().getRaidServerAlias()),
        "raidServerAlias is missing");
  }
}
