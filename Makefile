tag=latest

all: server

server: dummy
	./gradlew clean build -x test

run:
	./gradlew bootRun

test: dummy
	./gradlew test

dockerbuild:
	docker buildx build --platform linux/amd64 -t kobums/tomelaterspring:$(tag) .

docker: server dockerbuild

dockerrun:
	docker run --platform linux/amd64 -d --name="tomelaterspring" -p 8006:8006 kobums/tomelaterspring:$(tag)

push: docker
	docker push kobums/tomelaterspring:$(tag)

compose-up:
	docker-compose up -d --build

compose-down:
	docker-compose down

compose-logs:
	docker-compose logs -f

clean:
	./gradlew clean
	docker stop tomelaterspring || true
	docker rm tomelaterspring || true

dummy: