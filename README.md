<div align="center">

<img src="https://ssl.lunadeer.cn:14437/i/2024/03/28/6604f0cec0f0e.png" alt="" width="70%">

### [开源地址](https://github.com/ColdeZhang/Dominion) | [文档地址](https://ssl.lunadeer.cn:14448/doc/23/)

### [下载页面](https://ssl.lunadeer.cn:14446/mirror/Dominion/releases)

### [统计页面](https://bstats.org/plugin/bukkit/Dominion/21445) | [Hangar](https://hangar.papermc.io/zhangyuheng/Dominion)

</div>

## 简介

鉴于 Residence 插件的作者项目较多，维护压力大，无法及时跟进新版本以及适配Folia核心。故开发此插件，旨在平替纯净版生存服Residence的使用（支持从 Res 迁移数据）。

**请注意，本插件目前处于中期测试稳定阶段，绝大多数bug或漏洞已被修复，目前已具备完全可用性。但不排除仍然存在某些问题，如果遇到任何 BUG 欢迎及时发送邮件或添加QQ群告知，感激不尽。**

## 说明

本插件基本还原了Residence的核心功能，主要适用于原版纯净生存服务器的防破坏目的，支持基础的价格系统。

![](https://ssl.lunadeer.cn:14437/i/2024/02/16/65cf3b08c986b.png)

为了提高存储效率，本插件使用了数据库+缓存的方式存储领地数据，玩家配置领地权限直接修改数据库内容，随后触发缓存更新。权限控制则以异步的方式访问缓存，减少事件阻塞。

权限系统主要由领地权限、玩家特权组成，玩家特权优先级高于领地权限。没有特权的玩家在领地内收到领地权限的控制，有特权配置则按照特权设置受控。

## 功能介绍

- 支持 Postgresql、Mysql、Sqlite3 存储数据；
- 支持 BlueMap、Dynmap 卫星地图渲染；
- 支持为玩家单独设置特权；
- 支持设置领地管理员；
- 支持子领地；
- 采用 TUI 方式进行权限配置交互，简单快捷；
- 支持经济系统（需要 Vault 前置）；
- 领地区域可视化；
- 管理员可在游戏内使用TUI配置领地系统；
- 支持[从 Residence 迁移](https://ssl.lunadeer.cn:14448/doc/73/)领地数据（1.33.7+）；
- 超高性能（一个坐标在10127个领地内的搜索平均耗时不超过0.2ms，仅占用1tick的不到0.4%时间）

<div align="center">

### 创建领地

<img src="https://ssl.lunadeer.cn:14437/i/2024/05/10/663debf78eca4.gif" alt="" width="60%">

### 权限管理

<img src="https://ssl.lunadeer.cn:14437/i/2024/05/10/663debe052786.gif" alt="" width="60%">

### 配置

<img src="https://ssl.lunadeer.cn:14437/i/2024/05/10/663debec11dad.gif" alt="" width="60%">

### 高性能

<img src="https://ssl.lunadeer.cn:14437/i/2024/08/13/66bad56cc9fac.png" alt="" width="60%">

</div>

## 支持版本

- 1.20.1+ (Bukkit、Spigot、Paper、Folia)

> 需要使用 Java21 运行你的服务端，如果你还在使用 Java17 可以放心替换为 Java21，理论上 1.20.1+ 版本的服务端核心可以直接升级到 Java21 启动。

## TODO

- WebUI

## 建议与反馈

Mail: [zhangyuheng@lunadeer.cn](mailto:zhangyuheng@lunadeer.cn)

QQ群：309428300

## 统计

![bstats](https://bstats.org/signatures/bukkit/Dominion.svg)
