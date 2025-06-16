import {
  test,
  expect,
  type MountResult,
} from "@playwright/experimental-ct-react";
import React from "react";
import { SimplePageWithAppBar } from "./index.story";

const feature = test.extend<{
  Given: {
    "the app bar is being shown": (
      props?: React.ComponentProps<typeof SimplePageWithAppBar>
    ) => Promise<void>;
  };
  Once: {};
  When: {};
  Then: {
    "there will be a visually hidden heading with {title} should be shown": (props: {
      title: string;
    }) => Promise<void>;
    "the icon menu buttons should be in the order: account, then help": () => Promise<void>;
    "the icon menu buttons should be in the order: accessibility tips, then help": () => Promise<void>;
  };
  networkRequests: Array<URL>;
}>({
  Given: async ({ mount }, use) => {
    await use({
      "the app bar is being shown": async (props) => {
        const mountResult: MountResult = await mount(
          <SimplePageWithAppBar {...props} />
        );
        expect(mountResult).toBeDefined();
      },
    });
  },
  Once: async ({ page }, use) => {
    await use({});
  },
  When: async ({ page }, use) => {
    await use({});
  },
  Then: async ({ page }, use) => {
    await use({
      "there will be a visually hidden heading with {title} should be shown":
        async ({ title }) => {
          const heading = page.locator("h1");
          await expect(heading).toHaveText(title);
        },
      "the icon menu buttons should be in the order: account, then help":
        async () => {
          const accountMenuButton = page.getByRole("button", {
            name: "Account Menu",
          });
          await expect(accountMenuButton).toBeVisible();
          const accountMenuButtonHandle =
            await accountMenuButton.evaluateHandle((x) => Promise.resolve(x));

          const helpMenuButton = page.getByRole("button", {
            name: "Open Help",
          });
          await expect(helpMenuButton).toBeVisible();
          const helpMenuButtonHandle = await helpMenuButton.evaluateHandle(
            (x) => Promise.resolve(x)
          );

          const orderResults = await page.evaluate(
            ({ accountButton, helpButton }) => {
              if (!accountButton || !helpButton) {
                return { error: "Failed to find all elements" };
              }
              const accountBeforeHelp = Boolean(
                accountButton.compareDocumentPosition(helpButton) &
                  Node.DOCUMENT_POSITION_FOLLOWING
              );
              return {
                accountBeforeHelp,
              };
            },
            {
              accountButton: accountMenuButtonHandle,
              helpButton: helpMenuButtonHandle,
            }
          );

          if ("error" in orderResults) {
            throw new Error(orderResults.error);
          }

          expect(
            orderResults.accountBeforeHelp,
            "Account button should be before Help button"
          ).toBe(true);
        },
      "the icon menu buttons should be in the order: accessibility tips, then help":
        async () => {
          const accessibilityTipsButton = page.getByRole("button", {
            name: "Accessibility Tips",
          });
          await expect(accessibilityTipsButton).toBeVisible();
          const accessibilityTipsButtonHandle =
            await accessibilityTipsButton.evaluateHandle((x) =>
              Promise.resolve(x)
            );

          const helpMenuButton = page.getByRole("button", {
            name: "Open Help",
          });
          await expect(helpMenuButton).toBeVisible();
          const helpMenuButtonHandle = await helpMenuButton.evaluateHandle(
            (x) => Promise.resolve(x)
          );

          const orderResults = await page.evaluate(
            ({ accessibilityTipsButtonDomNode, helpButtonDomNode }) => {
              if (!accessibilityTipsButtonDomNode || !helpButtonDomNode) {
                return { error: "Failed to find all elements" };
              }
              const accessibilityTipsBeforeHelp = Boolean(
                accessibilityTipsButtonDomNode.compareDocumentPosition(
                  helpButtonDomNode
                ) & Node.DOCUMENT_POSITION_FOLLOWING
              );
              return {
                accessibilityTipsBeforeHelp,
              };
            },
            {
              accessibilityTipsButtonDomNode: accessibilityTipsButtonHandle,
              helpButtonDomNode: helpMenuButtonHandle,
            }
          );

          if ("error" in orderResults) {
            throw new Error(orderResults.error);
          }

          expect(
            orderResults.accessibilityTipsBeforeHelp,
            "Accessibility Tips button should be before Help button"
          ).toBe(true);
        },
    });
  },
  networkRequests: async ({}, use) => {
    await use([]);
  },
});

feature.beforeEach(async ({ router }) => {
  await router.route("/userform/ajax/inventoryOauthToken", async (route) => {
    await route.fulfill({
      status: 200,
      contentType: "application/json",
      body: JSON.stringify({
        data: "eyJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJodHRwOi8vbG9jYWxob3N0OjgwODAiLCJpYXQiOjE3NDI5MTc1NTgsImV4cCI6MTc0MjkyMTE1OCwicmVmcmVzaFRva2VuSGFzaCI6ImE3ZGZkYjVkMjhiMmRkYWRmYWJhYzhkOTRlM2ZlZWE2Y2QxM2I3M2EyMWVmYTRmNTJmMDVkOTI4MTBmMTc5YTQifQ.JsqUiGczASw4Pr9pEsc3aYFqgymMGVvHNCsyL6oudjY",
      }),
    });
  });
  await router.route("/session/ajax/livechatProperties", async (route) => {
    await route.fulfill({
      status: 200,
      contentType: "application/json",
      body: JSON.stringify({ livechatEnabled: false }),
    });
  });
  await router.route("/api/v1/userDetails/uiNavigationData", async (route) => {
    await route.fulfill({
      status: 200,
      contentType: "application/json",
      body: JSON.stringify({
        bannerImgSrc: "/public/banner",
        visibleTabs: {
          inventory: true,
          myLabGroups: true,
          published: false,
          system: false,
        },
        userDetails: {
          username: "user1a",
          fullName: "user user",
          email: "user@user.com",
          orcidId: null,
          orcidAvailable: false,
          profileImgSrc: null,
          lastSession: "2025-03-25T15:45:57.000Z",
        },
        operatedAs: false,
        nextMaintenance: null,
      }),
    });
  });
  await router.route("/public/banner", async (route) => {
    await route.fulfill({
      status: 200,
      contentType: "image/png",
      body: Buffer.from("fake image data"),
    });
  });
});

feature.afterEach(({}) => {});

test.describe("App Bar", () => {
  feature("Heading for screen readers", async ({ Given, Then }) => {
    await Given["the app bar is being shown"]({
      variant: "page",
      currentPage: "Test Page",
    });
    await Then[
      "there will be a visually hidden heading with {title} should be shown"
    ]({
      title: "Test Page",
    });
    /*
     * The app bar should have a visually hidden heading that is accessible to
     * screen readers when we don't show the heading to all users.
     */
  });

  feature(
    "On page variant, the icons on the right should be in the correct order",
    async ({ Given, Then }) => {
      await Given["the app bar is being shown"]({
        variant: "page",
        accessibilityTips: { supportsHighContrastMode: true },
      });
      await Then[
        "the icon menu buttons should be in the order: account, then help"
      ]();
      /*
       * This is so that across the different variants the help button remains
       * in a consistent location -- the furthest right -- as having help be in
       * a consistent location across the entire product is an a11y requirement.
       */
    }
  );

  feature(
    "On dialog variant, the icons on the right should be in the correct order",
    async ({ Given, Then }) => {
      await Given["the app bar is being shown"]({
        variant: "dialog",
        accessibilityTips: { supportsHighContrastMode: true },
      });
      await Then[
        "the icon menu buttons should be in the order: accessibility tips, then help"
      ]();
      /*
       * This is so that across the different variants the help button remains
       * in a consistent location -- the furthest right -- as having help be in
       * a consistent location across the entire product is an a11y requirement.
       */
    }
  );
});
