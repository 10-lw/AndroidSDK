package com.william.androidsdk.widget.armdialog.manager;

import android.content.Context;
import android.view.View;
import android.widget.TextView;

import com.william.androidsdk.R;
import com.william.androidsdk.widget.armdialog.ArmDialog;
import com.william.androidsdk.widget.armdialog.IDialog;

public class ArmMessageChangeableDialog {

    private static ArmDialog dialog;

    public static void createLoadingDialog(Context context, final String loadingText) {
        ArmDialog.Builder builder = new ArmDialog.Builder(context);
        dialog = builder
                .setWindowBackgroundP(0.5f)
                .setCancelableOutSide(false)
                .setCancelable(false)
                .show();
    }
}
