package smaillns.smail.dzair.utility;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import smaillns.smail.dzair.R;

import smaillns.smail.dzair.entity.Spot;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.imageaware.ImageViewAware;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

import java.util.ArrayList;

public class MapInfoWindowAdapter implements GoogleMap.InfoWindowAdapter {

    private LayoutInflater inflater;
    private Context context;
    private Spot mSpot;
    static private ArrayList<Spot> spotList = new ArrayList<Spot>();

    private ImageLoader mImageLoader ;
    private DisplayImageOptions mDisplayImageOptions;
    private ImageLoadingListener mImageLoadingListener;

    public MapInfoWindowAdapter(Context context, Spot spot,
                                ImageLoader mImageLoader, DisplayImageOptions mDisplayImageOptions,
                                ImageLoadingListener mImageLoadingListener
                                ){
        inflater = (LayoutInflater) context.getSystemService( Context.LAYOUT_INFLATER_SERVICE );
        this.context = context;
        this.mSpot = spot;
        spotList.add(spot);

        this.mImageLoader = mImageLoader;
        this.mDisplayImageOptions = mDisplayImageOptions;
        this.mImageLoadingListener = mImageLoadingListener;

//        mImageLoader.init(ImageLoaderConfiguration.createDefault(context));
//
//        // image caching options
//        mDisplayImageOptions = new DisplayImageOptions.Builder()
//                .showImageOnLoading(android.R.color.transparent)
//                .showImageForEmptyUri(R.drawable.ic_location_on_black_24dp)
//                .showImageOnFail(R.drawable.ic_location_on_black_24dp)
//                .cacheInMemory(true)
//                .cacheOnDisk(true)
//                .displayer(new SimpleBitmapDisplayer())
//                .build();
//        mImageLoadingListener = new AnimateImageLoadingListener();

    }

    @Override
    public View getInfoWindow(Marker marker) {
        // Getting view from the layout file
        View v = inflater.inflate(R.layout.info_marker_layout, null);

        ImageView iv = (ImageView) v.findViewById(R.id.markerImage);
        TextView title = (TextView) v.findViewById(R.id.nomMarker);
        for (Spot spt : spotList){
            //Toast.makeText(context, ""+ spt.getName(), Toast.LENGTH_SHORT).show();
            if (spt.getName().equals(marker.getTitle())){
                title.setText(spt.getName());

                //iv.setImageResource();
//                mImageLoader.displayImage("assets://"+spt.getImage(), iv, mDisplayImageOptions, mImageLoadingListener);

                ImageViewAware imageAware = new ImageViewAware(iv, false);
//                mImageLoader.displayImage(RetrofitService.baseUrl+"/"+myDataset[position].image, imageAware,mDisplayImageOptions, mImageLoadingListener)

                mImageLoader.displayImage("https://algiers-city-guide.appspot.com"+"/"+spt.getImage(), imageAware, mDisplayImageOptions, mImageLoadingListener);

                //mImageLoader.displayImage("assets://"+spt.getImage(), iv);
                break;
            }
        }






        return v;
    }

    @Override
    public View getInfoContents(Marker marker) {
        return null;
    }

}