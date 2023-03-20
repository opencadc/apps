#!/bin/bash

ERROR_URIS=(
"test:/broken|||No such file."
"test:/broken2|||No such path."
)

URLS=(
"https://mysite.com/download/file1.fits"
"https://mysite.com/download/file2.fits"
"https://mysite.com/download/file3.fits"
"https://mysite.com/download/file4.fits"
)

for URI_ERROR in "${ERROR_URIS[@]}"; do
  # Split on three vertical bars.
  echo "ERROR trying resolve ${URI_ERROR/\|\|\|/: }"
done

for URL in "${URLS[@]}" ; do
  echo "${URL}"
  curl --location --remote-name --remote-header-name --progress-bar "${URL}"
done
