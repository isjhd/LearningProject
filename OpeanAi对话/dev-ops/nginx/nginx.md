docker container cp Nginx:/etc/nginx/nginx.conf /Users/fuzhengwei/1024/KnowledgePlanet/chatgpt/dev-ops/nginx/conf
docker container cp Nginx:/etc/nginx/conf.d/default.conf /Users/fuzhengwei/1024/KnowledgePlanet/chatgpt/dev-ops/nginx/conf/conf.d
docker container cp Nginx:/usr/share/nginx/html/index.html /Users/fuzhengwei/1024/KnowledgePlanet/chatgpt/dev-ops/nginx/html

docker run \
--name Nginx \
-p 80:80 \
-v /Users/fuzhengwei/1024/KnowledgePlanet/chatgpt/dev-ops/nginx/logs:/var/log/nginx \
-v /Users/fuzhengwei/1024/KnowledgePlanet/chatgpt/dev-ops/nginx/html:/usr/share/nginx/html \
-v /Users/fuzhengwei/1024/KnowledgePlanet/chatgpt/dev-ops/nginx/conf/nginx.conf:/etc/nginx/nginx.conf \
-v /Users/fuzhengwei/1024/KnowledgePlanet/chatgpt/dev-ops/nginx/conf/conf.d:/etc/nginx/conf.d \
-v /Users/fuzhengwei/1024/KnowledgePlanet/chatgpt/dev-ops/nginx/ssl:/etc/nginx/ssl/  \
--privileged=true -d --restart=always nginx

详细请看 dev-ops 第4节：Nginx环境配置