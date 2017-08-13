
uberjar:
	lein uberjar

docker-build: uberjar
	docker build -t highloadcup .

docker-run:
	docker run -it --rm -p 8080:80 highloadcup
