import { useForm } from "@tanstack/react-form";
import * as v from "valibot";
import { TextField } from "@/modules/common/forms/mui";
import { Button, Stack } from "@mui/material";

// Valibot schema for the RaID identifier field
const RaidIdentifierSchema = v.pipe(
  v.string(),
  v.nonEmpty("RaID identifier is required"),
  v.minLength(3, "RaID identifier must be at least 3 characters")
);

// Full form schema
const RaidConnectionsFormSchema = v.object({
  raidIdentifier: RaidIdentifierSchema,
});

type RaidConnectionsAddFormValues = v.InferOutput<typeof RaidConnectionsFormSchema>;

interface RaidConnectionsAddFormProps {
  handleSubmit: (value: RaidConnectionsAddFormValues) => Promise<void> | void;
}

const RaidConnectionsAddForm = ({ handleSubmit }: RaidConnectionsAddFormProps) => {
  const form = useForm({
    defaultValues: {
      raidIdentifier: "",
    } satisfies RaidConnectionsAddFormValues,
    onSubmit: async ({ value }) => {
      // Validate with Valibot schema before submitting
      const result = v.safeParse(RaidConnectionsFormSchema, value);
      if (!result.success) {
        throw new Error("Validation failed");
      }
      await handleSubmit(result.output);
    },
  });

  return (
    <form
      onSubmit={(e) => {
        e.preventDefault();
        e.stopPropagation();
        void form.handleSubmit();
      }}
    >
      <Stack spacing={2} direction="row">
        <form.Field
          name="raidIdentifier"
          validators={{
            onChange: ({ value }) => {
              const result = v.safeParse(RaidIdentifierSchema, value);
              if (!result.success) {
                return result.issues[0]?.message ?? "Invalid value";
              }
              return undefined;
            },
          }}
        >
          {(field) => (
            <TextField
              field={field}
              label="RaID Identifier"
              placeholder="Enter RaID identifier"
              required
            />
          )}
        </form.Field>

        <form.Subscribe
          selector={(state) => [state.canSubmit, state.isSubmitting]}
        >
          {([canSubmit, isSubmitting]) => (
            <Button
              type="submit"
              variant="contained"
              disabled={!canSubmit}
            >
              {isSubmitting ? "Adding..." : "Add"}
            </Button>
          )}
        </form.Subscribe>
      </Stack>
    </form>
  );
};

export default RaidConnectionsAddForm;
