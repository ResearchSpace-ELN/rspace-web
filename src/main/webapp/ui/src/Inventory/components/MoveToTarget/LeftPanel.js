//@flow

import useStores from "../../../stores/use-stores";
import InventoryPicker from "../Picker/Picker";
import Typography from "@mui/material/Typography";
import { observer } from "mobx-react-lite";
import React, { type ComponentType } from "react";
import ContainerModel from "../../../stores/models/ContainerModel";
import HelpDocs from "../../../components/Help/HelpDocs";
import IconButtonWithTooltip from "../../../components/IconButtonWithTooltip";
import HelpIcon from "@mui/icons-material/Help";

type LeftPanelArgs = {||};

function LeftPanel(_: LeftPanelArgs) {
  const { moveStore, uiStore } = useStores();

  const selectionHelpText = () => {
    if (!(moveStore.search?.activeResult instanceof ContainerModel))
      return null;
    if (
      moveStore.search.activeResult.isWorkbench &&
      moveStore.search.fetcher.parentIsBench
    ) {
      if (uiStore.isSingleColumnLayout) {
        return "Press 'Next', followed by 'Move', to move the selected items to this bench.";
      }
      return "Press 'Move' in the bottom-right to move the selected items to this bench.";
    }
    return null;
  };

  return (
    <>
      {moveStore.search ? (
        <InventoryPicker
          search={moveStore.search}
          onAddition={() => {
            /*
             * Do nothing; having passed moveStore.search, its activeResult
             * will automatically be the picked destination
             */
          }}
          header={
            <Typography variant="h6" component="h3">
              Pick Destination&nbsp;
              <HelpDocs
                suggestions={["article:dncoti2i4t"]}
                Action={({ onClick, disabled }) => (
                  <IconButtonWithTooltip
                    color="primary"
                    size="small"
                    onClick={onClick}
                    icon={<HelpIcon />}
                    title="Info on moving items."
                    disabled={disabled}
                  />
                )}
              />
            </Typography>
          }
          selectionHelpText={selectionHelpText()}
          testId="movePicker"
        />
      ) : (
        <Typography variant="h5">Loading</Typography>
      )}
    </>
  );
}

export default (observer(LeftPanel): ComponentType<LeftPanelArgs>);
