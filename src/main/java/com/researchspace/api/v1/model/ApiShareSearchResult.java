/** RSpace API Access your RSpace documents programmatically. */
package com.researchspace.api.v1.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.researchspace.api.v1.controller.BaseApiController;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;

/** DocumentSearchResults */
@Data
@EqualsAndHashCode(callSuper = true)
@JsonPropertyOrder({"totalHits", "pageNumber", "shares", "_links"})
public class ApiShareSearchResult extends ApiPaginatedResultList<ApiShareInfo> {

  @JsonProperty("shares")
  private List<ApiShareInfo> shares = new ArrayList<>();

  @Override
  protected String getSearchEndpoint() {
    return BaseApiController.FORMS_ENDPOINT;
  }

  @Override
  public void setItems(List<ApiShareInfo> items) {
    setShares(items);
  }
}
