#!/bin/bash

ERROR_URIS=(
%%%ERRORURIS%%%
)

URLS=(
%%%URLS%%%
)

for URI_ERROR in "${ERROR_URIS[@]}"; do
  # Split on three vertical bars.
  echo "ERROR trying resolve ${URI_ERROR/\|\|\|/: }"
done

for URL in "${URLS[@]}" ; do
  echo "${URL}"
  curl --location --remote-name --remote-header-name --progress-bar "${URL}"
done
