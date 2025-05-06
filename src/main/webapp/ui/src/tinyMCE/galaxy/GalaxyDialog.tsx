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

// Mock data for file list
const MOCK_FILES = [
  {
    id: "file1",
    name: "sample_rnaseq_1.fastq",
  },
  {
    id: "file2",
    name: "sample_rnaseq_2.fastq",
  },
  {
    id: "file3",
    name: "human_genome_reads.fastq",
  },
  {
    id: "file4",
    name: "mouse_genome_reads.fastq",
  },
  {
    id: "file5",
    name: "experiment_results.fastq",
  },
];

interface GalaxyDialogProps {
  open: boolean;
  onClose: () => void;
}

/**
 * GalaxyDialog component for selecting a file attached to the current document
 * field and executing an RNAseq workflow.
 */
export default function GalaxyDialog({ open, onClose }: GalaxyDialogProps) {
  const [selectedFile, setSelectedFile] = useState<string | null>(null);
  const [loading, setLoading] = useState<boolean>(false);

  const handleFileSelect = (fileId: string) => {
    setSelectedFile(fileId);
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
        {loading ? (
          <div
            style={{
              display: "flex",
              justifyContent: "center",
              padding: "2rem",
            }}
          >
            <CircularProgress />
          </div>
        ) : (
          <>
            <Typography variant="body1" paragraph>
              This integration allows you to trigger an RNAseq workflow in
              Galaxy. The workflow will process your selected file and analyze
              RNA sequencing data. Please select a file from the list below to
              use as input for the Galaxy RNAseq workflow.
            </Typography>

            <Typography variant="h6" gutterBottom>
              Available Files
            </Typography>

            <List>
              {MOCK_FILES.map((file) => (
                <ListItem key={file.id}>
                  <ListItemButton
                    onClick={() => handleFileSelect(file.id)}
                    selected={selectedFile === file.id}
                  >
                    <ListItemText primary={file.name} />
                  </ListItemButton>
                </ListItem>
              ))}
            </List>
          </>
        )}
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
