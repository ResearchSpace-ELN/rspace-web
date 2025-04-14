import React from "react";
import { DataGrid } from "@mui/x-data-grid";
import { type Identifier, useIdentifiersListing } from "../../useIdentifiers";
import Checkbox from "@mui/material/Checkbox";
import RsSet from "../../../util/set";
import { DataGridColumn } from "../../../util/table";
import { toTitleCase } from "../../../util/Util";
import GlobalId from "../../../components/GlobalId";
import LinkableRecordFromGlobalId from "../../../stores/models/LinkableRecordFromGlobalId";

/**
 * A table listing all of the IGSNs that the current users owns.
 */
export default function IgsnTable({
  selectedIgsns,
  setSelectedIgsns,
}: {
  selectedIgsns: RsSet<Identifier>;
  setSelectedIgsns: (newSet: RsSet<Identifier>) => void;
}): React.ReactNode {
  const [state] = React.useState<"draft" | "findable" | "registered" | null>(
    null
  );
  const [isAssociated] = React.useState<boolean | null>(null);
  const { identifiers, loading } = useIdentifiersListing({
    state,
    isAssociated,
  });

  return (
    <DataGrid
      rows={identifiers}
      columns={[
        {
          field: "checkbox",
          headerName: "Select",
          renderCell: (params: { row: Identifier }) => (
            <Checkbox
              color="primary"
              value={selectedIgsns.hasWithEq(
                params.row,
                (a, b) => a.doi === b.doi
              )}
              checked={selectedIgsns.hasWithEq(
                params.row,
                (a, b) => a.doi === b.doi
              )}
              inputProps={{ "aria-label": "IGSN ID selection" }}
              onChange={(e) => {
                if (e.target.checked) {
                  setSelectedIgsns(
                    selectedIgsns.unionWithEq(
                      new RsSet([params.row]),
                      (a, b) => a.doi === b.doi
                    )
                  );
                } else {
                  setSelectedIgsns(
                    selectedIgsns.subtractWithEq(
                      new RsSet([params.row]),
                      (a, b) => a.doi === b.doi
                    )
                  );
                }
              }}
              sx={{ p: 0.5 }}
            />
          ),
          hideable: false,
          width: 70,
          flex: 0,
          disableColumnMenu: true,
          sortable: false,
          disableExport: true,
        },
        DataGridColumn.newColumnWithFieldName<"doi", Identifier>("doi", {
          headerName: "DOI",
          flex: 1,
          sortable: false,
          resizable: true,
        }),
        DataGridColumn.newColumnWithFieldName<"state", Identifier>("state", {
          headerName: "State",
          flex: 1,
          resizable: true,
          sortable: false,
          renderCell: ({ row }) => toTitleCase(row.state),
        }),
        DataGridColumn.newColumnWithFieldName<"associatedGlobalId", Identifier>(
          "associatedGlobalId",
          {
            headerName: "Linked Item",
            flex: 1,
            resizable: true,
            sortable: false,
            renderCell: ({ row }) => {
              if (row.associatedGlobalId === null) {
                return "None";
              }
              return (
                <GlobalId
                  record={
                    new LinkableRecordFromGlobalId(row.associatedGlobalId)
                  }
                  onClick={() => {}}
                />
              );
            },
          }
        ),
      ]}
      loading={loading}
      getRowId={(row) => row.doi}
      initialState={{
        columns: {},
      }}
      density="compact"
      disableColumnFilter
      hideFooter
      autoHeight
      slots={{
        pagination: null,
      }}
      localeText={{
        noRowsLabel: "No IGSN IDs",
      }}
    />
  );
}
