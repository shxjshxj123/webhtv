package com.fongmi.android.tv.ui.dialog;

import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class AboutDialogLayoutTest {

    @Test
    public void aboutDialogWindowWrapsItsContentInsteadOfForcingFullHeight() throws Exception {
        String source = read(findMainJavaPath().resolve(Path.of("com", "fongmi", "android", "tv", "ui", "dialog", "AboutDialog.java")));

        assertTrue("About dialog window height should wrap its content so short text does not float above the buttons",
                source.contains("params.height = WindowManager.LayoutParams.WRAP_CONTENT;"));
        assertFalse("About dialog must not force a near-full-screen window height",
                source.contains("getDialogHeight"));
    }

    @Test
    public void aboutDialogUsesOneScrollableDialogShell() throws Exception {
        String source = read(findMainJavaPath().resolve(Path.of("com", "fongmi", "android", "tv", "ui", "dialog", "AboutDialog.java")));
        String layout = read(findMainResPath().resolve(Path.of("layout", "dialog_about.xml")));

        assertTrue("About dialog should install its styled content directly into the window",
                source.contains("dialog.setContentView(binding.getRoot());"));
        assertFalse("About dialog content must not be wrapped in a second padded dialog shell",
                source.contains("LightDialog.create(activity, null, binding.getRoot())"));
        assertTrue("About dialog root should wrap its content so the window can size to the disclaimer",
                layout.contains("android:layout_height=\"wrap_content\""));
        assertTrue("About dialog should set maxHeight dynamically in code based on screen size",
                source.contains("binding.contentScroll.setMaxHeight(maxScrollHeight);"));
        String contentScroll = layout.substring(layout.indexOf("android:id=\"@+id/contentScroll\""));
        assertFalse("About dialog should not hardcode maxHeight in XML — it must adapt to screen size",
                contentScroll.contains("app:maxHeight="));
    }

    @Test
    public void aboutDialogConstrainsScrollableContentBeforeFinalWindowSizing() throws Exception {
        String source = read(findMainJavaPath().resolve(Path.of("com", "fongmi", "android", "tv", "ui", "dialog", "AboutDialog.java")));

        int constrainScroll = source.indexOf("binding.contentScroll.setMaxHeight(maxScrollHeight);");
        int remeasureRoot = source.indexOf("binding.getRoot().measure(widthSpec, heightSpec);", constrainScroll);
        int finalWindowLayout = source.lastIndexOf("window.setLayout(params.width, dialogHeight);");

        assertTrue("The disclaimer must be constrained before the complete dialog is measured again",
                constrainScroll >= 0 && remeasureRoot > constrainScroll);
        assertTrue("The final window height must use the post-constraint root measurement so action buttons stay visible",
                finalWindowLayout > remeasureRoot);
        assertTrue("When fixed dialog chrome is tall, the dialog should sacrifice outer margin instead of clipping actions",
                source.contains("Math.min(availableHeight, Math.max(chromeHeight + 1, availableHeight - ResUtil.dp2px(DIALOG_VERTICAL_MARGIN_DP)))"));
        assertFalse("A 200dp scroll minimum can exceed the remaining TV height and push all action buttons outside the window",
                source.contains("Math.max(ResUtil.dp2px(200), availableHeight - chromeHeight"));
    }

    @Test
    public void lightAlertDialogWidthUsesPhysicalWidthAndHeight() {
        assertEquals(998, LightDialog.resolveAlertWidth(1920, 1080));
        assertEquals(1120, LightDialog.resolveAlertWidth(2560, 1080));
        assertEquals(972, LightDialog.resolveAlertWidth(1080, 1920));
    }

    @Test
    public void lightAlertDialogListHeightTracksPhysicalScreenHeight() {
        assertEquals(626, LightDialog.resolveAlertListMaxHeight(1080));
        assertEquals(418, LightDialog.resolveAlertListMaxHeight(720));
        assertEquals(886, LightDialog.resolveAlertWindowMaxHeight(1080));
        assertEquals(590, LightDialog.resolveAlertWindowMaxHeight(720));
        assertEquals(698, LightDialog.resolveAlertWindowHeight(496, 278, 480, 886));
        assertEquals(506, LightDialog.resolveAlertWindowHeight(496, 278, 288, 886));
        assertEquals(886, LightDialog.resolveAlertWindowHeight(700, 300, 700, 886));
    }

    @Test
    public void aboutDialogFillsTheScreenOnTvButKeepsWrapContentOnPhones() throws Exception {
        String source = read(findMainJavaPath().resolve(Path.of("com", "fongmi", "android", "tv", "ui", "dialog", "AboutDialog.java")));

        // 电视端走独立的全屏分支，手机端保持原有 wrap_content 行为不变。
        assertTrue("TV should take a dedicated fullscreen path",
                source.contains("if (Util.isLeanback()) return configureFullscreenWindow(window, binding);"));
        assertTrue("The scroll area should absorb leftover space so the action buttons stay pinned",
                source.contains("weight = 1"));
        assertTrue("Phones must keep sizing the window to their content",
                source.contains("params.height = WindowManager.LayoutParams.WRAP_CONTENT;"));
    }


    private static String read(Path path) throws Exception {
        return new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
    }

    private static Path findMainJavaPath() {
        Path moduleRelative = Path.of("src", "main", "java");
        if (Files.exists(moduleRelative)) return moduleRelative;
        return Path.of("app", "src", "main", "java");
    }

    private static Path findMainResPath() {
        Path moduleRelative = Path.of("src", "main", "res");
        if (Files.exists(moduleRelative)) return moduleRelative;
        return Path.of("app", "src", "main", "res");
    }

}
