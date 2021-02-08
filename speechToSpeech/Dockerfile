FROM debian

RUN apt-get update && apt-get install -y default-jre

COPY target/speechToSpeech-*-jar-with-dependencies.jar /tmp/speech.jar
WORKDIR /tmp

EXPOSE $PORT

CMD java -jar /tmp/speech.jar