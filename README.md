# kpop-suggest-api

How to run on local make sure you have maven and spring boot
./mvnw spring-boot:run



Docker path:kpopsuggest-api/kpopsuggest-java-api
docker run -d -e AWS_ACCESS_KEY_ID={access} -e AWS_SECRET_ACCESS_KEY={key} -p 8080:8080 --name kpopsuggest-java-api kpopsuggest-song-api
 