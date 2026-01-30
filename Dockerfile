FROM ubuntu:latest
LABEL authors="panpan"

ENTRYPOINT ["top", "-b"]