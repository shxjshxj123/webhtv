package com.fongmi.android.tv.ui.dialog;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

import androidx.fragment.app.FragmentActivity;

import com.fongmi.android.tv.BuildConfig;
import com.fongmi.android.tv.R;
import com.fongmi.android.tv.databinding.DialogAboutBinding;
import com.fongmi.android.tv.utils.AppVersion;
import com.fongmi.android.tv.utils.ResUtil;
import com.fongmi.android.tv.utils.Util;

public final class AboutDialog {

    private static final int DIALOG_VERTICAL_MARGIN_DP = 96;
    private static final int FULLSCREEN_INSET_DP = 32;

    private AboutDialog() {
    }

    public static void show(FragmentActivity activity) {
        DialogAboutBinding binding = DialogAboutBinding.inflate(LayoutInflater.from(activity));
        binding.version.setText(activity.getString(R.string.about_version, AppVersion.fullName(), BuildConfig.FLAVOR_mode, BuildConfig.FLAVOR_abi));

        Dialog dialog = new Dialog(activity);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(binding.getRoot());
        binding.confirm.setOnClickListener(v -> dialog.dismiss());
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
        configureWindow(activity, dialog, binding);
        binding.confirm.requestFocus();
    }

    /**
     * 电视端铺满全屏，不再手算窗口高度。
     *
     * 手算精确高度需要同时算对屏宽高、content 高度、visibleFrame、systemBars inset、
     * Material 背景 inset 和 chrome 高度，每一项都有设备差异，任何一项出错就裁掉内容底部。
     * 铺满全屏后这些变量全部无关，改由 LinearLayout 分配空间：固定高的按钮区永远保住，
     * 滚动区吃掉剩余空间。
     */
    private static boolean configureFullscreenWindow(Window window, DialogAboutBinding binding) {
        window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        window.getDecorView().setPadding(0, 0, 0, 0);
        WindowManager.LayoutParams params = window.getAttributes();
        params.width = WindowManager.LayoutParams.MATCH_PARENT;
        params.height = WindowManager.LayoutParams.MATCH_PARENT;
        params.gravity = Gravity.CENTER;
        params.dimAmount = 0.58f;
        window.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        window.setAttributes(params);
        // XML 里 root 是 wrap_content，铺满全屏必须显式改成 match_parent，
        // 否则窗口虽然全屏、内容仍只占中间一小块。
        ViewGroup.LayoutParams rootParams = binding.getRoot().getLayoutParams();
        if (rootParams != null) {
            rootParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
            rootParams.height = ViewGroup.LayoutParams.MATCH_PARENT;
            binding.getRoot().setLayoutParams(rootParams);
        }
        // 电视存在 overscan，正文不能贴边。在 XML 既有 padding 之上再加一圈安全边距。
        int inset = ResUtil.dp2px(FULLSCREEN_INSET_DP);
        View root = binding.getRoot();
        root.setPadding(
                root.getPaddingLeft() + inset,
                root.getPaddingTop() + inset,
                root.getPaddingRight() + inset,
                root.getPaddingBottom() + inset);
        // 滚动区在 XML 里是 wrap_content，全屏下要改成加权填充，否则正文短时按钮会飘在中间。
        ViewGroup.LayoutParams scrollParams = binding.contentScroll.getLayoutParams();
        scrollParams.height = 0;
        if (scrollParams instanceof androidx.appcompat.widget.LinearLayoutCompat.LayoutParams) {
            ((androidx.appcompat.widget.LinearLayoutCompat.LayoutParams) scrollParams).weight = 1;
        }
        binding.contentScroll.setLayoutParams(scrollParams);
        // 铺满后不再需要 maxHeight 约束，交回给 weight 决定。
        // CustomNestedScrollView 以 maxHeight > 0 作为启用条件，所以这里必须给 0 而非 MAX_VALUE：
        // MeasureSpec 的尺寸只有 30 位，MAX_VALUE 会溢出到 mode 位上。
        binding.contentScroll.setMaxHeight(0);
        return true;
    }

    private static boolean configureWindow(FragmentActivity activity, Dialog dialog, DialogAboutBinding binding) {
        Window window = dialog.getWindow();
        if (window == null) return false;
        if (Util.isLeanback()) return configureFullscreenWindow(window, binding);
        WindowManager.LayoutParams params = window.getAttributes();
        params.width = (int) (ResUtil.getScreenWidth(activity) * (ResUtil.isLand(activity) ? 0.62f : 0.92f));
        params.height = WindowManager.LayoutParams.WRAP_CONTENT;
        params.gravity = Gravity.CENTER;
        params.dimAmount = 0.58f;
        window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        window.getDecorView().setPadding(0, 0, 0, 0);
        window.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        window.setAttributes(params);

        int availableHeight = activity.findViewById(android.R.id.content).getHeight();
        if (availableHeight == 0) {
            availableHeight = ResUtil.getScreenHeight(activity);
            android.view.WindowInsets insets = window.getDecorView().getRootWindowInsets();
            if (insets != null && android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
                android.graphics.Insets systemBars = insets.getInsets(android.view.WindowInsets.Type.systemBars());
                availableHeight -= (systemBars.top + systemBars.bottom);
            }
        }

        int widthSpec = View.MeasureSpec.makeMeasureSpec(params.width, View.MeasureSpec.EXACTLY);
        int heightSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        binding.contentScroll.getLayoutParams().height = 1;
        binding.getRoot().measure(widthSpec, heightSpec);
        int chromeHeight = binding.getRoot().getMeasuredHeight() - 1;
        binding.contentScroll.getLayoutParams().height = ViewGroup.LayoutParams.WRAP_CONTENT;

        int maxDialogHeight = Math.min(availableHeight, Math.max(chromeHeight + 1, availableHeight - ResUtil.dp2px(DIALOG_VERTICAL_MARGIN_DP)));
        int maxScrollHeight = Math.max(1, maxDialogHeight - chromeHeight);
        binding.contentScroll.setMaxHeight(maxScrollHeight);
        binding.getRoot().measure(widthSpec, heightSpec);
        int dialogHeight = Math.min(binding.getRoot().getMeasuredHeight(), maxDialogHeight);
        window.setLayout(params.width, dialogHeight);
        return true;
    }
}
