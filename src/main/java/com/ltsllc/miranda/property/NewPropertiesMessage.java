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

package com.ltsllc.miranda.property;

import com.ltsllc.miranda.Message;

import java.util.concurrent.BlockingQueue;

/**
 * Created by Clark on 3/19/2017.
 */
public class NewPropertiesMessage extends Message {
    private MirandaProperties mirandaProperties;

    public MirandaProperties getMirandaProperties() {
        return mirandaProperties;
    }

    public NewPropertiesMessage (BlockingQueue<Message> senderQueue, Object sender, MirandaProperties mirandaProperties) {
        super(Subjects.NewProperties, senderQueue, sender);

        this.mirandaProperties = mirandaProperties;
    }
}
