// @flow

import React, { type Node, useEffect, useContext } from "react";
import RecordsImport from "./RecordsImport";
import { Navigate } from "react-router-dom";
import NavigateContext from "../../stores/contexts/Navigate";
import NavigationContext from "./NavigationContext";
import useStores from "../../stores/use-stores";

export default function ImportRouter(): Node {
  const { importStore } = useStores();
  const { useLocation } = useContext(NavigateContext);
  const location = useLocation();

  /*
   * Whenever the URL path changes, such as because the user tapped a link to
   * navigate around the Import UI, we want to update the state stored by
   * importStore.importData
   */
  useEffect(() => {
    importStore.importData?.updateRecordType(
      new URLSearchParams(location.search)
    );
  }, [location.search]);

  const recordType = importStore.importData?.recordType;
  if (
    recordType === "SAMPLES" ||
    recordType === "CONTAINERS" ||
    recordType === "SUBSAMPLES"
  ) {
    return (
      <NavigationContext>
        <RecordsImport />
      </NavigationContext>
    );
  }
  return <Navigate to="/inventory/pageNotFound" replace={true} />;
}
