# 品牌改名 + 删除应用内更新

## Recovery anchor

- 目标：应用显示名由“默影视”改为“冉神影视”；删除设置关于页的检查更新 / 加速源 / 更新设置入口与全部应用内更新逻辑（含开机自动更新开关与更新源 / 代理 / OCI 设置）；验证打包通过后提交并打 recovery tag。
- 任务 ID：`brand-rename-remove-update`；Lane：standard；分支：`main`，HEAD `eab79fb`；任务开始时工作树干净，无需保护的脏文件。
- 开始时间：2026-11-05 约 17:45 CST（Asia/Shanghai）。
- 预计：改名约 10 分钟；删除更新子系统约 60–80 分钟；验证（leanback debug 打包 + mobile 编译 + 相关单测）约 20–30 分钟；提交 / tag / 新 APK 上传 Drive 约 10 分钟；总计约 1 小时 45 分钟–2 小时，预计 19:30–19:45 CST 完成。
- 最便宜的决定性验证：`assembleLeanbackArm64_v8aDebug` + `compileMobileArm64_v8aDebugJavaWithJavac` + `AppBrandingContractTest` 与剩余的 `AboutDialogLayoutTest`。

## 关键调查结论（2026-11-05）

- “默影视”只出现在：`app_name`（`values` / `values-zh-rCN` / `values-zh-rTW`，繁体为“默影視”）、`AppBranding.java:55` 注释、`AppBrandingContractTest.java` 注释与断言。其他“默”均为“默认”，不动。包名 `com.silent.android.webhtv` 不变（改包名会断签名与更新身份，不在本次范围）。
- 代码里没有 `BOOT_COMPLETED` 接收器，也没有任何开机自动调用 `Updater.start()` 的位置。所谓“开机自动更新”就是 `Setting` 的 `update` 开关（默认 true）+ 关于页手动检查入口；删掉整个更新子系统后，开机自动更新即不存在。
- 关于页更新入口只有两处调用点：leanback `SettingActivity.onVersion` 与 mobile `SettingFragment.onVersion`，均调 `AboutDialog.show(activity, () -> Updater.create().force().start(...))`。
- `Updater.create().resume(this)` 只出现在 leanback / mobile 的 `BaseActivity.onResume`，仅恢复进行中的下载弹窗，随更新子系统一并删除。
- `utils/GithubProxy` 保留：`NodeLib` 与 `WebHomeExtension` 用它下载非更新资源；只删关于页的加速源入口（弹窗 + adapter + 布局）。因此 `Setting.getGithubProxy / putGithubProxy / isGithubProxyEnabled / putGithubProxyEnabled` 与备份键 `github_proxy` / `github_proxy_enabled` 保留。
- `utils/Github` 只被 `Updater` 使用，随子系统删除。
- 字符串：三地 `strings.xml` 各有 66 个相关串（`about_check_update` + 9 个 `setting_github_proxy*` + 56 个 `update*`）；其中 `update_check` 被 `WebHomeExtensionDialog` 用于非更新功能，保留，其余 65 个删除。
- `shape_lab_update_btn` 无引用，不动（闭合范围）。`selector_update_version_item` 仅被两处 `dialog_update.xml` 引用，随布局删除。

## 删除清单

- main java：`Updater.java`、`bean/Update.java`、`impl/UpdateListener.java`、`utils/Github.java`、`ui/dialog/UpdateSettingsDialog.java`、`ui/adapter/GithubProxyAdapter.java`、`update/` 全部 13 个文件。
- main res：`layout/dialog_update_settings.xml`、`layout/dialog_github_proxy.xml`、`layout/adapter_github_proxy.xml`、`drawable/selector_update_version_item.xml`。
- leanback：`ui/dialog/UpdateDialog.java`、`ui/dialog/UpdateDialogLayout.java`、`res/layout/dialog_update.xml`。
- mobile：`ui/dialog/UpdateDialog.java`、`res/layout/dialog_update.xml`、`res/layout/adapter_github_proxy.xml`。
- 测试（整文件删除）：`UpdateChannelTest`、`UpdaterFutureTest`、`update/OciAuthChallengeTest`、`update/OciRegistryClientTest`、`update/UpdateRoutePlannerTest`、testLeanback `UpdateDialogLayoutTest`、testMobile `UpdateDialogChannelVisibilityTest`。
- 测试（编辑）：`AboutDialogLayoutTest` 仅删 4 个 GitHub 代理相关测试，其余关于 About 弹窗外壳的测试保留。

## 编辑清单

- 改名：三地 `strings.xml` 的 `app_name`（繁体用“冉神影視”）、`AppBranding.java:55` 注释、`AppBrandingContractTest.java` 注释与断言。
- `AboutDialog.java`：`show(activity)` 去掉 `updateAction` 参数，删检查更新 / 加速源 / 更新设置三处入口及 `showGithubProxy` / `refreshGithubProxy` / `configureGithubProxyWindow`；保留版本、确认、滚动与窗口逻辑。
- `dialog_about.xml`：删检查更新 + 加速源按钮行与更新设置齿轮按钮，保留确认按钮。
- `SettingActivity.onVersion` / `SettingFragment.onVersion`：改为 `AboutDialog.show(...)` 无回调。
- 两处 `BaseActivity`：删 `Updater.create().resume(this)` 及 import。
- `Setting.java`：删 `getUpdate / putUpdate`、`getUpdateSource / putUpdateSource`、`getUpdateGithubProxy* / put*`、`getUpdateOciMirror* / put*` 及 `update` 包 import；保留 GitHub 代理通用方法。
- `Backup.java`：`APP_PREFS` 删 `"update"`；删 `update_source / update_github_proxy* / update_oci_mirror*` 键集合。
- 三地 `strings.xml`：删上述 65 个串，保留 `update_check`。

## 不动

- `.github/workflows/android-release.yml` 与发布流程（只删应用内更新器，不动发版）。
- `utils/GithubProxy`、`NodeLib`、`WebHomeExtension`、备份键 `github_proxy` / `github_proxy_enabled`。
- 设计研究门（AGENTS.md §7）：豁免——无新功能、无架构变化，是用户明确下令的删除与改名，局部收窄。

## 状态

- [x] 调查完成
- [x] 改名完成
- [x] 删除更新子系统完成
- [x] 验证通过（leanback 打包 + mobile 编译 + 相关单测）——2026-11-05 18:06 CST，BUILD SUCCESSFUL（58s）；AppBrandingContractTest 11/11 通过，AboutDialogLayoutTest 6/6 通过；APK `app-leanback-arm64_v8a-debug.apk` 184,924,796 字节，aapt2 验证标签 冉神影视，包名 com.silent.android.webhtv，5.6.0 / 560。
- [ ] 提交 + recovery tag + 新 APK 上传 Drive

## 验证结果与构建排障（2026-11-05）

- Chaquo pip 407 的根因：Gradle daemon 启动时继承的 egress 代理 token 会过期轮换，daemon 里是旧 token（与当前 shell 的 token 哈希不同），pip 连索引 407。修复：构建前 `gradle --stop` 再重跑，让新 daemon 继承当前 token；Chaquo 步骤随即通过。另：`chaquopy.pip_install` 会禁用 pip 配置文件（`PIP_CONFIG_FILE=/dev/null`），且显式 `--extra-index-url` 会让 `PIP_NO_INDEX` 不生效；本地 wheelhouse 的 Android wheel 只有带 `--platform` 的 Chaquo pip 才能接受。
- 构建中途补删/补改了两个遗漏的测试：`utils/GithubTest.java` 整文件删除（测的是已删的 `Github` 类）；`utils/GithubProxyTest.java` 仅 `applyPrefixesFixedUpdateChannelUrl` 一处把 `Github.getChannelAsset(...)` 换成字面 URL（该字面值即原 GithubTest 断言的 `https://github.com/Silent1566/webhtv/releases/download/update-channel/mobile-arm64_v8a-beta.json`），其余 4 个测保留的 `GithubProxy` 的测试不动。
- 最终 diff：53 个文件，13 行新增，5012 行删除。
- 任务范围补记：验证中补删的 `utils/GithubTest.java` 与补改的 `utils/GithubProxyTest.java` 原不在声明范围，`task_guard.sh finish` 曾因范围越界拒绝提交；已把这两条路径追加进 guard 的 scope 文件（共 39 条），`task_guard.sh check` 通过。

## 下一步

- 跑 `task_guard.sh finish` 提交并打 tag，再把新 APK 传到 Drive。
