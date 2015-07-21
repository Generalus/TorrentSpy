package ru.thesn.torrentspy.app.tools;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.squareup.picasso.*;
import ru.thesn.torrentspy.app.R;

import java.util.ArrayList;
import java.util.List;

public class BasicListAdapter extends AbstractListAdapter<BasicListAdapter.Entity, BasicListAdapter.ViewHolder> {


    private final LayoutInflater mInflater;
    private       OnItemClickListener mOnItemClickListener;

    public BasicListAdapter(Context context) {
        mInflater = LayoutInflater.from(context);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        return new ViewHolder(
                mInflater.inflate(R.layout.section_item, viewGroup, false)
        );
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int position) {
        viewHolder.bind(mData.get(position));
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        mOnItemClickListener = listener;
    }

    public static class Entity {
        private final String id;
        private String ruName;
        private String enName;
        private String description;
        private String year;
        private String season;
        private String episode;
        private String quality;
        private String ruLocale;
        private String goodSound;
        private String notify;
        private String status;
        private String imageURL;
        private String type;
        private List<String> releaseGroups = new ArrayList<>();
        private List<String> soundStudios = new ArrayList<>();

        public Entity(String id) {
            this.id = id;
        }

        public String getId() {
            return id;
        }

        public String getRuName() {
            return ruName;
        }

        public void setRuName(String ruName) {
            this.ruName = ruName;
        }

        public String getEnName() {
            return enName;
        }

        public void setEnName(String enName) {
            this.enName = enName;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public String getYear() {
            return year;
        }

        public void setYear(String year) {
            this.year = year;
        }

        public String getSeason() {
            return season;
        }

        public void setSeason(String season) {
            this.season = season;
        }

        public String getEpisode() {
            return episode;
        }

        public void setEpisode(String episode) {
            this.episode = episode;
        }

        public String getQuality() {
            return quality;
        }

        public void setQuality(String quality) {
            this.quality = quality;
        }

        public String getRuLocale() {
            return ruLocale;
        }

        public void setRuLocale(String ruLocale) {
            this.ruLocale = ruLocale;
        }

        public String getGoodSound() {
            return goodSound;
        }

        public void setGoodSound(String goodSound) {
            this.goodSound = goodSound;
        }

        public String getNotify() {
            return notify;
        }

        public void setNotify(String notify) {
            this.notify = notify;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public List<String> getReleaseGroups() {
            return releaseGroups;
        }

        public void setReleaseGroups(List<String> releaseGroups) {
            this.releaseGroups = releaseGroups;
        }

        public List<String> getSoundStudios() {
            return soundStudios;
        }

        public void setSoundStudios(List<String> soundStudios) {
            this.soundStudios = soundStudios;
        }

        public String getImageURL() {
            return imageURL;
        }

        public void setImageURL(String imageURL) {
            this.imageURL = imageURL;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getResultStringFromList(List<String> list){
            String result = "";
            for(String s: list){
                result = result + s + ", ";
                Log.i("DEV_", "элемент" + s);
            }
            if (result.endsWith(", ")) result = result.substring(0, result.length() - 2);
            return result;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Entity entity = (Entity) o;

            if (id != null ? !id.equals(entity.id) : entity.id != null)
                return false;

            return true;
        }

        @Override
        public int hashCode() {
            return id != null ? id.hashCode() : 0;
        }
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView mTextView;
        private final CardView cardView;
        private Entity   mEntity;
        private final View view;


        public ViewHolder(View v) {
            super(v);
            mTextView = (TextView)v.findViewById(R.id.label);
            cardView = (CardView) v.findViewById(R.id.cv);
            view = v;

            cardView.setCardBackgroundColor(Color.TRANSPARENT);
            cardView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mOnItemClickListener != null) {
                        mOnItemClickListener.onItemClick(mEntity);
                    }
                }
            });
        }

        public void bind(final Entity entity) {
            mEntity = entity;

            final LinearLayout layout = (LinearLayout) (view.findViewById(R.id.item_lin_layout));
            layout.setVisibility(View.INVISIBLE);

            if(entity.getStatus() != null && !entity.getStatus().equals("0")) {
                mTextView.setText("Доступно на " + entity.getStatus() + "!");
                ((ImageView) (view.findViewById(R.id.item_img_status))).setImageResource(R.drawable.ic_thumb_up_white_36dp);
                layout.setVisibility(View.VISIBLE);
            }

            cardView.setVisibility(View.INVISIBLE);

            final ImageView imageView = (ImageView) view.findViewById(R.id.person_photo);
            final LinearLayout layout1 = (LinearLayout)view.findViewById(R.id.item_lin_layout_top);
            layout1.setVisibility(View.INVISIBLE);
            final TextView textView = (TextView)view.findViewById(R.id.item_title);

            String splash = "";
            if (!entity.getEnName().equals("") && !entity.getRuName().equals("")) splash = " / ";
            textView.setText(((!entity.getEnName().equals("")) ? entity.getEnName() : "") + splash +
                            ((!entity.getRuName().equals("")) ? entity.getRuName() : "") +
                           ((entity.getYear() != null && !entity.getYear().equals("")) ? " (" + entity.getYear() + ")" : ""));


            layout.getBackground().setAlpha(200);


            Callback callBack = new Callback() {
                @Override
                public void onSuccess() {

                    Bitmap bitmap = ((BitmapDrawable) imageView.getDrawable()).getBitmap();
                    Palette.Builder builder = Palette.from(bitmap);
                    Palette palette = builder.generate();
                    int mainColor = palette.getMutedColor(palette.getVibrantColor(0));
                    layout.setBackgroundColor(mainColor);
                    layout.getBackground().setAlpha(200);
                    layout1.setBackgroundColor(mainColor);
                    layout1.getBackground().setAlpha(200);
                    Log.i("DEV_", "Картинка успешно отображена!");
                    cardView.setVisibility(View.VISIBLE);

                }

                @Override
                public void onError() {
                    Bitmap bitmap = ((BitmapDrawable) imageView.getDrawable()).getBitmap();
                    Palette.Builder builder = Palette.from(bitmap);
                    Palette palette = builder.generate();
                    int mainColor = palette.getDarkMutedColor(palette.getVibrantColor(0));
                    layout.setBackgroundColor(mainColor);
                    layout.getBackground().setAlpha(200);
                    layout1.setBackgroundColor(mainColor);
                    layout1.getBackground().setAlpha(200);
                    layout1.setVisibility(View.VISIBLE);
                    Log.i("DEV_", "Ошибка при загрузке картинки! url: " + entity.getImageURL());
                    cardView.setVisibility(View.VISIBLE);
                }
            };

            if (entity.getImageURL() == null || entity.getImageURL().equals("")) entity.setImageURL("none");

            Picasso
                    .with(view.getContext())
                    .load(entity.getImageURL())
                    .resize(400, 500)
                    .placeholder(R.drawable.default_movie)
                    .centerInside()
                    .networkPolicy(NetworkPolicy.OFFLINE)
                    .into(imageView, callBack);

        }

    }

    public interface OnItemClickListener {
        void onItemClick(Entity entity);
    }

    public BasicListAdapter.Entity getEntityByID(String id){
        for(BasicListAdapter.Entity entity: mData){
            if (entity.getId().equals(id)) return entity;
        }
        return null;
    }
}