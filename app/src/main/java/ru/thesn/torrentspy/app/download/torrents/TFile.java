package ru.thesn.torrentspy.app.download.torrents;

import android.util.Log;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import ru.thesn.torrentspy.app.tools.BasicListAdapter;

import java.util.*;


public class TFile implements TorrentSite {
    public static final String NAME = "TFile";
    public static final String URL = "http://tfile.me/rss/?q=";
    public static final String CHECK_TEXT = "tfile.me";

    private Set<String> imageBlackList = new HashSet<>();

    public static String newImageURL = "";
    private boolean isServerAvailable = true;
    private long lastTime = System.currentTimeMillis();

    public boolean isServerAvailable() {
        return isServerAvailable;
    }

    public long getLastTime() {
        return lastTime;
    }

    public TFile(){
        imageBlackList.add("1pic.org");
    }

    public boolean find(BasicListAdapter.Entity entity){
        boolean result = false;

        try {

            String name = !entity.getEnName().equals("") ? entity.getEnName() : entity.getRuName();

            Document doc = Jsoup.connect(URL + name).followRedirects(true).get();
            lastTime = System.currentTimeMillis();

            isServerAvailable = doc.outerHtml().contains(CHECK_TEXT);
            Log.w("DEV_", NAME + " доступен: " + isServerAvailable());
            lastTime = System.currentTimeMillis();
            if (!isServerAvailable()) return false;

            Elements entries = doc.select("item");
            int count = 0;
            Log.w("DEV_", "Всего элементов в таблице: " + entries.size());
            for(Element entry: entries){
                Log.w("DEV_", "Запись " + count++);
                String title = entry.select("title").first().outerHtml();
                String description = entry.select("description").first().outerHtml();
                String titleInLowerCase = title.toLowerCase();
                String descriptionInLowerCase = description.toLowerCase();
                if (!entity.getEnName().equals(""))
                    if (!descriptionInLowerCase.contains(entity.getEnName().toLowerCase())) continue;

                if (!entity.getRuName().equals(""))
                    if (!descriptionInLowerCase.contains(entity.getRuName().toLowerCase())) continue;

                if (entity.getType().equals("game")){
                    if (!descriptionInLowerCase.contains(" pc ") && !descriptionInLowerCase.contains("windows")) continue;

                    if (!entity.getYear().equals(""))
                        if (!title.contains(entity.getYear())) continue;

                    // условий выше уже достаточно для загрузки картинки

                    if ((entity.getImageURL() == null || entity.getImageURL().equals("") || entity.getImageURL().equals("none")) && newImageURL.equals("")) {
                        Document document = Jsoup.parse(entry.outerHtml().replace("<![CDATA[", "").replace("]]>", "").replaceAll("&lt;","<").replaceAll("&gt;",">"));
                        Elements posters = document.select(".postImgAligned");
                        if (posters.size() > 0) {
                            boolean blackFlag = false;
                            String imgUrl = posters.first().attr("src");
                            for(String black: imageBlackList) {
                                if (imgUrl.contains(black)) blackFlag = true;
                            }
                            if (!blackFlag) newImageURL = posters.first().attr("src");
                        }
                    }


                    if (entity.getReleaseGroups().size() > 0) {
                        boolean flag = true;
                        for (String group : entity.getReleaseGroups())
                            if (titleInLowerCase.contains(group.toLowerCase())) {
                                flag = false;
                                break;
                            }
                        if (flag) continue;
                    }

                    if (entity.getRuLocale().equals("1"))
                        if (!descriptionInLowerCase.contains("русский") && !descriptionInLowerCase.contains("rus")) continue;

                }

                if (entity.getType().equals("movie")){
                    if (!entity.getYear().equals(""))
                        if (!description.contains(entity.getYear())) continue;


                    String[] qualities = {"bdrip", "hdrip", "satrip", "telesync", "dvdrip", "imax", "dvd5", "dvd9", "hdtvrip", "web-dl", "webdl", "camrip", " ts ", "dvdscr"};
                    boolean fl = false;
                    for(String q: qualities)
                        if (descriptionInLowerCase.contains(q)) {
                            fl = true;
                            break;
                        }

                    if (!fl) continue;


                    if ((entity.getImageURL() == null || entity.getImageURL().equals("") || entity.getImageURL().equals("none")) && newImageURL.equals("")) {
                            Document document = Jsoup.parse(entry.outerHtml().replace("<![CDATA[", "").replace("]]>", "").replaceAll("&lt;", "<").replaceAll("&gt;", ">"));
                            Elements posters = document.select(".postImgAligned");
                        if (posters.size() > 0) {
                            boolean blackFlag = false;
                            String imgUrl = posters.first().attr("src");
                            for(String black: imageBlackList)
                                if (imgUrl.contains(black)) blackFlag = true;
                            if (!blackFlag) newImageURL = posters.first().attr("src");
                        }

                    }

                    if (!entity.getQuality().equalsIgnoreCase("Любое") && !entity.getQuality().equals(""))
                        if (!descriptionInLowerCase.contains("bdrip") && !descriptionInLowerCase.contains(entity.getQuality().toLowerCase())) continue;

                    if (entity.getGoodSound().equals("1"))
                        if (!descriptionInLowerCase.contains("лицензия")) continue;

                }

                if (entity.getType().equals("serial")){

                    if (!descriptionInLowerCase.contains(entity.getSeason() + " сезон")) continue;

                    int i = descriptionInLowerCase.indexOf(" серии из ");
                    if (i == -1) i = descriptionInLowerCase.indexOf(" серия из ");
                    if (i == -1) i = descriptionInLowerCase.indexOf(" серии ");
                    if (i == -1) continue;
                    Log.w("DEV_", "флаг3");
                    if ((entity.getImageURL() == null || entity.getImageURL().equals("") || entity.getImageURL().equals("none")) && newImageURL.equals("")) {
                        Document document = Jsoup.parse(entry.outerHtml().replace("<![CDATA[", "").replace("]]>", "").replaceAll("&lt;", "<").replaceAll("&gt;", ">"));
                        Elements posters = document.select(".postImgAligned");
                        if (posters.size() > 0) {
                            boolean blackFlag = false;
                            String imgUrl = posters.first().attr("src");
                            for(String black: imageBlackList) {
                                if (imgUrl.contains(black)) blackFlag = true;
                            }
                            if (!blackFlag) newImageURL = posters.first().attr("src");
                        }
                    }

                    int left = i - 1;
                    for(int k = 0; k < 3; k++)
                        if (Character.isDigit(descriptionInLowerCase.charAt(left)))
                            left--;
                        else break;
                    try {
                        Log.w("DEV_", "Номер эпизода: " + Integer.parseInt(descriptionInLowerCase.substring(left + 1, i)));
                        if (Integer.parseInt(entity.getEpisode()) > Integer.parseInt(descriptionInLowerCase.substring(left + 1, i))) continue;
                    }catch (Exception e){continue;}


                    if (entity.getSoundStudios().size() > 0) {
                        boolean flag = true;
                        for (String studio : entity.getSoundStudios())
                            if (descriptionInLowerCase.contains(studio.toLowerCase())) {
                                flag = false;
                                break;
                            }
                        if (flag) continue;
                    }
                }


                result = true;
                if (!newImageURL.equals("") || entity.getImageURL().length() > 5) break;
            }

        }catch(Exception e){
            Log.w("DEV_", "Something wrong");
            e.printStackTrace();
            isServerAvailable = false;
        }
        return result;
    }

    @Override
    public String toString() {
        return NAME;
    }
}