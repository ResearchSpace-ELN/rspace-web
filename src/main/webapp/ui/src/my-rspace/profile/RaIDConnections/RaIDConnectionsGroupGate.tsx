import { useSuspenseQuery } from "@tanstack/react-query";
import Typography from "@mui/material/Typography";
import RaIDConnectionsTable from "@/my-rspace/profile/RaIDConnections/RaIDConnectionsTable";
import { useOauthTokenQuery } from "@/modules/common/hooks/auth";
import { getGroupById } from "@/modules/groups/queries";

const RaIDConnectionsGroupGate = ({ groupId }) => {

  const { data: token } = useOauthTokenQuery();

  const groupInfo = useSuspenseQuery({
    queryKey: ["rspace.common.groups.groupInfo", { groupId }],
    queryFn: () => getGroupById(groupId, { token: token }),
  });


  if (groupInfo.data.type !== "PROJECT_GROUP") {
    return (
      <Typography variant="body2">
        RaID is disabled for this group - only project groups can have RaID
        Connections.
      </Typography>
    );
  }

  return <RaIDConnectionsTable groupId={groupId} />;
}

export default RaIDConnectionsGroupGate;