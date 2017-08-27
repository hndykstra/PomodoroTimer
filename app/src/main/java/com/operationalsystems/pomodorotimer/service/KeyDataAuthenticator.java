package com.operationalsystems.pomodorotimer.service;

import android.accounts.AbstractAccountAuthenticator;
import android.accounts.Account;
import android.accounts.AccountAuthenticatorResponse;
import android.accounts.NetworkErrorException;
import android.content.Context;
import android.os.Bundle;

/**
 * Stub authenticator
 */

public class KeyDataAuthenticator extends AbstractAccountAuthenticator {

    KeyDataAuthenticator(Context context) {
        super(context);
    }
    /**
     * Not supported
     * @param response Not used.
     * @param accountType Not used.
     * @return Not used.
     */
    @Override
    public Bundle editProperties(AccountAuthenticatorResponse response, String accountType) {
        throw new UnsupportedOperationException("editProperties");
    }

    /**
     * Empty implementation.
     * @param response Not used.
     * @param accountType Not used.
     * @param authTokenType Not used.
     * @param requiredFeatures Not used.
     * @param options Not used.
     * @return Not used
     */
    @Override
    public Bundle addAccount(AccountAuthenticatorResponse response, String accountType, String authTokenType, String[] requiredFeatures, Bundle options) {
        return null;
    }

    /**
     * Ignored.
     * @param response Not used.
     * @param account Not used.
     * @param options Not used.
     * @return Not used.
     * @throws NetworkErrorException
     */
    @Override
    public Bundle confirmCredentials(AccountAuthenticatorResponse response, Account account, Bundle options) throws NetworkErrorException {
        return null;
    }

    /**
     * Not supported
     * @param response Not used.
     * @param account Not used.
     * @param authTokenType Not used.
     * @param options Not used.
     * @return Not used.
     */
    @Override
    public Bundle getAuthToken(AccountAuthenticatorResponse response, Account account, String authTokenType, Bundle options) {
        return null;
    }

    /**
     * Not supported
     * @param authTokenType Not used.
     * @return Not used.
     */
    @Override
    public String getAuthTokenLabel(String authTokenType) {
        throw new UnsupportedOperationException("getAuthTokenLabel");
    }

    /**
     * Not supported.
     * @param response Not used.
     * @param account Not used.
     * @param authTokenType Not used.
     * @param options Not used.
     * @return Not used.
     */
    @Override
    public Bundle updateCredentials(AccountAuthenticatorResponse response, Account account, String authTokenType, Bundle options) {
        return null;
    }

    /**
     * Not supported.
     * @param response Not used.
     * @param account Not used.
     * @param features Not used.
     * @return Not used.
     */
    @Override
    public Bundle hasFeatures(AccountAuthenticatorResponse response, Account account, String[] features) {
        return null;
    }
}
