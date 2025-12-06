import Typography from "@mui/material/Typography";
import { Suspense } from "react";
import Stack from "@mui/material/Stack";
import RaIDConnectionsTable from "@/my-rspace/profile/RaIDConnections/RaIDConnectionsTable";
import ErrorBoundary from "@/components/ErrorBoundary";
import RaidConnectionsAddForm from "@/my-rspace/profile/RaIDConnections/RaidConnectionsAddForm";

const RaIDConnections = ({ groupId }: { groupId: string }) => {
  return (
    <Stack spacing={2} sx={{ width: "100%" }} direction="column">
      <Typography variant="h6">RaID Connections</Typography>
      <ErrorBoundary>
        <Suspense fallback={<div>Loading...</div>}>
          <RaIDConnectionsTable groupId={groupId} />
        </Suspense>
        <RaidConnectionsAddForm handleSubmit={() => {}} />
      </ErrorBoundary>
    </Stack>
  )
};

export default RaIDConnections;