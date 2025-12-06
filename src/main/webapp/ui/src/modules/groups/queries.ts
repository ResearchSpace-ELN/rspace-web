import {
  GroupInfo,
  Error as ErrorSchema,
  type Error as ErrorType,
} from "@/modules/groups/schema";
import { parse, parseOrThrow } from "@/modules/common/queries/parseOrThrow";
import type { Either } from "purify-ts/Either";

const API_BASE_URL = "/api/v1";

export const groupQueryKeys = {
  all: ["rspace.apps.raid"] as const,
  apps: () => [...groupQueryKeys.all, "apps"] as const,
  integrationInfo: () => [...groupQueryKeys.all, "integrationInfo"] as const,
};

export async function getGroupById(id: string, { token }: { token: string; }): Promise<GroupInfo> {
  const response = await fetch(`${API_BASE_URL}/groups/${id}`, {
    method: "GET",
    headers: {
      "X-Requested-With": "XMLHttpRequest",
      Authorization: `Bearer ${token}`,
    },
  });

  const data: unknown = await response.json();

  if (!response.ok) {
    // Try to parse and throw typed error
    const errorResult: Either<Error, ErrorType> = parse(ErrorSchema, data);
    throw errorResult
      .map((validatedError: ErrorType) => new Error(validatedError.message))
      .orDefault(new Error(`Failed to fetch group: ${response.statusText}`));
  }

  return parseOrThrow(GroupInfo, data);
}
