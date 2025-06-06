import { test, expect } from "@playwright/experimental-ct-react";
import { Download } from "playwright-core";
import React from "react";
import identifiersJson from "../../__tests__/identifiers.json";
import {
  SimpleIgsnTable,
  SingularSelectionIgsnTable,
  IgsnTableWithControlDefaults,
} from "./IgsnTable.story";
import fs from "fs/promises";
import * as Jwt from "jsonwebtoken";

const feature = test.extend<{
  Given: {
    "the researcher is viewing the IGSN table": () => Promise<void>;
    "the researcher is viewing the IGSN table with singular selection": () => Promise<void>;
    "the researcher is viewing the IGSN table with control defaults": () => Promise<void>;
    "the researcher is viewing the IGSN table with no results": () => Promise<void>;
  };
  Once: {
    "the table has loaded": () => Promise<void>;
  };
  When: {
    "a CSV export is downloaded": () => Promise<Download>;
    "the researcher selects 'Draft' from the state menu": () => Promise<void>;
    "the researcher selects 'No Linked Item' from the Linked Item menu": () => Promise<void>;
    "the researcher selects the IGSN with DOI '10.82316/khma-em96'": () => Promise<void>;
    "the researcher selects {count} IGSNs": ({
      count,
    }: {
      count: number;
    }) => Promise<void>;
    "the researcher types 'test' in the search box": () => Promise<void>;
    "the researcher clicks the Scan QR Code button": () => Promise<void>;
    "a QR code is scanned with value {value}": ({
      value,
    }: {
      value: string;
    }) => Promise<void>;
  };
  Then: {
    "a table should be shown": () => Promise<void>;
    "the default columns should be Select, DOI, State, and Linked Item": () => Promise<void>;
    "there should be four rows": () => Promise<void>;
    "there should be a menu for changing column visibility": () => Promise<void>;
    "there should be a menu for exporting the IGSN table to CSV": () => Promise<void>;
    "a 'No IGSN IDs' message should be displayed": () => Promise<void>;
    "{CSV} should have {count} rows": ({
      csv,
      count,
    }: {
      csv: Download;
      count: number;
    }) => Promise<void>;
    "there should be a network request with state set to 'draft'": () => void;
    "there should be a network request with isAssociated set to 'false'": () => void;
    "the IGSN with DOI '10.82316/khma-em96' is added to the selection state": () => Promise<void>;
    "the Linked Item column should contains links": () => Promise<void>;
    "a search box should be shown in the toolbar": () => Promise<void>;
    "there should be a network request with searchTerm set to 'test'": () => void;
    "the toolbar controls should be in the order: search, scan, then filters": () => Promise<void>;
    "the search box should have the placeholder 'Search IGSN IDs...'": () => Promise<void>;
  };
  networkRequests: Array<URL>;
}>({
  Given: async ({ mount, page }, use) => {
    await use({
      "the researcher is viewing the IGSN table": async () => {
        await mount(<SimpleIgsnTable />);
      },
      "the researcher is viewing the IGSN table with singular selection":
        async () => {
          await mount(<SingularSelectionIgsnTable />);
        },
      "the researcher is viewing the IGSN table with control defaults":
        async () => {
          await mount(<IgsnTableWithControlDefaults />);
        },
      "the researcher is viewing the IGSN table with no results": async () => {
        // Intercept API requests and return an empty array
        await page.route(
          (url) => url.pathname === "/api/inventory/v1/identifiers",
          async (route) => {
            await route.fulfill({
              status: 200,
              contentType: "application/json",
              body: JSON.stringify([]),
            });
          }
        );
        await mount(<SimpleIgsnTable />);
      },
    });
  },
  Once: async ({ page }, use) => {
    await use({
      "the table has loaded": async () => {
        await page.waitForFunction(() => {
          const rows = document.querySelectorAll('[role="row"]').length;
          const noIgsnMessage =
            document.body.textContent?.includes("No IGSN IDs");
          return rows > 1 || noIgsnMessage; // (1 is for the header row) or empty state message
        });
      },
    });
  },
  When: async ({ page }, use) => {
    await use({
      "a CSV export is downloaded": async () => {
        await page.getByRole("button", { name: /Export/ }).click();
        const [download] = await Promise.all([
          page.waitForEvent("download"),
          page
            .getByRole("menuitem", {
              name: /Export to CSV/,
            })
            .click(),
        ]);
        return download;
      },
      "the researcher clicks the Scan QR Code button": async () => {
        await page.getByRole("button", { name: "Scan" }).click();
      },
      "the researcher selects 'Draft' from the state menu": async () => {
        await page.getByRole("button", { name: /State/ }).click();
        await page.getByRole("menuitem", { name: /Draft/ }).click();
      },
      "the researcher selects 'No Linked Item' from the Linked Item menu":
        async () => {
          await page.getByRole("button", { name: /Linked Item/ }).click();
          await page.getByRole("menuitem", { name: /No Linked Item/ }).click();
        },
      "the researcher types 'test' in the search box": async () => {
        await page.getByRole("searchbox").fill("test");
        await page.waitForTimeout(500);
      },
      "a QR code is scanned with value {value}": async ({ value }) => {
        // Since we can't easily mock the camera API in tests,
        // we'll simulate manual entry which is an alternative in the UI
        await page
          .getByRole("textbox", {
            name: "Alternatively, enter the data encoded in the barcode",
          })
          .fill(value);
        await page.getByRole("button", { name: /Search for IGSN/ }).click();
      },
      "the researcher selects the IGSN with DOI '10.82316/khma-em96'":
        async () => {
          const row = page
            .getByRole("row", { name: /10.82316\/khma-em96/ })
            .first();
          const checkbox = row.getByRole("checkbox").first();
          const checkboxCount = await checkbox.count();
          if (checkboxCount > 0) {
            await checkbox.click();
          } else {
            await row.getByRole("radio").first().click();
          }
        },
      "the researcher selects {count} IGSNs": async ({
        count,
      }: {
        count: number;
      }) => {
        for (let i = 0; i < count; i++) {
          await page
            .getByRole("checkbox", { name: /Select row/ })
            .nth(0) // for some reason, only the unchecked ones are found
            .click();
        }
      },
    });
  },
  Then: async ({ page, networkRequests }, use) => {
    await use({
      "a table should be shown": async () => {
        const table = page.getByRole("grid");
        await expect(table).toBeVisible();
      },
      "the default columns should be Select, DOI, State, and Linked Item":
        async () => {
          const headers = await page
            .getByRole("columnheader")
            .allTextContents();
          expect(headers).toEqual(["", "DOI", "State", "Linked Item"]);
          // the empty string at the beginning is the checkbox column
        },
      "there should be four rows": async () => {
        const rows = await page.getByRole("row").count();
        expect(rows).toBe(5); // + 1 for the header row
      },
      "there should be a menu for changing column visibility": async () => {
        const menuButton = page.getByRole("button", { name: "Columns" });
        await menuButton.click();
        const menu = page.getByRole("tooltip");
        await expect(menu).toBeVisible();
      },
      "there should be a menu for exporting the IGSN table to CSV":
        async () => {
          const menuButton = page.getByRole("button", { name: "Export" });
          await menuButton.click();
          const menu = page.getByRole("tooltip");
          await expect(menu).toBeVisible();
        },
      "a search box should be shown in the toolbar": async () => {
        const searchBox = page.getByRole("searchbox");
        await expect(searchBox).toBeVisible();
      },
      "{CSV} should have {count} rows": async ({ csv, count }) => {
        const path = await csv.path();
        const fileContents = await fs.readFile(path, "utf8");
        const lines = fileContents.split("\n");
        expect(lines.length).toBe(count + 1);
      },
      "there should be a network request with state set to 'draft'": () => {
        expect(
          networkRequests
            .find((url) => url.searchParams.has("state"))
            ?.searchParams.get("state")
        ).toBe("draft");
      },
      "there should be a network request with isAssociated set to 'false'":
        () => {
          expect(
            networkRequests
              .find((url) => url.searchParams.has("isAssociated"))
              ?.searchParams.get("isAssociated")
          ).toBe("false");
        },
      "there should be a network request with searchTerm set to 'test'": () => {
        expect(
          networkRequests
            .find(
              (url) =>
                url.searchParams.has("identifier") &&
                url.searchParams.get("identifier") === "test"
            )
            ?.searchParams.get("identifier")
        ).toBe("test");
      },
      "the IGSN with DOI '10.82316/khma-em96' is added to the selection state":
        async () => {
          /*
           * We can't check that setSelectedIdentifiers has actually been called
           * using Playwright, but because IgsnTable.story renders the selection
           * we can check what's been rendered.
           */
          await expect(
            page.getByLabel("selected IGSNs").getByText("10.82316/khma-em96")
          ).toBeVisible();

          const row = page
            .getByRole("row", { name: /10.82316\/khma-em96/ })
            .first();
          let widget = row.getByRole("checkbox").first();
          if ((await widget.count()) === 0) {
            widget = row.getByRole("radio").first();
          }
          await expect(widget).toBeChecked();
        },
      "the Linked Item column should contains links": async () => {
        const table = page.getByRole("grid");

        // Locate the header row
        const headerRow = table.getByRole("row").first();

        // Find the index of the "Linked Item" column
        const headers = headerRow.getByRole("columnheader");
        let linkedItemColumnIndex = -1;
        for (let i = 0; i < (await headers.count()); i++) {
          const headerText = await headers.nth(i).textContent();
          if (headerText?.trim() === "Linked Item") {
            linkedItemColumnIndex = i;
            break;
          }
        }

        // Ensure the "Linked Item" column was found
        expect(linkedItemColumnIndex).not.toBe(-1);

        // Locate all rows within the table body
        const rows = table.getByRole("rowgroup").getByRole("row");

        // Iterate through each row and check if the cell in the "Linked Item" column contains a link
        for (let i = 0; i < (await rows.count()); i++) {
          const cell = rows
            .nth(i)
            .getByRole("gridcell")
            .nth(linkedItemColumnIndex);
          const link = cell.locator("a");

          // Assert that the cell contains a link
          expect(await link.count()).toBeGreaterThan(0);
        }
      },
      "a 'No IGSN IDs' message should be displayed": async () => {
        // Verify the overlay message is displayed
        await expect(
          page.getByRole("grid").getByText("No IGSN IDs")
        ).toBeVisible();

        // Verify the grid has a header row but no data rows
        const headerRow = page
          .getByRole("row")
          .filter({ has: page.getByRole("columnheader") });
        await expect(headerRow).toBeVisible();

        // Count should be 1 (just the header row)
        const rowCount = await page.getByRole("row").count();
        expect(rowCount).toBe(1);
      },
      "the toolbar controls should be in the order: search, scan, then filters":
        async () => {
          const searchControl = page.getByRole("searchbox");
          const scanButton = page.getByRole("button", { name: "Scan" });
          const stateFilter = page.getByRole("button", { name: "State" });
          const linkedItemFilter = page.getByRole("button", {
            name: "Linked Item",
          });

          const searchControlHandle = await searchControl.evaluateHandle((x) =>
            Promise.resolve(x)
          );
          const scanButtonHandle = await scanButton.evaluateHandle((x) =>
            Promise.resolve(x)
          );
          const stateFilterHandle = await stateFilter.evaluateHandle((x) =>
            Promise.resolve(x)
          );
          const linkedItemFilterHandle = await linkedItemFilter.evaluateHandle(
            (x) => Promise.resolve(x)
          );

          await expect(searchControl).toBeVisible();
          await expect(scanButton).toBeVisible();
          await expect(stateFilter).toBeVisible();
          await expect(linkedItemFilter).toBeVisible();

          const orderResults = await page.evaluate(
            ({ search, scan, state, linkedItem }) => {
              if (!search || !scan || !state || !linkedItem) {
                return { error: "Failed to find all elements" };
              }

              const searchBeforeScan = Boolean(
                search.compareDocumentPosition(scan) &
                  Node.DOCUMENT_POSITION_FOLLOWING
              );
              const scanBeforeState = Boolean(
                scan.compareDocumentPosition(state) &
                  Node.DOCUMENT_POSITION_FOLLOWING
              );
              const stateBeforeLinkedItem = Boolean(
                state.compareDocumentPosition(linkedItem) &
                  Node.DOCUMENT_POSITION_FOLLOWING
              );

              return {
                searchBeforeScan,
                scanBeforeState,
                stateBeforeLinkedItem,
              };
            },
            {
              search: searchControlHandle,
              scan: scanButtonHandle,
              state: stateFilterHandle,
              linkedItem: linkedItemFilterHandle,
            }
          );

          // Check for evaluate errors
          if ("error" in orderResults) {
            throw new Error(orderResults.error);
          }

          expect(
            orderResults.searchBeforeScan,
            "Search textfield should be before scan button"
          ).toBe(true);
          expect(
            orderResults.scanBeforeState,
            "Scan button should be before state filter control"
          ).toBe(true);
          expect(
            orderResults.stateBeforeLinkedItem,
            "State filter controlshould be before linked item filter control"
          ).toBe(true);
        },
      "the search box should have the placeholder 'Search IGSN IDs...'":
        async () => {
          const searchBox = page.getByRole("searchbox");
          await expect(searchBox).toHaveAttribute(
            "placeholder",
            "Search IGSN IDs..."
          );
        },
    });
  },
  networkRequests: async ({}, use) => {
    await use([]);
  },
});

feature.beforeEach(async ({ router, page, networkRequests }) => {
  await router.route("/userform/ajax/inventoryOauthToken", (route) => {
    const payload = {
      iss: "http://localhost:8080",
      iat: new Date().getTime(),
      exp: Math.floor(Date.now() / 1000) + 300,
      refreshTokenHash:
        "fe15fa3d5e3d5a47e33e9e34229b1ea2314ad6e6f13fa42addca4f1439582a4d",
    };
    return route.fulfill({
      status: 200,
      contentType: "application/json",
      body: JSON.stringify({
        data: Jwt.sign(payload, "dummySecretKey"),
      }),
    });
  });

  await router.route(
    (url) => url.pathname === "/api/inventory/v1/identifiers",
    (route) => {
      const url = new URL(route.request().url());
      const state = url.searchParams.get("state");
      const isAssociated = url.searchParams.get("isAssociated");
      const searchTerm = url.searchParams.get("searchTerm");

      let filteredIdentifiers = identifiersJson;

      if (state) {
        filteredIdentifiers = filteredIdentifiers.filter(
          (identifier) => identifier.state === state
        );
      }

      if (searchTerm) {
        filteredIdentifiers = filteredIdentifiers.filter((identifier) =>
          identifier.doi.includes(searchTerm)
        );
      }

      if (isAssociated === "true") {
        filteredIdentifiers = filteredIdentifiers.filter(
          (identifier) => identifier.associatedGlobalId !== null
        );
      } else if (isAssociated === "false") {
        filteredIdentifiers = filteredIdentifiers.filter(
          (identifier) => identifier.associatedGlobalId === null
        );
      }

      return route.fulfill({
        status: 200,
        contentType: "application/json",
        body: JSON.stringify(filteredIdentifiers),
      });
    }
  );

  page.on("request", (request) => {
    networkRequests.push(new URL(request.url()));
  });
});

feature.afterEach(({ networkRequests }) => {
  networkRequests.splice(0, networkRequests.length);
});

test.describe("IGSN Table", () => {
  feature(
    "When the researcher is viewing the IGSN table, a table should be shown.",
    async ({ Given, Then }) => {
      await Given["the researcher is viewing the IGSN table"]();
      await Then["a table should be shown"]();
    }
  );

  feature(
    "The default columns should be Select, DOI, State, and Linked Item",
    async ({ Given, Then }) => {
      await Given["the researcher is viewing the IGSN table"]();
      await Then[
        "the default columns should be Select, DOI, State, and Linked Item"
      ]();
    }
  );

  feature(
    "The mocked data displays four rows",
    async ({ Given, Once, Then }) => {
      await Given["the researcher is viewing the IGSN table"]();
      await Once["the table has loaded"]();
      await Then["there should be four rows"]();
    }
  );

  feature(
    "The toolbar should contain a search box",
    async ({ Given, Then }) => {
      await Given["the researcher is viewing the IGSN table"]();
      await Then["a search box should be shown in the toolbar"]();
    }
  );

  feature(
    "The toolbar's search box should have the correct placeholder text",
    async ({ Given, Then }) => {
      await Given["the researcher is viewing the IGSN table"]();
      await Then[
        "the search box should have the placeholder 'Search IGSN IDs...'"
      ]();
      /*
       * International Generic Sample Number (IGSN), confusingly, refers to the
       * organsiation and the IDs themselves are referred to as IGSN IDs.
       */
    }
  );

  feature(
    "Searching makes API call with searchTerm parameter",
    async ({ Given, When, Then }) => {
      await Given["the researcher is viewing the IGSN table"]();
      await When["the researcher types 'test' in the search box"]();
      void Then[
        "there should be a network request with searchTerm set to 'test'"
      ]();
    }
  );

  feature(
    "There should be a menu for changing column visibility",
    async ({ Given, Then }) => {
      await Given["the researcher is viewing the IGSN table"]();
      await Then["there should be a menu for changing column visibility"]();
    }
  );

  feature(
    "There should be a menu for exporting the IGSN table to CSV",
    async ({ Given, Then }) => {
      await Given["the researcher is viewing the IGSN table"]();
      await Then[
        "there should be a menu for exporting the IGSN table to CSV"
      ]();
    }
  );

  feature(
    "When there is no selection, all rows should be included in the export.",
    async ({ Given, When, Then }) => {
      await Given["the researcher is viewing the IGSN table"]();
      // Note that no selection is made
      const csv = await When["a CSV export is downloaded"]();
      await Then["{CSV} should have {count} rows"]({ csv, count: 4 });
    }
  );

  feature(
    "Filtering by state makes API call with state parameter",
    async ({ Given, When, Then }) => {
      await Given["the researcher is viewing the IGSN table"]();
      await When["the researcher selects 'Draft' from the state menu"]();
      void Then[
        "there should be a network request with state set to 'draft'"
      ]();
    }
  );

  feature(
    "Filtering by linked item makes API call with isAssociated parameter",
    async ({ Given, When, Then }) => {
      await Given["the researcher is viewing the IGSN table"]();
      await When[
        "the researcher selects 'No Linked Item' from the Linked Item menu"
      ]();
      void Then[
        "there should be a network request with isAssociated set to 'false'"
      ]();
    }
  );

  feature(
    "When a researcher selects an identifier, the selection state is updated",
    async ({ Given, Once, When, Then }) => {
      await Given["the researcher is viewing the IGSN table"]();
      await Once["the table has loaded"]();
      await When[
        "the researcher selects the IGSN with DOI '10.82316/khma-em96'"
      ]();
      await Then[
        "the IGSN with DOI '10.82316/khma-em96' is added to the selection state"
      ]();
    }
  );

  feature(
    "When a researcher selects an identifier in singluar selection mode, the selection state is updated",
    async ({ Given, Once, When, Then }) => {
      await Given[
        "the researcher is viewing the IGSN table with singular selection"
      ]();
      await Once["the table has loaded"]();
      await When[
        "the researcher selects the IGSN with DOI '10.82316/khma-em96'"
      ]();
      await Then[
        "the IGSN with DOI '10.82316/khma-em96' is added to the selection state"
      ]();
    }
  );

  feature(
    "Control defaults are applied to the table when provided",
    async ({ Given, Once, Then }) => {
      await Given[
        "the researcher is viewing the IGSN table with control defaults"
      ]();
      await Once["the table has loaded"]();
      Then["there should be a network request with state set to 'draft'"]();
      Then[
        "there should be a network request with isAssociated set to 'false'"
      ]();
      Then["there should be a network request with searchTerm set to 'test'"]();
    }
  );

  feature(
    "When some IGSNs are selected, CSV exports should include just those rows",
    async ({ Given, Once, When, Then }) => {
      await Given["the researcher is viewing the IGSN table"]();
      await Once["the table has loaded"]();
      await When["the researcher selects {count} IGSNs"]({ count: 2 });
      const csv = await When["a CSV export is downloaded"]();
      await Then["{CSV} should have {count} rows"]({ csv, count: 2 });
    }
  );

  feature(
    "The Linked Item column should contain links to the Inventory record",
    async ({ Given, Once, Then }) => {
      await Given["the researcher is viewing the IGSN table"]();
      await Once["the table has loaded"]();
      await Then["the Linked Item column should contains links"]();
    }
  );

  feature(
    "When there are no results, a 'No IGSN IDs' message is displayed",
    async ({ Given, Once, Then }) => {
      await Given["the researcher is viewing the IGSN table with no results"]();
      await Once["the table has loaded"]();
      await Then["a 'No IGSN IDs' message should be displayed"]();
    }
  );

  feature(
    "Scanning a QR code updates the search term",
    async ({ Given, When, Then }) => {
      await Given["the researcher is viewing the IGSN table"]();
      await When["the researcher clicks the Scan QR Code button"]();
      await When["a QR code is scanned with value {value}"]({
        value: "test",
      });
      Then["there should be a network request with searchTerm set to 'test'"]();
    }
  );

  feature(
    "The toolbar controls should be in the order: search, scan, then filters",
    async ({ Given, Then }) => {
      await Given["the researcher is viewing the IGSN table"]();
      await Then[
        "the toolbar controls should be in the order: search, scan, then filters"
      ]();
      /*
       * This is so that the controls are in a consistent order across the whole
       * product.
       */
    }
  );
});
