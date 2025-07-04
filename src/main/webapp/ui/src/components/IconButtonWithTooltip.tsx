import React from "react";
import { observer } from "mobx-react-lite";
import CustomTooltip from "./CustomTooltip";
import IconButton from "@mui/material/IconButton";
import { IconButtonProps } from "@mui/material";

type IconButtonWithTooltipProps = {
  title: string;
  icon: React.ReactNode;
  disabled?: boolean;
  className?: string;
  onClick?: (event: React.MouseEvent<HTMLButtonElement>) => void;
  "data-test-id"?: string;
  size?: "small" | "medium" | "large";
  color?: "primary" | "secondary" | "standardIcon" | "inherit";
  "aria-haspopup"?: "true" | "menu" | "listbox" | "tree" | "grid" | "dialog";
  classes?: { [key: string]: string };
  ariaLabel?: string;
  "aria-controls"?: string;
  "aria-expanded"?: boolean | "true" | "false";
  id?: string;
  tabIndex?: number;
  component?: React.ElementType;
  href?: string;
  sx?: IconButtonProps["sx"];
};

const IconButtonWithTooltip = React.forwardRef<
  HTMLButtonElement,
  IconButtonWithTooltipProps
>(
  (
    { title, icon, ariaLabel, disabled, ...rest }: IconButtonWithTooltipProps,
    ref,
  ) => {
    // When button is disabled, we need to use a wrapper span to show the tooltip
    const buttonElement = (
      <IconButton
        color="inherit"
        aria-label={ariaLabel ?? title}
        disabled={disabled}
        {...rest}
        ref={ref}
      >
        {icon}
      </IconButton>
    );

    return disabled ? (
      <span style={{ display: "inline-flex" }}>
        <CustomTooltip title={title}>{buttonElement}</CustomTooltip>
      </span>
    ) : (
      <CustomTooltip title={title}>{buttonElement}</CustomTooltip>
    );
  },
);

IconButtonWithTooltip.displayName = "IconButtonWithTooltip";
/**
 * This component provides a clickable icon button with a tooltip. The tooltip
 * is then used as the aria-label for the button.
 */
export default observer(IconButtonWithTooltip);
