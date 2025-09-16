/*
 * @jest-environment jsdom
 */
/* eslint-env jest */
import React from "react";
import { render, screen, act } from "@testing-library/react";
import "@testing-library/jest-dom";
import userEvent from "@testing-library/user-event";
import RsSet from "../../../util/set";
import { useGalleryActions, rootDestination } from "../useGalleryActions";
import { dummyId, Description, LocalGalleryFile } from "../useGalleryListing";
import Alerts from "../../../components/Alerts/Alerts";
import MockAdapter from "axios-mock-adapter";
import axios from "@/common/axios";

beforeEach(() => {
  jest.clearAllMocks();
});

afterEach(cleanup);

describe("useGalleryActions", () => {
  describe("duplicateFiles", () => {
    function TestingDuplicateComponent() {
      const { duplicateFiles } = useGalleryActions();
      return (
        <button
          onClick={() => {
            void duplicateFiles(
              new RsSet([
                new LocalGalleryFile({
                  id: dummyId(),
                  globalId: "GF1",
                  name: "Foo",
                  extension: "txt",
                  creationDate: new Date(),
                  modificationDate: new Date(),
                  type: "image",
                  ownerName: "Joe Bloggs",
                  description: new Description({ key: "empty" }),
                  version: 1,
                  size: 1024,
                  path: [],
                  thumbnailId: null,
                  gallerySection: "Images",
                  originalImageId: null,
                  metadata: {},
                  token: "",
                }),
              ]),
            );
          }}
        >
          click me
        </button>
      );
    }

    test("errorMessages should result in an error alert", async () => {
      const user = userEvent.setup();
      const mockAxios = new MockAdapter(axios);

      mockAxios.onPost("/gallery/ajax/copyGalleries").reply(200, {
        data: null,
        error: {
          errorMessages: ["This is a test error message."],
        },
        success: false,
        errorMsg: {
          errorMessages: ["This is a test error message."],
        },
      });

      render(
        <Alerts>
          <TestingDuplicateComponent />
        </Alerts>,
      );

      await act(async () => {
        await user.click(screen.getByRole("button"));
      });

      const toast = await screen.findByRole("alert");

      expect(toast).toHaveTextContent("This is a test error message.");
    });
    test("exceptionMessage should result in an error alert", async () => {
      const user = userEvent.setup();
      const mockAxios = new MockAdapter(axios);

      mockAxios.onPost("/gallery/ajax/copyGalleries").reply(200, {
        exceptionMessage: "This is a test error message.",
        tstamp: "2024-12-10T15:12:57.591Z",
        errorId: "dvnaxNHumB",
      });

      render(
        <Alerts>
          <TestingDuplicateComponent />
        </Alerts>,
      );

      await act(async () => {
        await user.click(screen.getByRole("button"));
      });

      const toast = await screen.findByRole("alert");

      expect(toast).toHaveTextContent("This is a test error message.");
    });
  });
  describe("deleteFiles", () => {
    function TestingDeleteComponent() {
      const { deleteFiles } = useGalleryActions();
      return (
        <button
          onClick={() => {
            void deleteFiles(
              new RsSet([
                new LocalGalleryFile({
                  id: dummyId(),
                  globalId: "GF1",
                  name: "Foo",
                  extension: "txt",
                  creationDate: new Date(),
                  modificationDate: new Date(),
                  type: "image",
                  ownerName: "Joe Bloggs",
                  description: new Description({ key: "empty" }),
                  version: 1,
                  size: 1024,
                  path: [],
                  thumbnailId: null,
                  gallerySection: "Images",
                  originalImageId: null,
                  metadata: {},
                  token: "",
                }),
              ]),
            );
          }}
        >
          click me
        </button>
      );
    }

    test("errorMessages should result in an error alert", async () => {
      const user = userEvent.setup();
      const mockAxios = new MockAdapter(axios);

      mockAxios.onPost("/gallery/ajax/deleteElementFromGallery").reply(200, {
        data: null,
        error: {
          errorMessages: ["This is a test error message."],
        },
        success: false,
        errorMsg: {
          errorMessages: ["This is a test error message."],
        },
      });

      render(
        <Alerts>
          <TestingDeleteComponent />
        </Alerts>,
      );

      await act(async () => {
        await user.click(screen.getByRole("button"));
      });

      const toast = await screen.findByRole("alert");

      expect(toast).toHaveTextContent("This is a test error message.");
    });
    test("exceptionMessage should result in an error alert", async () => {
      const user = userEvent.setup();
      const mockAxios = new MockAdapter(axios);

      mockAxios.onPost("/gallery/ajax/deleteElementFromGallery").reply(200, {
        exceptionMessage: "This is a test error message.",
        tstamp: "2024-12-10T15:12:57.591Z",
        errorId: "dvnaxNHumB",
      });

      render(
        <Alerts>
          <TestingDeleteComponent />
        </Alerts>,
      );

      await act(async () => {
        await user.click(screen.getByRole("button"));
      });

      const toast = await screen.findByRole("alert");

      expect(toast).toHaveTextContent("This is a test error message.");
    });
  });
  describe("moveFiles", () => {
    function TestingMoveComponent() {
      const { moveFiles } = useGalleryActions();
      return (
        <button
          onClick={() => {
            void moveFiles(
              "Images",
              rootDestination(),
              new RsSet([
                new LocalGalleryFile({
                  id: dummyId(),
                  globalId: "GF1",
                  name: "Foo",
                  extension: "txt",
                  creationDate: new Date(),
                  modificationDate: new Date(),
                  type: "image",
                  ownerName: "Joe Bloggs",
                  description: new Description({ key: "empty" }),
                  version: 1,
                  size: 1024,
                  path: [],
                  thumbnailId: null,
                  gallerySection: "Images",
                  originalImageId: null,
                  metadata: {},
                  token: "",
                }),
              ]),
            );
          }}
        >
          click me
        </button>
      );
    }

    test("errorMessages should result in an error alert", async () => {
      const user = userEvent.setup();
      const mockAxios = new MockAdapter(axios);

      mockAxios.onPost("/gallery/ajax/moveGalleriesElements").reply(200, {
        data: null,
        error: {
          errorMessages: ["This is a test error message."],
        },
        success: false,
        errorMsg: {
          errorMessages: ["This is a test error message."],
        },
      });

      render(
        <Alerts>
          <TestingMoveComponent />
        </Alerts>,
      );

      await act(async () => {
        await user.click(screen.getByRole("button"));
      });

      const toast = await screen.findByRole("alert");

      expect(toast).toHaveTextContent("This is a test error message.");
    });
    test("exceptionMessage should result in an error alert", async () => {
      const user = userEvent.setup();
      const mockAxios = new MockAdapter(axios);

      mockAxios.onPost("/gallery/ajax/moveGalleriesElements").reply(200, {
        exceptionMessage: "This is a test error message.",
        tstamp: "2024-12-10T15:12:57.591Z",
        errorId: "dvnaxNHumB",
      });

      render(
        <Alerts>
          <TestingMoveComponent />
        </Alerts>,
      );

      await act(async () => {
        await user.click(screen.getByRole("button"));
      });

      const toast = await screen.findByRole("alert");

      expect(toast).toHaveTextContent("This is a test error message.");
    });
  });
  describe("changeDescription", () => {
    function TestingChangeDescriptionComponent({
      shouldFail = false,
    }: {
      shouldFail?: boolean;
    }) {
      const { changeDescription } = useGalleryActions();
      return (
        <button
          onClick={() => {
            void changeDescription(
              new LocalGalleryFile({
                id: dummyId(),
                globalId: "GF1",
                name: "Foo",
                extension: "txt",
                creationDate: new Date(),
                modificationDate: new Date(),
                type: "image",
                ownerName: "Joe Bloggs",
                description: new Description({
                  key: "present",
                  value: "current description",
                }),
                version: 1,
                size: 1024,
                path: [],
                thumbnailId: null,
                gallerySection: "Images",
                originalImageId: null,
                metadata: {},
                token: "",
              }),
              new Description({
                key: "present",
                value: shouldFail
                  ? "new description that is too long".repeat(10)
                  : "new valid description",
              }),
            ).catch(() => {
              // Expect error to be thrown and caught here
            });
          }}
        >
          click me
        </button>
      );
    }

    test("successful description update should result in success alert", async () => {
      const user = userEvent.setup();
      const mockAxios = new MockAdapter(axios);

      mockAxios.onPost("description").reply(200, {
        success: true,
      });

      render(
        <Alerts>
          <TestingChangeDescriptionComponent shouldFail={false} />
        </Alerts>,
      );

      await act(async () => {
        await user.click(screen.getByRole("button"));
      });

      const toast = await screen.findByRole("alert");

      expect(toast).toHaveTextContent("Successfully updated description.");
    });

    test("exceptionMessage error response format should result in error toast", async () => {
      const user = userEvent.setup();
      const mockAxios = new MockAdapter(axios);

      mockAxios.onPost("description").reply(200, {
        exceptionMessage:
          "Something went wrong: \ndescription too long, should be max 250 chars\n\n",
        tstamp: "2025-09-15T14:17:48.775+01:00",
        errorId: "XWBuZQsJik",
      });

      render(
        <Alerts>
          <TestingChangeDescriptionComponent shouldFail={true} />
        </Alerts>,
      );

      await act(async () => {
        await user.click(screen.getByRole("button"));
        // Wait a bit for the async error handling to complete
        await new Promise((resolve) => setTimeout(resolve, 100));
      });

      const toast = await screen.findByRole("alert");

      expect(toast).toHaveTextContent("Failed to update description.");
      expect(toast).toHaveTextContent(
        "Something went wrong: description too long, should be max 250 chars",
      );
    });
  });
});
