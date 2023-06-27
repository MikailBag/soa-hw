#!/usr/bin/env bash

protoc --go_out=pb --go_opt=paths=source_relative \
    --go-grpc_out=pb --go-grpc_opt=paths=source_relative \
    --proto_path ../src/main/proto ../src/main/proto/*