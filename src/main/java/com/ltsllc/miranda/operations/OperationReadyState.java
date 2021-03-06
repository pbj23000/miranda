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

package com.ltsllc.miranda.operations;

import com.ltsllc.miranda.Consumer;
import com.ltsllc.miranda.Message;
import com.ltsllc.miranda.State;
import com.ltsllc.miranda.StopState;
import org.apache.log4j.Logger;

/**
 * Created by Clark on 4/16/2017.
 */
public class OperationReadyState extends State {
    private static Logger logger = Logger.getLogger(OperationReadyState.class);

    public OperationReadyState (Consumer consumer) {
        super (consumer);
    }

    public State processMessage (Message message) {
        logger.error (this + " does not understand " + message + ".  Terminating.", message.getWhere());
        return StopState.getInstance();
    }
}
