import { CacheProvider } from "@emotion/react";
import Box from "@mui/material/Box";
import CssBaseline from "@mui/material/CssBaseline";
import { ThemeProvider } from "@mui/material/styles";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import * as React from "react";
import { createRoot } from "react-dom/client";
import { createMuiCssLayerCache } from "@/components/MuiCssLayerProvider";
import { color, currentPage } from "@/util/pageBranding";
import createAccentedTheme from "../accentedTheme";
import Analytics from "../components/Analytics";
import AppBar from "../components/AppBar";
import { DialogBoundary } from "../components/DialogBoundary";
import ErrorBoundary from "../components/ErrorBoundary";

const queryClient = new QueryClient();

window.addEventListener("load", () => {
  /*
   * We append the app bar to the body to be outside of the wide margins on
   * many pages
   */
  const domContainer = document.createElement("div");
  domContainer.setAttribute("id", "app-bar");
  document.body?.insertBefore(domContainer, document.body.firstChild);

  /*
   * We use a shadow DOM so that the MUI styles to not leak
   */
  const shadow = domContainer.attachShadow({ mode: "open" });
  const wrapper = document.createElement("div");
  shadow.appendChild(wrapper);

  const cache = createMuiCssLayerCache({
    key: "css",
    prepend: true,
    container: shadow,
  });

  const branding = color(currentPage());

  const root = createRoot(wrapper);
  root.render(
    <React.StrictMode>
      <CacheProvider value={cache}>
        <QueryClientProvider client={queryClient}>
          <Analytics>
            <ErrorBoundary>
              <CssBaseline />
              <meta
                name="theme-color"
                content={`hsl(${branding.background.hue}, ${branding.background.saturation}%, ${branding.background.lightness}%)`}
              />
              <ThemeProvider theme={createAccentedTheme(branding)}>
                <Box sx={{ fontSize: "1rem", lineHeight: "1.5" }}>
                  {/*
                   * We use a DialogBoundary to keep the menu inside the shadow DOM
                   */}
                  <DialogBoundary>
                    <AppBar variant="page" currentPage={currentPage()} accessibilityTips={{}} />
                  </DialogBoundary>
                </Box>
                <Box sx={{ height: "30px" }}></Box>
              </ThemeProvider>
            </ErrorBoundary>
          </Analytics>
        </QueryClientProvider>
      </CacheProvider>
    </React.StrictMode>,
  );
});
