package ru.thesn.torrentspy.app.download.torrents;

import android.util.Log;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import ru.thesn.torrentspy.app.tools.BasicListAdapter;
import java.util.HashMap;
import java.util.Map;


public class RuTor implements TorrentSite {
    public static final String NAME = "RuTor";
    public static final String URL = "http://rutor.org/search/";
    public static final String URL_2 = "http://rutor.org";
    public static final String CHECK_TEXT = "Поиск";
    public static Map<String, String> cookies = new HashMap<>();

    private boolean isServerAvailable = true;
    private long lastTime = System.currentTimeMillis();

    public boolean isServerAvailable() {
        return isServerAvailable;
    }

    public long getLastTime() {
        return lastTime;
    }

    public boolean find(BasicListAdapter.Entity entity){
        boolean result = false;

        try {
            Document doc;
            String name = !entity.getEnName().equals("") ? entity.getEnName() : entity.getRuName();
            if (cookies.isEmpty()) {
                doc = Jsoup
                        .connect(URL + name)
                        .userAgent("Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/42.0.2311.90 Safari/537.36")
                        .referrer("none")
                        .followRedirects(true)
                        .get();
                String html = doc.outerHtml();
                if(!html.contains(CHECK_TEXT)) { // скорее всего сайте включена защита от ddos
                    String tag = "document.cookie";
                    int pos = html.indexOf(tag);
                    String temp = html.substring(pos);
                    String[] arr = temp.split("'");
                    String temp2 = arr[1].replace(" ", "");
                    String[] arr2 = temp2.split(";");
                    for (String s : arr2) {
                        String[] arr3 = s.split("=");
                        cookies.put(arr3[0], arr3[1]);
                    }
                }
            }


            doc = Jsoup
                    .connect(URL + name)
                    .cookies(cookies)
                    .userAgent("Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/42.0.2311.90 Safari/537.36")
                    .referrer("none")
                    .followRedirects(true)
                    .get();

            lastTime = System.currentTimeMillis();

            isServerAvailable = doc.outerHtml().contains(CHECK_TEXT);
            Log.w("DEV_", NAME + " доступен: " + isServerAvailable());
            lastTime = System.currentTimeMillis();
            if (!isServerAvailable()) return false;
            Elements backgr = doc.select(".backgr");
            Elements entries;
            if (backgr.size() == 1)
                entries = backgr.first().parent().select("tr");
            else
                return false;


            Log.w("DEV_", "Всего элементов в таблице: " + entries.size());

            int count = 0;
            for(Element entry: entries){
                if (entry == entries.get(0)) continue; // первый tr - это заголовки таблицы
                Log.w("DEV_", "Запись " + count++);

                Element element = entry.select("td").get(1).select("a").get(2);
                String title = element.text();
                String titleInLowerCase = title.toLowerCase();
                String entryURL = element.attr("href");

                if (!entity.getEnName().equals(""))
                    if (!titleInLowerCase.contains(entity.getEnName().toLowerCase())) continue;
                if (!entity.getRuName().equals(""))
                    if (!titleInLowerCase.contains(entity.getRuName().toLowerCase())) continue;


                if (entity.getType().equals("game")){
                    if (!titleInLowerCase.contains(" pc ")) continue;

                    if (!entity.getYear().equals(""))
                        if (!title.contains(entity.getYear())) continue;


                    if (entity.getReleaseGroups().size() > 0) {
                        boolean flag = true;
                        for (String group : entity.getReleaseGroups())
                            if (titleInLowerCase.contains(group.toLowerCase())) {
                                flag = false;
                                break;
                            }
                        if (flag) continue;
                    }

                    if (entity.getRuLocale().equals("1")) {
                        Document document2 = Jsoup.connect(URL_2 + entryURL).cookies(cookies).userAgent("Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/42.0.2311.90 Safari/537.36")
                                .referrer("none").followRedirects(true).get();
                        String details = document2.select("#details").first().outerHtml();
                        if (!details.contains("Русский")) continue;
                    }

                }

                if (entity.getType().equals("movie")){
                    if (!entity.getYear().equals(""))
                        if (!title.contains(entity.getYear())) continue;

                    if (!entity.getQuality().equals("Любое"))
                        if (!titleInLowerCase.contains("bdrip") && !titleInLowerCase.contains(entity.getQuality().toLowerCase())) continue;

                    if (entity.getGoodSound().equals("1"))
                        if (!titleInLowerCase.contains("лицензия")) continue;
                }

                if (entity.getType().equals("serial")){
                    String season = entity.getSeason();
                    if (season.length() == 1) season = 0 + season;
                    if (!title.contains("[S" + season + "]")) {
                        try {
                            String[] arr = title.replace("[", "~").replace("]","~").split("~");
                            if (arr.length == 3){
                                String info = arr[1];
                                String[] arr2 = info
                                        .replaceAll(" ", "")
                                        .replace("из", "~")
                                        .replace("x", "~")
                                        .replace("-", "~")
                                        .split("~");

                                int index;
                                if (arr2.length == 4)
                                    index = 2;
                                else if (arr2.length == 3)
                                    index = 1;
                                else continue;

                                for(String s: arr2){
                                    if (s.charAt(0) == '0') s = s.substring(1, s.length());
                                }
                                int curSeason = Integer.parseInt(arr2[0]);
                                int entSeason = Integer.parseInt(entity.getSeason());
                                int curEpisode = Integer.parseInt(arr2[index]);
                                int entEpisode = Integer.parseInt(entity.getEpisode());

                                Log.w("DEV_", "Номер сезона: " + curSeason + ", номер эпизода: " + curEpisode);

                                if (curSeason != entSeason || curEpisode < entEpisode) continue;


                            } else continue;
                        }catch (Exception e) {continue;}
                    } else {
                        Log.w("DEV_", "Запись содержит [S" + season + "]");
                    }




                    Log.w("DEV_", "Проверка студий озвучки...");
                    if (entity.getSoundStudios().size() > 0) {
                        boolean flag = true;
                        for (String studio : entity.getSoundStudios())
                            if (titleInLowerCase.contains(studio.toLowerCase())) {
                                flag = false;
                                break;
                            }
                        if (flag) continue;
                    }
                }

                result = true;
                break;
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