/*
 * Copyright (C) 2022 Hedera Hashgraph, LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.hedera.node.app.service.schedule;

import com.hedera.node.app.spi.PreTransactionHandler;
import com.hedera.node.app.spi.meta.TransactionMetadata;
import com.hederahashgraph.api.proto.java.TransactionBody;

/**
 * The pre-handler for the HAPI <a
 * href="https://github.com/hashgraph/hedera-protobufs/blob/main/services/schedule_service.proto">Schedule
 * Service</a>.
 */
public interface SchedulePreTransactionHandler extends PreTransactionHandler {
    /**
     * Pre-handles a {@link com.hederahashgraph.api.proto.java.HederaFunctionality#ScheduleCreate}
     * transaction, returning the metadata required to, at minimum, validate the signatures of all
     * required signing keys.
     *
     * @param txn a transaction with a {@link
     *     com.hederahashgraph.api.proto.java.ScheduleCreateTransactionBody}
     * @return the metadata for the schedule creation
     */
    TransactionMetadata preHandleCreateSchedule(TransactionBody txn);

    /**
     * Pre-handles a {@link com.hederahashgraph.api.proto.java.HederaFunctionality#ScheduleSign}
     * transaction, returning the metadata required to, at minimum, validate the signatures of all
     * required signing keys.
     *
     * @param txn a transaction with a {@link
     *     com.hederahashgraph.api.proto.java.ScheduleSignTransactionBody}
     * @return the metadata for the schedule signing
     */
    TransactionMetadata preHandleSignSchedule(TransactionBody txn);

    /**
     * Pre-handles a {@link com.hederahashgraph.api.proto.java.HederaFunctionality#ScheduleDelete}
     * transaction, returning the metadata required to, at minimum, validate the signatures of all
     * required signing keys.
     *
     * @param txn a transaction with a {@link
     *     com.hederahashgraph.api.proto.java.ScheduleDeleteTransactionBody}
     * @return the metadata for the schedule deletion
     */
    TransactionMetadata preHandleDeleteSchedule(TransactionBody txn);
}
