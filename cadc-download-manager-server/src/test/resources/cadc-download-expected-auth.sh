#!/bin/bash

echo "Warning!  The token provided in the cURL commands will expire on 1977-11-25T01:15:00."

TOKEN="SUPERTOKEN"

echo "https://mysite.com/download/proprietary-1.fits"
curl --location --remote-name --remote-header-name --progress-bar -H "authorization: bearer ${TOKEN}" "https://mysite.com/download/proprietary-1.fits"

echo "https://mysite.com/download/proprietary-2.fits"
curl --location --remote-name --remote-header-name --progress-bar -H "authorization: bearer ${TOKEN}" "https://mysite.com/download/proprietary-2.fits"

echo "https://mysite.com/download/proprietary-3.fits"
curl --location --remote-name --remote-header-name --progress-bar -H "authorization: bearer ${TOKEN}" "https://mysite.com/download/proprietary-3.fits"

echo "https://mysite.com/download/proprietary-4.fits"
curl --location --remote-name --remote-header-name --progress-bar -H "authorization: bearer ${TOKEN}" "https://mysite.com/download/proprietary-4.fits"
