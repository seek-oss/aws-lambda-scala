# aws-lambda-scala example

Hopefully a production usable example of a scala lambda with logging, alarms, a dead letter queue, and deployment to multiple environments (eg: prod/dev).

For building, uses gradle.

For deployment, uses cloudformation and aws-cli.

For running the lambda locally in the lamdba runtime, uses [lambci/docker-lambda](https://github.com/lambci/docker-lambda).

To see your options, run `make`