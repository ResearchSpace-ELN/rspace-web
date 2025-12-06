import { Effect } from "effect";
import { Atom, Result, useAtomSuspense } from "@effect-atom/atom-react";
import { RaidClient } from "@/modules/raid/raidApi";
import { GroupsClient } from "@/modules/groups/groupsApi";
import Typography from "@mui/material/Typography";
import useOauthToken from "@/hooks/auth/useOauthToken";

const RaIDConnectionsTable = ({
  groupId,
}: {
  groupId: string;
}) => {
  const { getToken } = useOauthToken();
  const groupInfoAtom = Atom.runtime(
    Effect.gen(function* () {
      const token = yield* Effect.promise(() => getToken());

      return yield* GroupsClient.{
        path: {
          id: groupId,
        },
        headers: {
          authorization: `Bearer ${token}`,
        },
      });
    }),
  );


  const groupInfo = useAtomSuspense(groupInfoAtom);

  const raidIntegrationInfo = useAtomSuspense(
    RaidClient.query("integrationInfo", "getRaidIntegrationInfo", {}),
  );
  // const data = useAtomSuspense(RaidClient.query("raidApps", "getRaidApps", {}));



  const groupInfoData = Result.getOrThrow(groupInfo);

  if (groupInfoData.type !== "PROJECT_GROUP") {
    return (
      <Typography variant="body1">
        NOTE: Only project groups can have RaID Connections.
      </Typography>
    );
  }

  const raidIntegrationInfoData = Result.getOrElse(raidIntegrationInfo, () => ({
    data: {
      available: false,
    },
  }));

  if (!raidIntegrationInfoData || !raidIntegrationInfoData.data?.available)
    return <Typography>RaID </Typography>;

  return (
    <>
      {/*<p>Count: {JSON.stringify(Result.getOrElse(data, () => []))}</p>*/}
      <p>
        RaID Integration Info:{" "}
        {JSON.stringify(
          Result.getOrElse(raidIntegrationInfo, () => ({
            data: { available: false },
          })),
        )}
      </p>
    </>
  );
};

export default RaIDConnectionsTable;
