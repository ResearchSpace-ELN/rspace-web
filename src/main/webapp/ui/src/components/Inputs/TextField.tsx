import React from "react";
import Editor from "./StyledTinyMceEditor";
import NoValue from "../../components/NoValue";
import DOMPurify from "dompurify";

export type TextFieldArgs = {
  // required
  value: string;

  // optional
  disabled?: boolean;
  name?: string;
  noValueLabel?: string | null;
  onChange?: (event: { target: { name: string; value: string } }) => void;
  variant?: "filled" | "outlined" | "standard";
  id?: string;
};

export default function TextField({ onChange, ...props }: TextFieldArgs): React.ReactNode {
  const handleEditorChange = (content: string) => {
    const e = {
      target: {
        name: props.name ?? "",
        value: content,
      },
    };
    if (onChange) onChange(e);
  };

  if (props.disabled) {
    if (props.value) {
      return (
        <span
          dangerouslySetInnerHTML={{
            __html: DOMPurify.sanitize(props.value, { ADD_ATTR: ["target"] }),
          }}
        ></span>
      );
    }
    return <NoValue label={props.noValueLabel ?? "None"} />;
  }
  return (
    <Editor
      {...props}
      onEditorChange={handleEditorChange}
      init={{
        menubar: false,
        plugins: ["autolink", "lists", "link", "charmap", "autoresize"],
        toolbar:
          "undo redo blocks bold italic backcolor link \
             bullist numlist removeformat",
        paste_block_drop: true,
      }}
    />
  );
}
