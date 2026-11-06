# 修正式版工作流的测试引用（配合改名/删更新/改包名）

## Recovery anchor

- 目标：`.github/workflows/android-release.yml` 的测试步骤不再引用已删的 5 个测试类，换成现存测试；提交为一个新提交。注意：用户 2026-11-06 15:45 已决定不在 GitHub 上构建 APK（后续仍在云电脑虚拟机上构建），GitHub 只同步最新源码；本提交只修工作流文件，不触发任何工作流。
- 任务 ID：`workflow-test-fix`；Lane：standard；分支：`main`，HEAD `eca2f14`；任务开始时工作树有两处上一任务的文档改动（`docs/brand-rename-remove-update.md`、`docs/package-rename-release-build.md`，本次不动，属受保护脏文件）及工作流文件改动（本次提交范围，已领养）。
- 开始时间：2026-11-06 约 15:46 CST（Asia/Shanghai）。
- 预计：提交约 5 分钟；推送待 GitHub git 凭证（见“推送状态”），不在本次预计内。
- 最便宜的决定性验证：文件级核对——工作流里不再出现 5 个已删类名；保留/新增的每个 `--tests` 引用都能对应到现存测试文件的包声明；YAML 能解析。未跑 Gradle 测试命令（见下）。

## 改动内容

- mobile 步（`testMobileArm64_v8aDebugUnitTest`）删 4 行已删类：`UpdateChannelTest`、`UpdaterFutureTest`、`utils/GithubTest`、`ui/dialog/UpdateDialogChannelVisibilityTest`；保留 `danmaku.*`、`utils/GithubProxyTest`、`ui/dialog/AboutDialogLayoutTest`、`ui/adapter/VodAdapterLayoutTest`；新增 `setting/AppBrandingContractTest`（改名验证测试，2026-11-05 本地 11/11 通过）。
- leanback 步（`testLeanbackArm64_v8aDebugUnitTest`）把唯一的已删过滤条件 `ui/dialog/UpdateDialogLayoutTest` 换成现存的 `ui/dialog/HomeMenuDialogSourceTest`。

## 验证（文件级，2026-11-06）

- `grep` 工作流文件：5 个已删类名出现 0 次；YAML 解析通过。
- 每个保留/新增引用的包声明逐一核对：`setting/AppBrandingContractTest`、`utils/GithubProxyTest`、`ui/dialog/AboutDialogLayoutTest`、`ui/adapter/VodAdapterLayoutTest`、`ui/dialog/HomeMenuDialogSourceTest` 均存在且包名与引用一致；`danmaku.*` 下有多个现存测试文件。
- 未跑两条 Gradle 测试命令：2026-11-06 上午后台准备时两次尝试均在 settings 求值阶段失败（foojay 插件找不到，`/root/.gradle/caches/modules-2` 只剩 44K，缓存丢了），与本次改动无关；改动只涉及 `--tests` 行。工作流目前按用户决定不再触发，因此不补跑全量构建。

## 推送状态

- 本地 main 待推送：`7c752ed`（改名冉神影视、删更新子系统）、`eca2f14`（包名改为 com.ranshen.cc）、本次提交（修工作流测试引用）。
- 推送方式核查（2026-11-06）：远端是 HTTPS，无 credential helper；`gh` 未登录；SSH 到 github.com:22 超时；GitHub 连接器有 `push_files` 但快照推送不支持删除/二进制/超 2000 文件/超 16 MiB，且本次提交含约 40 个文件删除、仓库 4170 个文件，不适用；按连接器文档不另起 PAT/token 流程。因此本次无法从虚拟机完成 `git push`，需用户决定凭证方式。

## 任务清单

- [x] 修工作流测试引用（文件级验证通过）
- [ ] 提交 + recovery tag
- [ ] 推送到 GitHub（待用户决定凭证方式）
