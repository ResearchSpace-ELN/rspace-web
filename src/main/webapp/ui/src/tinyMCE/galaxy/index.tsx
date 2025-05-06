import React from "react";
import { createRoot } from "react-dom/client";
import createAccentedTheme from "../../accentedTheme";
import { ThemeProvider } from "@mui/material/styles";
import StyledEngineProvider from "@mui/styled-engine/StyledEngineProvider";
import { ACCENT_COLOR } from "../../assets/branding/galaxy";
import GalaxyDialog from "./GalaxyDialog";

// Define interfaces for TinyMCE and global window
declare global {
  interface Window {
    insertActions?: Map<
      string,
      {
        text: string;
        icon: string;
        action: () => void;
      }
    >;
  }

  const tinymce: {
    PluginManager: {
      add: (name: string, plugin: typeof GalaxyPlugin) => void;
    };
  };
}

// TinyMCE Editor interface
interface TinyMCEEditor {
  ui: {
    registry: {
      addButton: (
        name: string,
        config: {
          tooltip: string;
          icon: string;
          onAction: () => void;
        }
      ) => void;
      addMenuItem: (
        name: string,
        config: {
          text: string;
          icon: string;
          onAction: () => void;
        }
      ) => void;
    };
  };
}

// Galaxy Props interface
interface GalaxyProps {
  open: boolean;
  onClose?: () => void;
}

function getElement(): HTMLElement {
  if (!document.getElementById("tinymce-galaxy")) {
    const div = document.createElement("div");
    div.id = "tinymce-galaxy";
    document.body.appendChild(div);
  }
  return document.getElementById("tinymce-galaxy") as HTMLElement;
}

class GalaxyPlugin {
  constructor(editor: TinyMCEEditor) {
    function* renderGalaxy(
      domContainer: HTMLElement
    ): Generator<void, void, GalaxyProps> {
      const root = createRoot(domContainer);
      while (true) {
        const newProps: GalaxyProps = yield;
        root.render(
          <StyledEngineProvider injectFirst>
            <ThemeProvider theme={createAccentedTheme(ACCENT_COLOR)}>
              <GalaxyDialog
                open={newProps.open}
                onClose={newProps.onClose ?? (() => {})}
              />
            </ThemeProvider>
          </StyledEngineProvider>
        );
      }
    }

    if (!document.getElementById("tinymce-galaxy")) {
      const div = document.createElement("div");
      div.id = "tinymce-galaxy";
      document.body.appendChild(div);
    }
    const galaxyRenderer = renderGalaxy(getElement());
    galaxyRenderer.next({ open: false });

    // Add a button to the toolbar
    editor.ui.registry.addButton("galaxy", {
      tooltip: "Execute Galaxy workflow",
      icon: "galaxy",
      onAction() {
        galaxyRenderer.next({
          open: true,
          onClose: () => {
            galaxyRenderer.next({ open: false });
          },
        });
      },
    });

    // Adds a menu item to the insert menu
    editor.ui.registry.addMenuItem("optGalaxy", {
      text: "Execute Galaxy workflow",
      icon: "galaxy",
      onAction() {
        galaxyRenderer.next({
          open: true,
          onClose: () => {
            galaxyRenderer.next({ open: false });
          },
        });
      },
    });

    // Adds an option to the slash-menu
    if (!window.insertActions) window.insertActions = new Map();
    window.insertActions.set("optGalaxy", {
      text: "Execute Galaxy workflow",
      icon: "galaxy",
      action: () => {
        galaxyRenderer.next({
          open: true,
          onClose: () => {
            galaxyRenderer.next({ open: false });
          },
        });
      },
    });
  }
}

tinymce.PluginManager.add("galaxy", GalaxyPlugin);
