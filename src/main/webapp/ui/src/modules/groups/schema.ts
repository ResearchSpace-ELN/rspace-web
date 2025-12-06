import * as v from "valibot";

// User Group Info Schema - represents a user in a group
export const UserGroupInfo = v.object({
  id: v.number(),
  username: v.string(),
  role: v.picklist(["PI", "LAB_ADMIN", "USER"]),
});

export type UserGroupInfo = v.InferOutput<typeof UserGroupInfo>;

// Group Info Schema - represents a group
export const GroupInfo = v.object({
  id: v.number(),
  globalId: v.string(),
  name: v.string(),
  type: v.picklist(["LAB_GROUP", "COLLABORATION_GROUP", "PROJECT_GROUP"]),
  sharedFolderId: v.number(),
  sharedSnippetFolderId: v.number(),
  members: v.array(UserGroupInfo),
});

export type GroupInfo = v.InferOutput<typeof GroupInfo>;

// Error Schema
export const Error = v.object({
  status: v.string(),
  httpCode: v.number(),
  internalCode: v.number(),
  message: v.string(),
});

export type Error = v.InferOutput<typeof Error>;
