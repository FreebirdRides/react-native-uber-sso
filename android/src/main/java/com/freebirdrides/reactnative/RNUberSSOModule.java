
package com.freebirdrides.reactnative;

import com.facebook.react.bridge.ActivityEventListener;
import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;

import com.uber.sdk.android.core.auth.AccessTokenManager;
import com.uber.sdk.android.core.auth.AuthenticationError;
import com.uber.sdk.android.core.auth.LoginButton;
import com.uber.sdk.android.core.auth.LoginCallback;
import com.uber.sdk.android.core.auth.LoginManager;
import com.uber.sdk.android.core.UberSdk;
import com.uber.sdk.core.auth.AccessToken;
import com.uber.sdk.core.auth.AccessTokenStorage;
import com.uber.sdk.core.auth.Scope;
import com.uber.sdk.core.client.Session;
import com.uber.sdk.core.client.SessionConfiguration;
import com.uber.sdk.rides.client.UberRidesApi;
import com.uber.sdk.rides.client.error.ApiError;
import com.uber.sdk.rides.client.error.ErrorParser;
import com.uber.sdk.rides.client.model.UserProfile;
import com.uber.sdk.rides.client.services.RidesService;

import android.app.Activity;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import static com.freebirdrides.reactnative.RNUberSSOConstants.*;

public class RNUberSSOModule extends ReactContextBaseJavaModule implements ActivityEventListener {

  private static final String LOG_TAG = "RNUberSSOModule";
  private static final int LOGIN_BUTTON_CUSTOM_REQUEST_CODE = 1112;
  private static final int CUSTOM_BUTTON_REQUEST_CODE = 1113;

  private final ReactApplicationContext reactContext;

  private AccessTokenStorage accessTokenStorage;
  private LoginManager loginManager;
  private SessionConfiguration configuration;
  private boolean isDebug;

  public RNUberSSOModule(ReactApplicationContext reactContext) {
    super(reactContext);
    this.reactContext = reactContext;
    this.isDebug = false;
    this.reactContext.addActivityEventListener(this);
  }

  @Override
  public String getName() {
    return "RNUberSSO";
  }

  /**
   * PUBLIC REACT API
   * 
   * initSdk() 
   */
  @ReactMethod
  public void initSdk(ReadableMap _options, Callback successCallback, Callback errorCallback) {

    String clientId = null;
    String environment = null;
    String redirectUri = null;

    try {

      JSONObject options = RNUtil.readableMapToJson(_options);

      clientId = options.optString(uberClientId, "");

      if (clientId == null || clientId.trim().equals("")) {
        errorCallback.invoke(new Exception("Uber option clientId not found.").getMessage());
        return;
      }

      redirectUri = options.optString(uberRedirectUri, "");

      if (redirectUri == null || redirectUri.trim().equals("")) {
        errorCallback.invoke(new Exception("Uber option redirectUri not found.").getMessage());
        return;
      }

      environment = options.optString(uberEnvironment, "sandbox");

      configuration = new SessionConfiguration.Builder()
        .setClientId(clientId)
        .setEnvironment(environment == "sandbox" ? SessionConfiguration.Environment.SANDBOX : SessionConfiguration.Environment.PRODUCTION)
        .setRedirectUri(redirectUri)
        .setScopes(Arrays.asList(
          Scope.HISTORY, 
          Scope.PLACES, 
          Scope.PROFILE, 
          Scope.ALL_TRIPS,
          Scope.REQUEST_RECEIPT,
          Scope.REQUEST
        )).build();

      // validateConfiguration(configuration);

      UberSdk.initialize(configuration);

      accessTokenStorage = new AccessTokenManager(reactContext.getCurrentActivity());

      // Use a custom button with an onClickListener to call the LoginManager directly
      loginManager = new LoginManager(accessTokenStorage, new SampleLoginCallback(), configuration,
          CUSTOM_BUTTON_REQUEST_CODE);

      isDebug = options.optBoolean(uberIsDebug, false);

      if (isDebug) {
        Log.d("UberSDK", "SSO setup complete");
      }

      successCallback.invoke(SUCCESS);
    } catch (Exception e) {
      errorCallback.invoke(e.getMessage());
      return;
    }
  }

  @ReactMethod
  public void login() {
    if (loginManager) {
      loginManager.login(reactContext.getCurrentActivity());
    }
  }

  /*
   * Called when the OAuth browser activity completes
   */
  @Override
  public void onActivityResult(Activity activity, int requestCode, int resultCode, Intent data) {
    String message = String.format("onActivityResult requestCode: [%s] resultCode [%s]", requestCode, resultCode);
    if (isDebug) {
      Log.i(LOG_TAG, message);
      Toast.makeText(reactContext.getCurrentActivity(), message, Toast.LENGTH_LONG).show();
    }
    // Allow each a chance to catch it.
    if (loginManager) {
      loginManager.onActivityResult(activity, requestCode, resultCode, data);
    }
  }

  @Override
  public void onNewIntent(Intent intent) {

  }

  private class SampleLoginCallback implements LoginCallback {

    @Override
    public void onLoginCancel() {
      String message = "User cancels login";
      if (isDebug) {
        Log.i(LOG_TAG, message);
        Toast.makeText(reactContext.getCurrentActivity(), message, Toast.LENGTH_LONG).show();
      }
      handleError(uberOnSSOFailure, message);
    }

    @Override
    public void onLoginError(@NonNull AuthenticationError error) {
      String message = String.format("Error occured during login: [%s]", error.name());
      if (isDebug) {
        Log.i(LOG_TAG, message);
        Toast.makeText(reactContext.getCurrentActivity(), message, Toast.LENGTH_LONG).show();
      }
      handleError(uberOnSSOFailure, error.name());
    }

    @Override
    public void onLoginSuccess(@NonNull AccessToken accessToken) {
      String message = String.format("Login successful with accessToken: [%s]", accessToken.getToken());
      if (isDebug) {
        Log.i(LOG_TAG, message);
        Toast.makeText(reactContext.getCurrentActivity(), message, Toast.LENGTH_LONG).show();
      }
      handleSuccess(uberOnSSOSuccess, accessToken);
    }

    @Override
    public void onAuthorizationCodeReceived(@NonNull String authorizationCode) {
      String message = String.format("Received an authorization code:\n [%s]", authorizationCode);
      if (isDebug) {
        Log.i(LOG_TAG, message);
        Toast.makeText(reactContext.getCurrentActivity(), message, Toast.LENGTH_LONG).show();
      }
    }

    private void handleSuccess(String eventType, @NonNull AccessToken accessToken) {
      JSONObject obj = new JSONObject();

      Map<String,String> data = new HashMap<String,String>();
      data.put("accessToken", accessToken.getToken());
      data.put("refreshToken", accessToken.getRefreshToken());

      try {
        obj.put("status", uberSuccess);
        obj.put("type", eventType);
        obj.put("data", new JSONObject(data));
        sendEvent(reactContext, uberOnSSOAccessToken, obj.toString());
      } catch (JSONException e) {
        e.printStackTrace();
      }
    }

    private void handleError(String eventType, String errorMessage) {
      JSONObject obj = new JSONObject();

      try {
        obj.put("status", uberFailure);
        obj.put("type", eventType);
        obj.put("data", errorMessage);
        sendEvent(reactContext, uberOnSSOAccessToken, obj.toString());
      } catch (JSONException e) {
        e.printStackTrace();
      }
    }

    private void sendEvent(ReactContext reactContext, String eventName, Object params) {
      reactContext.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class).emit(eventName, params);
    }
  }
}