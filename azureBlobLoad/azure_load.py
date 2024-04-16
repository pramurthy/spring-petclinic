import os

from azure.storage.blob import BlobServiceClient, ContainerClient, BlobClient, DelimitedJsonDialect, DelimitedTextDialect



import argparse




errors = []

def on_error(error):

    errors.append(error)



# Upload the local file to the given container

def upload_localfile(local_file_path,blob_name,container_name):

    # Create the container if it doesn't exist
    container_client = blob_service_client.get_container_client(container_name)
    if not container_client.exists():
        container_client.create_container()
    blob_client = container_client.get_blob_client(blob_name)

    # Open the CSV file in binary mode and upload it to Blob Storage
    with open(local_file_path, "rb") as data:
        blob_client.upload_blob(data,overwrite=True)
    print(blob_name," uploaded into container ",container_name," successfully.")





def random_upload(n,size_in_kb,container_name):
    # Create the container if it doesn't exist
    container_client = blob_service_client.get_container_client(container_name)
    if not container_client.exists():
        container_client.create_container()
    for i in range(int(n)):
        blob_name = f"file-{i}"
        blob_client = container_client.get_blob_client(blob_name)
        blob_client.upload_blob(b"0" *int(size_in_kb)*1024 , overwrite=True)
        print(blob_name," uploaded into container ",container_name," successfully.")



def random_read(n,container_name):
    container_client = blob_service_client.get_container_client(container_name)
    if not container_client.exists():
        print("container",container_name," doesn't exist.")
        return
    for i in range(int(n)):
        blob_name = f"file-{i % 10}"
        blob_client = container_client.get_blob_client(blob_name)
        data = blob_client.download_blob().readall()
        print("Read blob ",blob_name," successfully.")



def query_blob_content(container_name,blob_name,query_expression,printF):

    container_client = blob_service_client.get_container_client(container_name)
    if not container_client.exists():
        print("container",container_name," doesn't exist.")
        return
    blob_client = container_client.get_blob_client(blob_name)
    input_format = DelimitedTextDialect(delimiter=',', quotechar='"', lineterminator='\n', escapechar='', has_header=True)
    output_format = DelimitedJsonDialect(delimiter='\n')
    reader = blob_client.query_blob(query_expression, on_error=on_error,blob_format=input_format, output_format=output_format)
    content = reader.readall()
    print("Query was done successfully on ",blob_name)

    if(printF):
        print(content)


# Clear the container by deleting all blobs
def clear_container(container_name):
    container_client = blob_service_client.get_container_client(container_name)
    if not container_client.exists():
        print("container",container_name," doesn't exist.")
        return

    for blob in container_client.list_blobs():
        container_client.delete_blob(blob.name)
    print("Deleted all blobs inside ",container_name," successfully.")


# Delete the container
def delete_container(container_name):
    container_client = blob_service_client.get_container_client(container_name)
    if not container_client.exists():
        print("container",container_name," doesn't exist.")
        return
    container_client.delete_container()
    print("Deleted ",container_name," container successfully.")




parser = argparse.ArgumentParser()
parser.add_argument("--containerName", required=True)
parser.add_argument("--operation", required=True , choices=["UPLOAD_FILE","UPLOAD","RANDOM_READ","CLEAR_CONTAINER","DELETE_CONTAINER","QUERY_BLOB"])
parser.add_argument("--filePath")
parser.add_argument("--size_in_kb")
parser.add_argument("--count")
parser.add_argument("--blobName")
parser.add_argument("--query")

args = parser.parse_args()



container_name = args.containerName
operation = args.operation
local_file_path = args.filePath
blob_name = args.blobName
count = args.count
size_in_kb = args.size_in_kb
query_expression = args.query









# print(f"container_name: {container_name}")

# Set your connection string here or set it as an environment variable
connection_string = os.getenv("CONNECTION_STRING")
# Initialize the BlobServiceClient
blob_service_client = BlobServiceClient.from_connection_string(connection_string)



if(operation == "UPLOAD_FILE"):
    if(local_file_path and blob_name and container_name):
        upload_localfile(local_file_path,blob_name,container_name)
    else:
        print("require containerName,blobName and filePath to perform UPLOAD_FILE operation")

elif(operation == "UPLOAD"):
    if(count and size_in_kb and container_name):
        random_upload(count,size_in_kb,container_name)
    else:
        print("require count,size_in_kb and containerName to perform UPLOAD operation")

elif(operation == "RANDOM_READ"):
    if(count and container_name):
        random_read(count,container_name)
    else:
        print("require count and containerName to perform RANDOM_READ operation")

elif(operation == "CLEAR_CONTAINER"):
    if(container_name):
        clear_container(container_name)
    else:
        print("require containerName to perform CLEAR_CONTAINER operation")

elif(operation == "DELETE_CONTAINER"):
    if(container_name):
        delete_container(container_name)
    else:
        print("require containerName to perform CLEAR_CONTAINER operation") 

elif(operation == "QUERY_BLOB"):
    if(container_name and blob_name and query_expression):
        query_blob_content(container_name,blob_name,query_expression,False)
    else:
        print("require containerName, blobName and query to perform QUERY_BLOB operation") 


else:
    print("not valid operation")


