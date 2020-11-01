package com.ceit.management.util;

import android.content.Context;
import cn.pedant.SweetAlert.SweetAlertDialog;

public final class DialogUtil
{
    private static SweetAlertDialog swal;

    public static final boolean isDialogShown()
    {
        return (swal != null && swal.isShowing());
    }

    public static final void dismissDialog()
    {
        if(swal != null && swal.isShowing())
            swal.dismissWithAnimation();
        swal = null;
    }

    public static final void successDialog(Context context, String title, String message)
    {
        if(swal != null)
        {
            if(swal.isShowing())
                swal.dismissWithAnimation();
            swal = null;
        }

        swal = new SweetAlertDialog(context, SweetAlertDialog.SUCCESS_TYPE)
                .setTitleText(title)
                .showCancelButton(false)
                .setContentText(message);
        swal.show();
    }

    public static final void errorDialog(Context context, String title, String message)
    {
        if(swal != null)
        {
            if(swal.isShowing())
                swal.dismissWithAnimation();
            swal = null;
        }

        swal = new SweetAlertDialog(context, SweetAlertDialog.ERROR_TYPE)
                    .setTitleText(title)
                    .showCancelButton(false)
                    .setContentText(message);
        swal.show();
    }

    public static final void warningDialog(Context context, String title, String message)
    {
        if(swal != null)
        {
            if(swal.isShowing())
                swal.dismissWithAnimation();
            swal = null;
        }

        swal = new SweetAlertDialog(context, SweetAlertDialog.WARNING_TYPE)
                .setTitleText(title)
                .showCancelButton(false)
                .setContentText(message);
        swal.show();
    }

    public static final void successDialog(Context context, String title, String message, boolean cancelable)
    {
        if(swal != null)
        {
            if(swal.isShowing())
                swal.dismissWithAnimation();
            swal = null;
        }

        swal = new SweetAlertDialog(context, SweetAlertDialog.SUCCESS_TYPE)
                .setTitleText(title)
                .showCancelButton(false)
                .setContentText(message);
        swal.setCancelable(cancelable);
        swal.setCanceledOnTouchOutside(cancelable);
        swal.show();
    }

    public static final void errorDialog(Context context, String title, String message, boolean cancelable)
    {
        if(swal != null)
        {
            if(swal.isShowing())
                swal.dismissWithAnimation();
            swal = null;
        }

        swal = new SweetAlertDialog(context, SweetAlertDialog.ERROR_TYPE)
                .setTitleText(title)
                .showCancelButton(false)
                .setContentText(message);
        swal.setCancelable(cancelable);
        swal.setCanceledOnTouchOutside(cancelable);
        swal.show();
    }

    public static final void warningDialog(Context context, String title, String message, boolean cancelable)
    {
        if(swal != null)
        {
            if(swal.isShowing())
                swal.dismissWithAnimation();
            swal = null;
        }

        swal = new SweetAlertDialog(context, SweetAlertDialog.WARNING_TYPE)
                .setTitleText(title)
                .showCancelButton(false)
                .setConfirmText("")
                .setContentText(message);
        swal.setCancelable(cancelable);
        swal.setCanceledOnTouchOutside(cancelable);
        swal.show();
    }

    public static final void successDialog(Context context, String title, String message, String confirm, String cancel, boolean cancelable)
    {
        if(swal != null)
        {
            if(swal.isShowing())
                swal.dismissWithAnimation();
            swal = null;
        }

        swal = new SweetAlertDialog(context, SweetAlertDialog.SUCCESS_TYPE)
                .setTitleText(title)
                .setContentText(message)
                .setConfirmText(confirm)
                .setCancelText(cancel);
        swal.setCanceledOnTouchOutside(cancelable);
        swal.setCancelable(cancelable);
        swal.show();
    }

    public static final void errorDialog(Context context, String title, String message, String confirm, String cancel, boolean cancelable)
    {
        if(swal != null)
        {
            if(swal.isShowing())
                swal.dismissWithAnimation();
            swal = null;
        }

        swal = new SweetAlertDialog(context, SweetAlertDialog.ERROR_TYPE)
                .setTitleText(title)
                .setContentText(message)
                .setConfirmText(confirm)
                .setCancelText(cancel);
        swal.setCanceledOnTouchOutside(cancelable);
        swal.setCancelable(cancelable);
        swal.show();
    }

    public static final void warningDialog(Context context, String title, String message, String confirm, String cancel, boolean cancelable)
    {
        if(swal != null)
        {
            if(swal.isShowing())
                swal.dismissWithAnimation();
            swal = null;
        }

        swal = new SweetAlertDialog(context, SweetAlertDialog.WARNING_TYPE)
                .setTitleText(title)
                .setContentText(message)
                .setConfirmText(confirm)
                .setCancelText(cancel);
        swal.setCanceledOnTouchOutside(cancelable);
        swal.setCancelable(cancelable);
        swal.show();
    }

    public static final void successDialog(Context context, String title, String message, String confirm, boolean cancelable)
    {
        if(swal != null)
        {
            if(swal.isShowing())
                swal.dismissWithAnimation();
            swal = null;
        }

        swal = new SweetAlertDialog(context, SweetAlertDialog.SUCCESS_TYPE)
                .setTitleText(title)
                .setContentText(message)
                .setConfirmText(confirm);
        swal.setCanceledOnTouchOutside(cancelable);
        swal.setCancelable(cancelable);
        swal.show();
    }

    public static final void errorDialog(Context context, String title, String message, String confirm, boolean cancelable)
    {
        if(swal != null)
        {
            if(swal.isShowing())
                swal.dismissWithAnimation();
            swal = null;
        }

        swal = new SweetAlertDialog(context, SweetAlertDialog.ERROR_TYPE)
                .setTitleText(title)
                .setContentText(message)
                .setConfirmText(confirm);
        swal.setCanceledOnTouchOutside(cancelable);
        swal.setCancelable(cancelable);
        swal.show();
    }

    public static final void warningDialog(Context context, String title, String message, String confirm, boolean cancelable)
    {
        if(swal != null)
        {
            if(swal.isShowing())
                swal.dismissWithAnimation();
            swal = null;
        }

        swal = new SweetAlertDialog(context, SweetAlertDialog.WARNING_TYPE)
                .setTitleText(title)
                .setContentText(message)
                .setConfirmText(confirm);
        swal.setCanceledOnTouchOutside(cancelable);
        swal.setCancelable(cancelable);
        swal.show();
    }

    public static final void progressDialog(Context context, String message, int progressColor, boolean cancelable)
    {
        if(swal != null)
        {
            if(swal.isShowing())
                swal.dismissWithAnimation();
            swal = null;
        }

        swal = new SweetAlertDialog(context, SweetAlertDialog.PROGRESS_TYPE)
                .setTitleText(message);
        swal.setCanceledOnTouchOutside(cancelable);
        swal.setCancelable(cancelable);

        swal.getProgressHelper().setBarColor(progressColor);
        swal.show();
    }

    public static final void successDialog(Context context, String title, String message, String confirm, String cancel, SweetAlertDialog.OnSweetClickListener confirmClick, SweetAlertDialog.OnSweetClickListener cancelClick, boolean cancelable)
    {
        if(swal != null)
        {
            if(swal.isShowing())
                swal.dismissWithAnimation();
            swal = null;
        }

        swal = new SweetAlertDialog(context, SweetAlertDialog.SUCCESS_TYPE)
                .setTitleText(title)
                .setContentText(message)
                .setConfirmText(confirm)
                .setCancelText(cancel)
                .setCancelClickListener(cancelClick)
                .setConfirmClickListener(confirmClick);
        swal.setCanceledOnTouchOutside(cancelable);
        swal.setCancelable(cancelable);
        swal.show();
    }

    public static final void errorDialog(Context context, String title, String message, String confirm, String cancel, SweetAlertDialog.OnSweetClickListener confirmClick, SweetAlertDialog.OnSweetClickListener cancelClick, boolean cancelable)
    {
        if(swal != null)
        {
            if(swal.isShowing())
                swal.dismissWithAnimation();
            swal = null;
        }

        swal = new SweetAlertDialog(context, SweetAlertDialog.ERROR_TYPE)
                .setTitleText(title)
                .setContentText(message)
                .setConfirmText(confirm)
                .setCancelText(cancel)
                .setCancelClickListener(cancelClick)
                .setConfirmClickListener(confirmClick);
        swal.setCanceledOnTouchOutside(cancelable);
        swal.setCancelable(cancelable);
        swal.show();
    }

    public static final void warningDialog(Context context, String title, String message, String confirm, String cancel, SweetAlertDialog.OnSweetClickListener confirmClick, SweetAlertDialog.OnSweetClickListener cancelClick, boolean cancelable)
    {
        if(swal != null)
        {
            if(swal.isShowing())
                swal.dismissWithAnimation();
            swal = null;
        }

        swal = new SweetAlertDialog(context, SweetAlertDialog.WARNING_TYPE)
                .setTitleText(title)
                .setContentText(message)
                .setConfirmText(confirm)
                .setCancelText(cancel)
                .setCancelClickListener(cancelClick)
                .setConfirmClickListener(confirmClick);
        swal.setCanceledOnTouchOutside(cancelable);
        swal.setCancelable(cancelable);
        swal.show();
    }

    public static final void successDialog(Context context, String title, String message, String confirm, SweetAlertDialog.OnSweetClickListener confirmClick, boolean cancelable)
    {
        if(swal != null)
        {
            if(swal.isShowing())
                swal.dismissWithAnimation();
            swal = null;
        }

        swal = new SweetAlertDialog(context, SweetAlertDialog.SUCCESS_TYPE)
                .setTitleText(title)
                .setContentText(message)
                .setConfirmText(confirm)
                .setConfirmClickListener(confirmClick);
        swal.setCanceledOnTouchOutside(cancelable);
        swal.setCancelable(cancelable);
        swal.show();
    }

    public static final void errorDialog(Context context, String title, String message, String confirm, SweetAlertDialog.OnSweetClickListener confirmClick, boolean cancelable)
    {
        if(swal != null)
        {
            if(swal.isShowing())
                swal.dismissWithAnimation();
            swal = null;
        }

        swal = new SweetAlertDialog(context, SweetAlertDialog.ERROR_TYPE)
                .setTitleText(title)
                .setContentText(message)
                .setConfirmText(confirm)
                .setConfirmClickListener(confirmClick);
        swal.setCanceledOnTouchOutside(cancelable);
        swal.setCancelable(cancelable);
        swal.show();
    }

    public static final void warningDialog(Context context, String title, String message, String confirm, SweetAlertDialog.OnSweetClickListener confirmClick, boolean cancelable)
    {
        if(swal != null)
        {
            if(swal.isShowing())
                swal.dismissWithAnimation();
            swal = null;
        }

        swal = new SweetAlertDialog(context, SweetAlertDialog.WARNING_TYPE)
                .setTitleText(title)
                .setContentText(message)
                .setConfirmText(confirm)
                .setConfirmClickListener(confirmClick);
        swal.setCanceledOnTouchOutside(cancelable);
        swal.setCancelable(cancelable);
        swal.show();
    }
}
