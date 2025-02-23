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
package com.hedera.services.evm.contracts.execution.traceability;

import org.hyperledger.besu.evm.frame.MessageFrame;
import org.hyperledger.besu.evm.tracing.OperationTracer;

public interface HederaEvmOperationTracer extends OperationTracer {

    @Override
    default void traceExecution(MessageFrame currentFrame, ExecuteOperation executeOperation) {
        executeOperation.execute();
    }

    /**
     * Perform initialization logic before EVM execution begins.
     *
     * @param initialFrame the initial frame associated with this EVM execution
     */
    default void init(final MessageFrame initialFrame) {}
}
