package com.ltsllc.miranda.subsciptions;

import com.google.gson.reflect.TypeToken;
import com.ltsllc.miranda.Message;
import com.ltsllc.miranda.Subscription;
import com.ltsllc.miranda.file.SingleFile;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;

/**
 * Created by Clark on 1/5/2017.
 */
public class SubscriptionsFile extends SingleFile<Subscription> {
    private static SubscriptionsFile ourInstance;

    public static SubscriptionsFile getInstance () {
        return ourInstance;
    }

    public static synchronized void initialize (String filename, BlockingQueue<Message> writerQueue) {
        if (null == ourInstance) {
            ourInstance = new SubscriptionsFile(writerQueue, filename);
            ourInstance.start();
            ourInstance.load();
        }
    }

    private SubscriptionsFile (BlockingQueue<Message> queue, String filename) {
        super(filename, queue);

        SubscriptionsFileReadyState subscriptionsFileReadyState = new SubscriptionsFileReadyState(this);
        setCurrentState(subscriptionsFileReadyState);
    }

    public Type getBasicType () {
        return new TypeToken<ArrayList<Subscription>>() {}.getType();
    }

    public List buildEmptyList () {
        return new ArrayList<Subscription>();
    }

    public Type listType () {
        return new TypeToken<ArrayList<Subscription>>(){}.getType();
    }

}