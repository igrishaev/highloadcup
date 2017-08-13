
repl:
	lein repl

uberjar:
	lein uberjar

docker-build: uberjar
	docker build -t highloadcup .

docker-run:
	docker run -it --rm -p 8080:80 -v $(CURDIR)/tmp/data:/tmp/data highloadcup

docker-tag:
	docker tag highloadcup:latest stor.highloadcup.ru/travels/fancy_yak

docker-push:
	docker push stor.highloadcup.ru/travels/fancy_yak

docker-auth:
	docker login stor.highloadcup.ru
