# aws-lambda-scala

Hopefully a fairly complete production example of a simple scala lambda with cloudwatch logs and alarms, a dead letter queue, a stack policy, deployment of separate stacks for prod and dev environments, and tooling to manage the stacks.

The philosophy is to keep the toolset small and common to what folks probably already have installed and are familiar with:
* For building, uses gradle.
* For deployment, uses cloudformation and aws cli.
* For orchestrating build/deployment/ops, uses make.
* For running the lambda locally, uses [lambci/docker-lambda](https://github.com/lambci/docker-lambda).

## Prereqs

* java 8
* docker
* make
* jq
* python + aws cli

Check the config section of the Makefile is configured for your region, and `buildBucket` is unique. Then run `make build-bucket` to create the S3 build bucket.

## Commands

`make` to describe all commands  
`make build` build lambda.zip  
`make run` run lambda.zip locally inside the lambda runtime   
`make build run` build and run the lambda locally  

### Environment specific commands

The following commands all require the variable `environment`, which can be exported from the shell first, eg:
```
export environment=dev
make deploy
```
or alternatively, supplied on the command line, eg:`make deploy environment=dev` 

`make deploy` deploy lambda.zip  
`make stack-events` describe stack events (useful when stack updates fail)  
`make invoke` invoke the deployed lambda (RequestResponse invocation type)  
`make invoke payload='"bye byte"'` invoke the deployed lambda with an alternate payload  
`make deploy invoke` deploy then invoke the lambda  
`make delete-stack` delete the stack  
`make logs` show last 5 mins of logs  
`make logs mins=10` show last 10 mins of logs  
`make logs filter='START'` show logs containing `START`
