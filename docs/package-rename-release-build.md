# 包名改 com.ranshen.cc + 打 4 版 release 包

## Recovery anchor

- 目标：包名由 `com.silent.android.webhtv` 改为 `com.ranshen.cc`；打 4 个 release 包（leanback / mobile × arm64_v8a / armeabi_v7a，5.6.0 / 560，R8 混淆 + 资源压缩）；4 个 APK 传 Drive 分享后提交并打 recovery tag。
- 任务 ID：`package-rename-release-build`；Lane：standard；分支：`main`，HEAD `7c752ed`；任务开始时工作树只有上一任务的文档改动（`docs/brand-rename-remove-update.md`，本次不动，属受保护脏文件）。
- 开始时间：2026-11-05 约 18:47 CST（Asia/Shanghai）。
- 预计：改名约 10 分钟；4 个 release 包构建约 20–30 分钟（R8 + armeabi_v7a 的 Chaquo pip 首次安装）；Drive 上传 4 个文件约 10 分钟；提交 / tag 约 5 分钟；总计约 45–60 分钟，预计 19:30–19:50 CST 完成。
- 最便宜的决定性验证：`assembleRelease`（4 个变体一次出）+ aapt2 检查每个 APK 的 package 为 `com.ranshen.cc`、version 5.6.0、标签 冉神影视。

## 关键调查结论（2026-11-05）

- 包名只出现在两处：`app/build.gradle:27` 的 `applicationId`，以及 `WebViewDataDirectoryGuardTest.java` 的 7 处测试夹具字符串（假 /proc 进程名，改为新包名保持一致）。Manifest 里的 provider authorities 与 intent action 全是 `${applicationId}` 占位符，自动跟随；代码命名空间 `com.fongmi.android.tv` 不是包名，不动；`Silent1566`（GitHub 用户名）与英文 `silent`（如 `BACKGROUND_MODE_SILENT`）与包名无关，不动。
- 签名：本地没有 release keystore（真 key 只在 GitHub secrets 里，`RELEASE_KEYSTORE_BASE64` 等）；`app/build.gradle` 在 `local.properties` 无签名配置时让 release 构建回退到 debug 签名。因此本地打出的 4 个包是 release 类型（R8 + 资源压缩，体积比 185MB 的 debug 包小得多），但签名是 debug 签名，不是正式 release 签名。正式签名的 4 个包只能走 GitHub release 工作流（仍等用户填 GitHub 登录卡）。这点已告知用户。
- 包名一改即新应用身份：旧包名装的版本无法原地升级到新包名版本，需全新安装，旧数据不迁移；新旧可共存。
- 版本号不动：versionCode 560，versionName 5.6.0。
- 构建注意：armeabi_v7a 的 Chaquo pip 是首次安装（之前只装过 arm64），需要联网下载 armeabi 轮子；构建前 `gradle --stop` 让新 daemon 继承当前 egress token（token 会轮换，旧 daemon 会 407）。

## 任务清单

- [x] 包名改为 com.ranshen.cc（build.gradle + 测试夹具）
- [x] 打 4 个 release 包并用 aapt2 验证——2026-11-05 20:05 CST，BUILD SUCCESSFUL；4 个包 aapt2 均为包名 com.ranshen.cc、5.6.0 / 560、标签 冉神影视。电视版 arm64 164,920,108 字节，电视版 32 位 135,444,649 字节，手机版 arm64 165,369,131 字节，手机版 32 位 135,893,672 字节。构建中途云电脑重启了一次（19:29），已打好的 2 个电视版包还在，从断点续跑完成。
- [x] 4 个 APK 传 Drive 分享，链接发给用户——2026-11-05 20:06 CST 全部上传并设为 anyone-with-link reader；链接见下。
- [x] 提交 + recovery tag（commit `eca2f14`，tag `recovery/package-rename-release-build/20261105200803-eca2f14ed49e`，工作树只剩上一任务的受保护文档改动，未推送）

## 4 个 release 包下载链接（Drive，anyone-with-link）

- 电视版 arm64：https://drive.google.com/uc?id=1FG10Hg_dqhUlilwzXReWWR-vIhdRcNGx&export=download
- 电视版 32 位：https://drive.google.com/uc?id=1wUm8eRtaCODKobzdhpjTJ8pCBQP6VQVt&export=download
- 手机版 arm64：https://drive.google.com/uc?id=1ntaS-JE1GrXsV0Elqr8Jm2oZVru5fvZ1&export=download
- 手机版 32 位：https://drive.google.com/uc?id=1lS0dHwawzawcb5PsGm6nvi_QN1MQR1Q-&export=download

## 下一步

- 全部完成。本次任务无遗留事项（正式版发布触发仍在等 GitHub 登录卡，那是另一件事）。
