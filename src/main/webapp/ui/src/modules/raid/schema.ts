import { Schema } from "effect";
import { BooleanFromString } from "effect/Schema";

// RaID Reference DTO Schema
export const RaIDReferenceDTO = Schema.Struct({
  raidServerAlias: Schema.String,
  raidIdentifier: Schema.String,
});

export type RaIDReferenceDTO = Schema.Schema.Type<typeof RaIDReferenceDTO>;

// Error item schema for validation errors
export const ErrorItem = Schema.Struct({
  field: Schema.String,
  errorCode: Schema.String,
  defaultMessage: Schema.String,
});

// Error list schema
export const ErrorList = Schema.Struct({
  errorMessages: Schema.Array(ErrorItem),
});

// Ajax return object for RaID list endpoint
export const GetRaIDListResponse = Schema.Struct({
  data: Schema.NullOr(Schema.Array(RaIDReferenceDTO)),
  error: Schema.NullOr(ErrorList),
  errorMsg: Schema.NullOr(Schema.String),
  success: Schema.Boolean,
});

export type GetRaIDListResponse = Schema.Schema.Type<
  typeof GetRaIDListResponse
>;

// Endpoint definition
export const GetRaIDListEndpoint = {
  method: "GET" as const,
  path: "/apps/raid",
  response: GetRaIDListResponse,
  statusCode: 201,
  description:
    "Retrieves all RaIDs created by the authenticated user, excluding those already associated with RSpace groups",
  authentication: "required" as const,
};


// Schema for the credentials stored under each options key
export const RaIDOptionValue = Schema.Struct({
  RAID_OAUTH_CONNECTED: BooleanFromString,
  RAID_URL: Schema.URL,
  RAID_ALIAS: Schema.String,
});

// Data object for the integration info
export const IntegrationRaIdInfoData = Schema.Struct({
  name: Schema.Literal("RAID"),
  displayName: Schema.Literal("RaID"),
  available: Schema.Boolean,
  enabled: Schema.Boolean,
  oauthConnected: Schema.Boolean,
  // map of string keys to RaIDOptionValue
  options: Schema.Record({ key: Schema.String, value: RaIDOptionValue }),
});

// Full response schema
export const IntegrationRaIdInfoResponse = Schema.Struct({
  data: Schema.NullOr(IntegrationRaIdInfoData),
  error: Schema.NullOr(ErrorList),
  errorMsg: Schema.NullOr(Schema.String),
  success: Schema.Boolean,
});

export type IntegrationRaIdInfo = Schema.Schema.Type<
  typeof IntegrationRaIdInfoResponse
>;

