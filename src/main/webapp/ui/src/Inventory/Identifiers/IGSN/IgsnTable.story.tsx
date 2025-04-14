import React from "react";
import StyledEngineProvider from "@mui/styled-engine/StyledEngineProvider";
import { ThemeProvider } from "@mui/material/styles";
import materialTheme from "../../../theme";
import IgsnTable from "./IgsnTable";
import RsSet from "../../../util/set";

export default function IgsnTableStory() {
  return (
    <StyledEngineProvider injectFirst>
      <ThemeProvider theme={materialTheme}>
        <IgsnTable selectedIgsns={new RsSet([])} setSelectedIgsns={() => {}} />
      </ThemeProvider>
    </StyledEngineProvider>
  );
}
