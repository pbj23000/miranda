/*
 * Copyright 2017 Long Term Software LLC
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ltsllc.miranda.topics.messages;

import com.ltsllc.miranda.Message;
import com.ltsllc.miranda.clientinterface.Results;
import com.ltsllc.miranda.clientinterface.basicclasses.Topic;

import java.util.concurrent.BlockingQueue;

/**
 * Created by Clark on 4/9/2017.
 */
public class GetTopicResponseMessage extends Message {
    private Results result;
    private Topic topic;

    public Topic getTopic() {
        return topic;
    }

    public Results getResult() {
        return result;
    }

    public GetTopicResponseMessage (BlockingQueue<Message> senderQueue, Object sender, Results result, Topic topic) {
        super(Subjects.GetTopicResponse, senderQueue, sender);

        this.result = result;
        this.topic = topic;
    }
}
