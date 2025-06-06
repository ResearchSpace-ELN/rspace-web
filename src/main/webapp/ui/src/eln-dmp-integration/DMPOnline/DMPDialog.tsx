import React from "react";
import { Dialog, DialogBoundary } from "../../components/DialogBoundary";
import Portal from "@mui/material/Portal";
import useViewportDimensions from "../../util/useViewportDimensions";
import { withStyles } from "Styles";
import DialogContent from "@mui/material/DialogContent";
import DialogActions from "@mui/material/DialogActions";
import DialogTitle from "@mui/material/DialogTitle";
import Button from "@mui/material/Button";
import Grid from "@mui/material/Grid";
import Typography from "@mui/material/Typography";
import {
  GridToolbarContainer,
  GridToolbarColumnsButton,
  GridRowId,
} from "@mui/x-data-grid";
import { makeStyles } from "tss-react/mui";
import { ThemeProvider } from "@mui/material/styles";
import Link from "@mui/material/Link";
import * as FetchingData from "../../util/fetchingData";
import {
  useDmpOnlineEndpoint,
  type DmpListing,
  type DmpSummary,
} from "./useDmpOnlineEndpoint";
import docLinks from "../../assets/DocLinks";
import NoValue from "../../components/NoValue";
import { doNotAwait } from "../../util/Util";
import createAccentedTheme from "../../accentedTheme";
import AppBar from "../../components/AppBar";
import ValidatingSubmitButton, {
  IsValid,
  IsInvalid,
} from "../../components/ValidatingSubmitButton";
import { DataGridColumn } from "../../util/table";
import { ACCENT_COLOR } from "../../assets/branding/dmponline";
import { DataGridWithRadioSelection } from "../../components/DataGridWithRadioSelection";

const useStyles = makeStyles<{ listing: FetchingData.Fetched<unknown> }>()(
  (theme, props: { listing: FetchingData.Fetched<unknown> }) => ({
    table: {
      display:
        typeof props === "undefined"
          ? "none"
          : FetchingData.match(props.listing, {
              loading: () => "none",
              error: () => "none",
              success: () => "flex",
            }),
    },
  })
);

const CustomDialog = withStyles<
  { fullScreen: boolean } & React.ComponentProps<typeof Dialog>,
  { paper?: string }
>((theme, { fullScreen }) => ({
  paper: {
    overflow: "hidden",
    margin: fullScreen ? 0 : theme.spacing(2.625),
    maxHeight: "unset",
    minHeight: "unset",

    // this is so that the heights of the dialog's content are constrained and scrollbars appear
    // 24px margin above and below, 3px border above and below
    height: fullScreen ? "100%" : "calc(100% - 48px)",
  },
}))(Dialog);

const DMPDialogContent = ({
  setOpen,
}: {
  setOpen: (open: boolean) => void;
}) => {
  const [selection, setSelection] = React.useState<DmpSummary | null>(null);
  const [importing, setImporting] = React.useState(false);

  const { firstPage } = useDmpOnlineEndpoint();
  const [listing, setListing] =
    React.useState<FetchingData.Fetched<DmpListing>>(firstPage);
  const { classes } = useStyles({ listing });

  React.useEffect(() => {
    setListing(firstPage);
  }, [firstPage]);

  const onSubmit = async (e: React.MouseEvent<HTMLButtonElement>) => {
    e.preventDefault();
    if (!selection) return;
    setImporting(true);
    try {
      await selection.importIntoGallery();
    } finally {
      setImporting(false);
    }
  };

  const columns = [
    DataGridColumn.newColumnWithFieldName<"title", DmpSummary>("title", {
      headerName: "Title",
      hideable: false,
    }),
    {
      field: "contact name",
      headerName: "Contact Name",
      renderCell: (params: { row: DmpSummary }) =>
        params.row.contactName.orElse(<NoValue label="Not Specified" />),
    },
    {
      field: "contact affiliation",
      headerName: "Contact Affiliation",
      renderCell: (params: { row: DmpSummary }) =>
        params.row.contactAffiliationName.orElse(
          <NoValue label="Not Specified" />
        ),
    },
    DataGridColumn.newColumnWithFieldName<"created", DmpSummary>("created", {
      headerName: "Created",
    }),
    DataGridColumn.newColumnWithFieldName<"modified", DmpSummary>("modified", {
      headerName: "Modified",
    }),
  ].map((colDefinition) => ({
    sortable: false,
    flex: 1,
    ...colDefinition,
  }));

  return (
    <>
      <AppBar
        variant="dialog"
        currentPage="DMPonline"
        accessibilityTips={{
          supportsHighContrastMode: true,
        }}
        helpPage={{
          docLink: docLinks.dmponline,
          title: "DMPonline help",
        }}
      />
      <DialogTitle variant="h3">Import a DMP into the Gallery</DialogTitle>
      <DialogContent>
        <Grid
          container
          direction="column"
          spacing={2}
          flexWrap="nowrap"
          /*
           * The height of 100% ensures that the table is scrollable
           * The extra 16px prevents excessive whitespace, more and we get double scrollbars
           */
          height="calc(100% + 16px)"
        >
          <Grid item>
            <Typography variant="body2">
              Importing a DMP from <strong>dmponline.dcc.ac.uk</strong> will
              make it available to view and reference within RSpace.
            </Typography>
            <Typography variant="body2">
              See{" "}
              <Link href="https://dmponline.dcc.ac.uk">
                dmponline.dcc.ac.uk
              </Link>{" "}
              and our{" "}
              <Link href={docLinks.dmponline}>DMPonline integration docs</Link>{" "}
              for more.
            </Typography>
          </Grid>
          <Grid item sx={{ overflowY: "auto" }} flexGrow={1}>
            {FetchingData.match(listing, {
              loading: () => (
                <Typography variant="body2">
                  Loading listing of DMPs.
                </Typography>
              ),
              error: (error) => (
                <>
                  <Typography variant="body2">
                    Failed to load listing of DMPs. Please try refreshing.
                  </Typography>
                  <samp>{error}</samp>
                </>
              ),
              success: () => <></>,
            })}
            <DataGridWithRadioSelection
              rows={FetchingData.match(listing, {
                loading: () => [] as Array<DmpSummary>,
                error: () => [] as Array<DmpSummary>,
                success: (l) => l.dmps,
              })}
              columns={columns}
              selectedRowId={selection?.id}
              onSelectionChange={(newSelectionId: GridRowId) => {
                FetchingData.match(listing, {
                  loading: () => {},
                  error: () => {},
                  success: (l) => {
                    setSelection(l.getById(String(newSelectionId)));
                  },
                });
              }}
              selectRadioAriaLabelFunc={(row) => `Select ${row.title}`}
              initialState={{
                columns: {
                  columnVisibilityModel: {
                    created: false,
                    modified: false,
                  },
                },
              }}
              disableColumnFilter
              density="compact"
              getRowId={(row) => row.id}
              hideFooterSelectedRowCount
              paginationMode="server"
              rowCount={FetchingData.match(listing, {
                loading: () => 0,
                error: () => 0,
                success: (l) => l.totalCount,
              })}
              paginationModel={FetchingData.match(listing, {
                loading: () => ({ page: 0, pageSize: 0 }),
                error: () => ({ page: 0, pageSize: 0 }),
                success: (l) => ({ page: l.page, pageSize: l.pageSize }),
              })}
              pageSizeOptions={[10, 25, 100]}
              onPaginationModelChange={({
                pageSize: newPageSize,
                page: newPage,
              }) => {
                FetchingData.match(listing, {
                  loading: () => {},
                  error: () => {},
                  success: doNotAwait(async (l) => {
                    try {
                      if (newPage !== l.page) {
                        setListing({ tag: "loading" });
                        setListing({
                          tag: "success",
                          value: await l.setPage(newPage),
                        });
                      }
                    } catch (error) {
                      if (error instanceof Error)
                        setListing({ tag: "error", error: error.message });
                    }
                    try {
                      if (newPageSize !== l.pageSize) {
                        setListing({ tag: "loading" });
                        setListing({
                          tag: "success",
                          value: await l.setPageSize(newPageSize),
                        });
                      }
                    } catch (error) {
                      if (error instanceof Error)
                        setListing({ tag: "error", error: error.message });
                    }
                  }),
                });
              }}
              slots={{
                toolbar: () => (
                  <GridToolbarContainer>
                    <GridToolbarColumnsButton />
                  </GridToolbarContainer>
                ),
              }}
              className={classes.table}
              getRowHeight={() => "auto"}
              {...FetchingData.match(listing, {
                loading: () => ({ "aria-hidden": true }),
                error: () => ({ "aria-hidden": true }),
                success: () => ({}),
              })}
            />
          </Grid>
        </Grid>
      </DialogContent>
      <DialogActions>
        <Button onClick={() => setOpen(false)}>Close</Button>
        <ValidatingSubmitButton
          validationResult={
            selection ? IsValid() : IsInvalid("No DMP is selected.")
          }
          loading={importing}
          onClick={(e) => void onSubmit(e)}
        >
          Import
        </ValidatingSubmitButton>
      </DialogActions>
    </>
  );
};

type DMPDialogArgs = {
  open: boolean;
  setOpen: (open: boolean) => void;
};

/**
 * This simple function just for the outer-most components is so that the
 * content of the dialog can use the Alerts context
 *
 * A11y: note that tabbing through this dialog is not possible because the
 * custom tabbing behaviour of the Gallery page takes control of the tab key
 * events away from the React+MUI tech stack. See ../../../../scripts/global.js
 */
export default function DMPDialog({
  open,
  setOpen,
}: DMPDialogArgs): React.ReactNode {
  const { isViewportSmall } = useViewportDimensions();

  /*
   * We use DialogBoundary to wrap the Dialog so that Alerts can be shown atop the dialog whilst
   * keeping them accessible to screen readers. We then have to manually add Portal back (Dialogs
   * normally include a Portal) so that the Dialog isn't rendered inside the Menu where it will
   * not be seen once the Menu is closed.
   */

  return (
    <ThemeProvider theme={createAccentedTheme(ACCENT_COLOR)}>
      <Portal>
        <DialogBoundary>
          <CustomDialog
            onClose={() => {
              setOpen(false);
            }}
            open={open}
            maxWidth="lg"
            fullWidth
            fullScreen={isViewportSmall}
          >
            <DMPDialogContent setOpen={setOpen} />
          </CustomDialog>
        </DialogBoundary>
      </Portal>
    </ThemeProvider>
  );
}
