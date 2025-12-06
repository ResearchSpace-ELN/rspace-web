import { HttpApiGroup } from "@effect/platform";
import { HttpApi } from "@effect/platform";
import { HttpApiEndpoint } from "@effect/platform";
import { AtomHttpApi } from "@effect-atom/atom-react";
import { GroupInfo, Error } from "@/modules/groups/schema";
import { RSpaceFetchClient } from "@/modules/common/api";

export class GroupsApi extends HttpApi.make("groupsApi").add(
  HttpApiGroup.make("groups").add(
    HttpApiEndpoint.get("getGroupById", "/groups/:id")
      .addSuccess(GroupInfo)
      .addError(Error),
  ),
) {}

export class GroupsClient extends AtomHttpApi.Tag<GroupsClient>()(
  "GroupsApiClient",
  {
    api: GroupsApi,
    // Provide a Layer that provides the HttpClient
    httpClient: RSpaceFetchClient,
    baseUrl: "/api/v1",
  },
) {}

