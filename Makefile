
PROJECT := highloadcup
REPO_HOST := stor.highloadcup.ru
REPO_PATH := travels/fancy_yak
TAG := sqlite

repl:
	lein repl

.PHONY: test
test:
	lein test

uberjar:
	lein uberjar

uberjar-run:
	source .env && java -jar target/uberjar/highloadcup.jar

docker-build:
	docker build --no-cache -t $(PROJECT):$(TAG) .

docker-run:
	docker run -it --rm -p 8080:80 -v $(CURDIR)/tmp/data:/tmp/data:ro $(PROJECT):$(TAG)

docker-tag:
	docker tag $(PROJECT):$(TAG) $(REPO_HOST)/$(REPO_PATH)

docker-push:
	docker push $(REPO_HOST)/$(REPO_PATH)

docker-auth:
	docker login $(REPO_HOST)

deploy: uberjar docker-build docker-tag docker-push
