/*
 * @jest-environment jsdom
 */
/* eslint-env jest */
import React from "react";
import { render, cleanup, screen, fireEvent } from "@testing-library/react";
import "@testing-library/jest-dom";
import SubmitSpinnerButton from "../SubmitSpinnerButton";
import { calculateProgress } from "../../util/progress";
import { ThemeProvider } from "@mui/material/styles";
import materialTheme from "../../theme";

beforeEach(() => {
  jest.clearAllMocks();
});

afterEach(cleanup);

describe("SubmitSpinnerButton", () => {
  test("When the button is tapped, onClick should be called.", () => {
    const onClick = jest.fn();

    render(
      <ThemeProvider theme={materialTheme}>
        <SubmitSpinnerButton
          onClick={onClick}
          disabled={false}
          loading={false}
          label="foo"
        />
      </ThemeProvider>
    );

    fireEvent.click(screen.getByRole("button", { name: "foo" }));

    expect(onClick).toHaveBeenCalled();
  });

  test("When the button is disabled and tapped, onClick should not be called.", () => {
    const onClick = jest.fn();

    render(
      <ThemeProvider theme={materialTheme}>
        <SubmitSpinnerButton
          onClick={onClick}
          disabled={true}
          loading={false}
          label="foo"
        />
      </ThemeProvider>
    );

    fireEvent.click(screen.getByRole("button", { name: "foo" }));

    expect(onClick).not.toHaveBeenCalled();
  });

  test("When progress is set, the progress bar should have correct aria attributes.", () => {
    render(
      <ThemeProvider theme={materialTheme}>
        <SubmitSpinnerButton
          onClick={() => {}}
          disabled={true}
          loading={false}
          label="foo"
          progress={calculateProgress({ progressMade: 2, total: 4 })}
        />
      </ThemeProvider>
    );

    expect(screen.getByRole("progressbar")).toHaveAttribute(
      "aria-valuenow",
      "50"
    );
    expect(screen.getByRole("progressbar")).toHaveAttribute(
      "aria-valuemin",
      "0"
    );
    expect(screen.getByRole("progressbar")).toHaveAttribute(
      "aria-valuemax",
      "100"
    );
  });

  test("When loading is not set, the label should be shown.", () => {
    render(
      <ThemeProvider theme={materialTheme}>
        <SubmitSpinnerButton
          onClick={() => {}}
          disabled={true}
          loading={false}
          label="foo"
          progress={calculateProgress({ progressMade: 2, total: 4 })}
        />
      </ThemeProvider>
    );

    expect(screen.getByText("foo")).toBeVisible();
  });

  test("When loading is set, the label should not be shown.", () => {
    render(
      <ThemeProvider theme={materialTheme}>
        <SubmitSpinnerButton
          onClick={() => {}}
          disabled={true}
          loading={true}
          label="foo"
          progress={calculateProgress({ progressMade: 2, total: 4 })}
        />
      </ThemeProvider>
    );

    expect(screen.getByText("foo")).not.toBeVisible();
  });
});
