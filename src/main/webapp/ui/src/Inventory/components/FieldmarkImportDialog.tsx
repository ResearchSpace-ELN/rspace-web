import React from "react";
import { ThemeProvider, styled } from "@mui/material/styles";
import Box from "@mui/material/Box";
import { Dialog } from "../../components/DialogBoundary";
import createAccentedTheme from "../../accentedTheme";
import AppBar from "../../components/AppBar";
import DialogContent from "@mui/material/DialogContent";
import DialogTitle from "@mui/material/DialogTitle";
import Grid from "@mui/material/Grid";
import Link from "@mui/material/Link";
import Typography from "@mui/material/Typography";
import InvApiService from "../../common/InvApiService";
import { doNotAwait } from "../../util/Util";
import { DataGridColumn } from "../../util/table";
import {
  GridToolbarContainer,
  GridToolbarColumnsButton,
} from "@mui/x-data-grid";
import useViewportDimensions from "../../util/useViewportDimensions";
import * as ArrayUtils from "../../util/ArrayUtils";
import * as Parsers from "../../util/parsers";
import Stack from "@mui/material/Stack";
import Button from "@mui/material/Button";
import ValidatingSubmitButton, {
  IsInvalid,
  IsValid,
} from "../../components/ValidatingSubmitButton";
import DialogActions from "@mui/material/DialogActions";
import AlertContext, { mkAlert } from "../../stores/contexts/Alert";
import CircularProgress from "@mui/material/CircularProgress";
import { type LinkableRecord } from "../../stores/definitions/LinkableRecord";
import docLinks from "../../assets/DocLinks";
import { ACCENT_COLOR } from "../../assets/branding/fieldmark";
import { DataGridWithRadioSelection } from "../../components/DataGridWithRadioSelection";

/**
 * This class allows us to provide a link to the newly created container in the
 * success alert toast.
 */
class ResponseContainer implements LinkableRecord {
  id: number | null;
  globalId: string | null;
  name: string;

  constructor({ globalId, name }: { globalId: string; name: string }) {
    this.globalId = globalId;
    this.name = name;
    this.id = 0;
  }

  get recordTypeLabel(): string {
    return "Container";
  }

  get iconName(): string {
    return "container";
  }

  get permalinkURL(): string {
    if (!this.globalId) throw new Error("Impossible");
    return `/globalId/${this.globalId}`;
  }
}

const GridToolbar = ({
  setColumnsMenuAnchorEl,
}: {
  setColumnsMenuAnchorEl: (anchorEl: HTMLElement) => void;
}) => {
  /**
   * The columns menu can be opened by either tapping the "Columns" toolbar
   * button or by tapping the "Manage columns" menu item in each column's menu,
   * logic that is handled my MUI. We provide a custom `anchorEl` so that the
   * menu is positioned beneath the "Columns" toolbar button to be consistent
   * with the other toolbar menus, otherwise is appears far to the left. Rather
   * than having to hook into the logic that triggers the opening of the
   * columns menu in both places, we just set the `anchorEl` pre-emptively.
   */
  const columnMenuRef = React.useRef<HTMLElement | null>(null);
  React.useEffect(() => {
    if (columnMenuRef.current) setColumnsMenuAnchorEl(columnMenuRef.current);
  }, [setColumnsMenuAnchorEl]);

  return (
    <GridToolbarContainer sx={{ width: "100%" }}>
      <Box flexGrow={1}></Box>
      <GridToolbarColumnsButton
        ref={(node) => {
          if (node) columnMenuRef.current = node;
        }}
      />
    </GridToolbarContainer>
  );
};

const StyledGridOverlay = styled("div")(({ theme }) => ({
  display: "flex",
  flexDirection: "column",
  alignItems: "center",
  justifyContent: "center",
  height: "100%",
  backgroundColor: "rgba(18, 18, 18, 0.9)",
  ...theme.applyStyles("light", {
    backgroundColor: "rgba(255, 255, 255, 0.9)",
  }),
}));

function CustomLoadingOverlay() {
  return (
    <StyledGridOverlay>
      <CircularProgress variant="indeterminate" value={1} />
      <Box sx={{ mt: 2 }}>Fetching notebooks from Fieldmark…</Box>
    </StyledGridOverlay>
  );
}

type FieldmarkImportDialogArgs = {
  open: boolean;
  onClose: () => void;
};

type Notebook = {
  name: string;
  metadata: {
    project_id: string;
    ispublic: boolean;
    pre_description: string;
    project_lead: string;
  };
  status: string;
};

/**
 * Fieldmark is a third-party tool for collecting sample data in the field as
 * "records" of a "notebook". Our integration allows these notebooks to be
 * imported as containers, with each record imported as a subsample.
 *
 * This dialog provides a mechanism for choosing a Fieldmark notebook and
 * triggering an import.
 */
export default function FieldmarkImportDialog({
  open,
  onClose,
}: FieldmarkImportDialogArgs): React.ReactNode {
  const { isViewportSmall } = useViewportDimensions();
  const { addAlert } = React.useContext(AlertContext);
  const [notebooks, setNotebooks] =
    React.useState<null | ReadonlyArray<Notebook>>(null);
  const [selectedNotebook, setSelectedNotebook] =
    React.useState<null | Notebook>(null);
  const [columnsMenuAnchorEl, setColumnsMenuAnchorEl] =
    React.useState<HTMLElement | null>(null);
  const [fetchingNotebooks, setFetchingNotebooks] = React.useState(false);
  const [importing, setImporting] = React.useState(false);

  React.useEffect(
    doNotAwait(async () => {
      if (!open) return;
      setFetchingNotebooks(true);
      try {
        const { data } = await InvApiService.get<ReadonlyArray<Notebook>>(
          "/fieldmark/notebooks"
        );
        setNotebooks(data);
      } catch (e) {
        console.error(e);
        if (e instanceof Error) {
          const message = Parsers.objectPath(
            ["response", "data", "data", "validationErrors"],
            e
          )
            .flatMap(Parsers.isArray)
            .flatMap(ArrayUtils.head)
            .flatMap(Parsers.isObject)
            .flatMap(Parsers.isNotNull)
            .flatMap(Parsers.getValueWithKey("message"))
            .flatMap(Parsers.isString)
            .orElse(e.message);
          addAlert(
            mkAlert({
              variant: "error",
              title: "Could not get notebooks from Fieldmark",
              message,
            })
          );
        }
      } finally {
        setFetchingNotebooks(false);
      }
    }),
    [open]
  );

  async function importNotebook(notebook: Notebook) {
    setImporting(true);
    try {
      const { data } = await InvApiService.post<{
        containerName: string;
        containerGlobalId: string;
      }>("/import/fieldmark/notebook", {
        notebookId: notebook.metadata.project_id,
      });
      addAlert(
        mkAlert({
          variant: "success",
          message: "Successfully imported notebook.",
          details: [
            {
              variant: "success",
              title: data.containerName,
              record: new ResponseContainer({
                globalId: data.containerGlobalId,
                name: data.containerName,
              }),
            },
          ],
        })
      );
    } catch (e) {
      console.error(e);
      if (e instanceof Error)
        addAlert(
          mkAlert({
            variant: "error",
            title: "Could not import notebook.",
            message: e.message,
          })
        );
      throw e;
    } finally {
      setImporting(false);
    }
  }

  return (
    <ThemeProvider theme={createAccentedTheme(ACCENT_COLOR)}>
      <Dialog
        open={open}
        onClose={onClose}
        maxWidth="lg"
        fullWidth
        fullScreen={isViewportSmall}
      >
        <AppBar
          variant="dialog"
          currentPage="Fieldmark"
          accessibilityTips={{
            supportsHighContrastMode: true,
          }}
          helpPage={{
            docLink: docLinks.fieldmark,
            title: "Fieldmark help",
          }}
        />
        <DialogTitle variant="h3">Import from Fieldmark</DialogTitle>
        <DialogContent>
          <Grid
            container
            direction="column"
            spacing={2}
            sx={{ height: "100%", flexWrap: "nowrap" }}
          >
            <Grid item>
              <Typography
                variant="body2"
                sx={{ maxWidth: "54em" /* entirely arbitrary */ }}
              >
                Choose a Fieldmark notebook to import into Inventory. A Sample
                will be created for each record inside the notebook. A new list
                container will be placed on your bench, containing a singular
                subsample for each sample.
              </Typography>
              <Typography variant="body2">
                See{" "}
                <Link href="https://docs.fieldmark.au">docs.fieldmark.au</Link>{" "}
                and our{" "}
                <Link href={docLinks.fieldmark}>
                  Fieldmark integration docs
                </Link>{" "}
                for more.
              </Typography>
            </Grid>
            <Grid item>
              <DataGridWithRadioSelection
                columns={[
                  DataGridColumn.newColumnWithFieldName<"name", Notebook>(
                    "name",
                    {
                      headerName: "Name",
                      flex: 1,
                      sortable: false,
                    }
                  ),
                  DataGridColumn.newColumnWithFieldName<"status", Notebook>(
                    "status",
                    {
                      headerName: "Status",
                      flex: 1,
                      sortable: false,
                    }
                  ),
                  DataGridColumn.newColumnWithValueGetter<
                    "isPublic",
                    Notebook,
                    boolean
                  >("isPublic", (notebook) => notebook.metadata.ispublic, {
                    headerName: "Is Public",
                    flex: 1,
                    sortable: false,
                  }),
                  DataGridColumn.newColumnWithValueGetter<
                    "description",
                    Notebook,
                    string
                  >(
                    "description",
                    (notebook) => notebook.metadata.pre_description,
                    {
                      headerName: "Description",
                      flex: 1,
                      sortable: false,
                    }
                  ),
                  DataGridColumn.newColumnWithValueGetter<
                    "projectLead",
                    Notebook,
                    string
                  >(
                    "projectLead",
                    (notebook) => notebook.metadata.project_lead,
                    {
                      headerName: "Project Lead",
                      flex: 1,
                      sortable: false,
                    }
                  ),
                  DataGridColumn.newColumnWithValueGetter<
                    "id",
                    Notebook,
                    string
                  >("id", (notebook) => notebook.metadata.project_id, {
                    headerName: "Id",
                    flex: 1,
                    sortable: false,
                  }),
                ]}
                initialState={{
                  columns: {
                    columnVisibilityModel: {
                      status: !isViewportSmall,
                      isPublic: !isViewportSmall,
                      description: false,
                      projectLead: false,
                      id: false,
                    },
                  },
                }}
                rows={notebooks ?? []}
                selectedRowId={selectedNotebook?.metadata.project_id}
                onSelectionChange={(newSelectionId) => {
                  ArrayUtils.find(
                    (n) => n.metadata.project_id === newSelectionId,
                    notebooks ?? []
                  ).do((newlySelectedNotebook) => {
                    setSelectedNotebook(newlySelectedNotebook);
                  });
                }}
                selectRadioAriaLabelFunc={(row) =>
                  `Select notebook: ${row.name}`
                }
                disableColumnFilter
                hideFooter
                autoHeight
                localeText={{
                  noRowsLabel: "No Notebooks",
                }}
                loading={fetchingNotebooks}
                slots={{
                  toolbar: GridToolbar,
                  loadingOverlay: CustomLoadingOverlay,
                }}
                slotProps={{
                  toolbar: {
                    setColumnsMenuAnchorEl,
                  },
                  panel: {
                    anchorEl: columnsMenuAnchorEl,
                  },
                }}
                getRowId={(row) => row.metadata.project_id}
              />
            </Grid>
          </Grid>
        </DialogContent>
        <DialogActions>
          <Grid container direction="row" spacing={1}>
            <Grid item sx={{ ml: "auto" }}>
              <Stack direction="row" spacing={1}>
                <Button onClick={() => onClose()} disabled={importing}>
                  {selectedNotebook ? "Cancel" : "Close"}
                </Button>
                <ValidatingSubmitButton
                  onClick={() => {
                    if (selectedNotebook)
                      void importNotebook(selectedNotebook).then(() =>
                        onClose()
                      );
                  }}
                  validationResult={
                    !selectedNotebook
                      ? IsInvalid("No Notebook selected.")
                      : IsValid()
                  }
                  loading={importing}
                >
                  Import
                </ValidatingSubmitButton>
              </Stack>
            </Grid>
          </Grid>
        </DialogActions>
      </Dialog>
    </ThemeProvider>
  );
}
