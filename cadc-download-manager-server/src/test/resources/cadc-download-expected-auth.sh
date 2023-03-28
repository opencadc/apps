#!/bin/bash

echo "Warning!  The token provided in the cURL commands will expire on 1977-11-25T01:15:00."

TOKEN="SUPERTOKEN"

ERROR_URIS=(

)

URLS=(
"https://mysite.com/download/proprietary-1.fits"
"https://mysite.com/download/proprietary-2.fits"
"https://mysite.com/download/proprietary-3.fits"
"https://mysite.com/download/proprietary-4.fits"
)

for URI_ERROR in "${ERROR_URIS[@]}"; do
  # Split on three vertical bars.
  echo "ERROR trying resolve ${URI_ERROR/\|\|\|/: }"
done

for URL in "${URLS[@]}" ; do
  echo "${URL}"
  curl --location --remote-name --remote-header-name --progress-bar -H "authorization: bearer ${TOKEN}" "${URL}"
done
