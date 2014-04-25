#!/bin/bash

sbt "run $1 --genHarness --compile --test --backend c"
