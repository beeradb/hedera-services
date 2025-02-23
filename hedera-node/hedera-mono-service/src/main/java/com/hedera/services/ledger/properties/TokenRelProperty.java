/*
 * Copyright (C) 2020-2022 Hedera Hashgraph, LLC
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
package com.hedera.services.ledger.properties;

import com.hedera.services.state.migration.HederaTokenRel;
import java.util.function.BiConsumer;
import java.util.function.Function;

public enum TokenRelProperty implements BeanProperty<HederaTokenRel> {
    TOKEN_BALANCE {
        @Override
        public BiConsumer<HederaTokenRel, Object> setter() {
            return (a, l) -> a.setBalance((long) l);
        }

        @Override
        public Function<HederaTokenRel, Object> getter() {
            return HederaTokenRel::getBalance;
        }
    },
    IS_FROZEN {
        @Override
        public BiConsumer<HederaTokenRel, Object> setter() {
            return (a, f) -> a.setFrozen((boolean) f);
        }

        @Override
        public Function<HederaTokenRel, Object> getter() {
            return HederaTokenRel::isFrozen;
        }
    },
    IS_KYC_GRANTED {
        @Override
        public BiConsumer<HederaTokenRel, Object> setter() {
            return (a, f) -> a.setKycGranted((boolean) f);
        }

        @Override
        public Function<HederaTokenRel, Object> getter() {
            return HederaTokenRel::isKycGranted;
        }
    },
    IS_AUTOMATIC_ASSOCIATION {
        @Override
        public BiConsumer<HederaTokenRel, Object> setter() {
            return (a, f) -> a.setAutomaticAssociation((boolean) f);
        }

        @Override
        public Function<HederaTokenRel, Object> getter() {
            return HederaTokenRel::isAutomaticAssociation;
        }
    },
}
