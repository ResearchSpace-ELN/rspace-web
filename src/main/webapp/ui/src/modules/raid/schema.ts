import * as v from "valibot";

// RaID Reference DTO Schema
export const RaIDReferenceDTO = v.object({
  raidServerAlias: v.string(),
  raidIdentifier: v.string(),
});

export type RaIDReferenceDTO = v.InferOutput<typeof RaIDReferenceDTO>;

// Error item schema for validation errors
export const ErrorItem = v.object({
  field: v.string(),
  errorCode: v.string(),
  defaultMessage: v.string(),
});

// Error list schema
export const ErrorList = v.object({
  errorMessages: v.array(ErrorItem),
});

// Ajax return object for RaID list endpoint
export const GetRaIDListResponse = v.object({
  data: v.nullable(v.array(RaIDReferenceDTO)),
  error: v.nullable(ErrorList),
  errorMsg: v.nullable(v.string()),
  success: v.boolean(),
});

export type GetRaIDListResponse = v.InferOutput<typeof GetRaIDListResponse>;

// Schema for the credentials stored under each options key
export const RaIDOptionValue = v.object({
  RAID_OAUTH_CONNECTED: v.pipe(v.string(), v.transform((val) => val === "true")),
  RAID_URL: v.pipe(v.string(), v.url()),
  RAID_ALIAS: v.string(),
});

// Data object for the integration info
export const IntegrationRaIdInfoData = v.object({
  name: v.literal("RAID"),
  displayName: v.literal("RaID"),
  available: v.boolean(),
  enabled: v.boolean(),
  oauthConnected: v.boolean(),
  // map of string keys to RaIDOptionValue
  options: v.record(v.string(), RaIDOptionValue),
});

// Full response schema
export const IntegrationRaIdInfoResponse = v.object({
  data: v.nullable(IntegrationRaIdInfoData),
  error: v.nullable(ErrorList),
  errorMsg: v.nullable(v.string()),
  success: v.boolean(),
});

export type IntegrationRaIdInfo = v.InferOutput<
  typeof IntegrationRaIdInfoResponse
>;

