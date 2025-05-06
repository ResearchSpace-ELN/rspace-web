import React from "react";
import useOauthToken from "@/common/useOauthToken";
import * as FetchingData from "../../util/fetchingData";
import axios from "@/common/axios";

export type Document = {
  id: number;
  globalId: string;
  title: string;
  fields: ReadonlyArray<{
    id: number;
    globalId: string;
    name: string;
    type: string;
    content: string;
    files: ReadonlyArray<{
      id: number;
      globalId: string;
      name: string;
    }>;
  }>;
};

export function useGetDocument(id: number): FetchingData.Fetched<Document> {
  const { getToken } = useOauthToken();
  const [state, setState] = React.useState<FetchingData.Fetched<Document>>({
    tag: "loading",
  });

  React.useEffect(() => {
    void (async () => {
      try {
        const { data } = await axios.get(`/api/v1/documents/${id}`, {
          headers: {
            Authorization: `Bearer ${await getToken()}`,
          },
        });
        // TODO: parse
        setState({ tag: "success", value: data as Document });
      } catch (error) {
        if (error instanceof Error) {
          setState({ tag: "error", error: error.message });
        } else {
          setState({ tag: "error", error: "Unknown error" });
        }
      }
    })();
  }, [id, getToken]);

  return state;
}
