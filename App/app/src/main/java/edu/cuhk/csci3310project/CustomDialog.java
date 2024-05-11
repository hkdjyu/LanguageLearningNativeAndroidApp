package edu.cuhk.csci3310project;

import android.app.Activity;
import android.app.Dialog;
import android.graphics.Bitmap;
import android.graphics.drawable.ColorDrawable;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;

public class CustomDialog {
    public void showImageDialog(Activity activity, Bitmap decodedByte){
        final Dialog dialog = new Dialog(activity);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialog.setCancelable(false);
        dialog.setContentView(R.layout.custom_image_dialog);

        ImageView image = (ImageView) dialog.findViewById(R.id.custom_image_dialog_image_view);
        image.setImageBitmap(decodedByte);

        Button dialogButton = (Button) dialog.findViewById(R.id.custom_image_dialog_button);
        dialogButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        dialog.show();

    }
}
