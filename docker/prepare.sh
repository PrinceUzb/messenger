#!/bin/bash
source env.sh

cd postgres || exit
source run.sh

cd ../redis || exit
source run.sh