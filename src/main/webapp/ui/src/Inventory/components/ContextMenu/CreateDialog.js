//@flow

import React, { type Node, type ComponentType } from "react";
import {
  type CreateFrom,
  type InventoryRecord,
} from "../../../stores/definitions/InventoryRecord";
import docLinks from "../../../assets/DocLinks";
import HelpLinkIcon from "../../../components/HelpLinkIcon";
import Dialog from "@mui/material/Dialog";
import DialogActions from "@mui/material/DialogActions";
import DialogContent from "@mui/material/DialogContent";
import DialogTitle from "@mui/material/DialogTitle";
import Stepper from "@mui/material/Stepper";
import Step from "@mui/material/Step";
import StepLabel from "@mui/material/StepLabel";
import StepContent from "@mui/material/StepContent";
import FormControl from "@mui/material/FormControl";
import FormLabel from "@mui/material/FormLabel";
import FormControlLabel from "@mui/material/FormControlLabel";
import RadioGroup from "@mui/material/RadioGroup";
import Radio from "@mui/material/Radio";
import Button from "@mui/material/Button";
import Box from "@mui/material/Box";
import {
  OptionHeading,
  OptionExplanation,
} from "../../../components/Inputs/RadioField";
import NumberField from "../../../components/Inputs/NumberField";
import InputAdornment from "@mui/material/InputAdornment";
import { observer } from "mobx-react-lite";
import { runInAction } from "mobx";
import SubmitSpinner from "../../../components/SubmitSpinnerButton";
import NoValue from "../../../components/NoValue";
import StringField from "../../../components/Inputs/StringField";
import { type Id } from "../../../stores/definitions/BaseRecord";
import Grid from "@mui/material/Grid";
import Table from "@mui/material/Table";
import TableBody from "@mui/material/TableBody";
import TableCell from "@mui/material/TableCell";
import TableContainer from "@mui/material/TableContainer";
import TableHead from "@mui/material/TableHead";
import TableRow from "@mui/material/TableRow";
import Checkbox from "@mui/material/Checkbox";
import Stack from "@mui/material/Stack";
import Typography from "@mui/material/Typography";
import SearchView from "../../Search/SearchView";
import SearchContext from "../../../stores/contexts/Search";
import AlwaysNewWindowNavigationContext from "../../../components/AlwaysNewWindowNavigationContext";
import AlwaysNewFactory from "../../../stores/models/Factory/AlwaysNewFactory";
import {
  type AllowedSearchModules,
  type AllowedTypeFilters,
} from "../../../stores/definitions/Search";
import {
  type Container,
  cTypeToDefaultSearchView,
} from "../../../stores/definitions/Container";
import { menuIDs } from "../../../util/menuIDs";
import Search from "../../../stores/models/Search";

type CreateDialogProps = {|
  existingRecord: CreateFrom & InventoryRecord,
  open: boolean,
  onClose: () => void,
|};

const Name: ComponentType<{|
  state: { value: string },
|}> = observer(({ state }): Node => {
  return (
    <StringField
      value={state.value}
      onChange={({ target }) => {
        runInAction(() => {
          state.value = target.value;
        });
      }}
      variant="outlined"
    />
  );
});

const Fields: ComponentType<{|
  state: {
    copyFieldContent: $ReadOnlyArray<{|
      id: Id,
      name: string,
      content: string,
      hasContent: boolean,
      selected: boolean,
    |}>,
  },
|}> = observer(({ state }): Node => {
  if (state.copyFieldContent.length === 0)
    return <NoValue label="No fields." />;
  return (
    <TableContainer>
      <Table size="small">
        <TableHead>
          <TableRow>
            <TableCell variant="head" padding="checkbox">
              <Checkbox
                indeterminate={
                  state.copyFieldContent.some(({ selected }) => selected) &&
                  !state.copyFieldContent.every(({ selected }) => selected)
                }
                checked={state.copyFieldContent.every(
                  ({ selected }) => selected
                )}
                onChange={({ target: { checked } }) => {
                  runInAction(() => {
                    state.copyFieldContent.forEach((f) => {
                      f.selected = checked;
                    });
                  });
                }}
              />
            </TableCell>
            <TableCell width="70%">Field</TableCell>
            <TableCell width="30%">Default Value</TableCell>
          </TableRow>
        </TableHead>
        <TableBody>
          {state.copyFieldContent.map((f) => (
            <TableRow key={f.id}>
              <TableCell variant="head" padding="checkbox">
                <Checkbox
                  checked={f.selected}
                  onChange={({ target: { checked } }) => {
                    runInAction(() => {
                      f.selected = checked;
                    });
                  }}
                  color="default"
                  disabled={!f.hasContent}
                />
              </TableCell>
              <TableCell>{f.name}</TableCell>
              <TableCell
                style={{
                  opacity: f.selected ? 1.0 : 0.3,
                }}
              >
                {f.content}
              </TableCell>
            </TableRow>
          ))}
        </TableBody>
      </Table>
    </TableContainer>
  );
});

const SplitCount: ComponentType<{|
  state: { copies: number, ... },
|}> = observer(({ state }): Node => {
  const MIN = 2;
  const MAX = 100;

  return (
    <Box>
      <FormControl>
        <NumberField
          name="copies"
          autoFocus
          value={state.copies}
          onChange={({ target }) => {
            runInAction(() => {
              state.copies = parseInt(target.value, 10);
            });
          }}
          variant="outlined"
          size="small"
          InputProps={{
            startAdornment: (
              <InputAdornment position="start">Copies</InputAdornment>
            ),
          }}
          inputProps={{
            min: MIN,
            max: MAX,
            step: 1,
          }}
        />
      </FormControl>
    </Box>
  );
});

const LocationPicker: ComponentType<{|
  state: { container: Container },
|}> = observer(({ state }): Node => {
  const [search] = React.useState<Search>(
    new Search({
      fetcherParams: {
        parentGlobalId: state.container.globalId,
      },
      uiConfig: {
        allowedSearchModules: (new Set([]): AllowedSearchModules),
        allowedTypeFilters: (new Set([]): AllowedTypeFilters),
        hideContentsOfChip: true,
      },
      factory: new AlwaysNewFactory(),
    })
  );

  // is there really no way to do this without a useEffect?
  React.useEffect(() => {
    void search.setSearchView(cTypeToDefaultSearchView(state.container.cType));
  }, [state.container]);

  return (
    <SearchContext.Provider
      value={{
        search,
        scopedResult: state.container,
        differentSearchForSettingActiveResult: search,
      }}
    >
      <AlwaysNewWindowNavigationContext>
        <SearchView contextMenuId={menuIDs.NONE} />
      </AlwaysNewWindowNavigationContext>
    </SearchContext.Provider>
  );
});

function CreateDialog({
  existingRecord,
  open,
  onClose,
}: CreateDialogProps): Node {
  const [selectedCreateOptionIndex, setSelectedCreateOptionIndex] =
    React.useState<null | number>(null);
  const [activeStep, setActiveStep] = React.useState<number>(0);

  React.useEffect(() => {
    if (activeStep === 0) setSelectedCreateOptionIndex(null);
  }, [activeStep]);

  const handleSubmit = () => {
    void (async () => {
      if (!selectedCreateOptionIndex)
        throw new Error("Cannot submit until an option is chosen");
      await existingRecord.createOptions[selectedCreateOptionIndex].onSubmit();
      onClose();
    })();
  };

  return (
    <Dialog open={open} onClose={onClose} maxWidth="sm" fullWidth>
      <DialogTitle>
        Create new items
        <HelpLinkIcon
          link={docLinks.createDialog}
          title="Info on creating new items."
        />
      </DialogTitle>
      <form
        onSubmit={(e) => {
          e.preventDefault();
          handleSubmit();
        }}
      >
        <DialogContent>
          <Stepper activeStep={activeStep} orientation="vertical">
            <Step>
              <StepLabel
                optional={
                  selectedCreateOptionIndex !== null && (
                    <Button
                      onClick={() => {
                        setSelectedCreateOptionIndex(null);
                        setActiveStep(0);
                      }}
                    >
                      Change
                    </Button>
                  )
                }
              >
                {selectedCreateOptionIndex
                  ? existingRecord.createOptions[selectedCreateOptionIndex]
                      .label
                  : "Create What?"}
              </StepLabel>
              <StepContent>
                <FormControl>
                  <FormLabel sx={{ fontWeight: 400 }}>
                    What kind of record would you like to create from{" "}
                    <strong>{existingRecord.name}</strong>?
                  </FormLabel>
                  <RadioGroup
                    value={selectedCreateOptionIndex}
                    onChange={(_event, index) => {
                      setSelectedCreateOptionIndex(index);
                      setActiveStep(1);
                    }}
                  >
                    {existingRecord.createOptions.length === 0 && (
                      <NoValue label="No options available." />
                    )}
                    {existingRecord.createOptions.map(
                      ({ label, explanation, disabled }, index) => (
                        <FormControlLabel
                          key={index}
                          value={index}
                          control={<Radio />}
                          disabled={disabled}
                          label={
                            <>
                              <OptionHeading>{label}</OptionHeading>
                              <OptionExplanation>
                                {explanation}
                              </OptionExplanation>
                            </>
                          }
                          sx={{ mt: 2 }}
                        />
                      )
                    )}
                  </RadioGroup>
                </FormControl>
              </StepContent>
            </Step>
            {selectedCreateOptionIndex !== null &&
              existingRecord.createOptions[selectedCreateOptionIndex]
                .parameters &&
              existingRecord.createOptions[
                selectedCreateOptionIndex
              ].parameters.map(
                ({ label, explanation, state, validState }, index) => (
                  <Step key={index}>
                    <StepLabel>
                      {label}
                      <Typography variant="body2">{explanation}</Typography>
                    </StepLabel>
                    <StepContent>
                      <Grid container direction="column" spacing={1}>
                        <Grid item>
                          {state.key === "split" && (
                            <SplitCount state={state} />
                          )}
                          {state.key === "name" && <Name state={state} />}
                          {state.key === "location" && (
                            <LocationPicker state={state} />
                          )}
                          {state.key === "fields" && <Fields state={state} />}
                        </Grid>
                        <Grid item>
                          <Stack spacing={1} direction="row">
                            {index <
                              (
                                existingRecord.createOptions[
                                  selectedCreateOptionIndex
                                ].parameters ?? []
                              ).length -
                                1 && (
                              <Button
                                color="primary"
                                variant="contained"
                                disableElevation
                                onClick={() => {
                                  setActiveStep(activeStep + 1);
                                }}
                                disabled={!validState()}
                              >
                                Next
                              </Button>
                            )}
                            <Button
                              variant="outlined"
                              onClick={() => {
                                setActiveStep(activeStep - 1);
                              }}
                            >
                              Back
                            </Button>
                          </Stack>
                        </Grid>
                      </Grid>
                    </StepContent>
                  </Step>
                )
              )}
          </Stepper>
        </DialogContent>
        <DialogActions>
          <Button onClick={onClose}>Cancel</Button>
          <SubmitSpinner
            label="Create"
            onClick={handleSubmit}
            disabled={
              !selectedCreateOptionIndex ||
              activeStep <
                (
                  existingRecord.createOptions[selectedCreateOptionIndex]
                    .parameters ?? []
                ).length ||
              (
                existingRecord.createOptions[selectedCreateOptionIndex]
                  .parameters ?? []
              ).some(({ validState }) => !validState())
            }
            loading={false}
          />
        </DialogActions>
      </form>
    </Dialog>
  );
}

export default (observer(CreateDialog): typeof CreateDialog);