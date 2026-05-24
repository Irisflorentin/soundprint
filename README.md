# Soundprint 🎵

> 个人无损音乐库 + 在线播放器 + 音频格式转换工坊
> 东北大学软件工程专业 2026 春《Web 开发技术》课程大作业

## ✨ 项目简介

Soundprint 是一个面向音乐爱好者的个人音乐管理系统，支持：

- 上传本地音乐文件（FLAC / MP3 / WAV / AAC 等）
- 自动读取并管理 ID3 元数据
- 在线流式播放，带波形可视化
- 多种音频格式之间相互转换（基于 FFmpeg）
- 个人歌单管理、模糊搜索
- 听歌数据可视化（ECharts）

## 🛠 技术栈

**后端**
- Java 17 / Spring Boot 3.2 / MyBatis-Plus 3.5
- MySQL 8.0
- jaudiotagger（元数据） / FFmpeg（转码）

**前端**
- Vue 3 + TypeScript + Vite
- Element-Plus（业务组件） + Tailwind CSS（样式）
- vue-bits（动效组件库，用于视觉点缀）
- ECharts（数据可视化）
- wavesurfer.js（波形）

**部署**
- Docker + Docker Compose

## 🚀 快速开始

（等开发到 Phase 8 Docker 化后填写）

## 📁 项目结构

（等后续阶段填写）

## 🙏 引用与鸣谢

本项目使用以下开源资源，特此致谢：

- [Vue.js](https://vuejs.org/) - 渐进式 JavaScript 框架（MIT）
- [Element-Plus](https://element-plus.org/) - Vue 3 组件库（MIT）
- [vue-bits](https://github.com/DavidHDev/vue-bits) - Vue 动效组件库（MIT + Commons Clause）
- [Spring Boot](https://spring.io/projects/spring-boot) - Java 应用框架（Apache 2.0）
- [MyBatis-Plus](https://baomidou.com/) - MyBatis 增强工具（Apache 2.0）
- [FFmpeg](https://ffmpeg.org/) - 音视频处理工具（LGPL/GPL）
- [ECharts](https://echarts.apache.org/) - 数据可视化（Apache 2.0）
- [wavesurfer.js](https://wavesurfer.xyz/) - 音频波形可视化（BSD-3-Clause）

具体的 vue-bits 组件引用清单见 `docs/vue-bits-references.md`。

## 📝 开发者

姓名 · 班级 · 学号（提交前填写）

## 📄 License

本项目为课程作业，仅供学习交流。引用的第三方资源各自遵循其原协议。
