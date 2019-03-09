MAKEFLAGS += --warn-undefined-variables
SHELL = /bin/bash -o pipefail
.DEFAULT_GOAL := help
.PHONY: *

# -----------------------------------------
# Version

commit_sha = $(shell (git diff-index --quiet HEAD -- && git rev-parse --short HEAD) || echo "uncommited")
build_number = $(shell whoami).snapshot
ifdef BUILDKITE_BUILD_NUMBER
	build_number = $(BUILDKITE_BUILD_NUMBER)
endif
version = $(build_number).$(commit_sha)

# -----------------------------------------
# Config

stackName = aws-lambda-scala-$(environment)
buildBucket = $(stackName)-builds
template = src/main/cloudformation/$(stackName).yaml
template-packaged = build/cloudformation/packaged.yml

# -----------------------------------------
# Stack params

ifeq ($(environment),prod)
  AlarmSubscriptionEndpoint = https://events.pagerduty.com/integration/1234567890/enqueue
  AlarmSubscriptionProtocol = https
else ifeq ($(environment),dev)
  AlarmSubscriptionEndpoint = dev-alarms@example.com
  AlarmSubscriptionProtocol = email
else ifneq ($(MAKECMDGOALS),help)
 ifneq ($(MAKECMDGOALS),)
  $(error Please provide account=prod or account=dev)
 endif
endif

LambdaName = $(stackName)

params = $(call expand,Environment AlarmSubscriptionEndpoint AlarmSubscriptionProtocol LambdaName)

# -----------------------------------------
# Stack tags

owner = tekumara@example.com

tags = $(call expand,version owner)

# -----------------------------------------
# Targets

## display this help message
help:
	@awk '/^##.*$$/,/^[~\/\.a-zA-Z_-]+:/' $(MAKEFILE_LIST) | awk '!(NR%2){print $$0p}{p=$$0}' | awk 'BEGIN {FS = ":.*?##"}; {printf "\033[36m%-20s\033[0m %s\n", $$1, $$2}' | sort

## print environment for build debugging
debug:
	@printf ".DEFAULT_GOAL=%s\n" "$(.DEFAULT_GOAL)"
	@printf "\n"
	@printf "Stack params:\n%s\n" "$(params)"
	@printf "\n"
	@printf "Stack tags:\n%s\n" "$(tags)"

## Build build/distributions/app.zip
build:
	rm -f build/distributions/app.zip
	make build/distributions/app.zip

build/distributions/app.zip:
	./gradlew -x test build

## build
build:
	./gradlew build

## run locally in a container containing the lambda runtime
run: memory = 1600
run: /tmp/task
	cat "hello world" | \
		docker run --rm -v /tmp/task:/var/task							\
			-i -e DOCKER_LAMBDA_USE_STDIN=1       						\
			-e AWS_DEFAULT_REGION=$(region)								\
			-e AWS_ACCESS_KEY_ID										\
			-e AWS_SECRET_ACCESS_KEY									\
			-e AWS_SESSION_TOKEN										\
			-e AWS_LAMBDA_FUNCTION_MEMORY_SIZE=$(memory)				\
			-e STAGE=test												\
			--memory=$(memory)m											\
			lambci/lambda:java8 seek.aips.cf.Handler

package:
	# upload lambda code to s3 and produce template with the s3 location
	aws cloudformation package 							\
            --template-file $(template) 				\
            --output-template-file $(template-packaged) \
            --s3-bucket $(buildBucket) 					\
            --s3-prefix $(stackName)

## deploy
deploy: package
	aws cloudformation deploy 							\
		--template-file $(template-packaged) 			\
		--stack-name $(stackName)						\
		--capabilities CAPABILITY_IAM 					\
		--parameter-overrides $(params) 				\
		--tags $(tags)
	aws cloudformation set-stack-policy					\
		--stack-name $(stackName)						\
		--stack-policy-body file://src/main/cloudformation/policy.json


# -----------------------------------------
# Helpers

# produces list of key value pairs, eg: version=12345 LambdaName=aws-lambda-scala
expand = $(foreach var,$1,$(var)=$($(var)))