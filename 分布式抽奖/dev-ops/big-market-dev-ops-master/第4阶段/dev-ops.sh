cd /dev-ops
docker-compose -f docker-compose-environment.yml up -d

cd /dev-ops/
mkdir "github"

## 构建后端镜像
cd /dev-ops/github/
git clone -b docker-images-v4.0 https://gitcode.net/KnowledgePlanet/big-market/big-market.git

git checkout -b docker-images-v4.0

cd big-market/

mvn clean install

cd big-market-app/
cd
ls

chmod +x build.sh

./build.sh

## 构建前端镜像
cd /dev-ops/github/
git clone -b docker-images-v4.0 https://gitcode.net/KnowledgePlanet/big-market/big-market-front.git

git checkout -b docker-images-v4.0

cd big-market-front

chmod +x build.sh

./build.sh

