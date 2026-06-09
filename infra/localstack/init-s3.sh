#!/bin/bash
# Hook de inicializacao do LocalStack (executado quando o S3 fica pronto).
# Cria o bucket usado pelo service-files.
set -e
awslocal s3 mb s3://ouroboros-files
echo "Bucket ouroboros-files criado."
