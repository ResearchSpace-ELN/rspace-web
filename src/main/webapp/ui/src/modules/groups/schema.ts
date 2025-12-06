import { Schema } from "effect";

// User Group Info Schema - represents a user in a group
export const UserGroupInfo = Schema.Struct({
  id: Schema.Number,
  username: Schema.String,
  role: Schema.Literal("PI", "LAB_ADMIN", "USER"),
});

export type UserGroupInfo = Schema.Schema.Type<typeof UserGroupInfo>;

// Group Info Schema - represents a group
export const GroupInfo = Schema.Struct({
  id: Schema.Number,
  globalId: Schema.Number,
  name: Schema.String,
  type: Schema.Literal("LAB_GROUP", "COLLABORATION_GROUP", "PROJECT_GROUP"),
  sharedFolderId: Schema.Number,
  sharedSnippetFolderId: Schema.Number,
  members: Schema.Array(UserGroupInfo),
});

export type GroupInfo = Schema.Schema.Type<typeof GroupInfo>;

// Error Schema
export const Error = Schema.Struct({
  status: Schema.String,
  httpCode: Schema.Number,
  internalCode: Schema.Number,
  message: Schema.String,
});

export type Error = Schema.Schema.Type<typeof Error>;
