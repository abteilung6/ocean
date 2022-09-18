CONTAINER_DEV_PREFIX = "ocean_dev_container_"
ORCHESTRATION_PATH = "orchestration/"

deploy-dev: ;@docker compose -f "${ORCHESTRATION_PATH}docker-compose.dev.yml" up --detach;

teardown-dev: ;@docker compose -f "${ORCHESTRATION_PATH}docker-compose.dev.yml" down;

monitor-dev: ;@docker container ls | grep "${CONTAINER_DEV_PREFIX}";

.PHONY: deploy-dev teardown-dev monitor-dev
