# Cource-Reply

A simple course reply service API server. Build with Spring Boot & Keycloak.

基于Spring Boot实现的后端带鉴权的公共API,不仅支持Get还支持Post.

[前端地址](https://github.com/OldTaoge/Cource-Reply-Frontend)

## 架构

- 最简单的单体应用,单库,单表

- 最简单的鉴权方法(Run with Keycloak OpenID)

- 最花里胡哨的功能: 课程上传自动转码,文件支持

- 最穷的储存方案:除了本地储存之外还可以推到Onedrive云盘通过API+反代供Web使用

## 使用前必读

- 在`space.oldtaoge.edu.junsen.worker.TranscodeWorker`类里有转码和上传的全逻辑,使用前需要按需modify一下

- 在服务器上使用`Runtime`执行系统命令ssh到远程主机总是出问题,因此使用了[Remote Execer]()
