import React from "react";

export const JSONDisplay = ({ data, title = "JSON Data" }) => {
  if (!data) {
    return <p className="text-muted-foreground">No data to display</p>;
  }

  return (
    <div className="rounded-lg bg-muted p-4 overflow-auto max-h-[400px]">
      {title && <h4 className="font-semibold mb-2">{title}</h4>}
      <pre className="text-sm">
        {JSON.stringify(data, null, 2)}
      </pre>
    </div>
  );
};
