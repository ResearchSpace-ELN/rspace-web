import ErrorBoundary from "../ErrorBoundary";
import Snackbar, { SnackbarCloseReason } from "@mui/material/Snackbar";
import SnackbarContentWrapper from "./SnackbarContentWrapper";
import React, { useContext } from "react";
import { makeStyles } from "tss-react/mui";
import { observer } from "mobx-react-lite";
import useViewportDimensions from "../../util/useViewportDimensions";
import AlertContext, { type Alert } from "../../stores/contexts/Alert";

const useStyles = makeStyles<{ verySmallLayout: boolean }>()(
  (theme, { verySmallLayout }: { verySmallLayout: boolean }) => ({
    toastMessageSnackbar: {
      position: "relative",
      marginBottom: 10,
      maxWidth: verySmallLayout ? "100%" : 500,
      textAlign: "left",
      width: "100%",
      left: 0,
    },
  })
);

type ToastMessageArgs = {
  alert: Alert;
};

function ToastMessage({ alert }: ToastMessageArgs): React.ReactNode {
  const [expanded, setExpanded] = React.useState(false);
  const [timeoutId, setTimeoutId] = React.useState<NodeJS.Timeout | null>(null);
  const { isViewportVerySmall } = useViewportDimensions();
  const { classes } = useStyles({ verySmallLayout: isViewportVerySmall });
  const { removeAlert } = useContext(AlertContext);

  const handleClose = (
    event?: Event | React.SyntheticEvent,
    reason?: SnackbarCloseReason
  ): void => {
    if (reason !== "clickaway") removeAlert(alert);
  };

  React.useEffect(() => {
    if (!alert.isInfinite) {
      setTimeoutId(setTimeout(() => removeAlert(alert), alert.duration));
    }
    /* eslint-disable-next-line react-hooks/exhaustive-deps --
     * - alert will not change because the caller sets the key to the alert id
     */
  }, []);

  const stopTimeout = () => {
    if (timeoutId) {
      clearTimeout(timeoutId);
      setTimeoutId(null);
    }
  };

  return (
    <ErrorBoundary>
      <Snackbar
        data-test-id={`toast-element-${alert.id}`}
        anchorOrigin={{
          vertical: "top",
          horizontal: "right",
        }}
        open={alert.isOpen}
        onClose={handleClose}
        className={classes.toastMessageSnackbar}
        role="group"
        aria-label={`${alert.variant} alert`}
      >
        <SnackbarContentWrapper
          onClose={handleClose}
          alert={alert}
          expanded={expanded}
          setExpanded={setExpanded}
          onInteraction={stopTimeout}
        />
      </Snackbar>
    </ErrorBoundary>
  );
}

/**
 * This components displays a single toast alert.
 */
export default observer(ToastMessage);
