
package com.freebirdrides.reactnative;

import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.Callback;

import com.uber.sdk.android.core.auth.AccessTokenManager;
import com.uber.sdk.android.core.auth.AuthenticationError;
import com.uber.sdk.android.core.auth.LoginButton;
import com.uber.sdk.android.core.auth.LoginCallback;
import com.uber.sdk.android.core.auth.LoginManager;
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

import android.util.Log;
import android.widget.Toast;

import java.util.Arrays;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RNUberSSOModule extends ReactContextBaseJavaModule {

  private static final int LOGIN_BUTTON_CUSTOM_REQUEST_CODE = 1112;
  private static final int CUSTOM_BUTTON_REQUEST_CODE = 1113;

  private Application application;
  private final ReactApplicationContext reactContext;

  private AccessTokenStorage accessTokenStorage;
  private LoginManager loginManager;
  private SessionConfiguration configuration;
  private boolean isDebug;

  public RNUberSSOModule(ReactApplicationContext reactContext, Application application) {
    super(reactContext);
    this.reactContext = reactContext;
    this.application = application;
    this.isDebug = false;
  }

  @Override
  public String getName() {
    return "RNUberSSO";
  }

  /**
   * PUBLIC REACT API
   * 
   * isAvailable() Returns true if the fingerprint reader can be used
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

      environment = options.optString(environment, "sandbox");

      configuration = new SessionConfiguration.Builder()
        .setClientId(clientId)
        .setEnvironment(environment == "sandbox" ? Environment.SANDBOX : Environment.PRODUCTION)
        .setRedirectUri(redirectUri)
        .setScopes(Arrays.asList(Scope.PROFILE, Scope.RIDE_WIDGETS)).build();

      validateConfiguration(configuration);

      accessTokenStorage = new AccessTokenManager(this);

      // Use a custom button with an onClickListener to call the LoginManager directly
      loginManager = new LoginManager(accessTokenStorage, new SampleLoginCallback(), configuration,
          CUSTOM_BUTTON_REQUEST_CODE);

      this.isDebug = options.optBoolean(uberIsDebug, false);

      if (isDebug == true) {
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
    loginManager.login(reactContext.currentActivity);
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    Log.i(LOG_TAG, String.format("onActivityResult requestCode:[%s] resultCode [%s]", requestCode, resultCode));

    // Allow each a chance to catch it.
    loginManager.onActivityResult(this, requestCode, resultCode, data);
  }

  private class SampleLoginCallback implements LoginCallback {

    @Override
    public void onLoginCancel() {
      Log.i(LOG_TAG, "User cancels login");
    }

    @Override
    public void onLoginError(@NonNull AuthenticationError error) {
      if (this.isDebug) {
        Log.i(LOG_TAG, getString("Error occured during login: %s", error.name())); 
      }
      handleError(uberOnSSOFailure, error.name());
    }

    @Override
    public void onLoginSuccess(@NonNull AccessToken accessToken) {
      if (this.isDebug) {
        Log.i(LOG_TAG, getString("Login successful with accessToken: %s", accessToken.getString()));
      }
      handleSuccess(uberOnSSOSuccess, accessToken.getString());
    }

    @Override
    public void onAuthorizationCodeReceived(@NonNull String authorizationCode) {
      if (this.isDebug) {
        Log.i(LOG_TAG, getString("Received an authorization code:\n %s", authorizationCode));
      }
    }

    private void handleSuccess(String eventType, String accessToken) {
      JSONObject obj = new JSONObject();

      try {
        obj.put("status", uberSuccess);
        obj.put("type", eventType);
        obj.put("data", accessToken);
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