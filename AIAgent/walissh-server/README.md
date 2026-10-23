# walissh-server ai shell 智能终端

环境说明；

- jdk 17
- maven 3.8.x
- application-dev.yml、ssh-agent.yml，有两处要配置 LLM - `如果学习过 ai agent 脚手架，则可以更好的上手此项目`

>注意，先启动服务端 walissh-server。把ssh 的操作操作，ai的操作，统一放到服务端，可以更有效的统一控制风险（企业中最为常见的做法）。如果都在客户端，可能会有人误操作执行危险命令，或者把核心服务器信息泄露。