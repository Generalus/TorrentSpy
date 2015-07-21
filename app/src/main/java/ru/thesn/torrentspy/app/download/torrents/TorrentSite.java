package ru.thesn.torrentspy.app.download.torrents;

import ru.thesn.torrentspy.app.tools.BasicListAdapter;


public interface TorrentSite {
    boolean find(BasicListAdapter.Entity entity);
    boolean isServerAvailable();
    long getLastTime();
}
