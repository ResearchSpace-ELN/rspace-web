package com.researchspace.webapp.integrations.raid;

import com.researchspace.model.Group;
import com.researchspace.model.GroupType;
import com.researchspace.model.User;
import com.researchspace.model.dtos.RaidGroupAssociation;
import com.researchspace.model.field.ErrorList;
import com.researchspace.model.raid.UserRaid;
import com.researchspace.service.RaIDServiceManager;
import com.researchspace.service.raid.RaIDServerConfigurationDTO;
import com.researchspace.service.raid.RaIDServiceClientAdapter;
import com.researchspace.webapp.controller.AjaxReturnObject;
import com.researchspace.webapp.integrations.helper.BaseOAuth2Controller;
import java.security.Principal;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
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
  public AjaxReturnObject<Set<RaIDReferenceDTO>> getRaidListByUser(Principal principal) {
    Set<RaIDReferenceDTO> result = new HashSet<>();
    try {
      Map<String, RaIDServerConfigurationDTO> serverByAlias =
          raidServiceClientAdapter.getServerMapByAlias();
      Set<RaIDReferenceDTO> externalRaIDList = null;
      // for each server get the list of RaID
      for (String currentServerAlias : serverByAlias.keySet()) {
        try {
          externalRaIDList =
              raidServiceClientAdapter.getRaIDList(principal.getName(), currentServerAlias);
          if (externalRaIDList != null && !externalRaIDList.isEmpty()) {
            // remove from the external list the RaID that have already been associated
            // by this user
            Set<RaIDReferenceDTO> userRaidAlreadyAssociated =
                raidServiceManager.getAssociatedRaidByUserAndAlias(
                    userManager.getUserByUsername(principal.getName()), currentServerAlias);
            externalRaIDList.removeAll(userRaidAlreadyAssociated);

            result.addAll(externalRaIDList);
          }
        } catch (Exception ex) {
          log.warn(
              "Not able to get RaID list for the user="
                  + principal.getName()
                  + " and server alias="
                  + currentServerAlias);
        }
      }
    } catch (HttpClientErrorException e) {
      log.warn("error connecting to RaID", e);
      return new AjaxReturnObject<>(null, ErrorList.of("Error connecting to RaID."));
    }
    return new AjaxReturnObject<>(result, null);
  }

  // TODO[nik]:  remove the GET since it is here just for testing without UI
  @GetMapping("/associate/{projectGroupId}/{raidServerAlias}")
  @ResponseStatus(HttpStatus.CREATED)
  @ResponseBody
  public AjaxReturnObject<Group> associateRaidToGroup_GET(
      @PathVariable Long projectGroupId,
      @PathVariable String raidServerAlias,
      @RequestParam(name = "raidIdentifier") String raidIdentifier) {
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
      Validate.isTrue(
          group.getGroupType().equals(GroupType.PROJECT_GROUP),
          "Only Project Group can be associated to RaID");

      if (group.getRaid() == null) {
        group.setRaid(
            new UserRaid(
                user,
                group,
                raidGroupAssociation.getRaid().getRaidServerAlias(),
                raidGroupAssociation.getRaid().getRaidIdentifier()));
        groupManager.saveGroup(group, false, user);
        log.info(
            "ProjectGroupId \""
                + group.getId()
                + "\" has been associated to RaID \""
                + raidGroupAssociation.getRaid().getRaidIdentifier()
                + "\"");
      } else {
        String message =
            "The group with projectGroupId"
                + raidGroupAssociation.getProjectGroupId()
                + " was had already a RaID association. Please remove it first before to associate"
                + " another RaID";
        errors.rejectValue("projectGroupId", "raid.alreadyAssociated", message);
        log.error(message);
      }
    } catch (Exception e) {
      errors.reject(
          "associateRaidToGroup", "Not able to associate RaID to group: " + e.getMessage());
      log.error("Not able to associate RaID to group: " + e.getMessage());
    }
    if (errors.hasErrors()) {
      ErrorList el = inputValidator.populateErrorList(errors, new ErrorList());
      return new AjaxReturnObject<>(null, el);
    }

    return new AjaxReturnObject<>(group, null);
  }

  // TODO[nik]:  remove the GET since it is here just for testing without UI
  @GetMapping("/disassociate/{projectGroupId}")
  @ResponseStatus(HttpStatus.CREATED)
  @ResponseBody
  public AjaxReturnObject<Group> disassociateRaidToGroup_GET(@PathVariable Long projectGroupId) {
    return disassociateRaidFromGroup(
        projectGroupId, new BeanPropertyBindingResult(projectGroupId, "projectGroupId"));
  }

  @DeleteMapping()
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  public AjaxReturnObject<Group> disassociateRaidFromGroup(
      @RequestBody Long groupId, BindingResult errors) {
    Group group = null;
    try {
      User user = userManager.getAuthenticatedUserInSession();
      group = groupManager.getGroup(groupId);
      Validate.isTrue(
          group.getGroupType().equals(GroupType.PROJECT_GROUP),
          "Only Project Group can be disassociated to RaID");

      if (group.getRaid() != null) {
        UserRaid raidToDelete = group.getRaid();
        raidServiceManager.deleteAssociation(user, group, raidToDelete);
        log.info("ProjectGroupId \"" + groupId + "\" has not more RaID associated");
      } else {
        errors.rejectValue(
            "projectGroupId",
            "raid.noAssociationFound",
            "The group with projectGroupId " + groupId + " has no RaiDs already associated.");
        log.error("The group with projectGroupId " + groupId + " has no RaiDs already associated.");
      }
    } catch (Exception e) {
      errors.reject(
          "disassociateRaidFromGroup",
          "Not able to disassociate RaID from group: " + e.getMessage());
      log.error("Not able to disassociate RaID from group: " + e.getMessage());
    }
    if (errors.hasErrors()) {
      ErrorList el = inputValidator.populateErrorList(errors, new ErrorList());
      return new AjaxReturnObject<>(null, el);
    }

    return new AjaxReturnObject<>(group, null);
  }

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
