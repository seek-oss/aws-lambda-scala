MAKEFLAGS += --warn-undefined-variables
SHELL = /bin/bash -o pipefail
.DEFAULT_GOAL := help
.PHONY: help debug build rm run require-environment package deploy

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

region = ap-southeast-2
name = aws-lambda-scala
stackName = $(name)-$(environment)
buildBucket = $(name)-builds
template = src/main/cloudformation/lambda.yaml
template-packaged = build/distributions/lambda.yml
payload = {"hello":"world"}
bundle = build/distributions/lambda.zip

# -----------------------------------------
# Stack params

ifeq ($(environment),prod)
  alarmSubscriptionEndpoint = https://events.pagerduty.com/integration/1234567890/enqueue
  alarmSubscriptionProtocol = https
else ifeq ($(environment),dev)
  alarmSubscriptionEndpoint = dev-alarms@example.com
  alarmSubscriptionProtocol = email
endif

lambdaName = $(stackName)
memory = 256

params = $(call expand,alarmSubscriptionEndpoint alarmSubscriptionProtocol environment lambdaName memory version)

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

## create the S3 build bucket
build-bucket:
	aws s3 mb s3://$(buildBucket)

## build bundle
build: rm $(bundle)

rm:
	rm -f $(bundle)

$(bundle):
	./gradlew -x test build

build/distributions/lambda: $(bundle)
	rm -rf build/distributions/lambda/
	unzip -q -o $(bundle) -d build/distributions/lambda/

## run locally in a container containing the lambda runtime
run: build/distributions/lambda
	printf "%s" '$(payload)' | \
		docker run --rm -v $(PWD)/build/distributions/lambda:/var/task	\
			-i -e DOCKER_LAMBDA_USE_STDIN=1       						\
			-e AWS_DEFAULT_REGION=$(region)								\
			-e AWS_ACCESS_KEY_ID										\
			-e AWS_SECRET_ACCESS_KEY									\
			-e AWS_SESSION_TOKEN										\
			-e AWS_LAMBDA_FUNCTION_MEMORY_SIZE=$(memory)				\
			-e environment=$(environment)								\
			-e version=$(version)										\
			--memory=$(memory)m											\
			lambci/lambda:java8 tekumara.Lambda

require-environment:
	$(if $(value environment),,$(error Please provide environment=prod or environment=dev))

## deploy stack
deploy: require-environment
	# upload lambda.zip to s3 (if not already present) and produce template with the s3 location
	aws cloudformation package 							\
            --template-file $(template) 				\
            --output-template-file $(template-packaged) \
            --s3-bucket $(buildBucket) 					\
            --s3-prefix $(stackName)
	aws cloudformation deploy 							\
		--template-file $(template-packaged) 			\
		--stack-name $(stackName)						\
		--capabilities CAPABILITY_IAM 					\
		--parameter-overrides $(params) 				\
		--tags $(tags)
	aws cloudformation set-stack-policy					\
		--stack-name $(stackName)						\
		--stack-policy-body file://src/main/cloudformation/policy.json

## invoke
invoke: require-environment
	aws lambda invoke --invocation-type RequestResponse --function-name $(lambdaName) --region $(region) --payload '$(payload)' --log-type Tail build/distributions/invoke.resp.payload > build/distributions/invoke.resp
	@jq -r 'del(.LogResult)' build/distributions/invoke.resp
	@jq -r '.LogResult | @base64d' build/distributions/invoke.resp
	@cat build/distributions/invoke.resp.payload


## describe stack events (useful when stack updates fail)
stack-events: require-environment
	aws cloudformation describe-stack-events --stack-name $(stackName) | jq -r '.StackEvents[] | [.ResourceStatus, .LogicalResourceId, .ResourceStatusReason] | @tsv' | column -t -s $$'\t'

## delete the stack
delete-stack: require-environment
	aws cloudformation delete-stack --stack-name $(stackName)
	aws cloudformation wait stack-delete-complete --stack-name $(stackName)

## logs [mins=number] [filter=string]
logs: mins ?= 5
logs: nowmillis = $(shell echo $$((($$(date +%s)-(60*$(mins)))*1000)))
logs: filter ?= ""
logs: require-environment
	aws logs filter-log-events --log-group-name /aws/lambda/$(lambdaName) --interleaved --start-time $(nowmillis) --filter-pattern $(filter) | jq -r '.events[] | [(.timestamp / 1000 | todate), (.message | gsub("\\s+$$";""))] | join("\t")'

# -----------------------------------------
# Helpers

# produces list of key value pairs, eg: version=12345 LambdaName=aws-lambda-scala
expand = $(foreach var,$1,$(var)=$($(var)))