// @flow

import React, { type Node } from "react";
import IgsnManagementPage from "./IgsnManagementPage";
import RsSet from "../../../util/set";
import { type Identifier } from "../../useIdentifiers";

/**
 * This is the page where researchers can manage their IGSNs.
 */
export default function IGSN(): Node {
  const [selectedIgsns, setSelectedIgsns] = React.useState(
    new RsSet<Identifier>()
  );
  return (
    <IgsnManagementPage
      selectedIgsns={selectedIgsns}
      setSelectedIgsns={setSelectedIgsns}
    />
  );
}
