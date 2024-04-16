#!/bin/bash


export CONNECTION_STRING=""


filename="azure_load.py"

if [ -f "$filename" ]; then
    python3 $filename --containerName "transactions" --operation "UPLOAD_FILE" --filePath "sample.csv" --blobName "sample.csv"

    while :
	do
        python3 $filename --containerName "test" --operation "UPLOAD" --count 10 --size_in_kb 1
        sleep 1
        python3 $filename --containerName "test" --operation "RANDOM_READ" --count 10
        sleep 1
        python3 $filename --containerName "test" --operation "CLEAR_CONTAINER"
        sleep 1
        python3 $filename --containerName "transactions" --operation "QUERY_BLOB" --blobName "sample.csv" --query "SELECT _2 from BlobStorage"
        sleep 1
    done

else
    echo "File does not exist in the current directory."
      	exit 1
fi
