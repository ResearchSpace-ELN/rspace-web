import { HttpApiGroup } from "@effect/platform";
import { HttpApi } from "@effect/platform";
import { HttpApiEndpoint } from "@effect/platform";
import { AtomHttpApi } from "@effect-atom/atom-react";
import {
  ErrorList,
  GetRaIDListResponse,
  IntegrationRaIdInfoResponse,
} from "@/modules/raid/schema";
import { RSpaceFetchClient } from "@/modules/common/api";

export class RaidApi extends HttpApi.make("raidApi")
  .add(
    HttpApiGroup.make("raidApps").add(
      HttpApiEndpoint.get("getRaidApps", "/apps/raid")
        .addSuccess(GetRaIDListResponse)
        .addError(ErrorList),
    ),
  )
  .add(
    HttpApiGroup.make("integrationInfo").add(
      HttpApiEndpoint.get(
        "getRaidIntegrationInfo",
        "/integration/integrationInfo?name=RAID",
      )
        .addSuccess(IntegrationRaIdInfoResponse)
        .addError(ErrorList),
    ),
  ) {}

export class RaidClient extends AtomHttpApi.Tag<RaidClient>()("RaidApiClient", {
  api: RaidApi,
  // Provide a Layer that provides the HttpClient
  httpClient: RSpaceFetchClient,
  baseUrl: "/",
}) {}

