#!/bin/bash

source docker/env.sh
sbt -mem 3000 "project server" ~reStart