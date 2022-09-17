CONTAINER_DEV_PREFIX = "ocean_dev_"

run-dev-ldap: ;@docker compose run --detach --name "${CONTAINER_DEV_PREFIX}openldap" openldap;

monitor-dev: ;@docker container ls | grep "${CONTAINER_DEV_PREFIX}";

teardown-dev: ;@docker compose down;

.PHONY: run-dev-ldap monitor-dev teardown-dev
