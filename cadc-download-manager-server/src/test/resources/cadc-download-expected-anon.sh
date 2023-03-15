#!/bin/bash

echo "https://mysite.com/download/file1.fits"
curl --location --remote-name --remote-header-name --progress-bar "https://mysite.com/download/file1.fits"

echo "https://mysite.com/download/file2.fits"
curl --location --remote-name --remote-header-name --progress-bar "https://mysite.com/download/file2.fits"

echo "ERROR trying resolve test:/broken: No such file."

echo "https://mysite.com/download/file3.fits"
curl --location --remote-name --remote-header-name --progress-bar "https://mysite.com/download/file3.fits"

echo "https://mysite.com/download/file4.fits"
curl --location --remote-name --remote-header-name --progress-bar "https://mysite.com/download/file4.fits"
