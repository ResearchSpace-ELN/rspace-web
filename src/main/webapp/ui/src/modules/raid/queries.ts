import { useSuspenseQuery } from "@tanstack/react-query";
import {
  GetRaIDListResponse,
  IntegrationRaIdInfoResponse,
  type IntegrationRaIdInfo,
} from "@/modules/raid/schema";
import { parse } from "@/modules/common/queries/parseOrThrow";
import type { Either } from "purify-ts/Either";

/**
 * Query key factory for RaID-related queries
 */
export const raidQueryKeys = {
  all: ["rspace.apps.raid"] as const,
  apps: () => [...raidQueryKeys.all, "apps"] as const,
  integrationInfo: () => [...raidQueryKeys.all, "integrationInfo"] as const,
};

/**
 * Fetch the list of RaID apps for the current user
 * @returns Promise that resolves to the GetRaIDListResponse
 * @throws Error if the request fails or validation fails
 */
export async function getRaidApps(): Promise<GetRaIDListResponse> {
  const response = await fetch("/apps/raid", {
    method: "GET",
    headers: {
      "X-Requested-With": "XMLHttpRequest",
    },
  });

  const data: unknown = await response.json();

  if (!response.ok) {
    throw new Error(`Failed to fetch RaID apps: ${response.statusText}`);
  }

  const result: Either<Error, GetRaIDListResponse> = parse(
    GetRaIDListResponse,
    data,
  );
  return result.caseOf({
    Left: (error: Error) => {
      throw error;
    },
    Right: (validatedData: GetRaIDListResponse) => validatedData,
  });
}

/**
 * Fetch RaID integration information
 * @returns Promise that resolves to the IntegrationRaIdInfo
 * @throws Error if the request fails or validation fails
 */
export async function getRaidIntegrationInfo(): Promise<IntegrationRaIdInfo> {
  const response = await fetch("/integration/integrationInfo?name=RAID", {
    method: "GET",
    headers: {
      "X-Requested-With": "XMLHttpRequest",
    },
  });

  const data: unknown = await response.json();

  if (!response.ok) {
    throw new Error(
      `Failed to fetch RaID integration info: ${response.statusText}`,
    );
  }

  const result: Either<Error, IntegrationRaIdInfo> = parse(
    IntegrationRaIdInfoResponse,
    data,
  );
  return result.caseOf({
    Left: (error: Error) => {
      throw error;
    },
    Right: (validatedData: IntegrationRaIdInfo) => validatedData,
  });
}

/**
 * Hook to fetch the list of RaID apps with Suspense
 * Throws on error - wrap with Error Boundary
 * @returns useSuspenseQuery result with GetRaIDListResponse data
 */
export function useRaidAppsQuery() {
  return useSuspenseQuery({
    queryKey: raidQueryKeys.apps(),
    queryFn: getRaidApps,
  });
}

/**
 * Hook to fetch RaID integration information with Suspense
 * Throws on error - wrap with Error Boundary
 * @returns useSuspenseQuery result with IntegrationRaIdInfo data
 */
export function useRaidIntegrationInfoQuery() {
  return useSuspenseQuery({
    queryKey: raidQueryKeys.integrationInfo(),
    queryFn: getRaidIntegrationInfo,
  });
}
