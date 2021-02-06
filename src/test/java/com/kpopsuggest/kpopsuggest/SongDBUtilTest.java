package com.kpopsuggest.kpopsuggest;
import Model.Song;
import Utils.SongDBUtil;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RunWith(MockitoJUnitRunner.class)
public class SongDBUtilTest {

    SongDBUtil songDBUtil;
    Iterator<java.util.Map.Entry<String, Object>> testIterator;

    @Before
    public void setUp(){
        songDBUtil = new SongDBUtil();
        ConcurrentHashMap<String,Object> map = new ConcurrentHashMap<String, Object>();

        map.put("artistName","Lee Hi");
        map.put("length", new BigDecimal(1));
        map.put("link","https://www.youtube.com/watch?v=VdeK");
        map.put("name","HOLO");
        map.put("likes",new BigDecimal(10));
        map.put("songId",new BigDecimal(2100));
        testIterator = map.entrySet().iterator();
    }

    @Test
    public void artistNameIsSet(){
      Song testSong = songDBUtil.transferItem(testIterator,new Song());
      Assert.assertEquals("Lee Hi",testSong.getArtistName());
    }

    @Test
    public void lengthIsSet(){
        Song testSong = songDBUtil.transferItem(testIterator,new Song());
        Assert.assertEquals(1,testSong.getTimeLength());
    }

    @Test
    public void linkIsSet(){
        Song testSong = songDBUtil.transferItem(testIterator,new Song());
        Assert.assertEquals("https://www.youtube.com/watch?v=VdeK",testSong.getLink());
    }

    @Test
    public void namesIsSet(){
        Song testSong = songDBUtil.transferItem(testIterator,new Song());
        Assert.assertEquals("HOLO",testSong.getTitle());
    }

    @Test
    public void likesIsSet(){
        Song testSong = songDBUtil.transferItem(testIterator,new Song());
        Assert.assertEquals(10,testSong.getLikes());
    }

    @Test
    public void songIDIsSet(){
        Song testSong = songDBUtil.transferItem(testIterator,new Song());
        Assert.assertEquals(2100,testSong.getSongId());
    }
}
