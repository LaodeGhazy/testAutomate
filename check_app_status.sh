#!/bin/bash

STATUS_CODE=$(curl -s -o /dev/null -w "%{http_code}" http://localhost:5000)

if [[ $STATUS_CODE -eq 200 ]]; then
  echo "Aplikasi web berjalan."
else
  echo "Aplikasi web tidak berjalan."
  exit 1
fi
