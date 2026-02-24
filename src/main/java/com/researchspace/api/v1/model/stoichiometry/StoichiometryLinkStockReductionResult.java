package com.researchspace.api.v1.model.stoichiometry;

import com.researchspace.apiutils.ApiError;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StoichiometryLinkStockReductionResult {

  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  public static class IndividualResult {
    private Long linkId;
    private boolean success;
    private ApiError error;
  }

  private List<IndividualResult> results = new ArrayList<>();
  private int successCount;
  private int errorCount;

  public void addResult(IndividualResult result) {
    this.results.add(result);
    if (result.isSuccess()) {
      successCount++;
    } else {
      errorCount++;
    }
  }
}
