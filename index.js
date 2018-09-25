import { NativeModules } from 'react-native'
import { NativeAppEventEmitter } from 'react-native'

const { RNUberSSO } = NativeModules

const uber = {}

const eventsMap = {}

uber.initSdk = (options, successCallback, errorCallback) => {
  console.log(
    'react-native-uber-sso initSdk onSSOUberSSOAccessToken is set?',
    !!eventsMap['onSSOUberSSOAccessToken']
  )
  // options.isDebug = eventsMap['isDebug']
  //   ? true
  //   : false
  return RNUberSSO.initSdk(options, successCallback, errorCallback)
}

uber.login = () => {
  console.log('react-native-uber-sso login')
  return RNUberSSO.login()
}

/**
 * Accessing Uber Attribution / Conversion Data from the SDK (Deferred Deeplinking)
 * @param callback: contains fields:
 *    status: success/failure
 *    type:
 *          onSSOUberSSOAccessToken
 *    data: metadata,
 * @example {"status":"success","type":"onSSOUberSSOAccessToken","data":"accessToken"}
 *
 * @returns {remove: function - unregister listener}
 */
uber.onSSOUberSSOAccessToken = callback => {
  console.log('onSSOUberSSOAccessToken is called')

  const listener = NativeAppEventEmitter.addListener(
    'onSSOUberSSOAccessToken',
    _data => {
      if (callback && typeof callback === typeof Function) {
        try {
          let data = JSON.parse(_data)
          console.log('onSSOUberSSOAccessToken with data', data)
          callback(data)
        } catch (_error) {
          //throw new UberParseJSONException("...");
          //TODO: for today we return an error in callback
          //callback(new UberParseJSONException('Invalid data structure', _data))
          callback(null)
        }
      }
    }
  )

  eventsMap['onSSOUberSSOAccessToken'] = listener

  // unregister listener (suppose should be called from componentWillUnmount() )
  return function remove() {
    listener.remove()
  }
}

function UberParseJSONException(_message, _data) {
  this.message = _message
  this.data = _data
  this.name = 'UberParseJSONException'
}

export default uber
