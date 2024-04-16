#!/bin/bash
bucket_name=$1
key_file_name=$2
upload_file_path="./$key_file_name"
jar_file="./s3loadgen.jar"

if [ $# -lt 2 ]
  then
    echo "No arguments supplied"
    exit 1
fi

if [ -f $jar_file ]
then
	java -jar s3loadgen.jar --bucketName $bucket_name --keyFileName $key_file_name --operation UPLOAD_FILE --filePath $upload_file_path
	while :
	do 
		java -jar s3loadgen.jar --bucketName $bucket_name --operation UPLOAD -n 10 --size 1K
		sleep 1
		java -jar s3loadgen.jar --bucketName $bucket_name --operation RANDOM_READ -n 10
		sleep 1
		java -jar s3loadgen.jar --bucketName $bucket_name --operation CLEAR_BUCKET -n 10
		sleep 1
		java -jar s3loadgen.jar --bucketName $bucket_name --keyFileName $key_file_name --operation S3_SELECT -n 10
		sleep 1
	done
else
	echo "$jar_file not exists"
  	exit 1
fi
