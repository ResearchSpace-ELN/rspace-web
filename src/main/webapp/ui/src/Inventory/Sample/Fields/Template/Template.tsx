import React, { useState } from "react";
import { observer } from "mobx-react-lite";
import useStores from "../../../../stores/use-stores";
import InputWrapper from "../../../../components/Inputs/InputWrapper";
import SummaryInfo from "../../../Template/SummaryInfo";
import TemplateModel from "../../../../stores/models/TemplateModel";
import { mkAlert } from "../../../../stores/contexts/Alert";
import SampleModel from "../../../../stores/models/SampleModel";
import docLinks from "../../../../assets/DocLinks";
import Divider from "@mui/material/Divider";
import Box from "@mui/material/Box";
import VersionInfo from "../../../Template/Fields/VersionInfo";
import TemplatePicker from "../../../components/Picker/TemplatePicker";
import ExpandCollapseIcon from "../../../../components/ExpandCollapseIcon";
import Grid from "@mui/material/Grid";
import IconButton from "@mui/material/IconButton";
import Collapse from "@mui/material/Collapse";
import Alert from "@mui/material/Alert";
import { getErrorMessage } from "../../../../util/error";
import FormControlLabel from "@mui/material/FormControlLabel";
import Radio from "@mui/material/Radio";
import { Stack } from "@mui/material";

function Template(): React.ReactNode {
  const {
    searchStore: { activeResult },
    uiStore,
  } = useStores();
  if (!activeResult || !(activeResult instanceof SampleModel))
    throw new Error("ActiveResult must be a Sample");

  const [open, setOpen] = useState(true);

  const setTemplate = React.useCallback(
    (t: TemplateModel | null) => {
      activeResult.setTemplate(t).catch((error) => {
        uiStore.addAlert(
          mkAlert({
            title: "Could not fetch template details.",
            message: getErrorMessage(error, "Unknown reason."),
            variant: "error",
          }),
        );
        console.error("Could not set template", error);
      });
    },
    [activeResult, uiStore],
  );

  const template = activeResult.template;
  if (!(template === null || template instanceof TemplateModel))
    throw new Error("Template is not a TemplateModel");
  if (!activeResult.id)
    return (
      <>
        <InputWrapper
          label="Sample Template"
          dataTestId="ChooseTemplate"
          explanation={
            activeResult.isFieldEditable("template") ? (
              <>
                If you select a sample template below, initial metadata and
                custom fields will be automatically generated.
                <a
                  href={docLinks.createTemplate}
                  target="_blank"
                  rel="noreferrer"
                >
                  (Learn more about sample templates)
                </a>
              </>
            ) : null
          }
        >
          <Stack flexWrap="nowrap">
            <FormControlLabel
              value="no-template"
              control={<Radio checked={template === null} />}
              label="No template"
              onClick={() => {
                setTemplate(null);
              }}
              sx={{ mb: 2, mt: 1 }}
            />
            {activeResult.isFieldEditable("template") && (
              <TemplatePicker
                disabled={!activeResult.isFieldEditable("template")}
                setTemplate={setTemplate}
                sample={activeResult}
              />
            )}
          </Stack>
        </InputWrapper>
      </>
    );
  return (
    <>
      <InputWrapper
        label="Sample Template"
        dataTestId="ChooseTemplate"
        explanation={
          activeResult.isFieldEditable("template") ? (
            <>
              See the documentation for information on{" "}
              <a
                href={docLinks.createTemplate}
                target="_blank"
                rel="noreferrer"
              >
                how to create custom templates
              </a>
              .
            </>
          ) : null
        }
      >
        <>
          <Grid
            container
            direction="row"
            alignItems="center"
            justifyContent="space-between"
          >
            <Grid item style={{ flexGrow: 1 }}>
              <SummaryInfo
                template={template}
                loading={
                  activeResult.templateId !== null &&
                  typeof activeResult.templateId !== "undefined" &&
                  !activeResult.template
                }
                paddingless={!activeResult.isFieldEditable("template")}
              />
            </Grid>
            {activeResult.isFieldEditable("template") && (
              <Grid item>
                <IconButton
                  onClick={() => {
                    setOpen(!open);
                  }}
                  size="small"
                >
                  <ExpandCollapseIcon open={open} />
                </IconButton>
              </Grid>
            )}
          </Grid>
          {template && (
            <VersionInfo
              template={template}
              onUpdate={() => {
                void activeResult.updateToLatestTemplate();
              }}
              disabled={activeResult.deleted || activeResult.editing}
            />
          )}
        </>
      </InputWrapper>
      {activeResult.isFieldEditable("template") && (
        <>
          {!activeResult.template && (
            <Alert severity="info" role="status">
              {open
                ? "Select a template from the list below."
                : "Expand to select a template."}
            </Alert>
          )}
          <Collapse in={open} component="div" collapsedSize={0}>
            <Box mb={1}>
              <Divider />
            </Box>
            <TemplatePicker
              disabled={!activeResult.isFieldEditable("template")}
              setTemplate={setTemplate}
              sample={activeResult}
            />
          </Collapse>
        </>
      )}
    </>
  );
}

export default observer(Template);
