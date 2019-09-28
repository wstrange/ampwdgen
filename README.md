# AM password encryptor

Utility to encrypt passwords for ForgeRock Access Manager. Deployed as a 
GCP Cloud Run function, it can be called using
 `curl` in installation scripts that need to replace passwords in an AM configuration. The service
accepts POST form data, and does not log credentials. The response is the
the encrypted AM password. 

To build and deploy (adjust for your project)

```shell script
gcloud builds submit
gcloud beta run deploy --image gcr.io/engineering-devops/ampwdgen --platform managed
```

# Usage
 
Using the URL that cloud run `deploy` generates, run:

```shell script
URL=https://ampwdgen-7escakmhgq-ue.a.run.app
curl -X POST -d "key=my_am_encryption_key" -d "password=amadmin_password"  -d "hash=true" $URL
```

The POST form parameters are:
* key - the AM instance encryption key
* password - the clear text password to encrypt
* hash=true/false (optional parameter, false if omitted) if true, hash the password first before encrypting. The 
amAdmin password must be hashed first.

The returned value is the encrypted string that can be used in a templated AM installation.


# Security

The cloud run service does not log any form data. If you have any concerns, you can
deploy this service to your own environment.

# Notes

If you need to retrieve the URL of the cloud function, run:

`gcloud beta run services list --platform managed`
