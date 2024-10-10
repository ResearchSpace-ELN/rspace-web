//@flow strict

import Grid from "@mui/material/Grid";
import React, { type Node, type AbstractComponent } from "react";
import IntegrationCard from "../IntegrationCard";
import { type IntegrationStates } from "../useIntegrationsEndpoint";
import Button from "@mui/material/Button";
import FieldmarktIcon from "../icons/fieldmark.svg";
import { useFieldmarkEndpoint } from "../useFieldmarkEndpoint";
import AlertContext, { mkAlert } from "../../../stores/contexts/Alert";

type FieldmarkArgs = {|
  integrationState: IntegrationStates["FIELDMARK"],
  update: (IntegrationStates["FIELDMARK"]) => void,
|};

export const COLOR = {
  hue: 82,
  saturation: 80,
  lightness: 33,
};

/*
 * Fieldmark uses OAuth based authentication, as implemeted by the form below.
 */
function Fieldmark({ integrationState, update }: FieldmarkArgs): Node {
  const { addAlert } = React.useContext(AlertContext);
  const { disconnect } = useFieldmarkEndpoint();
  const [connected, setConnected] = React.useState(
    integrationState.credentials.FIELDMARK_USER_TOKEN.isPresent()
  );

  React.useEffect(() => {
    const f = () => {
      setConnected(true);
      addAlert(
        mkAlert({
          variant: "success",
          message: "Successfully connected to Fieldmark.",
        })
      );
    };
    window.addEventListener("FIELDMARK_CONNECTED", f);
    return () => {
      window.removeEventListener("FIELDMARK_CONNECTED", f);
    };
  }, []);

  return (
    <Grid item sm={6} xs={12} sx={{ display: "flex" }}>
      <IntegrationCard
        name="Fieldmark"
        integrationState={integrationState}
        explanatoryText="Collect structured, geospatial, instrument and multimedia data into notebooks for easy importing into Inventory."
        image={FieldmarktIcon}
        color={COLOR}
        update={(newMode) =>
          update({ mode: newMode, credentials: integrationState.credentials })
        }
        helpLinkText="Fieldmark integration docs"
        website="fieldnote.au/fieldmark"
        docLink="fieldmark"
        usageText="You can import you notebooks of data into Inventory."
        setupSection={
          <>
            <ol>
              <li>
                Click on Connect to authorise RSpace to access your Fieldmark
                account.
              </li>
              <li>Enable the integration.</li>
              <li>Use the import button in Inventory to choose a notebook.</li>
            </ol>
            {connected ? (
              <form
                onSubmit={(e) => {
                  e.preventDefault();
                  void (async () => {
                    await disconnect();
                    setConnected(false);
                  })();
                }}
              >
                <Button type="submit" sx={{ mt: 1 }}>
                  Disconnect
                </Button>
              </form>
            ) : (
              <form
                action="/apps/fieldmark/connect"
                method="POST"
                target="_blank"
                rel="opener"
              >
                <Button type="submit" sx={{ mt: 1 }} value="Connect">
                  Connect
                </Button>
              </form>
            )}
          </>
        }
      />
    </Grid>
  );
}

export default (React.memo(Fieldmark): AbstractComponent<FieldmarkArgs>);
