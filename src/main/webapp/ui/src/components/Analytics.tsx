import React, { useEffect, useRef, useContext } from "react";
import axios from "@/common/axios";
import AnalyticsContext from "../stores/contexts/Analytics";
import { runInAction } from "mobx";
import { usePostHog, PostHogProvider } from "posthog-js/react";
import posthog from "posthog-js";

declare global {
  interface Window {
    analytics: {
      initialize: unknown;
      invoked: boolean;
      methods: Array<string>;
      factory: (
        method: unknown
      ) => (...args: unknown[]) => typeof window.analytics;
      push: (...args: unknown[]) => void;
      load: (
        key: string,
        options?: typeof window.analytics._loadOptions
      ) => void;
      identify: (userId: string) => void;
      track: (event: string, properties?: Record<string, unknown>) => void;
      page: () => void;
      _loadOptions: unknown;
      _writeKey: string;
      SNIPPET_VERSION: string;
    };
  }
}

const ONE_MINUTE_IN_MS = 60 * 60 * 1000;

/*
 * This component sets up an analytics context. Any code that wishes to report
 * analytics events to intercom should wrap the react components in this
 * component and then use the `trackEvent` function exposed by the analytics
 * context.
 */

function loadIntercom({
  analyticsServerKey,
  analyticsUserId,
}: {
  analyticsServerKey: string;
  analyticsUserId: string;
}): Promise<void> {
  return new Promise((resolve, reject) => {
    try {
      // Segment loads Intercom, hide the intercom icon as we use Lighthouse (HelpDocs.js)
      // @ts-expect-error we're not passing the app_id here
      window.intercomSettings = {
        hide_default_launcher: true,
        // place intercom in same position as lighthouse
        horizontal_padding: 120,
      };

      // Segment tracking
      // The following code is copied from https://segment.com/docs/connections/sources/catalog/libraries/website/javascript/quickstart/
      (function () {
        // Create a queue, but don't obliterate an existing one!
        var analytics = (window.analytics = window.analytics || []); //eslint-disable-line
        // If the real analytics.js is already on the page return.
        if (analytics.initialize) return;
        // If the snippet was invoked already show an error.
        if (analytics.invoked) {
          if (window.console && console.error) {
            console.error("Segment snippet included twice.");
          }
          return;
        }
        // Invoked flag, to make sure the snippet
        // is never invoked twice.
        analytics.invoked = true;
        // A list of the methods in Analytics.js to stub.
        analytics.methods = [
          "trackSubmit",
          "trackClick",
          "trackLink",
          "trackForm",
          "pageview",
          "identify",
          "reset",
          "group",
          "track",
          "ready",
          "alias",
          "debug",
          "page",
          "once",
          "off",
          "on",
          "addSourceMiddleware",
          "addIntegrationMiddleware",
          "setAnonymousId",
          "addDestinationMiddleware",
        ];
        // Define a factory to create stubs. These are placeholders
        // for methods in Analytics.js so that you never have to wait
        // for it to load to actually record data. The `method` is
        // stored as the first argument, so we can replay the data.
        analytics.factory = function (method) {
          return function (...args) {
            analytics.push([method, ...args]);
            return analytics;
          };
        };
        // For each of our methods, generate a queueing stub.
        for (let i = 0; i < analytics.methods.length; i++) {
          const key = analytics.methods[i];
          // @ts-expect-error the type of analytics[key] and analytics.factory(key) may not unify
          analytics[key] = analytics.factory(key);
        }
        // Define a method to load Analytics.js from our CDN,
        // and that will be sure to only ever load it once.
        analytics.load = function (key, options) {
          //eslint-disable-line
          // Create an async script element based on your key.
          const script = document.createElement("script");
          script.type = "text/javascript";
          script.async = true;
          script.src =
            "https://cdn.segment.com/analytics.js/v1/" +
            key +
            "/analytics.min.js";
          // Insert our script next to the first script element.
          const first = document.getElementsByTagName("script")[0];
          first.parentNode?.insertBefore(script, first);
          analytics._loadOptions = options;
        };
        analytics._writeKey = analyticsServerKey;
        // Add a version to keep track of what's in the wild.
        analytics.SNIPPET_VERSION = "4.15.2";
        // Load Analytics.js with your key, which will automatically
        // load the tools you've enabled for your account. Boosh!
        analytics.load(analyticsServerKey);

        analytics.identify(analyticsUserId);
        analytics.page();
      })();

      resolve();
    } catch (e) {
      console.warn("Could not load Segment", e);
      reject();
    }
  });
}

function PostHogAnalyticsContext() {
  const posthog = usePostHog();
  const analyticsContext = useContext(AnalyticsContext);

  useEffect(() => {
    runInAction(() => {
      analyticsContext.trackEvent = (name, properties) => {
        posthog.capture(name, properties);
      };
    });
  }, []);

  return <></>;
}

type AnalyticsArgs = {
  children: React.ReactNode;
};

export default function Analytics({
  children,
}: AnalyticsArgs): React.ReactNode {
  const [postHogEnabled, setPostHogEnable] = React.useState(false);
  const analyticsContext = useContext(AnalyticsContext);
  const api = useRef(
    axios.create({
      baseURL: "/session/ajax",
      timeout: ONE_MINUTE_IN_MS,
    })
  );

  useEffect(() => {
    void (async () => {
      const {
        data: {
          analyticsEnabled,
          analyticsServerType,
          analyticsServerHost,
          analyticsServerKey,
          analyticsUserId,
        },
      } = await api.current.get<{
        analyticsEnabled: boolean;
        analyticsServerType: "posthog" | "segment";
        analyticsServerHost: string;
        analyticsServerKey: string;
        analyticsUserId: string;
      }>("/analyticsProperties");
      if (analyticsEnabled) {
        if (analyticsServerType === "posthog") {
          setPostHogEnable(true);
          posthog.init(analyticsServerKey, {
            api_host: analyticsServerHost,
          });
          runInAction(() => {
            analyticsContext.isAvailable = true;
          });
        } else {
          void loadIntercom({ analyticsServerKey, analyticsUserId })
            .then(() => {
              runInAction(() => {
                analyticsContext.isAvailable = true;
                analyticsContext.trackEvent = (name, properties) => {
                  window.analytics.track(name, properties);
                };
              });
            })
            .catch(() => {
              runInAction(() => {
                analyticsContext.isAvailable = false;
              });
            });
        }
      } else {
        runInAction(() => {
          analyticsContext.isAvailable = false;
        });
      }
    })();
  }, []);

  return (
    <AnalyticsContext.Provider value={analyticsContext}>
      {postHogEnabled && (
        <PostHogProvider client={posthog}>
          <PostHogAnalyticsContext />
        </PostHogProvider>
      )}
      {children}
    </AnalyticsContext.Provider>
  );
}
