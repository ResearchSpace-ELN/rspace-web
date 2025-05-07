import React, { useState } from "react";
import Dialog from "@mui/material/Dialog";
import DialogTitle from "@mui/material/DialogTitle";
import DialogContent from "@mui/material/DialogContent";
import DialogActions from "@mui/material/DialogActions";
import Button from "@mui/material/Button";
import Typography from "@mui/material/Typography";
import CircularProgress from "@mui/material/CircularProgress";
import AppBar from "../../components/AppBar";
import * as FetchingData from "../../util/fetchingData";
import { useGetDocument } from "../../eln/documents/useGetDocument";
import Alert from "@mui/material/Alert";
import { GridRowId } from "@mui/x-data-grid";
import { DataGridColumn } from "../../util/table";
import { DataGridWithRadioSelection } from "../../components/DataGridWithRadioSelection";

// Type definition for file attachments
type FileAttachment = {
  id: number;
  globalId: string;
  name: string;
};

function useGetFieldAttachments(
  documentId: number,
  fieldId: number
): FetchingData.Fetched<ReadonlyArray<FileAttachment>> {
  const doc = useGetDocument(documentId);
  const attachments = React.useMemo(() => {
    return FetchingData.flatMap(doc, (d) => {
      const field = d.fields.find(({ id }) => id === fieldId);
      if (!field) return { tag: "error", error: "Field not found" };

      // Ensure the field files conform to our FileAttachment type
      // This handles the case where the actual API might return a different structure
      const enhancedFiles = field.files.map((file) => {
        // Assume file has at least id, name, and globalId properties from the API
        const fileAttachment: FileAttachment = {
          id: typeof file.id === "number" ? file.id : 0,
          name: typeof file.name === "string" ? file.name : "Unknown file",
          globalId: typeof file.globalId === "string" ? file.globalId : "",
        };
        return fileAttachment;
      });

      return { tag: "success", value: enhancedFiles };
    });
  }, [doc, fieldId]);

  return attachments;
}

interface GalaxyDialogProps {
  open: boolean;
  onClose: () => void;
  documentId: number;
  fieldId: number;
  editor: any;
}

/**
 * GalaxyDialog component for selecting a file attached to the current document
 * field and executing an RNAseq workflow.
 */
export default function GalaxyDialog({
  open,
  onClose,
  documentId,
  fieldId,
  editor,
}: GalaxyDialogProps) {
  const [selectedRowId, setSelectedRowId] = useState<GridRowId | null>(null);
  const [loading, setLoading] = useState<boolean>(false);
  const files = useGetFieldAttachments(documentId, fieldId);

  const columns = [
    DataGridColumn.newColumnWithFieldName<"name", FileAttachment>("name", {
      headerName: "File Name",
      flex: 2,
    }),
    DataGridColumn.newColumnWithFieldName<"globalId", FileAttachment>(
      "globalId",
      {
        headerName: "Global ID",
        flex: 2,
      }
    ),
  ];

  const handleRunWorkflow = () => {
    // Get the selected file globalId from the selectedRowId
    if (selectedRowId !== null) return;

    try {
      setLoading(true);
      // Get the selected file from the files
      const selectedFile = FetchingData.match(files, {
        loading: () => null,
        error: () => null,
        success: (fileList) =>
          fileList.find((file) => file.id === selectedRowId),
      });

      // Simulating the workflow execution
      setTimeout(() => {
        setLoading(false);
      }, 1000);
    } catch (error) {
      console.error("Error running workflow:", error);
      setLoading(false);
    }
  };

  return (
    <Dialog open={open} onClose={onClose} fullWidth maxWidth="md">
      <AppBar variant="dialog" currentPage="Galaxy" accessibilityTips={{}} />
      <DialogTitle>Execute Galaxy RNAseq Workflow</DialogTitle>
      <DialogContent>
        <Typography variant="body1" paragraph>
          This integration allows you to trigger an RNAseq workflow in Galaxy.
          The workflow will process your selected file and analyze RNA
          sequencing data. Please select a file from the list below to use as
          input for the Galaxy RNAseq workflow.
        </Typography>

        <Typography variant="h6" gutterBottom>
          Available Files
        </Typography>

        {FetchingData.match(files, {
          loading: () => (
            <div
              style={{
                display: "flex",
                justifyContent: "center",
                padding: "2rem",
              }}
            >
              <CircularProgress />
            </div>
          ),
          error: (error) => (
            <div
              style={{
                display: "flex",
                justifyContent: "center",
                padding: "2rem",
              }}
            >
              <Alert severity="error">{error}</Alert>
            </div>
          ),
          success: (filesList) => (
            <div style={{ height: 400, width: "100%" }}>
              <DataGridWithRadioSelection
                rows={filesList}
                columns={columns}
                getRowId={(row) => row.id}
                autoHeight
                disableRowSelectionOnClick={false}
                selectedRowId={selectedRowId}
                onSelectionChange={(newSelection) => {
                  setSelectedRowId(newSelection);
                }}
                hideFooter
                hideFooterSelectedRowCount
                disableColumnFilter
                selectRadioAriaLabelFunc={(row: FileAttachment) =>
                  `Select ${row.name}`
                }
              />
            </div>
          ),
        })}
      </DialogContent>
      <DialogActions>
        <Button onClick={onClose}>Cancel</Button>
        <Button
          disabled={selectedRowId === null || loading}
          color="primary"
          variant="contained"
          onClick={handleRunWorkflow}
        >
          Run Workflow
        </Button>
      </DialogActions>
    </Dialog>
  );
}
