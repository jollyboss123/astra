.PHONY: app
app:
	docker-compose -f src/main/docker/app.yml up -d

.PHONY: postgres
postgres:
	docker-compose -f src/main/docker/postgresql.yml up -d

.PHONY: keycloak
keycloak:
	docker-compose -f src/main/docker/keycloak.yml up -d

.PHONY: jib
jib:
	./mvnw -Pprod package verify jib:dockerBuild --offline
