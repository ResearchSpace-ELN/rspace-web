import { Suspense } from "react";
import { createRoot } from "react-dom/client";
import materialTheme from "@/theme";
import RaIDConnections from "@/my-rspace/profile/RaIDConnections/RaIDConnections";
import StyledEngineProvider from "@mui/styled-engine/StyledEngineProvider";
import { ThemeProvider } from "@mui/material/styles";

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

  const root = createRoot(domContainer);
  root.render(
    <StyledEngineProvider injectFirst>
      <ThemeProvider theme={materialTheme}>
        <Suspense fallback={<>Loading...</>}>
          <RaIDConnections groupId={groupId} />
        </Suspense>
      </ThemeProvider>
    </StyledEngineProvider>,
  );

  window.___RaIDConnectionsInitialised = true;
}