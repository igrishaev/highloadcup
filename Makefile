
PROJECT := highloadcup
REPO_HOST := stor.highloadcup.ru
REPO_PATH := travels/fancy_yak

repl:
	lein repl

uberjar:
	lein uberjar

docker-build: uberjar
	docker build -t $(PROJECT) .

docker-run:
	docker run -it --rm -p 8080:80 -v $(CURDIR)/tmp/data:/tmp/data $(PROJECT)

docker-tag:
	docker tag $(PROJECT):latest $(REPO_HOST)/$(REPO_PATH)

docker-push:
	docker push $(REPO_HOST)/$(REPO_PATH)

docker-auth:
	docker login $(REPO_HOST)
