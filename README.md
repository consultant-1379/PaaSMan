# Local Development

## Build Locally

> NOTES:
> * It will build the docker image after run all the tests
> * Require you have read permission to the docker repo setup in `~/.docker/config.json`

```sh

./mvnw clean package

```

## Run The Image Locally

```sh

docker run -it --rm -p 8888:8888 armdocker.rnd.ericsson.se/aia/aia-sdk-paas-manager --host=192.168.99.100 --spring.profiles.active=gateway,paas,local

```

You should be able to access the site once it's started:

* http://192.168.99.100:8888/
* http://192.168.99.100:8888/paas/v1/serviceability/info
* http://192.168.99.100:8888/marathon/

## Build Locally And Push The Docker Image

> NOTE: Require you have push permission to the docker repo

```sh

./mvnw clean package docker:push

```

## Build On Jenkins

> NOTES:
> * Run all tests
> * Build the docker image
> * Generate a site which will fail if code coverage is lower than 80%

```sh

./mvnw clean package site -P site

```


# Server Deployment

## atrcxb2994.athtem.eei.ericsson.se

```sh

ssh root@atrcxb2994.athtem.eei.ericsson.se  <<'ENDSSH'
docker pull armdocker.rnd.ericsson.se/aia/aia-sdk-paas-manager
docker stop aia-sdk-paas-manager
docker rm aia-sdk-paas-manager
mkdir -p /data/aia-sdk-paas-manager/maven-store
mkdir -p /data/aia-sdk-paas-manager/logs
docker run -d -p 80:8888 --name aia-sdk-paas-manager -v /data:/data armdocker.rnd.ericsson.se/aia/aia-sdk-paas-manager --host=$HOSTNAME
ENDSSH

```

You should be able to access the site once it's started:

* http://atrcxb2994.athtem.eei.ericsson.se/
* http://atrcxb2994.athtem.eei.ericsson.se/paas/v1/serviceability/info
* http://atrcxb2994.athtem.eei.ericsson.se/marathon/

# TODO


* Ray to document deployment to analytics.ericsson.se
* CI integration
