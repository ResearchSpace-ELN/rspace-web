import React, { useState } from "react";
import Chip from "@mui/material/Chip";
import Grid from "@mui/material/Grid";
import IconButtonWithTooltip from "../IconButtonWithTooltip";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { library } from "@fortawesome/fontawesome-svg-core";
import { faSitemap } from "@fortawesome/free-solid-svg-icons";
import Card from "@mui/material/Card";
import CardContent from "@mui/material/CardContent";
import DescriptionList from "../DescriptionList";
import Popover from "@mui/material/Popover";
import { type Tag } from "../../stores/definitions/Tag";
library.add(faSitemap);

function cardContent(tag: Tag) {
  return [
    {
      label: "Term",
      value: tag.value,
    },
    {
      label: "Term's URI",
      value: tag.uri.orElse(<>&mdash;</>),
    },
    {
      label: "Controlled Vocabulary Name",
      value: tag.vocabulary.orElse(<>&mdash;</>),
    },
    {
      label: "Controlled Vocabulary Version",
      value: tag.version.orElse(<>&mdash;</>),
    },
  ];
}

type TagListingArgs = {
  tags: Array<Tag>;
  size?: "small" | "medium";

  /*
   * Additional content may be added inline to the end of the listing, such as
   * a button to add more tags. The content MUST be wrapped in a <Grid item>
   */
  endAdornment?: React.ReactNode;

  showMetadataPopup?: boolean;
  onDelete?: (index: number, tag: Tag) => void;
  onClick?: (tag: Tag) => void;
};

/**
 * This component provides a listing of tags, with the ability to delete tags
 * if `onDelete` is true, to show the associated metadata if
 * `showMetadataPopup` is true, and to perform an action when a tag is clicked
 * if `onClick` is provided.
 */
export default function TagListing({
  tags,
  size = "medium",
  endAdornment,
  showMetadataPopup = true,
  onDelete,
  onClick = () => {},
}: TagListingArgs): React.ReactNode {
  const [metadataPopup, setMetadataPopup] = useState<{
    anchorEl: HTMLElement;
    tag: Tag;
  } | null>(null);

  return (
    <Grid container direction="row" spacing={1}>
      {tags.map((tag, index) => (
        <Grid item key={tag.uri.orElse(tag.value)}>
          <Chip
            label={tag.value}
            size={size}
            /*
             * When the chip is a clickable link to performing a search, we
             * set the `a` and `href` props so that the user can right-click
             * the chip and open it in a new tab/copy the link to the tag
             * search.
             */
            component={onDelete ? "div" : "a"}
            variant={
              tag.uri.isPresent() &&
              tag.version.isPresent() &&
              tag.vocabulary.isPresent()
                ? "filled"
                : "outlined"
            }
            style={{
              /*
               * By unsetting the userSelect style, when the chip is right
               * clicked the browser's context menu provides the option of
               * copying the link text, which is to say the tag's value.
               */
              userSelect: "unset",
              cursor: onDelete ? "default" : "pointer",
            }}
            {...(onDelete
              ? { onDelete: () => onDelete(index, tag) }
              : {
                  href: `/inventory/search?query=l: (tags:"${tag.value}")`,
                  onClick: (e: React.MouseEvent<HTMLDivElement>) => {
                    e.preventDefault();
                    e.stopPropagation();
                    onClick(tag);
                  },
                })}
            {...(showMetadataPopup &&
              tag.uri.isPresent() &&
              tag.vocabulary.isPresent() &&
              tag.version.isPresent() && {
                icon: (
                  <IconButtonWithTooltip
                    title="View metadata"
                    icon={<FontAwesomeIcon icon={faSitemap} />}
                    size="small"
                    color="primary"
                    onClick={(e) => {
                      e.preventDefault();
                      e.stopPropagation();
                      setMetadataPopup({
                        anchorEl: e.currentTarget,
                        tag,
                      });
                    }}
                  />
                ),
              })}
          />
        </Grid>
      ))}
      {metadataPopup && showMetadataPopup && (
        <Popover
          open={true}
          anchorEl={metadataPopup.anchorEl}
          anchorOrigin={{
            vertical: "top",
            horizontal: "right",
          }}
          transformOrigin={{
            vertical: "top",
            horizontal: "left",
          }}
          onClose={() => {
            setMetadataPopup(null);
          }}
        >
          <Card>
            <CardContent>
              <DescriptionList
                content={cardContent(metadataPopup.tag)}
                dividers={true}
              />
            </CardContent>
          </Card>
        </Popover>
      )}
      {endAdornment}
    </Grid>
  );
}
