/*
 * Copyright (C) 2023 Hedera Hashgraph, LLC
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

package com.hedera.node.app.service.token.impl.test.handlers;

import static com.hedera.node.app.service.token.impl.test.handlers.util.StateBuilderUtil.TOKENS;
import static com.hedera.node.app.service.token.impl.test.handlers.util.StateBuilderUtil.TOKEN_RELS;
import static com.hedera.node.app.spi.fixtures.workflows.ExceptionConditions.responseCode;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mock.Strictness.LENIENT;
import static org.mockito.Mockito.when;

import com.hedera.hapi.node.base.AccountID;
import com.hedera.hapi.node.base.ContractID;
import com.hedera.hapi.node.base.QueryHeader;
import com.hedera.hapi.node.base.ResponseCodeEnum;
import com.hedera.hapi.node.base.ResponseHeader;
import com.hedera.hapi.node.base.TokenBalance;
import com.hedera.hapi.node.base.TokenID;
import com.hedera.hapi.node.state.token.Account;
import com.hedera.hapi.node.state.token.Token;
import com.hedera.hapi.node.state.token.TokenRelation;
import com.hedera.hapi.node.token.CryptoGetAccountBalanceQuery;
import com.hedera.hapi.node.token.CryptoGetAccountBalanceResponse;
import com.hedera.hapi.node.transaction.Query;
import com.hedera.hapi.node.transaction.Response;
import com.hedera.node.app.service.mono.utils.EntityNum;
import com.hedera.node.app.service.mono.utils.EntityNumPair;
import com.hedera.node.app.service.token.ReadableAccountStore;
import com.hedera.node.app.service.token.ReadableTokenRelationStore;
import com.hedera.node.app.service.token.ReadableTokenStore;
import com.hedera.node.app.service.token.impl.ReadableAccountStoreImpl;
import com.hedera.node.app.service.token.impl.ReadableTokenRelationStoreImpl;
import com.hedera.node.app.service.token.impl.ReadableTokenStoreImpl;
import com.hedera.node.app.service.token.impl.handlers.CryptoGetAccountBalanceHandler;
import com.hedera.node.app.service.token.impl.test.handlers.util.CryptoHandlerTestBase;
import com.hedera.node.app.spi.fixtures.state.MapReadableKVState;
import com.hedera.node.app.spi.state.ReadableStates;
import com.hedera.node.app.spi.workflows.PreCheckException;
import com.hedera.node.app.spi.workflows.QueryContext;
import com.hedera.node.config.testfixtures.HederaTestConfigBuilder;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CryptoGetAccountBalanceHandlerTest extends CryptoHandlerTestBase {

    @Mock(strictness = LENIENT)
    private QueryContext context;

    @Mock
    private Token token1, token2, token3;
    @Mock
    private ReadableStates readableStates1, readableStates2, readableStates3;

    private CryptoGetAccountBalanceHandler subject;

    @BeforeEach
    public void setUp() {
        super.setUp();
        subject = new CryptoGetAccountBalanceHandler();
    }

    @Test
    @DisplayName("Query header is extracted correctly")
    void extractsHeader() {
        final var query = createGetAccountBalanceQuery(accountNum);
        final var header = subject.extractHeader(query);
        final var op = query.cryptogetAccountBalance();
        assertEquals(op.header(), header);
    }

    @Test
    @DisplayName("Check empty query response is created correctly")
    void createsEmptyResponse() {
        final var responseHeader = ResponseHeader.newBuilder()
                .nodeTransactionPrecheckCode(ResponseCodeEnum.FAIL_FEE)
                .build();
        final var response = subject.createEmptyResponse(responseHeader);
        final var expectedResponse = Response.newBuilder()
                .cryptogetAccountBalance(
                        CryptoGetAccountBalanceResponse.newBuilder().header(responseHeader))
                .build();
        assertEquals(expectedResponse, response);
    }

    @Test
    @DisplayName("Validate query is successful with valid account")
    void validatesQueryWhenValidAccount() {
        givenValidAccount(accountNum);
        readableAccounts = emptyReadableAccountStateBuilder().value(id, account).build();
        given(readableStates.<AccountID, Account>get(ACCOUNTS)).willReturn(readableAccounts);
        readableStore = new ReadableAccountStoreImpl(readableStates);

        final var query = createGetAccountBalanceQuery(accountNum);
        given(context.query()).willReturn(query);
        given(context.createStore(ReadableAccountStore.class)).willReturn(readableStore);

        assertThatCode(() -> subject.validate(context)).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Validate query is successful with valid contract")
    void validatesQueryWhenValidContract() {
        givenValidContract();
        readableAccounts = emptyReadableAccountStateBuilder().value(id, account).build();
        given(readableStates.<AccountID, Account>get(ACCOUNTS)).willReturn(readableAccounts);
        readableStore = new ReadableAccountStoreImpl(readableStates);

        final var query = createGetAccountBalanceQueryWithContract(accountNum);
        given(context.query()).willReturn(query);
        given(context.createStore(ReadableAccountStore.class)).willReturn(readableStore);

        assertThatCode(() -> subject.validate(context)).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Empty account failed during validate")
    void validatesQueryIfEmptyAccount() throws Throwable {
        final var state =
                MapReadableKVState.<AccountID, Account>builder(ACCOUNTS).build();
        given(readableStates.<AccountID, Account>get(ACCOUNTS)).willReturn(state);
        final var store = new ReadableAccountStoreImpl(readableStates);

        final var query = createEmptyGetAccountBalanceQuery();

        when(context.query()).thenReturn(query);
        when(context.createStore(ReadableAccountStore.class)).thenReturn(store);

        assertThatThrownBy(() -> subject.validate(context))
                .isInstanceOf(PreCheckException.class)
                .has(responseCode(ResponseCodeEnum.INVALID_ACCOUNT_ID));
    }

    @Test
    @DisplayName("Account Id is needed during validate")
    void validatesQueryIfInvalidAccount() throws Throwable {
        final var state =
                MapReadableKVState.<AccountID, Account>builder(ACCOUNTS).build();
        given(readableStates.<AccountID, Account>get(ACCOUNTS)).willReturn(state);
        final var store = new ReadableAccountStoreImpl(readableStates);

        final var query = createGetAccountBalanceQuery(accountNum);
        when(context.query()).thenReturn(query);
        when(context.createStore(ReadableAccountStore.class)).thenReturn(store);

        assertThatThrownBy(() -> subject.validate(context))
                .isInstanceOf(PreCheckException.class)
                .has(responseCode(ResponseCodeEnum.INVALID_ACCOUNT_ID));
    }

    @Test
    @DisplayName("Contract Id is needed during validate")
    void validatesQueryIfInvalidContract() throws Throwable {
        final var state =
                MapReadableKVState.<AccountID, Account>builder(ACCOUNTS).build();
        given(readableStates.<AccountID, Account>get(ACCOUNTS)).willReturn(state);
        final var store = new ReadableAccountStoreImpl(readableStates);

        final var query = createGetAccountBalanceQueryWithContract(accountNum);
        when(context.query()).thenReturn(query);
        when(context.createStore(ReadableAccountStore.class)).thenReturn(store);

        assertThatThrownBy(() -> subject.validate(context))
                .isInstanceOf(PreCheckException.class)
                .has(responseCode(ResponseCodeEnum.INVALID_CONTRACT_ID));
    }

    @Test
    @DisplayName("deleted account is not valid")
    void validatesQueryIfDeletedAccount() throws Throwable {
        deleteAccount = deleteAccount.copyBuilder().deleted(true).build();
        readableAccounts = emptyReadableAccountStateBuilder()
                .value(deleteAccountId, deleteAccount)
                .build();
        given(readableStates.<AccountID, Account>get(ACCOUNTS)).willReturn(readableAccounts);
        readableStore = new ReadableAccountStoreImpl(readableStates);

        final var query = createGetAccountBalanceQuery(deleteAccountNum);
        when(context.query()).thenReturn(query);
        when(context.createStore(ReadableAccountStore.class)).thenReturn(readableStore);

        assertThatThrownBy(() -> subject.validate(context))
                .isInstanceOf(PreCheckException.class)
                .has(responseCode(ResponseCodeEnum.ACCOUNT_DELETED));
    }

    @Test
    @DisplayName("deleted contract is not valid")
    void validatesQueryIfDeletedContract() throws Throwable {
        deleteAccount =
                deleteAccount.copyBuilder().deleted(true).smartContract(true).build();
        readableAccounts = emptyReadableAccountStateBuilder()
                .value(deleteAccountId, deleteAccount)
                .build();
        given(readableStates.<AccountID, Account>get(ACCOUNTS)).willReturn(readableAccounts);
        readableStore = new ReadableAccountStoreImpl(readableStates);

        final var query = createGetAccountBalanceQueryWithContract(deleteAccountNum);
        when(context.query()).thenReturn(query);
        when(context.createStore(ReadableAccountStore.class)).thenReturn(readableStore);

        assertThatThrownBy(() -> subject.validate(context))
                .isInstanceOf(PreCheckException.class)
                .has(responseCode(ResponseCodeEnum.CONTRACT_DELETED));
    }

    @Test
    @DisplayName("failed response is correctly handled in findResponse")
    void getsResponseIfFailedResponse() {
        final var responseHeader = ResponseHeader.newBuilder()
                .nodeTransactionPrecheckCode(ResponseCodeEnum.INVALID_ACCOUNT_ID)
                .build();

        final var query = createGetAccountBalanceQuery(accountNum);
        when(context.query()).thenReturn(query);
        when(context.createStore(ReadableAccountStore.class)).thenReturn(readableStore);

        final var config = HederaTestConfigBuilder.create()
                .withValue("tokens.maxRelsPerInfoQuery", 1000)
                .getOrCreateConfig();
        given(context.configuration()).willReturn(config);

        final var response = subject.findResponse(context, responseHeader);
        final var op = response.cryptogetAccountBalance();
        assertEquals(ResponseCodeEnum.INVALID_ACCOUNT_ID, op.header().nodeTransactionPrecheckCode());
        assertNull(op.accountID());
    }

    @Test
    @DisplayName("OK response is correctly handled in findResponse")
    void getsResponseIfOkResponse() {
        givenValidAccount(accountNum);
        final var responseHeader = ResponseHeader.newBuilder()
                .nodeTransactionPrecheckCode(ResponseCodeEnum.OK)
                .build();
        final var expectedInfo = getExpectedInfo();

        final var readableAccounts = MapReadableKVState.<AccountID, Account>builder(ACCOUNTS)
                .value(id, account)
                .build();
        given(readableStates1.<AccountID, Account>get(ACCOUNTS)).willReturn(readableAccounts);
        ReadableAccountStore ReadableAccountStore = new ReadableAccountStoreImpl(readableStates1);

        given(token1.decimals()).willReturn(100);
        final var readableToken = MapReadableKVState.<EntityNum, Token>builder(TOKENS)
                .value(EntityNum.fromLong(3L), token1)
                .build();
        given(readableStates2.<EntityNum, Token>get(TOKENS)).willReturn(readableToken);
        final var readableTokenStore = new ReadableTokenStoreImpl(readableStates2);

        final var tokenRelation = TokenRelation.newBuilder()
                .tokenNumber(3L)
                .accountNumber(accountNum)
                .balance(1000L)
                .frozen(false)
                .kycGranted(false)
                .deleted(false)
                .automaticAssociation(true)
                .nextToken(4L)
                .previousToken(2L)
                .build();
        final var readableTokenRel = MapReadableKVState.<EntityNumPair, TokenRelation>builder(TOKEN_RELS)
                .value(EntityNumPair.fromLongs(3L, accountNum), tokenRelation)
                .build();
        given(readableStates3.<EntityNumPair, TokenRelation>get(TOKEN_RELS)).willReturn(readableTokenRel);
        final var readableTokenRelStore = new ReadableTokenRelationStoreImpl(readableStates3);

        final var query = createGetAccountBalanceQuery(accountNum);
        when(context.query()).thenReturn(query);
        when(context.createStore(ReadableAccountStore.class)).thenReturn(ReadableAccountStore);
        when(context.createStore(ReadableTokenStore.class)).thenReturn(readableTokenStore);
        when(context.createStore(ReadableTokenRelationStore.class)).thenReturn(readableTokenRelStore);

        final var config = HederaTestConfigBuilder.create()
                .withValue("tokens.maxRelsPerInfoQuery", 2)
                .getOrCreateConfig();
        given(context.configuration()).willReturn(config);

        final var response = subject.findResponse(context, responseHeader);
        final var accountBalanceResponse = response.cryptogetAccountBalance();
        assertEquals(ResponseCodeEnum.OK, accountBalanceResponse.header().nodeTransactionPrecheckCode());
        assertEquals(expectedInfo.tinybarBalance(), accountBalanceResponse.balance());
        assertIterableEquals(getExpectedTokenBalance(3L), accountBalanceResponse.tokenBalances());
    }

    @Test
    @DisplayName("check maxRelsPerInfoQuery in TokenConfig is correctly handled")
    void checkConfigmaxRelsPerInfoQuery() {
        givenValidAccount(accountNum);
        final var responseHeader = ResponseHeader.newBuilder()
                .nodeTransactionPrecheckCode(ResponseCodeEnum.OK)
                .build();
        final var expectedInfo = getExpectedInfo();

        final var readableAccounts = MapReadableKVState.<AccountID, Account>builder(ACCOUNTS)
                .value(id, account)
                .build();
        given(readableStates1.<AccountID, Account>get(ACCOUNTS)).willReturn(readableAccounts);
        ReadableAccountStore ReadableAccountStore = new ReadableAccountStoreImpl(readableStates1);

        given(token1.decimals()).willReturn(100);
        given(token2.decimals()).willReturn(50);
        final var readableToken = MapReadableKVState.<EntityNum, Token>builder(TOKENS)
                .value(EntityNum.fromLong(3L), token1)
                .value(EntityNum.fromLong(4L), token2)
                .value(EntityNum.fromLong(5L), token3)
                .build();
        given(readableStates2.<EntityNum, Token>get(TOKENS)).willReturn(readableToken);
        final var readableTokenStore = new ReadableTokenStoreImpl(readableStates2);

        final var tokenRelation1 = TokenRelation.newBuilder()
                .tokenNumber(3L)
                .accountNumber(accountNum)
                .balance(1000L)
                .frozen(false)
                .kycGranted(false)
                .deleted(false)
                .automaticAssociation(true)
                .nextToken(4L)
                .previousToken(2L)
                .build();
        final var tokenRelation2 = TokenRelation.newBuilder()
                .tokenNumber(4L)
                .accountNumber(accountNum)
                .balance(100L)
                .frozen(false)
                .kycGranted(false)
                .deleted(false)
                .automaticAssociation(true)
                .nextToken(5L)
                .previousToken(3L)
                .build();
        final var tokenRelation3 = TokenRelation.newBuilder()
                .tokenNumber(5L)
                .accountNumber(accountNum)
                .balance(10L)
                .frozen(false)
                .kycGranted(false)
                .deleted(false)
                .automaticAssociation(true)
                .nextToken(6L)
                .previousToken(4L)
                .build();
        final var readableTokenRel = MapReadableKVState.<EntityNumPair, TokenRelation>builder(TOKEN_RELS)
                .value(EntityNumPair.fromLongs(accountNum, 3L), tokenRelation1)
                .value(EntityNumPair.fromLongs(accountNum, 4L), tokenRelation2)
                .value(EntityNumPair.fromLongs(accountNum, 5L), tokenRelation3)
                .build();
        given(readableStates3.<EntityNumPair, TokenRelation>get(TOKEN_RELS)).willReturn(readableTokenRel);
        final var readableTokenRelStore = new ReadableTokenRelationStoreImpl(readableStates3);

        final var query = createGetAccountBalanceQuery(accountNum);
        when(context.query()).thenReturn(query);
        when(context.createStore(ReadableAccountStore.class)).thenReturn(ReadableAccountStore);
        when(context.createStore(ReadableTokenStore.class)).thenReturn(readableTokenStore);
        when(context.createStore(ReadableTokenRelationStore.class)).thenReturn(readableTokenRelStore);

        final var config = HederaTestConfigBuilder.create()
                .withValue("tokens.maxRelsPerInfoQuery", 2)
                .getOrCreateConfig();
        given(context.configuration()).willReturn(config);

        final var response = subject.findResponse(context, responseHeader);
        final var accountBalanceResponse = response.cryptogetAccountBalance();
        assertEquals(ResponseCodeEnum.OK, accountBalanceResponse.header().nodeTransactionPrecheckCode());
        assertEquals(expectedInfo.tinybarBalance(), accountBalanceResponse.balance());
        assertIterableEquals(getExpectedTokenBalances(), accountBalanceResponse.tokenBalances());
        assertEquals(2, accountBalanceResponse.tokenBalances().size());
    }

    private Account getExpectedInfo() {
        return Account.newBuilder()
                .accountNumber(accountNum)
                .tinybarBalance(payerBalance)
                .build();
    }

    private List<TokenBalance> getExpectedTokenBalance(long tokenNum) {
        var ret = new ArrayList<TokenBalance>();
        final var tokenBalance = TokenBalance.newBuilder()
                .tokenId(TokenID.newBuilder().tokenNum(tokenNum).build())
                .balance(1000)
                .decimals(100)
                .build();
        ret.add(tokenBalance);
        return ret;
    }

    private List<TokenBalance> getExpectedTokenBalances() {
        var ret = new ArrayList<TokenBalance>();
        final var tokenBalance1 = TokenBalance.newBuilder()
                .tokenId(TokenID.newBuilder().tokenNum(3L).build())
                .balance(1000)
                .decimals(100)
                .build();
        final var tokenBalance2 = TokenBalance.newBuilder()
                .tokenId(TokenID.newBuilder().tokenNum(4L).build())
                .balance(100)
                .decimals(50)
                .build();
        ret.add(tokenBalance1);
        ret.add(tokenBalance2);
        return ret;
    }

    private Query createGetAccountBalanceQuery(final long accountId) {
        final var data = CryptoGetAccountBalanceQuery.newBuilder()
                .accountID(AccountID.newBuilder().accountNum(accountId).build())
                .header(QueryHeader.newBuilder().build())
                .build();

        return Query.newBuilder().cryptogetAccountBalance(data).build();
    }

    private Query createGetAccountBalanceQueryWithContract(final long contractId) {
        final var data = CryptoGetAccountBalanceQuery.newBuilder()
                .contractID(ContractID.newBuilder().contractNum(contractId).build())
                .header(QueryHeader.newBuilder().build())
                .build();

        return Query.newBuilder().cryptogetAccountBalance(data).build();
    }

    private Query createEmptyGetAccountBalanceQuery() {
        final var data = CryptoGetAccountBalanceQuery.newBuilder()
                .header(QueryHeader.newBuilder().build())
                .build();

        return Query.newBuilder().cryptogetAccountBalance(data).build();
    }
}
