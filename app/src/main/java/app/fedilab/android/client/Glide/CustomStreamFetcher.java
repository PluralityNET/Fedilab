package app.fedilab.android.client.Glide;

import android.content.Context;
import android.support.annotation.NonNull;

import com.bumptech.glide.Priority;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.data.DataFetcher;
import com.bumptech.glide.load.model.GlideUrl;

import java.io.InputStream;
import java.lang.ref.WeakReference;

import app.fedilab.android.client.HttpsConnection;
import app.fedilab.android.helper.Helper;

/**
 * Created by Thomas on 13/12/2017.
 * Custom stream fetcher which will use getPicture from HttpsConnection to get the inputstream
 */

public class CustomStreamFetcher implements DataFetcher<InputStream> {

    private GlideUrl url;
    private WeakReference<Context> contextWeakReference;

    CustomStreamFetcher(Context context, GlideUrl url) {
        this.contextWeakReference = new WeakReference<>(context);
        this.url = url;
    }

    @Override
    public void loadData(@NonNull Priority priority, @NonNull DataCallback<? super InputStream> callback) {
        String instance = Helper.getLiveInstance(this.contextWeakReference.get());
        callback.onDataReady(new HttpsConnection(this.contextWeakReference.get(), instance).getPicture(url.toStringUrl()));
    }

    @Override
    public void cleanup() {

    }

    @Override
    public void cancel() {

    }

    @NonNull
    @Override
    public Class<InputStream> getDataClass() {
        return InputStream.class;
    }

    @NonNull
    @Override
    public DataSource getDataSource() {
        return DataSource.REMOTE;
    }
}
