# aws-lambda-scala

Hopefully a fairly complete production example of a simple scala lambda with cloudwatch logs and alarms, a dead letter queue, and deployment of separate stacks for prod and dev environments.

The philosophy is to keep the toolset small and common to what folks probably already have installed and are familiar with:
* For building, uses gradle.
* For deployment, uses cloudformation and aws-cli.
* For running the lambda locally in the lambda runtime, uses [lambci/docker-lambda](https://github.com/lambci/docker-lambda).

## Prereqs

* java 8
* docker
* make
* python + aws cli

Check the config section of the Makefile is configured for your region, and `buildBucket` is unique.

## Commands

`make build` build lambda.zip  
`make run` run lambda.zip locally inside the lambda runtime   
`make build-bucket` creates the S3 build bucket  
`make deploy environment=dev` deploy lambda.zip with the dev stack  
`make deploy environment=prod` deploy lambda.zip with the prod stack  
`make describe-stack-events environment=dev` describe stack events (useful when stack updates fail)  
`make invoke environment=dev` invoke the deployed lambda  
`make delete-stack environment=dev` delete the dev stack

To describe all your options, run `make`