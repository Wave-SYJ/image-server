mvn clean package

cd target && ls | grep -v quarkus-app | xargs rm -rf && cd ..

mkdir ./target/quarkus-app/config
cp ./src/main/resources/application-prod.yml ./target/quarkus-app/config/application-prod.yml
cp ./src/main/resources/application.yml ./target/quarkus-app/config/application.yml

cp docker-compose.yml ./target/quarkus-app/docker-compose.yml

mkdir ./target/quarkus-app/minio_data_dev

echo "java -jar quarkus-run.jar" > ./target/quarkus-app/run.sh

mv ./target/quarkus-app ./target/image-server
