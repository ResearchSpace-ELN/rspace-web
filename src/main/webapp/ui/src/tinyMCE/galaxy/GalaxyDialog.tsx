import React, { useState } from "react";
import Dialog from "@mui/material/Dialog";
import DialogTitle from "@mui/material/DialogTitle";
import DialogContent from "@mui/material/DialogContent";
import DialogActions from "@mui/material/DialogActions";
import Button from "@mui/material/Button";
import Typography from "@mui/material/Typography";
import List from "@mui/material/List";
import ListItem from "@mui/material/ListItem";
import ListItemButton from "@mui/material/ListItemButton";
import ListItemText from "@mui/material/ListItemText";
import CircularProgress from "@mui/material/CircularProgress";
import AppBar from "../../components/AppBar";
import * as FetchingData from "../../util/fetchingData";
import { useGetDocument } from "../../eln/documents/useGetDocument";
import Alert from "@mui/material/Alert";

function useGetFieldAttachments(
  documentId: number,
  fieldId: number
): FetchingData.Fetched<
  ReadonlyArray<{
    id: number;
    globalId: string;
    name: string;
  }>
> {
  const doc = useGetDocument(documentId);
  const attachments = React.useMemo(() => {
    return FetchingData.flatMap(doc, (d) => {
      const field = d.fields.find(({ id }) => id === fieldId);
      if (!field) return { tag: "error", error: "Field not found" };
      return { tag: "success", value: field.files };
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
  const [selectedFile, setSelectedFile] = useState<string | null>(null);
  const [loading, setLoading] = useState<boolean>(false);
  const files = useGetFieldAttachments(documentId, fieldId);

  const handleFileSelect = (fileGlobalId: string) => {
    setSelectedFile(fileGlobalId);
  };

  const handleRunWorkflow = () => {
    if (!selectedFile) return;
    try {
      setLoading(true);
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
          success: (files) => (
            <List>
              {files.map((file) => (
                <ListItem key={file.id}>
                  <ListItemButton
                    onClick={() => handleFileSelect(file.globalId)}
                    selected={selectedFile === file.globalId}
                  >
                    <ListItemText primary={file.name} />
                  </ListItemButton>
                </ListItem>
              ))}
            </List>
          ),
        })}
      </DialogContent>
      <DialogActions>
        <Button onClick={onClose}>Cancel</Button>
        <Button
          disabled={!selectedFile || loading}
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
