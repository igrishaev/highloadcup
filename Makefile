
PROJECT := highloadcup
REPO_HOST := stor.highloadcup.ru
REPO_PATH := travels/fancy_yak
TAG := 0.1

repl:
	lein repl

uberjar:
	lein uberjar

uberjar-run:
	source .env && java -jar target/uberjar/highloadcup.jar

docker-build: uberjar
	docker build --no-cache -t $(PROJECT):$(TAG) .

docker-run:
	docker run -it --rm -p 8080:80 -v $(CURDIR)/tmp/data:/tmp/data $(PROJECT):$(TAG)

docker-tag:
	docker tag $(PROJECT):$(TAG) $(REPO_HOST)/$(REPO_PATH)

docker-push:
	docker push $(REPO_HOST)/$(REPO_PATH)

docker-auth:
	docker login $(REPO_HOST)
