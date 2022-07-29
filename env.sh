#!/bin/bash

export POSTGRES_HOST="localhost"
export POSTGRES_PORT=5430
export POSTGRES_USER="messenger"
export POSTGRES_PASSWORD="123"
export POSTGRES_DATABASE="messenger"
export POSTGRES_POOL_SIZE=1024
export POSTGRES_MAX_CONNECTION=100
export HTTP_HEADER_LOG=false
export HTTP_BODY_LOG=false
export HTTP_HOST="localhost"
export HTTP_PORT=9000
export REDIS_SERVER_URI="redis://localhost"
export ACCESS_TOKEN_SECRET_KEY=dah3EeJ8xohtaeJ5ahyah-
export JWT_SECRET_KEY=dah3EeJ8xohtaeJ5ahyah-
export JWT_TOKEN_EXPIRATION=30.minutes
export PASSWORD_SALT=06!grsnxXG0d*Pj496p6fuA*o
export REDIS_SERVER_URI="redis://localhost"
export APP_ENV=TEST
source local_env.sh
