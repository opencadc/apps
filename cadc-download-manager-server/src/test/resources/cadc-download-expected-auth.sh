#!/bin/bash

echo "Warning!  The token provided in the cURL commands will expire on 1977-11-25T09:15:00."

curl -LJO --progress-bar -H "authorization: bearer SUPERTOKEN" "https://mysite.com/download/proprietary-1.fits"
curl -LJO --progress-bar -H "authorization: bearer SUPERTOKEN" "https://mysite.com/download/proprietary-2.fits"
curl -LJO --progress-bar -H "authorization: bearer SUPERTOKEN" "https://mysite.com/download/proprietary-3.fits"
curl -LJO --progress-bar -H "authorization: bearer SUPERTOKEN" "https://mysite.com/download/proprietary-4.fits"
