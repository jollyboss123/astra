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

.PHONY: info
info:
	./mvnw -ntp enforcer:display-info --batch-mode

.PHONY: checkstyle
checkstyle:
	./mvnw -ntp checkstyle:check --batch-mode

.PHONY: doc
doc:
	./mvnw -ntp javadoc:javadoc --batch-mode

.PHONY: unit-test
unit-test:
	./mvnw -ntp test --batch-mode -Dlogging.level.ROOT=OFF -Dlogging.level.com.jolly.astra=OFF -Dlogging.level.org.springframework=OFF -Dlogging.level.org.springframework.web=OFF -Dlogging.level.org.springframework.security=OFF

.PHONY: integration-test
integration-test:
	./mvnw -ntp verify --batch-mode -Dlogging.level.ROOT=OFF -Dlogging.level.com.jolly.astra=OFF -Dlogging.level.org.springframework=OFF -Dlogging.level.org.springframework.web=OFF -Dlogging.level.org.springframework.security=OFF

.PHONY: test-all
test-all: info doc checkstyle unit-test
