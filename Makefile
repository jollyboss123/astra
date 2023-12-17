app:
	docker-compose -f src/main/docker/app.yml up -d

postgres:
	docker-compose -f src/main/docker/postgresql.yml up -d

jib:
	./mvnw -Pprod package verify jib:dockerBuild --offline
