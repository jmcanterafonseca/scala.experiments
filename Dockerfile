FROM openjdk:jre-alpine

ENV sbt_version 1.1.2
ENV scala_version 2.12.5
ENV sbt_home /usr/local/sbt
ENV scala_home /usr/local/scala
ENV PATH ${PATH}:${sbt_home}/bin:${scala_home}/scala-${scala_version}/bin

WORKDIR /app

RUN mkdir -p $sbt_home $scala_home /opt \
&&  apk add --no-cache --update python3 git wget bash \
&&  wget -qO - --no-check-certificate "https://github.com/sbt/sbt/releases/download/v${sbt_version}/sbt-${sbt_version}.tgz" | tar xz -C $sbt_home --strip-components=1 \
&&  sbt sbtVersion \
&&  wget -qO - --no-check-certificate "https://downloads.typesafe.com/scala/${scala_version}/scala-${scala_version}.tgz" | tar xz -C $scala_home \
&&  git clone https://github.com/jmcanterafonseca/scala.experiments.git -b check_crate /app/\
&&  apk del wget git \
&&  sbt assembly \
&&  mv /app/target/scala-2.12/DMO_NGSI-assembly-0.1.jar /opt/app.jar \
&&  mv /app/src/test/resources /opt/list \
&&  rm -f /opt/list/Example* \
&&  rm -Rf /app

WORKDIR /opt

ADD stream.py /opt/stream.py

CMD ["python3","-u", "/opt/stream.py"]
