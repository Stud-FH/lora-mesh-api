
echo Building executable Jar File
mvn clean compile assembly:single

echo Building Docker Image
docker build -t lora-mesh-api .

echo Saving Docker Image as File
docker save -o lora-mesh-api.tar lora-mesh-api