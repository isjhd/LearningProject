cd /dev-ops
docker-compose -f docker-compose-environment.yml up -d

cd /dev-ops/
mkdir "github"

## 构建后端镜像
cd /dev-ops/github/
git clone https://gitcode.net/KnowledgePlanet/big-market/big-market.git

git checkout -b 240217-xfg-dev-ops-tag-raffle

cd big-market/

mvn clean install

cd big-market-app/

ls

chmod +x build.sh

./build.sh

## 构建前端镜像
cd /dev-ops/github/
git clone https://gitcode.net/KnowledgePlanet/big-market/big-market-front.git

git checkout -b 240217-xfg-dev-ops-tag-raffle
240217-xfg-dev-ops-tag-raffle

cd big-market-front

chmod +x build.sh

./build.sh

