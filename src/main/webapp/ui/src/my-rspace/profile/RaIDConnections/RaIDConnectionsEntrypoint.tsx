import { Suspense } from "react";
import { createRoot } from "react-dom/client";
import materialTheme from "@/theme";
import RaIDConnections from "@/my-rspace/profile/RaIDConnections/RaIDConnections";
import StyledEngineProvider from "@mui/styled-engine/StyledEngineProvider";
import { ThemeProvider } from "@mui/material/styles";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";

declare global {
  interface Window {
    ___RaIDConnectionsInitialised: boolean;
  }
}

if (!window.___RaIDConnectionsInitialised) {
  const domContainer = document.getElementById("raid-connections");
  if (!domContainer) {
    throw new Error("RaIDConnectionsEntrypoint: no domContainer");
  }

  const groupId = domContainer.getAttribute("data-group-id");

  if (!groupId) {
    throw new Error("RaIDConnectionsEntrypoint: no groupId");
  }

  const queryClient = new QueryClient();

  const root = createRoot(domContainer);
  root.render(
    <StyledEngineProvider injectFirst>
      <ThemeProvider theme={materialTheme}>
        <QueryClientProvider client={queryClient}>
          <Suspense fallback={<>Loading...</>}>
            <RaIDConnections groupId={groupId} />
          </Suspense>
        </QueryClientProvider>
      </ThemeProvider>
    </StyledEngineProvider>,
  );

  window.___RaIDConnectionsInitialised = true;
}