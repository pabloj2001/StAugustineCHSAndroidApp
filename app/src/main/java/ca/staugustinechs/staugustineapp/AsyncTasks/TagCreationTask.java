package ca.staugustinechs.staugustineapp.AsyncTasks;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.AsyncTask;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import java.util.HashMap;
import java.util.Map;

import ca.staugustinechs.staugustineapp.AppUtils;
import ca.staugustinechs.staugustineapp.Fragments.TitanTagFragment;

public class TagCreationTask extends AsyncTask<String, Void, Bitmap> {

    private Bitmap logo;
    private TitanTagFragment tagFragment;
    private boolean finished = false;

    public TagCreationTask(Bitmap logo, TitanTagFragment tagFragment){
        this.logo = logo;
        this.tagFragment = tagFragment;
    }

    @Override
    protected Bitmap doInBackground(String... strings) {
        return createCode(strings[0]);
    }

    private Bitmap createCode(String content){
        try {
            //System.out.println(TitanTagEncryption.decrypt(content));

            Map<EncodeHintType, ErrorCorrectionLevel> hints = new HashMap<>();
            hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);

            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix bitMatrix = qrCodeWriter.encode(content, BarcodeFormat.QR_CODE, 1000, 1000, hints);

            int width = bitMatrix.getWidth();
            int height = bitMatrix.getHeight();
            Bitmap bmp = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
            int startY = (bmp.getHeight() - logo.getHeight()) / 2;
            int startX = (bmp.getWidth() - logo.getWidth()) / 2;

            int[] pixels = new int[width * height];
            if (tagFragment != null && !tagFragment.isDetached() && tagFragment.isVisible()) {
                int accent = AppUtils.ACCENT_COLOR;
                for (int y = 0; y < height; y++) {
                    for (int x = 0; x < width; x++) {
                        if (AppUtils.SHOW_LOGO_IN_TT && (y >= startY && x >= startX) &&
                                (y < startY + logo.getHeight() && x < startX + logo.getWidth())) {
                            int pixel = logo.getPixel(x - startX, y - startY);
                            if (pixel != Color.TRANSPARENT) {
                                pixels[(y * width) + x] = pixel;
                            } else {
                                pixels[(y * width) + x] = bitMatrix.get(x, y) ? accent : Color.WHITE;
                            }
                        } else {
                            pixels[(y * width) + x] = bitMatrix.get(x, y) ? accent : Color.WHITE;
                        }
                    }
                }
                bmp.setPixels(pixels, 0, width, 0, 0, width, height);
            }

            return bmp;
        } catch (WriterException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    protected void onPostExecute(Bitmap bitmap) {
        finished = true;
        if(!this.isCancelled()){
            tagFragment.updateTag(bitmap);
        }
    }

    public boolean isFinished() {
        return finished;
    }
}
